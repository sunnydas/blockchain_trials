package com.sunny.blockchain.helloworld.wallet;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * This is the logical entity representing a wallet
 *
 * Created by sundas on 2/1/2018.
 */
public class Wallet {
  private PrivateKey privateKey;
  private PublicKey publicKey;

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public Wallet(){
    generateKeyPair();
  }

  private void generateKeyPair() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
      // Initialize the key generator and generate a KeyPair
      keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
      KeyPair keyPair = keyGen.generateKeyPair();
      // Set the public and private keys from the keyPair
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
    }catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
