package MMS.EdgeRouter.WebSocketManager;

import org.apache.logging.log4j.core.jmx.Server;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

public class WebSocketManager
{
    private static WebSocketManager instance;

    private final DeploymentService deploymentService;

    private WebSocketManager()
    {
        this.deploymentService = DeploymentService.getService();
    }


    public static WebSocketManager getManager()
    {
        if (instance == null)
        {
            instance = new WebSocketManager();
        }

        return instance;
    }


    public void deployEndpoint(int port, String path, int maxConnections) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException
    {
        deploymentService.deployEndpoint(port, path, maxConnections);
    }


    public void undeployEndpoint(URI uri) throws Exception
    {
        deploymentService.undeployEndpoint(uri);
    }


    public ArrayList<ServerInfo> getDeployedEndpoints()
    {
        return deploymentService.getDeployedEndpoints();
    }
}
