package com.sunny.blockchain.helloworld.dataobjects;

import com.sunny.blockchain.helloworld.transactions.Transaction;
import com.sunny.blockchain.helloworld.utils.SignatureUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents the logical entity - block
 *
 * Created by sundas on 2/1/2018.
 */
public class Block {

  private String merkleRoot;

    /*
    Main attributes
     */
    private String hash;
    private String previousHash;
  public List<Transaction> data = new ArrayList<Transaction>(); //our data will be a simple message.
    private long timeStamp;

  public void setHash(String hash) {
    this.hash = hash;
  }

  public void setPreviousHash(String previousHash) {
    this.previousHash = previousHash;
  }

  public List<Transaction> getTransactions() {
    return data;
  }

  @Override
  public String toString() {
    return "Block{" +
        "hash='" + hash + '\'' +
        ", previousHash='" + previousHash + '\'' +
        ", transactions=" + data +
        ", timeStamp=" + timeStamp +
        ", nonce=" + nonce +
        '}';
  }

  public void setTransactions(List<Transaction> transactions) {
    this.data = transactions;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public void setNonce(int nonce) {
    this.nonce = nonce;
  }


  public int getNonce() {

    return nonce;
  }

  private int nonce;

  public Block(String previousHash) {
    //this.data = data;
    this.previousHash = previousHash;
    this.timeStamp = new Date().getTime();
    //Hash calculation should come at the end
    this.hash = calculateHash();
  }

  /**
   *
   * @return
   */
  public String calculateHash() {
    String calculatedhash = SignatureUtility.applySha256(
        previousHash +
            Long.toString(timeStamp) +
             Integer.toString(nonce)  + merkleRoot
    );
    return calculatedhash;
  }


  public String getHash() {
    return hash;
  }

  public String getPreviousHash() {
    return previousHash;
  }

  public long getTimeStamp() {
    return timeStamp;
  }


  /**
   * Very important critical, this is the proof of work logic.
   * Essentially we need to generate a hash with the same number of zeros
   * as the difficulty.
   *
   * @param difficulty
   */
  public void mineBlock(int difficulty) {
    merkleRoot = SignatureUtility.getMerkleRoot(data);
    String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
    while(!hash.substring( 0, difficulty).equals(target)) {
      //The change is nonce affects the hash and hopefully we achive the number of zeroes required
      nonce ++;
      hash = calculateHash();
      //System.out.println(hash);
    }
    System.out.println("Block Mined!!! : " + hash);
  }

  //Add transactions to this block
  public boolean addTransaction(Transaction transaction) {
    //process transaction and check if valid, unless block is genesis block then ignore.
    if(transaction == null) return false;
    if((previousHash != "0")) {
      if((transaction.processTransaction() != true)) {
        System.out.println("Transaction failed to process. Discarded.");
        return false;
      }
    }
    data.add(transaction);
    System.out.println("Transaction Successfully added to Block");
    return true;
  }

}
