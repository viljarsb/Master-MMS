package MMS.EdgeRouter;

public class Config
{
    private static int numOfWorkerThreads = 255;
    private static String keystorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\keystore-test-viljar";
    private static String keystorePassword = "8r91fpin885elh46aju8q0do6f";
    private static String truststorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\truststore-root-ca";
    private static String truststorePassword = "changeit";

    public static int getNumOfWorkerThreads()
    {
        return numOfWorkerThreads;
    }


    public static String getKeystorePath()
    {
        return keystorePath;
    }

    public static String getKeystorePassword()
    {
        return keystorePassword;
    }

    public static String getTruststorePath()
    {
        return truststorePath;
    }

    public static String getTruststorePassword()
    {
        return truststorePassword;
    }

}
