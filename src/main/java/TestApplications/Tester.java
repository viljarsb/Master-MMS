package TestApplications;

import MMS.ClientMMS.Client;
import MMS.ClientMMS.ClientConfiguration;
import MMS.ClientMMS.Exceptions.SetupException;
import MMS.ClientMMS.SMMPCallback;



public class Tester
{
    public static void main(String[] args) throws SetupException
    {
        SMMPCallback callback = new SMMPCallback()
        {
            @Override
            public void onDirectMessage(byte[] message, String sender)
            {

            }


            @Override
            public void onSubjectCast(byte[] message, String subject)
            {

            }


            @Override
            public void onMessageDelivered(String id, String mrn)
            {

            }


            @Override
            public void onMessageDeliveryFailed(String id, String mrn)
            {

            }
        };

       String keystorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\keystore-test-viljar";
      String keystorePassword = "8r91fpin885elh46aju8q0do6f";
     String truststorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\truststore-root-ca";
    String truststorePassword = "changeit";

        ClientConfiguration.ConfigBuilder builder = new ClientConfiguration.ConfigBuilder();
        builder.setCallback(callback);
        builder.setKeyStore(keystorePath, keystorePassword);
        builder.setTrustStore(truststorePath, truststorePassword);
        builder.setCertificateStore(truststorePath, truststorePassword);
        Client client = new Client(builder.build());

        System.out.println(client.Discover());
    }
}
