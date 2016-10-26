
#!/bin/bash


#distributedproximitygrid.jar
#proximitygrid.jar
#SMdistributedproximitygrid.jar
#SNdistributedproximitygrid.jar
#spiralproximitygrid.jar
CLIENT=$1

for((c=2;c<=2;c++))
do
echo "Cell size ".$c;
for((k=2;k<=64;k*=2))
do

echo "K="$k;
#./runScript.sh "./distributedproximitygrid.jar" $((k))  $(($c)) $(($CLIENT));
#./runScript.sh "./proximitygrid.jar"  $((k))  $(($c)) $(($CLIENT));
#./runScript.sh "./SMdistributedproximitygrid.jar"  $((k))  $(($c)) $(($CLIENT));
#./runScript.sh "./SNdistributedproximitygrid.jar"  $((k))  $(($c)) $(($CLIENT));
./runScript.sh "./spitfire.jar"  $((k))  $(($c)) $(($CLIENT));
#./runScript.sh "./bruteforce.jar"  $((k))  $(($c)) $(($CLIENT));
#./runScript.sh "./spiralproximitygrid.jar"  $((k))  $(($c)) $(($CLIENT));
done;

done;
