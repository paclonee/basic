package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/** Ví điện tử (MoMo, ZaloPay, ...). */
public class EWallet extends Wallet {

  private String provider;

  public EWallet() {
    super("Ví điện tử", 0);
  }

  public EWallet(String name, double balance, String provider) {
    super(name, balance);
    this.provider = provider;
  }

  @Override
  public WalletType getWalletType() {
    return WalletType.EWALLET;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }
}
