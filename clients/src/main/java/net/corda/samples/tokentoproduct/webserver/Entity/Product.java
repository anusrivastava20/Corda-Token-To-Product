package net.corda.samples.tokentoproduct.webserver.Entity;
import net.corda.core.contracts.Amount;
import net.corda.core.identity.Party;

import java.util.Currency;


public class Product {
    private final String holder;
    private final String valuation;
    private final int size;
    private final String type;
    private final String name;
    private final String companyName;
    public Product(String holder, String valuation, int size, String type, String name, String companyName) {
        this.holder = holder;
        this.valuation = valuation;
        this.size = size;
        this.type = type;
        this.name = name;
        this.companyName = companyName;
    }

    public String getHolder() {
        return holder;
    }

    public String getValuation() {
        return valuation;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCompanyName() {
        return companyName;
    }

}