package py.com.ucom.sipap.util;

import py.com.ucom.sipap.exception.TlvParseException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TlvUtils {
    private TlvUtils() {
    }

    public static String tlv(String tag, String value) {
        if (tag == null || tag.length() != 2) {
            throw new IllegalArgumentException("El tag debe tener exactamente 2 caracteres");
        }
        if (value == null) {
            value = "";
        }
        if (value.length() > 99) {
            throw new IllegalArgumentException("El valor supera la longitud TLV simplificada de 99 caracteres");
        }
        return tag + String.format("%02d", value.length()) + value;
    }

    public static Map<String, String> parse(String input) {
        if (input == null || input.isBlank()) {
            throw new TlvParseException("La cadena TLV está vacía");
        }

        Map<String, String> fields = new LinkedHashMap<>();
        int index = 0;

        while (index < input.length()) {
            if (input.length() - index < 4) {
                throw new TlvParseException("Cabecera TLV incompleta en posición " + index);
            }

            String tag = input.substring(index, index + 2);
            String lengthText = input.substring(index + 2, index + 4);

            if (!lengthText.chars().allMatch(Character::isDigit)) {
                throw new TlvParseException("Longitud TLV inválida para tag " + tag + ": " + lengthText);
            }

            int length = Integer.parseInt(lengthText);
            int valueStart = index + 4;
            int valueEnd = valueStart + length;

            if (valueEnd > input.length()) {
                throw new TlvParseException("Longitud declarada excede el contenido disponible para tag " + tag);
            }
            if (fields.containsKey(tag)) {
                throw new TlvParseException("Tag duplicado no soportado: " + tag);
            }

            fields.put(tag, input.substring(valueStart, valueEnd));
            index = valueEnd;
        }

        return fields;
    }
}
