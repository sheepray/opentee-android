## Project Descriptions
This project provides Java APIs (named OT-J) for Trusted Execution Environments.<br/>
It is highly recommended to read the **document/thesis-main.pdf** to gain a full view and background of this project. 
It contains:
- **document**: includes the documents for this project, such as the Java API documentation and detailed descriptions of this project.

- **opentee**: contains
	* TEE Proxy Service
	* NativeLibtee
	* Libtee
	* Open-TEE

- **otclient**: contains the Java API (OT-J) and its implementations. Android Client Applications need to import this module in order to interact with the **opentee** module.

- **testapp**: contains an Android test application which utilizes the OT-J.

Bug report to: rui.yang at aalto.fi

## Before you start
### Why should you use it?
### What it can provide?
### What you must have in order to utilize it?

## How To -
### - start
#### Set Up Environment:

### - generate a java doc out of the API.
There is already a generated java doc in **document/teec_java_api.pdf**. If changes have been made to API and the **teec_java_api.pdf** does not follow up with the changes, it is recommended to generate the java doc using the following command: (check required packages before generating the java doc)
```shell
	$ javadoc -doclet com.tarsec.javadoc.pdfdoclet.PDFDoclet -docletpath $PDFDOCLET_UNZIPPED_DIR/pdfdoclet-1.0.3-all.jar -pdf $OUTPUT_FILE_WITH_FULL_PATH $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/exception/*.*
```

#### required packages
1. javadoc: it should come with the Oracle OpenJDK pakcage. Try to issue $javadoc command to check it is ready or not. If not, just install latest OpenJDK package.
2. pdfdoclet: download it from [here](https://sourceforge.net/projects/pdfdoclet/). After download the zipped file, unzipp it to a certain directory.

## FAQ
