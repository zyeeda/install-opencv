"""
Copyright (c) Steven P. Goldsmith. All rights reserved.

Created by Steven P. Goldsmith on December 24, 2013
sgoldsmith@codeferm.com
"""

import sys, time, cv2, cv2.cv as cv

"""Histogram of Oriented Gradients ([Dalal2005]) object detector.

sys.argv[1] = source file or will default to "../../resources/walking.avi" if no args passed.

@author: sgoldsmith

"""

# If no args passed then default to internal file
if len(sys.argv) < 2:
    url = "../../resources/walking.avi"
else:
    url = sys.argv[1]
videoCapture = cv2.VideoCapture(url)
print "URL: %s" % url
print "Resolution: %d x %d" % (videoCapture.get(cv.CV_CAP_PROP_FRAME_WIDTH),
                               videoCapture.get(cv.CV_CAP_PROP_FRAME_HEIGHT))
hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())
lastFrame = False
frames = 0
framesWithPeople = 0
start = time.time()
while not lastFrame:
    ret, image = videoCapture.read()
    if ret:
        found, w = hog.detectMultiScale(image, winStride=(8, 8), padding=(32, 32), scale=1.05)
        if len (found) > 0:
            framesWithPeople += 1
        frames += 1
    else:
        lastFrame = True
elapse = time.time() - start
print "%d frames, %d frames with people" % (frames, framesWithPeople)
print "Elapse time: %4.2f seconds" % elapse
del videoCapture

