package MMS.EdgeRouter;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.ServiceRegistry.EdgeRouterService;
import MMS.EdgeRouter.ServiceRegistry.ServiceRegistry;
import MMS.EdgeRouter.WebSocketManager.WebSocketManager;

import java.io.IOException;

public class ControlInterface
{
    private ServiceBroadcastManager serviceBroadcastManager;
    private WebSocketManager webSocketManager;
    private ServiceRegistry serviceRegistry;
    private static ControlInterface instance;

    private ControlInterface()
    {
        serviceBroadcastManager = ServiceBroadcastManager.getManager();
        webSocketManager = WebSocketManager.getManager();
        serviceRegistry = ServiceRegistry.getRegistry();
    }

    public static ControlInterface getInterface()
    {
        if (instance == null)
        {
            instance = new ControlInterface();
        }
        return instance;
    }

    public void deployService(String serviceName, String servicePath, int servicePort, boolean isPublic) throws IOException
    {
        EdgeRouterService service = new EdgeRouterService(serviceName, servicePath, servicePort, isPublic);

        if(service.isPublic())
        {
            serviceBroadcastManager.broadcastService(service);
        }

        webSocketManager.deployEndpoint(service);


    }



}
