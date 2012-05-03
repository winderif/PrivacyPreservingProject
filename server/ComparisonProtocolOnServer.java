package server;

import java.math.BigInteger;
import java.util.Queue;
import java.util.Random;

import util.Mode;

public class ComparisonProtocolOnServer{
	private Mode mMode;
	private Queue<Object> mQueue;
	private PaillierOnServer mPaillier;
	private static int RANDOM_BIT = 100;
	private static final int L = 15;
	private static final BigInteger L_big = new BigInteger(Integer.toString(L));
	private static final BigInteger NEG_ONE = BigInteger.ONE.negate();
	private static final BigInteger THREE = new BigInteger("3");	
	private BigInteger TwoPowL = (new BigInteger("2")).pow(L);
	private BigInteger TwoPowNeL;
	private BigInteger[] d_bin_Array;
	private BigInteger[] r_bin_Array;
	private BigInteger[] c_Array;
	
	
	public ComparisonProtocolOnServer() {
		this.mMode = null;
		this.mQueue = null;
		this.mPaillier = null;
		this.d_bin_Array = null;
		this.r_bin_Array = null;
		this.c_Array = null;
	}
	public ComparisonProtocolOnServer(Mode m, Queue<Object> q, PaillierOnServer p) {
		this.mMode = m;
		this.mQueue = q;
		this.mPaillier = p;		
		TwoPowNeL = (new BigInteger("2")).modPow(L_big.negate(), mPaillier.nsquare);		
		this.d_bin_Array = new BigInteger[L+1];
		this.r_bin_Array = new BigInteger[L+1];
		this.c_Array = new BigInteger[L+1];
	}
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		// [z] = [ 2^L + a - b ] = [2^L]*[a]*[b]^(-1)
		
		BigInteger z_Enc = mPaillier.Encryption(TwoPowL)
							.multiply(EncA)
							.multiply(EncB.modInverse(mPaillier.nsquare))
							.mod(mPaillier.nsquare);
		
		//BigInteger z = TwoPowL.add(a).subtract(b);
		//System.out.println("z = \t" + z);		
		//BigInteger r = BigInteger.probablePrime(RANDOM_BIT, new Random());
		BigInteger rr = BigInteger.probablePrime(L/2, new Random());
		String rr_bin = Long.toBinaryString(rr.longValue());
		for(int i=0; i<(L+1) - rr_bin.length(); i++) {
			r_bin_Array[i] = BigInteger.ZERO;
		}
		for(int i=0; i<rr_bin.length(); i++) {
			r_bin_Array[i+(L+1) - rr_bin.length()] = (new BigInteger(Character.toString(rr_bin.charAt(i)))).mod(TwoPowL);
		}
		//System.out.println("rr\t" + rr);
		// [d] = [z + r] = [z]*[r]
		BigInteger d_Enc = z_Enc.multiply(mPaillier.Encryption(rr)).mod(mPaillier.nsquare);
		// send [d] to Alice
		mQueue.add(d_Enc);
		//mQueue.add(mPaillier.Encryption(z).multiply(mPaillier.Encryption(rr)).mod(mPaillier.nsquare));
		mMode.setMode(Mode.CP_REDUCE_C);
		
		while(!mMode.isMaskOnServer()) {;}
		//System.out.println("\t[S][START]\tMasking");
		//BigInteger d_head = new BigInteger(mQueue.poll().toString());
		BigInteger d_head_Enc = new BigInteger(mQueue.poll().toString());
		
		int index = 0;
		while(mQueue.size() != 0) {
			d_bin_Array[index] = new BigInteger(mQueue.poll().toString());
			//System.out.println(mQueue.size()+1 + " " + d_bin_Array[i]);
			index++;
		}
		
		BigInteger s = BigInteger.ONE;
		BigInteger s_Enc = mPaillier.Encryption(s);
		//Mask(s);
		EncMask(s_Enc);
		for(int i=0; i<L+1; i++) {
			//mQueue.add(c_Array[i]);
			mQueue.add(c_Array[i].modPow(rr, mPaillier.nsquare));
		}
		mMode.setMode(Mode.CP_CHECK_C);
		
