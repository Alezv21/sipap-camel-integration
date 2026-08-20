package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

public class ApiRejectionProcessor implements Processor {
    private final String estado;
    private final int statusCode;

    public ApiRejectionProcessor(String estado, int statusCode) {
        this.estado = estado;
        this.statusCode = statusCode;
    }

    @Override
    public void process(Exchange exchange) {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        String message = cause == null ? "Solicitud rechazada" : cause.getMessage();
        exchange.getMessage().setBody(new ResultadoTransferencia(id, estado, message));
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
    }
}
