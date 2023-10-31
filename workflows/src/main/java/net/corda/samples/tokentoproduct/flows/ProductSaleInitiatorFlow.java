package net.corda.samples.tokentoproduct.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.workflows.flows.move.MoveTokensUtilities;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.samples.tokentoproduct.states.ProductState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Initiator Flow class to propose the sale of the house. The house token would be exchanged with an equivalent amount of
 * fiat currency as mentioned in the valuation of the house. The flow takes the linearId of the house token and the buyer
 * party as the input parameters.
 */
@InitiatingFlow
@StartableByRPC
public class ProductSaleInitiatorFlow extends FlowLogic<String> {

    private final String productId;
    private final Party customer;

    public ProductSaleInitiatorFlow(String productId, Party customer) {
        this.productId = productId;
        this.customer = customer;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {

        // Obtain a reference to a notary we wish to use.
        /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
        final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));

        /* Get the UUID from the houseId parameter */
        UUID uuid = UUID.fromString(productId);

        /* Fetch the house state from the vault using the vault query */
        QueryCriteria.LinearStateQueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null, ImmutableList.of(uuid), null, Vault.StateStatus.UNCONSUMED);

        StateAndRef<ProductState> houseStateAndRef = getServiceHub().getVaultService().
                queryBy(ProductState.class).getStates().get(0);

        ProductState productState = houseStateAndRef.getState().getData();

        /* Build the transaction builder */
        TransactionBuilder txBuilder = new TransactionBuilder(notary);

        /* Create a move token proposal for the house token using the helper function provided by Token SDK. This would
        create the movement proposal and would be committed in the ledgers of parties once the transaction in finalized */
        MoveTokensUtilities.addMoveNonFungibleTokens(txBuilder, getServiceHub(), productState.toPointer(), customer);

        /* Initiate a flow session with the buyer to send the house valuation and transfer of the fiat currency */
        FlowSession customerSession = initiateFlow(customer);

        /* Send the house valuation to the buyer */
        customerSession.send(productState.getValuation());

        /* Receive inputStatesAndRef for the fiat currency exchange from the buyer, these would be inputs to the fiat currency exchange transaction */
        List<StateAndRef<FungibleToken>> inputs = subFlow(new ReceiveStateAndRefFlow<>(customerSession));

        /* Receive output for the fiat currency from the buyer, this would contain the transferred amount from buyer to yourself */
        List<FungibleToken> moneyReceived = customerSession.receive(List.class).unwrap(value -> value);

        /* Create a fiat currency proposal for the house token using the helper function provided by Token SDK */
        MoveTokensUtilities.addMoveTokens(txBuilder, inputs, moneyReceived);

        /* Sign the transaction */
        SignedTransaction initialSignedTrnx = getServiceHub().signInitialTransaction(txBuilder, getOurIdentity().getOwningKey());

        /* Call the CollectSignaturesFlow to receive signature of the buyer */
        SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(initialSignedTrnx, ImmutableList.of(customerSession)));

        /* Call finality flow to notarise the transaction */
        SignedTransaction stx = subFlow(new FinalityFlow(signedTransaction, ImmutableList.of(customerSession)));

        /* Distribution list is a list of identities that should receive updates. For this mechanism to behave correctly we call the UpdateDistributionListFlow flow */
        subFlow(new UpdateDistributionListFlow(stx));

        return "\nThe house is sold to "+ this.customer.getName().getOrganisation() + "\nTransaction ID: "
                + stx.getId();
    }
}
