#!/bin/bash

#
# decompile com.owon.vds.foundation_1.0.0.jar
#

# check: jdk6 is available in current directory
if [ ! -d ./jdk1.6.0_45 ]; then
	echo "./jdk1.6.0_45 directory not found"
        echo "download jdk-6u45-linux-x64.bin from https://www.oracle.com/nl/java/technologies/javase-java-archive-javase6-downloads.html"
	exit -1
fi

# copy jar to orig.jar if not present
if [ ! -f ./com.owon.vds.foundation_1.0.0.orig.jar ]; then
	cp ./VDS_S2/plugins/com.owon.vds.foundation_1.0.0.jar com.owon.vds.foundation_1.0.0.orig.jar
fi

# get jd-cli.jar if not yet present. Using older jdk6 compatible version
echo "get jd-cli.jar" &&\
wget -nc https://github.com/intoolswetrust/jd-cli/releases/download/jd-cmd-0.9.1.Final/jd-cli-0.9.1.Final-dist.tar.gz &&\
tar xvfz jd-cli-0.9.1.Final-dist.tar.gz jd-cli.jar &&\

# unpack to build/, decompile to source/
echo "unpack com.owon.vds.foundation_1.0.0.orig.jar to build/" &&\
mkdir -p build &&\
cd build &&\
unzip -q ../com.owon.vds.foundation_1.0.0.orig.jar &&\
cd .. &&\
echo "decompile to source/" &&\
./jdk1.6.0_45/bin/java -jar jd-cli.jar -od source build &&\

echo "decompiled source is available in source/"
