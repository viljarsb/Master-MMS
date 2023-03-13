package MMS.EdgeRouter.UserInterface;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.ServiceRegistry.EdgeRouterService;
import MMS.EdgeRouter.ServiceRegistry.ServiceRegistry;
import MMS.EdgeRouter.WebSocketManager.ConnectionHandler;
import MMS.EdgeRouter.WebSocketManager.WebSocketManager;
import org.apache.commons.cli.*;

import java.security.Provider;
import java.util.Scanner;


public class CommandLineInterface
{
    private Scanner scanner;
    private Options options;
    private CommandLineParser parser;
    private static CommandLineInterface instance;
    private WebSocketManager webSocketManager;
    private ServiceBroadcastManager serviceBroadcastManager;
    private ServiceRegistry serviceRegistry;



    private CommandLineInterface()
    {
        webSocketManager = WebSocketManager.getManager();
        serviceBroadcastManager = ServiceBroadcastManager.getManager();

        scanner = new Scanner(System.in);
        options = new Options();
        parser = new DefaultParser();

        initOptions();
    }


    public static CommandLineInterface getCLI()
    {
        if (instance == null)
        {
            instance = new CommandLineInterface();
        }
        return instance;
    }


    private void initOptions()
    {
        options.addOption("h", "help", false, "Prints a list of commands");
        options.addOption("sd", "shutdown", false, "Shuts down the Edge Router");
        options.addOption("ls", "list-services", false, "Lists all services");

        Option deployService = Option.builder("ds")
                .argName("<service-name> <service-path> <service-port> <is-public>")
                .desc("Starts a new service")
                .longOpt("deploy-service")
                .numberOfArgs(4)
                .build();

        options.addOption(deployService);
    }


    public void start()
    {
        System.out.println("Welcome to the Edge Router CLI");
        System.out.println("Type 'help' for a list of commands");

        while (true)
        {
            System.out.print("MMS-EDGE-ROUTER>\t");
            String input = scanner.nextLine();

            try
            {
                CommandLine cmd = parser.parse(options, input.split("\\s+"));

                if (cmd.hasOption("h"))
                {
                    printHelp();
                }


                else if (cmd.hasOption("sd"))
                {
                    shutdown();
                }

                else if (cmd.hasOption("ls"))
                {
                   listServices();
                }

                else if (cmd.hasOption("ds"))
                {
                    startService(cmd.getOptionValues("ds"));
                }

                else
                {
                    System.out.println(input + " is not a valid command");
                }
            }

            catch (ParseException e)
            {
                System.out.println("Invalid command");
            }
        }
    }


    private void printHelp()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MMS-EDGE-ROUTER", options);
    }


    private void startService(String[] args)
    {
        if (args == null || args.length != 4)
        {
            System.out.println("Invalid number of arguments, expected 4");
            System.out.println("Usage: deploy-service <service-name> <service-path> <service-port> <is-public>");
            return;
        }

        String name = args[0];
        String path = args[1];

        if(!path.startsWith("/"))
        {
            System.out.println("Path must start with a forward slash");
            return;
        }


        int port;
        boolean isPublic;

        try
        {
            port = Integer.parseInt(args[2]);

            if (port < 1 || port > 65535)
            {
                System.out.println("Port must be between 1 and 65535");
                return;
            }

            isPublic = Boolean.parseBoolean(args[3]);
        }

        catch (NumberFormatException e)
        {
            System.out.println("Port must be an integer between 1 and 65535");
            return;
        }

        catch (IllegalArgumentException e)
        {
            System.out.println("isPublic must be either true or false");
            return;
        }


        if (isPublic)
        {
            System.out.println("Starting public service " + name + " on port " + port + " with path " + path);

            try
            {
                webSocketManager.deployEndpoint(port, path, 10);
                serviceBroadcastManager.broadcastService(name, port, path);
            }

            catch (Exception ex)
            {
                System.out.println("Failed to start service: " + ex.getMessage());
            }
        }

        else
        {
            System.out.println("Starting private service " + name + " on port " + port + " with path " + path);

            try
            {
                webSocketManager.deployEndpoint(port, path, 10);
            }

            catch (Exception ex)
            {
                System.out.println("Failed to start service: " + ex.getMessage());
            }
        }

        EdgeRouterService service = new EdgeRouterService(name, path, port, isPublic);
        serviceRegistry.addService(service);
    }


    private void shutdown()
    {
        System.out.println("The following action will shut down the Edge Router");
        System.out.print("Are you sure you want to proceed? (y/n):\t");
        String answer = scanner.nextLine();

        if (answer.equals("y"))
        {
            System.out.println("Shutdown initiated");
            System.exit(0);
        }

        else
        {
            System.out.println("Shutdown aborted");
        }
    }


    private void listServices()
    {
        System.out.println("Listing all services");
        System.out.println(serviceRegistry.getAllServices());
    }
}
