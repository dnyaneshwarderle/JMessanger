import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class JPrivateMessage extends JFrame implements ActionListener
{
  private JEditorPane   txtConversation = new JEditorPane();
  private JTextArea     txtSend = new JTextArea();
  private JButton       btnSend = new JButton("Send");
  private CPrivateMessageListener listener;
  private JScrollPane   scrlConversation;
  public  String localUsername;
  public  String remoteUsername;

  // Message Data items
  private String messageHeader = "<html><body>";
  private String messageFooter = "</body></html>";

  public JPrivateMessage(CPrivateMessageListener l, String title, String localUser, String remoteUser)
  {
    super(title);

    listener = l;
    localUsername = localUser;
    remoteUsername = remoteUser;

    // Setup the controls
    controlsSetup();
    pack();

    // Catch pressing the [x] and call the close event
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        listener.onClosePrivateMessage(remoteUsername);
      }
    });

    setVisible(true);
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
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                          lefttopPane,
                                          leftbottomPane);
    splitPane.setOneTouchExpandable(false);
    splitPane.setDividerLocation(300);
    splitPane.setPreferredSize(new Dimension(400, 400));

    setContentPane(splitPane);
  }

  public void actionPerformed(ActionEvent e)
  {
    if(e.getSource() == btnSend)
    {
      String message = txtSend.getText().replaceAll("\n", "<br>");
      // Replace all new lines with <br> tags when we send the data
      listener.onSendMessage(remoteUsername, message);
      // Add the local message to the conversation window
      addMessage("<p><b>" + localUsername + ": </b>" + message + "</p>");
  
      txtSend.setText("");
    }
  }

  public void messageReceived(String message)
  {
    addMessage("<p><b>" + remoteUsername + ": </b>" + message + "</p>");
  }

  public void userLeft()
  {
    addMessage("<p><b>" + remoteUsername + " has disconnected</b></p>");
    // Disable the send button
    btnSend.setEnabled(false);
  }

  private void addMessage(String message)
  {
    messageHeader += message;
    txtConversation.setText(messageHeader + messageFooter);

    txtConversation.selectAll();
    txtConversation.setCaretPosition(txtConversation.getSelectedText().length());
  }
}