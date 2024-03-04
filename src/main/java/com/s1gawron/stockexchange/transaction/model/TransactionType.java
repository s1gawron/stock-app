package com.s1gawron.stockexchange.transaction.model;

public enum TransactionType {
    PURCHASE,
    SELL;

    public boolean isPurchase() {
        return this.equals(PURCHASE);
    }

    public boolean isSell() {
        return this.equals(SELL);
    }
}
