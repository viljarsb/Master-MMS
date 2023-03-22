package MMS.EdgeRouter;

import MMS.EdgeRouter.WebsocketServerManager.ConnectionHandler;
import MMS.EdgeRouter.WebsocketServerManager.ServerManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Status
{



    public static String getStatus()
    {
        ServerManager serverManager = ServerManager.getManager();

        LocalDateTime deploymentTime = serverManager.getDeploymentTime();
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(deploymentTime, currentTime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        ConnectionHandler connectionHandler = ConnectionHandler.getHandler();

        int totalConnections = connectionHandler.numberOfConnections();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDeploymentTime = deploymentTime.format(formatter);

        String status = "Deployment Time: " + formattedDeploymentTime + "\nUptime: " + days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds\nTotal Connections: " + totalConnections;
        return status;
    }


    public static String getShutdownStats()
    {
        int totalConnections = ConnectionHandler.getHandler().numberOfConnections();

        return "Total Agents connected: " + totalConnections + "\nTotal Routers connected: 0";
    }
}
