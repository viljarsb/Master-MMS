package MMS.EdgeRouter.RouterWebSocketManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class PingHandler
{
    private static final Logger logger = LogManager.getLogger(PingHandler.class);
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService workerPool;
    private final ConcurrentHashMap<String, PingRecord> pingRecordMap;
    private static final int PING_INTERVAL = 10;


    public PingHandler()
    {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.workerPool = Executors.newCachedThreadPool();
        this.pingRecordMap = new ConcurrentHashMap<>();
        scheduledExecutorService.scheduleAtFixedRate(this::pingClients, 0, PING_INTERVAL, TimeUnit.SECONDS);
    }


    public void addConnection(Session session)
    {
        PingRecord pingRecord = new PingRecord(session);
        this.pingRecordMap.put(session.getId(), pingRecord);
    }


    public void removeConnection(Session session)
    {
        this.pingRecordMap.remove(session.getId());
    }


    public void registerPong(Session session)
    {
        PingRecord pingRecord = this.pingRecordMap.get(session.getId());
        pingRecord.setLastPongTime(System.currentTimeMillis());
    }

    private void pingClients()
    {
        for (PingRecord pingRecord : this.pingRecordMap.values())
        {
            workerPool.execute(() -> pingClient(pingRecord));
        }
    }


    private void pingClient(PingRecord pingRecord)
    {
        int livesLeft = pingRecord.getLivesLeft();
        long lastPingTime = pingRecord.getLastPingTime();
        long lastPongTime = pingRecord.getLastPongTime();
        Session session = pingRecord.getSession();

        if (livesLeft == 0)
        {
            try
            {
                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Ping timeout"));
            }

            catch (IOException ex)
            {
                logger.error("Error closing session: " + ex.getMessage());
            }

            return;
        }

        if (lastPongTime > lastPingTime)
        {
            pingRecord.setLastPongTime(System.currentTimeMillis());
            return;
        }

        pingRecord.decrementLivesLeft();
        pingRecord.setLastPingTime(System.currentTimeMillis());

        try
        {
            session.getBasicRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
        }

        catch (IOException ex)
        {

        }
    }

}
