package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.Transferencia;

import java.math.BigDecimal;

public class StaticAmountEnrichmentProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Transferencia transferencia = exchange.getMessage().getBody(Transferencia.class);
        BigDecimal requestAmount = exchange.getMessage().getHeader("requestAmount", BigDecimal.class);

        if (transferencia != null
                && "11".equals(transferencia.getPointOfInitiationMethod())
                && transferencia.getTransactionAmount() == null
                && requestAmount != null) {
            transferencia.setTransactionAmount(requestAmount);
        }
    }
}
