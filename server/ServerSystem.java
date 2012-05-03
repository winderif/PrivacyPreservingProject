package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.Scanner;
import java.util.Arrays;
import java.math.BigInteger;
import javax.imageio.ImageIO;

import java.util.Queue;
import util.VideoFrame;
import util.EncFastHungarianAlgorithm;
import util.FastHungarianAlgorithm;
import util.AdditivelyBlindProtocol;
import util.Mode;
import server.PaillierOnServer;

public class ServerSystem implements Runnable {
	private static final int NO_ENC = 0;
	private static final int ENC = 1;
	private int EncState;
	private Mode mMode;	
	private Queue<Object> mQueue;
	private PaillierOnServer mPaillier;
	private String databaseDirName = null;
	private File[] databaseTagDirFile = null;	
	private Vector<File[]> databaseDatasFile = new Vector<File[]>();
	private Vector<Vector<VideoFrame>> databaseDatas = new Vector<Vector<VideoFrame>>();
	private String[] allTags = null;
	
	private static int FILTER_NUM = 2;	
	// Bin of HSV color histogram = 8 + 4 + 4 = 16 
	private static int BIN_HISTO = 16; 	
	
	private double[][] mTagAverageHistogram = null;
	private BigInteger[][] mEncTagAverageHistogram = null;
	private double[][] mQueryHistogram = null;
	private BigInteger[][] mEncQueryHistogram = null;
	private double[][] mHungarianMatrix = null;
	private BigInteger[][] mEncHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
	private HashMap<String, Vector<VideoFrame>> imageClustersMap = new HashMap<String, Vector<VideoFrame>>();	
	
	public ServerSystem() {
		this.EncState = NO_ENC;
		this.mMode = null;
		this.mQueue = null;
		this.mPaillier = null;
		this.databaseDirName = null;
	}	
	public ServerSystem(int state, Mode m, Queue<Object> q, String dirName) {
		this.EncState = state;
		this.mMode = m;
		this.mQueue = q;
		this.mPaillier = null;
		this.databaseDirName = dirName;			
	}
	
	public void run() {
		if(this.EncState == NO_ENC) {
			/**
			 * No Enc. version
			 */
			readData();
			generateTagClusters();
			getAverageColorHistogram();
			recvClientDatas();
			buildBipartileGraph();
			FindBestMatching();
		}
		else if(this.EncState == ENC) {
			/**
			 * Enc. version
			 */
			readData();
			recvClientPublicKey();
			generateTagClusters();
			getAverageColorHistogram();
			EncrptTagAverageHistogram();
			recvClientEncDatas();
			buildEncBipartileGraph();
			FindBestEncMatching();		
		}
		else {
			System.out.println("\t[S]The state is not No-Enc or Enc.");
			System.exit(0);
		}
	}
	
