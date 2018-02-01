package com.sunny.blockchain.helloworld.transactions;

import com.sunny.blockchain.helloworld.impl.RudimentaryBlockChain;
import com.sunny.blockchain.helloworld.utils.SignatureUtility;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represnts a transaction as per:
 * https://medium.com/programmers-blockchain/creating-your-first-blockchain-with-java-part-2-transactions-2cdac335e0ce
 *
 * Created by sundas on 2/1/2018.
 */
public class Transaction {

  private String transactionId; // this is also the hash of the transaction.
  private PublicKey sender; // senders address/public key.
  private PublicKey reciepient; // Recipients address/public key.
  private float value;
  private byte[] signature; // this is to prevent anybody else from spending funds in our wallet.

  public String getTransactionId() {
    return transactionId;
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "transactionId='" + transactionId + '\'' +
        ", sender=" + sender +
        ", reciepient=" + reciepient +
        ", value=" + value +
        ", signature=" + Arrays.toString(signature) +
        ", inputs=" + inputs +
        ", outputs=" + outputs +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Transaction that = (Transaction) o;

    if (Float.compare(that.getValue(), getValue()) != 0) return false;
    if (getTransactionId() != null ? !getTransactionId().equals(that.getTransactionId()) : that.getTransactionId() != null)
      return false;
    if (getSender() != null ? !getSender().equals(that.getSender()) : that.getSender() != null) return false;
    if (getReciepient() != null ? !getReciepient().equals(that.getReciepient()) : that.getReciepient() != null)
      return false;
    if (!Arrays.equals(getSignature(), that.getSignature())) return false;
    if (getInputs() != null ? !getInputs().equals(that.getInputs()) : that.getInputs() != null) return false;
    return !(getOutputs() != null ? !getOutputs().equals(that.getOutputs()) : that.getOutputs() != null);

  }

  @Override
  public int hashCode() {
    int result = getTransactionId() != null ? getTransactionId().hashCode() : 0;
    result = 31 * result + (getSender() != null ? getSender().hashCode() : 0);
    result = 31 * result + (getReciepient() != null ? getReciepient().hashCode() : 0);
    result = 31 * result + (getValue() != +0.0f ? Float.floatToIntBits(getValue()) : 0);
    result = 31 * result + (getSignature() != null ? Arrays.hashCode(getSignature()) : 0);
    result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
    result = 31 * result + (getOutputs() != null ? getOutputs().hashCode() : 0);
    return result;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public PublicKey getSender() {
    return sender;
  }

  public void setSender(PublicKey sender) {
    this.sender = sender;
  }

  public PublicKey getReciepient() {
    return reciepient;
  }

  public void setReciepient(PublicKey reciepient) {
    this.reciepient = reciepient;
  }

  public float getValue() {
    return value;
  }

  public void setValue(float value) {
    this.value = value;
  }

  public byte[] getSignature() {
    return signature;
  }

  public void setSignature(byte[] signature) {
    this.signature = signature;
  }

  public List<TransactionInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<TransactionInput> inputs) {
    this.inputs = inputs;
  }

  public List<TransactionOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TransactionOutput> outputs) {
    this.outputs = outputs;
  }

  private List<TransactionInput> inputs = new ArrayList<TransactionInput>();
  private List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

  public Transaction(PublicKey sender, PublicKey reciepient, float value,List<TransactionInput> inputs) {
    this.sender = sender;
    this.reciepient = reciepient;
    this.value = value;
    this.signature = signature;
    this.inputs = inputs;
    this.outputs = outputs;
  }
  private static int sequence = 0; //A rough count of how many transactions have been generated

  //Signs all the data we dont wish to be tampered with.
  public byte[] generateSignature(PrivateKey privateKey) {
    String data = SignatureUtility.getStringFromKey(sender) + SignatureUtility.getStringFromKey(reciepient) + Float.toString(value)	;
    signature = SignatureUtility.applyECDSASig(privateKey,data);
    return signature;
  }
  //Verifies the data we signed hasnt been tampered with
  public boolean verifiySignature() {
    String data = SignatureUtility.getStringFromKey(sender) + SignatureUtility.getStringFromKey(reciepient) + Float.toString(value)	;
    return SignatureUtility.verifyECDSASig(sender, data, signature);
  }

  private String calulateHash() {
    sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
    return SignatureUtility.applySha256(
        SignatureUtility.getStringFromKey(sender) +
            SignatureUtility.getStringFromKey(reciepient) +
            Float.toString(value) + sequence
    );
  }

  //Returns true if new transaction could be created.
  public boolean processTransaction() {

    if(verifiySignature() == false) {
      System.out.println("#Transaction Signature failed to verify");
      return false;
    }

    //gather transaction inputs (Make sure they are unspent):
    for(TransactionInput i : inputs) {
      i.setUTXO(RudimentaryBlockChain.UTXOs.get(i.getTransactionOutputId()));
    }

    //check if transaction is valid:
    if(getInputsValue() < RudimentaryBlockChain.minimumTransaction) {
      System.out.println("#Transaction Inputs to small: " + getInputsValue());
      return false;
    }

    //generate transaction outputs:
    float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
    transactionId = calulateHash();
    outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
    outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

    //add outputs to Unspent list
    for(TransactionOutput o : outputs) {
      RudimentaryBlockChain.UTXOs.put(o.getId() , o);
    }

    //remove transaction inputs from UTXO lists as spent:
    for(TransactionInput i : inputs) {
      if(i.getUTXO() == null) continue; //if Transaction can't be found skip it
      RudimentaryBlockChain.UTXOs.remove(i.getUTXO().getId());
    }

    return true;
  }

  //returns sum of inputs(UTXOs) values
  public float getInputsValue() {
    float total = 0;
    for(TransactionInput i : inputs) {
      if(i.getUTXO() == null) continue; //if Transaction can't be found skip it
      total += i.getUTXO().getValue();
    }
    return total;
  }

  //returns sum of outputs:
  public float getOutputsValue() {
    float total = 0;
    for(TransactionOutput o : outputs) {
      total += o.getValue();
    }
    return total;
  }

}
