package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.ServiceRegistrationException;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.ServiceRegistry.EndpointRegistry;
import MMS.EdgeRouter.UserInterface.CommandLineHandler;
import MMS.EdgeRouter.WsManagement.WsEndpointManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);

    public static void main(String[] args) throws IOException
    {
        logger.info("Starting up the Edge Router...");
        CommandLineHandler commandLineHandler = new CommandLineHandler();
        commandLineHandler.startCli();
    }
}
