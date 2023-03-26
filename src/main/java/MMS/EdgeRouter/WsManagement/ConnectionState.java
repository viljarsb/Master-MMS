package MMS.EdgeRouter.WsManagement;

import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.cert.X509Certificate;


/**
 * The {@code ConnectionState} class represents the state of a WebSocket connection.
 * It holds information about the session, the URI the connection was made to,
 * and the MCP-PKI identity of the connected user if available.
 */
public class ConnectionState
{
    private final Session session;
    private PKIIdentity identity;
    private final URI connectionURI;


    /**
     * Constructs a new {@code ConnectionState} object for a given session.
     *
     * @param session the WebSocket session for which the {@code ConnectionState} is created
     */
    public ConnectionState(Session session)
    {
        this.session = session;
        this.connectionURI = session.getUpgradeRequest().getRequestURI();
        checkAuthentication();
    }


    /**
     * Checks the authentication status of the session by looking for a
     * client certificate and, if present, extracts the MCP-PKI identity.
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
     * @return the PKI identity, or null if not available
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
     * Returns the WebSocket session associated with the {@code ConnectionState}.
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
}
