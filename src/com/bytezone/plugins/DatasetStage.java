package com.bytezone.plugins;

import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.ScreenField;

public class DatasetStage extends Stage
{
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver = new WindowSaver (prefs, this, "PluginDataset");
  private final Button hideButton = new Button ("Hide Window");
  private final TextArea textArea = new TextArea ();
  private final Pattern p;
  private String datasetName;

  public DatasetStage ()
  {
    setTitle ("Dataset Display");
    setOnCloseRequest (e -> hide ());

    String pattern = "([A-Z0-9]{1,8}(\\.[A-Z0-9]{1,8})*)" // dataset name
        + "(\\([A-Z0-9]{1,8}\\))?"                        // member name
        + "( - [0-9]{2}\\.[0-9]{2})?";                    // editing data
    p = Pattern.compile (pattern);

    BorderPane root = new BorderPane ();
    BorderPane innerPane = new BorderPane ();

    HBox hbox = new HBox ();
    hideButton.setPrefWidth (150);
    hbox.setAlignment (Pos.CENTER_RIGHT);
    hbox.setPadding (new Insets (10, 10, 10, 10));         // trbl

    hideButton.setOnAction (e -> {
      closing ();
      hide ();
    });
    hbox.getChildren ().add (hideButton);

    root.setCenter (innerPane);
    root.setBottom (hbox);

    innerPane.setCenter (textArea);
    textArea.setFont (Font.font ("Monospaced", 13));

    Scene scene = new Scene (root, 500, 700);      // width, height
    setScene (scene);

    setOnCloseRequest (e -> closing ());
    windowSaver.restoreWindow ();
  }

  public void showDataset (PluginData data, List<ScreenField> modifiableFields)
  {
    if (data.size () > 20 && "EDIT".equals (data.trimField (11))
        && "Columns".equals (data.trimField (13))
        && "Command ===>".equals (data.trimField (17))
        && "Scroll ===>".equals (data.trimField (19)))
    {
      datasetName = data.trimField (12);
      Matcher m = p.matcher (datasetName);
      if (m.matches ())
      {
        System.out.println (m.groupCount ());
        for (int i = 0; i <= m.groupCount (); i++)
          System.out.printf ("%2d %s%n", i, m.group (i));

        String name = m.group (1);
        if (m.groupCount () >= 3)
          name += " " + m.group (3);

        setTitle (name);
      }

      StringBuilder text = new StringBuilder ();
      //      int count = 0;
      for (ScreenField sf : modifiableFields)
      {
        if (sf.isModifiable)
        {
          if (sf.getColumn () == 1 && !sf.data.equals ("******"))
          {
            text.append (sf.data);
            text.append (" ");
          }
          if (sf.getColumn () == 8)
          {
            text.append (sf.data);
            text.append ("\n");
          }
        }
        //        count++;
      }

      //      if (text.length () > 0)
      //        text.deleteCharAt (text.length () - 1);

      textArea.appendText (text.toString ());
    }

    if (!isShowing ())
      show ();

    requestFocus ();
    hideButton.requestFocus ();
  }

  public void closing ()
  {
    windowSaver.saveWindow ();
  }
}