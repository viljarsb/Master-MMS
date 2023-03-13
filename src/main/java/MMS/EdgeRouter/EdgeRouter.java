package MMS.EdgeRouter;

import MMS.EdgeRouter.UserInterface.CommandLineInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EdgeRouter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouter.class);
    private static final CommandLineInterface commandLineInterface = CommandLineInterface.getCLI();

    public static void main(String[] args)
    {
        logger.info("Starting Edge Router");
        commandLineInterface.start();
    }
}
