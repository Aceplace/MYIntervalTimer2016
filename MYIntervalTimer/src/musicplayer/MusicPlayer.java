/*
 * Project: MYIntervalTimer
 * Package: musicplayer
 * Name: MusicPlayer
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 26, 2016
 */
package musicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;
import script.ScriptEditorController;

class MediaPlayerEx {

    private MediaPlayer mediaPlayer;
    private ObjectProperty<Duration> duration = new SimpleObjectProperty(this, "duration", Duration.UNKNOWN);
    boolean stopRequested = true;
    BooleanProperty currentPlayingMedia = new SimpleBooleanProperty(this, "currentPlayingMedia", false);
    StringProperty name = new SimpleStringProperty(this, "name", null);

    public static Callback<MediaPlayerEx, Observable[]> extractor() {
        return new Callback<MediaPlayerEx, Observable[]>() {
            public Observable[] call(MediaPlayerEx param) {
                return new Observable[]{param.currentPlayingMedia, param.name};
            }

        };
    }

    public MediaPlayerEx(Media media) {
        mediaPlayer = new MediaPlayer(media);

        StringBuilder buildname = new StringBuilder();
        if (mediaPlayer.getMedia().getMetadata().containsKey("title")) {
            if (mediaPlayer.getMedia().getMetadata().containsKey("author")) {
                buildname.append(mediaPlayer.getMedia().getMetadata().get("author") + " - ");
            }
            buildname.append(mediaPlayer.getMedia().getMetadata().get("title"));
        }
        if (buildname.length() > 0) {
            name.set(buildname.toString());
        } else {
            name.set(new File(mediaPlayer.getMedia().getSource()).getName());
        }

        mediaPlayer.setOnReady(() -> {
            duration.set(mediaPlayer.getMedia().getDuration());

            StringBuilder sbuildname = new StringBuilder();
            if (mediaPlayer.getMedia().getMetadata().containsKey("title")) {
                if (mediaPlayer.getMedia().getMetadata().containsKey("author")) {
                    sbuildname.append(mediaPlayer.getMedia().getMetadata().get("author") + " - ");
                }
                sbuildname.append(mediaPlayer.getMedia().getMetadata().get("title"));
            }
            if (sbuildname.length() > 0) {
                name.set(sbuildname.toString());
            } else {
                name.set(new File(mediaPlayer.getMedia().getSource()).getName());
            }
        });

        mediaPlayer.currentTimeProperty().addListener((timeProperty, oldValue, newValue) -> {
            if (stopRequested) {
                mediaPlayer.stop();
            }
        });
    }

    //getters and setters
    //<editor-fold>
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public Duration getDuration() {
        return duration.get();
    }

    public void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void setStopRequested(boolean stopRequested) {
        this.stopRequested = stopRequested;
    }

    public final void setCurrentPlayingMedia(Boolean value) {
        currentPlayingMedia.set(value);
    }

    public final Boolean getCurrentPlayingMedia() {
        return currentPlayingMedia.get();
    }

    public final BooleanProperty currentPlayingMediaProperty() {
        return currentPlayingMedia;

    }

    public final void setName(String value) {
        name.set(value);
    }

    public final String getName() {
        return name.get();
    }

    public final StringProperty nameProperty() {
        return name;
    }

    //</editor-fold>
}

public class MusicPlayer extends VBox {

    //data format so list view can only drag amongst itself
    private static final DataFormat dataFormat = new DataFormat("com.coachyeiter.musicplayer.playList.cellFactory");

    ListView<MediaPlayerEx> playList;
    ObservableList<MediaPlayerEx> mediaPlayers = FXCollections.observableArrayList(MediaPlayerEx.extractor());

    ObjectProperty<MediaPlayerEx> currentMediaPlayer = new SimpleObjectProperty<MediaPlayerEx>(this, "currentMediaPlayerEx", null);
    IntegerProperty currentIndex = new SimpleIntegerProperty(this, "currentIndex", -1);
    private ObjectProperty<Duration> duration = new SimpleObjectProperty(this, "duration", Duration.UNKNOWN);

    //volume amount to allow outsider to mess with volume
    double volume;
    MusicFader musicFader = new MusicFader(this);

