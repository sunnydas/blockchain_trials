package com.sunny.blockchain.helloworld.impl;

import com.google.gson.GsonBuilder;
import com.sunny.blockchain.helloworld.dataobjects.Block;
import com.sunny.blockchain.helloworld.transactions.Transaction;
import com.sunny.blockchain.helloworld.utils.SignatureUtility;
import com.sunny.blockchain.helloworld.wallet.Wallet;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundas on 2/1/2018.
 */
public class RudimentaryBlockChain {

  private List<Block> blockChain;

  /**
   * For proof of work
   */
  private int difficulty = 5;

  public RudimentaryBlockChain(){
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    this.blockChain = new ArrayList<Block>();
    Block genesisBlock = new Block("Genesis block","0");
    blockChain.add(genesisBlock);
  }

  /**
   *
   * @param data
   */
  public void addToBlockChain(String data){
    Block block = new Block(data,this.blockChain.get(blockChain.size() - 1).getHash());
    System.out.println("Adding data = " + data);
    System.out.println("Mining begins ... ");
    block.mineBlock(difficulty);
    System.out.println("Mining done ...");
    blockChain.add(block);
  }

  /**
   * print details of block chain
   */
  public void printBlockChainDetails(){
    String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
    System.out.println(blockchainJson);
  }

  /**
   * Is this a valid block chain
   *
   * @return
   */
  public boolean isValidChain(){
    boolean valid = true;
    if(blockChain.size() > 1) {
      String target = new String(new char[difficulty]).replace('\0', '0');
      for (int i = 1; i < blockChain.size(); i++) {
        Block currentBlock = blockChain.get(i);
        Block previousBlock = blockChain.get(i-1);
        // Is current hash correct
        if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
          System.err.println("[Error] Current Hash at index i = " + i + " does not match.");
          valid = false;
          break;
        }
        //Is previosu hash valid
        if(!currentBlock.getPreviousHash().equals(previousBlock.getHash())){
          System.err.println("[Error] Previous Hash at index i - 1  = " + (i - 1) + " does not match.");
          valid = false;
          break;
        }
        //Is current block already mined
        if(!currentBlock.getHash().substring(0,difficulty).equals(target)){
          System.err.println("[Error] Current block at index i = " + i + " does not seem to have been mined.");
          valid = false;
          break;
        }
      }
    }
    return valid;
  }

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    RudimentaryBlockChain rudimentaryBlockChain = new RudimentaryBlockChain();
    rudimentaryBlockChain.addToBlockChain("Adding block sunny 1");
    rudimentaryBlockChain.addToBlockChain("Adding block sunny 2");
    rudimentaryBlockChain.printBlockChainDetails();
    System.out.println(rudimentaryBlockChain.isValidChain());
    Wallet walletA = new Wallet();
    Wallet walletB = new Wallet();
    System.out.println("Private and public keys:");
    System.out.println(SignatureUtility.getStringFromKey(walletA.getPrivateKey()));
    System.out.println(SignatureUtility.getStringFromKey(walletA.getPublicKey()));
    //Create a test transaction from WalletA to walletB
    Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(), 5, null);
    transaction.setSignature(transaction.generateSignature(walletA.getPrivateKey()));
    //Verify the signature works and verify it from the public key
    System.out.println("Is signature verified");
    System.out.println(transaction.verifiySignature());
  }

}
