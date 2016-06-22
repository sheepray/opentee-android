## Project Descriptions
This project provides Java APIs (named OT-J) for GlobalPlatform compliant Trusted Execution Environments.<br/>
For more detailed information please check the [document/thesis-main.pdf](document/thesis-main.pdf) to gain a full view and background of this project.

It has:
- **document**: consists of the documents for this project, such as the Java API documentation and detailed descriptions of this project.

- **opentee**: contains
	* TEE Proxy Service
	* NativeLibtee
	* Libtee
	* [Open-TEE](https://open-tee.github.io)

- **otclient**: contains the Java API (OT-J) and one type of its implementation. Android Client Applications need to import this module in order to interact with the **opentee** module.

- **testapp**: contains an Android test application which utilizes the OT-J.

Bug report to: rui.yang at aalto.fi, or create a new issue for this repo.

## Before you start
Take a galance at **document/thesis-main.pdf** before continue if you are not familiar with TEE.

### Why should you use it and What it can provide?
This project has a virtual GP-compliant TEE, which allows you to develop Android CAs that use Java API to interact with the TEE/TA without writing native code.

### In which spot you are so that you can utilize it?
You already have ELF formatted TAs with compliance to the GP TEE specifications. Right now you want to develop Android CAs to use the features that these TAs provide while not ready to deploy into a real GP-compliant TEE yet. For how to deply TAs, please refer to next section.

## How To -
### - start
1. Clone this repo.
2. Copy TAs into **opentee/src/main/assets/$abi_version**.
3. Change the value of TA_List in **opentee/src/main/assets/config.properties** to the name of your TAs. Mutiple names must be separated using ",".
4. Run "opentee" and "testapp" run time configurations.
5. Check there are no compilations errors. If there is, please refer to **FAQ** section.
6. Click the buttons in the following sequence in the test application: "CREAT ROOT KEY" -> "INITIALIZE" -> "CREATE DIRECTORY KEY" -> "ENCRYPT DATA" -> "DECRYPT DATA" -> "FINALIZE".
7. After you clikced the "DECRYPT DATA", the decrypted data should be the same as the initial data buffer. If not, or there are running errors, please refere to **FAQ** section.
8. Take the **testapp** module as the example to start developing your own CAs.

### - run your TAs
Once you have followed the instructions to install your TAs correctly, the TAs will be started by Open-TEE automatically. Please check the log of the **opentee** to make sure that your TAs are installed. In the log message, you will see something similar to the following text:
```java
I/TEE Proxy Service: -------- begin installing TAs -----------
I/TEE Proxy Service: installing TA:libomnishare_ta.so
I/TEE Proxy Service: -----------------------------------------
```

### - update Open-TEE

### - generate a java doc out of the API.
There is already a generated java doc in **document/teec_java_api.pdf**. If changes have been made to API and the **teec_java_api.pdf** does not follow up with the changes, it is recommended to generate the java doc using the following command: (check required packages before generating the java doc)
```shell
	$ javadoc -doclet com.tarsec.javadoc.pdfdoclet.PDFDoclet -docletpath $PDFDOCLET_UNZIPPED_DIR/pdfdoclet-1.0.3-all.jar -pdf $OUTPUT_FILE_WITH_FULL_PATH $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/exception/*.*
```

#### required packages
1. javadoc: it should come with the Oracle OpenJDK pakcage. Try to issue $javadoc command to check it is ready or not. If not, just install latest OpenJDK package.
2. pdfdoclet: download it from [here](https://sourceforge.net/projects/pdfdoclet/). After download the zipped file, unzipp it to a certain directory.

## FAQ
For issues not mentioned below, please report it as an issue or bug.
