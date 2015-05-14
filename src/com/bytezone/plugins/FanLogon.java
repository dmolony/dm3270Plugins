package com.bytezone.plugins;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import com.bytezone.dm3270.application.Parameters;
import com.bytezone.dm3270.application.Parameters.SiteParameters;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.ScreenField;

public class FanLogon extends DefaultPlugin
{
  private static final String TSO_HEADING = "------------------------------- "
      + "TSO/E LOGON -----------------------------------";
  private static final String FANDEZHI_HEADING =
      "Mainframe Operating System                              z/OS V1.6";
  private boolean doesAuto = false;
  private boolean fanDeZhi = false;

  private final Parameters parameters = new Parameters ();
  private String password;
  private String user;

  @Override
  public void activate ()
  {
    SiteParameters sp = parameters.getSiteParameters ("FanDeZhi");
    if (sp == null)
    {
      showAlert (AlertType.ERROR,
                 String.format ("Parameters for %s not found", "FanDeZhi"));
      doesAuto = false;
    }
    else
    {
      user = sp.getParameter ("user");
      password = sp.getParameter ("password");
      if (user == null || password == null)
      {
        showAlert (AlertType.ERROR, "Parameters not found");
        doesAuto = false;
      }
      else
        doesAuto = true;
    }
  }

  @Override
  public boolean doesAuto ()
  {
    return doesAuto;
  }

  @Override
  public void processAuto (PluginData data)
  {
    if (data.sequence == 0)
    {
      if (data.size () > 6 && FANDEZHI_HEADING.equals (data.trimField (0))
          && "TSO".equals (data.trimField (5))
          && "- Logon to TSO/ISPF".equals (data.trimField (6)))
      {
        ScreenField command = data.getField (17);
        if (command != null && command.isModifiableLength (58))
        {
          command.change ("TSO " + user);
          data.setKey (AIDCommand.AID_ENTER_KEY);
          fanDeZhi = true;
        }
      }
      return;
    }
    else if (!fanDeZhi)
    {
      showAlert (AlertType.ERROR, "Wrong server");
      doesAuto = false;
      return;
    }

    if (data.sequence >= 1 && data.sequence <= 2)
    {
      // do nothing
    }
    else if (data.sequence == 3)        // password screen
    {
      if (TSO_HEADING.equals (data.trimField (0))
          && "Enter LOGON parameters below:".equals (data.trimField (3)))
      {
        ScreenField passwordField = data.getField (12);
        if (passwordField.isModifiableLength (8))
        {
          passwordField.change (password);
          data.setKey (AIDCommand.AID_ENTER_KEY);
        }
        else
        {
          showAlert (AlertType.ERROR, "Wrong screen (can't find password field)");
          doesAuto = false;
        }
      }
      else
      {
        showAlert (AlertType.ERROR, "Wrong screen (heading doesn't match)");
        doesAuto = false;
      }
    }
    else if (data.sequence >= 4 && data.sequence <= 20)
    {
      // check for password screen error
      if (data.sequence == 4 && TSO_HEADING.equals (data.trimField (0)))
      {
        showAlert (AlertType.ERROR, "Password entry failed");
        doesAuto = false;
      }
      else if ("ISPF Primary Option Menu".equals (data.trimField (10))
          && "User ID . :".equals (data.trimField (23)) //
          && "DMOLONY".equals (data.trimField (24)))
      {
        doesAuto = false;
        data.suppressDisplay = true;
      }
      else
        data.setKey (AIDCommand.AID_ENTER_KEY);
    }
    else
      doesAuto = false;
  }

  private boolean showAlert (AlertType alertType, String message)
  {
    Alert alert = new Alert (alertType, message);
    alert.getDialogPane ().setHeaderText (null);
    Optional<ButtonType> result = alert.showAndWait ();
    return (result.isPresent () && result.get () == ButtonType.OK);
  }
}