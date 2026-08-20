package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.MerchantAccountInformation;
import py.com.ucom.sipap.domain.Transferencia;
import py.com.ucom.sipap.exception.TransferValidationException;
import py.com.ucom.sipap.util.QrTestData;

import java.math.BigDecimal;
import java.util.Map;

public class TransferValidationProcessor implements Processor {
    private static final Map<String, String> BANKS = Map.of(
            QrTestData.ITAU, "ITAU",
            QrTestData.ATLAS, "ATLAS",
            QrTestData.FAMILIAR, "FAMILIAR"
    );

    @Override
    public void process(Exchange exchange) {
        Transferencia t = exchange.getMessage().getBody(Transferencia.class);

        require("01".equals(t.getPayloadFormatIndicator()), "Payload Format Indicator inválido; se esperaba 01");
        require("11".equals(t.getPointOfInitiationMethod()) || "12".equals(t.getPointOfInitiationMethod()),
                "Point of Initiation Method inválido; se esperaba 11 o 12");
        require(notBlank(t.getMerchantCategoryCode()), "Falta el Merchant Category Code (tag 52)");
        require("600".equals(t.getTransactionCurrency()), "Moneda inválida; se esperaba 600 (PYG)");
        require("PY".equals(t.getCountryCode()), "Country Code inválido; se esperaba PY");
        require(notBlank(t.getMerchantName()), "Falta Merchant Name (tag 59)");
        require(notBlank(t.getMerchantCity()), "Falta Merchant City (tag 60)");
        require("A1B2".equals(t.getCrc()), "Checksum inválido; se esperaba A1B2");

        MerchantAccountInformation mai = t.getMerchantAccountInformation();
        require(mai != null, "Falta Merchant Account Information (tags 32 a 45)");
        require("py.gov.bcp.sip".equals(mai.getGloballyUniqueIdentifier()),
                "Globally Unique Identifier inválido");
        require(notBlank(mai.getCodigoEntidad()), "Falta código de entidad en sub-tag 01");
        require(notBlank(mai.getNumeroCuenta()), "Falta número de cuenta en sub-tag 02");

        String bankName = BANKS.get(mai.getCodigoEntidad());
        require(bankName != null, "Banco destino desconocido: " + mai.getCodigoEntidad());

        if ("12".equals(t.getPointOfInitiationMethod())) {
            require(t.getTransactionAmount() != null, "El monto es obligatorio para QR dinámico");
        }

        if (t.getTransactionAmount() != null) {
            require(t.getTransactionAmount().compareTo(BigDecimal.ZERO) > 0, "El monto debe ser positivo");
        }

        exchange.getMessage().setHeader("bankCode", mai.getCodigoEntidad());
        exchange.getMessage().setHeader("bankName", bankName);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new TransferValidationException(message);
        }
    }
}
