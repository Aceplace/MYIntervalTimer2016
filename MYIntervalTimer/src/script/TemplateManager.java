/*
 * Project: MYIntervalTimer
 * Package: script
 * Name: TemplateManager
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 23, 2016
 */
package script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TemplateManager {

    private ObservableList<TemplateItem> templates = FXCollections.observableArrayList();

    public TemplateManager() {

    }

    public void defaultTemplates() {
        templates.clear();
        templates.add(new TemplateItem(new Period(), "Default"));
    }

    public Period getPeriodCopy(int index) {
        if (index >= 0 && index < templates.size()) {
            Period selectedPeriod = templates.get(index).getPeriod();
            Period tempPeriod = new Period(selectedPeriod.getLength());
            tempPeriod.setCountdown(selectedPeriod.getCountdown());
            tempPeriod.setTeachingPeriod(selectedPeriod.getTeachingPeriod());
            tempPeriod.setWaterPeriod(selectedPeriod.getWaterPeriod());
            for (Integer announceTime : selectedPeriod.getAnnouncementTimes()) {
                tempPeriod.getAnnouncementTimes().add(announceTime);
            }
            return tempPeriod;
        }
        return null;
    }

    public void copyPeriodToTemplate(Period period, String name) {
        Period tempPeriod = new Period(period.getLength());
        tempPeriod.setCountdown(period.getCountdown());
        tempPeriod.setTeachingPeriod(period.getTeachingPeriod());
        tempPeriod.setWaterPeriod(period.getWaterPeriod());
        for (Integer announceTime : period.getAnnouncementTimes()) {
            tempPeriod.getAnnouncementTimes().add(announceTime);
        }
        templates.add(new TemplateItem(tempPeriod, name));
    }

    public void deletePeriod(int index) {
        if (index >= 0 && index < templates.size()) {
            templates.remove(index);
        }

    }

    public void deletePeriod(TemplateItem item) {
        templates.remove(item);
    }

    public ObservableList<TemplateItem> getTemplates() {
        return templates;
    }

    public void setTemplates(ObservableList<TemplateItem> templates) {
        this.templates = templates;
    }

    public boolean contains(String id) {
        for (TemplateItem item : templates) {
            if (item.getName().equals(id))
                return true;
        }
        return false;
    }

    /*loading and saving templates is done automatically. The templates are saved in
    a file that is made in the directory of the executable. If the file does exist, then
    templates will make a default one. The format for the file is...
    Integer number of templateitems
    For each template item
    String name int length, boolean countdown, arraylist<integer> announcementtimes
     */
    public void loadTemplates() {
        File file = new File("templates.pref");

        try (InputStream is = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(is)) {
            String version = (String) ois.readObject();

            //using temporary object until assured everything is assured to be loaded in correctly
            int numberOfTemplates = ois.readInt();
            int tempLength;
            boolean tempCountdown;
            boolean tempTeachingPeriod;
            boolean tempWaterPeriod;
            ArrayList<Integer> tempAnnouncementTimes;

            Period tempPeriod;
            String tempName;
            ObservableList<TemplateItem> tempItems = FXCollections.observableArrayList();

            for (int i = 1; i <= numberOfTemplates; i++) {
                tempName = (String) ois.readObject();
                tempLength = ois.readInt();
                tempCountdown = ois.readBoolean();
                tempTeachingPeriod = ois.readBoolean();
                tempWaterPeriod = ois.readBoolean();
                tempAnnouncementTimes = (ArrayList<Integer>) ois.readObject();

                tempPeriod = new Period(tempLength);
                tempPeriod.setCountdown(tempCountdown);
                tempPeriod.setTeachingPeriod(tempTeachingPeriod);
                tempPeriod.setWaterPeriod(tempWaterPeriod);
                tempPeriod.setAnnouncementTimes(FXCollections.observableArrayList(tempAnnouncementTimes));

                tempItems.add(new TemplateItem(tempPeriod, tempName));
            }

            //if we got here, then apparently all file i/o went well
            //load periods into the script
            templates.setAll(tempItems);

        } catch (Exception ex) {
            defaultTemplates();//any issues with loading will be resolved by make default templates 
        }
    }

    public void saveTemplates() {

        File file = new File("templates.pref");
        try (OutputStream os = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(os)) {

            oos.writeObject("1.0");

            oos.writeInt(templates.size());
            for (TemplateItem item : templates) {
                oos.writeObject(item.getName());
                oos.writeInt(item.getPeriod().getLength());
                oos.writeBoolean(item.getPeriod().getCountdown());
                oos.writeBoolean(item.getPeriod().getTeachingPeriod());
                oos.writeBoolean(item.getPeriod().getWaterPeriod());
                oos.writeObject(new ArrayList<Integer>(item.getPeriod().getAnnouncementTimes()));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
