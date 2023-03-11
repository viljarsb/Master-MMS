package TestApplications;

import MMS.Agent.Agent;
import MMS.Agent.ServiceDiscoveryManager.RouterInfo;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Tester
{
    public static void main(String[] args) throws DeploymentException, URISyntaxException, IOException, TimeoutException
    {
        Agent agent = new Agent();
        ArrayList<RouterInfo> routers = (ArrayList<RouterInfo>) agent.discover();
        agent.connectAnonymously(routers.get(0));

        try
        {
            Thread.sleep(100000);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
