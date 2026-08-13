package py.com.ucom.sipap.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import py.com.ucom.sipap.exception.TlvParseException;
import py.com.ucom.sipap.exception.TransferValidationException;
import py.com.ucom.sipap.processor.BankConsumerProcessor;
import py.com.ucom.sipap.processor.QrParserProcessor;
import py.com.ucom.sipap.processor.RejectionProcessor;
import py.com.ucom.sipap.processor.TransferValidationProcessor;
import py.com.ucom.sipap.util.QrTestData;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SipapRouteBuilder extends RouteBuilder {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private static final AtomicInteger PRODUCER_A_CASE = new AtomicInteger(0);
    private static final AtomicInteger PRODUCER_B_CASE = new AtomicInteger(0);

    @Override
    public void configure() {
        onException(TlvParseException.class, TransferValidationException.class)
                .handled(true)
                .process(new RejectionProcessor())
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:resultado");

        /*
         * PRODUCTOR A ejecuta los casos 1, 3, 5 y 7.
         * PRODUCTOR B ejecuta los casos 2, 4 y 6.
         * Los delays están intercalados para que la consola muestre los siete
         * escenarios en el mismo orden de la consigna.
         */
        from("timer:bancoA?period=4000&delay=1000&repeatCount=4")
                .routeId("productor-banco-a")
                .process(exchange -> {
                    int execution = PRODUCER_A_CASE.incrementAndGet();
                    exchange.getMessage().setHeader("transactionId", nextTransactionId());

                    switch (execution) {
                        case 1 -> setScenario(exchange, 1, "TRANSFERENCIA VÁLIDA ITAU",
                                QrTestData.validDynamic(QrTestData.ITAU, "1234567890", "15000"));
                        case 2 -> setScenario(exchange, 3, "TRANSFERENCIA VÁLIDA FAMILIAR",
                                QrTestData.validDynamic(QrTestData.FAMILIAR, "9876543210", "25000"));
                        case 3 -> setScenario(exchange, 5, "LONGITUD TLV INCORRECTA",
                                QrTestData.invalidDeclaredLength());
                        case 4 -> setScenario(exchange, 7, "CHECKSUM INVÁLIDO",
                                QrTestData.invalidChecksum());
                        default -> throw new IllegalStateException("Caso no configurado para productor A");
                    }
                })
                .log(LoggingLevel.INFO,
                        "\n====================================================\n" +
                        "INICIO CASO ${header.scenarioNumber} - ${header.scenarioName}\n" +
                        "TX: ${header.transactionId}\n" +
                        "Productor: BANCO A\n" +
                        "QR: ${body}\n" +
                        "====================================================")
                .to("direct:sipap-in");

        from("timer:bancoB?period=4000&delay=3000&repeatCount=3")
                .routeId("productor-banco-b")
                .process(exchange -> {
                    int execution = PRODUCER_B_CASE.incrementAndGet();
                    exchange.getMessage().setHeader("transactionId", nextTransactionId());

                    switch (execution) {
                        case 1 -> setScenario(exchange, 2, "TRANSFERENCIA VÁLIDA ATLAS",
                                QrTestData.validDynamic(QrTestData.ATLAS, "2222222222", "20000"));
                        case 2 -> setScenario(exchange, 4, "BANCO DESTINO DESCONOCIDO",
                                QrTestData.unknownBank());
                        case 3 -> setScenario(exchange, 6, "MONTO MAYOR O IGUAL A 10.000.000",
                                QrTestData.amountAtLimit());
                        default -> throw new IllegalStateException("Caso no configurado para productor B");
                    }
                })
                .log(LoggingLevel.INFO,
                        "\n====================================================\n" +
                        "INICIO CASO ${header.scenarioNumber} - ${header.scenarioName}\n" +
                        "TX: ${header.transactionId}\n" +
                        "Productor: BANCO B\n" +
                        "QR: ${body}\n" +
                        "====================================================")
                .to("direct:sipap-in");

        // Message Channel + Wire Tap + Pipes and Filters
        from("direct:sipap-in")
                .routeId("sipap-mediator")
                .wireTap("direct:audit")
                .to("direct:parse")
                .to("direct:validate")
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:route-bank");

        from("direct:parse")
                .routeId("parse-tlv")
                .process(new QrParserProcessor());

        from("direct:validate")
                .routeId("validate-transfer")
                .process(new TransferValidationProcessor());

        // Content-Based Router
        from("direct:route-bank")
                .routeId("route-by-bank")
                .choice()
                    .when(header("bankCode").isEqualTo(QrTestData.ITAU)).to("direct:itau")
                    .when(header("bankCode").isEqualTo(QrTestData.ATLAS)).to("direct:atlas")
                    .when(header("bankCode").isEqualTo(QrTestData.FAMILIAR)).to("direct:familiar")
                    .otherwise().throwException(new TransferValidationException("Banco destino no enrutable"))
                .end();

        from("direct:itau")
                .routeId("consumer-itau")
                .process(new BankConsumerProcessor("ITAU"))
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:resultado");

        from("direct:atlas")
                .routeId("consumer-atlas")
                .process(new BankConsumerProcessor("ATLAS"))
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:resultado");

        from("direct:familiar")
                .routeId("consumer-familiar")
                .process(new BankConsumerProcessor("FAMILIAR"))
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:resultado");

        from("direct:resultado")
                .routeId("common-result")
                .log(LoggingLevel.INFO,
                        "\n====================================================\n" +
                        "FIN CASO ${header.scenarioNumber} - ${header.scenarioName}\n" +
                        "TX: ${header.transactionId}\n" +
                        "RESULTADO FINAL -> ${body}\n" +
                        "====================================================");

        from("direct:audit")
                .routeId("audit-wiretap")
                .log(LoggingLevel.INFO,
                        "AUDITORIA CASO=${header.scenarioNumber} TX=${header.transactionId} QR=${body}");
    }

    private static void setScenario(org.apache.camel.Exchange exchange,
                                    int scenarioNumber,
                                    String scenarioName,
                                    String qr) {
        exchange.getMessage().setHeader("scenarioNumber", scenarioNumber);
        exchange.getMessage().setHeader("scenarioName", scenarioName);
        exchange.getMessage().setBody(qr);
    }

    public static String nextTransactionId() {
        return "TX%06d".formatted(SEQUENCE.getAndIncrement());
    }
}
