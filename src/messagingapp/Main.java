package messagingapp;

import client.MessagingClient;
import java.io.IOException;
import server.MessagingServer;

/**
 *
 * @author shriroop
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args[0].equalsIgnoreCase("server")) {
            MessagingServer ms = new MessagingServer();
            ms.start();
        } else {
            MessagingClient mc = new MessagingClient();
            mc.start();
        }
    }
}
