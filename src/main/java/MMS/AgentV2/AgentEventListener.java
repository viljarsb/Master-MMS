package MMS.AgentV2;

import MMS.Agent.ServiceDiscoveryListner.RouterInfo;

import java.util.List;

public interface AgentEventListener
{
    void onRouterDiscovery(List<RouterInfo> routerInfos);
    void onAnonymousConnection(AnonymousConnection connection);
    void onAuthenticatedConnection(AuthenticatedConnection connection);
    void onDisconnect(String reason, int statusCode);
    void onDirectMessage(String sender, byte[] message);
    void onSubjectCastMessage(String subject, String sender, byte[] message);
    void onConnectionError(String reason, Throwable cause);
}
