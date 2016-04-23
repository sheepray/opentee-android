This project is the re-design and implementation of previous opentee-android project on Github. It contains two folders. The first one is "design-doc-latex" which is the design document for this project. It provides theory backgrounds of this project and corresponding designs for the implementation. The second one is "Opentee_Android" which contains the real implementation. The readers are suggested to read the design document first to grab a big picture about this project and then conbine these two to have a clear understand of this project.

Build Environment:
	protocol buffer version 2.6.1

[How to generate Java Doc pdf version of public APIs]
	[required packages]
	1. javadoc: it should come with the Oracle OpenJDK pakcage. Try to issue $javadoc command to check it is ready or not. If not, just install latest OpenJDK package.
	2. pdfdoclet: download it from here \url{https://sourceforge.net/projects/pdfdoclet/}. After download the zipped file, unzipp it.

	[command to generate java doc]
	$ javadoc -doclet com.tarsec.javadoc.pdfdoclet.PDFDoclet -docletpath $PDFDOCLET_UNZIPPED_DIR/pdfdoclet-1.0.3-all.jar -pdf $OUTPUT_FILE_WITH_FULL_PATH $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg    /opentee/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/exception/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/OpenTEE.java
	
