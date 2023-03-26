package MMS.EdgeRouter.UserInterface;

import MMS.EdgeRouter.Exceptions.WsEndpointUndeploymentException;
import MMS.EdgeRouter.ServiceManager;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import MMS.EdgeRouter.ThreadPoolService;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.*;

public class CommandLineHandler
{
    private final Options cliOptions;
    private final CommandLineParser commandLineParser;
    private final Scanner scanner;
    private volatile boolean running = true;

    private final ServiceManager serviceManager;
    private final SubscriptionManager subscriptionManager;


    public CommandLineHandler() throws IOException
    {
        cliOptions = new Options();
        commandLineParser = new DefaultParser();
        scanner = new Scanner(System.in);

        serviceManager = ServiceManager.getServiceManager();
        subscriptionManager = SubscriptionManager.getInstance();
        initOptions();
    }


    private void initOptions()
    {
        cliOptions.addOption("h", "help", false, "Print this message");
        cliOptions.addOption("l", "list-services", false, "List all services");
        cliOptions.addOption("s", "shutdown", false, "Shutdown the edge router");
        cliOptions.addOption("a", "start-service", false, "Starts a new service");
        cliOptions.addOption("b", "stop-service", false, "Stop a service");
        cliOptions.addOption("c", "list-subscriptions", false, "List all subscriptions");
        cliOptions.addOption("d", "list-connections", false, "Lists all connections to the MMS");
    }


    public void startCli()
    {
        running = true;
        ThreadPoolService.getWorkerPool().execute(this::start);
    }


    private void stopCli()
    {
        running = false;
    }


