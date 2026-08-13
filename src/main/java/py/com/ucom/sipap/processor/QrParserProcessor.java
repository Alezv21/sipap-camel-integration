package py.com.ucom.sipap.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import py.com.ucom.sipap.domain.MerchantAccountInformation;
import py.com.ucom.sipap.domain.Transferencia;
import py.com.ucom.sipap.exception.TlvParseException;
import py.com.ucom.sipap.util.TlvUtils;

import java.math.BigDecimal;
import java.util.Map;

public class QrParserProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String qr = exchange.getMessage().getBody(String.class);
        Map<String, String> fields = TlvUtils.parse(qr);

        String merchantTag = fields.keySet().stream()
                .filter(tag -> isMerchantAccountTag(tag))
                .findFirst()
                .orElse(null);

        Transferencia transferencia = new Transferencia();
        transferencia.setPayloadFormatIndicator(fields.get("00"));
        transferencia.setPointOfInitiationMethod(fields.get("01"));
        transferencia.setMerchantCategoryCode(fields.get("52"));
        transferencia.setTransactionCurrency(fields.get("53"));
        transferencia.setCountryCode(fields.get("58"));
        transferencia.setMerchantName(fields.get("59"));
        transferencia.setMerchantCity(fields.get("60"));
        transferencia.setCrc(fields.get("63"));

        String amount = fields.get("54");
        if (amount != null && !amount.isBlank()) {
            try {
                transferencia.setTransactionAmount(new BigDecimal(amount));
            } catch (NumberFormatException ex) {
                throw new TlvParseException("El monto del tag 54 no es numérico");
            }
        }

        if (merchantTag != null) {
            Map<String, String> merchantFields = TlvUtils.parse(fields.get(merchantTag));
            MerchantAccountInformation merchant = new MerchantAccountInformation();
            merchant.setGloballyUniqueIdentifier(merchantFields.get("00"));
            merchant.setCodigoEntidad(merchantFields.get("01"));
            merchant.setNumeroCuenta(merchantFields.get("02"));
            transferencia.setMerchantAccountInformation(merchant);
        }

        exchange.getMessage().setBody(transferencia);
    }

    private boolean isMerchantAccountTag(String tag) {
        try {
            int value = Integer.parseInt(tag);
            return value >= 32 && value <= 45;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
