package py.com.ucom.sipap.route;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import py.com.ucom.sipap.domain.TransferMessage;
import py.com.ucom.sipap.domain.TransferRequest;
import py.com.ucom.sipap.exception.DuplicateTransactionException;
import py.com.ucom.sipap.exception.TlvParseException;
import py.com.ucom.sipap.exception.TransferValidationException;
import py.com.ucom.sipap.processor.*;
import py.com.ucom.sipap.util.QrTestData;

public class SipapRouteBuilder extends RouteBuilder {
    private final String bankMockBaseUrl = env("BANK_MOCK_BASE_URL", "http://localhost:8089");

    @Override
    public void configure() {
        restConfiguration()
                .component("undertow")
                .host("0.0.0.0")
                .port(8080)
                .bindingMode(RestBindingMode.off);

        onException(DuplicateTransactionException.class)
                .handled(true)
                .process(new ApiRejectionProcessor("DUPLICADA", 409))
                .marshal().json(JsonLibrary.Jackson);

        onException(TlvParseException.class, TransferValidationException.class)
                .handled(true)
                .process(new ApiRejectionProcessor("RECHAZADA", 400))
                .marshal().json(JsonLibrary.Jackson);

        rest("/transferencias")
                .description("API REST de entrada para transferencias SIP/QR")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:api-transferencias");

        // Conector REST de entrada + pipeline heredado de Tarea 1.
        from("direct:api-transferencias")
                .routeId("api-transferencias")
                .unmarshal().json(JsonLibrary.Jackson, TransferRequest.class)
                .process(new RequestMetadataProcessor())
                .wireTap("direct:audit-input")
                .to("direct:parse")
                .process(new StaticAmountEnrichmentProcessor())
                .to("direct:validate")
                .process(new AmountLimitProcessor())
                .process(new IdempotencyProcessor())
                .process(new TransferMessageProcessor())
                .marshal().json(JsonLibrary.Jackson)
                .log(LoggingLevel.INFO, "PUBLICANDO EN transferencias.in TX=${header.transactionId} JMSCorrelationID=${header.JMSCorrelationID}")
                .to("jms:queue:transferencias.in")
                .process(new AcceptedResponseProcessor())
                .marshal().json(JsonLibrary.Jackson);

        from("direct:parse")
                .routeId("parse-tlv")
                .process(new QrParserProcessor());

        from("direct:validate")
                .routeId("validate-transfer")
                .process(new TransferValidationProcessor());

        // Cola externa de entrada: desacopla la API del procesamiento bancario.
        from("jms:queue:transferencias.in")
                .routeId("artemis-transferencias-in")
                .unmarshal().json(JsonLibrary.Jackson, TransferMessage.class)
                .process(new RestoreMetadataProcessor())
                .log(LoggingLevel.INFO, "CONSUMIDA transferencias.in TX=${header.transactionId} banco=${header.bankCode}")
                .marshal().json(JsonLibrary.Jackson)
                .choice()
                    .when(header("bankCode").isEqualTo(QrTestData.ITAU)).to("jms:queue:cola.itau")
                    .when(header("bankCode").isEqualTo(QrTestData.ATLAS)).to("jms:queue:cola.atlas")
                    .when(header("bankCode").isEqualTo(QrTestData.FAMILIAR)).to("jms:queue:cola.familiar")
                    .otherwise().log(LoggingLevel.ERROR, "Banco no enrutable después de Artemis TX=${header.transactionId}")
                .end();

        bankConsumer("jms:queue:cola.itau", "consumer-itau", "ITAU");
        bankConsumer("jms:queue:cola.atlas", "consumer-atlas", "ATLAS");
        bankConsumer("jms:queue:cola.familiar", "consumer-familiar", "FAMILIAR");

        from("direct:bank-processing")
                .routeId("bank-processing")
                .process(new DateValidationProcessor())
                .choice()
                    .when(header("dateValid").isEqualTo(true))
                        .process(new CanonicalBodyProcessor())
                        .marshal().json(JsonLibrary.Jackson)
                        .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                        .setHeader("X-Transaction-Id", header("transactionId"))
                        .setHeader("X-Bank", header("bankName"))
                        .log(LoggingLevel.INFO,
                                "REQUEST-REPLY banco mock=${header.bankName} TX=${header.transactionId} body=${body}")
                        .toD(bankMockBaseUrl + "/banks/${header.bankName}/transfer?httpMethod=POST&throwExceptionOnFailure=false")
                        .process(new BankResponseProcessor())
                    .otherwise()
                        .log(LoggingLevel.WARN,
                                "RECHAZADA POR FECHA TX=${header.transactionId} fecha=${header.transactionDate}")
                .end()
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:resultado-final");

        from("direct:resultado-final")
                .routeId("resultado-final")
                .log(LoggingLevel.INFO,
                        "RESULTADO FINAL TX=${header.transactionId} banco=${header.bankName} -> ${body}");

        from("direct:audit-input")
                .routeId("audit-input")
                .log(LoggingLevel.INFO,
                        "AUDITORIA ENTRADA TX=${header.transactionId} fecha=${header.transactionDate} qr=${body}");
    }

    private void bankConsumer(String endpoint, String routeId, String bankName) {
        from(endpoint)
                .routeId(routeId)
                .unmarshal().json(JsonLibrary.Jackson, TransferMessage.class)
                .process(new RestoreMetadataProcessor())
                .setHeader("bankName", constant(bankName))
                .log(LoggingLevel.INFO,
                        "CONSUMIDOR " + bankName + " TX=${header.transactionId} fecha=${header.transactionDate} JMSCorrelationID=${header.JMSCorrelationID}")
                .to("direct:bank-processing");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
