#!/bin/bash

mkdir -p /home/ubuntu/app
mkdir -p /home/ubuntu/app/scripts

find /home/ubuntu/app -maxdepth 1 -type f -name "*.jar" -delete

# Ensure correct owner and writable directories
sudo chown -R ubuntu:ubuntu /home/ubuntu/app
sudo chmod -R u+rwX /home/ubuntu/app
