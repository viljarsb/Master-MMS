package MMS.EdgeRouter;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.UserInterface.CommandLineInterface;
import MMS.EdgeRouter.WebSocketManager.WebSocketManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);
    private static final CommandLineInterface commandLineInterface = CommandLineInterface.getCLI();

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        logger.info("Starting Edge Router");
        commandLineInterface.start();
    }
}
