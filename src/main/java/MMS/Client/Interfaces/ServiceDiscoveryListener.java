package MMS.Client.Interfaces;

import MMS.Client.ServiceDiscovery.RouterInfo;

import java.util.List;

public interface ServiceDiscoveryListener
{
    void servicesDiscovered(List<RouterInfo> services);
}
