package MMS.EdgeRouter.RouterWebSocketManager;

import javax.websocket.Session;

public class PingRecord
{
    private long lastPingTime;
    private long lastPongTime;
    private int livesLeft;
    private final Session session;

    public PingRecord(Session session)
    {
        this.lastPingTime = 0;
        this.lastPongTime = 0;
        this.livesLeft = 3;
        this.session = session;
    }

    public long getLastPingTime()
    {
        return this.lastPingTime;
    }

    public long getLastPongTime()
    {
        return this.lastPongTime;
    }

    public int getLivesLeft()
    {
        return this.livesLeft;
    }

    public void setLastPingTime(long lastPingTime)
    {
        this.lastPingTime = lastPingTime;
    }

    public void setLastPongTime(long lastPongTime)
    {
        this.lastPongTime = lastPongTime;
        this.livesLeft = 3;
    }

    public void getLivesLeft(int livesLeft)
    {
        this.livesLeft = livesLeft;
    }

    public void decrementLivesLeft()
    {
        this.livesLeft--;
    }

    public Session getSession()
    {
        return this.session;
    }
}
