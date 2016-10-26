#!/bin/bash

iotop --batch --pid 1 > log
line_num=0
while read line; do 
    line_num=$(($line_n+1)) 
    if [[ $(($line_num % 3)) -eq 0 ]]; then 
        #print Column 3
        echo $line | awk '{print $3}'
    fi 
done < log > processed_file
#Get total of column three:
cat processed_file | (tr '\n' +; echo 0) | bc
