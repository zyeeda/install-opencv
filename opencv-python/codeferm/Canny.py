"""
Copyright (c) Steven P. Goldsmith. All rights reserved.

Created by Steven P. Goldsmith on December 23, 2013
sgoldsmith@codeferm.com
"""

import sys, time, cv2, cv2.cv as cv

"""Canny Edge Detection of video.

sys.argv[1] = source file or will default to "../../resources/traffic.mp4" if no args passed.

@author: sgoldsmith

"""

# If no args passed then default to internal file
if len(sys.argv) < 2:
    url = "../../resources/traffic.mp4"
else:
    url = sys.argv[1]
videoCapture = cv2.VideoCapture(url)
print "URL: %s" % url
print "Resolution: %d x %d" % (videoCapture.get(cv.CV_CAP_PROP_FRAME_WIDTH),
                               videoCapture.get(cv.CV_CAP_PROP_FRAME_HEIGHT))
lastFrame = False
frames = 0
start = time.time()
while not lastFrame:
    ret, image = videoCapture.read()
    if ret:
        # Convert the image to grayscale
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        # Reduce noise with a kernel 3x3
        blur = cv2.GaussianBlur(gray, (3, 3), 0)
        # Canny detector
        edges = cv2.Canny(blur, 100, 200, apertureSize=3)
        # Add some colors to edges from original image
        dst = cv2.bitwise_and(image, image, mask=edges)
        frames += 1
    else:
        lastFrame = True
elapse = time.time() - start
print "%d frames" % frames
print "Elapse time: %4.2f seconds" % elapse
del videoCapture
