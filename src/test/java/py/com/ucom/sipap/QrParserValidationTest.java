package py.com.ucom.sipap;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import py.com.ucom.sipap.domain.Transferencia;
import py.com.ucom.sipap.exception.TransferValidationException;
import py.com.ucom.sipap.processor.QrParserProcessor;
import py.com.ucom.sipap.processor.TransferValidationProcessor;
import py.com.ucom.sipap.util.QrTestData;

import static org.junit.jupiter.api.Assertions.*;

class QrParserValidationTest {
    private final QrParserProcessor parser = new QrParserProcessor();
    private final TransferValidationProcessor validator = new TransferValidationProcessor();

    @Test
    void validItau() throws Exception {
        Exchange ex = exchange(QrTestData.validDynamic(QrTestData.ITAU, "1234567890", "15000"));
        parser.process(ex);
        validator.process(ex);
        Transferencia t = ex.getMessage().getBody(Transferencia.class);
        assertEquals("0015", t.getMerchantAccountInformation().getCodigoEntidad());
        assertEquals("ITAU", ex.getMessage().getHeader("bankName"));
    }

    @Test
    void validAtlas() throws Exception {
        Exchange ex = exchange(QrTestData.validDynamic(QrTestData.ATLAS, "1234567890", "25000"));
        parser.process(ex);
        validator.process(ex);
        assertEquals("ATLAS", ex.getMessage().getHeader("bankName"));
    }

    @Test
    void validFamiliar() throws Exception {
        Exchange ex = exchange(QrTestData.validDynamic(QrTestData.FAMILIAR, "1234567890", "35000"));
        parser.process(ex);
        validator.process(ex);
        assertEquals("FAMILIAR", ex.getMessage().getHeader("bankName"));
    }

    @Test
    void rejectUnknownBank() throws Exception {
        Exchange ex = exchange(QrTestData.validDynamic("9999", "1234567890", "15000"));
        parser.process(ex);
        assertThrows(TransferValidationException.class, () -> validator.process(ex));
    }

    @Test
    void rejectMissingRequiredMerchantName() throws Exception {
        String qr = QrTestData.build(QrTestData.ITAU, "1234567890", "15000", "12", "A1B2", "");
        Exchange ex = exchange(qr);
        parser.process(ex);
        assertThrows(TransferValidationException.class, () -> validator.process(ex));
    }

    @Test
    void rejectAmountGreaterOrEqualTenMillion() throws Exception {
        Exchange ex = exchange(QrTestData.validDynamic(QrTestData.ITAU, "1234567890", "10000000"));
        parser.process(ex);
        assertThrows(TransferValidationException.class, () -> validator.process(ex));
    }

    @Test
    void rejectBadChecksum() throws Exception {
        String qr = QrTestData.build(QrTestData.ITAU, "1234567890", "15000", "12", "FFFF", "JUAN PEREZ");
        Exchange ex = exchange(qr);
        parser.process(ex);
        assertThrows(TransferValidationException.class, () -> validator.process(ex));
    }

    @Test
    void rejectInvalidDeclaredLength() {
        Exchange ex = exchange(QrTestData.invalidDeclaredLength());
        assertThrows(py.com.ucom.sipap.exception.TlvParseException.class, () -> parser.process(ex));
    }

    @Test
    void validStaticWithoutAmount() throws Exception {
        Exchange ex = exchange(QrTestData.validStatic(QrTestData.ITAU, "1234567890"));
        parser.process(ex);
        validator.process(ex);
        Transferencia t = ex.getMessage().getBody(Transferencia.class);
        assertNull(t.getTransactionAmount());
    }

    private Exchange exchange(String body) {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setHeader("transactionId", "TXTEST001");
        exchange.getMessage().setBody(body);
        return exchange;
    }
}
