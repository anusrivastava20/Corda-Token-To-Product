package net.corda.samples.tokentoproduct.contracts;


import net.corda.samples.tokentoproduct.states.ProductState;
import net.corda.testing.node.MockServices;
import org.junit.Test;
public class StateTests {
    private final MockServices ledgerServices = new MockServices();

    //sample State tests
    @Test
    public void hasConstructionAreaFieldOfCorrectType() throws NoSuchFieldException {
        // Does the message field exist?
//        ProductState.class.getDeclaredField("constructionArea");
//        // Is the message field of the correct type?
//        assert(ProductState.class.getDeclaredField("constructionArea").getType().equals(String.class));
    }
}