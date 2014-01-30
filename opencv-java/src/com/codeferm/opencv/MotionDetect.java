/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on January 4, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.VideoWriter;
import org.opencv.imgproc.Imgproc;

/**
 * Uses moving average to determine change percent.
 * 
 * args[0] = source file or will default to "../resources/traffic.mp4" if no
 * args passed.
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class MotionDetect {
    /**
     * Serializable class version number.
     */
    private static final long serialVersionUID = -3988850198352906350L;
    /**
     * Logger.
     */
    // CHECKSTYLE:OFF This is not a constant, so naming convenetion is correct
    private static final Logger logger = Logger.getLogger(MotionDetect.class
            .getName());
    // CHECKSTYLE:ON
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
     * Kernel used for contours.
     */
    private static final Mat contourKernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_DILATE, new Size(3, 3), new Point(1, 1));
    /**
     * Contour hierarchy.
     */
    private static final Mat hierarchy = new Mat();
    /**
     * Point used for contour dilate and erode.
     */
    private static final Point contourPoint = new Point(-1, -1);

    /**
     * Private constructor prevents instantiation by untrusted callers.
     */
    private MotionDetect() {
    }

    /**
     * Get contours from image.
     * 
     * @param source
     *            Source image.
     * @return List of rectangles.
     */
    public static List<Rect> contours(final Mat source) {
        Imgproc.dilate(source, source, contourKernel, contourPoint, 15);
        Imgproc.erode(source, source, contourKernel, contourPoint, 10);
        final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(source, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> rectList = new ArrayList<Rect>();
        // Convert MatOfPoint to Rectangles
        for (MatOfPoint mop : contours) {
            rectList.add(Imgproc.boundingRect(mop));
        }
        return rectList;
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
        final String outputFile = "../output/motion-detect-java.avi";
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
                    MotionDetect.class.getClassLoader().getResourceAsStream(
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
        final Mat workImg = new Mat();
        Mat movingAvgImg = null;
        final Mat gray = new Mat();
        final Mat diffImg = new Mat();
        final Mat scaleImg = new Mat();
        final Point rectPoint1 = new Point();
        final Point rectPoint2 = new Point();
        final Scalar rectColor = new Scalar(0, 255, 0);
        final Size kSize = new Size(8, 8);
        final double totalPixels = frameSize.area();
        double motionPercent = 0.0;
        int framesWithMotion = 0;
        final long startTime = System.currentTimeMillis();
        while (videoCapture.read(mat)) {
            // Generate work image by blurring
            Imgproc.blur(mat, workImg, kSize);
            // Generate moving average image if needed
            if (movingAvgImg == null) {
                movingAvgImg = workImg.clone();
                movingAvgImg.convertTo(movingAvgImg, CvType.CV_32F);

            }
            // Generate moving average image
            Imgproc.accumulateWeighted(workImg, movingAvgImg, .03);
            Core.convertScaleAbs(movingAvgImg, scaleImg);
            Core.absdiff(workImg, scaleImg, diffImg);
            // Convert the image to grayscale
            Imgproc.cvtColor(diffImg, gray, Imgproc.COLOR_BGR2GRAY);
            // Convert to BW
            Imgproc.threshold(gray, gray, 25, 255, Imgproc.THRESH_BINARY);
            // Total number of changed motion pixels
            motionPercent = 100.0 * Core.countNonZero(gray) / totalPixels;
            // Detect if camera is adjusting and reset reference if more than
            // maxChange
            if (motionPercent > 25.0) {
                movingAvgImg = workImg.clone();
                movingAvgImg.convertTo(movingAvgImg, CvType.CV_32F);
            }
            List<Rect> movementLocations = contours(gray);
            // Threshold trigger motion
            if (motionPercent > 0.75) {
                framesWithMotion++;
                for (Rect rect : movementLocations) {
                    rectPoint1.x = rect.x;
                    rectPoint1.y = rect.y;
                    rectPoint2.x = rect.x + rect.width;
                    rectPoint2.y = rect.y + rect.height;
                    // Draw rectangle around fond object
                    Core.rectangle(mat, rectPoint1, rectPoint2, rectColor, 2);
                }
            }
            videoWriter.write(mat);
            frames++;
        }
        final long estimatedTime = System.currentTimeMillis() - startTime;
        logger.log(Level.INFO, String.format(
                "%d frames, %d frames with motion", frames, framesWithMotion));
        logger.log(Level.INFO, String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
    }
}
