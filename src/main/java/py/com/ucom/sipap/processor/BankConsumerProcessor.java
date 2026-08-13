package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

public class BankConsumerProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(BankConsumerProcessor.class);
    private final String bankName;

    public BankConsumerProcessor(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public void process(Exchange exchange) {
        String transactionId = exchange.getMessage().getHeader("transactionId", String.class);
        String canonicalJson = exchange.getMessage().getBody(String.class);

        LOG.info("Consumidor {} recibió TX={} modelo canónico={}", bankName, transactionId, canonicalJson);

        exchange.getMessage().setBody(new ResultadoTransferencia(
                transactionId,
                "PROCESADA",
                "Transferencia procesada exitosamente por " + bankName
        ));
    }
}
