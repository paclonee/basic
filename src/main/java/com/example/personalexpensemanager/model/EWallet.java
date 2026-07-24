package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/** Ví điện tử (MoMo, ZaloPay, ...). */
public class EWallet extends Wallet {

  private String provider;

  public EWallet() {
    // TODO
  }

  public EWallet(String name, double balance, String provider) {
    // TODO
  }

  @Override
  public WalletType getWalletType() {
    // TODO: return WalletType.EWALLET
    return null;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    // TODO
  }
}
