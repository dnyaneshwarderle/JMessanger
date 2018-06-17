public interface CListenThreadListener
{
  public void onDataReceived(String data);
  public void onDataError(int id, String description);
}