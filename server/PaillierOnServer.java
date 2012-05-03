package server;
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
import util.CryptosystemAbstract;

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
public class PaillierOnServer extends CryptosystemAbstract {
    
    /**
     * n = p*q, where p and q are two large primes.
     */
    public BigInteger n;
    /**
     * nsquare = n*n
     */
    public BigInteger nsquare;
    /**
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
     */
    private BigInteger g;
    /**
     * number of bits of modulus
     */    
    private static final BigInteger TWO = new BigInteger("2"); 
    
    /**
     * Constructs an instance of the Paillier cryptosystem with 512 bits of modulus and at least 1-2^(-64) certainty of primes generation.
     */
    public PaillierOnServer() {
        bitLength = 512;
        this.n = null;
        this.nsquare = null;
        this.g = null;
    }
    
    public PaillierOnServer(BigInteger[] pkey) {
        KeyGeneration(512, 64, pkey);
    }
    
    public void KeyGeneration(int bitLengthVal, int certainty, BigInteger[] publicKey) {
        bitLength = bitLengthVal;
        /*Constructs two randomly generated positive BigIntegers that are probably prime, with the specified bitLength and certainty.*/        
        n = publicKey[0];
        nsquare = n.multiply(n);
        g = publicKey[1]; 	       
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function explicitly requires random input r to help with encryption.
     * @param m plaintext as a BigInteger
     * @param r random plaintext to help with encryption
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m, BigInteger r) {
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
    }

    /**
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function automatically generates random input r (to help with encryption).
     * @param m plaintext as a BigInteger
     * @return ciphertext as a BigInteger
     */
    public BigInteger Encryption(BigInteger m) {
        BigInteger r = new BigInteger(bitLength, new Random());
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
    }
    
    public BigInteger Decryption(BigInteger p) {
    	return p;
    }	
    
    /**
     * XOR in encrypted domain
     * @param EncA
     * @param EncB
     * @param B
     * @return
     * 12.03.29 winderif
     */
    public BigInteger EncXOR(BigInteger EncA, BigInteger EncB, BigInteger B) {
    	// [w] = [a + b - 2ab] = [a]*[b]*[a]^(-2b)
    	return EncA.multiply(EncB).multiply(EncA.modPow(B.multiply(TWO).negate(), nsquare));
    }
}
