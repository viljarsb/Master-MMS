package MMS.EdgeRouter;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);

    public static void main(String[] args) throws IOException
    {
        logger.info("Starting Edge Router");

        ServiceBroadcastManager serviceBroadcastManager = new ServiceBroadcastManager();
        serviceBroadcastManager.broadcastService("hei", 10, "test");

      //  DeploymentHandler deploymetHandler = new DeploymentHandler();
      //  deploymetHandler.deployEndpoint(8080, "/test", 10);

        try
        {
            Thread.sleep(100000);
        }

        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
    }
}
