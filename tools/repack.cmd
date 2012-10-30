rm -rf source
rm -rf working
mkdir source

set %PAUSE%=pause

echo step 1:unzip %1.apk to dir[working]
%PAUSE% 
unzip %1.apk -d working

echo step 2:dex to jar
%PAUSE% 
call dex2jar-0.0.9.9\dex2jar.bat working\classes.dex

echo step 3:move ./working/classes_dex2jar.jar ./source/
%PAUSE% 
mv ./working/classes_dex2jar.jar ./source/

echo step 4:unzip classes to .\source\src
unzip .\source\classes_dex2jar.jar -d .\source\src

echo step 5:unzip our classes to .\source\src
%PAUSE% 
unzip com.psher.sdk.jar -d .\source\src

echo step 6:pack classes to jar file
%PAUSE% 
cd .\source\src
rm ..\classes.new.jar
jar -cvf ..\classes.new.jar .jar *
cd %~dp0

echo step 7:jar 2 dex
%PAUSE% 
set cpath=%~dp0
call dx --dex --output %cpath%\source\classes.new.dex %cpath%\source\classes.new.jar
cp ./source/classes.new.dex ./working/classes.dex


echo step 8:pack apk
%PAUSE% 
java -jar apktool.jar b working
dir working\dist

echo step 9:sign apk
%PAUSE%
jarsigner -keystore crazyamber.keystore -storepass testok -keypass testok -signedjar ./working/dist/out.unzipaligned.apk ./working/dist/out.apk cath -digestalg SHA1 -sigalg MD5withRSA

echo step 10:zip align
%PAUSE%
rm -f %1.new.apk
zipalign -v 4 ./working/dist/out.unzipaligned.apk %1.new.apk

rm -rf source
rm -rf working

