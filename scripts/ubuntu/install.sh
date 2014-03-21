#!/bin/sh
#
# Created on Dec 18, 2013
#
# @author: sgoldsmith
#
# Install and configure OpenCV for Ubuntu 12.04.3 and 14.04.0 (Desktop/Server 
# x86/x86_64 bit/armv7l). Please note that since some of the operations change
# configurations, etc. I cannot guarantee it will work on future or previous
# versions. All testing was performed on Ubuntu 12.04.3 and 14.04.0 LTS x86_64,
# x86 and armv7l with the latest updates applied.
#
# WARNING: This script has the ability to install/remove Ubuntu packages and it also
# installs some libraries from source. This could potentially screw up your system,
# so use with caution! I suggest using a VM for testing before using it on your
# physical systems.
#
# Steven P. Goldsmith
# sgjava@gmail.com
# 
# Prerequisites:
#
# o Install Ubuntu 12.04.3 or 14.04.0, update (I used VirtualBox for testing) and
#   make sure to select OpenSSH Server during install. Internet connection is
#   required to download libraries, frameworks, etc.
#    o sudo apt-get update
#    o sudo apt-get upgrade
#    o sudo apt-get dist-upgrade
# o Set variables in config.sh before running.
# o sudo ./install.sh
#

# Get start time
dateformat="+%a %b %-eth %Y %I:%M:%S %p %Z"
starttime=$(date "$dateformat")
starttimesec=$(date +%s)

# Get user who ran sudo
if logname &> /dev/null; then
	curuser=$(logname)
else
	if [ -n "$SUDO_USER" ]; then
		curuser=$SUDO_USER
	else
		curuser=$(whoami)
	fi
fi

# Get current directory
curdir=$(cd `dirname $0` && pwd)

# stdout and stderr for commands logged
logfile="$curdir/install.log"

# Source config file
. "$curdir"/config.sh

# Hostname and domain
hostname=$(hostname -s)
domain=$(hostname -d)
fqdn=$(hostname -f)
# dc1 and dc2
dc1=$(echo $domain | awk '{split($0,a,".");print a[1]}')
dc2=$(echo $domain | awk '{split($0,a,".");print a[2]}')

# Ubuntu version
ubuntuver=$DISTRIB_RELEASE

# Use shared lib?
if [ "$arch" = "i686" -o "$arch" = "i386" -o "$arch" = "i486" -o "$arch" = "i586" ]; then
	shared=0
else
	shared=1
fi

echo "\nInstalling OpenCV $opencvver on Ubuntu $ubuntuver $arch...\n\nHost:   $hostname\nDomain: $domain\nUser:   $curuser\n"
echo "\nInstalling OpenCV $opencvver on Ubuntu $ubuntuver $arch...\n\nHost:   $hostname\nDomain: $domain\nUser:   $curuser\n" > $logfile 2>&1

