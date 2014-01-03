/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 29, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

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
 * The following codecs were tested using Gstreamer Opencv backend (FourCC value):
 * 
 * Codec Container
 * ===== =========
 * DIVX  avi
 * XVID  avi
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class Writer {
    /**
     * Serializable class version number.
     */
    private static final long serialVersionUID = -3988850198352906350L;
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
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
        VideoCapture videoCapture = new VideoCapture(url);
        System.out.println(String.format("Resolution: %4.0f x%4.0f",
                videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT)));
        final Size frameSize = new Size(
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        final FourCC fourCC = new FourCC("DIVX");
        VideoWriter videoWriter = new VideoWriter("../output/java.avi",
                fourCC.toInt(), 30.0, frameSize, true);
        final Mat mat = new Mat();
        int frames = 0;
        final long startTime = System.currentTimeMillis();
        while (videoCapture.read(mat)) {
            videoWriter.write(mat);
            frames++;
        }
        final long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("%d frames", frames));
        System.out.println(String.format("Elipse time: %4.2f seconds",
                (double) estimatedTime / 1000));
    }
}
