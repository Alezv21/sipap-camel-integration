package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.Transferencia;
import py.com.ucom.sipap.exception.TransferValidationException;

import java.math.BigDecimal;

public class AmountLimitProcessor implements Processor {
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");

    @Override
    public void process(Exchange exchange) {
        Transferencia transferencia = exchange.getMessage().getBody(Transferencia.class);
        if (transferencia != null
                && transferencia.getTransactionAmount() != null
                && transferencia.getTransactionAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new TransferValidationException("El monto supera máximo permitido");
        }
    }
}
