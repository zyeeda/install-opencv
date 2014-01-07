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
* OpenCV from source
* Java 7 and Apache Ant
    * Patch gen_java.py to generate missing VideoWriter class and add some missing CV_CAP_PROP constants
    * FourCC class
    * CaptureUI Applet to view images/video since there's no imshow with the bindings
* Java and Python examples
    * Capture UI
    * Motion detection
    * People detection
    
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
* On ARM WebM codec (libvpx) failed to build because due to "Requested CPU
 'cortex-a8' not supported by compiler" even though I tested it on a
 Cortex-A9. For now the install script doesn't build WebM on ARM.
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

#### Upgrades
Once you've built ffmpeg and OpenCV with this package you will only need to
build OpenCV in the future. You can also remove any package installed with
checkinstall using `sudo dpkg -r packagename`. To upgrade OpenCV:
* `cd /home/<username>/opencv-2.4.x/build`
* `sudo make uninstall`
* Download and extract new OpenCV archive to your home dir
* `cd /home/<username>/opencv-2.4.x`
* `mkdir build`
* `cd build`
* `cmake -DCMAKE_BUILD_TYPE=RELEASE -DBUILD_SHARED_LIBS=ON -DBUILD_NEW_PYTHON_SUPPORT=ON -DINSTALL_PYTHON_EXAMPLES=ON -DWITH_TBB=ON -DWITH_V4L=ON -DWITH_OPENGL=ON -DWITH_OPENCL=ON -DWITH_EIGEN=ON -DWITH_OPENEXR=ON .. > install.log 2>&1`
* `make -j8 >> install.log 2>&1`
* `make install >> install.log 2>&1`
* `echo "/usr/local/lib" > /etc/ld.so.conf.d/opencv.conf`
* `ldconfig`

### Java
To run Java programs in Eclipse you need add the OpenCV library.
* Window, Preferences, Java, Build Path, User Libraries, New..., OpenCV, OK
* Add External JARs..., /home/&lt;username&gt;/opencv-2.4.x/build-java/bin/opencv-24x.jar
* Native library location, Edit..., External Folder..., /home/&lt;username&gt;/opencv-2.4.x/build-java/lib, OK
* Right click project, Properties, Java Build Path, Libraries, Add Library..., User Library, OpenCV, Finish, OK
* Import [Eclipse project](https://github.com/sgjava/install-opencv/tree/master/opencv-java)

To run compiled class (Canny for this example) from shell:
* `cd /home/<username>/workspace/install-opencv/opencv-java`
* `java -Djava.library.path=/home/<username>/opencv-2.4.x/build/lib -cp /home/<username>/opencv-2.4.x/build/bin/opencv-24x.jar:bin com.codeferm.opencv.Canny`

#### Things to be aware of
* There's no bindings generated for OpenCV's GPU module.
* Missing VideoWriter (I fixed this by patching gen_java.py)
* Constants are missing (These can by patched as well in the install script)
* There's no imshow equivalent, so check out [CaptureUI](https://github.com/sgjava/install-opencv/blob/master/opencv-java/src/com/codeferm/opencv/CaptureUI.java)

![CaptureUI Java](images/captureui-java.png)

The Canny example is slightly faster in Java (3.08 seconds) compared to Python
(3.18 seconds). In general, there's not enough difference in processing over 900
frames to pick one set of bindings over another for performance reasons.
`-agentlib:hprof=cpu=samples` is used to profile.
```
Input file: ../resources/traffic.mp4
Output file: ../output/canny-java.avi
Resolution: 480x360
919 frames
Elipse time: 3.03 seconds

CPU SAMPLES BEGIN (total = 310) Fri Jan  3 16:09:24 2014
rank   self  accum   count trace method
   1 40.32% 40.32%     125 300218 org.opencv.imgproc.Imgproc.Canny_0
   2 22.58% 62.90%      70 300220 org.opencv.highgui.VideoWriter.write_0
   3 10.00% 72.90%      31 300215 org.opencv.highgui.VideoCapture.read_0
   4  9.03% 81.94%      28 300219 org.opencv.core.Core.bitwise_and_0
   5  9.03% 90.97%      28 300221 org.opencv.imgproc.Imgproc.cvtColor_1
   6  5.81% 96.77%      18 300222 org.opencv.imgproc.Imgproc.GaussianBlur_2
   7  0.32% 97.10%       1 300016 sun.misc.Perf.createLong
   8  0.32% 97.42%       1 300077 java.util.zip.ZipFile.open
   9  0.32% 97.74%       1 300095 java.util.jar.JarVerifier.<init>
  10  0.32% 98.06%       1 300102 java.lang.ClassLoader$NativeLibrary.load
  11  0.32% 98.39%       1 300105 java.util.Arrays.copyOfRange
  12  0.32% 98.71%       1 300163 sun.nio.fs.UnixNativeDispatcher.init
  13  0.32% 99.03%       1 300212 sun.reflect.ReflectionFactory.newConstructorAccessor
  14  0.32% 99.35%       1 300214 org.opencv.highgui.VideoCapture.VideoCapture_1
  15  0.32% 99.68%       1 300216 java.util.Arrays.copyOfRange
  16  0.32% 100.00%       1 300217 com.codeferm.opencv.Canny.main
CPU SAMPLES END
```
### Python
To run Python programs in Eclipse you need [PyDev](http://pydev.org) installed.
* Help, Install New Software..., Add..., Name: PyDev, Location: http://pydev.org/updates, OK, check PyDev, Next>, Next>, I accept the terms of the license agreement, Finish, Trust certificate, OK
* Import [Eclipse project](https://github.com/sgjava/install-opencv/tree/master/opencv-python)

![CaptureUI Java](images/captureui-python.png)

`-m cProfile -s time` is used to profile.
```
Input file: ../../resources/traffic.mp4
Output file: ../../output/canny-python.avi
Resolution: 480x360
919 frames
Elapse time: 3.18 seconds

   Ordered by: internal time

   ncalls  tottime  percall  cumtime  percall filename:lineno(function)
      919    1.231    0.001    1.231    0.001 {cv2.Canny}
      919    0.932    0.001    0.932    0.001 {method 'write' of 'cv2.VideoWriter' objects}
      920    0.375    0.000    0.375    0.000 {method 'read' of 'cv2.VideoCapture' objects}
      919    0.230    0.000    0.230    0.000 {cv2.bitwise_and}
      919    0.188    0.000    0.188    0.000 {cv2.cvtColor}
      919    0.175    0.000    0.175    0.000 {cv2.GaussianBlur}
        1    0.075    0.075    3.263    3.263 Canny.py:6(<module>)
        1    0.007    0.007    0.007    0.007 {cv2.VideoCapture}
      262    0.003    0.000    0.004    0.000 function_base.py:3181(add_newdoc)
        2    0.003    0.001    0.012    0.006 __init__.py:2(<module>)
        1    0.002    0.002    0.003    0.003 polynomial.py:48(<module>)
        1    0.002    0.002    0.003    0.003 chebyshev.py:78(<module>)
        1    0.002    0.002    0.003    0.003 hermite_e.py:50(<module>)
        6    0.002    0.000    0.003    0.000 {method 'sub' of '_sre.SRE_Pattern' objects}
        1    0.002    0.002    0.003    0.003 hermite.py:50(<module>)
        1    0.002    0.002    0.003    0.003 legendre.py:74(<module>)
```
### FreeBSD License
Copyright (c) Steven P. Goldsmith

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
