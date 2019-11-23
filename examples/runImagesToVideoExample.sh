#/bin/sh
set -x 
imagesToVideo.sh -?
imagesToVideo.sh "$HOME/render/basename ????.jpg"  "$HOME/render/test.avi"
imagesToVideo.sh -r "$HOME/render/filesToRemove ????.jpg"  "$HOME/render/movie.avi"
