#!/bin/bash

# start new
while true; do
read URL
youtube-dl -o "~/smartcast/youtube-music/temp/%(title)s.%(ext)s" --restrict-filenames $URL
for f in $( find ~/smartcast/youtube-music/temp/ -type f); do
echo -n "[filename] "; echo $f
echo -n "[duration] "; avconv -i $f 2>&1 | grep 'Duration' | awk '{print $2}' | sed s/,//  
omxplayer $f
rm $f
done
done
