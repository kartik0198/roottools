package com.example.deviceidchanger;

import android.os.Environment;
import com.stericson.RootTools.RootTools;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Properties;

/**
 * Main class of Build.prop Tools
 */
public class BuildProp {

    private static String tempFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp";
    private static String propReplaceFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/propreplace.txt";

    /**
     * Get the value of a property
     *
     * @param key     The name of the property
     * @param useRoot If set to true, root will be required and the values retrieved will be up to date.
     *                If set to false, root will not be required, but the values retrieved will only be updated on reboot.
     * @return The value of the property
     */
    public static String getProp(String key, boolean useRoot) {
        String value = "";
        if (useRoot) {
            Process p = null;
            try {
                p = new ProcessBuilder("/system/bin/getprop", key).redirectErrorStream(true).start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    value = line;
                }
                p.destroy();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return value;
        } else {
            final Properties prop = new Properties();
            Process process = null;
            DataOutputStream os = null;
            createTempFile();

            try {
                FileInputStream in = new FileInputStream(new File(tempFile));
                prop.load(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (prop.getProperty(key) != null) {
                value = prop.getProperty(key);
                new File(tempFile).delete();
            }
            return value;
        }
    }

    /**
     * Gets all properties as a string array
     *
     * @return All properties as a string array
     */
    @SuppressWarnings("unused")
    public static String[] getPropArray() {
        final Properties prop = new Properties();
        Process process = null;
        DataOutputStream os = null;
        String[] values = {};
        createTempFile();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        values = (String[]) prop.stringPropertyNames().toArray();
        new File(tempFile).delete();
        return values;
    }

    /**
     * Gets a property's name and value and stores them in a HashMap
     *
     * @param key The name of the property
     * @return The name and value in a HashMap
     */
    @SuppressWarnings("unused")
    public static HashMap<String, String> getPropAsHash(String key) {
        final Properties prop = new Properties();
        Process process = null;
        DataOutputStream os = null;
        HashMap<String, String> hash = new HashMap<String, String>();
        createTempFile();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (prop.getProperty(key) != null) {
            hash.put(key, prop.getProperty(key));
            new File(tempFile).delete();
            return hash;
        }
        new File(tempFile).delete();
        hash.put("", "");
        return hash;
    }

    /**
     * Sets the value of a property
     *
     * @param key   The name of the property
     * @param value The value to set the property to
     */
    public static void setProp(String key, String value) {
        final Properties prop = new Properties();
        createTempFile();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (prop.containsKey(key))
            prop.setProperty(key, value);

        try {
            FileOutputStream out = new FileOutputStream(new File(tempFile));
            prop.store(out, null);
            out.close();

            replaceInFile(new File(tempFile));
            transferFileToSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a property
     *
     * @param key The name of the property
     */
    public static void removeProp(String key) {
        final Properties prop = new Properties();
        createTempFile();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (prop.containsKey(key))
            prop.remove(key);

        try {
            FileOutputStream out = new FileOutputStream(new File(tempFile));
            prop.store(out, null);
            out.close();

            replaceInFile(new File(tempFile));
            transferFileToSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a property exists
     *
     * @param key The name of the property
     * @return Whether or not the property exists
     */
    @SuppressWarnings("unused")
    public static boolean propExists(String key) {
        final Properties prop = new Properties();
        Process process = null;
        DataOutputStream os = null;
        createTempFile();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (prop.getProperty(key) != null) {
            boolean exists = prop.containsKey(key);
            new File(tempFile).delete();
            return exists;
        }
        new File(tempFile).delete();
        return false;
    }

    /**
     * Makes a backup of build.prop, transfers the temporary file to
     * /system/build.prop and cleans up<br>
     * <br>
     * This method is called from setProp() so you don't need to call it
     * manually.
     */
    private static void transferFileToSystem() {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            RootTools.remount("/system/", "rw");
            os.writeBytes("mv -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak\n");
            RootTools.copyFile(propReplaceFile, "/system/build.prop", false, false);
            os.writeBytes("chmod 644 /system/build.prop\n");
            RootTools.remount("/system/", "ro");
            os.writeBytes("rm " + propReplaceFile + "\n");
            os.writeBytes("rm " + tempFile + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inserts and replaces your changes into a temporary file <br>
     * <br>
     * You do not need to call this method.
     *
     * @param file The file to replace into the second temporary file
     * @throws IOException
     */
    private static void replaceInFile(File file) throws IOException {
        File tmpFile = new File(propReplaceFile);
        FileWriter fw = new FileWriter(tmpFile);
        Reader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        while (br.ready()) {
            fw.write(br.readLine().replaceAll("\\\\", "") + "\n");
        }

        fw.close();
        br.close();
        fr.close();
    }

    /**
     * Creates a temporary copy of build.prop to make changes to <br>
     * <br>
     * You do not need to call this method.
     */
    private static void createTempFile() {
        Process process = null;
        DataOutputStream os = null;

        try {
            RootTools.remount("/system/", "rw");
            RootTools.copyFile("/system/build.prop", Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp", false, true);
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("chmod 777 " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp\n");
            //RootTools.remount("/system/", "ro");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp";
    }

}

