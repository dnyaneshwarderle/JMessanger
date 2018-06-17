import java.net.*;
import java.io.*;

class CListenThread extends Thread
{
  protected CListenListener listener;

  protected boolean shutdown;
  protected boolean running;
  protected ServerSocket listen_socket;
  protected int port;

  public CListenThread(CListenListener l, int port)
  {
    listener = l;
    shutdown = false;
    running = false;
    this.port = port;

    this.start();
  }

  public void run()
  {
    Socket new_client = null;

    // Keep listening until the user exits
    while(!shutdown)
    {
      if(running)
      {
        // See if there are any new connections
        try
        {
          new_client = listen_socket.accept();
        }
        catch (IOException e)
        {
          new_client = null;
        }
        if(new_client != null)
        {
          // Create the new connection event
          listener.onNewConnection(new_client);
        }
      }
    }
  }

  public void shutDown()
  {
    shutdown = true;
    // Disable the server listen state
    setServerRunState(false);
  }

  public void setServerRunState(boolean running)
  {
    // Only do something if the state has changed
    if(this.running != running)
    {
      if(running)
      {
        // Create the listen socket
        try
        {
          listen_socket = new ServerSocket(port);
        }
        catch(IOException e)
        {
          // There was an error to raise an event
          listener.onListenError(e.getMessage()); 
        }
        // Set the listen timeout to a 10th of a second
        try
        {
          listen_socket.setSoTimeout(100);
          // This was successful, call the success event
          listener.onListen();
        }
        catch (IOException e)
        {
          // Raise an error event
          listener.onListenError(e.getMessage());
        }
      }
      else
      {
        // Close the listen socket
        try
        {
          listen_socket.close();
          listen_socket = null;
          listener.onClose();
        }
        catch (IOException e)
        {
          // Raise the error
          listener.onListenError(e.getMessage());
        }
      }
      this.running = running;
    }
  }
}