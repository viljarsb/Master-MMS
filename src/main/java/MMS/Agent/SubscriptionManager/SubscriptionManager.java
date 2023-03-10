package MMS.Agent.SubscriptionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubscriptionManager
{
    private static final Logger logger = LogManager.getLogger(SubscriptionManager.class);
    private static SubscriptionManager manager = null;

    private SubscriptionManager()
    {
    }

    public static SubscriptionManager getManager()
    {
        if (manager == null)
        {
            manager = new SubscriptionManager();
            logger.info("SubscriptionManager created");
        }
        return manager;
    }
}
