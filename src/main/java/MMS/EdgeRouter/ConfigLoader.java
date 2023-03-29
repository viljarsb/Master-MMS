package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.ConfigLoadException;


public class ConfigLoader
{
    public static EdgeRouterConfig loadConfig(String configFile) throws ConfigLoadException
    {
        try
        {
            String path = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\truststore-root-ca";
            String password = "changeit";
            String ksPath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\keystore_test";
            String ksPass = "changeit";
            return new EdgeRouterConfig.Builder(ksPath, ksPass, path, password).build();
        }

        catch (Exception ex)
        {
            throw new ConfigLoadException("Error loading configuration.", ex);
        }
    }
}
