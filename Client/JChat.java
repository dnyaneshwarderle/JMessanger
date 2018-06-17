import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class JChat extends JFrame implements ActionListener
{
  private JEditorPane   txtConversation = new JEditorPane();
  private JTextArea     txtSend = new JTextArea();
  private JButton       btnSend = new JButton("Send");
  private JList         lstUsers;
  private CChatListener listener;
  private JScrollPane   scrlConversation;
  private DefaultListModel lstUserList = new DefaultListModel();

  // Menu items
  private JMenuBar topMenu;
  private JMenu loginMenu, helpMenu;
  private JMenuItem menuItemLogin, menuItemSettings, menuItemExit, menuItemAbout;

  // Message Data items
  private String messageHeader = "<html><body bgcolor=\"#FFFFFF\" text=\"#000000\" " +
                                 "leftmargin=\"5\" topmargin=\"5\" marginwidth=\"5\" marginheight=\"5\">";

  private String messageFooter = "</body></html>";
  private String messages = "";

  public JChat(CChatListener l, String title)
  {
    super(title);

    listener = l;

    // Setup the controls
    controlsSetup();
    // Setup the menu
    menuSetup();
    pack();

    // Treat pressing the [x] as logging out
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }

  private void controlsSetup()
  {
    // Create the conversation label
    JLabel lblConversation = new JLabel("Conversation:");

    // Create the conversation text area
    txtConversation.setEditable(false);
    txtConversation.setContentType("text/html");
    txtConversation.setText("<html><p style=\"font-family:verdana\">Welcome</p></html>");
    // Create the conversation scroll pane
    scrlConversation = new JScrollPane(txtConversation);
    scrlConversation.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrlConversation.setPreferredSize(new Dimension(400,300));
    scrlConversation.setMinimumSize(new Dimension(10, 10));    

    // Create the lefttop pane
    JPanel lefttopPane = new JPanel();
    BoxLayout lefttopBox = new BoxLayout(lefttopPane, BoxLayout.Y_AXIS);
    lefttopPane.setLayout(lefttopBox);
    lefttopPane.add(lblConversation);
    lefttopPane.add(scrlConversation);

    // Create the send label
    JLabel lblSend = new JLabel("Send Message:");

    // Create the send text scroll pane and the send button
    JScrollPane scrlSend = new JScrollPane(txtSend);
    scrlSend.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrlSend.setPreferredSize(new Dimension(330, 30));
    scrlSend.setMinimumSize(new Dimension(10, 10));

    // Set the send button properties
    btnSend.setPreferredSize(new Dimension(70,30));
    btnSend.setMinimumSize(new Dimension(10, 10));
    btnSend.addActionListener(this);

    // Set the send pane
    JPanel sendPane = new JPanel();
    BoxLayout sendBox = new BoxLayout(sendPane, BoxLayout.X_AXIS);
    sendPane.setLayout(sendBox);
    sendPane.add(scrlSend);
    sendPane.add(btnSend);

    // Create the leftbottom pane
    JPanel leftbottomPane = new JPanel();
    BoxLayout leftBottomBox = new BoxLayout(leftbottomPane, BoxLayout.Y_AXIS);
    leftbottomPane.setLayout(leftBottomBox);
    leftbottomPane.add(lblSend);
    leftbottomPane.add(sendPane);

    // Create the left split pane
    JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                          lefttopPane,
                                          leftbottomPane);
    leftSplitPane.setOneTouchExpandable(false);
    leftSplitPane.setDividerLocation(300);
    leftSplitPane.setPreferredSize(new Dimension(400, 400));

    // Create the list on the right hand side
    lstUsers = new JList(lstUserList);
    lstUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrlContacts = new JScrollPane(lstUsers);
    scrlContacts.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrlContacts.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrlContacts.setPreferredSize(new Dimension(150, 400));
    scrlContacts.setMinimumSize(new Dimension(10, 10));

    // Create a split pane between the conversation and the list
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                          leftSplitPane,
                                          scrlContacts);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(400);
    splitPane.setPreferredSize(new Dimension(550, 400));

    setContentPane(splitPane);

    // Setup a mouse listener to trap double clicks
    // on a list item
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int index = lstUsers.locationToIndex(e.getPoint());
          listener.onInitiatePrivateMessage((String)lstUserList.get(index));
        }
      }
   };
   lstUsers.addMouseListener(mouseListener);

  }

  public void menuSetup()
  {
    topMenu = new JMenuBar();
    setJMenuBar(topMenu);
    loginMenu = new JMenu("Login");
    menuItemLogin = new JMenuItem("Change user");
    loginMenu.add(menuItemLogin);
    menuItemExit = new JMenuItem("Exit");
    loginMenu.add(menuItemExit);
    menuItemLogin.addActionListener(this);
    menuItemExit.addActionListener(this);
    topMenu.add(loginMenu);
    helpMenu = new JMenu("Help");
    menuItemAbout = new JMenuItem("About...");
    menuItemAbout.addActionListener(this);
    helpMenu.add(menuItemAbout);
    topMenu.add(helpMenu);
  }

  public void actionPerformed(ActionEvent e)
  {
    if(e.getSource() == btnSend)
    {
      // Replace all new lines with <br> tags when we send the data
      listener.onSendMessageToAll(txtSend.getText().replaceAll("\n", "<br>"));
      txtSend.setText("");
    }
    if(e.getSource() == menuItemExit)
    {
      System.exit(0);
    }
    if(e.getSource() == menuItemAbout)
    {
      JOptionPane.showMessageDialog(null, "Created by Martin Adams", "JMessenger", JOptionPane.INFORMATION_MESSAGE);
    }
    if(e.getSource() == menuItemLogin)
    {
      // Disconnect and show the login menu again
    }
  }

  public void showChat()
  {
    // Clear the list and reset the message header
    lstUserList.clear();
    messages = "";
    txtConversation.setText(messageHeader + messageFooter);
    txtSend.setText("");

    setVisible(true);
  }

  public void messageReceived(String from, String message)
  {
   addMessage("<b><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"2\" color=\"#000066\">" +
               from + ": </font></b>" +
              "<font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"2\" color=\"#006699\">" +
              message + "</font><br>");
  }

  private void addMessage(String message)
  {
    messages += message;
    txtConversation.setText(messageHeader + messages + messageFooter);
    txtConversation.selectAll();
    txtConversation.setCaretPosition(txtConversation.getSelectedText().length());
  }

  public void userJoined(String username)
  {
    lstUserList.addElement(username);
   addMessage("<b><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"2\" color=\"#990000\">" +
               "User " + username + " has joined!</font></b><br>");
  }

  public void userLeft(String username)
  {
    for(int i = 0; i < lstUserList.getSize(); i++)
    {
      if(((String)lstUserList.get(i)).equalsIgnoreCase(username))
      {
        lstUserList.remove(i);
        break;
      }
    }
   addMessage("<b><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"2\" color=\"#990000\">" +
               "User " + username + " has left!</font></b><br>");
  }
}