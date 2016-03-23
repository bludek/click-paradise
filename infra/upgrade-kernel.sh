#!/bin/bash

set -e

echo "Upgrade Kernel..."

apt-get install -y \
  linux-headers-generic-lts-utopic \
  linux-image-generic-lts-utopic
