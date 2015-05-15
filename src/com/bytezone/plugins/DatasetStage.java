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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

  private final TextField datasetNameText;
  private final TextField memberNameText;
  private final TextField datasetLinesText;
  private final TextField datasetColumnsText;

  private final Pattern p;
  private int totalLines = 0;
  private final Font defaultFont;

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
    defaultFont = Font.font ("Monospaced", 14);

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

    HBox row1 = new HBox (10);
    HBox row2 = new HBox (10);

    Label datasetLabel = getLabel ("Dataset", 55, 25, Pos.CENTER_LEFT);
    Label datasetLinesLabel = getLabel ("Lines", 55, 25, Pos.CENTER_LEFT);

    datasetNameText = getOutputField (250);
    datasetLinesText = getOutputField (55);

    Label memberLabel = getLabel ("Member", 55, 25, Pos.CENTER_LEFT);
    Label datasetColumnsLabel = getLabel ("Columns", 55, 25, Pos.CENTER_LEFT);

    memberNameText = getOutputField (250);
    datasetColumnsText = getOutputField (55);

    row1.getChildren ().addAll (datasetLabel, datasetNameText, datasetLinesLabel,
                                datasetLinesText);
    row2.getChildren ().addAll (memberLabel, memberNameText, datasetColumnsLabel,
                                datasetColumnsText);

    VBox vbox = new VBox (8);
    vbox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    vbox.getChildren ().addAll (row1, row2);

    innerPane.setTop (vbox);
    innerPane.setCenter (textArea);

    textArea.setFont (defaultFont);
    textArea.setEditable (false);
    textArea.setFocusTraversable (false);

    Scene scene = new Scene (root, 500, 700);      // width, height
    setScene (scene);

    setOnCloseRequest (e -> closing ());
    windowSaver.restoreWindow ();
    hideButton.requestFocus ();
  }

  public void showDataset (PluginData data, List<ScreenField> modifiableFields)
  {
    if (!isShowing ())
      show ();

    int editPosition = findField ("EDIT", data);
    int commandPosition = findField ("Command ===>", data);
    int scrollPosition = findField ("Scroll ===>", data);

    if (editPosition > 0 && commandPosition > editPosition
        && scrollPosition > commandPosition + 1)
    {
      EditorPage editorPage = new EditorPage (data, modifiableFields);

      datasetNameText.setText (editorPage.datasetName);
      memberNameText.setText (editorPage.memberName);
      totalLines += editorPage.lines.size ();
      datasetLinesText.setText (totalLines + "");
      datasetColumnsText.setText (editorPage.rightColumn + "");

      StringBuilder text = new StringBuilder ();

      int count = 0;
      for (String line : editorPage.lines)
        text.append (String.format ("%s %s%n", editorPage.numbers.get (count++), line));

      if (editorPage.hasEnd && text.length () > 0)
        text.deleteCharAt (text.length () - 1);

      if (editorPage.hasBeginning)
        textArea.setText ("");

      textArea.appendText (text.toString ());

      if (editorPage.hasEnd)
      {
        doesAuto = false;
        requestFocus ();

        if (!editorPage.hasBeginning)
        {
          ScreenField command = data.getField (commandPosition + 1);
          command.change ("m");
          data.setKey (AIDCommand.AID_PF7);
          textArea.positionCaret (0);
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
  }

  private int findField (String text, PluginData data)
  {
    for (ScreenField sf : data.screenFields)
      if (text.equals (sf.data))
        return sf.sequence;
    return -1;
  }

  private Label getLabel (String text, int width, int height, Pos pos)
  {
    Label label = new Label (text);

    label.setPrefWidth (width);
    label.setPrefHeight (height);
    label.setAlignment (pos);
    label.setFocusTraversable (false);

    return label;
  }

  private TextField getOutputField (int width)
  {
    TextField textField = new TextField ();

    textField.setEditable (false);
    textField.setPrefWidth (width);
    textField.setFont (defaultFont);
    textField.setFocusTraversable (false);

    return textField;
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
        switch (sf.location.column)
        {
          case 1:
            if (sf.length == 6 && sf.data.equals ("******"))
            {
              ScreenField nextField = data.getField (sf.sequence + 1);
              if (nextField != null && nextField.isProtected && nextField.length >= 72)
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