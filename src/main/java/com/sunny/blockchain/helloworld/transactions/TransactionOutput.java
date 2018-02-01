package com.sunny.blockchain.helloworld.transactions;

import com.sunny.blockchain.helloworld.utils.SignatureUtility;

import java.security.PublicKey;

/**
 * Created by sundas on 2/1/2018.
 */
public class TransactionOutput {
  private String id;

  public String getId() {
    return id;
  }

  public PublicKey getReciepient() {
    return reciepient;
  }

  public float getValue() {
    return value;
  }

  public String getParentTransactionId() {
    return parentTransactionId;
  }

  private PublicKey reciepient; //also known as the new owner of these coins.
  private float value; //the amount of coins they own
  private String parentTransactionId; //the id of the transaction this output was created in

  //Constructor
  public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
    this.reciepient = reciepient;
    this.value = value;
    this.parentTransactionId = parentTransactionId;
    this.id = SignatureUtility.applySha256(SignatureUtility.getStringFromKey(reciepient) + Float.toString(value) + parentTransactionId);
  }

  //Check if coin belongs to you
  public boolean isMine(PublicKey publicKey) {
    return (publicKey == reciepient);
  }
}
