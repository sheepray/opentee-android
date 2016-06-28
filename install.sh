#!/bin/bash

chmod +x gradlew
./gradlew assembleDebug

avaDevices=$(adb devices)
echo "$avaDevices"

echo -n "select the device (copy its name) > "
read DeviceName

# determine CPU ABI
ABI="$(adb -s $DeviceName shell getprop ro.product.cpu.abi)"

echo "device abi: ${ABI}"

ABI=$(echo "${ABI}" | tr -d '\r')

if [ "$ABI" = "armeabi-v7a" ] || [ "$ABI" = "armeabi" ] ; then
	APKABI="arm"
else
	APKABI="x86"
fi

# Uninstall TEE Proxy Service and Test Applications.
echo -n "uninstall fi.aalto.ssg.opentee.openteeandroid "
adb -s $DeviceName uninstall fi.aalto.ssg.opentee.openteeandroid

echo -n "uninstall fi.aalto.ssg.opentee.test_app "
adb -s $DeviceName uninstall fi.aalto.ssg.opentee.test_app

# Reinstall them
echo -n "install TEE Proxy Service App "
adb -s $DeviceName install opentee/build/outputs/apk/opentee-$APKABI-debug.apk

echo -n "install Test App "
adb -s $DeviceName install testapp/build/outputs/apk/testapp-debug.apk

# Launch Apps
adb -s $DeviceName shell am start -n fi.aalto.ssg.opentee.openteeandroid/fi.aalto.ssg.opentee.openteeandroid.MainActivity
adb -s $DeviceName shell am start -n fi.aalto.ssg.opentee.test_app/fi.aalto.ssg.opentee.testapp.MainActivity

echo "installation done"
