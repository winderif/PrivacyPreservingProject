package client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.math.BigInteger;

import util.VideoFrame;
import util.Mode; 
import java.util.Queue;

public class ClientSystem implements Runnable {
	private static final int NO_ENC = 0;
	private static final int ENC = 1;
	private int EncState;
	private Mode mMode;	
	private Queue<Object> mQueue;
	private PaillierOnClient mPaillier = null;
	private String queryDirName = null;
	private File[] queryDatasFile = null;	
	private Vector<VideoFrame> videoFrames = new Vector<VideoFrame>();
	// Bin of HSV color histogram = 8 + 4 + 4 = 16 	
	public static final int BIN_HISTO = 16;
	
	private String[] mSuggestedTags = null;
	
	public ClientSystem() {
		this.EncState = NO_ENC;
		this.mMode = null;
		this.mQueue = null;
		this.mPaillier = null;
		this.queryDirName = null;				
	}	
	public ClientSystem(int state, Mode m, Queue<Object> q, String dirName) {
		this.EncState = state;
		this.mMode = m;
		this.mQueue = q;
		this.mPaillier = new PaillierOnClient();
		this.queryDirName = dirName;			
	}	
	
	public void run() {		
		if(this.EncState == NO_ENC) {
			/**
			 * No Enc. version
			 */
			readData();
			sendQueryDatas();
			sendQueryAverageDatas();
		}
		else if(this.EncState == ENC){
			/**
			 * Enc. version
			 */
			readData();
			sendPublicKey();
			sendEncQueryDatas();
			(new Thread(new ComputingScoreOnClient(mMode, mQueue, mPaillier))).start();
			(new Thread(new ComparisonProtocolOnClient(mMode, mQueue, mPaillier))).start();
			//ComputeServerDistance();
		}
		else {
			System.out.println("[C]The state is not No-Enc or Enc.");
			System.exit(0);
		}
	}
	
	// Read image data and generate HSV color histogram
	private void readData() {
		File dirFile = new File(queryDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("[ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {
			System.out.println("[C][START]\tRead query datas.");
			
			this.queryDatasFile = new File[dirFile.listFiles().length];
			for(int i=0; i<dirFile.listFiles().length; i++) {
				this.queryDatasFile[i] = dirFile.listFiles()[i];							
				this.videoFrames.add(new VideoFrame(this.queryDatasFile[i], i));
				//System.out.println(this.queryDatasFile[i].getName());
			}
		}
	}	

	private void sendPublicKey() {		
		System.out.println("[C][STRAT]\tsend public key pair (n, g).");		
		mQueue.add(mPaillier.getPublicKey()[0]);		
		mQueue.add(mPaillier.getPublicKey()[1]);				
		mMode.setMode(Mode.SEND_KEY);
	}	
	private void sendQueryDatas() {				
		System.out.println("[C][STRAT]\tsend Query datas.");
		// Number of Query		
		mQueue.add(videoFrames.size());
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");
				mQueue.add(videoFrames.elementAt(i).getHistogram()[j]);
			}
			//System.out.println();
		}
		//System.out.println();
		mMode.setMode(Mode.SEND_QUERY);
	}
	private void sendQueryAverageDatas() {
		System.out.println("[C][STRAT]\tsend Query Average data.");
		double[] tmpAveHisto = new double[BIN_HISTO];
		Arrays.fill(tmpAveHisto, 0.0);
		
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");
				tmpAveHisto[j] += videoFrames.elementAt(i).getHistogram()[j];				
			}
			//System.out.println();
		}
		//System.out.println();
		for(int i=0; i<BIN_HISTO; i++) {
			tmpAveHisto[i] /= BIN_HISTO;
		}
	}
	private void sendEncQueryDatas() {
		while(!mMode.isSendPublicKeyAlready()) {;}
		System.out.println("[C][STRAT]\tsend Encrypted Query datas.");
		// Number of Query
		mQueue.add(videoFrames.size());		
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				mQueue.add(encryption(this.mPaillier.DoubleToBigInteger(videoFrames.elementAt(i).getHistogram()[j])));
			}
			//System.out.println();
		}
		mMode.setMode(Mode.SEND_QUERY);
	}

	private double[][] getQueryHistogram() {
		double[][] tmpHistogram = new double[this.videoFrames.size()][BIN_HISTO];
		for(int i=0; i<this.videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				tmpHistogram[i][j] = videoFrames.elementAt(i).getHistogram()[j];
			}
		}
		return tmpHistogram;
	}	
	public BigInteger[][] getEncyptedQueryHistogram() {			
		return encryption(mPaillier.DoubleToBigInteger2DArray(getQueryHistogram()));
	}	
	private BigInteger[][] encryption(BigInteger[][] plaintext) {
		System.out.println("[START]\tEncrypt Query.");
		BigInteger[][] ciphertext = new BigInteger[plaintext.length][plaintext[0].length];
		for(int i=0; i<plaintext.length; i++) {
			for(int j=0; j<plaintext[0].length; j++) {
				ciphertext[i][j] = this.mPaillier.Encryption(plaintext[i][j]);
			}
		}
		return ciphertext;
	}	
	private BigInteger encryption(BigInteger plaintext) {		
		//System.out.print(plaintext + " ");
		return this.mPaillier.Encryption(plaintext);
	}	
}
