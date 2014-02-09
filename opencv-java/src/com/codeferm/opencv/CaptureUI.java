/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 21, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * A simple video capture applet. The Java bindings do not have an imshow
 * equivalent (highgui wrapper) yet.
 * 
 * args[0] = camera index, url or will default to "0" if no args passed.
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CaptureUI extends Applet implements Runnable {
    /**
     * Serializable class version number.
     */
    private static final long serialVersionUID = -3988850198352906349L;
    /**
     * Logger.
     */
    // CHECKSTYLE:OFF This is not a constant, so naming convenetion is correct
    private static final Logger logger = Logger.getLogger(CaptureUI.class
            .getName());
    // CHECKSTYLE:ON
    /* Load the OpenCV system library */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
     * Class for video capturing from video files or cameras.
     */
    private VideoCapture videoCapture = null;
    /**
     * Frame size.
     */
    private Size frameSize = null;
    /**
     * Holds conversion from Mat to BufferedImage.
     */
    private byte[] pixelBytes = null;
    /**
     * Applet drawing canvas.
     */
    private BufferedImage bufferedImage = null;
    /**
     * Processing thread.
     */
    private Thread captureThread = null;

    /**
     * Initialize VideoCapture.
     * 
     * @param url
     *            Camera URL.
     */
    public CaptureUI(final String url) {
        // See if URL is an integer: -? = negative sign, could have none or one,
        // \\d+ = one or more digits
        if (url.matches("-?\\d+")) {
            videoCapture = new VideoCapture(Integer.parseInt(url));
        } else {
            videoCapture = new VideoCapture(url);
        }
        // Custom logging properties via class loader
        try {
            LogManager.getLogManager().readConfiguration(
                    CaptureUI.class.getClassLoader().getResourceAsStream(
                            "logging.properties"));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        frameSize = new Size(
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                (int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        logger.log(Level.INFO, String.format("OpenCV %s", Core.VERSION));
        logger.log(Level.INFO, "Press [Esc] to exit");
        logger.log(Level.INFO, String.format("URL: %s", url));
        init();
    }

    /**
     * VideoCapture accessor.
     * 
     * @return VideoCapture.
     */
    public VideoCapture getCap() {
        return videoCapture;
    }

    /**
     * Frame size accessor.
     * 
     * @return Frame size.
     */
    public Size getFrameSize() {
        return frameSize;
    }

    /**
     * Displays diagnostic information.
     */
    @Override
    public void init() {
        logger.log(Level.INFO, String.format("Resolution: %s", frameSize));
    }

    /**
     * Create frame acquisition thread and start.
     */
    @Override
    public void start() {
        if (captureThread == null) {
            captureThread = new Thread(this);
            captureThread.start();
        }
    }

    /**
     * Stop frame acquisition thread.
     */
    @Override
    public void stop() {
        if (captureThread != null) {
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            captureThread = null;
        }
    }

    /**
     * Read frame, convert from Mat to byte array, repaint canvas with new
     * frame.
     */
    @Override
    public void run() {
        final Mat mat = new Mat();
        while (true) {
            if (videoCapture.read(mat)) {
                /*
                 * Add image processing code here.
                 */
                convert(mat);
                repaint();
            } else {
                break;
            }
            try {
                // You will max out at ~50 FPS
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Convert from Mat to BufferedImage.
     * 
     * @param mat
     *            Mat array.
     */
    public void convert(final Mat mat) {
        final Mat mat2 = new Mat();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        // Color image
        if (mat.channels() > 1) {
            Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        final int pixels = mat.channels() * mat.cols() * mat.rows();
        // Create byte array if null or different length
        if (pixelBytes == null || pixelBytes.length != pixels) {
            pixelBytes = new byte[pixels];
        }
        mat2.get(0, 0, pixelBytes);
        bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
        bufferedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(),
                pixelBytes);
    }

    @Override
    public synchronized void update(final Graphics g) {
        g.drawImage(bufferedImage, 0, 0, this);
        // Memory leak will occur without this call
        g.dispose();
    }

    /**
     * Create window, frame and set window to visible.
     * 
     * args[0] = camera index, url or will default to "0" if no args passed.
     * 
     * @param args
     *            String array of arguments.
     */
    public static void main(final String[] args) {
        String url = null;
        // Check how many arguments were passed in
        if (args.length == 0) {
            // If no arguments were passed then default to camera index 0
            url = "0";
        } else {
            url = args[0];
        }
        CaptureUI window = new CaptureUI(url);
        // Deal with VideoCapture always returning True otherwise it will hang
        // on VideoCapture.read()
        if (window.frameSize.width > 0 && window.frameSize.height > 0) {
            KeyEventFrame frame = new KeyEventFrame();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.add(window);
            // Set frame size based on image size
            frame.setSize((int) window.getFrameSize().width,
                    (int) window.getFrameSize().height);
            frame.setVisible(true);
            window.start();
        } else {
            logger.log(Level.SEVERE, "Unable to open device");
        }
    }
}
