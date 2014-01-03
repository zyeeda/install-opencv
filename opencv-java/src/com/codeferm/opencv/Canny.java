/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 23, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.VideoWriter;
import org.opencv.imgproc.Imgproc;

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
    private static final long serialVersionUID = -3988850198352906350L;
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
        // Check how many arguments were passed in
        if (args.length == 0) {
            // If no arguments were passed then default to ../resources/traffic.mp4
            url = "../resources/traffic.mp4";
        } else {
            url = args[0];
        }
        System.out.println(String.format("URL: %s", url));
        VideoCapture videoCapture = new VideoCapture(url);
        final Size frameSize = new Size(
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        System.out.println(String.format("Resolution: %s", frameSize));
        final FourCC fourCC = new FourCC("DIVX");
        VideoWriter videoWriter = new VideoWriter("../output/canny-java.avi",
                fourCC.toInt(), videoCapture.get(Highgui.CV_CAP_PROP_FPS), frameSize, true);        
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
        System.out.println(String.format("%d frames", frames));
        System.out.println(String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
    }
}
