package net.corda.samples.tokentoproduct.webserver.Entity;

import net.corda.core.identity.Party;

public class AddCurrency {

    public  String currency;
    public  Long amount;
    public  String recipient;
    public AddCurrency(String currency, Long amount, String recipient) {
        this.currency = currency;
        this.amount = amount;
        this.recipient = recipient;
    }
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

}
