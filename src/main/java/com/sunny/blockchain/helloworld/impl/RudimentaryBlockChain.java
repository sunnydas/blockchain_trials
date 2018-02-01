package com.sunny.blockchain.helloworld.impl;

import com.google.gson.GsonBuilder;
import com.sunny.blockchain.helloworld.dataobjects.Block;
import com.sunny.blockchain.helloworld.transactions.Transaction;
import com.sunny.blockchain.helloworld.transactions.TransactionInput;
import com.sunny.blockchain.helloworld.transactions.TransactionOutput;
import com.sunny.blockchain.helloworld.utils.SignatureUtility;
import com.sunny.blockchain.helloworld.wallet.Wallet;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sundas on 2/1/2018.
 */
public class RudimentaryBlockChain {

  private List<Block> blockChain;

  public static Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions.

  public static float minimumTransaction = 0.1f;

  /**
   * For proof of work
   */
  private int difficulty = 3;

  private Transaction genesisTransaction;

  public Wallet getWalletA() {
    return walletA;
  }

  public void setWalletA(Wallet walletA) {
    this.walletA = walletA;
  }

  public Wallet getWalletB() {
    return walletB;
  }

  public void setWalletB(Wallet walletB) {
    this.walletB = walletB;
  }

  private Wallet walletA;
  private Wallet walletB;

  public RudimentaryBlockChain(){
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    this.blockChain = new ArrayList<Block>();
    //Create wallets:
    walletA = new Wallet();
    walletB = new Wallet();
    Wallet coinbase = new Wallet();
    //create genesis transaction, which sends 100 coins to walletA:
    genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
    genesisTransaction.generateSignature(coinbase.getPrivateKey());	 //manually sign the genesis transaction
    genesisTransaction.setTransactionId("0"); //manually set the transaction id
    genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getReciepient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())); //manually add the Transactions Output
    UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0)); //its important to store our first transaction in the UTXOs list.
    Block genesisBlock = new Block("0");
    genesisBlock.addTransaction(genesisTransaction);
    genesisBlock.mineBlock(difficulty);
    blockChain.add(genesisBlock);
  }

  public void addToBlockChain(Transaction transaction){
    Block block = new Block(blockChain.get(blockChain.size() - 1).getHash());
    block.addTransaction(transaction);
    block.mineBlock(difficulty);
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
    HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
    tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

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
        //loop thru blockchains transactions:
        TransactionOutput tempOutput;
        for(int t=0; t <currentBlock.getTransactions().size(); t++) {
          Transaction currentTransaction = currentBlock.getTransactions().get(t);

          if(!currentTransaction.verifiySignature()) {
            System.out.println("#Signature on Transaction(" + t + ") is Invalid");
            return false;
          }
          if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
            System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
            return false;
          }

          for(TransactionInput input: currentTransaction.getInputs()) {
            tempOutput = tempUTXOs.get(input.getTransactionOutputId());

            if(tempOutput == null) {
              System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
              return false;
            }

            if(input.getUTXO().getValue() != tempOutput.getValue()) {
              System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
              return false;
            }

            tempUTXOs.remove(input.getTransactionOutputId());
          }

          for(TransactionOutput output: currentTransaction.getOutputs()) {
            tempUTXOs.put(output.getId(), output);
          }

          if( currentTransaction.getOutputs().get(0).getReciepient() != currentTransaction.getReciepient()) {
            System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
            return false;
          }
          if( currentTransaction.getOutputs().get(1).getReciepient() != currentTransaction.getSender()) {
            System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
            return false;
          }

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
    System.out.println("\nWalletA's balance is: " + rudimentaryBlockChain.getWalletA().getBalance());
    System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
    Transaction transaction = rudimentaryBlockChain.getWalletA().sendFunds(rudimentaryBlockChain.getWalletB().getPublicKey(), 40f);
    rudimentaryBlockChain.addToBlockChain(transaction);
    System.out.println("\nWalletA's balance is: " + rudimentaryBlockChain.getWalletA().getBalance());
    System.out.println("\nWalletB's balance is: " + rudimentaryBlockChain.getWalletB().getBalance());
    System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
    transaction = rudimentaryBlockChain.getWalletA().sendFunds(rudimentaryBlockChain.getWalletB().getPublicKey(), 100f);
    rudimentaryBlockChain.addToBlockChain(transaction);
    System.out.println("\nWalletA's balance is: " + rudimentaryBlockChain.getWalletA().getBalance());
    System.out.println("\nWalletB's balance is: " + rudimentaryBlockChain.getWalletB().getBalance());
    System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
    transaction = rudimentaryBlockChain.getWalletB().sendFunds(rudimentaryBlockChain.getWalletA().getPublicKey(), 20f);
    rudimentaryBlockChain.addToBlockChain(transaction);
    System.out.println("\nWalletA's balance is: " + rudimentaryBlockChain.getWalletA().getBalance());
    System.out.println("\nWalletB's balance is: " + rudimentaryBlockChain.getWalletB().getBalance());
    System.out.println(rudimentaryBlockChain.isValidChain());
    //rudimentaryBlockChain.printBlockChainDetails();
  }

}
