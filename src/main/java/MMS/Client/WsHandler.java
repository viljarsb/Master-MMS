package MMS.Client;

import MMS.Client.Connections.AnonymousConnection;
import MMS.Client.Connections.AuthenticatedConnection;
import MMS.Client.Connections.Connection;
import MMS.Client.ServiceDiscovery.RouterInfo;
import MMS.Client.TLSConfiguration.TLSConfig;
import MMS.Client.TLSConfiguration.mTLSConfig;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class WsHandler
{
    public AnonymousConnection connectAnonymously(RouterInfo routerInfo, TLSConfig tlsConfig, AgentAdapter adapter)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        return connect(URI, factory, adapter);
    }


    public AuthenticatedConnection connectAuthenticated(RouterInfo routerInfo, mTLSConfig tlsConfig, AgentAdapter adapter)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        return connect(URI, factory, adapter);
    }


    public <T extends Connection> T connect(String URI, SslContextFactory tlsContextFactory, AgentAdapter adapter)
    {
        HttpClient httpClient;
        WebSocketClient client;

        try
        {
            httpClient = new HttpClient(tlsContextFactory);
            httpClient.start();

            client = new WebSocketClient(httpClient);
            client.start();

            java.net.URI destination = new URI(URI);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Sec-WebSocket-Protocol", "MMTP/1.0");

            WsEndpoint wsEndpoint = new WsEndpoint(adapter);

            Future<Session> future = client.connect(wsEndpoint, destination, request);
            future.get(5, TimeUnit.SECONDS);
            Connection connection = wsEndpoint.getConnection();
            return (T) connection;
        }

        catch (Exception ex)
        {
            adapter.onConnectionError(ex.getMessage(), ex.getCause());
        }

        return null;
    }
}
