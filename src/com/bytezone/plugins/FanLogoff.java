package com.bytezone.plugins;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;
import com.bytezone.dm3270.plugins.ScreenLocation;

public class FanLogoff extends DefaultPlugin
{
  private boolean doesAuto = false;
  private boolean doesRequest = true;

  private final ScreenLocation commandLocation = new ScreenLocation (3, 1);
  private final ScreenLocation optionLocation = new ScreenLocation (3, 1);

  private PluginData data;

  @Override
  public boolean doesAuto ()
  {
    return doesAuto;
  }

  @Override
  public boolean doesRequest ()
  {
    return doesRequest;
  }

  @Override
  public void processAuto (PluginData data)
  {
    PluginField command = null;
    if ("READY".equals (data.trimField (0)))
      command = data.getField (1);
    else if ("READY".equals (data.trimField (2)))
      command = data.getField (3);

    if (command != null
        && (command.isModifiableLength (1911) || command.isModifiableLength (1831)))
    {
      command.change ("logoff");
      data.setKey (AIDCommand.AID_ENTER);
      doesAuto = false;
    }
  }

  @Override
  public void processRequest (PluginData data)
  {
    this.data = data;

    int maxFields = Math.min (20, data.screenFields.size ());
    for (int i = 0; i < maxFields; i++)
    {
      PluginField field = data.getField (i);
      if (matches (i, field, "Command ===>", commandLocation, 48)
          || matches (i, field, "Option ===>", optionLocation, 66))
      {
        setLogoff (data.getField (i + 1));
        return;
      }
    }
  }

  private boolean matches (int index, PluginField field, String text,
      ScreenLocation expectedLocation, int nextFieldLength)
  {
    if (field.isProtected && field.location.matches (expectedLocation)
        && field.getFieldValue ().equals (text))
    {
      PluginField nextField = data.getField (index + 1);
      if (nextField != null && nextField.isModifiable
          && nextField.getLength () == nextFieldLength)
        return true;
    }
    return false;
  }

  private void setLogoff (PluginField command)
  {
    doesAuto = true;
    doesRequest = false;

    command.change ("=x");
    data.setKey (AIDCommand.AID_ENTER);
  }
}