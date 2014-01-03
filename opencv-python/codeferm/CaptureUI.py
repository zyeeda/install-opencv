"""
Copyright (c) Steven P. Goldsmith. All rights reserved.

Created by Steven P. Goldsmith on December 21, 2013
sgoldsmith@codeferm.com
"""

import logging, sys, re, cv2, cv2.cv as cv

"""A simple video capture script using imshow.

sys.argv[1] = camera index, url or will default to "0" if no args passed.

@author: sgoldsmith

"""

# Configure logger
logger = logging.getLogger("VideoLoop")
logger.setLevel("INFO")
formatter = logging.Formatter("%(asctime)s %(levelname)-8s %(module)s %(message)s")
handler = logging.StreamHandler(sys.stdout)
handler.setFormatter(formatter)
logger.addHandler(handler)
# If no args passed then default to device 0
if len(sys.argv) < 2:
    url = 0
# If arg is an integer then convert to int
elif re.match(r"[-+]?\d+$", sys.argv[1]) is not None:
    url = int(sys.argv[1])
else:
    url = sys.argv[1]
videoCapture = cv2.VideoCapture(url)
logger.info("Press [Esc] to exit")
logger.info("URL: %s" % url)
logger.info("Resolution: %dx%d" % (videoCapture.get(cv.CV_CAP_PROP_FRAME_WIDTH),
                               videoCapture.get(cv.CV_CAP_PROP_FRAME_HEIGHT)))
cv2.namedWindow("Python Capture")
key = -1
# Wait for escape to be pressed
while(key < 0):
    videoCapture.grab()
    success, image = videoCapture.read()
    cv2.imshow("Python Capture", image)
    key = cv2.waitKey(1)
cv2.destroyAllWindows()
