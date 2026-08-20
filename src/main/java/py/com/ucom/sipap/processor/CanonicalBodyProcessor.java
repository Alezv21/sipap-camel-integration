package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.TransferMessage;

public class CanonicalBodyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        TransferMessage message = exchange.getMessage().getBody(TransferMessage.class);
        exchange.getMessage().setBody(message.getTransferencia());
    }
}
