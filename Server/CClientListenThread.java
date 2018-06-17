import java.net.*;
import java.io.*;

class CClientListenThread extends Thread
{
  // Constants
  public static final int CMD_AUTHENTICATE         = 1;
  public static final int CMD_USER_JOINED          = 2;
  public static final int CMD_USER_LEFT            = 3;
  public static final int CMD_MESSAGE_RECEIVED     = 4;
  public static final int CMD_ALL_MESSAGE_RECEIVED = 5;
  public static final int CMD_SERVER_SHUTDOWN      = 6; 

  protected CClientListenListener listener;
  protected Socket conn;
  public String username;
  protected PrintStream out;
  protected BufferedReader in;
  protected boolean running;
  protected CCommandParser cmd = new CCommandParser();

  public CClientListenThread(CClientListenListener l, Socket s)
  {
    cmd.clientProtocol(false);
    listener = l;
    conn   = s;
    running = true;
    username = "";
    // Create an output stream
    try
    {
      in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      out = new PrintStream(conn.getOutputStream());
    }
    catch(IOException e)
    {
      try
      {
        conn.close();
      }
      catch (IOException e2) {}
      System.err.println("Exception while getting socket streams");
      return;
    }

    this.start();
  }

  public void run()
  {
    String buffer = "";

    while(running)
    {
      try
      {
        buffer = in.readLine();
        // Make sure the connection wasn't dropped
        if(buffer == null)
        {
          try
          {
            conn.close();
          }
          catch (IOException e3) {}
          conn = null;
          // Call the user left event
          listener.onUserLeave(this);
          return;
        }
        // Process the data received
        cmd.parseData(buffer);
        switch(cmd.getCommand())
        {
          case CMD_AUTHENTICATE:
          {
            listener.onUserAuthenticate(this, cmd.getUser());
            break;
          }
          case CMD_MESSAGE_RECEIVED:
          {
            listener.onUserSendMessage(this, cmd.getUser(), cmd.getData());
            break;
          }
          case CMD_ALL_MESSAGE_RECEIVED:
          {
            listener.onUserSendMessageToAll(this, cmd.getData());
            break;
          }
        }
      }
      catch (IOException e)
      {
        // There was an error so close the connection
        // to this client
        running = false;
        try
        {
          if(conn != null)
            conn.close();
        }
        catch (IOException e2) {}
        conn = null;
        // Call the user left event
        listener.onUserLeave(this);
      }
    }
  }

  public void sendMessage(String from, String message)
  {
    sendCommand(true, CMD_MESSAGE_RECEIVED, from + "~" + message);
  }

  public void sendMessageToAll(String from, String message)
  {
    sendCommand(true, CMD_ALL_MESSAGE_RECEIVED, from + "~" + message);
  }

  public void sendCommand(boolean valid, int id, String data)
  {
    String command;

    if(valid)
      command = "+";
    else
      command = "-";

    if(id < 10)
      command += "0" + id;
    else
      command += id;

    command += data;

    out.println(command);
      
  }

  public void authenticate(String username)
  {
    this.username = username;
    sendCommand(true, CMD_AUTHENTICATE, "");
  }

  public void reject(String reason)
  {
    sendCommand(false, CMD_AUTHENTICATE, reason);
  }

  public void userJoined(String username)
  {
    sendCommand(true, CMD_USER_JOINED, username);
  }

  public void userLeft(String username)
  {
    sendCommand(true, CMD_USER_LEFT, username);
  }

  public void shutDown()
  {
    // Inform the client of the shutdown
    sendCommand(true, CMD_SERVER_SHUTDOWN, "");
    // Close the connection
    running = false;
    try
    {
      conn.close();
    }
    catch (IOException e) {}
    conn = null;
  }
}