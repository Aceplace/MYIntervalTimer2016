package mainframe;

/*
 * Project: MYIntervalTimer
 * Package: Default
 * Name: Main
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
import intervaltimer.IntervalTimer;
import intervaltimer.IntervalTimerController;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import script.Period;
import script.Script;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import musicplayer.MusicPlayer;
import script.ScriptEditorController;

public class Main extends Application {

    //overall layout of application
    private BorderPane layout = new BorderPane();

    //fields to represent the parts of our menus
    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveAsItem;
    private MenuItem exitItem;
    private MenuItem helpItem;
    private MenuItem aboutItem;
    private ToggleGroup viewGroup;
    private RadioMenuItem scriptEditorItem;
    private RadioMenuItem timerEditorItem;
    private RadioMenuItem musicPlayerItem;
    private String lastSavePathName = new String();

    //references to other components of application
    private IntervalTimer intervalTimer = null;
    private IntervalTimerController intervalTimerController = null;

    private Script script = null;
    private ScriptEditorController scriptEditorController = null;

    private MusicPlayer musicPlayer;

    private Scene scene;
    private Preferences preferences;

    private Mode mode;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        musicPlayer = new MusicPlayer();
        IntervalTimer iTimer = new IntervalTimer(createNewScript(), musicPlayer);

        scene = new Scene(layout, Screen.getPrimary().getVisualBounds().getWidth() / 2.0, Screen.getPrimary().getVisualBounds().getHeight() / 2.0, true);
        preferences = new Preferences();

        setUpMenu();
        script = createNewScript();
        scriptEditorController = new ScriptEditorController(script);
        mode = Mode.SCRIPT_EDITOR;
        layout.setCenter(new ScrollPane(scriptEditorController));

        primaryStage.setTitle("MY Interval Timer");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> {
            if (scriptEditorController != null) {
                scriptEditorController.saveTemplates();
            }
            if (!exit()) {
                e.consume();
            } else
                preferences.savePreferences();
        });

        //update text size in interval timer needs screen size
        scene.widthProperty().addListener((widthProperty, oldValue, newValue) -> {
            if (intervalTimerController != null) {
                intervalTimerController.updateTextSize(newValue.doubleValue());
            }
        });

    }

    public void setUpMenu() {
        MenuBar menuBar = new MenuBar();

        //file items to load, save and make scripts
        Menu fileMenu = new Menu("File");
        newItem = new MenuItem("New");
        openItem = new MenuItem("Open...");
        saveItem = new MenuItem("Save");
        saveAsItem = new MenuItem("Save as...");
        exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newItem,
                new SeparatorMenuItem(),
                openItem,
                new SeparatorMenuItem(),
                saveItem,
                saveAsItem,
                new SeparatorMenuItem(),
                exitItem);

        newItem.setOnAction(e -> {
            newItem();
        });
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));

        saveItem.setOnAction(e -> {
            save();
        });
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));

        saveAsItem.setOnAction(e -> {
            saveAs();
        });

        openItem.setOnAction(e -> {
            open();
        });
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));

        exitItem.setOnAction(e -> {
            if (exit()) {
                preferences.savePreferences();
                Platform.exit();
            }
        });

        //view switches between the script editor and interval timer
        Menu viewMenu = new Menu("View");
        viewGroup = new ToggleGroup();
        scriptEditorItem = new RadioMenuItem("Script Editor");
        timerEditorItem = new RadioMenuItem("Interval Timer");
        musicPlayerItem = new RadioMenuItem("Music Player");
        viewGroup.getToggles().addAll(scriptEditorItem, timerEditorItem, musicPlayerItem);
        scriptEditorItem.setSelected(true);
        viewMenu.getItems().addAll(scriptEditorItem, timerEditorItem, musicPlayerItem);

        viewGroup.selectedToggleProperty().addListener((selectedProperty, oldValue, newValue) -> {
            if (newValue == timerEditorItem) {
                //disable file option while in timer
                newItem.setDisable(true);
                openItem.setDisable(true);
                saveItem.setDisable(true);
                saveAsItem.setDisable(true);
                scriptEditorController.saveTemplates();//automatically save templates when possible

                if (mode == Mode.SCRIPT_EDITOR || intervalTimer == null) {//when coming from script editor, start a new interval timer
                    boolean omitTimeAnnouncement = ScriptEditorController.shouldOmitTimeAnnouncements();
                    intervalTimer = new IntervalTimer(script, musicPlayer);
                    intervalTimerController = new IntervalTimerController(intervalTimer, ScriptEditorController.shouldShowPeriod(), scene, musicPlayer);
                    intervalTimer.startPeriod(false);
                    intervalTimer.setPaused(true);
                    intervalTimer.setOmitTimeAnnouncement(omitTimeAnnouncement);
                    musicPlayer.setVolume(musicPlayer.getMaxVolume());
                }
                mode = Mode.TIMER;
                layout.setCenter(intervalTimerController);
            } else if (newValue == scriptEditorItem) {
                if (intervalTimer != null) {
                    intervalTimer.close();
                    intervalTimer = null;
                }
                newItem.setDisable(false);
                openItem.setDisable(false);
                saveItem.setDisable(false);
                saveAsItem.setDisable(false);

                scriptEditorController = new ScriptEditorController(script);
                mode = Mode.SCRIPT_EDITOR;
                layout.setCenter(new ScrollPane(scriptEditorController));
            } else if (newValue == musicPlayerItem) {
                newItem.setDisable(false);
                openItem.setDisable(false);
                saveItem.setDisable(false);
                saveAsItem.setDisable(false);

                if (oldValue == timerEditorItem && intervalTimerController != null) {
                    musicPlayer.setVolumeSlider(intervalTimerController.getMusicVolumeSliderValue());
                }

                musicPlayer.setVolume(musicPlayer.getMaxVolume());
                mode = Mode.MUSIC_PLAYER;
                layout.setCenter(musicPlayer);
            }
        });

        Menu helpMenu = new Menu("Help");
        helpItem = new MenuItem("View Help");
        aboutItem = new MenuItem("About Yeiter Interval Timer");
        helpMenu.getItems().addAll(helpItem, new SeparatorMenuItem(), aboutItem);
        helpItem.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.youtube.com/playlist?list=PLSx22sQP5BbbY7myiHl5v5r0ZrpyYJEHw"));
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        });

        aboutItem.setOnAction(e -> {
            //load internal "about" file and present it in an alert dialog
            InputStream is = Main.class.getClassLoader().getResourceAsStream("resources/about.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                Alert about = new Alert(Alert.AlertType.CONFIRMATION);
                about.setTitle("About");
                about.setHeaderText("Yeiter Interval Timer");
                about.setContentText(stringBuilder.toString());
                about.showAndWait();

            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        layout.setTop(menuBar);

    }

    public Script createNewScript() {
        Script script = new Script();

        Period tempPeriod;

        tempPeriod = new Period(10);
        tempPeriod.getAnnouncementTimes().addAll(5, 2, 1);
        script.getPeriods().add(tempPeriod);

        return script;
    }

    public void newItem() {
        if (ScriptEditorController.madeChanges()) {
            //check to see if user wants to save first, 
            //continue only if they pick no or successfully save first
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Yeiter Interval Timer");
            alert.setHeaderText("Save script before continuing?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                if (save() == false)//attempt to save
                {
                    return;
                }
            }
            if (result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        script = createNewScript();
        scriptEditorController = new ScriptEditorController(script);
        layout.setCenter(scriptEditorController);
        ScriptEditorController.setMadeChanges(true);
        lastSavePathName = "";
    }

    public boolean save() {
        if (lastSavePathName.length() > 0) {
            File file = new File(lastSavePathName);
            if (file.isFile()) {
                script.saveScript(lastSavePathName);
                lastSavePathName = file.getPath();
                ScriptEditorController.setMadeChanges(false);
                preferences.setLastFileName(lastSavePathName);
                return true;
            } else {
                return saveAs();
            }
        } else {
            return saveAs();
        }
    }

    public boolean saveAs() {
        FileChooser fc = new FileChooser();
        if (preferences.getDirectory().length() > 0)
            fc.setInitialDirectory(new File(preferences.getDirectory()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MY Interval Timer Script File", "*.itsf"));
        File file = fc.showSaveDialog(null);
        if (file != null) {
            script.saveScript(file.getPath());
            lastSavePathName = file.getPath();
            ScriptEditorController.setMadeChanges(false);
            preferences.setLastFileName(lastSavePathName);
            return true;
        }
        return false;
    }

    public boolean open() {
        if (ScriptEditorController.madeChanges()) {
            //check to see if user wants to save first, 
            //continue only if they pick no or successfully save first
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Yeiter Interval Timer");
            alert.setHeaderText("Save script before continuing?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                if (save() == false)//attempt to save
                {
                    return false;
                }
            }
            if (result.get() == ButtonType.CANCEL) {
                return false;
            }
        }
        FileChooser fc = new FileChooser();
        if (preferences.getDirectory().length() > 0)
            fc.setInitialDirectory(new File(preferences.getDirectory()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MY Interval Timer Script File", "*.itsf"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            script.loadScript(file.getPath());
            lastSavePathName = file.getPath();
            ScriptEditorController.setMadeChanges(false);
            preferences.setLastFileName(lastSavePathName);
            return true;
        }
        return false;
    }

    public boolean exit() {
        if (ScriptEditorController.madeChanges()) {
            //check to see if user wants to save first, 
            //continue only if they pick no or successfully save first
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Yeiter Interval Timer");
            alert.setHeaderText("Save script before exiting?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                if (save() == false)//attempt to save
                {
                    return false;
                }
            }
            if (result.get() == ButtonType.CANCEL) {
                return false;
            }
        }
        return true;
    }

}
