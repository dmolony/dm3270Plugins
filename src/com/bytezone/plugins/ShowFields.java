package com.bytezone.plugins;

import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;

public class ShowFields extends DefaultPlugin
{
  DebugStage debugStage;

  @Override
  public void activate ()
  {
    if (debugStage == null)
      debugStage = new DebugStage ();
  }

  @Override
  public void deactivate ()
  {
    if (debugStage != null)
      debugStage.hide ();
  }

  @Override
  public boolean doesRequest ()
  {
    return true;
  }

  @Override
  public void processRequest (PluginData data)
  {
    debugStage.showData (data);
  }
}