import java.net.*;

public interface CListenListener
{
  public void onListen();
  public void onClose();
  public void onNewConnection(Socket s);
  public void onListenError(String description);
}