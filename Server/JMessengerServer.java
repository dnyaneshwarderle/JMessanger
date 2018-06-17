import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class JMessengerServer
  implements CMenuListener, CListenListener, CClientListenListener
{
  public static final int DEFAULT_PORT = 1000;
  public static final String LOG_FILE = "JMessenger.log";

  private int port;
  private CMenuThread menu;
  private CListenThread listen;
  private LinkedList clients = new LinkedList();
  private boolean bLogging = false;
  private FileOutputStream log;

  public JMessengerServer(int port)
  {
    if(port == 0)
      this.port = DEFAULT_PORT;
    else
      this.port = port;

    // Create a new text menu
    menu = new CMenuThread(this);
    // Create a listen thread
    listen = new CListenThread(this, this.port);
  }

  public void onServerRunning(boolean enable)
  {
    if(!enable)
    {
      // Inform all clients that the server is stoping
      for(int i = 0; i < clients.size(); i++)
        ((CClientListenThread)clients.get(i)).shutDown();
      clients.clear();

      writeLog("Server has stopped running");
    }
    else
    {
      writeLog("Server has started running");
    }

    listen.setServerRunState(enable);
      
  }

  public void onShutdown()
  {
    // Inform all clients that the server is shutting down
    for(int i = 0; i < clients.size(); i++)
      ((CClientListenThread)clients.get(i)).shutDown(); 
    menu.shutDown();
    listen.shutDown();
    writeLog("Server shut down");
    closeLogFile();
  }

  public void onListen()
  {
    menu.setServerRunState(true);
  }

  public void onClose()
  {
    menu.setServerRunState(false);
  }

  public void onNewConnection(Socket s)
  {
    // Add this connection to the list of users
    clients.add(new CClientListenThread(this, s));
    writeLog("User connected at address " + s.getRemoteSocketAddress().toString());
  }

  public void onListenError(String description)
  {
    System.out.println("Error: " + description);
    writeLog("Error - " + description);
  }

  public void onUserLeave(CClientListenThread c)
  {
    // Remove this user
    clients.remove(c);

    // Inform all the other users that this user has left provided they
    // were authenticated
    if(!c.username.equals(""))
      for(int i = 0; i < clients.size(); i++)
        ((CClientListenThread)clients.get(i)).userLeft(c.username);
    writeLog("User " + c.username + " has left");  
  }

  public void onUserAuthenticate(CClientListenThread c, String username)
  {
    boolean valid = true;
    // The user has request authentication so make sure the username
    // isn't already taken
    for(int i = 0; i < clients.size(); i++)
      if(((CClientListenThread)clients.get(i)).username.equalsIgnoreCase(username))
        valid = false;

    if(valid)
    {
      c.authenticate(username);
      // Inform all the users that this user has joined
      for(int i = 0; i < clients.size(); i++)
      {
        // Let users who isn't the new one know of the new member
        if(!((CClientListenThread)clients.get(i)).username.equalsIgnoreCase(username))
          ((CClientListenThread)clients.get(i)).userJoined(username);
        // Let the new user know who everyone else is
        c.userJoined(((CClientListenThread)clients.get(i)).username);
      }
      writeLog("user " + username + " has joined");   
    }
    else
    {
      c.reject("Username already taken");
      // Remove the client from the list
      clients.remove(c);
      writeLog("User " + username + " was rejected because the name was already taken");
    }
  }

  public void onUserSendMessage(CClientListenThread c, String to, String message)
  {
    // Loop through each client until we have the correct user
    for(int i = 0; i < clients.size(); i++)
    {
      if(((CClientListenThread)clients.get(i)).username.equalsIgnoreCase(to))
      {
        ((CClientListenThread)clients.get(i)).sendMessage(c.username, message);
      }
    }
    writeLog(c.username + " sent message to " + to + "\r\n" + message);
  }

  public void onUserSendMessageToAll(CClientListenThread c, String message)
  {
    // Loop through each client and send the message
    for(int i = 0; i < clients.size(); i++)
      ((CClientListenThread)clients.get(i)).sendMessageToAll(c.username, message);
    writeLog(c.username + " sent to all\r\n" + message);
  }

  public void onLogging(boolean enable)
  {
    bLogging = enable;
    if(bLogging)
    {
      // Open the log file and append contents
      try
      {
        log = new FileOutputStream(LOG_FILE, true);
      }
      catch (IOException e) {
        bLogging = false;
      }
    }
    else
    {
      closeLogFile();
    }
  }

  public void onLogFileClear()
  {
    File f;

    // Close the log file if it is open
    if(bLogging)
      closeLogFile();

    f = new File(LOG_FILE);
    f.delete();

    // Reopen the log file if necessary
    if(bLogging)
    {
      try {
        log = new FileOutputStream(LOG_FILE, true);
      }
      catch (IOException e)
      {
        bLogging = false;
      }
    }     
  }

  public void onViewLogFile()
  {
    try
    {
      File f = new File(LOG_FILE);
      int size = (int)f.length();
      FileInputStream file = new FileInputStream(f);
      for (int i = 0; i < size; i++)
        System.out.print((char)file.read());
      file.close();
    }
    catch (IOException e) {
      System.out.println("Could not read " + LOG_FILE);
    }

    menu.anyKeyContinue();    
  }

  public void onShowConnectedUsers()
  {
    if(clients.size() == 0)
    {
      System.out.println("There are no users connected");
    }
    else
    {
      // Loop through each client and output their details
      for(int i = 0; i < clients.size(); i++)
      {
        System.out.println(((CClientListenThread)clients.get(i)).username);
      }
    }

    menu.anyKeyContinue();

  }

  private void writeLog(String message)
  {
    if (bLogging)
    {
      String output;
      Date time = new Date();
      output = DateFormat.getDateInstance().format(time) + " " +
               DateFormat.getTimeInstance().format(time) +
               ": " + message + "\r\n";

      try
      {
        log.write(output.getBytes());
      } catch (IOException e) {}
    }
  }

  private void closeLogFile()
  {
    if(bLogging)
    {
      // Close the log file
      try
      {
        log.close();
      }
      catch (IOException e)
      {
        bLogging = true;
      }
    }
  }


  public static void main (String[] args)
  {
    int port = 0;
    if (args.length == 1)
    {
      try
      {
        port = Integer.parseInt (args[0]);
      }
      catch (NumberFormatException e)
      {
        port = 0;
      }
    }

    new JMessengerServer (port);
  }
}