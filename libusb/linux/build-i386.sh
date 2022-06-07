#!/bin/bash

# sudo apt-get install gcc-multilib binutils

set -e
pushd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null

raise () {
    # $1: mnessage
    printf "Error: $1\n\n" >&2
    exit 1
}



echo "Build libusbJava.so  i386 ..."
mkdir -p i386

[ -f i386/libusb-0.1.so.4 ] || raise "i386/libusb-0.1.so.4 is missing."
pushd i386 >/dev/null
rm -f libusbJava.so

x86_64-linux-gnu-gcc -m32 -shared -fPIC -std=c99 -Wall\
 -Wno-pointer-to-int-cast\
 -Wno-int-to-pointer-cast\
 -I .. \
 -Wl,--enable-new-dtags\
 -Wl,-rpath=\$ORIGIN\
 -Wl,-soname,libusbJava.so\
 -o libusbJava.so\
 ../LibusbJava.c\
 libusb-0.1.so.4


printf "\n"
file libusbJava.so | tr , '\n'
readelf libusbJava.so -d | head -8

popd >/dev/null
popd >/dev/null

