This project will convert a sequence of images to a motion video AVI file.
You must have java run-time installed.
The images are assumed to follow a file naming pattern with a increasing sequencing (numeric or alphabetic) that can be described by a regular expression.
To use the application, place the build/imageToVideo.jar in a known location, and look at the examples/imagesToVideo.* files on how to invoke via java command line,
and the examples/runImagesToVideoExample on how to use the imagesToVideo.* scripts.

Folders, files:
src: java source
libs: external libs not available via Maven (pom.xml)
examples: example linux shell and windows batch files
build.xml: ant build install (assumes windows file system)
