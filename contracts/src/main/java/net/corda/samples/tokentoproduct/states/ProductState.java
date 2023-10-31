package net.corda.samples.tokentoproduct.states;

import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.samples.tokentoproduct.contracts.ProductContract;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;
import java.util.List;

@BelongsToContract(ProductContract.class)
public class ProductState extends EvolvableTokenType {

    private final UniqueIdentifier linearId;
    private final List<Party> maintainers;
    private final Party issuer;
    private final int fractionDigits = 0;

    //Properties of House State. Some of these values may evolve over time.
    private final Amount<Currency> valuation;
    private final int size;
    private final String type;
    private final String name;
    private final String companyName;

    public ProductState(UniqueIdentifier linearId, List<Party> maintainers, Amount<Currency> valuation, int size, String type, String name, String companyName) {
        this.linearId = linearId;
        this.maintainers = maintainers;
        this.valuation = valuation;
        this.issuer = maintainers.get(0);
        this.size = size;
        this.type = type;
        this.name = name;
        this.companyName = companyName;
    }



    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public Amount<Currency> getValuation() {
        return valuation;
    }

    public Party getIssuer() {
        return issuer;
    }

    @Override
    public int getFractionDigits() {
        return fractionDigits;
    }

    @NotNull
    @Override
    public List<Party> getMaintainers() {
        return ImmutableList.copyOf(maintainers);
    }

    /* This method returns a TokenPointer by using the linear Id of the evolvable state */
    public TokenPointer<ProductState> toPointer(){
        LinearPointer<ProductState> linearPointer = new LinearPointer<>(linearId, ProductState.class);
        return new TokenPointer<>(linearPointer, fractionDigits);
    }
}