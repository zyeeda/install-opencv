## Install OpenCV

Typically to get the latest version of OpenCV you have to build it from source.
In order to automate this process I've put together a script that installs
the necessary prerequisites and builds OpenCV with Java and Python bindings. I
also included example source, so you can test the installation.

### WARNING

This script has the ability to install/remove Ubuntu packages and it also
installs some libraries from source. This could potentially screw up your system,
so use with caution! I suggest using a VM for testing before using it on your
physical systems. I tried to make the defaults sane in config.sh.

### Provides
* FFMPEG from source (x264, fdk-aac, libvpx, libopus)
* Java 7 and Apache Ant
    * Patch gen_java.py to generate missing VideoWriter class and add some missing CV_CAP_PROP constants
    * FourCC class
    * CaptureUI Applet to view images/video since there's no imshow with the bindings
    * Example code
* OpenCV from source

### Platforms Supported by Install OpenCV
* Ubuntu 12.04.3 LTS x86_64
* Ubuntu 12.04.3 LTS x86
* Ubuntu 12.10 armv7l (PicUntu 0.9 RC3)

### Build
* On ARM platforms with limited memory create a swap file or the build may fail
with an out of memory exception. This is the case with PicUntu 0.9 RC3 since
there's no swap partition created by default. To create a 1GB swap file use:
    * `sudo su -`
    * `dd if=/dev/zero of=tmpswap bs=1024 count=1M`
    * `mkswap tmpswap`
    * `swapon tmpswap`
    * `free`
* `git clone https://github.com/sgjava/install-opencv.git`
* `cd install-opencv/scripts/ubuntu1204`
* Edit config.sh and change OpenCV and Java versions as needed
* Run script in foreground or background
    * `sudo ./install.sh` to run script in foreground
    * `sudo sh -c 'nohup ./install.sh &'` to run script in background

#### Build times
* Acer AM3470G-UW10P Desktop
    * AMD A6-3620 quad core
    * 2.20GHz, 4MB Cache
    * 8GB DIMM DDR3 Synchronous 1333 MHz
    * 500GB WDC WD5000AAKX-0 SATA 3 7200 RPM 16MB Cache
    * Ubuntu 12.04.3 LTS x86_64
    * ~45 minutes (depends on download times)
* MK808 Google TV stick
    * Rockchip RK3066 dual core
    * 1.6GHz Cortex-A9
    * 1GB DDR3
    * 32GB SDHC Class 10
    * PicUntu 0.9 RC3
    * ~4 hours (depends on download times)

#### Build output
* Check install.log output for any problems with the installation script.
* Dependent libraries `/home/<username>/opencv-2.4.x-libs`
* OpenCV home `/home/<username>/opencv-2.4.x`
* Java and Python bindings `/home/<username>/opencv-2.4.x/build`

### Java
To run Java programs in Eclipse you need add the OpenCV library.
* Window, Preferences, Java, Build Path, User Libraries, New..., OpenCV, OK
* Add External JARs..., /home/&lt;username&gt;/opencv-2.4.x/build-java/bin/opencv-24x.jar
* Native library location, Edit..., External Folder..., /home/&lt;username&gt;/opencv-2.4.x/build-java/lib, OK
* Right click project, Properties, Java Build Path, Libraries, Add Library..., User Library, OpenCV, Finish, OK
* Import [Eclipse project](https://github.com/sgjava/install-opencv/tree/master/opencv-java)

#### Things to be aware of
* There's no bindings generated for OpenCV's GPU module.
* Missing VideoWriter (I fixed this by patching gen_java.py)
* Constants are missing (These can by patched as well in the install script)
* There's no imshow equivalent, so check out [CaptureUI](https://github.com/sgjava/install-opencv/blob/master/opencv-java/src/com/codeferm/opencv/CaptureUI.java) 

![CaptureUI Java](images/captureui-java.png)

The Canny example is slightly faster in Python (4.08 seconds) compared to Java
(4.67 seconds). PeopleDetect is also slightly faster in Python (20.72 sceonds)
compared to Java (21.58 seconds). In general, there's not enough difference
in processing over 900 frames to pick one set of bindings over another for
performance reasons.
`-agentlib:hprof=cpu=samples` is used to profile.
```
URL: ../resources/traffic.mp4
Resolution:  480 x 360
923 frames
Elipse time: 4.67 seconds

CPU SAMPLES BEGIN (total = 446) Sun Dec 29 15:09:21 2013
rank   self  accum   count trace method
   1 55.16% 55.16%     246 300103 org.opencv.imgproc.Imgproc.Canny_0
   2 14.57% 69.73%      65 300101 org.opencv.highgui.VideoCapture.read_0
   3 13.45% 83.18%      60 300102 org.opencv.imgproc.Imgproc.cvtColor_1
   4  7.85% 91.03%      35 300105 org.opencv.imgproc.Imgproc.GaussianBlur_2
   5  7.40% 98.43%      33 300104 org.opencv.core.Core.bitwise_and_0
   6  0.45% 98.88%       2 300064 java.lang.ClassLoader$NativeLibrary.load
   7  0.22% 99.10%       1 300035 java.lang.Class.forName0
   8  0.22% 99.33%       1 300060 sun.misc.BASE64Decoder.<clinit>
   9  0.22% 99.55%       1 300062 com.codeferm.opencv.Canny.<clinit>
  10  0.22% 99.78%       1 300094 org.opencv.highgui.VideoCapture.VideoCapture_1
  11  0.22% 100.00%       1 300100 com.codeferm.opencv.Canny.main
CPU SAMPLES END 
```
### Python
To run Python programs in Eclipse you need PyDev installed
* Help, Install New Software..., Add..., Name: PyDev, Location: http://pydev.org/updates, OK, check PyDev, Next>, Next>, I accept the terms of the license agreement, Finish, Trust certificate, OK
* Import [Eclipse project](https://github.com/sgjava/install-opencv/tree/master/opencv-python)

![CaptureUI Java](images/captureui-python.png)

`-m cProfile -s time` is used to profile.
```
URL: ../../resources/traffic.mp4
Resolution: 480 x 360
923 frames
Elapse time: 4.08 seconds

   Ordered by: internal time

   ncalls  tottime  percall  cumtime  percall filename:lineno(function)
      923    1.935    0.002    1.935    0.002 {cv2.Canny}
      924    0.723    0.001    0.723    0.001 {method 'read' of 'cv2.VideoCapture' objects}
      923    0.589    0.001    0.589    0.001 {cv2.cvtColor}
      923    0.461    0.000    0.461    0.000 {cv2.bitwise_and}
      923    0.326    0.000    0.326    0.000 {cv2.GaussianBlur}
        1    0.178    0.178    4.349    4.349 Canny.py:6(<module>)
        2    0.017    0.008    0.046    0.023 __init__.py:2(<module>)
        1    0.009    0.009    0.009    0.009 {cv2.VideoCapture}
        1    0.007    0.007    0.007    0.007 numeric.py:1(<module>)
        1    0.007    0.007    0.007    0.007 linalg.py:10(<module>)
      262    0.006    0.000    0.008    0.000 function_base.py:3178(add_newdoc)
```
### FreeBSD License
Copyright (c) Steven P. Goldsmith

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