# Install Oracle Java JDK if installjava True
if [ $installjava = "True" ]; then
	echo "Installing Java $jdkver...\n"
	echo "Installing Java $jdkver...\n" >> $logfile 2>&1
	echo -n "Downloading $jdkurl$jdkarchive to $tmpdir     "
	wget --directory-prefix=$tmpdir --timestamping --progress=dot --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "$jdkurl$jdkarchive" 2>&1 | grep --line-buffered "%" |  sed -u -e "s,\.,,g" | awk '{printf("\b\b\b\b%4s", $2)}'
	echo "\nExtracting $tmpdir/$jdkarchive to $tmpdir"
	tar -xf "$tmpdir/$jdkarchive" -C "$tmpdir"
	echo "Removing $javahome"
	rm -rf "$javahome"
	mkdir -p /usr/lib/jvm
	echo "Moving $tmpdir/$jdkver to $javahome"
	mv "$tmpdir/$jdkver" "$javahome"
	update-alternatives --quiet --install "/usr/bin/java" "java" "$javahome/bin/java" 1
	update-alternatives --quiet --install "/usr/bin/javac" "javac" "$javahome/bin/javac" 1
	# ARM JVM doesn't have Java WebStart
	if [ "$arch" != "armv7l" ]; then
		update-alternatives --quiet --install "/usr/bin/javaws" "javaws" "$javahome/bin/javaws" 1
	fi
	# See if JAVA_HOME exists and if not add it to /etc/environment
	if grep -q "JAVA_HOME" /etc/environment; then
		echo "JAVA_HOME already exists"
	else
		# Add JAVA_HOME to /etc/environment
		echo "Adding JAVA_HOME to /etc/environment"
		echo "JAVA_HOME=$javahome" >> /etc/environment
		. /etc/environment
	fi
	# Make sure root picks up JAVA_HOME for this process
	export JAVA_HOME=$javahome
	echo "JAVA_HOME = $JAVA_HOME"
	# Latest ANT without all the junk from apt-get install ant
	echo "\nInstalling Ant $antver...\n"
	echo "Installing Ant $antver...\n" >> $logfile 2>&1
	echo -n "Downloading $anturl$antarchive to $tmpdir     "
	wget --directory-prefix=$tmpdir --timestamping --progress=dot "$anturl$antarchive" 2>&1 | grep --line-buffered "%" |  sed -u -e "s,\.,,g" | awk '{printf("\b\b\b\b%4s", $2)}'
	echo "\nExtracting $tmpdir/$antarchive to $tmpdir"
	tar -xf "$tmpdir/$antarchive" -C "$tmpdir"
	echo "Removing $anthome"
	rm -rf "$anthome"
	echo "Moving $tmpdir/$antver to $anthome"
	mv "$tmpdir/$antver" "$anthome"
	# See if ANT_HOME exists and if not add it to /etc/environment
	if grep -q "ANT_HOME" /etc/environment; then
		echo "ANT_HOME already exists"
	else
		# OpenCV make will not find ant by ANT_HOME, so create link to where it's looking
		ln -s "$antbin/ant" /usr/bin/ant
		# Add ANT_HOME to /etc/environment
		echo "Adding ANT_HOME to /etc/environment"
		echo "ANT_HOME=$anthome" >> /etc/environment
		# Add $ANT_HOME/bin to PATH
		sed -i 's@games@&'":$anthome/bin"'@g' /etc/environment
		. /etc/environment
		echo "ANT_HOME = $ANT_HOME"
		echo "PATH = $PATH"
	fi
fi

# Remove existing ffmpeg, x264, and other dependencies (this removes a lot of other dependencies)
if [ $removelibs = "True" ]; then
	echo "\nRemoving any pre-installed ffmpeg, x264, and other dependencies...\n"
	echo "Removing any pre-installed ffmpeg, x264, and other dependencies...\n" >> $logfile 2>&1
	apt-get -y remove ffmpeg x264 libx264-dev libvpx-dev libopencv-dev >> $logfile 2>&1
	apt-get -y update >> $logfile 2>&1
fi

# Install build dependenices
echo "\nInstalling build dependenices..."
echo "Installing build dependenices...\n" >> $logfile 2>&1
apt-get -y install autoconf build-essential checkinstall cmake git libass-dev libfaac-dev libgpac-dev libjack-jackd2-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev librtmp-dev libsdl1.2-dev libtheora-dev libtool libva-dev libvdpau-dev libvorbis-dev libx11-dev libxext-dev libxfixes-dev pkg-config texi2html zlib1g-dev >> $logfile 2>&1

# Install yasm
echo "\nInstalling yasm $yasmver...\n"
echo "\nInstalling yasm $yasmver...\n" >> $logfile 2>&1
echo -n "Downloading $yasmurl to $tmpdir     "
wget --directory-prefix=$tmpdir --timestamping --progress=dot "$yasmurl" 2>&1 | grep --line-buffered "%" |  sed -u -e "s,\.,,g" | awk '{printf("\b\b\b\b%4s", $2)}'
echo "\nExtracting $tmpdir/$yasmarchive to $tmpdir"
tar -xf "$tmpdir/$yasmarchive" -C "$tmpdir"
cd "$tmpdir/$yasmver"
./configure >> $logfile 2>&1
make >> $logfile 2>&1
checkinstall --pkgname=yasm --pkgversion="1.2.0" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1

# Install x264
echo "\nInstalling x264...\n"
echo "Installing x264...\n" >> $logfile 2>&1
cd "$tmpdir"
git clone "$x264url"
cd "x264"
if [ $shared -eq 0 ]; then
	./configure --enable-static --disable-opencl >> $logfile 2>&1
else
	./configure --enable-shared --disable-opencl >> $logfile 2>&1
fi
make >> $logfile 2>&1
checkinstall --pkgname=x264 --pkgversion="3:$(./version.sh | awk -F'[" ]' '/POINT/{print $4"+git"$5}')" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1

# Install fdk-aac
echo "\nInstalling fdk-aac (AAC audio encoder)...\n"
echo "Installing fdk-aac (AAC audio encoder)...\n" >> $logfile 2>&1
cd "$tmpdir"
git clone --depth 1 "$fdkaccurl"
cd "fdk-aac"
autoreconf -fiv >> $logfile 2>&1
if [ $shared -eq 0 ]; then
	./configure --disable-shared >> $logfile 2>&1