    //controls for the player
    private Label timeLbl;
    private Slider timeSlider;

    private Button previousBtn, nextBtn, pauseBtn, restartBtn, shuffleBtn;
    private Label volumeLbl;
    private Slider volumeSlider;

    //change listener for play/pause button
    //need the reference here so it can be removed from mediaPlayers after they are no longer playing
    //and attached to the new media that is playing
    private ChangeListener<MediaPlayer.Status> currentMediaStatusChangeListener = new ChangeListener<MediaPlayer.Status>() {
        public void changed(ObservableValue<? extends MediaPlayer.Status> observable, MediaPlayer.Status oldValue, MediaPlayer.Status newValue) {
            Image playIconImage;
            ImageView playIconView;
            if (newValue == Status.PLAYING || newValue == Status.STALLED) {
                playIconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/pause.png"));
                playIconView = new ImageView(playIconImage);
                playIconView.setFitHeight(32);
                playIconView.setFitWidth(32);
                pauseBtn.setGraphic(playIconView);
            } else {
                playIconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/play.png"));
                playIconView = new ImageView(playIconImage);
                playIconView.setFitHeight(32);
                playIconView.setFitWidth(32);
                pauseBtn.setGraphic(playIconView);
            }
        }
    };

    //change listener to update time slider amound. need references so they can be removed and added when the media file
    //changes
    private ChangeListener<Duration> currentMediaTimeListener = new ChangeListener<Duration>() {
        public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
            updateTimeLabel();
            timeSlider.setValue(newValue.toMillis());
        }
    };

    public MusicPlayer() {
        this.getStylesheets().add(MusicPlayer.class.getClassLoader().getResource("musicplayer/musicplayer.css").toString());

        currentMediaPlayer.addListener((mediaPlayerExProperty, oldValue, newValue) -> {
            if (oldValue != null) {
                //remove relevant listener to play/pause and automatically stop old media
                oldValue.getMediaPlayer().statusProperty().removeListener(currentMediaStatusChangeListener);
                oldValue.getMediaPlayer().currentTimeProperty().removeListener(currentMediaTimeListener);
                duration.unbind();
                duration.set(Duration.UNKNOWN);
                oldValue.setStopRequested(true);
                oldValue.getMediaPlayer().stop();
                oldValue.setCurrentPlayingMedia(false);
            }
            if (newValue != null) {
                //now listen to the relevant changes for the new media 
                newValue.getMediaPlayer().statusProperty().addListener(currentMediaStatusChangeListener);
                newValue.getMediaPlayer().currentTimeProperty().addListener(currentMediaTimeListener);
                duration.bind(newValue.durationProperty());
                newValue.setStopRequested(false);
                newValue.getMediaPlayer().seek(newValue.getMediaPlayer().getStartTime());
                newValue.getMediaPlayer().play();
                newValue.setCurrentPlayingMedia(true);
                setVolume(getVolume());
            }
        });
        createLayout();
    }

    public void createLayout() {

        createListViewLayout();
        createButtonLayout();

    }

    public void createListViewLayout() {

        playList = new ListView<MediaPlayerEx>(mediaPlayers);
        playList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        createListDragAndDropFeatures();

        playList.setCellFactory(listView -> {

            return new ListCell<MediaPlayerEx>() {

                {

                    setOnMouseClicked(e -> {
                        if (this.getItem() == null) {
                            return;
                        }

                        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                            currentMediaPlayer.set(this.getItem());
                        }
                    });

                    setOnDragDetected(e -> {
                        if (this.getItem() == null) {
                            return;
                        }

                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        Integer[] indices = playList.getSelectionModel().getSelectedIndices().toArray(new Integer[playList.getSelectionModel().getSelectedIndices().size()]);
                        Arrays.sort(indices);
                        content.put(dataFormat, indices);
                        db.setContent(content);

                        StringBuilder dragViewString = new StringBuilder();
                        for (int index : indices) {
                            dragViewString.append(mediaPlayers.get(index).getName() + "\n");
                        }
                        db.setDragView(new Text(dragViewString.toString()).snapshot(null, null));

                        e.consume();

                    });

                    setOnDragOver(e -> {
                        if (e.getGestureSource() != this && e.getDragboard().hasContent(dataFormat)) {
                            e.acceptTransferModes(TransferMode.MOVE);
                        }
                    });

                    setOnDragEntered(e -> {
                        if (e.getGestureSource() != this && this.getItem() != null && e.getDragboard().hasContent(dataFormat)) {
                            this.setOpacity(0.3);
                        }
                    });

                    setOnDragExited(e -> {
                        if (e.getGestureSource() != this && e.getDragboard().hasContent(dataFormat)) {
                            this.setOpacity(1.0);
                        }
                    });

                    setOnDragDropped(e -> {
                        boolean success = false;

                        if (e.getDragboard().hasContent(dataFormat)) {

                            Integer[] draggedIndices = (Integer[]) e.getDragboard().getContent(dataFormat);
                            MediaPlayerEx thisMediaPlayer;
                            if (this.getItem() != null) {
                                thisMediaPlayer = mediaPlayers.get(this.getIndex());
                            } else {
                                thisMediaPlayer = null;
                            }

                            ArrayList<MediaPlayerEx> tempMedias = new ArrayList();
                            MediaPlayerEx tempMedia;
                            for (int index : draggedIndices) {
                                tempMedia = mediaPlayers.get(index);
                                if (thisMediaPlayer == tempMedia) {
                                    e.setDropCompleted(success);
                                    e.consume();
                                    return;
                                }
                                tempMedias.add(tempMedia);
                            }

                            mediaPlayers.removeAll(tempMedias);
                            if (thisMediaPlayer != null) {
                                mediaPlayers.addAll(mediaPlayers.indexOf(thisMediaPlayer), tempMedias);
                            } else {
                                mediaPlayers.addAll(tempMedias);
                            }

                            playList.getSelectionModel().clearSelection();
                            playList.getSelectionModel().selectRange(mediaPlayers.indexOf(tempMedias.get(0)),
                                    mediaPlayers.indexOf(tempMedias.get(tempMedias.size() - 1)) + 1);

                            success = true;
                        }
                        e.setDropCompleted(success);
                        e.consume();

                    });

                    setOnDragDone(e -> e.consume());

                }

                protected void updateItem(MediaPlayerEx item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        this.setText(null);
                        this.setGraphic(null);
                    } else {

                        if (item.getCurrentPlayingMedia()) {
                            this.setId("selectedSong");
                        } else {
                            this.setId("notSelectedSong");
                        }

                        this.setText(item.nameProperty().get());

                    }

                }

            };
        });

        this.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
                if (playList.getSelectionModel().getSelectedIndices().size() > 0) {
                    Integer[] indices = playList.getSelectionModel().getSelectedIndices().toArray(new Integer[playList.getSelectionModel().getSelectedIndices().size()]);
                    Arrays.sort(indices);
                    int lowestIndex = indices[0];
                    mediaPlayers.removeAll(playList.getSelectionModel().getSelectedItems());
                    if (mediaPlayers.indexOf(currentMediaPlayer.get()) == -1)//if we deleted current playing media, then stop playback
                    {
                        currentMediaPlayer.set(null);
                    }
                    playList.getSelectionModel().clearAndSelect(lowestIndex);

                }
            }
            if (e.getCode() == KeyCode.F) {
                performFade(1000, 5000);
            }
        });

        playList.setPlaceholder(new Label("Drag and drop files to add to playlist...\nPress delete or backspace to removes file form playlist..."));

        this.getChildren().add(playList);
    }

    public void createListDragAndDropFeatures() {
        playList.setOnDragOver(dragEvent -> {
            if (dragEvent.getGestureSource() != this && dragEvent.getDragboard().hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
            dragEvent.consume();
        });

        playList.addEventFilter(DragEvent.DRAG_DROPPED, dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (db.hasFiles()) {
                loadFiles(db.getFiles());
                dragEvent.setDropCompleted(true);
                dragEvent.consume();
            }
        });
    }

    public void loadFiles(List<File> files) {
        for (File file : files) {
            //attempt to load file into a media
            //if successful, add it to the playlist
            Media tempMedia;
            if (file.isFile()) {
                try {
                    tempMedia = new Media(file.toURI().toString());
                    if (tempMedia.getError() == null) {
                        mediaPlayers.add(new MediaPlayerEx(tempMedia));
                        mediaPlayers.get(mediaPlayers.size() - 1).getMediaPlayer().setOnEndOfMedia(() -> {
                            nextMedia();
                        });
                        mediaPlayers.get(mediaPlayers.size() - 1).getMediaPlayer().setVolume(volume);
                    }
                } catch (Exception e) {
                }
            }

        }
        if (mediaPlayers.size() > 0 && playList.getSelectionModel().getSelectedIndex() == -1) {
            playList.getSelectionModel().selectFirst();
            currentMediaPlayer.set(playList.getSelectionModel().getSelectedItem());
            currentMediaPlayer.get().getMediaPlayer().play();
        }

    }

    public void createButtonLayout() {
        /*
        time slider
         */
        timeLbl = new Label();

        timeSlider = new Slider(0, 1.0, 0);
        //timeSlider.setMajorTickUnit(25);
        //timeSlider.setShowTickMarks(true);

        HBox timeBox = new HBox(timeLbl, timeSlider);
        HBox.setHgrow(this, Priority.ALWAYS);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);

        duration.addListener((durationProperty, oldValue, newValue) -> {
            timeSlider.setMax(newValue.toMillis());
        });

        timeSlider.setOnMousePressed(e -> {
            if (currentMediaPlayer.get() != null) {
                currentMediaPlayer.get().getMediaPlayer().seek(new Duration(timeSlider.getValue()));
            }
        });

        timeSlider.setOnMouseDragged(e -> {
            if (currentMediaPlayer.get() != null) {
                currentMediaPlayer.get().getMediaPlayer().seek(new Duration(timeSlider.getValue()));
            }
        });

        this.getChildren().add(timeBox);

        updateTimeLabel();
        /*
        button controls for playback
         */
        Image iconImage;
        ImageView iconView;
        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/previous.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        previousBtn = new Button();
        previousBtn.setOnMouseEntered(e -> {
            previousBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        previousBtn.setOnMouseExited(e -> {
            previousBtn.setEffect(null);
        });
        previousBtn.setGraphic(iconView);
        previousBtn.setOnAction(e -> {
            previousMedia();
        });

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/next.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        nextBtn = new Button();
        nextBtn.setOnMouseEntered(e -> {
            nextBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        nextBtn.setOnMouseExited(e -> {
            nextBtn.setEffect(null);
        });
        nextBtn.setGraphic(iconView);
        nextBtn.setOnAction(e -> {
            nextMedia();
        });

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/play.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        pauseBtn = new Button();
        pauseBtn.setOnMouseEntered(e -> {
            pauseBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        pauseBtn.setOnMouseExited(e -> {
            pauseBtn.setEffect(null);
        });
        pauseBtn.setGraphic(iconView);
        pauseBtn.setOnAction(e -> {
            pauseMedia();
        });

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/restartback.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        restartBtn = new Button();
        restartBtn.setOnMouseEntered(e -> {
            restartBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        restartBtn.setOnMouseExited(e -> {
            restartBtn.setEffect(null);
        });
        restartBtn.setGraphic(iconView);
        restartBtn.setOnAction(e -> {
            restartMedia();
        });

        shuffleBtn = new Button("Shuffle");
        shuffleBtn.setOnMouseEntered(e -> {
            shuffleBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        shuffleBtn.setOnMouseExited(e -> {
            shuffleBtn.setEffect(null);
        });
        shuffleBtn.setOnAction(e -> {
            if (playList.getItems().size() >= 2) {
                FXCollections.shuffle(playList.getItems(), new Random());
            }
        });

        Label volumeLabel = new Label("Volume: ");

        //volume slider behavior
        volumeSlider = new Slider(0.0, 1.0, 0.3);
        volume = volumeSlider.getValue();
        volumeSlider.valueProperty().addListener((valueProperty, oldValue, newValue) -> {
            /*volume = newValue.doubleValue();
            if (currentMediaPlayer.get() != null) {
                currentMediaPlayer.get().getMediaPlayer().setVolume(newValue.doubleValue());
            }*/
            setVolume(newValue.doubleValue());
        });
        volumeLbl = new Label("Volume: ");
        volumeLbl.setAlignment(Pos.CENTER);

        HBox playBackControls = new HBox(previousBtn, restartBtn, pauseBtn, nextBtn, shuffleBtn, volumeLbl, volumeSlider);
        playBackControls.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().add(playBackControls);

    }

    public double getVolume() {
        return volume;
    }

    public void updateTimeLabel() {
        if (!duration.get().equals(Duration.UNKNOWN) && currentMediaPlayer.get() != null) {
            int playedMinutes;
            int playedSeconds;
            int songMinutes;
            int songSeconds;

            int totalPlayedSeconds = (int) currentMediaPlayer.get().getMediaPlayer().getCurrentTime().toSeconds();
            playedMinutes = totalPlayedSeconds / 60;
            playedSeconds = totalPlayedSeconds % 60;
            int totalSongSeconds = (int) duration.get().toSeconds();
            songMinutes = totalSongSeconds / 60;
            songSeconds = totalSongSeconds % 60;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(playedMinutes + ":");
            stringBuilder.append(playedSeconds < 10 ? "0" + playedSeconds : playedSeconds);
            stringBuilder.append(" / ");
            stringBuilder.append(songMinutes + ":");
            stringBuilder.append(songSeconds < 10 ? "0" + songSeconds : songSeconds);
            stringBuilder.append(" ");

            timeLbl.setText(stringBuilder.toString());
        } else {
            timeLbl.setText("00:00 / 00:00 ");
        }
    }

    public void previousMedia() {
        if (mediaPlayers.size() > 1 && currentMediaPlayer.get() != null) {
            int currentIndex = mediaPlayers.indexOf(currentMediaPlayer.get());
            if (currentIndex == 0) {
                currentMediaPlayer.set(mediaPlayers.get(mediaPlayers.size() - 1));
            } else {
                currentMediaPlayer.set(mediaPlayers.get(currentIndex - 1));
            }
            setVolume(getVolume());
        }
    }

    public void nextMedia() {
        if (mediaPlayers.size() > 1 && currentMediaPlayer.get() != null) {
            int currentIndex = mediaPlayers.indexOf(currentMediaPlayer.get());
            if (currentIndex == mediaPlayers.size() - 1) {
                currentMediaPlayer.set(mediaPlayers.get(0));
            } else {
                currentMediaPlayer.set(mediaPlayers.get(currentIndex + 1));
            }
            setVolume(getVolume());
        }
    }

    public void pauseMedia() {
        if (currentMediaPlayer.get() != null) {
            if (currentMediaPlayer.get().getMediaPlayer().getStatus() != Status.PLAYING) {
                currentMediaPlayer.get().getMediaPlayer().play();
            } else {
                currentMediaPlayer.get().getMediaPlayer().pause();
            }
        }
    }

    public void restartMedia() {
        if (currentMediaPlayer.get() != null) {
            currentMediaPlayer.get().getMediaPlayer().seek(currentMediaPlayer.get().getMediaPlayer().getStartTime());
        }
    }

    public double getMaxVolume() {
        return volumeSlider.getValue();
    }

    public void setVolume(double volume) {
        /*this.volume = volume;
        if (currentMediaPlayer.get() != null) {
            currentMediaPlayer.get().getMediaPlayer().setVolume(this.volume);
        }*/
        this.volume = volume;
        //volumeSlider.setValue(volume);
        if (currentMediaPlayer.get() != null) {
            currentMediaPlayer.get().getMediaPlayer().setVolume(volume);
        }
    }

    public void setVolumeSlider(double volume) {
        volumeSlider.setValue(volume);
    }

    public void performFade(double fadeTimeMillis, double pauseTimeMillis) {
        musicFader.performFade(fadeTimeMillis, pauseTimeMillis);
    }

    public void haltFade() {
        musicFader.haltFade();
    }

    public boolean isFading() {
        return musicFader.isFading();
    }
}
