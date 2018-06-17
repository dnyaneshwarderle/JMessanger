import java.io.*;

class CMenuThread extends Thread
{
  protected CMenuListener listener;

  protected boolean shutdown;
  protected boolean running;
  protected boolean logging;
  protected boolean anykey;

  public CMenuThread(CMenuListener l)
  {
    listener = l;
    shutdown = false;
    running = false;
    logging = false;
    anykey= false;

    this.start();
  }

  public void run()
  {
    String dataIn;
    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    // Keep listening until the user exits
    while(!shutdown)
    {
      if(anykey)
        showPressAnyKey();
      else
        showMenuOptions();
      try
      {
        dataIn = stdIn.readLine();

        if(anykey)
        {
          anykey = false;
        }
        else
        {
          if(dataIn.equals("1"))
            listener.onServerRunning(!running);

          if(dataIn.equals("2"))
          {
            logging = !logging;
            listener.onLogging(logging);
          }

          if(dataIn.equals("3"))
            listener.onLogFileClear();
          
          if(dataIn.equals("4"))
            listener.onViewLogFile();

          if(dataIn.equals("5"))
            listener.onShowConnectedUsers();

          if(dataIn.equals("x"))
            listener.onShutdown();
        }
      }
      catch (IOException e)
      {
        System.err.println(e);
      }
    }
  }

  public void anyKeyContinue()
  {
    anykey = true;
  }

  private void showPressAnyKey()
  {
    System.out.println("");
    System.out.print("Press enter to continue");
  }

  private void showMenuOptions()
  {
    int i;
    for (i = 1; i < 20; i++)
      System.out.println("");
    System.out.println("JMessengerServer 1.0");
    System.out.println("----------------------------------");

    if(running)
      System.out.println("[1] Stop server");
    else
      System.out.println("[1] Start server");

    if(logging)
      System.out.println("[2] Disable logging");
    else
      System.out.println("[2] Enable logging");

    System.out.println("[3] Clear log file");
    System.out.println("[4] View log file");
    System.out.println("[5] View currently connected users");
    System.out.println("");
    System.out.println("[x] Shut down and exit");
    
    System.out.println("");
    System.out.print("Enter option: ");
  }


  public void shutDown()
  {
    shutdown = true;
  }

  public void setServerRunState(boolean running)
  {
    this.running = running;
  }
}