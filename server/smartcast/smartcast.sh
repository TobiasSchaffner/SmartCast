#!/bin/bash

# cleanup other processes
pkill -f youtube-music
for f in $(find /home/smartcast/smartcast/youtube-music/temp/ -type f); do
rm $f
done

~/smartcast/youtube-music/youtube-music.sh
