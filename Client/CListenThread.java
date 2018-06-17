import java.io.*;

class CListenThread extends Thread
{
  private BufferedReader in;
  private CListenThreadListener listener;

  public CListenThread(CListenThreadListener listener, InputStreamReader in)
  {
    this.listener = listener;
    this.in = new BufferedReader(in);
    this.start();
  }

  public void run()
  {
    String data;

    // Keep receiving data and posting it back
    try
    {
      for(;;)
      {
        data = in.readLine();
        if(data == null)
        {
          listener.onDataError(2, "Connection lost");
          return;
        }
        else
        {
          listener.onDataReceived(data);
        }
      }
    }
    catch (IOException e)
    {
      listener.onDataError(2, e.getMessage());
    }
  }
}