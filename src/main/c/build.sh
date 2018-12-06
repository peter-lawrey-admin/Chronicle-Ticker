#!/bin/bash

java -version 2>&1 | grep 11 > /dev/null
rc=$?

if [ $rc -eq 0 ]; then
  echo "Building for Java 10+"
  make -f Makefile.j11
else
  make -f Makefile.j8
fi

