package net.corda.samples.tokentoproduct.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilities;
import com.r3.corda.lib.tokens.money.DigitalCurrency;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.selection.TokenQueryBy;
import com.r3.corda.lib.tokens.selection.api.Selector;
import com.r3.corda.lib.tokens.selection.database.selector.DatabaseTokenSelection;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.tokentoproduct.states.ProductState;

import java.util.*;
import java.util.stream.Collectors;

public interface UtilityFlows {

    /**
     * LooseChangeFinder
     * returns all tokens of a certain TokenType which has fractional values
     * This demonstrates how lambda predicates can be passed to TokenQueryBY in order to have
     * very precise filtering with your chosen token selector
     */

    @StartableByRPC
    class TotalBalance extends FlowLogic<Amount<TokenType>> {

        private final TokenType tokenType;

        /**
         * LooseChangeFinder
         * @param tokenType - the TokenType you would like to find loose change for
         */

        public TotalBalance(TokenType tokenType) {
            this.tokenType = tokenType;
        }

        @Override
        public Amount<TokenType> call() throws FlowException {
            Amount<TokenType> totalAmountHeld = QueryUtilities.tokenBalance(getServiceHub().getVaultService(), tokenType);
//            TokenQueryBy tokenQueryBy = new TokenQueryBy(
//                    null,
//                    it -> it.getState().getData().getAmount().toDecimal().stripTrailingZeros().scale() > 0
//            );
            //Selector selector = new DatabaseTokenSelection(getServiceHub());
            //return selector.selectTokens(totalAmountHeld, tokenQueryBy);
            return totalAmountHeld;
        }
    }
    @StartableByRPC
    class QueryProductState extends FlowLogic<String> {

        private final String productId;

        public QueryProductState(String productId) {
            this.productId = productId;
        }

        @Override
        public String call() throws FlowException {
            QueryCriteria inputCriteria = new QueryCriteria.LinearStateQueryCriteria().withUuid(Arrays.asList(UUID.fromString(productId)))
                    .withStatus(Vault.StateStatus.UNCONSUMED);
            return getServiceHub().getVaultService().queryBy(ProductState.class,inputCriteria).getStates().get(0).getState().getData().toString();
        }
    }
}
