package MMS.EdgeRouter;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.WebsocketServerManager.ConnectionHandler;
import MMS.EdgeRouter.WebsocketServerManager.ServerManager;
import org.apache.commons.cli.*;

import java.util.Scanner;

public class CommandLineInterface
{
    private Scanner scanner;
    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private static CommandLineInterface instance;

    private CommandLineInterface()
    {
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
        options.addOption("st", "status", false, "Get the status of the Edge Router.");
        options.addOption("sd", "shutdown", false, "Shutdown the Edge Router.");
    }

    public void start()
    {
        while (true)
        {
            System.out.print("Edge Router> ");
            String input = scanner.nextLine();
            String[] args = input.split(" ");

            try
            {
                cmd = parser.parse(options, args);

                if(cmd.hasOption("st"))
                {
                    status();
                }

                else if(cmd.hasOption("sd"))
                {
                    shutdown();
                }

                else
                {
                    System.out.println("Invalid command: " + input);
                }
            }

            catch (Exception ex)
            {
                System.out.println("Invalid command.");
            }
        }
    }

    private void status()
    {
        System.out.println();
        System.out.println("Edge Router Status:");
        System.out.println(Status.getStatus());
        System.out.println();
    }


    private void shutdown()
    {
        System.out.println();
        System.out.println(Status.getShutdownStats());
        System.out.print("You are about to shutdown the Edge Router, are you sure? (y/n):\t");
        String input = scanner.nextLine();

        if(input.equals("y"))
        {
            System.out.println("Shutting down...");
            ServiceBroadcastManager.getManager().stopBroadcastingService();
            ServerManager.getManager().undeployServer();
            System.out.println("Shutdown complete, terminating...");
            System.exit(0);
        }

        else
        {
            System.out.println("Shutdown aborted.");
            System.out.println();
        }
    }


}
