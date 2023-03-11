package MMS.EdgeRouter.RouterWebSocketManager;

import MMS.Agent.WebSocketEndpointManager.TLSContextManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;


public class DeploymetHandler
{


/**    private static final Logger logger = LogManager.getLogger(DeploymetHandler.class);

    public void deployEndpoint(String endpointUrl, int endpointPort, String endpointPath) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException
    {
        String serverUrl = "wss://" + endpointUrl + ":" + endpointPort + endpointPath;
        SSLContext tlsContext = TLSContextManager.getTLSContext();
        Server server = new Server(endpointUrl, endpointPort, endpointPath, null, EdgeRouterEndpoint.class);


        try
        {
            server.start();
            logger.info("Endpoint deployed: " + serverUrl);
            endpoints.add(server);
        }

        catch (Exception ex)
        {
            logger.info("Endpoint deployment failed: " + serverUrl);
            throw new RuntimeException(ex);
        }
    }


    public void undeployEndpoints()
    {
        for (Server server : endpoints)
        {
            server.stop();
            logger.info("Endpoint undeployed");
        }
        endpoints.clear();
    } */
}
