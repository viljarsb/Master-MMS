package MMS.EdgeRouter.WebSocketManager;

import MMS.EdgeRouter.ServiceRegistry.EdgeRouterService;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class WebSocketManager
{
    private static WebSocketManager instance;
    private final DeploymentService deploymentService;
    private final WebSocketServerRegistry webSocketServerRegistry;


    private WebSocketManager()
    {
        this.deploymentService = DeploymentService.getService();
        this.webSocketServerRegistry = WebSocketServerRegistry.getRegistry();
    }


    public static WebSocketManager getManager()
    {
        if (instance == null)
        {
            instance = new WebSocketManager();
        }

        return instance;
    }


    public void deployEndpoint(EdgeRouterService service) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException
    {
        String serviceName = service.getServiceName();
        String path = service.getServicePath();
        int port = service.getServicePort();
        int maxConnections = 255;

        Server server = deploymentService.deployEndpoint(port, path, maxConnections);
        webSocketServerRegistry.deployServer(serviceName, server);
    }


    public void undeployEndpoint(EdgeRouterService edgeRouterService) throws Exception
    {
        Server server = webSocketServerRegistry.getServer(edgeRouterService.getServiceName());
        deploymentService.undeployEndpoint(server);
    }
}
