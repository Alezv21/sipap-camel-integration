package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

public class RejectionProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(RejectionProcessor.class);

    @Override
    public void process(Exchange exchange) {
        String transactionId = exchange.getMessage().getHeader("transactionId", String.class);
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String reason = cause == null ? "Error desconocido" : cause.getMessage();

        LOG.warn("Transferencia rechazada TX={} motivo={}", transactionId, reason);
        exchange.getMessage().setBody(new ResultadoTransferencia(transactionId, "RECHAZADA", reason));
    }
}
