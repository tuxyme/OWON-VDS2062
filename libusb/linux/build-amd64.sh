#!/bin/bash

# sudo apt-get install gcc-x86-64-linux-gnu binutils

set -e
pushd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null

raise () {
    # $1: mnessage
    printf "Error: $1\n\n" >&2
    exit 1
}



echo "Build libusbJava.so  amd64 ..."
mkdir -p amd64

[ -f /lib/x86_64-linux-gnu/libusb-0.1.so.4 ] || raise "amd64/libusb-0.1.so.4 is missing."
pushd amd64 >/dev/null
rm -f libusbJava.so

x86_64-linux-gnu-gcc -shared -fPIC -std=c99 -Wall\
 -I .. \
 -Wl,--enable-new-dtags\
 -Wl,-rpath=\$ORIGIN\
 -Wl,-soname,libusbJava.so\
 -o libusbJava.so\
 ../LibusbJava.c\
 /lib/x86_64-linux-gnu/libusb-0.1.so.4 


printf "\n"
file libusbJava.so | tr , '\n'
readelf libusbJava.so -d | head -8

popd >/dev/null
popd >/dev/null
