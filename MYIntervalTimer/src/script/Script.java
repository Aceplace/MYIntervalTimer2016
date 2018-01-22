/*
 * Project: MYIntervalTimer
 * Package: intervaltimer
 * Name: Script
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Script {

    private ObservableList<Period> periods = FXCollections.observableArrayList();
    private BooleanProperty periodZero = new SimpleBooleanProperty(this, "periodZero", false);

    public void add(Period period) {
        periods.add(period);
    }

    public void remove(Period period) {
        periods.remove(period);
    }

    public void remove(int index) {
        periods.remove(index);
    }

    public void moveUp(int index) {
        if (index > 0 && index < periods.size()) {
            Collections.swap(periods, index, index - 1);
        }
    }

    public void moveDown(int index) {
        if (index >= 0 && index < periods.size() - 1) {
            Collections.swap(periods, index, index + 1);
        }
    }

    public String getPeriodNumberAsString(int index) {
        if (index < 0 || index >= periods.size())
            return null;

        if (periods.get(index).getWaterPeriod())
            return "W";

        int periodNumber = 0;
        if (!periodZero.get())
            periodNumber++;

        for (Period period : periods) {
            if (period == periods.get(index))
                return "" + periodNumber;
            if (!period.getWaterPeriod())
                periodNumber++;
        }
        return null;
    }

    public int getTruePeriod(int index) {
        if (index < 0 || index >= periods.size())
            return -1;

        if (periods.get(index).getWaterPeriod())
            return -1;

        int periodNumber = 0;
        if (!periodZero.get())
            periodNumber++;

        for (Period period : periods) {
            if (period == periods.get(index))
                return periodNumber;
            if (!period.getWaterPeriod())
                periodNumber++;
        }
        return -1;
    }

    //Various getters and setter
    //<editor-fold>
    public ObservableList<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(ObservableList<Period> periods) {
        this.periods = periods;
    }

    public int getTime(int period) {
        return periods.get(period).getLength();
    }

    public final void setPeriodZero(Boolean value) {
        periodZero.set(value);
    }

    public final Boolean isPeriodZero() {
        return periodZero.get();
    }

    public final BooleanProperty periodZeroProperty() {
        return periodZero;
    }

    //</editor-fold>
    //Handle loading and saving
    //Script files should have very specific format
    /*
    boolean periodzero, int numperiods
    for each period
    int length, boolean countdown, arraylist<integer>
     */
    public void loadScript(String url) {
        File file = new File(url);

        try (InputStream is = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(is)) {

            String versionNumber = (String) ois.readObject();

            //using temporary object until assured everything is assured to be loaded in correctly
            boolean tempPeriodZero = ois.readBoolean();
            int numberOfPeriods = ois.readInt();

            int tempLength;
            boolean tempCountdown, tempTeachingPeriod, tempWaterPeriod;
            ArrayList<Integer> tempAnnouncementTimes;

            Period tempPeriod;
            ObservableList<Period> tempPeriods = FXCollections.observableArrayList();

            for (int i = 1; i <= numberOfPeriods; i++) {
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

                tempPeriods.add(tempPeriod);
            }

            //if we got here, then apparently all file i/o went well
            //load periods into the script
            periodZero.set(tempPeriodZero);
            periods.setAll(tempPeriods);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public void saveScript(String url) {

        File file = new File(url);
        try (OutputStream os = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(new String("1.0"));
            oos.writeBoolean(periodZero.get());
            oos.writeInt(periods.size());
            for (Period period : periods) {
                oos.writeInt(period.getLength());
                oos.writeBoolean(period.getCountdown());
                oos.writeBoolean(period.getTeachingPeriod());
                oos.writeBoolean(period.getWaterPeriod());
                oos.writeObject(new ArrayList<Integer>(period.getAnnouncementTimes()));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
