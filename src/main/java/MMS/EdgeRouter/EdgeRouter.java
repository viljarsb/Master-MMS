package MMS.EdgeRouter;

import MMS.EdgeRouter.UserInterface.CommandLineHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

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
