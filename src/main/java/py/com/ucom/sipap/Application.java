package py.com.ucom.sipap;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;
import py.com.ucom.sipap.route.SipapRouteBuilder;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) throws Exception {
        String brokerUrl = env("ARTEMIS_URL", "tcp://localhost:61616");
        String user = env("ARTEMIS_USER", "artemis");
        String password = env("ARTEMIS_PASSWORD", "artemis");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(user);
        connectionFactory.setPassword(password);
        JmsComponent jms = JmsComponent.jmsComponentAutoAcknowledge(connectionFactory);

        Main main = new Main();
        main.bind("jms", jms);
        main.configure().addRoutesBuilder(new SipapRouteBuilder());
        main.run(args);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
