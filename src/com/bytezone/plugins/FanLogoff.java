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

  private final ScreenLocation locationRow1 = new ScreenLocation (1, 1);
  private final ScreenLocation locationRow2 = new ScreenLocation (2, 1);
  private final ScreenLocation locationRow3 = new ScreenLocation (3, 1);
  private static final String COMMAND_PROMPT = "Command ===>";
  private static final String COMMAND_INPUT_PROMPT = "COMMAND INPUT ===>";
  private static final String ACTION_PROMPT = "Action ===>";
  private static final String OPTION_PROMPT = "Option ===>";
  //  private final ScreenLocation optionLocationRow3 = new ScreenLocation (3, 1);
  //  private final ScreenLocation actionLocationRow3 = new ScreenLocation (3, 1);

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
    }
    doesAuto = false;
  }

  @Override
  public void processRequest (PluginData data)
  {
    this.data = data;

    int maxFields = Math.min (20, data.screenFields.size ());
    for (int i = 0; i < maxFields; i++)
    {
      PluginField field = data.getField (i);

      if (matches (i, field, COMMAND_PROMPT, locationRow1, 48, 65)
          || matches (i, field, COMMAND_PROMPT, locationRow2, 48, 65)
          || matches (i, field, COMMAND_PROMPT, locationRow3, 48, 65)
          || matches (i, field, COMMAND_INPUT_PROMPT, locationRow3, 42)
          || matches (i, field, ACTION_PROMPT, locationRow3, 49)
          || matches (i, field, OPTION_PROMPT, locationRow3, 66))
      {
        setLogoff (data.getField (i + 1));
        return;
      }
    }

    if (data.screenFields.size () < 19)
      return;

    PluginField field = data.getField (10);
    if (field.getLength () == 18 && "ISPF Command Shell".equals (field.getFieldValue ()))
    {
      field = data.getField (17);
      if (field.getLength () == 4 && "===>".equals (field.getFieldValue ()))
      {
        field = data.getField (18);
        if (field.getLength () == 234)
        {
          setLogoff (field);
          return;
        }
      }
    }
  }

  private boolean matches (int index, PluginField field, String text,
      ScreenLocation expectedLocation, int... nextFieldLength)
  {
    if (field.isProtected && field.location.matches (expectedLocation)
        && field.getFieldValue ().equals (text))
    {
      PluginField nextField = data.getField (index + 1);
      for (int fieldLength : nextFieldLength)
        if (nextField != null && nextField.isModifiable
            && nextField.getLength () == fieldLength)
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