package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/** Ví tiền mặt — rút/nạp đơn giản, không phí. */
public class CashWallet extends Wallet {

  public CashWallet() {
    // TODO
  }

  public CashWallet(String name, double balance) {
    // TODO
  }

  @Override
  public WalletType getWalletType() {
    // TODO: return WalletType.CASH
    return null;
  }
}
