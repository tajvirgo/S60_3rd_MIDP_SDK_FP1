/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Codec handles the encryption, decryption and displays encrypted
 * data in hex format.
 */
class Codec
{
    private static final int blocksize = 16;
    private static final String hexCodes = "0123456789ABCDEF";
    private static final String decryptLabel = "AES/ECB/PKCS5Padding";
    private static final String secretKeySpec = "AES";
    /**
     * An instance of the SATSAMIDLET class
     */
    SATSAMIDlet midlet;

    /**
     * Creates a new codec object for this midlet.
     * @param midlet the {@link SATSAMIDlet} object
     */
    Codec(SATSAMIDlet midlet)
  {
    this.midlet = midlet;
  }

  /** Encrypts text with given AES key.*/
  public byte[] encrypt (byte[] keyBits, byte[] plaintext)
    throws GeneralSecurityException
  {
    Cipher cipher = Cipher.getInstance(decryptLabel);
    Key key = new SecretKeySpec(keyBits, 0, keyBits.length, secretKeySpec);
    int ciphertextLength = 0;
    int remainder = plaintext.length % blocksize;
    if (remainder == 0)
      ciphertextLength = plaintext.length;
    else
      ciphertextLength = plaintext.length - remainder + blocksize;
    byte[] cipherText = new byte[ciphertextLength];

    cipher.init(Cipher.ENCRYPT_MODE, key);
    cipher.doFinal(plaintext, 0, plaintext.length, cipherText, 0);
    return cipherText;
  }

  /** Decrypt text with given AES key.*/
  public byte[] decrypt (byte[] keyBits, byte[] cipherText)
    throws GeneralSecurityException
  {
    Cipher cipher = Cipher.getInstance(decryptLabel);
    Key key = new SecretKeySpec(keyBits, 0, keyBits.length, secretKeySpec);

    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] decrypted = new byte[cipherText.length];
    cipher.doFinal(cipherText, 0, cipherText.length, decrypted, 0);

    return decrypted;
  }

  /** Displays ecrypted data in hex.*/
  public static String byteToHex(byte[] data)
  {
    StringBuffer hexString = new StringBuffer();
    for (int i=0; i < data.length; i++)
    {
      hexString.append( hexCodes.charAt( (data[i] >> 4) & 0x0f) );
      hexString.append( hexCodes.charAt( data[i] & 0x0f) );
      if (i< data.length - 1)
      {
        hexString.append(":");
      }
      if ( ((i+1)%8) == 0)
        hexString.append("\n");
    }
    return hexString.toString();
  }
}
