package MMS.Client;

import MMS.Client.Connections.DisconnectionReason;
import MMS.Client.Interfaces.ConnectionListener;
import MMS.Client.Interfaces.MessageListener;
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


/**
 * WsHandler is responsible for establishing WebSocket connections
 * with a specified router, either anonymously or with client authentication.
 * It uses ConnectionListener and MessageListener for handling the events
 * during the WebSocket connection.
 */
public class WsHandler
{

    /**
     * Connects to the specified router anonymously using the given TLS configuration,
     * ConnectionListener, and MessageListener.
     *
     * @param routerInfo            the router information to connect to
     * @param tlsConfig             the TLS configuration for the connection
     * @param connectionListener    the listener for handling connection events
     * @param messageListener       the listener for handling message events
     */
    public void connectAnonymously(RouterInfo routerInfo, TLSConfig tlsConfig, ConnectionListener eventListener, MessageListener messageListener)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        connect(URI, factory, eventListener, messageListener);
    }


    /**
     * Connects to the specified router with client authentication using the given mTLS configuration,
     * ConnectionListener, and MessageListener.
     *
     * @param routerInfo           the router information to connect to
     * @param tlsConfig            the mTLS configuration for the connection
     * @param connectionListener   the listener for handling connection events
     * @param messageListener      the listener for handling message events
     */
    public void connectAuthenticated(RouterInfo routerInfo, mTLSConfig tlsConfig, ConnectionListener eventListener, MessageListener messageListener)
    {
        String URI = routerInfo.getUri();
        SslContextFactory factory = tlsConfig.getTLSContextFactory();
        connect(URI, factory, eventListener, messageListener);
    }


    /**
     * Establishes a WebSocket connection to the specified URI using the provided
     * TLS context factory, ConnectionListener, and MessageListener.
     *
     * @param URI                  the URI of the router to connect to
     * @param tlsContextFactory    the TLS context factory for creating the TLS context
     * @param connectionListener   the listener for handling connection events
     * @param messageListener      the listener for handling message events
     */
    private void connect(String URI, SslContextFactory tlsContextFactory, ConnectionListener connectionListener, MessageListener messageListener)
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

            WsEndpoint wsEndpoint = new WsEndpoint(connectionListener, messageListener);

            Future<Session> future = client.connect(wsEndpoint, destination, request);
            future.get(5, TimeUnit.SECONDS);
        }

        catch (Exception ex)
        {
            DisconnectionReason reason = new DisconnectionReason("Could not establish WebSocket connection to edge router: " + ex.getMessage(), 1006);
            connectionListener.onConnectError(reason);
        }
    }
}