    private void start()
    {
        System.out.println("Welcome to the EdgeRouter CLI - v0.1");
        System.out.println("Type 'help' for a list of commands");
        CommandLine commandLine = null;

        while (running)
        {
            System.out.println();
            System.out.print("EdgeRouter> ");
            String input = scanner.nextLine();
            String[] args = input.split(" ");

            try
            {
                commandLine = commandLineParser.parse(cliOptions, args);

                if (commandLine.hasOption("h"))
                {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("EdgeRouter", cliOptions);
                }

                else if (commandLine.hasOption("l"))
                {
                    listServices();
                }

                else if (commandLine.hasOption("s"))
                {
                    shutdown();
                }

                else if (commandLine.hasOption("a"))
                {
                    startService();
                }

                else if (commandLine.hasOption("b"))
                {
                    stopService();
                }

                else if (commandLine.hasOption("c"))
                {
                    listSubscriptions();
                }

                else
                {
                    System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            }

            catch (ParseException e)
            {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }


    private void listServices()
    {
        System.out.println();
        System.out.println("Listing services...");

        List<EndpointInfo> services = serviceManager.getAllServices();

        System.out.println();
        if (services.isEmpty())
        {
            System.out.println("There are no services registered.");
        }
        else
        {
            System.out.println();
            System.out.println("Currently deployed endpoints:");
            System.out.printf("%-30s %-20s %-10s %n", "Service Name", "Service Path", "Connections");
            for (EndpointInfo service : services)
            {
                int connections = serviceManager.getConnections(service.getServiceName());
                System.out.printf("%-30s %-20s %-10s %n", service.getServiceName(), service.getServicePath(), connections);
            }
        }
    }


    private void shutdown()
    {
        System.out.println();
        System.out.println("You are about to shut down the edge router.");

        List<EndpointInfo> endpointInfos = serviceManager.getAllServices();
        int totalConnections = 0;
        for (EndpointInfo info : endpointInfos)
        {
            totalConnections += serviceManager.getConnections(info.getServiceName());
        }

        System.out.println();
        System.out.printf("Total connections: %30s", totalConnections);
        System.out.println();

        if (endpointInfos.size() > 0)
        {
            System.out.println();
            System.out.println("Currently deployed endpoints:");
            System.out.printf("%-30s %-20s %-10s %n", "Service Name", "Service Path", "Connections");
            for (EndpointInfo service : endpointInfos)
            {
                int connections = serviceManager.getConnections(service.getServiceName());
                System.out.printf("%-30s %-20s %-10s %n", service.getServiceName(), service.getServicePath(), connections);
            }
        }

        else
            System.out.println("There are no services deployed.");

        System.out.println();
        System.out.print("Are you sure (y/n): ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("y"))
        {
            System.out.println("Shutting down all services, and terminating...");
            serviceManager.removeAll();
            System.exit(1);
        }
    }


    private void stopService()
    {
        System.out.println();
        List<EndpointInfo> services = serviceManager.getAllServices();

        for (EndpointInfo service : services)
        {
            int connections = serviceManager.getConnections(service.getServiceName());
            System.out.printf("%-30s %-20s %-10s %n", service.getServiceName(), service.getServicePath(), connections);
        }

        System.out.println();
        System.out.print("Enter the name of the service to stop: ");
        String serviceName = scanner.nextLine();

        int connections = serviceManager.getConnections(serviceName);
        if (connections > 0)
        {
            System.out.println("There are still " + connections + " connections open to this service.");
            System.out.print("Are you sure you want to stop this service (y/n): ");
            String input = scanner.nextLine();
            if (!input.equalsIgnoreCase("y"))
            {
                System.out.println();
                System.out.println("Service stop cancelled.");
                return;
            }
        }

        try
        {
            serviceManager.removeService(serviceName);
        }

        catch (WsEndpointUndeploymentException e)
        {
            System.out.println("An error occurred when attempting to remove the service: " + e.getMessage());
            return;
        }

        System.out.println("Service: " + serviceName + " has been removed, and all connections have been closed.");
    }


    private void startService()
    {
        String serviceName;
        System.out.println();
        System.out.println("Endpoints deployed from the terminal can only be configured with default values\nUse file configuration for more features.");

        System.out.println();
        System.out.println("Please enter the following information to configure the service:");
        System.out.println();

        System.out.print("Service name (e.g., my-service): ");
        serviceName = scanner.nextLine();

        System.out.println();
        System.out.print("Enter the port number to use for the service (e.g., 8080): ");
        int servicePort = scanner.nextInt();

        scanner.nextLine(); // consume the newline character

        System.out.println();
        System.out.print("Enter the path for the service (e.g., /test/edgerouter-one): ");
        String servicePath = scanner.nextLine();

        System.out.println();
        System.out.print("Is the service public? (true/false): ");
        boolean isPublic = scanner.nextBoolean();

        scanner.nextLine(); // consume the newline character

        System.out.println();
        System.out.print("Enter the path to the keystore file (e.g., /path/to/keystore): ");
        String ksPath = scanner.nextLine();

        System.out.println();
        System.out.print("Enter the keystore password: ");
        String ksPassword = scanner.nextLine();

        System.out.println();
        System.out.print("Enter the path to the truststore file (e.g., /path/to/truststore): ");
        String tsPath = scanner.nextLine();

        System.out.println();
        System.out.print("Enter the truststore password: ");
        String tsPass = scanner.nextLine();

        EndpointInfo endpointInfo = new EndpointInfo.Builder(serviceName, servicePath, servicePort, isPublic, ksPath, ksPassword, tsPath, tsPass).build();

        try
        {
            serviceManager.addService(endpointInfo);
            System.out.println();
            System.out.println("Service registered successfully.");
            System.out.println("Service name: " + endpointInfo.getServiceName());
            System.out.println("Service path: " + endpointInfo.getServicePath());
            System.out.println("Service port: " + endpointInfo.getServicePort());
            System.out.println("Service is public: " + endpointInfo.isPublic());
        }

        catch (Exception ex)
        {
            System.out.println();
            System.out.println("An error occurred when attempting to register service: " + ex.getMessage());
        }
    }


    private void listSubscriptions()
    {
        System.out.println();
        System.out.println("Listing subscriptions...");

        HashMap<String, Integer> overview = subscriptionManager.getSubscriptionNumbers();
        HashMap<String, Integer> directMessages = subscriptionManager.getDirectMessageSubscriptionNumbers();

        if (overview.isEmpty() && directMessages.isEmpty())
        {
            System.out.println();
            System.out.println("There are no subscriptions.");
        }

        else
        {
            System.out.println();
            System.out.println("Subscription Table:");
            for (Map.Entry<String, Integer> entry : overview.entrySet())
            {
                System.out.printf("%-30s %-10s %n", entry.getKey(), entry.getValue());
            }
        }

        if (directMessages.isEmpty())
        {
            System.out.println("There are no direct message subscriptions.");
        }

        else
        {
            System.out.println();
            System.out.println("Direct Message Subscriptions:");
            for (Map.Entry<String, Integer> entry : directMessages.entrySet())
            {
                System.out.printf("%-30s %-10s %n", entry.getKey(), entry.getValue());
            }
        }
    }
}
