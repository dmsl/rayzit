#!/bin/bash
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 0.25 &>  Series\ 10/v3/SF.final.random.0_25.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 1 &>  Series\ 10/v3/SF.final.random.1.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 4 &>  Series\ 10/v3/SF.final.random.4.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 16 &>  Series\ 10/v3/SF.final.random.16.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 64 &>  Series\ 10/v3/SF.final.random.64.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 256 &>  Series\ 10/v3/SF.final.random.256.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 1024 &>  Series\ 10/v3/SF.final.random.1024.1000k.txt
sleep 20;
java -Xmx8g -Dpj.np=9 -jar spitfire.jar 65 12 1000000 4096 &>  Series\ 10/v3/SF.final.random.4096.1000k.txt
sleep 20;
