package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.TransferRequest;
import py.com.ucom.sipap.exception.TransferValidationException;

public class RequestMetadataProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        TransferRequest request = exchange.getMessage().getBody(TransferRequest.class);
        if (request == null) {
            throw new TransferValidationException("Cuerpo JSON requerido");
        }
        require(request.getIdTransaccion() != null && !request.getIdTransaccion().isBlank(),
                "Falta id_transaccion");
        require(request.getFechaTransaccion() != null && !request.getFechaTransaccion().isBlank(),
                "Falta fecha_transaccion");
        require(request.getQr() != null && !request.getQr().isBlank(),
                "Falta qr");

        exchange.getMessage().setHeader("transactionId", request.getIdTransaccion());
        exchange.getMessage().setHeader("transactionDate", request.getFechaTransaccion());
        exchange.getMessage().setHeader("requestAmount", request.getMonto());
        exchange.getMessage().setBody(request.getQr());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new TransferValidationException(message);
        }
    }
}
