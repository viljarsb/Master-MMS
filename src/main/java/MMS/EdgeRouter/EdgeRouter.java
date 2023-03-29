package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.ConfigLoadException;
import MMS.EdgeRouter.Exceptions.ServiceBroadcastException;
import MMS.EdgeRouter.Exceptions.ServiceBroadcasterCreationException;
import MMS.EdgeRouter.Exceptions.WsEndpointDeploymentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);


    public static void main(String[] args)
    {
        logger.info("Edge Router Starting...");

        try
        {
            EdgeRouterConfig config = loadConfig();
            startWsServer(config);
            startServiceBroadcaster(config);

            Thread.sleep(1000000000);
            logger.info("Edge Router started successfully.");
        }

        catch (Exception ex)
        {
            logger.fatal("Failed to start Edge Router: {}", ex.getMessage(), ex);
            System.exit(1);
        }
    }


    private static EdgeRouterConfig loadConfig() throws ConfigLoadException
    {
        logger.info("Loading configuration file...");
        EdgeRouterConfig config = ConfigLoader.loadConfig("file");
        logger.info("Configuration file loaded successfully.");
        return config;
    }


    private static WsServer startWsServer(EdgeRouterConfig config) throws WsEndpointDeploymentException
    {
        logger.info("Starting WebSocket server...");
        try
        {
            WsServer wsServer = WsServer.create(config);
            wsServer.start();
            logger.info("WebSocket server started on port {}.", config.getPort());
            return wsServer;
        }

        catch (WsEndpointDeploymentException ex)
        {
            logger.fatal("Failed to start WebSocket server.");
            throw ex;
        }
    }


    private static ServiceBroadcaster startServiceBroadcaster(EdgeRouterConfig config) throws ServiceBroadcastException, ServiceBroadcasterCreationException
    {
        if (config.getBroadcast())
        {
            logger.info("Starting service broadcaster...");
            try
            {
                ServiceBroadcaster broadcaster = ServiceBroadcaster.create(config.getIp(), config.getName(), config.getPort(), config.getPath(), "MME EDGE ROUTER");
                broadcaster.start();
                logger.info("Service broadcaster started successfully.");
                return broadcaster;
            }

            catch (ServiceBroadcasterCreationException ex)
            {
                logger.fatal("Failed to start service broadcaster: {}", ex.getMessage(), ex);
                throw ex;
            }

            catch (ServiceBroadcastException ex)
            {
                logger.fatal("Failed to start service broadcaster.");
                throw ex;
            }
        }
        return null;
    }
}

