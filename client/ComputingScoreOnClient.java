package client;

import server.PaillierOnServer;
import util.Mode;
import util.CryptosystemAbstract;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;
import util.ComputingScore;
import java.util.Random;
import util.AdditivelyBlindProtocol;

public class ComputingScoreOnClient extends ComputingScore {
	private CryptosystemAbstract mPaillier;
	public ComputingScoreOnClient() {		
		super();
	}
	public ComputingScoreOnClient(Mode m, Queue<Object> q, CryptosystemAbstract p) {
		super(m, q);
		this.mPaillier = p;
	}
	
	public void run() {
		while(!getMode().isComputedScore()) {
			while(!getMode().isComputingScoreOnClient()) {;}//Wait
			
			System.out.println("[C][STRAT]\tCompute Server distance.");
			BigInteger x_dec = BigInteger.ZERO;
			BigInteger s3_c = BigInteger.ZERO;
			
			for(int i=0; i<ClientSystem.BIN_HISTO; i++) {		
				// de[x] = x
				x_dec = mPaillier.Decryption((new BigInteger(getQueue().poll().toString())));
				/**
				 * System.out.print(x_dec + " ");
				 */
				// S3' = S3' + x^2
				s3_c = s3_c.add(x_dec.pow(2));
				//s3_c = s3_c.add((new BigInteger(this.getQueue().poll().toString())));				
			}			
			/**
			 * System.out.println();
			 */
			System.out.println("[C][SUCCESS]\trecv Server [x].");
			// Send [S3'] to server
			System.out.println("[C][STRAT]\tsend [S3'].");
			//this.getQueue().add(s3_c);		
			getQueue().add(mPaillier.Encryption(s3_c));
			getMode().setMode(Mode.COMP_SCORE_S);			
		}		
	}
	
	public static void main(String[] args) {
		Mode m = new Mode(Mode.COMP_SCORE_S);
		Queue<Object> q = new LinkedList<Object>();
		PaillierOnClient pc = new PaillierOnClient();
		PaillierOnServer ps = new PaillierOnServer(pc.getPublicKey());
		(new Thread(new ComputingScoreOnClient(m, q, pc))).start();
		int L = 10;
		BigInteger[] w = new BigInteger[ClientSystem.BIN_HISTO];		
		BigInteger[] r = new BigInteger[ClientSystem.BIN_HISTO];
		BigInteger[] w_Enc = new BigInteger[ClientSystem.BIN_HISTO];
		BigInteger[] r_Enc = new BigInteger[ClientSystem.BIN_HISTO];
		BigInteger[] x_Enc = new BigInteger[ClientSystem.BIN_HISTO];		
		BigInteger orig = BigInteger.ZERO;
		
		for(int i=0; i<ClientSystem.BIN_HISTO; i++) {
			w[i] = BigInteger.probablePrime(L, new Random());
			w_Enc[i] = pc.Encryption(w[i]);
			orig = orig.add(w[i].pow(2));
			System.out.print(w[i] + " ");
		}
		System.out.println();
		AdditivelyBlindProtocol ab = new AdditivelyBlindProtocol(ps, w_Enc);
		r = ab.getUniformRandomNumbers();
		for(int i=0; i<ClientSystem.BIN_HISTO; i++) {
			//r[i] = BigInteger.probablePrime(L/2, new Random());
			r_Enc[i] = pc.Encryption(r[i]);			
			System.out.print(r[i] + " ");
		}
		System.out.println();
		x_Enc = ab.getAdditivelyBlindNumbers();
		for(int i=0; i<ClientSystem.BIN_HISTO; i++) {
			//x_Enc[i] = w_Enc[i].multiply(r_Enc[i]).mod(pc.nsquare);			
			System.out.print(pc.Decryption(x_Enc[i]) + " ");
			q.add(x_Enc[i]);
		}
		System.out.println();
		m.setMode(Mode.COMP_SCORE_C);
		while(!m.isComputingScoreOnServer()) {;}
		
		BigInteger s3_c = new BigInteger(q.poll().toString());
		/*
		BigInteger r_neg_square;
		BigInteger r_neg_square_Enc;
		BigInteger r_neg_TWO;
		BigInteger w_r_neg_TWO = BigInteger.ONE;
		BigInteger rightPart = BigInteger.ONE;
		for(int i=0; i<ClientSystem.BIN_HISTO; i++) {
			r_neg_square = BigInteger.ONE.negate().multiply(r[i].pow(2));
			r_neg_square_Enc = pc.Encryption(r_neg_square);
			r_neg_TWO = r[i].multiply(new BigInteger("-2"));
			w_r_neg_TWO = w_Enc[i].modPow(r_neg_TWO, pc.nsquare);
			rightPart = rightPart.multiply(w_r_neg_TWO.multiply(r_neg_square_Enc).mod(pc.nsquare)).mod(pc.nsquare);
		}		
		BigInteger s3_Enc = s3_c.multiply(rightPart).mod(pc.nsquare);
		*/
		BigInteger result = ab.getSumOfThirdPart(s3_c);
		System.out.println("result: " + pc.Decryption(result));
		System.out.println("orig: " + orig);
	}
}
