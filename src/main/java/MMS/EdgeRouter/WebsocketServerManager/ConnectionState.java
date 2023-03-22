package MMS.EdgeRouter.WebsocketServerManager;

import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;


import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

public class ConnectionState
{
    private Session session;
    private boolean connected;
    private PKIIdentity identity;


    public ConnectionState(Session session)
    {
        this.session = session;
        this.connected = true;

        checkAuthentication();
    }


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


    public PKIIdentity getIdentity()
    {
        return identity;
    }


    public Session getSession()
    {
        return session;
    }


    public boolean isConnected()
    {
        return connected;
    }


    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }
}
