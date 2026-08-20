package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.TransferMessage;
import py.com.ucom.sipap.domain.Transferencia;

public class TransferMessageProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        String date = exchange.getMessage().getHeader("transactionDate", String.class);
        Transferencia transferencia = exchange.getMessage().getBody(Transferencia.class);
        exchange.getMessage().setBody(new TransferMessage(id, date, transferencia));
        exchange.getMessage().setHeader("JMSCorrelationID", id);
    }
}
