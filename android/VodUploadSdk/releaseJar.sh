#!/usr/bin/env bash

# example : ./releaseJar.sh

version=`cat build.gradle|grep 'OUTPUT_VERSION'|grep -o '[0-9].*[0-9]'`

rm -rf output
mkdir output

./gradlew releaseJar

cd output
mkdir temp

mv *.jar temp/
cd temp

for file in $( ls )
do
   jar -xvf $file
done

rm *.jar
rm -rf META-INF
jar -cvfM aliyun-vod-upload-android-sdk-$version-final.jar .
mv aliyun-vod-upload-android-sdk-$version-final.jar ../aliyun-vod-upload-android-sdk-$version.jar



