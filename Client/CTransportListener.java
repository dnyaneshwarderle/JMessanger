public interface CTransportListener
{
  public void onConnect();
  public void onConnectError(int id, String description);
  public void onUserValidated();
  public void onUserRejected(String reason);
  public void onUserJoin(String username);
  public void onUserLeave(String username);
  public void onMessageReceived(String from, String message);
  public void onMessageReceivedFromAll(String from, String message); 
  public void onSendMessageError(int id, String description);
  public void onLostConnection();
}