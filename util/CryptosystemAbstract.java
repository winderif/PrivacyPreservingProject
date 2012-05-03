package util;
/**
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.math.*;
import java.util.*;

/**
 * Paillier Cryptosystem <br><br>
 * References: <br>
 * [1] Pascal Paillier, "Public-Key Cryptosystems Based on Composite Degree Residuosity Classes," EUROCRYPT'99.
 *    URL: <a href="http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf">http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf</a><br>
 * 
 * [2] Paillier cryptosystem from Wikipedia. 
 *    URL: <a href="http://en.wikipedia.org/wiki/Paillier_cryptosystem">http://en.wikipedia.org/wiki/Paillier_cryptosystem</a>
 * @author Kun Liu (kunliu1@cs.umbc.edu)
 * @version 1.0
 */
public abstract class CryptosystemAbstract {
    protected int bitLength;    
    protected static final double SCALAR = 1000.0;

    public void setBitLength(int length) { this.bitLength = length; }
    public abstract void KeyGeneration(int bitLengthVal, int certainty, BigInteger[] publicKey);       
    public abstract BigInteger Encryption(BigInteger m);
    public abstract BigInteger Decryption(BigInteger p);
    
    
    /**
     * Transform Double into BigInteger.
     * @param d double value as a double
     * @return bigInteger value as a BigInteger
     * 12.03.13 winderif
     */    
    public BigInteger DoubleToBigInteger(double d) {    
    	long tmp = Math.round(d * SCALAR);
    	return new BigInteger(Long.toString(tmp));
    }

    /**
     * Transform Double[] into BigInteger[].
     * @param d array of double value as a double[]
     * @return array of bigInteger value as a BigInteger[]
     * 12.03.13 winderif
     */
    public BigInteger[] DoubleToBigInteger1DArray(double d[]) {
    	BigInteger[] tmpBigInteger = new BigInteger[d.length];
    	for(int i=0; i<d.length; i++) {
    		tmpBigInteger[i] = DoubleToBigInteger(d[i]);
    	}
    	return tmpBigInteger;
    }
    
    /**    
     * Transform Double[][] into BigInteger[][].
     * @param d array of double value as a double[]
     * @return array of bigInteger value as a BigInteger[]
     * 12.03.13 winderif
     */
    public BigInteger[][] DoubleToBigInteger2DArray(double d[][]) {
    	BigInteger[][] tmpBigInteger = new BigInteger[d.length][d[0].length];
    	for(int i=0; i<d.length; i++) {    		
    		tmpBigInteger[i] = DoubleToBigInteger1DArray(d[i]);
    	}
    	return tmpBigInteger;
    }    
}