else
	./configure --enable-shared >> $logfile 2>&1
fi
make >> $logfile 2>&1
checkinstall --pkgname=fdk-aac --pkgversion="$(date +%Y%m%d%H%M)-git" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1

# Install libvpx (VP8/VP9 video encoder and decoder)
# ARM build failed because Cortex A* wasn't supported
if [ "$arch" != "armv7l" ]; then
	echo "\nInstalling libvpx (VP8/VP9 video encoder and decoder)...\n"
	echo "Installing libvpx (VP8/VP9 video encoder and decoder)...\n" >> $logfile 2>&1
	cd "$tmpdir"
	git clone --depth 1 "$libvpxurl"
	cd libvpx
	if [ $shared -eq 0 ]; then
		./configure --disable-examples --disable-unit-tests >> $logfile 2>&1
	else
		./configure --disable-examples --disable-unit-tests --enable-shared >> $logfile 2>&1
	fi
	make >> $logfile 2>&1
	checkinstall --pkgname=libvpx --pkgversion="1:$(date +%Y%m%d%H%M)-git" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1
fi

# Install libopus (Opus audio decoder and encoder)
echo "\nInstalling libopus $opusver (Opus audio decoder and encoder)...\n"
echo "Installing libopus $opusver (Opus audio decoder and encoder)...\n" >> $logfile 2>&1
echo -n "Downloading $opusurl to $tmpdir     "
wget --directory-prefix=$tmpdir --timestamping --progress=dot "$opusurl" 2>&1 | grep --line-buffered "%" |  sed -u -e "s,\.,,g" | awk '{printf("\b\b\b\b%4s", $2)}'
echo "\nExtracting $tmpdir/$opusarchive to $tmpdir"
tar -xf "$tmpdir/$opusarchive" -C "$tmpdir"
cd "$tmpdir/$opusver"
./configure  --disable-shared >> $logfile 2>&1
make >> $logfile 2>&1
checkinstall --pkgname=libopus --pkgversion="$(date +%Y%m%d%H%M)-git" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1

# Install ffmpeg
echo "\nInstalling ffmpeg...\n"
echo "Installing ffmpeg...\n" >> $logfile 2>&1
cd "$tmpdir"
git clone "$ffmpegurl"
cd ffmpeg
# ARM build without libvpx
if [ "$arch" = "armv7l" ]; then
	./configure --enable-gpl --enable-libass --enable-libfaac --enable-libfdk-aac --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-librtmp --enable-libtheora --enable-libvorbis --enable-x11grab --enable-libx264 --enable-nonfree --enable-version3 --enable-shared >> $logfile 2>&1
else
	if [ $shared -eq 0 ]; then
		./configure --enable-gpl --enable-libass --enable-libfaac --enable-libfdk-aac --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-librtmp --enable-libtheora --enable-libvorbis --enable-libvpx --enable-x11grab --enable-libx264 --enable-nonfree --enable-version3 >> $logfile 2>&1
	else
		./configure --enable-gpl --enable-libass --enable-libfaac --enable-libfdk-aac --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-librtmp --enable-libtheora --enable-libvorbis --enable-libvpx --enable-x11grab --enable-libx264 --enable-nonfree --enable-version3 --enable-shared >> $logfile 2>&1
	fi
fi
make >> $logfile 2>&1
checkinstall --pkgname=ffmpeg --pkgversion="7:$(date +%Y%m%d%H%M)-git" --backup=no --deldoc=yes --fstrans=no --default >> $logfile 2>&1
hash -r >> $logfile 2>&1

echo "\nInstalling OpenCV dependenices...\n"
echo "Installing OpenCV dependenices...\n" >> $logfile 2>&1
# Install Image I/O libraries 
apt-get -y install libtiff4-dev libjpeg-dev libjasper-dev >> $logfile 2>&1
# Install Video I/O libraries, support for Firewire video cameras and video streaming libraries
apt-get -y install libav-tools libavcodec-dev libavformat-dev libswscale-dev libdc1394-22-dev libxine-dev libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev libv4l-dev v4l-utils v4l-conf >> $logfile 2>&1
# Install the Python development environment and the Python Numerical library
apt-get -y install python-dev python-numpy >> $logfile 2>&1
# Install the parallel code processing library (the Intel tbb library)
apt-get -y install libtbb-dev >> $logfile 2>&1
# Install the Qt dev library
apt-get -y install libqt4-dev libgtk2.0-dev >> $logfile 2>&1
# Install other dependencies (if need be it would upgrade current version of the packages)
apt-get -y install patch subversion ruby librtmp0 librtmp-dev libfaac-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libvpx-dev libxvidcore-dev >> $logfile 2>&1
# Install optional packages
apt-get -y install libdc1394-utils libdc1394-22-dev libdc1394-22 libjpeg-dev libpng-dev libtiff-dev libjasper-dev >> $logfile 2>&1

