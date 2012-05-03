package util;

import java.math.BigInteger;
import java.util.Random;
import server.PaillierOnServer;

public class AdditivelyBlindProtocol {
	private PaillierOnServer mPaillier = null;
	private BigInteger[] mUniformRandomNumbers = null;
	private BigInteger[] mEncUniformRandomNumbers = null;
	private BigInteger[] mEncOriginalNumbers = null;
	private BigInteger[] mEncBlindNumbers = null;
	private int vectorLength;
	private static int RANDOM_BIT = 10; 
	
	public AdditivelyBlindProtocol(PaillierOnServer serverPaillier, BigInteger[] originalNumbers) {
		this.mPaillier = serverPaillier;
		this.vectorLength = originalNumbers.length;
		this.mUniformRandomNumbers = new BigInteger[vectorLength];
		this.mEncUniformRandomNumbers = new BigInteger[vectorLength];
		this.mEncOriginalNumbers = originalNumbers;
		this.mEncBlindNumbers = new BigInteger[vectorLength];
		numberAssignment();
		this.mEncUniformRandomNumbers = encryption(this.mUniformRandomNumbers);
		this.mEncBlindNumbers = CalculateAdditivelyBlindNumbers();
	}
	
	private void numberAssignment() {
		for(int i=0; i<this.vectorLength; i++) {
			this.mUniformRandomNumbers[i] = BigInteger.probablePrime(RANDOM_BIT, new Random());			
		}
	}
	
	private BigInteger[] encryption(BigInteger[] plaintexts) {
		BigInteger[] ciphertexts = new BigInteger[this.vectorLength];
		for(int i=0; i<this.vectorLength; i++) {
			ciphertexts[i] = this.mPaillier.Encryption(plaintexts[i]);			
		}
		return ciphertexts;
	}
	
	private BigInteger[] CalculateAdditivelyBlindNumbers() {
		BigInteger[] tmp = new BigInteger[vectorLength];
		for(int i=0; i<this.vectorLength; i++) {
			//tmp[i] = this.mEncOriginalNumbers[i];			
			tmp[i] = this.mEncOriginalNumbers[i].multiply(this.mEncUniformRandomNumbers[i])
												.mod(this.mPaillier.nsquare);			
		}
		return tmp;
	}
	
	public BigInteger[] getAdditivelyBlindNumbers() {
		/** Testing
		BigInteger[] tmp = new BigInteger[16];
		for(int i=0; i<16; i++) {
			tmp[i] = new BigInteger(Integer.toString(i+1));
		}
		return tmp;
		*/
		return this.mEncBlindNumbers;
	}
	
	public BigInteger getSumOfThirdPart(BigInteger s3_c) {
		BigInteger r_neg_square = BigInteger.ZERO;
		BigInteger neg_TWO_r = BigInteger.ZERO;
		BigInteger w_r_neg_TWO = BigInteger.ONE;
		BigInteger tmpRight = BigInteger.ONE;
		for(int i=0; i<this.vectorLength; i++) {
			// -(r^2) = (-1)*(r^2)
			r_neg_square = BigInteger.ONE.negate().multiply(mUniformRandomNumbers[i].pow(2));
			// (-2)*r
			neg_TWO_r = mUniformRandomNumbers[i].multiply(new BigInteger("-2"));
			// [w]^(-2r) mod N
			w_r_neg_TWO = mEncOriginalNumbers[i].modPow(neg_TWO_r, mPaillier.nsquare);
			// [w]^(-2r) * [-(r^2)]						
			tmpRight = tmpRight.multiply(w_r_neg_TWO.multiply(mPaillier.Encryption(r_neg_square)).mod(mPaillier.nsquare))
							   .mod(mPaillier.nsquare);
		}		
		return s3_c.multiply(tmpRight).mod(mPaillier.nsquare);
	}
	
	public BigInteger[] getUniformRandomNumbers() {
		return this.mUniformRandomNumbers;
	}
	
	public void printAllNumber(BigInteger[] n) {
		for(int i=0; i<this.vectorLength; i++) {
			System.out.println(n[i]);
		}
	}		
}
