package py.com.ucom.sipap;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import py.com.ucom.sipap.domain.ResultadoTransferencia;
import py.com.ucom.sipap.domain.Transferencia;
import py.com.ucom.sipap.exception.DuplicateTransactionException;
import py.com.ucom.sipap.processor.DateValidationProcessor;
import py.com.ucom.sipap.processor.IdempotencyProcessor;
import py.com.ucom.sipap.processor.StaticAmountEnrichmentProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Task2ProcessorsTest {
    private final IdempotencyProcessor idempotency = new IdempotencyProcessor();

    @BeforeEach
    void resetIdempotency() {
        IdempotencyProcessor.clearForTests();
    }

    @Test
    void duplicatedTransactionIsRejected() throws Exception {
        Exchange first = exchange();
        first.getMessage().setHeader("transactionId", "TX-DUP-001");
        idempotency.process(first);

        Exchange duplicate = exchange();
        duplicate.getMessage().setHeader("transactionId", "TX-DUP-001");
        assertThrows(DuplicateTransactionException.class, () -> idempotency.process(duplicate));
    }

    @Test
    void currentDateIsAcceptedUsingAsuncionTimezone() throws Exception {
        Exchange exchange = exchange();
        exchange.getMessage().setHeader("transactionId", "TX-DATE-OK");
        exchange.getMessage().setHeader("transactionDate", LocalDate.now(DateValidationProcessor.APP_ZONE).toString());
        new DateValidationProcessor().process(exchange);
        assertEquals(Boolean.TRUE, exchange.getMessage().getHeader("dateValid"));
    }

    @Test
    void pastDateIsRejected() throws Exception {
        Exchange exchange = exchange();
        exchange.getMessage().setHeader("transactionId", "TX-DATE-OLD");
        exchange.getMessage().setHeader("transactionDate", "2020-01-01");
        new DateValidationProcessor().process(exchange);
        assertEquals(Boolean.FALSE, exchange.getMessage().getHeader("dateValid"));
        ResultadoTransferencia result = exchange.getMessage().getBody(ResultadoTransferencia.class);
        assertEquals("RECHAZADA_FECHA", result.getEstado());
    }

    @Test
    void staticQrCanUseAmountFromRestRequest() throws Exception {
        Exchange exchange = exchange();
        Transferencia transferencia = new Transferencia();
        transferencia.setPointOfInitiationMethod("11");
        exchange.getMessage().setBody(transferencia);
        exchange.getMessage().setHeader("requestAmount", new BigDecimal("50000"));

        new StaticAmountEnrichmentProcessor().process(exchange);
        assertEquals(new BigDecimal("50000"), transferencia.getTransactionAmount());
    }

    private Exchange exchange() {
        return new DefaultExchange(new DefaultCamelContext());
    }
}
