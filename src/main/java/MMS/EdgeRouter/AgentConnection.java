package MMS.EdgeRouter;

import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.UUID;


/**
 * The {@code AgentConnection} class represents the state of a WebSocket connection.
 * It holds information about the session, the URI the connection was made to,
 * and the MCP-PKI identity of the connected user if available.
 */
public class AgentConnection
{
    private final Session session;
    private PKIIdentity identity;
    private final URI connectionURI;
    private String agentId;


    /**
     * Constructs a new {@code AgentConnection} object for a given session.
     *
     * @param session the WebSocket session for which the {@code AgentConnection} is created
     */
    public AgentConnection(Session session)
    {
        this.session = session;
        this.connectionURI = session.getUpgradeRequest().getRequestURI();
        this.agentId = UUID.randomUUID().toString();
        checkAuthentication();
    }


    /**
     * Checks the authentication status of the session by looking for a
     * client certificate and, if present, extracts the MCP-PKI identity
     * and stores it in the instance variable {@code identity}.
     */
    private void checkAuthentication()
    {
        HttpServletRequest request = ((ServletUpgradeRequest) session.getUpgradeRequest()).getHttpServletRequest();

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0)
        {
            X509Certificate cert = certs[0];
            this.identity = CertificateHandler.getIdentityFromCert(cert);
        }
    }


    /**
     * Returns the PKI identity associated with the connection, if available.
     * If null, the connection is not established by an authenticated user.
     *
     * @return the MCP PKI identity, or null if not available
     */
    public PKIIdentity getIdentity()
    {
        return identity;
    }


    /**
     * Returns the remote address of the connected WebSocket session.
     *
     * @return the remote address as a string
     */
    public String getRemoteAddress()
    {
        return session.getRemoteAddress().getAddress().getHostAddress();
    }


    /**
     * Returns the WebSocket session associated with the {@code AgentConnection}.
     *
     * @return the WebSocket session
     */
    public Session getSession()
    {
        return session;
    }


    /**
     * Returns the URI of the WebSocket connection, i.e. the URI the edge router
     * that the Agent has established the connection to.
     *
     * @return the connection URI
     */
    public URI getConnectionURI()
    {
        return connectionURI;
    }


    /**
     * Gets the Agent ID of the connected Agent.
     *
     * @return the Agent ID
     */
    public String getAgentId()
    {
        return agentId;
    }
}
