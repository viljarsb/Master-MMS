package MMS.Client;

import MMS.Client.Connections.*;
import MMS.Client.Exceptions.AgentInitException;
import MMS.Client.ServiceDiscovery.RouterInfo;
import MMS.Client.ServiceDiscovery.ServiceDiscoveryListener;
import MMS.Client.TLSConfiguration.TLSConfig;
import MMS.Client.TLSConfiguration.mTLSConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.*;

public class Agent implements ConnectionListener
{
    private static final Logger logger = LogManager.getLogger(Agent.class);
    private static ExecutorService workerPool;
    private static ServiceDiscoveryListener serviceDiscoveryListener;
   // private final SubscriptionManager subscriptionManager;
    private static WsHandler wsHandler;

    private AgentAdapter adapter;
    private Connection connection;
    private AgentStatus status;


    private Agent(Class<? extends AgentAdapter> adapterImpl) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        this.adapter = adapterImpl.getDeclaredConstructor(ConnectionListener.class).newInstance(this);
        ConnectionListener connectionListener = this;
        this.status = AgentStatus.NOT_CONNECTED;
    }


    public static Agent getInstance(Class<? extends AgentAdapter> adapterImpl) throws AgentInitException
    {
        try
        {
            if (workerPool == null)
                workerPool = Executors.newCachedThreadPool();

            if (serviceDiscoveryListener == null)
                serviceDiscoveryListener = new ServiceDiscoveryListener();

            if (wsHandler == null)
                wsHandler = new WsHandler();

            return new Agent(adapterImpl);
        }

        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException e)
        {
            throw new AgentInitException("Failed to initialize agent");
        }
    }


    public Future<List<RouterInfo>> discover()
    {
        return workerPool.submit(() -> serviceDiscoveryListener.listen());
    }


    public List<RouterInfo> discoverSync()
    {
        return serviceDiscoveryListener.listen();
    }


    public void connectAnonymously(RouterInfo routerInfo, TLSConfig tlsConfig, Class<? extends AgentAdapter> adapterClass)
    {
        try
        {
            AgentAdapter adapter = adapterClass.getDeclaredConstructor().newInstance();
            Runnable runnable = () -> this.connection = wsHandler.connectAnonymously(routerInfo, tlsConfig, adapter);
            delegateToWorker(runnable);
        }


        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            //
        }
    }


    public void connectAuthenticated(RouterInfo routerInfo, mTLSConfig tlsConfig, Class<AgentAdapter> adapterClass)
    {
        try
        {
            AgentAdapter adapter = adapterClass.getDeclaredConstructor().newInstance();
            Runnable runnable = () -> this.connection = wsHandler.connectAuthenticated(routerInfo, tlsConfig, adapter);
            delegateToWorker(runnable);
        }

        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            //
        }
    }


    public void disconnect()
    {
        Runnable runnable = () ->
        {
            if (this.connection.isConnected())
            {
                this.connection.close();
            }
        };

        delegateToWorker(runnable);
    }


    public void subscribe(String subject) throws SubscriptionException, NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

        //subscriptionManager.subscribe(subject, session);

    }


    public void subscribe(List<String> subjects) throws SubscriptionException, NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

//        subscriptionManager.subscribe(subjects, session);
    }


    public void unsubscribe(String subject) throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

  //      subscriptionManager.unsubscribe(subject, session);
    }


    public void unsubscribe(List<String> subjects) throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

    //    subscriptionManager.unsubscribe(subjects, session);
    }


    public List<String> getSubscriptions()
    {
       // return subscriptionManager.getSubscriptions();
    }


    private void delegateToWorker(Runnable runnable)
    {
        workerPool.execute(runnable);
    }


    @Override
    public void onConnectionEstablished(Connection connection)
    {
        if(connection instanceof AuthenticatedConnection)
        {
            this.connection = connection;
            this.status = AgentStatus.CONNECTED_AUTHENTICATED;
            logger.info("Agent successfully connected to router in authenticated mode.");
        }

        else if(connection instanceof AnonymousConnection)
        {
            this.connection = connection;
            this.status = AgentStatus.CONNECTED_ANONYMOUS;
            logger.info("Agent successfully connected to router in anonymous mode.");
        }

        else
        {
            this.connection = null;
            this.status = AgentStatus.NOT_CONNECTED;
        }
    }


    @Override
    public void onConnectionLost()
    {
        this.connection = null;
        this.status = AgentStatus.LOST_CONNECTION;
        logger.error("The connection to the router was lost.");
    }


    @Override
    public void onDisconnect(DisconnectionReason reason)
    {
        this.connection = null;
        this.status = AgentStatus.NOT_CONNECTED;
        logger.info("The agent was disconnected from the router.");
        this.adapter.onDisconnect();
    }
}

