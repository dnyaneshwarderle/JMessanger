import javax.swing.*;
import java.util.*;

public class JMessenger implements
  CTransportListener, CChatListener, CLoginListener,
  CPrivateMessageListener
{
  private CTransport conn;
  private JLogin login = null;
  private String username;
  private JChat chat = null;
  private LinkedList privateMessages = new LinkedList();

  public JMessenger()
  {
    // Create a new connection
    conn = new CTransport(this);
    login = new JLogin(this, "JMessenger Login");
    chat = new JChat(this, "JMessenger 1.0");

    // Show the login window
    login.setVisible(true);
  }

  public void onConnect()
  {
    conn.authenticateUser(username);
  }

  public void onConnectError(int id, String description)
  {
    login.setStatus(description);
  }

  public void onUserValidated()
  {
    // Show the conversation window
    login.setVisible(false);
    chat.setTitle("JMessenger 1.0 - Logged in as " + username);
    chat.showChat();
  }

  public void onUserRejected(String reason)
  {
    login.setStatus(reason);
  }

  public void onUserJoin(String username)
  {
    chat.userJoined(username);
  }

  public void onUserLeave(String username)
  {
    chat.userLeft(username);
    // See if there were any private messages to this
    // user to let them know the user left
    for(int i = 0; i < privateMessages.size(); i++)
    {
      if(((JPrivateMessage)privateMessages.get(i)).remoteUsername.equalsIgnoreCase(username))
      {
        ((JPrivateMessage)privateMessages.get(i)).userLeft();
        break;
      }
    }
  }

  public void onMessageReceived(String from, String message)
  {
    JPrivateMessage pm;

    boolean found = false;
    // See if there is already a private message to this user
    // and if so post the message to the window
    for(int i = 0; i < privateMessages.size(); i++)
    {
      if(((JPrivateMessage)privateMessages.get(i)).remoteUsername.equalsIgnoreCase(from))
      {
        found = true;
        ((JPrivateMessage)privateMessages.get(i)).messageReceived(message);
      }
    }

    if(!found)
    {
      // Create a new private message window and post the message
      
      pm = new JPrivateMessage(this, "Private Message with " + from, this.username, from);
      pm.messageReceived(message);
      privateMessages.add(pm);
    }

  }

  public void onMessageReceivedFromAll(String from, String message)
  {
    chat.messageReceived(from, message);
  }

  public void onSendMessageError(int id, String description)
  {
    System.out.println("Send error: " + description);
  }

  public void onLostConnection()
  {
  System.out.println("Lost connection");
    // We have lost the connection to the server, hide any visible windows
    // and show the login screen again
    chat.setVisible(false);
    // Close all private message windows
    for(int i = 1; i < privateMessages.size(); i++)
      ((JPrivateMessage)privateMessages.get(i)).dispose();
    privateMessages.clear();

    login.setVisible(true);
    login.setStatus("Lost server connection");
 }

  public void onSendMessageToAll(String message)
  {
    conn.sendMessageToAll(message);
  }

  public void onSendMessage(String to, String message)
  {
    conn.sendMessage(to, message);
  }

  public void onInitiatePrivateMessage(String username)
  {
    // Make sure the user isn't trying to talk to themselves
    if(!username.equalsIgnoreCase(this.username))
    {
      boolean found = false;
      // See if there is already a private message to this user
      // and if so, bring it to the front
      for(int i = 0; i < privateMessages.size(); i++)
      {
        if(((JPrivateMessage)privateMessages.get(i)).remoteUsername.equalsIgnoreCase(username))
        {
          found = true;
          ((JPrivateMessage)privateMessages.get(i)).toFront();
        }
      }

      if(!found)
      {
        // Create a new private message window
        privateMessages.add(new JPrivateMessage(this, "Private Message with " + username, this.username, username));
      }
    }
  }

  public void onClosePrivateMessage(String remoteUsername)
  {
    // Remote the private message
    for(int i = 0; i < privateMessages.size(); i++)
    {
      // See if this is the one to remove
      if(((JPrivateMessage)privateMessages.get(i)).remoteUsername.equalsIgnoreCase(remoteUsername))
      {
        // Dispose of the window and remove it
        ((JPrivateMessage)privateMessages.get(i)).dispose();
        privateMessages.remove(i);
        break;
      }
    }  }

  public void onConnectRequest(String host, int port, String username)
  {
    login.setStatus("Connecting...");
    this.username = username;
    conn.connect(host, port);
  }

  public void onLoginCancel()
  {
    System.exit(0);
  }

  public static void main(String[] args)
  {
    // Create the main application
    JMessenger main = new JMessenger();
  }
}