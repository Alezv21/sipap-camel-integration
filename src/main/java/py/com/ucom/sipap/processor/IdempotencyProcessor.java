package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.exception.DuplicateTransactionException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IdempotencyProcessor implements Processor {
    private static final Set<String> PROCESSED_IDS = ConcurrentHashMap.newKeySet();

    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        if (id == null || id.isBlank()) {
            return;
        }
        if (!PROCESSED_IDS.add(id)) {
            throw new DuplicateTransactionException("La transacción ya fue recibida anteriormente");
        }
    }

    public static void clearForTests() {
        PROCESSED_IDS.clear();
    }
}
