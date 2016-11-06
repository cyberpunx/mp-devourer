package ingestserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provide functions to process video files by FFmpeg/FFprobe. FFmpeg and
 * FFprobe paths are read from configuration file.
 *
 * @author cyberpunx
 */
public class FileProcessor {

    private String ffprobe;
    private final String ffmpeg;

    /**
     * Reads FFmpeg and FFprobe paths from properties file.
     */
    public FileProcessor() {
        Config config = null;
        try {
            config = new Config();
        } catch (IOException ex) {
            Logger.getLogger(DirectoryCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.ffmpeg = config.getFfmpeg();
        this.ffprobe = config.getFfprobe();
    }

    /**
     * Just adds PT[input]S to a String given representing seconds.
     *
     * @param seconds: Just a String in seconds
     * @return a String in seconds followin the ISO 8601 format.
     */
    public String secondsToDuration(String seconds) {
        return "PT" + seconds + "S";
    }

    /**
     * Takes the Ffprobe Framerate as a rational number and gives the integer
     * result, rounded up.
     *
     * @param fpsInput Framerate as given by Ffprobe in x/x format.
     * @return Framerate result, rounded up.
     */
    public String getFPS(String fpsInput) {
        String[] parts = fpsInput.split("/");
        int part1 = Integer.parseInt(parts[0]);
        int part2 = Integer.parseInt(parts[1]);
        int fpsint = (int) Math.ceil((double) part1 / part2); //rounds up
        String fps = Integer.toString(fpsint);
        return fps;
    }

    /**
     * Given a video file, creates a directory and, using Ffmpeg, fills that
     * directory with thumbnails from the video.
     *
     * @param file video file
     * @param duration integer duration in seconds of video
     * @param thumbCount number of generated thumbnails
     * @return an array with all thumbnails absolute paths
     * @throws IOException
     */
    public List<String> generateThumbnailGIF(File file, String duration, String thumbCount) throws IOException {
        String[] cmdOut;
        duration = duration.split("\\.")[0];
        List<String> thumbArray = new ArrayList<>();

        //get filename without extension
        String fname = file.getName();
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);
        }
        File thumbDir = new File(file.getParent() + "/" + fname);
        thumbDir.mkdir();

        cmdOut = execFfmpeg("-i " + file.getAbsolutePath() + " -vf fps=" + thumbCount + "/" + duration + " " + thumbDir + "/" + fname + "_%03d.png ");

        String dirPath = thumbDir.getAbsolutePath();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files.length == 0) {
            System.out.println("The directory is empty");
        } else {
            for (File aFile : files) {
                if (aFile.toString().toLowerCase().endsWith(".png")) {
                    thumbArray.add(aFile.getAbsolutePath());
                }
            }
        }

        //thumbArray.forEach(System.out::println);
        return thumbArray;
    }

    /**
     * Executes a FFprobe commands and returns the output.
     *
     * @param cmd command to be executed by ffprobe.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execFfprobe(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(ffprobe + " " + cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    /**
     * Executes a terminal command and return the output.
     *
     * @param cmd command to be executed.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execCmd(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    /**
     * Executes a FFmpeg commands and returns the output.
     *
     * @param cmd command to be executed.
     * @return array of 2 Strings. Position 0 is output. Position 1 is error.
     * @throws IOException
     */
    public String[] execFfmpeg(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(ffmpeg + " " + cmd);
        BufferedReader output = getOutput(p);
        BufferedReader error = getError(p);
        String line, outprint, errorprint;
        line = outprint = errorprint = "";

        while ((line = output.readLine()) != null) {
            outprint = outprint + line;
        }

        while ((line = error.readLine()) != null) {
            errorprint = errorprint + line;
        }

        return new String[]{outprint, errorprint};
    }

    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    /**
     * Given a file, returns if it is a valid format to be processed by
     * FileProcessor.
     *
     * @param file to be analyzed as a valid format.
     * @return True if File is a valid forma (video). False otherwise.
     */
    public boolean isCorrectFileType(Path file) {
        boolean flag = false;
        if (file.toString().toLowerCase().endsWith(".mp4")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".webm")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mkv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".avi")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".flv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mpeg")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".gif")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".m4v")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".wmv")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".mov")) {
            flag = true;
        } else if (file.toString().toLowerCase().endsWith(".txt")) {
            flag = true;
        }
        return flag;
    }

}