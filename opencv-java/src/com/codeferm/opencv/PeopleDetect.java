/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 23, 2013
 * sgoldsmith@codeferm.com
 */
package com.codeferm.opencv;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.VideoWriter;
import org.opencv.objdetect.HOGDescriptor;

/**
 * Histogram of Oriented Gradients ([Dalal2005]) object detector.
 * 
 * args[0] = source file or will default to "../resources/walking.avi" if no
 * args passed.
 * 
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class PeopleDetect {
	/**
	 * Serializable class version number.
	 */
	private static final long serialVersionUID = -3988850198352906351L;
	/* Load the OpenCV system library */
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	/**
	 * This is missing from org.opencv.highgui.Highgui.
	 */
	private static final int CV_CAP_PROP_FPS = 5;

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
			// If no arguments were passed then default to local file
			url = "../resources/walking.avi";
		} else {
			url = args[0];
		}
		System.out.println(String.format("URL: %s", url));
		final VideoCapture videoCapture = new VideoCapture(url);
		final Size frameSize = new Size(
				(int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
				(int) videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
		System.out.println(String.format("Resolution: %s", frameSize));
		final FourCC fourCC = new FourCC("DIVX");
		VideoWriter videoWriter = new VideoWriter(
				"../output/people-detect-java.avi", fourCC.toInt(),
				videoCapture.get(Highgui.CV_CAP_PROP_FPS), frameSize, true);
		final Mat mat = new Mat();
		final HOGDescriptor hog = new HOGDescriptor();
		// final HOGDescriptor hog = new HOGDescriptor(new Size(128, 64),
		// new Size(16, 16), new Size(8, 8), new Size(8, 8), 9, 0, -1, 0,
		// 0.2, false, 64);
		final MatOfRect foundLocations = new MatOfRect();
		final MatOfDouble foundWeights = new MatOfDouble();
		final Size winStride = new Size(8, 8);
		final Size padding = new Size(32, 32);
		final MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
		hog.setSVMDetector(descriptors);
		int frames = 0;
		int framesWithPeople = 0;
		final long startTime = System.currentTimeMillis();
		while (videoCapture.read(mat)) {
			hog.detectMultiScale(mat, foundLocations, foundWeights, 0.0,
					winStride, padding, 1.05, 2.0, false);
			if (foundLocations.rows() > 0) {
				framesWithPeople++;
				List<Rect> rectList = foundLocations.toList();
				for (Rect rect : rectList) {
					Core.rectangle(mat, new Point(rect.x, rect.y), new Point(
							rect.x + rect.width, rect.y + rect.height),
							new Scalar(0, 255, 0));
				}
			}
			videoWriter.write(mat);
			frames++;
		}
		final long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("%d frames, %d frames with people",
				frames, framesWithPeople));
		System.out.println(String.format("Elipse time: %4.2f seconds",
				(double) estimatedTime / 1000));
	}
}
