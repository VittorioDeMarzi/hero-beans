#!/bin/bash

mkdir -p /home/ubuntu/app
mkdir -p /home/ubuntu/app/scripts

find /home/ubuntu/app -maxdepth 1 -type f -name "*.jar" -delete