		while(!mMode.isGetLambdaOnServer()) {;}
		//System.out.println("\t[S][START]\tGet lambda_b value");
		
		//BigInteger lambda = transformLambda(new BigInteger(mQueue.poll().toString()), s);
		BigInteger lambda_Enc = transformLambda(new BigInteger(mQueue.poll().toString()), s);
		//System.out.println("lambda = " + lambda);
				
		BigInteger r_head = rr.mod(TwoPowL);
		BigInteger r_head_Enc = mPaillier.Encryption(r_head);
		//System.out.println("r_head = " + r_head);
		//BigInteger z_head = d_head.subtract(r_head);
		BigInteger z_head_Enc = d_head_Enc.multiply(r_head_Enc.modPow(BigInteger.ONE.negate(), mPaillier.nsquare))
										  .mod(mPaillier.nsquare);
		//System.out.println("z_head = " + z_head); 
		//BigInteger z_mod = z_head.add(lambda.multiply(TwoPowL));
		BigInteger z_mod_Enc = z_head_Enc.multiply(lambda_Enc.modPow(TwoPowL, mPaillier.nsquare))
										 .mod(mPaillier.nsquare);
		//System.out.println("z_mod = " + z_mod);
		//BigInteger z_LBS = TwoPowNeL.multiply(z.subtract(z_mod));
		BigInteger z_LBS_Enc = (z_Enc.multiply(z_mod_Enc.modPow(NEG_ONE, mPaillier.nsquare)).mod(mPaillier.nsquare))
								.modPow(TwoPowNeL, mPaillier.nsquare);
		//System.out.println("z_LBS = " + z_LBS.signum());		
		//return z_LBS;
		
		mQueue.add(z_LBS_Enc);
		mQueue.add(EncA);
		mQueue.add(EncB);
		mMode.setMode(Mode.CP_FIND_MIN_C);
		
		while(!mMode.isGetMinimumValueOnServer()) {;}
		BigInteger EncM_head = new BigInteger(mQueue.poll().toString());
		/*
		if(EncM_head.equals(EncB)) {
			System.out.println("\tEncB <= EncA");
		}
		else {
			System.out.println("\tEncA < EncB");
		}
		*/
		return EncM_head;
	}
	private void Mask(BigInteger s) {			
		for(int i=0; i<L+1; i++) {			
			c_Array[i] = (d_bin_Array[i].subtract(r_bin_Array[i]).add(s)).add(THREE.multiply(SumOfXOR(i)));
			//System.out.println(c_Array[i] + " " + d_bin_Array[i] + " " + r_bin_Array[i]);			
		}
	}
	private void EncMask(BigInteger s) {			
		for(int i=0; i<L+1; i++) {
			c_Array[i] = (d_bin_Array[i].multiply(mPaillier.Encryption(r_bin_Array[i].negate())).multiply(s))
			             	.multiply(MultiplyOfEncXOR(i).modPow(THREE, mPaillier.nsquare))
			             	.mod(mPaillier.nsquare);								
		}
	}
	private BigInteger SumOfXOR(int last) {
		BigInteger tmp = BigInteger.ZERO;
		for(int j=0; j<last; j++) {
			tmp = tmp.add(d_bin_Array[j].xor(r_bin_Array[j]));
		}
		return tmp;
	}
	private BigInteger MultiplyOfEncXOR(int last) {
		BigInteger tmp = BigInteger.ONE;
		for(int j=0; j<last; j++) {
			tmp = tmp.multiply(mPaillier.EncXOR(d_bin_Array[j], mPaillier.Encryption(r_bin_Array[j]), r_bin_Array[j]))
					 .mod(mPaillier.nsquare);
		}		
		return tmp;
	}	
	
	private BigInteger transformLambda(BigInteger lambda_b, BigInteger s) {
		if(s.equals(BigInteger.ONE)) {
			return lambda_b;
		}
		else {
			//return lambda_b.xor(BigInteger.ONE);
			return mPaillier.EncXOR(lambda_b, mPaillier.Encryption(BigInteger.ONE), BigInteger.ONE);
		}
	}
}
