package com.sunny.blockchain.helloworld.transactions;

/**
 * Created by sundas on 2/1/2018.
 */
public class TransactionInput {
  private String transactionOutputId; //Reference to TransactionOutputs -> transactionId

  public void setUTXO(TransactionOutput UTXO) {
    this.UTXO = UTXO;
  }

  private TransactionOutput UTXO; //Contains the Unspent transaction output

  public String getTransactionOutputId() {
    return transactionOutputId;
  }

  public TransactionOutput getUTXO() {
    return UTXO;
  }

  public TransactionInput(String transactionOutputId) {
    this.transactionOutputId = transactionOutputId;

  }
}
