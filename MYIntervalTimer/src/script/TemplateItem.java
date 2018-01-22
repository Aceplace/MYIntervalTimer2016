/*
 * Project: MYIntervalTimer
 * Package: script
 * Name: TemplateItem
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 23, 2016
 */
package script;

public class TemplateItem {

    private Period period;
    private String name;

    public TemplateItem(Period period, String name) {
        this.period = period;
        this.name = name;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}
