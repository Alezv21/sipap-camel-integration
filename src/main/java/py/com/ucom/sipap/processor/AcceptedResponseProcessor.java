package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

public class AcceptedResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        exchange.getMessage().setBody(new ResultadoTransferencia(
                id, "ACEPTADA_PARA_PROCESAMIENTO", "Transferencia enviada a la cola"));
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 202);
    }
}
