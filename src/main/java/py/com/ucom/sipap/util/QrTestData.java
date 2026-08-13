package py.com.ucom.sipap.util;

public final class QrTestData {
    public static final String ITAU = "0015";
    public static final String ATLAS = "0007";
    public static final String FAMILIAR = "0020";
    public static final String UNKNOWN_BANK = "9999";

    private QrTestData() {
    }

    public static String validDynamic(String bankCode, String account, String amount) {
        return build(bankCode, account, amount, "12", "A1B2", "JUAN PEREZ");
    }

    public static String validStatic(String bankCode, String account) {
        return build(bankCode, account, null, "11", "A1B2", "JUAN PEREZ");
    }

    public static String unknownBank() {
        return validDynamic(UNKNOWN_BANK, "5555555555", "45000");
    }

    public static String amountAtLimit() {
        return validDynamic(ITAU, "6666666666", "10000000");
    }

    public static String invalidChecksum() {
        return build(ITAU, "7777777777", "55000", "12", "FFFF", "JUAN PEREZ");
    }

    /**
     * Genera una cadena cuya longitud declarada para el tag 59 es imposible
     * de satisfacer. El parser debe rechazarla antes de llegar a un banco.
     */
    public static String invalidDeclaredLength() {
        String valid = validDynamic(ITAU, "8888888888", "35000");
        return valid.replace("5910JUAN PEREZ", "5999JUAN PEREZ");
    }

    public static String build(String bankCode,
                               String account,
                               String amount,
                               String pointOfInitiation,
                               String crc,
                               String merchantName) {
        String merchantAccount =
                TlvUtils.tlv("00", "py.gov.bcp.sip") +
                TlvUtils.tlv("01", bankCode) +
                TlvUtils.tlv("02", account);

        StringBuilder qr = new StringBuilder()
                .append(TlvUtils.tlv("00", "01"))
                .append(TlvUtils.tlv("01", pointOfInitiation))
                .append(TlvUtils.tlv("32", merchantAccount))
                .append(TlvUtils.tlv("52", "5731"))
                .append(TlvUtils.tlv("53", "600"));

        if (amount != null) {
            qr.append(TlvUtils.tlv("54", amount));
        }

        qr.append(TlvUtils.tlv("58", "PY"))
          .append(TlvUtils.tlv("59", merchantName))
          .append(TlvUtils.tlv("60", "ASUNCION"))
          .append(TlvUtils.tlv("63", crc));

        return qr.toString();
    }
}
