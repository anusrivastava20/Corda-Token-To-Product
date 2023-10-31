package net.corda.samples.tokentoproduct.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.workflows.utilities.NonFungibleTokenBuilder;
import net.corda.core.identity.CordaX500Name;
import net.corda.samples.tokentoproduct.states.ProductState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.TransactionState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.Currency;
import java.util.UUID;

/**
 * Flow to create and issue house tokens. Token SDK provides some in-built flows which could be called to Create and Issue tokens.
 * This flow should be called by the issuer of the token. The constructor takes the holder and other properties of the house as
 * input parameters, it first creates the house token onto the issuer's ledger and then issues it to the holder.
*/
@StartableByRPC
public class ProductTokenCreateAndIssueFlow extends FlowLogic<String> {

    private final Party holder;
    private final Amount<Currency> valuation;
    private final int size;
    private final String type;
    private final String name;
    private final String companyName;

    public ProductTokenCreateAndIssueFlow(Party holder, Amount<Currency> valuation,
                                          int size, String type, String name, String companyName) {
        this.holder = holder;
        this.valuation = valuation;
        this.size = size;
        this.type = type;
        this.name= name;
        this.companyName = companyName;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {

        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

        /* Get a reference of own identity */
        Party issuer = getOurIdentity();

        /* Construct the output state */
        UniqueIdentifier uuid = UniqueIdentifier.Companion.fromString(UUID.randomUUID().toString());
        final ProductState productState = new ProductState(uuid, ImmutableList.of(issuer),
                valuation,size,type,name,companyName);

        /* Create an instance of TransactionState using the houseState token and the notary */
        TransactionState<ProductState> transactionState = new TransactionState<>(productState, notary);

        /* Create the house token. Token SDK provides the CreateEvolvableTokens flow which could be called to create an
        evolvable token in the ledger.*/
        subFlow(new CreateEvolvableTokens(transactionState));

        /* Create an instance of the non-fungible house token with the holder as the token holder.
        * Notice the TokenPointer is used as the TokenType, since EvolvableTokenType is not TokenType, but is
        * a LinearState. This is done to separate the state info from the token so that the state can evolve independently.
        * */
        NonFungibleToken productToken = new NonFungibleTokenBuilder()
                .ofTokenType(productState.toPointer())
                .issuedBy(issuer)
                .heldBy(holder)
                .buildNonFungibleToken();

        /* Issue the house token by calling the IssueTokens flow provided with the TokenSDK */
        SignedTransaction stx = subFlow(new IssueTokens(ImmutableList.of(productToken)));
        return "\nThe non-fungible house token is created with UUID: "+ uuid +". (This is what you will use in next step)"
                +"\nTransaction ID: "+stx.getId();

    }
}
