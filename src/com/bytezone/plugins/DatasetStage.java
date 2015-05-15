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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.ScreenField;

public class DatasetStage extends Stage
{
  private static final String START_DATA = "***************************** Top of Dat"
      + "a ******************************";
  private static final String END_DATA = "**************************** Bottom of D"
      + "ata ****************************";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver = new WindowSaver (prefs, this, "PluginDataset");
  private final Button hideButton = new Button ("Hide Window");
  private final TextArea textArea = new TextArea ();
  private final Label datasetLabel = new Label ();
  private final Pattern p;

  private boolean doesAuto;
  private boolean doesRequest;

  public DatasetStage ()
  {
    setTitle ("Dataset Display");
    setOnCloseRequest (e -> hide ());
    setTitle ("Dataset Display");

    String pattern = "([A-Z0-9]{1,8}(\\.[A-Z0-9]{1,8})*)" // dataset name
        + "(\\([A-Z0-9]{1,8}\\))?"                        // member name
        + "( - [0-9]{2}\\.[0-9]{2})?";                    // editing data
    p = Pattern.compile (pattern);

    doesRequest = true;
    doesAuto = false;

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

    hbox = new HBox ();
    hbox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    hbox.getChildren ().addAll (new Label ("Dataset name : "), datasetLabel);

    innerPane.setTop (hbox);
    innerPane.setCenter (textArea);
    textArea.setFont (Font.font ("Monospaced", 13));

    Scene scene = new Scene (root, 500, 700);      // width, height
    setScene (scene);

    setOnCloseRequest (e -> closing ());
    windowSaver.restoreWindow ();
  }

  public void showDataset (PluginData data, List<ScreenField> modifiableFields)
  {
    int editPosition = findField ("EDIT", data);
    int commandPosition = findField ("Command ===>", data);
    int scrollPosition = findField ("Scroll ===>", data);

    if (editPosition > 0 && commandPosition > editPosition
        && scrollPosition > commandPosition + 1)
    {
      EditorPage editorPage = new EditorPage (data, modifiableFields);
      datasetLabel.setText (editorPage.datasetName);

      System.out.println (editorPage);

      StringBuilder text = new StringBuilder ();

      for (String line : editorPage.lines)
      {
        text.append (line);
        text.append ("\n");
      }

      if (editorPage.hasEnd && text.length () > 0)
        text.deleteCharAt (text.length () - 1);

      if (editorPage.hasBeginning)
        textArea.setText ("");

      textArea.appendText (text.toString ());

      if (editorPage.hasEnd)
      {
        doesAuto = false;
        if (!editorPage.hasBeginning)
        {
          ScreenField command = data.getField (commandPosition + 1);
          command.change ("m");
          data.setKey (AIDCommand.AID_PF7);
        }
      }
      else
      {
        doesAuto = true;
        data.setKey (AIDCommand.AID_PF8);
      }
      doesRequest = !doesAuto;
    }
    else
    {
      System.out.println ("failed test");
      for (int i = 0; i < data.size (); i++)
        System.out.printf ("%d [%s]%n", i, data.trimField (i));
      doesAuto = false;
      doesRequest = true;
    }

    if (!isShowing ())
      show ();

    //    requestFocus ();
    hideButton.requestFocus ();
  }

  private int findField (String text, PluginData data)
  {
    for (ScreenField sf : data.screenFields)
      if (text.equals (sf.data))
        return sf.sequence;
    return -1;
  }

  public boolean doesRequest ()
  {
    return doesRequest;
  }

  public boolean doesAuto ()
  {
    return doesAuto;
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
    int leftColumn;
    int rightColumn;
    List<String> numbers = new ArrayList<> ();
    List<String> lines = new ArrayList<> ();

    public EditorPage (PluginData data, List<ScreenField> modifiableFields)
    {
      getDatasetName (data);
      getColumns (data);

      for (ScreenField sf : modifiableFields)
      {
        switch (sf.location.column)
        {
          case 1:
            if (sf.length == 6 && sf.data.equals ("******"))
            {
              ScreenField nextField = data.getField (sf.sequence + 1);
              if (nextField != null && nextField.length >= 72
                  && nextField.data.startsWith ("********"))
                if (nextField.data.equals (START_DATA))
                  hasBeginning = true;
                else if (nextField.data.equals (END_DATA))
                  hasEnd = true;
            }
            else
              numbers.add (sf.data);
            break;

          case 8:
            lines.add (sf.data);
            break;

          case 14:      // command field
          case 75:      // CSR/PAGE
            break;

          default:
            System.out.printf ("column %d: %s%n", sf.location.column, sf.data);
        }
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
        {
          String name = m.group (3);
          memberName = name.substring (1, name.length () - 1);
        }
        else
          memberName = "";
      }
    }

    private void getColumns (PluginData data)
    {
      String col1 = data.trimField (14);
      String col2 = data.trimField (15);

      leftColumn = Integer.parseInt (col1);
      rightColumn = Integer.parseInt (col2);
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
      text.append (String.format ("Left column ..... %d%n", leftColumn));
      text.append (String.format ("Right column .... %d%n", rightColumn));
      text.append ("\n");

      for (int i = 0; i < lines.size (); i++)
        text.append (String.format ("%s %s%n", numbers.get (i), lines.get (i)));

      return text.toString ();
    }
  }
}