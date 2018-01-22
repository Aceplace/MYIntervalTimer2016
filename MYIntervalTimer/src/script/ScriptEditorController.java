/*
 * Project: MYIntervalTimer
 * Package: script
 * Name: ScriptEditorController
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
package script;

import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ScriptEditorController extends HBox {

    //controls for the script editor controller
    //Template controls and script option controls
    TemplateManager templateManager;
    Label totalScriptDurationLbl;
    ListView<TemplateItem> templateList;
    CheckBox omitTimeCheckBox;
    static boolean omitTimeAnnouncements = false;
    CheckBox showPeriodCheckBox;
    static boolean showPeriod = true;
    static boolean madeChanges = false;

    //Period Editor Buttons
    VBox listEditorButtons = new VBox();
    Button templateAddBtn;
    Button newBtn;
    Button removeBtn;
    Button moveUpBtn;
    Button moveDownBtn;
    //Button copyBtn;

    //Table editor columns
    private TableView<Period> scriptTable;
    TableColumn<Period, Number> periodNumberColumn;
    TableColumn<Period, Number> periodLengthColumn;
    TableColumn<Period, ObservableList<Integer>> announcementTimesColumn;

    //Detail view
    CheckBox countdownCheckBox;
    CheckBox teachingPeriodCheckBox;
    CheckBox waterPeriodCheckBox;
    ObservableList<CheckBox> announceCheckBoxes;

    private Script script;

    public ScriptEditorController(Script script) {
        this.script = script;
        createLayout();

        script.getPeriods().addListener(new ListChangeListener<Period>() {
            public void onChanged(ListChangeListener.Change<? extends Period> c) {
                ScriptEditorController.setMadeChanges(true);
            }

        });
    }

    private void createLayout() {
        this.setSpacing(2.0);
        createTemplateAddLayout();
        createPeriodEditorButtonLayout();
        createPeriodTableLayout();
        createPeriodDetailView();
    }

    private void createTemplateAddLayout() {
        VBox templateAddLayout = new VBox();

        //Template
        templateManager = new TemplateManager();
        templateManager.loadTemplates();
        templateList = new ListView(templateManager.getTemplates());
        templateList.setPrefHeight(300);
        templateList.setCellFactory((list) -> {
            return new ListCell<TemplateItem>() {
                @Override
                protected void updateItem(TemplateItem item, boolean empty) {
                    super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                    if (empty || item == null) {
                        this.setText(null);
                        this.setGraphic(null);

                    } else {
                        Button deleteTemplateButton = new Button("X");
                        deleteTemplateButton.setId("template");
                        deleteTemplateButton.getStylesheets().add(ScriptEditorController.class.getClassLoader().getResource("script/templateButton.css").toString());
                        deleteTemplateButton.setOnAction(e -> {
                            list.getItems().remove(item);
                        });
                        this.setGraphic(deleteTemplateButton);
                        this.setText(item.toString());
                    }
                }

            };
        });

        //Script option checkboxes
        Label scriptOptionsLbl = new Label("Script Options");
        scriptOptionsLbl.setUnderline(true);
        CheckBox periodZeroCheckBox = new CheckBox("Include Period Zero");
        periodZeroCheckBox.setSelected(script.isPeriodZero());
        omitTimeCheckBox = new CheckBox("Omit time announcements");
        omitTimeCheckBox.setSelected(omitTimeAnnouncements);
        omitTimeCheckBox.selectedProperty().addListener((selectedProperty, oldValue, newValue) -> {
            omitTimeAnnouncements = newValue;
        });

        showPeriodCheckBox = new CheckBox("Show Period");
        showPeriodCheckBox.setSelected(showPeriod);
        showPeriodCheckBox.selectedProperty().addListener((selectedProperty, oldValue, newValue) -> {
            showPeriod = newValue;
        });

        //check box should update script when clicked and vice versa (script loading updates check box)
        periodZeroCheckBox.selectedProperty().addListener((selected, oldValue, newValue) -> {
            script.setPeriodZero(newValue);
            ScriptEditorController.setMadeChanges(true);
            //hack to force columns to updata
            periodNumberColumn.setVisible(false);
            periodNumberColumn.setVisible(true);
        });

        //Duration label to show user how long their script is
        Label scriptDurationLbl = new Label("Total script duration");
        scriptDurationLbl.setUnderline(true);
        totalScriptDurationLbl = new Label();
        updateScriptDuration();

        script.periodZeroProperty().addListener((periodZero, oldValue, newValue) -> {
            periodZeroCheckBox.setSelected(script.isPeriodZero());
            //hack to force columns to updata
            periodNumberColumn.setVisible(false);
            periodNumberColumn.setVisible(true);
        });

        script.getPeriods().addListener(new ListChangeListener<Period>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Period> c) {
                updateScriptDuration();
            }
        });

        templateAddLayout.getChildren().addAll(new Label("Templates:"), templateList, new Label(),
                scriptOptionsLbl, periodZeroCheckBox, omitTimeCheckBox, showPeriodCheckBox,
                new Label(), scriptDurationLbl, totalScriptDurationLbl);
        this.getChildren().add(templateAddLayout);

    }

    private void createPeriodEditorButtonLayout() {

        //load int he icons for the buttons
        //add a little effect where buttons gets brighter when hovered over
        listEditorButtons.getStylesheets().add(ScriptEditorController.class.getClassLoader().getResource("script/buttonStyle.css").toString());
        Image iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/add.png"));
        ImageView iconView = new ImageView(iconImage);
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
        newBtn = new Button();
        newBtn.setOnMouseEntered(e -> {
            newBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0));
        });
        newBtn.setOnMouseExited(e -> {
            newBtn.setEffect(null);
        });
        newBtn.setGraphic(iconView);

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/right.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
        templateAddBtn = new Button();
        templateAddBtn.setOnMouseEntered(e -> {
            templateAddBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0));
        });
        templateAddBtn.setOnMouseExited(e -> {
            templateAddBtn.setEffect(null);
        });
        templateAddBtn.setGraphic(iconView);

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/delete.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
        removeBtn = new Button();
        removeBtn.setOnMouseEntered(e -> {
            removeBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        removeBtn.setOnMouseExited(e -> {
            removeBtn.setEffect(null);
        });
        removeBtn.setGraphic(iconView);

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/up.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
        moveUpBtn = new Button();
        moveUpBtn.setOnMouseEntered(e -> {
            moveUpBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        moveUpBtn.setOnMouseExited(e -> {
            moveUpBtn.setEffect(null);
        });
        moveUpBtn.setGraphic(iconView);

        iconImage = new Image(ScriptEditorController.class.getClassLoader().getResourceAsStream("resources/icons/down.png"));
        iconView = new ImageView(iconImage);
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
        moveDownBtn = new Button();
        moveDownBtn.setOnMouseEntered(e -> {
            moveDownBtn.setEffect(new ColorAdjust(0.1, 0, 0.4, 0.2));
        });
        moveDownBtn.setOnMouseExited(e -> {
            moveDownBtn.setEffect(null);
        });
        moveDownBtn.setGraphic(iconView);

        /* copyBtn = new Button("C");
        copyBtn.setMaxWidth(Double.MAX_VALUE);*/
        listEditorButtons.getChildren().addAll(templateAddBtn, newBtn, removeBtn, moveUpBtn, moveDownBtn);
        listEditorButtons.setAlignment(Pos.CENTER);
        this.getChildren().add(listEditorButtons);

        //add behavior for buttons
        templateAddBtn.setOnAction(e -> {
            if (script.getPeriods().size() >= 40)//limit the number of period in a script
            {
                return;
            }
            if (templateList.getSelectionModel().getSelectedIndex() != -1) {

                if (scriptTable.getSelectionModel().getSelectedIndex() == -1) {
                    script.add(templateManager.getPeriodCopy(templateList.getSelectionModel().getSelectedIndex()));
                } else {
                    script.getPeriods().add(scriptTable.getSelectionModel().getSelectedIndex() + 1,
                            templateManager.getPeriodCopy(templateList.getSelectionModel().getSelectedIndex()));
                    scriptTable.getSelectionModel().selectNext();
                }
            }
        });
        newBtn.setOnAction(e -> {
            if (script.getPeriods().size() >= 40)//limit the number of period in a script
            {
                return;
            }
            if (scriptTable.getSelectionModel().getSelectedIndex() == -1) {
                Period selectedPeriod = script.getPeriods().get(script.getPeriods().size() - 1);
                Period tempPeriod = new Period(selectedPeriod.getLength());
                tempPeriod.setCountdown(selectedPeriod.getCountdown());
                for (Integer announceTime : selectedPeriod.getAnnouncementTimes()) {
                    tempPeriod.getAnnouncementTimes().add(announceTime);
                }
                script.getPeriods().add(tempPeriod);
            } else {
                /*script.getPeriods().add(scriptTable.getSelectionModel().getSelectedIndex() + 1, new Period());
                scriptTable.getSelectionModel().selectNext();*/
                Period selectedPeriod = scriptTable.getSelectionModel().getSelectedItem();
                Period tempPeriod = new Period(selectedPeriod.getLength());
                tempPeriod.setCountdown(selectedPeriod.getCountdown());
                for (Integer announceTime : selectedPeriod.getAnnouncementTimes()) {
                    tempPeriod.getAnnouncementTimes().add(announceTime);
                }
                tempPeriod.setTeachingPeriod(selectedPeriod.getTeachingPeriod());
                tempPeriod.setWaterPeriod((selectedPeriod.getWaterPeriod()));
                script.getPeriods().add(scriptTable.getSelectionModel().getSelectedIndex(), tempPeriod);
            }
        });

        removeBtn.setOnAction(e -> {
            if (scriptTable.getSelectionModel().getSelectedIndex() != -1 && script.getPeriods().size() > 1) {
                int index = scriptTable.getSelectionModel().getSelectedIndex();
                script.getPeriods().remove(scriptTable.getSelectionModel().getSelectedIndex());
                if (index != 0) {
                    scriptTable.getSelectionModel().select(index);
                } else {
                    scriptTable.getSelectionModel().clearSelection();
                    scriptTable.getSelectionModel().selectFirst();
                }
            }
        });

        moveUpBtn.setOnAction(e -> {
            if (scriptTable.getSelectionModel().getSelectedIndex() != -1) {
                script.moveUp(scriptTable.getSelectionModel().getSelectedIndex());
            }
        });

        moveDownBtn.setOnAction(e -> {
            if (scriptTable.getSelectionModel().getSelectedIndex() != -1) {
                script.moveDown(scriptTable.getSelectionModel().getSelectedIndex());
            }
        });

        /*copyBtn.setOnAction(e -> {
            if (script.getPeriods().size() >= 40)//limit the number of period in a script
            {
                return;
            }
            if (scriptTable.getSelectionModel().getSelectedIndex() != -1) {
                Period selectedPeriod = scriptTable.getSelectionModel().getSelectedItem();
                Period tempPeriod = new Period(selectedPeriod.getLength());
                tempPeriod.setCountdown(selectedPeriod.getCountdown());
                for (Integer announceTime : selectedPeriod.getAnnouncementTimes()) {
                    tempPeriod.getAnnouncementTimes().add(announceTime);
                }
                script.getPeriods().add(scriptTable.getSelectionModel().getSelectedIndex(), tempPeriod);

            }
        });*/
    }

    private void createPeriodTableLayout() {

        scriptTable = new TableView(script.getPeriods());

        scriptTable.getStylesheets().add(ScriptEditorController.class.getClassLoader().getResource("script/disableTableHorizontalScrollbar.css").toString());
        scriptTable.setPrefWidth(400);
        periodNumberColumn = new TableColumn<>("Period");
        periodNumberColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<Number>(scriptTable.getItems().indexOf(cellData.getValue())));
        periodNumberColumn.setCellFactory(tableColumn -> {
            return new TableCell<Period, Number>() {
                @Override
                protected void updateItem(Number index, boolean empty) {
                    super.updateItem(index, empty);

                    if (!empty && index != null) {
                        this.setText("" + script.getPeriodNumberAsString(index.intValue()));
                    } else {
                        this.setText("");
                    }
                }

            };
        });
        periodNumberColumn.setSortable(false);

        periodLengthColumn = new TableColumn<>("Length");
        periodLengthColumn.setCellValueFactory(cellData -> cellData.getValue().lengthProperty());
        periodLengthColumn.setCellFactory(tableColumn -> {
            return new TableCell<Period, Number>() {
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);

                    if (!empty && item != null) {
                        this.setText(Period.formatHalfMinutes(item.intValue()));
                    } else {
                        this.setText("");
                    }
                }

            };
        });
        periodLengthColumn.setSortable(false);

        announcementTimesColumn = new TableColumn<>("Announcement Times");
        announcementTimesColumn.setCellValueFactory(cellData -> new ReadOnlyListWrapper<Integer>(cellData.getValue().getAnnouncementTimes()));

        announcementTimesColumn.setCellFactory(column -> {
            return new TableCell<Period, ObservableList<Integer>>() {
                @Override
                protected void updateItem(ObservableList<Integer> item, boolean empty) {
                    super.updateItem(item, empty);

                    if (!empty && this != null && this.getTableRow() != null && this.getTableRow().getItem() != null) {
                        this.setText(((Period) this.getTableRow().getItem()).getAnnouncementTimesString());
                    } else {
                        this.setText("");
                    }
                }

            };
        });
        announcementTimesColumn.setSortable(false);
        announcementTimesColumn.prefWidthProperty().bind(scriptTable.widthProperty()
                .subtract(periodNumberColumn.widthProperty())
                .subtract(periodLengthColumn.widthProperty()));
        scriptTable.getColumns().addAll(periodNumberColumn, periodLengthColumn, announcementTimesColumn);

        this.getChildren().add(scriptTable);

    }

    private void createPeriodDetailView() {
        VBox detailViewLayout = new VBox();

        /* Template item
        Users will be able to save whatever period is in detail view into templates by pressing button.
        They can then use them for quick additions later
         */
        Button templateBtn = new Button("Save as Template");
        TextField templateName = new TextField();
        templateName.setPromptText("Enter template name...");
        templateBtn.setOnAction(e -> {
            if (templateName.getText().length() > 0) {
                Period selectedPeriod = scriptTable.getSelectionModel().getSelectedItem();
                if (selectedPeriod != null) {
                    templateManager.copyPeriodToTemplate(selectedPeriod, templateName.getText());
                    templateName.setText("");
                }
            }
        });
        //disable template button if its name matches a value in the template
        templateName.textProperty().addListener((textProperty, oldValue, newValue) -> {
            templateBtn.setDisable(templateManager.contains(newValue));
        });

        /* Period length box
        Displays period and period length
        Period length is a choice box by which the user can change the period length
        Connects with the selected period from the table
         */
        Label periodLabel = new Label();
        Label lengthLabel = new Label("Period Length: ");
        ChoiceBox<Integer> lengthBox = new ChoiceBox();
        //add values for 2 minute up to 20 minutes into the choice box
        for (int i = 4; i <= 40; i++) {
            lengthBox.getItems().add(new Integer(i));
        }
        lengthBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer halfMinutes) {
                return Period.formatHalfMinutes(halfMinutes);
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });
        lengthBox.valueProperty().addListener((timePropert, oldValue, newValue) -> {
            //must update the corresponding period
            Period period = scriptTable.getSelectionModel().getSelectedItem();
            if (period != null) {
                ScriptEditorController.setMadeChanges(true);
                period.setLength(newValue);
                //clear out all annoucements above the stated time and their corresponding checkboxes
                for (int i = newValue.intValue() + 1; i <= 40; i++) {
                    period.removeAnnouncementTime(i);
                    announceCheckBoxes.get(i - 1).setSelected(false);
                }
                updateScriptDuration();
            }
        });

        //other checkboxes
        teachingPeriodCheckBox = new CheckBox("Teaching Period");
        teachingPeriodCheckBox.setOnAction(e -> {
            Period period = scriptTable.getSelectionModel().getSelectedItem();
            if (period != null) {
                period.setTeachingPeriod(((CheckBox) e.getSource()).isSelected());
                //hack to force column to update
                announcementTimesColumn.setVisible(false);
                announcementTimesColumn.setVisible(true);
                ScriptEditorController.setMadeChanges(true);
            }
        });

        waterPeriodCheckBox = new CheckBox("Water Period");
        waterPeriodCheckBox.setOnAction(e -> {
            Period period = scriptTable.getSelectionModel().getSelectedItem();
            if (period != null) {
                period.setWaterPeriod(((CheckBox) e.getSource()).isSelected());
                //hack to force column to update
                periodNumberColumn.setVisible(false);
                periodNumberColumn.setVisible(true);
                ScriptEditorController.setMadeChanges(true);
            }
        });

        /*Announcement block
        Displays a series of checkboxes that correspond to when announcements are made
        When a box is selected is add the appropriate announcement time to the period
        Connects with selected period from the table
         */
        ScrollPane announcementPane = new ScrollPane();
        VBox announcementBox = new VBox();
        countdownCheckBox = new CheckBox("0:05 <Countdown>");
        countdownCheckBox.setOnAction(e -> {
            Period period = scriptTable.getSelectionModel().getSelectedItem();
            if (period != null) {
                period.setCountdown(((CheckBox) e.getSource()).isSelected());
                //hack to force column to update
                announcementTimesColumn.setVisible(false);
                announcementTimesColumn.setVisible(true);
            }
        });
        announceCheckBoxes = FXCollections.observableArrayList();
        CheckBox tempCheckBox;
        for (int i = 1; i <= 40; i++) {
            tempCheckBox = new CheckBox();
            tempCheckBox.setText(Period.formatHalfMinutes(i));
            tempCheckBox.getProperties().put("halfMinutes", i);
            tempCheckBox.setVisible(false);

            //must add and remove corresponding announcement from the period when a 
            //checkbox is clicked
            tempCheckBox.setOnAction(e -> {
                Period period = scriptTable.getSelectionModel().getSelectedItem();
                if (period != null) {
                    ScriptEditorController.setMadeChanges(true);
                    CheckBox checkBox = (CheckBox) e.getSource();
                    if (checkBox.isSelected()) {
                        period.addAnnouncementTime((Integer) checkBox.getProperties().get("halfMinutes"));
                    } else {
                        period.removeAnnouncementTime((Integer) checkBox.getProperties().get("halfMinutes"));
                    }
                }
            });

            final CheckBox ftempCheckBox = tempCheckBox;
            lengthBox.valueProperty().addListener((lengthProperty, oldValue, newValue) -> {
                //all checkboxes with time values above the listed period length should not be displayed
                ftempCheckBox.setVisible((int) ftempCheckBox.getProperties().get("halfMinutes") <= newValue.intValue());
            });

            announceCheckBoxes.add(tempCheckBox);

        }
        announcementBox.getChildren().add(countdownCheckBox);
        announcementBox.getChildren().addAll(announceCheckBoxes);
        announcementPane.setContent(announcementBox);

        detailViewLayout.getChildren().addAll(new HBox(templateBtn, templateName),
                periodLabel, teachingPeriodCheckBox, waterPeriodCheckBox, new HBox(lengthLabel, lengthBox), new Label("Announce time at..."), announcementPane);

        //link the detail view to the table script selection
        //When selection changes so does the view
        detailViewLayout.setDisable(true);
        scriptTable.getSelectionModel().selectedIndexProperty().addListener((selection, oldValue, newValue) -> {
            if (newValue.intValue() == -1) {
                detailViewLayout.setDisable(true);
            } else {
                Period period = script.getPeriods().get(newValue.intValue());
                detailViewLayout.setDisable(false);
                periodLabel.setText("Period: " + (script.isPeriodZero() ? newValue : newValue.intValue() + 1));
                lengthBox.setValue(period.getLength());
                teachingPeriodCheckBox.setSelected(period.getTeachingPeriod());
                waterPeriodCheckBox.setSelected(period.getWaterPeriod());
                populateCheckBoxes();
            }
        });

        this.getChildren().add(detailViewLayout);

    }

    private void populateCheckBoxes() {
        Period period = scriptTable.getSelectionModel().getSelectedItem();
        if (period != null) {
            countdownCheckBox.setSelected(period.getCountdown());
            for (CheckBox checkBox : announceCheckBoxes) {
                checkBox.setSelected(period.doesAnnounceTime((int) checkBox.getProperties().get("halfMinutes")));
            }
        }
    }

    private void updateScriptDuration() {
        //get total time from script by adding the length of all the periods
        int totaltime = 0;
        for (Period period : script.getPeriods()) {
            totaltime += period.getLength();
        }

        int hours = totaltime / 120;
        int minutes = (totaltime % 120) / 2;
        int halfminutes = (totaltime % 120) % 2;

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(hours + ":");
        stringbuilder.append((minutes > 10 ? "" + minutes : "0" + minutes) + ":");
        stringbuilder.append((halfminutes == 1 ? "30" : "00"));

        totalScriptDurationLbl.setText(stringbuilder.toString());
    }

    public static boolean shouldOmitTimeAnnouncements() {
        return omitTimeAnnouncements;
    }

    public static boolean shouldShowPeriod() {
        return showPeriod;
    }

    public void saveTemplates() {
        if (templateManager != null) {
            templateManager.saveTemplates();
        }
    }

    public static boolean madeChanges() {
        return madeChanges;
    }

    public static void setMadeChanges(boolean madeChanges) {
        ScriptEditorController.madeChanges = madeChanges;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

}
