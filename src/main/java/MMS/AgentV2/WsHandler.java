package MMS.AgentV2;

import MMS.Agent.Exceptions.ConnectException;
import MMS.Agent.ServiceDiscoveryListner.RouterInfo;
import MMS.Agent.WebSocketEndpointManager.AgentEndpoint;
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
    private final AgentAdapter adapter;

    public WsHandler(AgentAdapter adapter)
    {
        this.adapter = adapter;
    }

    public void connectAnonymously(RouterInfo routerInfo, TLSConfig tlsConfig)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        connect(URI, factory);
    }

    public void connectAuthenticated(RouterInfo routerInfo, mTLSConfig tlsConfig)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        connect(URI, factory);
    }

    public void connect(String URI, SslContextFactory tlsContextFactory)
    {
        try
        {
            HttpClient httpClient = new HttpClient(tlsContextFactory);
            httpClient.start();

            WebSocketClient client = new WebSocketClient(httpClient);
            client.start();

            java.net.URI destination = new URI(URI);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Sec-WebSocket-Protocol", "MMTP/1.0");

            AgentEndpoint agentEndpoint = new AgentEndpoint(messageHandler);

            client.connect(agentEndpoint, destination, request);
        }

        catch (Exception ex)
        {
           adapter.onConnectionError(ex.getMessage(), ex.getCause());
        }
    }
}
