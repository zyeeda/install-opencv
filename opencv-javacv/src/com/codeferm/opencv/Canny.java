/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 23, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAnd;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.BORDER_DEFAULT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.Canny;
import static com.googlecode.javacv.cpp.opencv_imgproc.GaussianBlur;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Canny Edge Detection of video.
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
     * Serializable class version number.
     */
    private static final long serialVersionUID = -3988850198352906352L;

    /**
     * Create window, frame and set window to visible.
     * 
     * args[0] = source file or will default to "../resources/traffic.mp4" if no
     * args passed.
     * 
     * @param args
     *            String array of arguments.
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        String url = null;
        // Check how many arguments were passed in
        if (args.length == 0) {
            // If no arguments were passed then default to
            // ../resources/traffic.mp4
            url = "../resources/traffic.mp4";
        } else {
            url = args[0];
        }
        System.out.println(String.format("URL: %s", url));
        FrameGrabber videoCapture = new FFmpegFrameGrabber(url);
        videoCapture.start();
        IplImage image = videoCapture.grab();
        System.out.println(String.format("Resolution: %d x %d", image.width(),
                image.height()));
        // Create gray scale IplImage of the same dimensions, 8-bit and 1
        // channel
        IplImage gray = IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);
        IplImage blur = IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);
        IplImage edges = IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);
        IplImage dst = IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 3);
        int frames = 0;
        final long startTime = System.currentTimeMillis();
        do {
            frames++;
            // Convert the image to grayscale
            cvCvtColor(image, gray, CV_BGR2GRAY);
            // Reduce noise with a kernel 3x3
            GaussianBlur(gray, blur, cvSize(3, 3), 0, 0, BORDER_DEFAULT);
            // Canny detector
            Canny(blur, edges, 100, 200, 3, false);
            // Add some colors to edges from original image
            cvAnd(image, image, dst, edges);
            image = videoCapture.grab();
        } while (image != null);
        final long estimatedTime = System.currentTimeMillis() - startTime;
        videoCapture.release();
        cvReleaseImage(gray);
        cvReleaseImage(blur);
        cvReleaseImage(edges);
        cvReleaseImage(dst);
        System.out.println(String.format("%d frames", frames));
        System.out.println(String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
    }
}
