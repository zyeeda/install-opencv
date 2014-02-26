/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 23, 2013
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
import org.opencv.imgproc.Imgproc;

/**
 * Canny Edge Detector.
 * 
 * args[0] = source file or will default to "../resources/traffic.mp4" if no
 * args passed.
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class Canny {
    /**
     * Logger.
     */
    private static final Logger logger = Logger
            .getLogger(Canny.class.getName());
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Private constructor prevents instantiation by untrusted callers.
     */
    private Canny() {
    }

    /**
     * Create window, frame and set window to visible.
     * 
     * args[0] = source file or will default to "../resources/traffic.mp4" if no
     * args passed.
     * 
     * @param args
     *            String array of arguments.
     */
    public static void main(final String[] args) {
        String url = null;
        final String outputFile = "../output/canny-java.avi";
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
        final Mat gray = new Mat();
        final Mat blur = new Mat();
        final Mat edges = new Mat();
        final Mat dst = new Mat();
        final Size kSize = new Size(3, 3);
        final long startTime = System.currentTimeMillis();
        while (videoCapture.read(mat)) {
            // Convert the image to grayscale
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
            // Reduce noise with a kernel 3x3
            Imgproc.GaussianBlur(gray, blur, kSize, 0);
            // Canny detector
            Imgproc.Canny(blur, edges, 100, 200, 3, false);
            // Add some colors to edges from original image
            Core.bitwise_and(mat, mat, dst, edges);
            videoWriter.write(dst);
            dst.release();
            frames++;
        }
        final long estimatedTime = System.currentTimeMillis() - startTime;
        logger.log(Level.INFO, String.format("%d frames", frames));
        logger.log(Level.INFO, String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
        // Release native memory
        mat.release();
        gray.release();
        blur.release();
        edges.release();
        dst.release();
    }
}
