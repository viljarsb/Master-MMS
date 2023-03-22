package MMS.AgentV2;

import MMS.Agent.ServiceDiscoveryListner.RouterInfo;

import java.util.List;

public abstract class AgentAdapter implements AgentEventListener
{
    private AgentV2Status status;
    private Connection connection;

    public AgentAdapter()
    {
        this.status = AgentV2Status.NOT_CONNECTED;
    }


    @Override
    public void onRouterDiscovery(List<RouterInfo> routerInfos)
    {

    }


    @Override
    public void onAnonymousConnection(AnonymousConnection connection)
    {
        this.connection = connection;
        this.status = AgentV2Status.CONNECTED_ANONYMOUS;
    }


    @Override
    public void onAuthenticatedConnection(AuthenticatedConnection connection)
    {
        this.connection = connection;
        this.status = AgentV2Status.CONNECTED_AUTHENTICATED;
    }


    @Override
    public void onDisconnect(String reason, int statusCode)
    {
        this.connection = null;
        this.status = AgentV2Status.LOST_CONNECTION;
    }


    @Override
    public void onDirectMessage(String sender, byte[] message)
    {

    }


    @Override
    public void onSubjectCastMessage(String subject, String sender, byte[] message)
    {

    }


    @Override
    public void onConnectionError(String reason, Throwable cause)
    {

    }


}
