package sg.edu.nus.mda.simquery.pivotselection.kmean;

import sg.edu.nus.mda.simquery.preprocess.SQConfig;

public class Test2 {
	
	
	
	public static void main(String[] args) {
		String line = "1,1.1,1.1112,aaaaaaaaaa";
		int dim = 2;
		int pos = line.indexOf(SQConfig.sepStrForRecord, 0);

		for (int l = 0; l < dim; l ++) {
			pos = line.indexOf(SQConfig.sepStrForRecord, pos + 1);
		}
		
		System.out.println( line.substring(0, pos));
	}

}
