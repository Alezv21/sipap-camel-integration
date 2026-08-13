package py.com.ucom.sipap;

import org.junit.jupiter.api.Test;
import py.com.ucom.sipap.exception.TlvParseException;
import py.com.ucom.sipap.util.TlvUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TlvUtilsTest {
    @Test
    void shouldBuildAndParseTlv() {
        String data = TlvUtils.tlv("00", "01") + TlvUtils.tlv("58", "PY");
        Map<String, String> parsed = TlvUtils.parse(data);
        assertEquals("01", parsed.get("00"));
        assertEquals("PY", parsed.get("58"));
    }

    @Test
    void shouldRejectInvalidLength() {
        assertThrows(TlvParseException.class, () -> TlvUtils.parse("000501"));
    }
}
