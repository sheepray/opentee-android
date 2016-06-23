#!/bin/bash

./gradlew

avaDevices=$(adb devices)
echo "$avaDevices"

echo -n "select the device (copy its name) > "
read DeviceName

# Uninstall TEE Proxy Service and Test Applications.
echo -n "uninstall fi.aalto.ssg.opentee.openteeandroid "
adb -s $DeviceName uninstall fi.aalto.ssg.opentee.openteeandroid

echo -n "uninstall fi.aalto.ssg.opentee.test_app "
adb -s $DeviceName uninstall fi.aalto.ssg.opentee.test_app

# Reinstall them
echo -n "install TEE Proxy Service App "
adb -s $DeviceName install opentee/build/outputs/apk/opentee-*-debug.apk

echo -n "install Test App "
adb -s $DeviceName install testapp/build/outputs/apk/testapp-debug.apk

# Launch Apps
adb -s $DeviceName shell am start -n fi.aalto.ssg.opentee.openteeandroid/fi.aalto.ssg.opentee.openteeandroid.MainActivity
adb -s $DeviceName shell am start -n fi.aalto.ssg.opentee.test_app/fi.aalto.ssg.opentee.testapp.MainActivity
