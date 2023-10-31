package net.corda.samples.tokentoproduct.webserver;

import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.money.MoneyUtilities;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import java.util.*;
import com.r3.corda.lib.tokens.contracts.types.TokenType;


import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.tokentoproduct.flows.FiatCurrencyIssueFlow;
import net.corda.samples.tokentoproduct.flows.ProductSaleInitiatorFlow;
import net.corda.samples.tokentoproduct.flows.UtilityFlows;
import net.corda.samples.tokentoproduct.states.ProductState;
import net.corda.samples.tokentoproduct.webserver.Entity.AddCurrency;
import net.corda.samples.tokentoproduct.webserver.Entity.Product;
import net.corda.samples.tokentoproduct.flows.ProductTokenCreateAndIssueFlow;
import org.apache.logging.log4j.core.time.PreciseClock;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import java.util.stream.Collectors;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();

    }

    private boolean isMe(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isNetworkMap(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    /**
     * Helpers for filtering the network map cache.
     */
    public String toDisplayString(X500Name name) {
        return BCStyle.INSTANCE.toString(name);
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami() {
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @RequestMapping(value = "/createProductTokenAndIssue", method = RequestMethod.POST)
    public ResponseEntity<String> createProductTokenAndIssue(@RequestBody Product productInfo) {

        System.out.println(productInfo);
        System.out.println(productInfo.getCompanyName());
        System.out.println(productInfo.getType());
        System.out.println(productInfo.getName());
        System.out.println(productInfo.getSize());
        System.out.println(productInfo.getValuation());
        CordaX500Name partyX500Name = CordaX500Name.parse(productInfo.getHolder());
        System.out.println(partyX500Name.toString());
        Party holderParty = proxy.wellKnownPartyFromX500Name(partyX500Name);
        System.out.println(holderParty.toString());
        Amount<Currency> valuation = Amount.parseCurrency(productInfo.getValuation());
        System.out.println(valuation.getToken());


        try {
            String tokenStateResponse = proxy.startTrackedFlowDynamic(ProductTokenCreateAndIssueFlow.class, holderParty, valuation, productInfo.getSize(), productInfo.getType(), productInfo.getName(), productInfo.getCompanyName()).getReturnValue().get().toString();
            System.out.println("tokenStateId: "+tokenStateResponse);
            //String result = proxy.startTrackedFlowDynamic(ProductTokenCreateAndIssueFlow.class, tokenStateId).getReturnValue().get();
            //System.out.println("result"+result);
            return ResponseEntity.status(HttpStatus.CREATED).body(tokenStateResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(value = "get_product_state", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMyProduct(@RequestParam String product_id)
            throws ExecutionException, InterruptedException {
        String product_info = proxy.startTrackedFlowDynamic(UtilityFlows.QueryProductState.class,product_id ).getReturnValue().toCompletableFuture().toString();
        return ResponseEntity.ok(product_info);
    }

    @GetMapping(value = "get_current_balance", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getMyBalance(@RequestParam String tokenType) throws ExecutionException, InterruptedException {
        TokenType ty = new TokenType(tokenType,2);
        Amount<TokenType> amount = proxy.startTrackedFlowDynamic(UtilityFlows.TotalBalance.class,ty ).getReturnValue().get();
        System.out.println("amount: "+amount.getQuantity());
        System.out.println("amount: "+amount.getToken().toString());
        int itemCurrFractionDigits = amount.getToken().getFractionDigits();
        System.out.println("amount: "+amount.getToken().getTokenIdentifier());
        return ResponseEntity.ok((double) (amount.getQuantity()/Math.pow(10,itemCurrFractionDigits)));
    }

    @GetMapping(value = "buy_product",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> my_product_state_info(@RequestParam String productId, @RequestParam  String customer)
            throws ExecutionException, InterruptedException {
        CordaX500Name partyX500Name = CordaX500Name.parse(customer);
        System.out.println(partyX500Name.toString());
        Party product_customer = proxy.wellKnownPartyFromX500Name(partyX500Name);
        System.out.println(product_customer.toString());
        String productDetails = proxy.startTrackedFlowDynamic(ProductSaleInitiatorFlow.class,productId,product_customer).getReturnValue().get().toString();
        return ResponseEntity.ok(productDetails);
    }

    @PostMapping(value = "add_currency", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addCurrency(@RequestBody AddCurrency currency) {
        try {
            CordaX500Name partyX500Name = CordaX500Name.parse(currency.getRecipient());
            System.out.println(partyX500Name.toString());
            Party recipient = proxy.wellKnownPartyFromX500Name(partyX500Name);
            System.out.println(recipient.toString());
            String tokenStateId = proxy.startTrackedFlowDynamic(FiatCurrencyIssueFlow.class, currency.currency, currency.amount, recipient).getReturnValue().get().toString();
            return ResponseEntity.status(HttpStatus.CREATED).body(tokenStateId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
