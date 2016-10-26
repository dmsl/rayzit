#create the top leve directory
hadoop fs -rmr /mda
hadoop fs -mkdir /mda
#create the second level directory
hadoop fs -mkdir /mda/data.input
hadoop fs -mkdir /mda/datasets
hadoop fs -mkdir /mda/group
hadoop fs -mkdir /mda/index
hadoop fs -mkdir /mda/merge
hadoop fs -mkdir /mda/output
hadoop fs -mkdir /mda/partitions
hadoop fs -mkdir /mda/pivots
hadoop fs -mkdir /mda/split
#create the third level directory
hadoop fs -mkdir /mda/data.input/openStreetMap
hadoop fs -mkdir /mda/datasets/openStreetMap
hadoop fs -mkdir /mda/datasets/openStreetMap/50K
hadoop fs -mkdir /mda/group/openStreetMap
hadoop fs -mkdir /mda/index/openStreetMap
hadoop fs -mkdir /mda/merge/openStreetMap
hadoop fs -mkdir /mda/output/openStreetMap
hadoop fs -mkdir /mda/partitions/openStreetMap
hadoop fs -mkdir /mda/pivots/openStreetMap
hadoop fs -mkdir /mda/split/openStreetMap
hadoop fs -mkdir /mda/data.input/forest
hadoop fs -mkdir /mda/datasets/forest
hadoop fs -mkdir /mda/group/forest
hadoop fs -mkdir /mda/index/forest
hadoop fs -mkdir /mda/merge/forest
hadoop fs -mkdir /mda/output/forest
hadoop fs -mkdir /mda/partitions/forest
hadoop fs -mkdir /mda/pivots/forest
hadoop fs -mkdir /mda/split/forest



#upload data set to the folder /mda/datasets/openStreetMap
hadoop fs -put data/rayzit/rayzit.txt /mda/datasets/openStreetMap/50K/0.data
hadoop fs -cp /mda/datasets/openStreetMap/50K/0.data /mda/datasets/openStreetMap/50K/1.data
#hadoop fs -put 10-10.data /mda/datasets/openStreetMap/50K/0.data
#hadoop fs -put 10-10.data /mda/datasets/openStreetMap/50K/1.data

#SET TIME
STARTTIME=$(($(date +%s%N)/1000000))
#command block that takes time to complete...
#add id
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.AddRId /mda/datasets/openStreetMap/50K/1.data
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.AddRId /mda/datasets/openStreetMap/50K/0.data

#select pivots
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.pivotselection.PivotSelectionFactory -conf conf/openStreetMap-50K-2-50K-random.conf

#balance data accross mappers
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.MixJoinData -conf conf/openStreetMap-50K-2-50K-random.conf

#data split
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.DataSplit -conf conf/openStreetMap-50K-2-50K-random.conf

#merge index
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.MergeIndex -conf conf/openStreetMap-50K-2-50K-random.conf
#SET TIME
ENDTIME=$(($(date +%s%N)/1000000))
TOTAL=$(($ENDTIME-$STARTTIME))

echo -e ">>Total preprocess time is \t"$TOTAL;

# remove invalid index
hadoop fs -rm /mda/index/openStreetMap/50K/random/200/summary-m-*
#run join using partition based approach under Nest Loop manner
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.KNNJoinNL -conf conf/openStreetMap-50K-2-50K-random.conf
# merge the  final result
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.KNNJoinMerge -conf conf/openStreetMap-50K-2-50K-random.conf

# run join using NL
#hadoop fs -rmr /mda/output/openStreetMap/50K/random/50K
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.BaselineNL -conf conf/openStreetMap-50K-2-50K-random.conf
#hadoop fs -rmr /mda/output/openStreetMap/merge/50K/random/50K
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.KNNJoinMerge -conf conf/openStreetMap-50K-2-50K-random.conf

# run join using NLRtree
#hadoop fs -rmr /mda/output/openStreetMap/50K/random/50K
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.BaselineRtree -conf conf/openStreetMap-50K-2-50K-random.conf
#hadoop fs -rmr /mda/output/openStreetMap/merge/50K/random/50K
#hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.KNNJoinMerge -conf conf/openStreetMap-50K-2-50K-random.conf
#SET TIME
STARTTIME=$(($(date +%s%N)/1000000))
# run join using partition based method
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.preprocess.GeometricGrouping -conf conf/openStreetMap-50K-2-50K-random.conf
hadoop -cp simMR.jar sg.edu.nus.mda.simquery.knnjoin.KNNJoin -conf conf/openStreetMap-50K-2-50K-random.conf
#SET TIME
ENDTIME=$(($(date +%s%N)/1000000))
TOTAL=$(($ENDTIME-$STARTTIME))
echo -e ">>Total Find time is \t"$TOTAL;
echo "============================================================================";

