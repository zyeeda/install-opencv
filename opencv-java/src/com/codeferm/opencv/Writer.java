/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 29, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.VideoWriter;

/**
 * Example of VideoWriter class.
 * 
 * args[0] = source file or will default to "../resources/traffic.mp4" if no
 * args passed.
 * 
 * The following codecs were tested using Gstreamer Opencv backend (FourCC
 * value):
 * 
 * Codec Container ===== ========= DIVX avi XVID avi
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Writer {
    /**
     * Serializable class version number.
     */
    private static final long serialVersionUID = -3988850198352906350L;
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger(CaptureUI.class
            .getName());
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Private constructor prevents instantiation by untrusted callers.
     */
    private Writer() {
    }

    /**
     * Main method.
     * 
     * args[0] = source file or will default to "../resources/traffic.mp4" if no
     * args passed.
     * 
     * @param args
     *            Arguments passed.
     */
    public static void main(final String[] args) {
        String url = null;
        final String outputFile = "../output/people-detect-java.avi";
        // Check how many arguments were passed in
        if (args.length == 0) {
            // If no arguments were passed then default to
            // ../resources/traffic.mp4
            url = "../resources/traffic.mp4";
        } else {
            url = args[0];
        }
        // Custom logging properties via class loader
        try {
            LogManager.getLogManager().readConfiguration(
                    Canny.class.getClassLoader().getResourceAsStream(
                            "logging.properties"));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, String.format("OpenCV %s", Core.VERSION));
        logger.log(Level.INFO, String.format("Input file: %s", url));
        logger.log(Level.INFO, String.format("Output file: %s", outputFile));
        VideoCapture videoCapture = new VideoCapture(url);
        final Size frameSize = new Size(
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        logger.log(Level.INFO, String.format("Resolution: %s", frameSize));
        final FourCC fourCC = new FourCC("DIVX");
        VideoWriter videoWriter = new VideoWriter(outputFile, fourCC.toInt(),
                videoCapture.get(Highgui.CV_CAP_PROP_FPS), frameSize, true);
        final Mat mat = new Mat();
        int frames = 0;
        final long startTime = System.currentTimeMillis();
        while (videoCapture.read(mat)) {
            videoWriter.write(mat);
            frames++;
        }
        final long estimatedTime = System.currentTimeMillis() - startTime;
        logger.log(Level.INFO, String.format("%d frames", frames));
        logger.log(Level.INFO, String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
    }
}