echo "Installing OpenCV $opencvver...\n"
echo "\nInstalling OpenCV $opencvver..." >> $logfile 2>&1
opencvarchive="opencv-$opencvver.tar.gz"
opencvurl="http://sourceforge.net/projects/opencvlibrary/files/opencv-unix/$opencvver/$opencvarchive"
opencvhome="$HOME/opencv-$opencvver"
echo -n "Downloading $opencvurl to $tmpdir     "
wget --directory-prefix=$tmpdir --timestamping --progress=dot "$opencvurl" 2>&1 | grep --line-buffered "%" |  sed -u -e "s,\.,,g" | awk '{printf("\b\b\b\b%4s", $2)}'
echo "\nExtracting $tmpdir/$opencvarchive to $tmpdir"
tar -xf "$tmpdir/$opencvarchive" -C "$tmpdir"
echo "Removing $opencvhome"
rm -rf "$opencvhome"
echo "Moving $tmpdir/opencv-$opencvver to $opencvhome"
mv "$tmpdir/opencv-$opencvver" "$opencvhome"

# Patch gen_java.py to generate VideoWriter by removing from class_ignore_list
sed -i 's/\"VideoWriter\",/'\#\"VideoWriter\",'/g' "$opencvhome$genjava"

# Patch gen_java.py to generate constants by removing from const_ignore_list
sed -i 's/\"CV_CAP_PROP_FPS\",/'\#\"CV_CAP_PROP_FPS\",'/g' "$opencvhome$genjava"
sed -i 's/\"CV_CAP_PROP_FOURCC\",/'\#\"CV_CAP_PROP_FOURCC\",'/g' "$opencvhome$genjava"
sed -i 's/\"CV_CAP_PROP_FRAME_COUNT\",/'\#\"CV_CAP_PROP_FRAME_COUNT\",'/g' "$opencvhome$genjava"

# Patch jdhuff.c to remove "Invalid SOS parameters for sequential JPEG" warning
sed -i 's~if (cinfo->Ss~//if (cinfo->Ss~g' "$opencvhome$jdhuff"
sed -i 's~cinfo->Ah~//cinfo->Ah~g' "$opencvhome$jdhuff"
sed -i 's~WARNMS(cinfo, JWRN_NOT_SEQUENTIAL~//WARNMS(cinfo, JWRN_NOT_SEQUENTIAL~g' "$opencvhome$jdhuff"

# Compile OpenCV
echo "\nCompile OpenCV..."
echo "\nCompile OpenCV...\n" >> $logfile 2>&1
cd "$opencvhome"
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=RELEASE -DBUILD_SHARED_LIBS=ON -DBUILD_NEW_PYTHON_SUPPORT=ON -DINSTALL_PYTHON_EXAMPLES=ON -DWITH_TBB=ON -DWITH_V4L=ON -DWITH_OPENGL=ON -DWITH_OPENCL=ON -DWITH_EIGEN=ON -DWITH_OPENEXR=ON -DBUILD_JPEG=ON .. >> $logfile 2>&1
make -j$(getconf _NPROCESSORS_ONLN) >> $logfile 2>&1
make install >> $logfile 2>&1
echo "/usr/local/lib" > /etc/ld.so.conf.d/opencv.conf
ldconfig

# Set permissions on OpenCV dir to user that ran script
chown -R $curuser:$curuser $opencvhome
cd "$curdir"

# Get end time
endtime=$(date "$dateformat")
endtimesec=$(date +%s)

# Show elapse time
elapsedtimesec=$(expr $endtimesec - $starttimesec)
ds=$((elapsedtimesec % 60))
dm=$(((elapsedtimesec / 60) % 60))
dh=$((elapsedtimesec / 3600))
displaytime=$(printf "%02d:%02d:%02d" $dh $dm $ds)

echo "\nOpenCV home: $opencvhome" >> $logfile 2>&1
echo "\nElapse time: $displaytime\n" >> $logfile 2>&1
