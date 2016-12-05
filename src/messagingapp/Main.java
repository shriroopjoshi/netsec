package messagingapp;

import client.ServerLoginClient;
import exceptions.TamperedMessageException;
import java.io.IOException;
import server.MessagingServer;
import utility.Constants;

/**
 *
 * @author shriroop
 */
public class Main {

    public static void main(String[] args) throws IOException, TamperedMessageException {
        Constants.VERBOSE = args.length == 2 & args[1].equalsIgnoreCase("verbose");
        if (args[0].equalsIgnoreCase("server")) {
            MessagingServer ms = new MessagingServer();
            ms.startServer();
        } else {
            ServerLoginClient mc = new ServerLoginClient();
            mc.start();
        }
    }
}
