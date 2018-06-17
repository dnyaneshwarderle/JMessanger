
class CCommandParser
{
  // Constants
  public static final int CMD_AUTHENTICATE         = 1;
  public static final int CMD_USER_JOINED          = 2;
  public static final int CMD_USER_LEFT            = 3;
  public static final int CMD_MESSAGE_RECEIVED     = 4;
  public static final int CMD_ALL_MESSAGE_RECEIVED = 5;
  public static final int CMD_SERVER_SHUTDOWN      = 6; 

  private boolean bFromServer;
  private boolean bValid;
  private int iCommand;
  private String sUsername;
  private String sData;

  public void clientProtocol(boolean isClient)
  {
    bFromServer = isClient; 
  }

  public void parseData(String sData)
  {
    boolean bGetUser, bGetData;

    bGetUser = false;
    bGetData = false;
    
    // Clear the variables
    bValid = false;
    iCommand = 0;
    sUsername = "";
    this.sData = "";
    
    // First get the valid code (+ or -)
    bValid = sData.substring(0,1).equalsIgnoreCase("+");
    
    // Now get the code
    iCommand = Integer.parseInt(sData.substring(1,3));

    // Test the command and see what data to extract
    if(!bFromServer && iCommand == CMD_AUTHENTICATE)
      bGetUser = true;
    if(bFromServer && !bValid && iCommand == CMD_AUTHENTICATE)
      bGetData = true;
    if(bFromServer && bValid && iCommand == CMD_USER_JOINED)
      bGetUser = true;
    if(bFromServer && bValid && iCommand == CMD_USER_LEFT)
      bGetUser = true;
    if(iCommand == CMD_MESSAGE_RECEIVED ||
      (iCommand == CMD_ALL_MESSAGE_RECEIVED && bFromServer))
    {
      bGetUser = true;
      bGetData = true;
    }
    if(iCommand == CMD_ALL_MESSAGE_RECEIVED && ! bFromServer)
      bGetData = true;
  
  // If we only want the user then extract the user details
    if(bGetUser && !bGetData)
    {
      sUsername = sData.substring(3, sData.length());
    }

    // If we only want the data then extract the data
    if(!bGetUser && bGetData)
    {
      this.sData = sData.substring(3, sData.length());
    }
   
    // If we want both the username and data then get them both
    if(bGetUser && bGetData)
    {
      int iPos;
      iPos = sData.indexOf("~");
      if(iPos > 0)
      {
        sUsername = sData.substring(3, iPos);
        this.sData = sData.substring(iPos + 1, sData.length());
      }
    }
  }

  public boolean isValid()
  {
    return bValid;
  }

  public int getCommand()
  {
    return iCommand;
  }

  public String getUser()
  {
    return sUsername;
  }

  public String getData()
  {
    return sData;
  }
}