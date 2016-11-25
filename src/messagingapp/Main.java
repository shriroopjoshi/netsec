package messagingapp;

import client.AuthenticationClient;
import exceptions.TamperedMessageException;
import java.io.IOException;
import server.MessagingServer;

/**
 *
 * @author shriroop
 */
public class Main {

    public static void main(String[] args) throws IOException, TamperedMessageException {
        if (args[0].equalsIgnoreCase("server")) {
            MessagingServer ms = new MessagingServer();
            ms.start();
        } else {
            AuthenticationClient mc = new AuthenticationClient();
            mc.start();
        }
    }
}
