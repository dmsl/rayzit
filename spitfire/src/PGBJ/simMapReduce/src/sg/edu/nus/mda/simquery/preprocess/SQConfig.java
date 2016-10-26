package sg.edu.nus.mda.simquery.preprocess;

public class SQConfig {
	/** configuration paths */
	public static final String configPath1 = "/usr/local/hadoop/conf/core-site.xml";
	public static final String configPath2 = "/usr/local/hadoop/conf/hdfs-site.xml";
	public static final String configPath3 = "/home/crowdhood/hadoop-exp/conf/openStreetMap-50K-2-50K-random.conf";
//	public static final String configPath3 = "/home/cs246/Desktop/source code of mapjoin/conf/openStreetMap-50K-2-50K-random.conf";
	///home/crowdhood/hadoop-exp
	/** metric space */
	public static final String strMetricSpace = "simquery.metricspace.dataspace";
	/** metric */
	public static final String strMetric = "simquery.metricspace.metric";
	/** number of K */
	public static final String strK = "simquery.query.threshold.K";
	/** number of dimensions */
	public static final String strDimExpression = "simquery.vector.dim";
	/** file extension names of indexes, maintained for DistributedCache */
	public static final String strIndexExpression1 = ".index1";
	public static final String strIndexExpression2 = ".index2";
	public static final String strPivotExpression = ".pivot";
	public static final String strGroupExpression = ".group";
	/** seperator for items of every record in the index */
	public static final String sepStrForIndex = ",";
	public static final String sepStrForRecord = ",";
	public static final String sepStrForKeyValue = "\t";
	public static final String sepStrForIDDist = "|";
	public static final String sepSplitForIDDist = "\\|";
	public static final String sepSplitForGroupIDs = " ";
	/**============================= pivot selection ================ */
	/** number of pivots */
	public static final String strNumOfPivots = "simquery.pivot.count";
	public static final String strPivotInput = "simquery.pivot.input";
	public static final String strSelStrategy = "simquery.pivot.selection.strategy";
	public static final String strSampleSize = "simquery.pivot.sample.size";
	public static final String strDatasetSize = "simquery.pivot.dataset.size";
	/**============================= MixJoinData ================ */
	public static final String dataset = "simquery.dataset.input.dir";
	public static final String dataSplitInput = "simquery.dataset.balance.input.dir";
	public static final String strNumOfGroups = "simquery.group.count";
	// DataSplit
	public static final String strIndexOutput = "simquery.index.output";
	public static final String strMergeIndexOutput = "simquery.merge.index.output";
	public static final String strKNNJoinInput = "simquery.datasplit.output";
	// BaseLineNL
	public static final String strKNNJoinOutput = "simquery.knn.join.output";
	public static final String strKNNJoinOutputMerge = "simquery.knn.join.output.merge";
	public static final String strKNNJoinOutputMergeToOne = "simquery.knn.join.output.merge.toOne";
	// KNNJoin
//	public static final String strGroupInput = "simquery.group.input";
	public static final String strGroupOutput = "simquery.group.output";
	// BaselineRtree
	public static final String strMinNodeEntries = "simquery.rtree.node.entry.min";
	public static final String strMaxNodeEntries = "simquery.rtree.node.entry.max";
	public static final String strRTreeIndex = "simquery.BaseLineRTree.SpatialIndex.RTree";
}
