package MMS.AgentV2;

import MMS.Agent.Exceptions.AgentInitException;
import MMS.Agent.ServiceDiscoveryListner.RouterInfo;
import MMS.Agent.ServiceDiscoveryListner.ServiceDiscoveryListener;
import MMS.Agent.WebSocketEndpointManager.WebSocketEndpointManager;
import MMS.Router.ConnectionHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Agent
{
    private final ServiceDiscoveryListener serviceDiscoveryListener;
    private final WsHandler wsHandler;
    private final ExecutorService workerPool;

    private final AgentAdapter adapter;


    private Agent(AgentAdapter adapter) throws IOException
    {
        this.serviceDiscoveryListener = new ServiceDiscoveryListener();
        this.wsHandler = new WsHandler(adapter);
        this.workerPool = Executors.newCachedThreadPool();
        this.adapter = adapter;
    }


    public static Agent getInstance(AgentAdapter adapter) throws AgentInitException
    {
        try
        {
            return new Agent(adapter);
        }

        catch (Exception ex)
        {
            throw new AgentInitException("Failed to create MMS Agent instance");
        }
    }


    public void discovery()
    {
        Runnable runnable = () ->
        {
            List<RouterInfo> discoveredServices = serviceDiscoveryListener.listen();

            if (discoveredServices.size() > 0)
            {
                adapter.onRouterDiscovery(discoveredServices);
            }
        };

        delegate(runnable);
    }


    public void connectAnonymously(RouterInfo routerInfo)
    {
        Runnable runnable = () ->
        {
            adapter.onAnonymousConnection("");
            System.out.println("Anonymous connection");
        };

        delegate(runnable);
    }


    public void connectAuthenticated(RouterInfo routerInfo)
    {
        Runnable runnable = () ->
        {
            adapter.onAuthenticatedConnection("");
            System.out.println("Anonymous connection");
        };

        delegate(runnable);
    }


    private void delegate(Runnable runnable)
    {
        workerPool.execute(runnable);
    }
}
