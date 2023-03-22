package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.EdgeRouterInitException;
import MMS.EdgeRouter.Exceptions.EdgeRouterServiceLaunchException;
import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastService;
import MMS.EdgeRouter.WebsocketServerManager.ServerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.IO;

import java.io.IOException;
import java.security.Security;

public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);

    private static ServiceBroadcastManager serviceBroadcastManager;
    private static ServerManager serverManager;
    private static EdgeRouterService edgeRouterService;
    private static CommandLineInterface commandLineInterface;

    static
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void main(String[] args)
    {
        logger.info("Edge Router Started");

        try
        {
            initService();
            initComponents();
            startEdgeRouter();
            commandLineInterface.start();
        }

        catch (EdgeRouterInitException ex)
        {
            logger.fatal("A fatal error occurred while initializing the Edge Router.", ex);
        }

        catch (EdgeRouterServiceLaunchException ex)
        {
            logger.fatal("A fatal error occurred while starting the Edge Router.", ex);
        }
    }


    private static void initService()
    {
        edgeRouterService = new EdgeRouterService("Test", "/Test", 10, true);
    }


    private static void initComponents() throws EdgeRouterInitException
    {
        try
        {
            serviceBroadcastManager = ServiceBroadcastManager.getManager();
            serverManager = ServerManager.getManager();
            commandLineInterface = CommandLineInterface.getCLI();

            if(System.currentTimeMillis() == 0)
            {
                throw new IOException("Test Exception");
            }
        }

        catch (IOException ex)
        {
            logger.fatal("A fatal error occurred while initializing the Edge Router components.", ex);
            throw new EdgeRouterInitException("A fatal error occurred while initializing the Edge Router components.", ex);
        }
    }


    private static void startEdgeRouter() throws EdgeRouterServiceLaunchException
    {

        try
        {
            if (edgeRouterService.isPublic())
            {
                serviceBroadcastManager.broadcastService(edgeRouterService);
            }

            serverManager.deployServer(edgeRouterService);
        }

        catch (Exception ex)
        {
            logger.fatal("A fatal error occurred while starting the Edge Router.", ex);
            throw new EdgeRouterServiceLaunchException("A fatal error occurred while starting the Edge Router.", ex);
        }
    }
}
