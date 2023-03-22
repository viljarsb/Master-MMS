package MMS.Router;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dummy implementation, not a real router, just a rabbit MQ server.
 */
public class Router
{
    private static final Logger logger = LogManager.getLogger(Router.class);

    private static ServerSocket serverSocket;
    private static ConcurrentHashMap<String, ConnectionHandler> connectionHandlers;


    public Router(String inetAddress, int port)
    {
        try
        {
            serverSocket = TLSConfigurator.getSecureSeverSocket(inetAddress, port);
            connectionHandlers = new ConcurrentHashMap<>();
        }

        catch (Exception e)
        {
            logger.fatal("Error creating server socket", e);
            System.exit(1);
        }
    }


    private void awaitConnections()
    {
        while (true)
        {
            try
            {
                Socket connection = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(connection);
                String connectionId = UUID.randomUUID().toString();
                connectionHandlers.put(connectionId, connectionHandler);
                new Thread(connectionHandler).start();
            }

            catch (IOException e)
            {
                logger.error("Error accepting connection", ex);
            }
        }
    }

    public static void removeConnection(String connectionId)
    {
        connectionHandlers.remove(connectionId);
    }

    public static void main(String[] args)
    {
        Router router = new Router("123", 123);
        router.awaitConnections();
    }
}
