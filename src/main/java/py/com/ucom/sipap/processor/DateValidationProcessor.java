package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.ResultadoTransferencia;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class DateValidationProcessor implements Processor {
    public static final ZoneId APP_ZONE = ZoneId.of("America/Asuncion");

    @Override
    public void process(Exchange exchange) {
        String id = exchange.getMessage().getHeader("transactionId", String.class);
        String value = exchange.getMessage().getHeader("transactionDate", String.class);
        boolean valid;
        String reason = null;

        try {
            LocalDate received = LocalDate.parse(value);
            LocalDate today = LocalDate.now(APP_ZONE);
            valid = received.equals(today);
            if (!valid) {
                reason = "La fecha_transaccion no coincide con la fecha actual " + today;
            }
        } catch (DateTimeParseException | NullPointerException ex) {
            valid = false;
            reason = "fecha_transaccion inválida; se esperaba formato yyyy-MM-dd";
        }

        exchange.getMessage().setHeader("dateValid", valid);
        if (!valid) {
            exchange.getMessage().setBody(new ResultadoTransferencia(id, "RECHAZADA_FECHA", reason));
        }
    }
}
