package MMS.EdgeRouter.WebSocketManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class WebSocketManager
{
    private static WebSocketManager instance;
    private DeploymentHandler deploymentHandler;
    private ConnectionHandler connectionHandler;

    private WebSocketManager()
    {
        this.deploymentHandler = new DeploymentHandler();
        this.connectionHandler = ConnectionHandler.getHandler();
    }

    public static WebSocketManager getManager()
    {
        if(instance == null)
        {
            instance = new WebSocketManager();
        }

        return instance;
    }


    public void deployEndpoint(int port, String path, int maxConnections) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException
    {
        deploymentHandler.deployEndpoint(port, path, maxConnections);
    }


    public void undeployEndpoint() throws Exception
    {
        deploymentHandler.undeployEndpoint();
        connectionHandler.clearSessions();
    }
}
