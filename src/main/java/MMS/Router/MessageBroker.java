package MMS.Router;

import MMS.MMTP.ApplicationMessage;
import MMS.MMTP.Subscribe;
import MMS.MMTP.Unsubscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageBroker
{
    private static final Logger logger = LogManager.getLogger(MessageBroker.class);
    private static MessageBroker instance;

    private final ConcurrentHashMap<String, List<ConnectionHandler>> routingTable;
    private final ExecutorService workerPool;


    private MessageBroker()
    {
        routingTable = new ConcurrentHashMap<>();
        workerPool = Executors.newCachedThreadPool();
        logger.info("Message broker started");
    }


    public static MessageBroker getBroker()
    {
        if (instance == null)
        {
            instance = new MessageBroker();
        }

        return instance;
    }


    public void addToRoutingTable(Subscribe subscribe, ConnectionHandler connectionHandler)
    {
        workerPool.execute(() ->
        {
            List<String> subjects = subscribe.getInterestsList();

            for(String subject : subjects)
            {
                if (!routingTable.containsKey(subject))
                {
                    routingTable.put(subject, Collections.singletonList(connectionHandler));
                }
                else
                {
                    List<ConnectionHandler> connectionHandlers = routingTable.get(subject);
                    connectionHandlers.add(connectionHandler);
                    routingTable.put(subject, connectionHandlers);
                }
            }
        });
    }


    public void removeFromRoutingTable(Unsubscribe unsubscribe, ConnectionHandler connectionHandler)
    {
        workerPool.execute(() ->
        {
            List<String> subjects = unsubscribe.getInterestsList();

            for (String subject : subjects)
            {
                if (routingTable.containsKey(subject))
                {
                    List<ConnectionHandler> connectionHandlers = routingTable.get(subject);
                    connectionHandlers.remove(connectionHandler);
                    if (connectionHandlers.isEmpty())
                    {
                        routingTable.remove(subject);
                    }
                    else
                    {
                        routingTable.put(subject, connectionHandlers);
                    }
                }
            }
        });
    }


    public void removeFromRoutingTable(ConnectionHandler connectionHandler)
    {
        workerPool.execute(() ->
        {
            routingTable.forEach((subject, connectionHandlers) ->
            {
                connectionHandlers.remove(connectionHandler);
                if (connectionHandlers.isEmpty())
                {
                    routingTable.remove(subject);
                }
                else
                {
                    routingTable.put(subject, connectionHandlers);
                }
            });
        });
    }


    public void routeMessage(ApplicationMessage message)
    {
        workerPool.execute(() ->
        {
            String destination;

            if (message.hasRecipient())
            {
                destination = message.getRecipient();
            }
            else if (message.hasSubject())
            {
                destination = message.getSubject();
            }
            else
            {
                logger.error("Message has no recipient or subject");
                return;
            }

            byte[] messageBytes = message.toByteArray();

            if (routingTable.containsKey(destination))
            {
                List<ConnectionHandler> connectionHandlers = routingTable.get(destination);
                connectionHandlers.forEach(connectionHandler -> workerPool.execute(() -> connectionHandler.send(messageBytes)));
            }
        });
    }
}
