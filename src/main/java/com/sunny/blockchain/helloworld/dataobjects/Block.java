package com.sunny.blockchain.helloworld.dataobjects;

import com.sunny.blockchain.helloworld.utils.SignatureUtility;

import java.util.Date;

/**
 * This class represents the logical entity - block
 *
 * Created by sundas on 2/1/2018.
 */
public class Block {

    /*
    Main attributes
     */
    private String hash;
    private String previousHash;
    private String data;
    private long timeStamp;

  @Override
  public String toString() {
    return "Block{" +
        "hash='" + hash + '\'' +
        ", previousHash='" + previousHash + '\'' +
        ", data='" + data + '\'' +
        ", timeStamp=" + timeStamp +
        ", nonce=" + nonce +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Block block = (Block) o;

    if (getTimeStamp() != block.getTimeStamp()) return false;
    if (getNonce() != block.getNonce()) return false;
    if (getHash() != null ? !getHash().equals(block.getHash()) : block.getHash() != null) return false;
    if (getPreviousHash() != null ? !getPreviousHash().equals(block.getPreviousHash()) : block.getPreviousHash() != null)
      return false;
    return !(getData() != null ? !getData().equals(block.getData()) : block.getData() != null);

  }

  @Override
  public int hashCode() {
    int result = getHash() != null ? getHash().hashCode() : 0;
    result = 31 * result + (getPreviousHash() != null ? getPreviousHash().hashCode() : 0);
    result = 31 * result + (getData() != null ? getData().hashCode() : 0);
    result = 31 * result + (int) (getTimeStamp() ^ (getTimeStamp() >>> 32));
    result = 31 * result + getNonce();
    return result;
  }

  public int getNonce() {

    return nonce;
  }

  private int nonce;

  public Block(String data,String previousHash) {
    this.data = data;
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
            data + nonce
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


  public String getData() {
    return data;
  }

  /**
   * Very important critical, this is the proof of work logic.
   * Essentially we need to generate a hash with the same number of zeros
   * as the difficulty.
   *
   * @param difficulty
   */
  public void mineBlock(int difficulty) {
    String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
    while(!hash.substring( 0, difficulty).equals(target)) {
      //The change is nonce affects the hash and hopefully we achive the number of zeroes required
      nonce ++;
      hash = calculateHash();
      //System.out.println(hash);
    }
    System.out.println("Block Mined!!! : " + hash);
  }

}
