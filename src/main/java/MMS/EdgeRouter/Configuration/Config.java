package MMS.EdgeRouter.Configuration;

public class Config
{
    private static String keyStorePath = "/MMS/EdgeRouter/EdgeRouterKeys/EdgeRouterTest.p12";
    private static String keyStorePassword = "bcq8fsocuvu4mbdltm157bdpi8";

    public static String getKeyStorePath()
    {
        return keyStorePath;
    }

    public static String getKeyStorePassword()
    {
        return keyStorePassword;
    }
}


