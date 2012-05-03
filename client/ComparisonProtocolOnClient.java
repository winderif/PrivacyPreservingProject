package client;

import util.Mode;
import java.util.Random;

import java.util.LinkedList;
import java.util.Queue;
import java.math.BigInteger;
import server.ComparisonProtocolOnServer;
import server.PaillierOnServer;

public class ComparisonProtocolOnClient implements Runnable {
	private Mode mMode;
	private Queue<Object> mQueue;
	private PaillierOnClient mPaillier;
	private static final int L = 15;
	private BigInteger TwoPowL = (new BigInteger("2")).pow(L);
	private BigInteger Enc_ZERO;
	
	public ComparisonProtocolOnClient() {
		this.mMode = null;
		this.mQueue = null;
		this.mPaillier = null;
	}
	public ComparisonProtocolOnClient(Mode m, Queue<Object> q, PaillierOnClient p) {
		this.mMode = m;
		this.mQueue = q;
		this.mPaillier = p;				
		Enc_ZERO = BigInteger.ONE;
	}
	
	public void run() {
		while(!mMode.isFindBestMatchingResult()) {
			while(!mMode.isReduceDmodLOnClient()) {;}
			//System.out.println("[C][START]\tReduce d mod 2^L");
			// d = dec.( [d] )			
			BigInteger d = mPaillier.Decryption(new BigInteger(mQueue.poll().toString()));
			//System.out.println("d\t" + d);
			
			// d^ = d mod 2^L
			BigInteger d_head = d.mod(TwoPowL);
			//System.out.println("d mod\t" + d_head);
			BigInteger d_head_Enc = mPaillier.Encryption(d_head);
			// send [d^] to Bob
			mQueue.add(d_head_Enc);
			
			String d_bin = Long.toBinaryString(d_head.longValue());
			//System.out.println(d_bin.toCharArray().length);
			for(int i=0; i<(L+1) - d_bin.length(); i++) {
				//mQueue.add(0);
				mQueue.add(Enc_ZERO);
			}
			for(int i=0; i<d_bin.length(); i++) {			
				// send [[ d_bin ]]
				//System.out.print(d_bin.charAt(i));
				//mQueue.add(d_bin.charAt(i));
				mQueue.add(mPaillier.Encryption(new BigInteger(Character.toString(d_bin.charAt(i)))));
			}			
			mMode.setMode(Mode.CP_MASK_S);
			
			while(!mMode.isCheckWhetherOneOfZero()) {;}
			//System.out.println("[C][START]\tCheck all bit of Mask.");
			
			BigInteger check = BigInteger.ONE;
			while(mQueue.size() != 0) {
				//BigInteger tmp = new BigInteger(mQueue.poll().toString());
				BigInteger tmp = mPaillier.Decryption(new BigInteger(mQueue.poll().toString()));
				//System.out.println("bits = " + tmp);
				if(BigInteger.ZERO.equals(tmp)) {
					break;
				}
				if(mQueue.isEmpty()) {
					check = BigInteger.ZERO;
				}
			}			
			mQueue.clear();
			//System.out.println("check = " + check);
			//mQueue.add(check);
			mQueue.add(mPaillier.Encryption(check));
			mMode.setMode(Mode.CP_GET_LAMBDA_S);
			
			while(!mMode.isFindMinimumOfTwoOnClient()) {;}
			int z_LBS = mPaillier.Decryption(new BigInteger(mQueue.poll().toString())).signum();
			//System.out.println("(" + z_LBS + ")");
			BigInteger EncX = new BigInteger(mQueue.poll().toString());
			BigInteger EncY = new BigInteger(mQueue.poll().toString());
			// z_LBS = 1 <=> x >= y
			// z_LBS = 0 <=> x <  y
			BigInteger m_head = (z_LBS == 1)?(EncY):(EncX);
			mQueue.add(m_head);
			mMode.setMode(Mode.CP_GET_MIN_S);
		}
	}
	
	public static void main(String[] args) {
		Mode m = new Mode(Mode.COMPUTED_SCORE);
		Queue<Object> q = new LinkedList<Object>();
		PaillierOnClient pc = new PaillierOnClient();
		PaillierOnServer ps = new PaillierOnServer(pc.getPublicKey());
		(new Thread(new ComparisonProtocolOnClient(m, q, pc))).start();
		ComparisonProtocolOnServer cp_s = new ComparisonProtocolOnServer(m, q, ps);
		/*
		BigInteger a = pc.Encryption(new BigInteger("813"));
		BigInteger b = pc.Encryption(new BigInteger("769"));		
		BigInteger result = pc.Decryption(cp_s.GreaterEqualThan(a, b));
		System.out.println(result);
		if(result.signum() == 1) {
			System.out.println("yes");
		}
		else {
			System.out.println("No");
		}
		*/
		
		int len = 15;
		BigInteger[] a = new BigInteger[len];
		BigInteger[] tmp = new BigInteger[len];
		for(int i=0; i<len; i++) {
			a[i] = pc.Encryption(BigInteger.probablePrime(15, new Random()));
			System.out.print(pc.Decryption(a[i]) + " ");
		}		
		System.out.println();
		
		while(len != 1) {
			System.out.println("len: " + len);
			for(int j=0; j<len/2; j++) {
				if(cp_s.findMinimumOfTwoEncValues(a[j*2], a[j*2+1]).equals(a[j*2+1])) {
					tmp[j] = a[j*2+1];
					tmp[j+(len+1)/2] = a[j*2];
				}
				else {
					tmp[j] = a[j*2];
					tmp[j+(len+1)/2] = a[j*2+1];
				}				
			}
			if((len/2)*2 < len) {				
				tmp[(len/2)] = a[len-1];				
			}
			for(int k=0; k<a.length; k++) {
				a[k] = tmp[k];
				System.out.print(pc.Decryption(a[k]) + " ");
			}
			System.out.println();
			len = (len+1) / 2;
		}
		m.setMode(Mode.BEST_MATCHED);								
	}	
}
