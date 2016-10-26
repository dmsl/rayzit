#!/bin/bash

if [ $# -ne 4 ]; then
echo "Usage:"$0" <jar> <k> <cellsize> <users>";
exit 0
fi

if [ ! -d "./result2" ]; then
    mkdir result2;
fi

AVG=5;

filename=$(basename $1)
extension="${filename##*.}"
filename="${filename%.*}"

if [ ! -d "./result/$filename.$2.$3.$4" ]; then
    mkdir result2/$filename.$2.$3.$4;
fi

if [ ! -e $1 ]; then
echo "There is not any jar with this filename("$1")";
exit 0;
fi

echo -e "Running "$1;

total_time=0;

for((c=1;c<=$AVG;c++))
do
echo "("$c")";
#execute script
java -Xmx8g -Dpj.np=9 -jar $1 $(($2)) $(($3)) $(($4)) 2>> ./result2/$filename.$2.$3.$4/time_$filename.$2.$3.$4.txt;
echo "=================================================================" 2>> ./result2/$filename.$2.$3.$4/time_$filename.$2.$3.$4.txt;
done;

