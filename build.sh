#!/bin/bash
set -e

lein build-site

# We don't want to overwrite existing episode files, so this is the current hackaround
rclone copyto dist/rss.xml utv:utviklingslandet-public/rss.xml
rclone copyto dist/index.html utv:utviklingslandet-public/index.html
