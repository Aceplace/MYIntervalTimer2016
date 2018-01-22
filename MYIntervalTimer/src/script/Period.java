/*
 * Project: MYIntervalTimer
 * Package: intervaltimer
 * Name: Period
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
package script;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Period implements Serializable {

    public static String formatHalfMinutes(int halfMinutes) {
        return "" + (halfMinutes / 2) + ":" + ((halfMinutes % 2) == 1 ? "30" : "00");
    }
    //length units is (half minutes)
    private IntegerProperty length = new SimpleIntegerProperty(this, "length", 0);
    //flags for making announcments
    private ObservableList<Integer> announcementTimes = FXCollections.observableArrayList();
    private BooleanProperty countdown = new SimpleBooleanProperty(this, "countdown", true);
    private BooleanProperty teachingPeriod = new SimpleBooleanProperty(this, "teachingPeriod", false);
    private BooleanProperty waterPeriod = new SimpleBooleanProperty(this, "waterPeriod", false);

    public Period() {
        this.length.set(10);//default length is 5 minutes with several announcements
        addAnnouncementTime(5);
        addAnnouncementTime(2);
        addAnnouncementTime(1);
    }

    public Period(int length) {
        this.length.set(length);
    }

    public boolean doesAnnounceTime(int halfMinutes) {
        boolean announce = false;
        for (Integer time : announcementTimes) {
            if (time == halfMinutes)
                announce = true;
        }
        return announce;
    }

    public void removeAnnouncementTime(int halfMinutes) {
        for (Integer time : announcementTimes) {
            if (time == halfMinutes) {
                announcementTimes.remove(time);
                break;
            }
        }
    }

    public void addAnnouncementTime(int halfMinutes) {
        for (Integer time : announcementTimes) {//check to make sure it doesn't already exist
            if (time == halfMinutes)
                return;
        }
        announcementTimes.add(halfMinutes);
        FXCollections.sort(announcementTimes);
        FXCollections.reverse(announcementTimes);
    }

    public ObservableList<Integer> getAnnouncementTimes() {
        return announcementTimes;
    }

    public void setAnnouncementTimes(ObservableList<Integer> announcementTimes) {
        this.announcementTimes = announcementTimes;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(formatHalfMinutes(length.get()));
        stringBuilder.append("  Announce Time { ");
        FXCollections.sort(announcementTimes);
        FXCollections.reverse(announcementTimes);
        for (int i = 0; i < announcementTimes.size(); i++) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(formatHalfMinutes(announcementTimes.get(i)));
        }
        if (countdown.get()) {
            if (announcementTimes.size() > 0) {
                stringBuilder.append(", 0:05");
            } else {
                stringBuilder.append("0:05");
            }
        }
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }

    public String getLengthString() {
        return formatHalfMinutes(length.get());
    }

    public String getAnnouncementTimesString() {
        StringBuilder stringBuilder = new StringBuilder();
        FXCollections.sort(announcementTimes);
        FXCollections.reverse(announcementTimes);
        if (teachingPeriod.get())
            stringBuilder.append("teach");
        for (int i = 0; i < announcementTimes.size(); i++) {
            if (i != 0 || teachingPeriod.get()) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(formatHalfMinutes(announcementTimes.get(i)));
        }
        if (countdown.get()) {
            if (announcementTimes.size() > 0) {
                stringBuilder.append(", 0:05");
            } else {
                stringBuilder.append("0:05");
            }
        }

        return stringBuilder.toString();
    }

    public final void setLength(Integer value) {
        length.set(value);
    }

    public final Integer getLength() {
        return length.get();
    }

    public final IntegerProperty lengthProperty() {
        return length;
    }

    public final void setCountdown(Boolean value) {
        countdown.set(value);
    }

    public final Boolean getCountdown() {
        return countdown.get();
    }

    public final BooleanProperty countdownProperty() {
        return countdown;
    }

    public final void setTeachingPeriod(Boolean value) {
        teachingPeriod.set(value);
    }

    public final Boolean getTeachingPeriod() {
        return teachingPeriod.get();
    }

    public final BooleanProperty teachingPeriodProperty() {
        return teachingPeriod;
    }

    public final void setWaterPeriod(Boolean value) {
        waterPeriod.set(value);
    }

    public final Boolean getWaterPeriod() {
        return waterPeriod.get();
    }

    public final BooleanProperty waterPeriodProperty() {
        return waterPeriod;
    }

    //</editor-fold>
}
