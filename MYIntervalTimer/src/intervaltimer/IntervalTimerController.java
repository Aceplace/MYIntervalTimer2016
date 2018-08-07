/*
 * Project: MYIntervalTimer
 * Package: intervaltimer
 * Name: IntervalTimerController
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
package intervaltimer;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import musicplayer.MusicPlayer;
import script.ScriptEditorController;

public class IntervalTimerController extends VBox {

    //music player reference needed so interval timer can lower volume to make announcements
    MusicPlayer musicPlayer;
    
    private Scene scene;
    
    private Label periodLabel;
    private Label timeLabel;
    private GridPane labelGrid;
    private Slider timeSlider;
    private Button previousBtn, nextBtn, pauseBtn, restartBtn;
    private Slider intervalTimerVolumeSlider;
    private Slider musicPlayerVolumeSlider;
    private Button musicPauseBtn;
    private static double volume = 0.8;
    
    private IntervalTimer intervalTimer;
    boolean showPeriod;
    private BooleanProperty forceOneDigit = new SimpleBooleanProperty(this, "forceOneDigit", false);
    
    public IntervalTimerController(IntervalTimer intervalTimer, boolean showPeriod, Scene scene, MusicPlayer musicPlayer) {
        this.intervalTimer = intervalTimer;
        this.showPeriod = showPeriod;
        this.scene = scene;
        this.musicPlayer = musicPlayer;
        
        createLayout();
    }
    
    private void createLayout() {
        
        this.getStylesheets().add(IntervalTimerController.class.getClassLoader().getResource("script/buttonStyle.css").toString());

        /*slider for the timer. It shows how much time remains in period
        time can be clicked on to change the time in period*/
        timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setMax(intervalTimer.getCurrentPeriod().getLength() * 30);
        timeSlider.setValue(0.0);
        timeSlider.majorTickUnitProperty().bind(timeSlider.maxProperty().divide(4));
        timeSlider.setShowTickMarks(true);

        //max value is linked to and determine by the current periods length
        intervalTimer.currentPeriodProperty().addListener((periodValue, oldValue, newValue) -> {
            timeSlider.setMax(newValue.getLength() * 30);
        });

        //link together the slider with the playback time
        //they both will be listening to each other and can change the other
        /*timeSlider.valueProperty().addListener((sliderValue, oldValue, newValue) -> {
            intervalTimer.timeProperty().set(newValue.intValue());
        });*/
        timeSlider.setOnMousePressed(e -> {
            intervalTimer.timeProperty().set((int) timeSlider.getValue());
            musicPlayer.haltFade();
            if (intervalTimer.getCurrentPeriod().getTeachingPeriod()) {
                musicPlayer.setVolume(0.1);
            }
        });
        timeSlider.setOnMouseDragged(e -> {
            intervalTimer.timeProperty().set((int) timeSlider.getValue());
            musicPlayer.haltFade();
            if (intervalTimer.getCurrentPeriod().getTeachingPeriod()) {
                musicPlayer.setVolume(0.1);
            }
        });
        intervalTimer.timeProperty().addListener((timeValue, oldValue, newValue) -> {
            timeSlider.valueProperty().set(newValue.doubleValue());
        });
        
        this.getChildren().add(timeSlider);

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
            intervalTimer.previousPeriod(false);
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
            intervalTimer.nextPeriod(false);
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
        /* pauseBtn.setOnAction(e -> {
            intervalTimer.restartPeriod();
        });*/
        pauseBtn.setOnAction(e -> {
            intervalTimer.setPaused(!intervalTimer.isPaused());
        });
        intervalTimer.playingProperty().addListener((playingProperty, oldValue, newValue) -> {
            Image playIconImage;
            ImageView playIconView;
            if (newValue) {
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
            intervalTimer.restartPeriod();
        });

        //volume slider behavior
        intervalTimerVolumeSlider = new Slider(0.0, 1.0, IntervalTimerController.volume);
        IntervalTimer.adjustVolume(IntervalTimerController.volume);
        intervalTimerVolumeSlider.valueProperty().addListener((valueProperty, oldValue, newValue) -> {
            IntervalTimerController.volume = newValue.doubleValue();
            IntervalTimer.adjustVolume(volume);
        });
        
        musicPlayerVolumeSlider = new Slider(0.0, 1.0, musicPlayer.getVolume());
        musicPlayerVolumeSlider.valueProperty().addListener((valueProperty, oldValue, newValue) -> {
            musicPlayer.setVolume(newValue.doubleValue());
            musicPlayer.setVolumeSlider(newValue.doubleValue());
        });
        
        
        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/play.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        musicPauseBtn = new Button();
        musicPauseBtn.setOnMouseEntered(e -> {
            musicPauseBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        musicPauseBtn.setOnMouseExited(e -> {
            musicPauseBtn.setEffect(null);
        });
        musicPauseBtn.setGraphic(iconView);
        
        musicPauseBtn.setOnAction(e -> {
            musicPlayer.pauseMedia();
        });
        
        this.getChildren().add(new HBox(previousBtn, restartBtn, pauseBtn, nextBtn, new Label("Timer Volume: "), intervalTimerVolumeSlider,
                new Label("Music Volume: "), musicPlayerVolumeSlider, new Label("Pause/Play Music->"), musicPauseBtn));

        /*Label for the time remainging of the timer
        *
         */
        timeLabel = new Label();
        //add listener to update time label with proper formatting
        //label displays how much time remains in period
        intervalTimer.timeRemainingProperty().addListener((timeProperty, oldValue, newValue) -> {
            int minutes = newValue.intValue() / 60;
            int seconds = newValue.intValue() % 60;
            String secondsString = (seconds >= 10) ? "" + seconds : "0" + seconds;
            timeLabel.setText(minutes + ":" + secondsString);
        });

        //change font size to bigger value since we don't have tens digit anymore
        forceOneDigit.bind(intervalTimer.timeRemainingProperty().lessThan(600));
        forceOneDigit.addListener((value) -> updateTextSize(scene.getWidth()));
        
        periodLabel = new Label();
        //add listener to update period
        //periodLabel.setText("" + (intervalTimer.getScript().isPeriodZero() ? intervalTimer.getPeriodIndex() : intervalTimer.getPeriodIndex() + 1));
        periodLabel.setText(intervalTimer.getScript().getPeriodNumberAsString(intervalTimer.getPeriodIndex()));
        intervalTimer.periodIndexProperty().addListener((periodProperty, oldValue, newValue) -> {
            //periodLabel.setText("" + (intervalTimer.getScript().isPeriodZero() ? newValue.intValue() : newValue.intValue() + 1));
            periodLabel.setText(intervalTimer.getScript().getPeriodNumberAsString(intervalTimer.getPeriodIndex()));
            updateTextSize(scene.getWidth());
        });
        
        AnchorPane timeLblPane = new AnchorPane();
        timeLblPane.getChildren().add(timeLabel);
        //timeLabel.setAlignment(Pos.BASELINE_RIGHT);
        AnchorPane.setRightAnchor(timeLabel, 5.0);
        AnchorPane.setTopAnchor(timeLabel, 5.0);
        Separator labelSeparator = new Separator(Orientation.VERTICAL);
        
        labelGrid = new GridPane();
        if (showPeriod) {
            labelGrid.addRow(0, periodLabel, labelSeparator, timeLblPane);
        } else {
            labelGrid.addRow(0, timeLblPane);
        }
        GridPane.setHgrow(timeLblPane, Priority.ALWAYS);
        
        labelGrid.setMouseTransparent(true);
        timeLabel.setMouseTransparent(true);
        periodLabel.setMouseTransparent(true);
        this.setPickOnBounds(false);
        
        this.getChildren().add(labelGrid);
        this.getStylesheets().add(IntervalTimerController.class.getClassLoader().getResource("intervaltimer/separatorStyle.css").toString());
        
        updateTextSize(scene.getWidth());
        
    }
    
    public void setMusicVolumeSlider(double volume) {
        musicPlayerVolumeSlider.setValue(volume);
    }
    
    public double getMusicVolumeSliderValue() {
        return musicPlayerVolumeSlider.getValue();
    }
    
    public void updateTextSize(double width) {
        //to determine proper length, make a dummy label and keep increasing its size
        //until it doesn't fit in the grid pane
        //once we get there, then that will give us out font size for the labels
        double fontSize = 5;
        Text dummyLbl = new Text();
        //dummy label is filled with 9 to be largest size our text labels can be
        //label sizes are limited by the period
        StringBuilder stringBuilder = new StringBuilder();
        int truePeriod = intervalTimer.getScript().getTruePeriod(intervalTimer.getPeriodIndex());
        if (showPeriod) {
            stringBuilder.append("" + (truePeriod < 10 ? "9: " : "99: "));
        }
        
        stringBuilder.append("" + (intervalTimer.getScript().getPeriods().get(intervalTimer.getPeriodIndex()).getLength() < 20 || forceOneDigit.get() ? "9:99" : "99:99"));
        dummyLbl.setText(stringBuilder.toString());
        
        do {
            fontSize += 5;
            dummyLbl.setFont(new Font(fontSize));
        } while (dummyLbl.getLayoutBounds().getWidth() <= width - (!showPeriod ? 20 : 0));
        fontSize -= 5;
        
        timeLabel.setStyle("-fx-font-size: " + fontSize + ";");
        periodLabel.setStyle("-fx-font-size: " + fontSize + ";");
        
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(dummyLbl.getFont());
        timeLabel.setPadding(new Insets(-metrics.getDescent(), 0, 0, 0));
        periodLabel.setPadding(new Insets(-metrics.getDescent(), 0, 0, 0));
        
    }
    
}
