## Project Descriptions
This project provides Java APIs (named OT-J) for GlobalPlatform compliant Trusted Execution Environments.<br/>
For more detailed information please check this [article](document/thesis-main.pdf) to gain a full view and background of this project.

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
### Why should you use it and What it can provide?
This project has a virtual GP-compliant TEE, which allows you to develop Android CAs that use Java API to interact with the TEE/TA without writing native code.

### In which spot you are so that you can utilize it?
1. You just want to check out the test application.
2. You already have ELF formatted TAs with compliance to the GP TEE specifications. Right now you want to develop Android CAs to use the features that these TAs provide while not ready to deploy into a real GP-compliant TEE yet. For how to deply TAs, please refer to next section.

### Required Tools
* **Android Studio(optional)** is an IDE to develop Android applications. Please follow the [instructions](https://developer.android.com/studio/install.html) in here to install it.
* **Android SDK** normally comes with the Android Studio. If not, when you try to compile Android applications, it will notify you to download it. For how to specificly download it, please follow [instructions](https://developer.android.com/studio/command-line/index.html) to download it.
* **Android NDK** provides the ability for Android applications to integrate native code. Please check [here](https://developer.android.com/ndk/downloads/index.html) to download it.

## How To -
### - compile the project -

#### - using command line.
##### Linux
Go to the **opentee-android** project and run the following command:
```shell
	$ sh install.sh
```

#### - with Android Studio.
1. Create a new directory and clone this repo. This process may take a few minutes to finish.
```shell
	$ mkdir opentee-android-test
	$ cd opentee-android-test
	$ git clone --recursive git@git.ssg.aalto.fi:platsec/opentee-android.git
```

2. Import **opentee-android** to Android Studio. Go to **File->New->Import Project...** and select the **opentee-android** under the **opentee-android-test** directory. Then wait for Android Studio to finish the importing task.
3. You need either an Android device or an Android emulator to run our test application. Please check the instructions in [here](https://developer.android.com/studio/run/index.html) to set up a debugging environment.
4. Run **opentee** run-time configuration by selecting the opentee from the click-down list on the left side of the **Run** button. Click the **Run** button and select your target device either a real Android device or an emulator. This also takes few minutes to finish if you run it for the first time.
5. Follow the same step above to run the **testapp** run-time configuration.
6. Check there are no compilations errors. If there is, please refer to **FAQ** section.

### - run the test application
1. Once you have run the **opentee** and **testapp** run-time configurations, wait for test application to be started in the mobile device or emulator. After the UI is launched, click the buttons in the following sequence in the test application: "CREAT ROOT KEY" -> "INITIALIZE" -> "CREATE DIRECTORY KEY" -> "ENCRYPT DATA" -> "DECRYPT DATA" -> "FINALIZE".
2. After you clicked the "DECRYPT DATA", the decrypted data should be the same as the initial data buffer. If not, or there are running errors, please refere to **FAQ** section.
3. Take the **testapp** module as the example to start developing your own CAs.

### - run your TAs
* Copy your TAs into **opentee/src/main/assets/$abi_version**.
* Change the value of TA_List in **opentee/src/main/assets/config.properties** to the name of your TAs. Mutiple names must be separated using ",". See example as follows:

```shell
TA_List=ta_1.so,ta_2.so,ta_3.so
```

Once you have followed the instructions to install your TAs correctly, the TAs will be started by Open-TEE automatically. Please check the [log](https://developer.android.com/studio/debug/index.html#systemLog) of the **opentee** to make sure that your TAs are installed. In the log message, you will see something similar to the following text:
```c
I/TEE Proxy Service: -------- begin installing TAs -----------
I/TEE Proxy Service: installing TA:ta_1.so
I/TEE Proxy Service: installing TA:ta_2.so
I/TEE Proxy Service: installing TA:ta_3.so
I/TEE Proxy Service: -----------------------------------------
```

### - update Open-TEE

### - generate a java doc out of the API.
#### required packages
1. javadoc: it should come with the Oracle OpenJDK pakcage. Try to issue $javadoc command to check it is ready or not. If not, just install latest OpenJDK package.
2. pdfdoclet: download it from [here](https://sourceforge.net/projects/pdfdoclet/). After download the zipped file, unzipp it to a certain directory.

There is already a generated java doc in **document/teec_java_api.pdf**. If changes have been made to API and the **teec_java_api.pdf** does not follow up with the changes, it is recommended to generate the java doc using the following command: (check required packages before generating the java doc)
```shell
	$ javadoc -doclet com.tarsec.javadoc.pdfdoclet.PDFDoclet -docletpath $PDFDOCLET_UNZIPPED_DIR/pdfdoclet-1.0.3-all.jar -pdf $OUTPUT_FILE_WITH_FULL_PATH $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/exception/*.*
```

## FAQ
For issues not mentioned below, please report it as an issue or bug.
