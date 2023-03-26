package MMS.EdgeRouter.Exceptions;

import org.eclipse.jetty.server.Server;

import java.util.List;

public class WsEndpointUndeploymentException extends Exception
{
    private List<Server> servers;

    public WsEndpointUndeploymentException(String message)
    {
        super(message);
    }

    public WsEndpointUndeploymentException(String message, List<Server> servers)
    {
        super(message);
        this.servers = servers;
    }

    public WsEndpointUndeploymentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WsEndpointUndeploymentException(String message, Throwable cause, List<Server> servers)
    {
        super(message, cause);
        this.servers = servers;
    }

    public WsEndpointUndeploymentException(Throwable cause)
    {
        super(cause);
    }

    public WsEndpointUndeploymentException(Throwable cause, List<Server> servers)
    {
        super(cause);
        this.servers = servers;
    }

    public List<Server> getServers()
    {
        return servers;
    }
}
