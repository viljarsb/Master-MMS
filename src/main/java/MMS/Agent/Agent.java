package MMS.Agent;

import MMS.Agent.ServiceDiscoveryManager.RouterInfo;
import MMS.Agent.ServiceDiscoveryManager.ServiceDiscoveryListener;
import MMS.Agent.WebSocketEndpointManager.WebSocketEndpointManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.ServiceInfo;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class Agent
{
    private static final Logger logger = LogManager.getLogger(Agent.class);

    //private static final ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getManager();
    private final WebSocketEndpointManager webSocketEndpointManager = WebSocketEndpointManager.getManager();
    private final ServiceDiscoveryListener serviceDiscoveryListener = new ServiceDiscoveryListener();


    public List<RouterInfo> discover()
    {
        logger.info("Discovering routers");
        return serviceDiscoveryListener.listen();
    }


    public void connectAnonymously(RouterInfo routerInfo) throws DeploymentException, URISyntaxException, IOException, TimeoutException
    {
        webSocketEndpointManager.connectAnonymously(routerInfo);
    }


    public void connectAuthenticated(RouterInfo routerInfo)
    {

    }


    public void disconnect()
    {

    }


    public void reconnect()
    {

    }


    public void subscribe(String topic)
    {

    }


    public void unsubscribe(String topic)
    {

    }


    public void send()
    {

    }


    public void sendSecure()
    {

    }


    public void registerReceiver()
    {

    }
}
