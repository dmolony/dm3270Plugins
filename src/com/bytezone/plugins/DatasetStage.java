package com.bytezone.plugins;

import java.util.ArrayList;
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

      StringBuilder text = new StringBuilder ();
      EditorPage editorPage = new EditorPage (data, modifiableFields);
      System.out.println (editorPage);

      if (!editorPage.hasBeginning)
        text.append ("\n");

      setTitle (editorPage.datasetName);

      for (String line : editorPage.lines)
      {
        text.append (line);
        text.append ("\n");
      }

      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);

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

  public class EditorPage
  {
    String datasetName;
    String memberName;
    int firstLine = -1;
    int lastLine;
    boolean hasBeginning;
    boolean hasEnd;
    List<String> numbers = new ArrayList<> ();
    List<String> lines = new ArrayList<> ();

    public EditorPage (PluginData data, List<ScreenField> modifiableFields)
    {
      getDatasetName (data);

      for (ScreenField sf : modifiableFields)
      {
        if (sf.getColumn () == 1)
        {
          if (sf.length == 6 && sf.data.equals ("******"))
          {
            //            System.out.println (sf.sequence);
            ScreenField nextField = data.getField (sf.sequence + 1);
            //            System.out.println (nextField);
            if (nextField != null && nextField.length >= 72
                && nextField.data.startsWith ("********"))
              if (nextField.data.equals ("***************************** Top of Dat"
                  + "a ******************************"))
                hasBeginning = true;
              else if (nextField.data.equals ("**************************** Bottom of D"
                  + "ata ****************************"))
                hasEnd = true;
          }
          else
            numbers.add (sf.data);
        }
        if (sf.getColumn () == 8)
          lines.add (sf.data);
      }

      if (lines.size () > 0)
      {
        firstLine = Integer.parseInt (numbers.get (0));
        lastLine = Integer.parseInt (numbers.get (numbers.size () - 1));
      }
    }

    private void getDatasetName (PluginData data)
    {
      datasetName = data.trimField (12);
      Matcher m = p.matcher (datasetName);
      if (m.matches ())
      {
        if (false)
        {
          System.out.println (m.groupCount ());
          for (int i = 0; i <= m.groupCount (); i++)
            System.out.printf ("%2d %s%n", i, m.group (i));
        }

        datasetName = m.group (1);
        if (m.groupCount () >= 3)
          memberName = m.group (3);
        else
          memberName = "";
      }
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Dataset name .... %s%n", datasetName));
      text.append (String.format ("Member name ..... %s%n", memberName));
      text.append (String.format ("Lines ........... %d%n", lines.size ()));
      text.append (String.format ("First line ...... %d%n", firstLine));
      text.append (String.format ("Last line ....... %d%n", lastLine));
      text.append (String.format ("Has first ....... %s%n", hasBeginning));
      text.append (String.format ("Has last ........ %s%n", hasEnd));

      for (int i = 0; i < lines.size (); i++)
        text.append (String.format ("%s %s%n", numbers.get (i), lines.get (i)));

      return text.toString ();
    }
  }
}