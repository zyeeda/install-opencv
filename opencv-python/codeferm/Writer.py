"""
Copyright (c) Steven P. Goldsmith. All rights reserved.

Created by Steven P. Goldsmith on December 23, 2013
sgoldsmith@codeferm.com
"""

import sys, time, cv2, cv2.cv as cv

"""Example of VideoWriter module.

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
videoWriter = cv2.VideoWriter("../../resources/python.avi", cv.CV_FOURCC(*'DIV3'), videoCapture.get(cv.CV_CAP_PROP_FPS), (int(videoCapture.get(cv.CV_CAP_PROP_FRAME_WIDTH)), int(videoCapture.get(cv.CV_CAP_PROP_FRAME_HEIGHT))), True)
lastFrame = False
frames = 0
start = time.time()
while not lastFrame:
    ret, image = videoCapture.read()
    if ret:
        videoWriter.write(image)
        frames += 1
    else:
        lastFrame = True
elapse = time.time() - start
print "%d frames" % frames
print "Elapse time: %4.2f seconds" % elapse
del videoCapture
del videoWriter
