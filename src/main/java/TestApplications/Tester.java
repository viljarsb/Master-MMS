package TestApplications;

import MMS.AgentV2.AgentFactory;
import MMS.AgentV2.Exceptions.AgentFactoryInitException;
import MMS.AgentV2.RouterInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Tester
{
    public static void main(String[] args) throws AgentFactoryInitException, ExecutionException, InterruptedException
    {
        AgentFactory factory = AgentFactory.getFactory();
        Future<List<RouterInfo>> future = factory.discover();
        List<RouterInfo> routers = future.get();

        for (RouterInfo router : routers)
        {
            System.out.println(router.getServiceName());
            System.out.println(router.getServiceIP());
            System.out.println(router.getServicePort());
            System.out.println(router.getServicePath());
        }


    }

}
