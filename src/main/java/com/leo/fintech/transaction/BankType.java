package com.leo.fintech.transaction;

public enum BankType {
    BANCO_DO_BRASIL("Banco do Brasil"),
    BANK_OF_AMERICA("Bank of America"),
    CHASE("Chase"),
    WELLS_FARGO("Wells Fargo");

    private final String displayName;

    BankType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
