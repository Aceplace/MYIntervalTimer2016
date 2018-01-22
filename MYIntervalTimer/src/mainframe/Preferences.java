/*
 * Project: MYIntervalTimer
 * Package: mainframe
 * Name: Prefrences
 * Copyright (C) MYACEPLACE  - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Michael Yeiter <coachyeiter@gmail.com>, Jun 26, 2016
 */
package mainframe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Preferences {

    String lastFileName;
    String directory;

    public Preferences() {
        File file = new File("preferences.pref");
        
        lastFileName = new String();
        directory = new String();

        if (file.isFile()) {
            try (FileInputStream is = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(is);) {
                lastFileName = (String) ois.readObject();
                File lastFile = new File(lastFileName);
                if (lastFile.isFile()) {
                    directory = lastFile.getParent();
                } else {
                    directory = "";
                }

            } catch (Exception ex) {
                lastFileName = "";
                directory = "";
            }
        }
    }

    public void savePreferences() {
        File file = new File("preferences.pref");

        try (FileOutputStream os = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(os);) {
            oos.writeObject(lastFileName);

        } catch (Exception ex) {

        }

    }

    public String getLastFileName() {
        return lastFileName;
    }

    public void setLastFileName(String lastFileName) {
        this.lastFileName = lastFileName;
        File lastFile = new File(this.lastFileName);
        if (lastFile.isFile()) {
            directory = lastFile.getParent();
        } else {
            this.lastFileName = "";
            directory = "";
        }
    }

    public String getDirectory() {
        return directory;
    }
}
