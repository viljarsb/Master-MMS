package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.EdgeRouterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;


public class ServerManager
{
    private final static Logger logger = LogManager.getLogger(ServerManager.class);

    private static ServerManager instance;

    private final ServerDeployer serverDeployer;

    private final ConnectionHandler connectionHandler;

    private LocalDateTime started;


    private ServerManager()
    {
        serverDeployer = ServerDeployer.getDeployer();
        connectionHandler = ConnectionHandler.getHandler();
    }


    public static ServerManager getManager()
    {
        if (instance == null)
        {
            instance = new ServerManager();
            logger.info("Server Manager Created");
        }
        return instance;
    }


    public void deployServer(EdgeRouterService service) throws Exception
    {
        logger.info("Deploying Server");
        serverDeployer.deployServer(service);
        logger.info("Server Deployed");
        started = LocalDateTime.now();
    }


    public void undeployServer()
    {
        logger.info("Undeploy Server");
        connectionHandler.closeAllConnections();
        serverDeployer.undeployServer();
        logger.info("Server Undeploy");
    }


    public LocalDateTime getDeploymentTime()
    {
        return started;
    }
}
