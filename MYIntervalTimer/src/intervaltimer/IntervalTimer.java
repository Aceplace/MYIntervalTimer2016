/*
 * Project: MYIntervalTimer
 * Package: intervaltimer
 * Name: IntervalTimer
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 22, 2016
 */
package intervaltimer;

import script.Period;
import script.Script;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import musicplayer.MusicPlayer;

enum AnnouncementType {
    ANNOUNCE_PERIOD_START,
    ANNOUNCE_TIME,
    ANNOUNCE_COUNT_DOWN,
    ANNOUNCE_SCRIPT_END
}

public class IntervalTimer {

    //music player reference needed so interval timer can lower volume to make announcements
    MusicPlayer musicPlayer;

    private static Map<String, AudioClip> soundClips = new HashMap<>();

    //load in all sound clips
    static {
        AudioClip temp;
        String clipName;
        //load period announcement audio clips
        //there are 41 total clips all following the same convention "PeriodPP"
        for (int i = 0; i <= 40; i++) {
            if (i < 10) {
                clipName = "Period0" + i;
            } else {
                clipName = "Period" + i;
            }

            soundClips.put(clipName, new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/" + clipName + ".wav").toString()));
        }

        //load time remaining audio clips
        //there are 60 total clips all following the same convention "Remainingmmss"
        for (int halfminutes = 1; halfminutes <= 60; halfminutes++) {
            clipName = "Remaining";
            if (halfminutes / 2 == 0) {
                clipName += "00";
            } else if (halfminutes / 2 < 10) {
                clipName += "0" + (halfminutes / 2);
            } else {
                clipName += "" + (halfminutes / 2);
            }

            if (halfminutes % 2 == 1) {
                clipName += "30";
            } else {
                clipName += "00";
            }

            soundClips.put(clipName, new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/" + clipName + ".wav").toString()));
        }

        //load in other clips (coundown, scriptend, shortbeeps, longbeep)
        soundClips.put("FiveCountdown", new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/FiveCountdown.wav").toString()));
        soundClips.put("BeepLong", new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/BeepLong.wav").toString()));
        soundClips.put("BeepThree", new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/BeepThree.wav").toString()));
        soundClips.put("EndOfScript", new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/EndOfScript.wav").toString()));
        soundClips.put("Water", new AudioClip(IntervalTimer.class.getClassLoader().getResource("resources/sounds/Water.wav").toString()));
    }

    private IntervalTimerSettings settings;
    private Script script;
    private ObjectProperty<Period> currentPeriod = new SimpleObjectProperty<>(this, "currentPeriod", null);
    private IntegerProperty periodIndex = new SimpleIntegerProperty(this, "periodIndex", 0);

    private BooleanProperty playing = new SimpleBooleanProperty(this, "playing", false);
    private Timeline timeline;
    private IntegerProperty timeRemaining = new SimpleIntegerProperty(this, "timeRemaining", 0);
    private IntegerProperty time = new SimpleIntegerProperty(this, "time", 0);
    private boolean omitTimeAnnouncements = false;

    //create listeners to link time and timeline
    //need references to the listeners because they are removed by the close method before stopping the playback
    private ChangeListener<Number> timeListener = new ChangeListener<Number>() {
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            timeline.playFrom(Duration.seconds(newValue.doubleValue()));
        }
    };

    private ChangeListener<Duration> timelineListener = new ChangeListener<Duration>() {
        public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
            timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
            time.set((int) timeline.getCurrentTime().toSeconds());
        }

    };

    public IntervalTimer(Script script, MusicPlayer musicPlayer) {
        this.script = script;
        this.musicPlayer = musicPlayer;
        currentPeriod.set(script.getPeriods().get(0));
        timeline = new Timeline();
        playing.bind(Bindings.notEqual(timeline.currentRateProperty(), 0));

        //add listener to affect playback of timeline
        time.addListener(timeListener);
        timeline.currentTimeProperty().addListener(timelineListener);

    }

    public void startPeriod(boolean announceStart) {
        //set up timeline and keyframes. timeline length is determined by script
        //multiplication by 30 is due to units being (half minutes) in script
        //timeline.getKeyFrames().removeAll(timeline.getKeyFrames());//remove previous period keyframes
        if (currentPeriod.get().getTeachingPeriod()) {
            musicPlayer.setVolume(0.075);
        } else if (!musicPlayer.isFading())
            musicPlayer.setVolume(musicPlayer.getMaxVolume());
        timeline.getKeyFrames().clear();

        KeyFrame start = new KeyFrame(Duration.seconds(0), null);
        KeyFrame end = new KeyFrame(Duration.seconds(currentPeriod.get().getLength() * 30), e -> {
            if (periodIndex.get() < script.getPeriods().size() - 1) {
                nextPeriod(true);
            } else {
                makeAnnouncement(AnnouncementType.ANNOUNCE_SCRIPT_END);
            }
        });

        timeline.getKeyFrames().addAll(start, end);

        //add keyframes for announcements only when neccesary
        for (int announcementTime : currentPeriod.get().getAnnouncementTimes()) {
            //add a keyframe to perform the fade
            double fadeStartTime = (currentPeriod.get().getLength() * 30 - announcementTime * 30) - 1.5;
            if (fadeStartTime >= 0 && !currentPeriod.get().getTeachingPeriod()) {
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(fadeStartTime),
                        e -> {
                            if (!musicPlayer.isFading() && omitTimeAnnouncements == false && !currentPeriod.get().getTeachingPeriod()) {
                                musicPlayer.performFade(1200, 5000);
                            }
                        }));
            }
            //add a keyframe to do the announcement
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(currentPeriod.get().getLength() * 30 - announcementTime * 30),
                    e -> makeAnnouncement(AnnouncementType.ANNOUNCE_TIME,
                            currentPeriod.get().getLength() * 30 - (int) ((KeyFrame) e.getSource()).getTime().toSeconds())));
        }

        if (currentPeriod.get().getCountdown() == true) {
            //add a keyframe to perform the fade
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(currentPeriod.get().getLength() * 30 - 5 - 1.5),
                    e -> {
                        if (!musicPlayer.isFading() && omitTimeAnnouncements == false && !currentPeriod.get().getTeachingPeriod()) {
                            musicPlayer.performFade(1200, 12000);
                        }
                    }));
            //add a keyframe to do the announcement
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(currentPeriod.get().getLength() * 30 - 5),
                    e -> makeAnnouncement(AnnouncementType.ANNOUNCE_COUNT_DOWN)));
        }
        //still need to fade out before the period ends if we haven't
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(currentPeriod.get().getLength() * 30 - 1.5),
                e -> {
                    if (!musicPlayer.isFading() && !currentPeriod.get().getTeachingPeriod()) {
                        musicPlayer.performFade(1200, 7000);
                    }
                }));

        timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
        time.set((int) timeline.getCurrentTime().toSeconds());
        timeline.playFromStart();
        if (announceStart) {
            makeAnnouncement(AnnouncementType.ANNOUNCE_PERIOD_START);
        }

    }

    public void restartPeriod() {
        time.set(0);
    }

    //Makes a verbal announcement of specified type to indicate how much time remains
    //in period
    //This version doesn't handle time announcements
    public void makeAnnouncement(AnnouncementType announcementType) {
        String clipName;

        if (announcementType == AnnouncementType.ANNOUNCE_PERIOD_START) {
            //fade out music if neccessary
            if (!musicPlayer.isFading() && !currentPeriod.get().getTeachingPeriod()) {
                musicPlayer.performFade(1000, 7000);
            }

            int period = script.getTruePeriod(periodIndex.get());
            /*if (script.isPeriodZero() == false) {
                period++;
            }*/
            if (period < 10) {
                clipName = "Period0" + period;
            } else {
                clipName = "Period" + period;
            }

            if (script.getPeriodNumberAsString(periodIndex.get()).equals("W"))
                clipName = "Water";

            //announcer has to make announcement after the beep
            //a timeline is used to get the clips to play sequentially
            final String fpclipName = clipName;
            KeyFrame a1 = new KeyFrame(Duration.seconds(0), e -> soundClips.get("BeepLong").play());
            KeyFrame a2 = new KeyFrame(Duration.millis(3200), e -> soundClips.get(fpclipName).play());
            Timeline announcement = new Timeline(a1, a2);

            //check to see if we need to announce the time remaining
            for (int announcementTime : currentPeriod.get().getAnnouncementTimes()) {
                if (announcementTime == currentPeriod.get().getLength()) {
                    int halfminutes = currentPeriod.get().getLength();
                    clipName = "Remaining";
                    if (halfminutes / 2 == 0) {
                        clipName += "00";
                    } else if (halfminutes / 2 < 10) {
                        clipName += "0" + (halfminutes / 2);
                    } else {
                        clipName += "" + (halfminutes / 2);
                    }

                    if (halfminutes % 2 == 1) {
                        clipName += "30";
                    } else {
                        clipName += "00";
                    }

                    final String faclipName = clipName;
                    announcement.getKeyFrames().add(new KeyFrame(Duration.millis(4500), e -> soundClips.get(faclipName).play()));

                }

            }

            announcement.play();
        }

        if (announcementType == AnnouncementType.ANNOUNCE_COUNT_DOWN && omitTimeAnnouncements == false) {
            soundClips.get("FiveCountdown").play();
        }

        if (announcementType == AnnouncementType.ANNOUNCE_SCRIPT_END) {
            //announcer has to make announcement after the beep
            //a timeline is used to get the clips to play sequentially
            KeyFrame a1 = new KeyFrame(Duration.seconds(0), e -> soundClips.get("BeepLong").play());
            KeyFrame a2 = new KeyFrame(Duration.millis(3200), e -> soundClips.get("EndOfScript").play());
            Timeline announcement = new Timeline(a1, a2);
            announcement.play();

        }
    }

    //This version only handles time announcements
    public void makeAnnouncement(AnnouncementType announcementType, int time) {
        int halfminutes = time / 30;
        if (omitTimeAnnouncements) {
            return;
        }
        if (time / 30 == currentPeriod.get().getLength()) {//announcement made at same time as period start
            return;//don't announce this because period start will announce it
        }
        if (announcementType == AnnouncementType.ANNOUNCE_TIME) {
            String clipName = "Remaining";
            if (halfminutes / 2 == 0) {
                clipName += "00";
            } else if (halfminutes / 2 < 10) {
                clipName += "0" + (halfminutes / 2);
            } else {
                clipName += "" + (halfminutes / 2);
            }

            if (halfminutes % 2 == 1) {
                clipName += "30";
            } else {
                clipName += "00";
            }

            //announcer has to make announcement after the beep
            //a timeline is used to get the clips to play sequentially
            final String fclipName = clipName;
            KeyFrame a1 = new KeyFrame(Duration.ZERO, e -> soundClips.get("BeepThree").play());
            KeyFrame a2 = new KeyFrame(Duration.millis(2600), e -> soundClips.get(fclipName).play());
            Timeline announcement = new Timeline(a1, a2);
            announcement.play();

        }
    }

    public void nextPeriod(boolean announceStart) {
        timeline.stop();
        if (periodIndex.get() < script.getPeriods().size() - 1) {
            currentPeriod.set(script.getPeriods().get(periodIndex.get() + 1));
            //timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
            periodIndex.set(periodIndex.get() + 1);
        } else {
            currentPeriod.set(script.getPeriods().get(0));
            //timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
            periodIndex.set(0);
        }
        startPeriod(announceStart);
    }

    public void previousPeriod(boolean announceStart) {
        timeline.stop();
        if (periodIndex.get() > 0) {
            currentPeriod.set(script.getPeriods().get(periodIndex.get() - 1));
            //timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
            periodIndex.set(periodIndex.get() - 1);
        } else {
            currentPeriod.set(script.getPeriods().get(script.getPeriods().size() - 1));
            //timeRemaining.set(currentPeriod.get().getLength() * 30 - (int) timeline.getCurrentTime().toSeconds());
            periodIndex.set(script.getPeriods().size() - 1);
        }
        startPeriod(announceStart);

    }

    //call to shutdown the timers on the interval timer
    //make sure this is called when you are finished with interval timer
    public void close() {
        if (timeline != null) {
            time.removeListener(timeListener);
            timeline.currentTimeProperty().removeListener(timelineListener);
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeline.stop();
        }

    }

    //call to adjust volume on all the sound clips
    public static void adjustVolume(double volume) {
        for (String key : soundClips.keySet()) {
            soundClips.get(key).setVolume(volume);
        }
    }

    //getters and setters for properties and members
    //<editor-fold>     
    public void setPaused(boolean pause) {
        if (pause) {
            timeline.pause();
        } else {
            if (time.get() == 0) {
                makeAnnouncement(AnnouncementType.ANNOUNCE_PERIOD_START);
            }
            timeline.play();
        }
    }

    public boolean isPaused() {
        return timeline.getCurrentRate() == 0.0;
    }

    public final Period getCurrentPeriod() {
        return currentPeriod.get();
    }

    public ObjectProperty<Period> currentPeriodProperty() {
        return currentPeriod;
    }

    public IntervalTimerSettings getSettings() {
        return settings;
    }

    public void setSettings(IntervalTimerSettings settings) {
        this.settings = settings;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public final void setPeriodIndex(Integer value) {
        periodIndex.set(value);
    }

    public final Integer getPeriodIndex() {
        return periodIndex.get();
    }

    public final IntegerProperty periodIndexProperty() {
        return periodIndex;
    }

    public final void setTimeRemaining(Integer value) {
        timeRemaining.set(value);
    }

    public final Integer getTimeRemaining() {
        return timeRemaining.get();
    }

    public final IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }

    public final void setTime(Integer value) {
        time.set(value);
    }

    public final Integer getTime() {
        return time.get();
    }

    public final IntegerProperty timeProperty() {
        return time;
    }

    public boolean doesOmitTimeAnnouncement() {
        return omitTimeAnnouncements;
    }

    public void setOmitTimeAnnouncement(boolean omitTimeAnnouncements) {
        this.omitTimeAnnouncements = omitTimeAnnouncements;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    //</editor-fold>
}
