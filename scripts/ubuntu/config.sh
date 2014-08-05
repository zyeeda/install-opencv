#!/bin/sh
#
# Created on Dec 18, 2013
#
# @author: sgoldsmith
#
# Make sure you set these before running any script!
#
# Steven P. Goldsmith
# sgjava@gmail.com
#

# Get architecture
arch=$(uname -m)

# Source release info
. /etc/lsb-release

# OpenCV version
opencvver="2.4.9"

# Relative path to gen_java.py
genjava="/modules/java/generator/gen_java.py"

# Relative path to core+Mat.java
coremat="/modules/java/generator/src/java/core+Mat.java"

# Relative path to jdhuff.c
jdhuff="/3rdparty/libjpeg/jdhuff.c"

# Relative path to jdmarker.c
jdmarker="/3rdparty/libjpeg/jdmarker.c"

# Temp dir
tmpdir="$HOME/opencv-$opencvver-libs"

# Set to True to install Java
installjava="False"

# Oracle JDK
javahome=/usr/lib/jvm/jdk1.8.0

if [ $installjava = "True" ]; then
	jdkurl="http://download.oracle.com/otn-pub/java/jdk/8u5-b13/"
	jdkver="jdk1.8.0_05"
	if [ "$arch" = "x86_64" ]; then
		jdkarchive="jdk-8u5-linux-x64.tar.gz"
	elif [ "$arch" = "i586" ] || [ "$arch" = "i686" ]; then
		jdkarchive="jdk-8u5-linux-i586.tar.gz"
	elif [ "$arch" = "armv7l" ]; then
		jdkarchive="jdk-8u5-linux-arm-vfp-hflt.tar.gz"
	else
		echo "\nNo supported architectures detected!"
		exit 1
	fi
	# Apache Ant
	anturl="http://www.us.apache.org/dist/ant/binaries/"
	antarchive="apache-ant-1.9.4-bin.tar.gz"
	antver="apache-ant-1.9.4"
	anthome="/opt/ant"
	antbin="/opt/ant/bin"
fi

# Set to True to remove existing ffmpeg, x264, and other dependencies (this removes a lot of other dependencies you may want)
removelibs="False"

# yasm
yasmurl="http://www.tortall.net/projects/yasm/releases/yasm-1.2.0.tar.gz"
yasmarchive="yasm-1.2.0.tar.gz"
yasmver="yasm-1.2.0"

# x264
x264url="git://git.videolan.org/x264.git"

# fdk-aac
fdkaccurl="git://github.com/mstorsjo/fdk-aac.git"

# Opus
opusurl="http://downloads.xiph.org/releases/opus/opus-1.1.tar.gz"
opusarchive="opus-1.1.tar.gz"
opusver="opus-1.1"

# libvpx
libvpxurl="https://chromium.googlesource.com/webm/libvpx"

# ffmpeg
ffmpegurl="git://source.ffmpeg.org/ffmpeg.git"
