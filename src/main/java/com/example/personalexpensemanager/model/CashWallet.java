package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/** Ví tiền mặt — rút/nạp đơn giản, không phí. */
public class CashWallet extends Wallet {

  public CashWallet() {
    super("Ví tiền mặt", 0);
  }

  public CashWallet(String name, double balance) {
    super(name, balance);
  }

  @Override
  public WalletType getWalletType() {
    return WalletType.CASH;
  }
}