	// Read image data and generate HSV color histogram
 	private void readData() {
		File dirFile = new File(databaseDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("\t[S][ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {			
			System.out.println("\t[S][START]\tRead database datas.");
			
			File[] tmpFileArray = null; 
			Vector<VideoFrame> tmpVideoFrame = null;
			this.databaseTagDirFile = new File[dirFile.listFiles().length];
			
			for(int i=0; i<dirFile.listFiles().length; i++) {				
				this.databaseTagDirFile[i] = dirFile.listFiles()[i];
	
				if(this.databaseTagDirFile[i].listFiles().length != 0) {					
					int sizeOfTagDir = this.databaseTagDirFile[i].listFiles().length;
					tmpFileArray = new File[sizeOfTagDir];		
					tmpVideoFrame = new Vector<VideoFrame>();
					
					for(int j=0; j<sizeOfTagDir; j++) {
						if(this.databaseTagDirFile[i].listFiles()[j].getName().endsWith(".jpg")) {
							tmpFileArray[j] = this.databaseTagDirFile[i].listFiles()[j];
							tmpVideoFrame.add(new VideoFrame(tmpFileArray[j], j));
							//System.out.println("[DIR] " + tmpFileArray[j].getName());
						}						
					}
					databaseDatasFile.add(tmpFileArray.clone());
					databaseDatas.add(tmpVideoFrame);
				}											
				//System.out.println(this.databaseTagDirFile[i].getName());
			}
		}
	}			
	
 	private void recvClientPublicKey() {
 		while(!this.mMode.isSendPublicKey()) {;} 		
 		BigInteger[] pkey = new BigInteger[2]; 		
 		pkey[0] = new BigInteger(this.mQueue.poll().toString());
 		pkey[1] = new BigInteger(this.mQueue.poll().toString()); 		
 		this.mPaillier = new PaillierOnServer(pkey);
 		mMode.setMode(Mode.SEND_KEY_ACK);
 		System.out.println("\t[S][SUCCESS]\treceive public key pair (n, g).");
 	}
 	private void recvClientDatas() {
 		while(!this.mMode.isSendQueryDatas()) {;} 		
 		this.mQueryHistogram = new double[Integer.parseInt(mQueue.poll().toString())][BIN_HISTO];
 		for(int i=0; i<this.mQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				this.mQueryHistogram[i][j] = (Double)this.mQueue.poll();
 			}
 		}
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
 	} 
 	private void recvClientEncDatas() {
 		while(!this.mMode.isSendQueryDatas()) {;} 		 		
 		this.mEncQueryHistogram = new BigInteger[Integer.parseInt(mQueue.poll().toString())][BIN_HISTO];
 		for(int i=0; i<mEncQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				this.mEncQueryHistogram[i][j] = new BigInteger(this.mQueue.poll().toString());
 				//System.out.println(this.mEncQueryHistogram[i][j]);
 			}
 			//System.out.println();
 		}
 		System.out.println("\t[S][SUCCESS]\treceive Encrypted Query datas.");
 	} 	                             
 	
 	private void sendDistanceAdditivelyBlindDatas(BigInteger[] x) {
 		System.out.println("\t[S][STRAT]\tsend AB datas of distance.");
 		for(int i=0; i<BIN_HISTO; i++) { 		
 			this.mQueue.add(x[i]);
 		}
 		this.mMode.setMode(Mode.COMP_SCORE_C); 		
 	}
 	
	private void generateTagClusters() {
		System.out.println("\t[S][START]\tGenerate Photo Tag Clusters");
		Vector<VideoFrame> tmpPhotos = null;
		String[] tmpTags = null;
		Vector<String> stopwordVector = null;
				
        try {
    		File stopwords = new File("Stopwords.txt");
            Scanner scanner = new Scanner(stopwords);
        	
        	for(int i=0; i<this.databaseDatas.size(); i++) {
        		for(int j=0; j<this.databaseDatas.elementAt(i).size(); j++) {
        			//System.out.print(this.databaseDatasFile.elementAt(i)[j] + " ");
				
        			tmpTags = this.databaseDatas.elementAt(i).elementAt(j).getTags();
        			for(int k=0; k<tmpTags.length; k++) {
        				if(imageClustersMap.get(tmpTags[k]) == null) {
        					tmpPhotos = new Vector<VideoFrame>();
        					tmpPhotos.add(this.databaseDatas.elementAt(i).elementAt(j));
        				} else {
        					tmpPhotos = imageClustersMap.get(tmpTags[k]);
        					tmpPhotos.add(this.databaseDatas.elementAt(i).elementAt(j));
        				}
        				imageClustersMap.put(tmpTags[k], tmpPhotos);	
        			}
        		}
        	}	        	        
        	
        	String remove = "";
			stopwordVector = new Vector<String>();

	        while(scanner.hasNext()){
	        	remove = scanner.nextLine().trim();
	        	stopwordVector.add(remove);
	        	if(imageClustersMap.containsKey(remove)){
	        		imageClustersMap.remove(remove);
	        	}
	        }
	        	        
			String[] allTags = new String[imageClustersMap.keySet().size()];
			imageClustersMap.keySet().toArray(allTags);
			//System.out.println("[INFO]\t allTags.length: " + allTags.length);
			//System.out.println("[INFO]\t map.keySet().size(): "+imageClustersMap.keySet().size());
			
			/*** 移除小於4張圖的Tag cluster ***/
			for(int i = 0; i<allTags.length; i++) {
				if(imageClustersMap.get(allTags[i]).size() < FILTER_NUM) {
					imageClustersMap.remove(allTags[i]);
				}						
			}
			allTags = new String[imageClustersMap.keySet().size()];
			imageClustersMap.keySet().toArray(allTags);
			//System.out.println("[INFO]\t After remove : allTags.length: "+allTags.length);
			//System.out.println("[INFO]\t After remove : map.keySet().size(): "+imageClustersMap.keySet().size());
			/*** 移除小於4張圖的Tag cluster *** END */
        	
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e);
			System.exit(0);
		}						
	}

	/**	 
	 * Get mTagAverageHistogram[] and mEncTagAverageHistogram[]
	 * 12.03.13 winderif
	 */
	private void getAverageColorHistogram() {
		System.out.println("\t[S][START]\tGet Average Color Histogram");
		double[] tmpHistogram = null;
		this.mTagAverageHistogram = new double[this.imageClustersMap.keySet().size()][BIN_HISTO];
		this.allTags = new String[this.imageClustersMap.keySet().size()];
		this.imageClustersMap.keySet().toArray(allTags);
		for(int i=0; i<this.imageClustersMap.keySet().size(); i++) {
			//System.out.println("[TAG]\t" + allTags[i] + "\t" + this.imageClustersMap.get(allTags[i]).size());			
			for(VideoFrame mPhoto : this.imageClustersMap.get(allTags[i])) {
				tmpHistogram = mPhoto.getHistogram();
				for(int j=0; j<BIN_HISTO; j++) {
					this.mTagAverageHistogram[i][j] += tmpHistogram[j];
				}
			}
			
			for(int j=0; j<BIN_HISTO; j++) {
				this.mTagAverageHistogram[i][j] /= this.imageClustersMap.get(allTags[i]).size();
				//System.out.print(this.mTagAverageHistogram[i][j] + " ");
			}
			//System.out.println();
		}
		System.out.println("\t[S][SUCCESS]\tGet Average Color Histogram");
	}
	
	private void EncrptTagAverageHistogram() {		
		System.out.println("\t[S][START]\tEncrypt Database.");
		this.mEncTagAverageHistogram = 
			encryption(this.mPaillier.DoubleToBigInteger2DArray(this.mTagAverageHistogram));
		System.out.println("\t[S][SUCCESS]\tEncrypt Database.");
	}
	
	private double[] calculateWeighting(int index) {
		double[] tmpWeight = new double[BIN_HISTO];
		Arrays.fill(tmpWeight, 1.0);
		double[] tmpHistogram = new double[BIN_HISTO];
		Arrays.fill(tmpHistogram, 0.0);
		
		Vector<VideoFrame> tmpTagCluster = this.imageClustersMap.get(this.allTags[index]);
		double[] z_bar = new double[BIN_HISTO];
		Arrays.fill(z_bar, 0.0);
		double length = tmpTagCluster.size();		
		double epsilon = 1.0;
				
		// Sum of all histogram in tag cluster. 
		for(int l=0; l<length; l++) {
			for(int i=0; i<BIN_HISTO; i++) {
				tmpHistogram[i] += tmpTagCluster.elementAt(l).getHistogram()[i];
				z_bar[i] += (tmpTagCluster.elementAt(l).getHistogram()[i] > 0.0)?(1.0):(0.0);
			}
		}
		for(int i=0; i<BIN_HISTO; i++) {
			tmpWeight[i] = (tmpHistogram[i] / (z_bar[i]+epsilon))*(1.0 - (z_bar[i] / (length+1.0)));
		}		
		return tmpWeight;
	}
		
	private BigInteger[][] encryption(BigInteger[][] plaintext) {		
		BigInteger[][] ciphertext = new BigInteger[plaintext.length][plaintext[0].length];
		for(int i=0; i<plaintext.length; i++) {
			for(int j=0; j<plaintext[0].length; j++) {
				//System.out.print(plaintext[i][j] + " ");
				ciphertext[i][j] = this.mPaillier.Encryption(plaintext[i][j]);
			}
			//System.out.println();
		}
		return ciphertext;
	}
	
	public void buildBipartileGraph() {
		System.out.println("\t[S][START]\tBuild Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mHungarianMatrix = new double[this.mQueryHistogram.length][this.mTagAverageHistogram.length];
		for(int i=0; i<this.mQueryHistogram.length; i++) {
			for(int j=0; j<this.mTagAverageHistogram.length; j++) {				
				this.mHungarianMatrix[i][j] = Score(this.mQueryHistogram[i], this.mTagAverageHistogram[j]);
				//this.mHungarianMatrix[i][j] = Score(this.mQueryHistogram[i], this.mTagAverageHistogram[j], j);
				//System.out.print(this.mHungarianMatrix[i][j] + " ");
			}
			//System.out.println();
		}
		//System.out.println();
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Bipartile Graph." + time);
	}
	
	private void buildEncBipartileGraph() {
		System.out.println("\t[S][START]\tBuild Encrypted Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryHistogram.length][this.mEncTagAverageHistogram.length];
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			for(int j=0; j<this.mEncTagAverageHistogram.length; j++) {
				System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				this.mEncHungarianMatrix[i][j] = 
					EncScore(this.mEncQueryHistogram[i], this.mEncTagAverageHistogram[j], j);
			}
		}		
		this.mMode.setMode(Mode.COMPUTED_SCORE);
		
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);
	}
	
	public void FindBestMatching() {
		System.out.println("\t[S][START]\tFind Bset Matching for Bipartile Graph.");
		
		String sumType = "max";
		int[][] assignment = new int[this.mHungarianMatrix.length][2];
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = FastHungarianAlgorithm.hgAlgorithm(this.mHungarianMatrix, sumType);
		/*** ***/
		double endTime = System.nanoTime();		
		double time = (endTime - startTime)/1000000000.0;
		
		for(int k=0; k<assignment.length; k++) {
			System.out.printf("array(%d,%d) = %.2f %s\n", (assignment[k][0]+1), (assignment[k][1]+1),
					this.mHungarianMatrix[assignment[k][0]][assignment[k][1]], this.allTags[assignment[k][1]]);			
		}
		mMode.setMode(Mode.BEST_MATCHED);
		System.out.println("\t[S][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph." + time);
		
		mMatchingTags = new String[this.mQueryHistogram.length];		
		for(int i=0; i<this.mQueryHistogram.length; i++) {
			this.mMatchingTags[i] = this.allTags[assignment[i][1]];
			System.out.println("[MATCH]\t" + (i+1) + "\t" + this.mMatchingTags[i]);
		}
	}
	
	public void FindBestEncMatching() {				
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "max";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];
		EncFastHungarianAlgorithm EncFHA = 
			new EncFastHungarianAlgorithm(new ComparisonProtocolOnServer(mMode, mQueue, mPaillier), mPaillier, null);
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = EncFHA.hgAlgorithm(this.mEncHungarianMatrix, sumType);
		/*** ***/
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		
		for(int k=0; k<assignment.length; k++) {
			System.out.printf("array(%d,%d) = %s %s\n", 
					(assignment[k][0]+1), 
					(assignment[k][1]+1),
					this.mEncHungarianMatrix[assignment[k][0]][assignment[k][1]].toString(), 
					this.allTags[assignment[k][1]]);			
		}
		mMode.setMode(Mode.BEST_MATCHED);
		System.out.println("\t[S][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph." + time);
		
		mMatchingTags = new String[this.mEncQueryHistogram.length];		
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			this.mMatchingTags[i] = this.allTags[assignment[i][1]];
			System.out.println("[MATCH]\t" + (i+1) + "\t" + this.mMatchingTags[i]);
		}
	}
	
	public String[] getSuggestedTags() {
		return this.mMatchingTags;
	}
	
	// Calculating score with square Euclidain distance
	private double Score(double[] keyframeHistogram, double[] tagPhotosHistogram) {
		double tmpScore = 0.0;
		double diff = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			diff = keyframeHistogram[i] - tagPhotosHistogram[i];
			tmpScore += diff*diff;
		}
		return tmpScore;
	}
	
	// Calculating score with weighting scheme
	private double Score(double[] keyframeHistogram, double[] tagPhotosHistogram, int indexOfTag) {		
		double[] tmpWeight = calculateWeighting(indexOfTag);
		double tmpScore = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			tmpScore += tmpWeight[i]*Math.min(keyframeHistogram[i], tagPhotosHistogram[i]);
		}
		return tmpScore;
	}

	// Calculating score with square Euclidain distance
	private BigInteger EncScore(BigInteger[] EncKeyframeHistogram, BigInteger[] EncTagPhotosHistogram, int indexOfTag) {
		BigInteger tmpScore = BigInteger.ONE;
		double tmpS1 = 0.0;
		BigInteger tmpS2 = BigInteger.ONE;
		BigInteger tmpPow = BigInteger.ONE;
		BigInteger[] tmpTagHistogram = mPaillier.DoubleToBigInteger1DArray(this.mTagAverageHistogram[indexOfTag]);
		BigInteger s1 = BigInteger.ZERO;
		BigInteger s2 = BigInteger.ZERO;
		BigInteger s3 = BigInteger.ZERO;
		BigInteger s3_c = BigInteger.ZERO;		
		
		/*** S1 ***/
		for(int i=0; i<BIN_HISTO; i++) {	
			// w*w
			tmpS1 += this.mTagAverageHistogram[indexOfTag][i]*this.mTagAverageHistogram[indexOfTag][i];
		}
		s1 = mPaillier.Encryption(mPaillier.DoubleToBigInteger(tmpS1));
		
		/*** S2 ***/
		for(int i=0; i<BIN_HISTO; i++) {
			// (-2)*w
			tmpPow = tmpTagHistogram[i].multiply(new BigInteger("-2"));				
			// [w_bar]^((-2)*w) and w1*w2
			tmpS2 = tmpS2.multiply(EncKeyframeHistogram[i].modPow(tmpPow, mPaillier.nsquare));
		}
		// (w1*w2*...wK) mod N
		s2 = tmpS2.mod(mPaillier.nsquare);		
		
		AdditivelyBlindProtocol r = new AdditivelyBlindProtocol(this.mPaillier, EncKeyframeHistogram);
		//System.out.print("[S1]" + this.mMode.getMode());
		sendDistanceAdditivelyBlindDatas(r.getAdditivelyBlindNumbers());
		//System.out.print("[S2]" + this.mMode.getMode());
		while(!this.mMode.isComputingScoreOnServer()) {;}
		
		// recv [S3']
		s3_c = new BigInteger(this.mQueue.poll().toString());
		s3 = r.getSumOfThirdPart(s3_c);
		//System.out.println("s3 = " + s3);
		
		tmpScore = tmpScore.multiply(s1).multiply(s2).multiply(s3).mod(mPaillier.nsquare);
		//System.out.println("tmp = " + tmpScore);
		
		return tmpScore;
	}	
}
