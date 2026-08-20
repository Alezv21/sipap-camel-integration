package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpConstants;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

public class BankResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        String bank = exchange.getMessage().getHeader("bankName", String.class);
        Integer status = exchange.getMessage().getHeader(HttpConstants.HTTP_RESPONSE_CODE, Integer.class);
        String raw = exchange.getMessage().getBody(String.class);

        if (status != null && status >= 200 && status < 300) {
            exchange.getMessage().setBody(new ResultadoTransferencia(
                    id, "PROCESADA", "Transferencia procesada exitosamente por " + bank));
        } else {
            String detail = (raw == null || raw.isBlank()) ? "Sin detalle" : raw.replaceAll("\\s+", " ").trim();
            exchange.getMessage().setBody(new ResultadoTransferencia(
                    id, "ERROR_BANCO", "Banco mock rechazó la transferencia (HTTP " + status + "): " + detail));
        }
    }
}
