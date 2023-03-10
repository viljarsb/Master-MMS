package MMS.Agent.WebSocketEndpointManager;

import MMS.Agent.ServiceDiscoveryManager.RouterInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;


public class WebSocketEndpointManager
{
    private static Logger logger = LogManager.getLogger(WebSocketEndpointManager.class);
    private static WebSocketEndpointManager webSocketEndpointManager;
    private ConnectionHandler connectionHandler;

    private WebSocketEndpointManager()
    {
        this.connectionHandler = ConnectionHandler.getConnectionHandler();
    }



    public static WebSocketEndpointManager getManager()
    {
        if(webSocketEndpointManager == null)
        {
            webSocketEndpointManager = new WebSocketEndpointManager();
            logger.info("WebSocketEndpointManager created");
        }

        return webSocketEndpointManager;
    }


    public void connectAnonymously(RouterInfo routerInfo) throws DeploymentException, URISyntaxException, IOException
    {
           this.connectionHandler.connectAnonymously(routerInfo.getUri());
    }

    public void connectWithCredentials(RouterInfo routerInfo, String username, String password) throws DeploymentException, URISyntaxException, IOException
    {
        //this.connectionHandler.connectWithCredentials(routerInfo.getUri(), username, password);
    }

    public void disconnect() throws IOException
    {
        this.connectionHandler.disconnect();
    }
}
