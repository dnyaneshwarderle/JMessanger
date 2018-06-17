public interface CMenuListener
{
  public void onServerRunning(boolean enable);
  public void onShutdown();
  public void onLogging(boolean enable);
  public void onLogFileClear();
  public void onViewLogFile();
  public void onShowConnectedUsers();
}