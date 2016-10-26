package sg.edu.nus.mda.simquery.pivotselection.kmean;
import java.util.Random;

/**
 * kmean with convergence factor
 * 
 * <p>
 * time:2011-6-1
 * </p>
 * 
 * @author T. QIN
 */
public class KmeansCluster {
	private double[][] dataSet = null;
	private int k = 0;
	private double[][] centers = null;
	int[] clusters;
	private boolean convergence = false;
	private double convergenceDis = 0;
	private int replicates = 0;

	public KmeansCluster(double[][] x, int k) throws ClusterException {
		if (x == null || x.length == 0) {
			throw new ClusterException("K is not set or the dataset cannot be empty");
		}
		clusters = new int[x.length];
		this.dataSet = x;
		this.k = k;
		this.centers = new double[k][dataSet[0].length];
	}

	private void initKCenters() {
		Random r = new Random();
		int rn = r.nextInt(dataSet.length);
		for (int i = 0; i < this.k; i++)
		{
			for (int j = 0; j < dataSet[0].length; j++) {
				centers[i][j] = dataSet[rn][j];
			}
			rn = r.nextInt(dataSet.length);
		}
	}

	public void train() {
		if (replicates > 1) {
		} else {
			beginTrain();
		}
	}

	private void beginTrain() {
		int rows = dataSet.length;
		int cols = dataSet[0].length;
		int round = 0;
		int vote = 0;
		initKCenters();
		convergence = false;
		while (!convergence) {
			double minDistance = Double.MAX_VALUE;
			double currentDis = 0.0;
			int count = 0;
			int changedCenterNumber = 0;
			double[] temp = new double[cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < this.k; j++) {
					currentDis = Utils.distance(dataSet[i], centers[j]);
					if (currentDis < minDistance) {
						minDistance = currentDis;
						clusters[i] = j;
					}
				}
				minDistance = Double.MAX_VALUE;
			}
			for (int i = 0; i < this.k; i++) {
				for (int j = 0; j < clusters.length; j++) {
					if (clusters[j] == i) {
						temp = Utils.add(temp, dataSet[j]);
						count++;
					}
				}
				if (count != 0) {
					temp = ArrayCompute.devideC(temp, count);
					if (isCenterConvergence(centers[i], temp)) {
						vote++;
					}
					centers[i] = temp;
					changedCenterNumber++;
				}
				count = 0;

				temp = new double[cols];
			}
			if (vote == changedCenterNumber) {
				convergence = true;
			}
			vote = 0;
			changedCenterNumber = 0;
			System.out.println("==================round: " + round++);
		}
	}

	/**
	 * 
	 * @param center
	 * @param pCenter
	 * @return
	 * @see:
	 */
	private boolean isCenterConvergence(double[] center, double[] pCenter) {
		boolean result = true;
		double[] distance = ArrayCompute.minus(center, pCenter);
		for (int i = 0; i < distance.length; i++) {
			if (Math.abs(distance[i]) > convergenceDis) {
				result = false;
			}
		}
		return result;
	}

	public double[][] getCenters() {
		return centers;
	}

	public int[] getClusters() {
		return clusters;
	}
}
