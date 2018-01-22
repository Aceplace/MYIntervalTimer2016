/*
 * Project: MYIntervalTimer
 * Package: musicplayer
 * Name: MusicFader
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 28, 2016
 */
package musicplayer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class MusicFader {

    private MusicPlayer musicPlayer;

    private Timeline fadeTimeline = new Timeline();

    private KeyFrame fadeOutFrameStart;
    private KeyValue fadeOutValueStart;

    private KeyFrame fadeOutFrameEnd;
    private KeyValue fadeOutValueEnd;

    private KeyFrame fadeInFrameStart;
    private KeyValue fadeInValueStart;

    private KeyFrame fadeInFrameEnd;
    private KeyValue fadeInValueEnd;

    private DoubleProperty volume = new SimpleDoubleProperty(this, "volume", 0.0);

    public MusicFader(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;

        volume.addListener((volumeProperty, oldValue, newValue) -> {
            musicPlayer.setVolume(newValue.doubleValue());
        });

        fadeTimeline.setOnFinished(e -> {
            fadeTimeline.getKeyFrames().clear();
            volume.set(musicPlayer.getMaxVolume());
        });
    }

    public void performFade(double fadeTimeMillis, double pauseTimeMillis) {
        //fading will be done using a timeline and key frames
        //the keys will set the volume in this fader
        //the volume in this fade will set the volume for the music player
        haltFade();
        if (pauseTimeMillis > 0) {
            fadeOutValueStart = new KeyValue(volume, musicPlayer.getMaxVolume());
            fadeOutFrameStart = new KeyFrame(Duration.ZERO, fadeOutValueStart);
            fadeTimeline.getKeyFrames().add(fadeOutFrameStart);

            fadeOutValueEnd = new KeyValue(volume, 0.05);
            fadeOutFrameEnd = new KeyFrame(new Duration(fadeTimeMillis), fadeOutValueEnd);
            fadeTimeline.getKeyFrames().add(fadeOutFrameEnd);

            fadeInValueStart = new KeyValue(volume, 0.05);
            fadeInFrameStart = new KeyFrame(new Duration(fadeTimeMillis + pauseTimeMillis), fadeInValueStart);
            fadeTimeline.getKeyFrames().add(fadeInFrameStart);

            fadeInValueEnd = new KeyValue(volume, musicPlayer.getMaxVolume());
            fadeInFrameEnd = new KeyFrame(new Duration(2 * fadeTimeMillis + pauseTimeMillis), fadeInValueEnd);
            fadeTimeline.getKeyFrames().add(fadeInFrameEnd);
        }

        fadeTimeline.playFromStart();
    }

    public void haltFade() {
        fadeTimeline.getKeyFrames().clear();
        fadeTimeline.stop();
        volume.set(musicPlayer.getMaxVolume());
    }

    public boolean isFading() {
        if (fadeTimeline == null)
            return false;
        return fadeTimeline.getCurrentRate() != 0.0;
    }

}
