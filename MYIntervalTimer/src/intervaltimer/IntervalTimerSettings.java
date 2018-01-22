/*
 * Project: MYIntervalTimer
 * Package: intervaltimer
 * Name: IntervalTimerSettings
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
package intervaltimer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class IntervalTimerSettings {

    private BooleanProperty announcements = new SimpleBooleanProperty(this, "announcements", true);

    public final void setAnnouncements(Boolean value) {
        announcements.set(value);
    }

    public final Boolean getAnnouncements() {
        return announcements.get();
    }

    public final BooleanProperty announcementsProperty() {
        return announcements;
    }

}
