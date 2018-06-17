import java.net.*;
import java.io.*;

public class CTransport extends CCommandParser implements CListenThreadListener
{
  private String username;

  // Listerner variables
  private CTransportListener listener;

  private CListenThread listen_thread = null;
  private Socket conn = null;
  private PrintStream out;

  CTransport( CTransportListener l )
  {
    clientProtocol(true);
    listener = l;
  }

  public void connect(String remoteHost, int port)
  {
    // Connect to the remote client
    try
    {
      conn = new Socket(remoteHost, port);
      // Create a listen thread
      listen_thread = new CListenThread(this, new InputStreamReader(conn.getInputStream()));
      // Create an output stream
      out = new PrintStream(conn.getOutputStream());
      listener.onConnect(); 
    }
    catch(IOException e)
    {
      listener.onConnectError(1, e.getMessage());
    }
  }

  public void authenticateUser (String username)
  {
    this.username = username;
    sendCommand(CMD_AUTHENTICATE, username);
  }

  public void sendCommand(int id, String data)
  {
    if(id < 10)
      out.println("+0" + id + data);
    else
      out.println("+" + id + data);
  }

  public void sendMessage(String username, String message)
  {
    sendCommand(CMD_MESSAGE_RECEIVED, username + "~" + message);
  }

  public void sendMessageToAll(String message)
  {
    sendCommand(CMD_ALL_MESSAGE_RECEIVED, message);
  }

  public void onDataReceived(String data)
  {
    parseData(data);
    switch(getCommand())
    {
      case CMD_AUTHENTICATE:
      {
        if(isValid())
        {
          listener.onUserValidated();
        }
        else
        {
          // The user was rejected so close the
          // connection
          try
          {
            conn.close();
          }
          catch(IOException e) { }
          listener.onUserRejected(getData());
        }
        break;
      }
      case CMD_USER_JOINED:
      {
        listener.onUserJoin(getUser());
        break;
      }
      case CMD_USER_LEFT:
      {
        listener.onUserLeave(getUser());
        break;
      }
      case CMD_MESSAGE_RECEIVED:
      {
        listener.onMessageReceived(getUser(), getData());
        break;
      }
      case CMD_ALL_MESSAGE_RECEIVED:
      {
        listener.onMessageReceivedFromAll(getUser(), getData());
        break;
      }
      case CMD_SERVER_SHUTDOWN:
      {
        listener.onLostConnection();
        break;
      }
    }
  }

  public void onDataError(int id, String description)
  {
    listener.onLostConnection();
  }
}