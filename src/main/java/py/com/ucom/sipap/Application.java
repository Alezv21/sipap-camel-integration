package py.com.ucom.sipap;

import org.apache.camel.main.Main;
import py.com.ucom.sipap.route.SipapRouteBuilder;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new SipapRouteBuilder());
        main.run(args);
    }
}
