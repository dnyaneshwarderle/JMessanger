public interface CPrivateMessageListener
{
  public void onSendMessage(String to, String message);
  public void onClosePrivateMessage(String remoteUsername);
}