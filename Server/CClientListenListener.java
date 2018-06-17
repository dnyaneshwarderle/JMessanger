public interface CClientListenListener
{
  public void onUserLeave(CClientListenThread c);
  public void onUserAuthenticate(CClientListenThread c, String username);
  public void onUserSendMessage(CClientListenThread c, String to, String message);
  public void onUserSendMessageToAll(CClientListenThread c, String message);
}