package MMS.Router;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable
{
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private final Socket connection;
    private final BufferedInputStream input;
    private final BufferedOutputStream output;
    private final MessageHandler messageHandler;


    public ConnectionHandler(Socket connection) throws IOException
    {
        this.connection = connection;

        input = new BufferedInputStream(connection.getInputStream());
        output = new BufferedOutputStream(connection.getOutputStream());
        messageHandler = MessageHandler.getHandler();
    }


    @Override
    public void run()
    {
        logger.info("Connection handler for connection {} started", connection.getInetAddress());

        while(!connection.isClosed())
        {
            read();
        }

        Router.removeConnection(this);
    }



    private void read()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try
        {
            int bytesRead = input.read(buffer);
            while (bytesRead != -1)
            {
                baos.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
                messageHandler.handle(buffer);
            }
        }
        catch (IOException e)
        {
            logger.error("Error reading data from client", e);
        }
    }


    public void close()
    {
        try
        {
            connection.close();
        }
        catch (IOException e)
        {
            logger.error("Error closing connection", e);
        }
    }


    public void send(byte[] message)
    {
        try
        {
            output.write(message);
            output.flush();
        }

        catch (IOException e)
        {
            logger.error("Error sending data to client", e);
        }
    }
}
