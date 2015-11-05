package com.bytezone.plugins;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.utilities.WindowSaver;
import com.bytezone.plugins.Document.Line;

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

public class DatasetStage extends Stage
{
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver = new WindowSaver (prefs, this, "PluginDataset");

  private final Button hideButton = new Button ("Hide Window");
  private final TextArea textArea = new TextArea ();

  private final TextField datasetNameText;
  private final TextField memberNameText;
  private final TextField datasetLinesText;
  private final TextField datasetColumnsText;

  //  private int totalLines;
  //  private int largestColumn;
  private final Font defaultFont;

  //  private final Document document = new Document ();

  public DatasetStage ()
  {
    setTitle ("Dataset Display");
    setOnCloseRequest (e -> hide ());
    setTitle ("Dataset Display");

    //    doesRequest = true;
    //    doesAuto = false;
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

  public void setDocument (Document document)
  {
    datasetNameText.setText (document.datasetName);
    memberNameText.setText (document.memberName);
    datasetLinesText.setText (document.getLines ().size () + "");

    textArea.clear ();
    for (Line line : document.getLines ())
    {
      textArea.appendText (line.toString ());
      textArea.appendText ("\n");
    }
    datasetColumnsText.setText (document.maxColumns + "");

    if (textArea.getLength () > 0)
    {
      textArea.deleteText (textArea.getLength () - 1, textArea.getLength ());
      textArea.positionCaret (0);
    }
  }

  //  public void showDataset (Document document)
  //  {
  //    if (textArea.getLength () == 0)
  //      setDocument (document);
  //
  //    if (!isShowing ())
  //      show ();
  //  }

  //  private void showDataset (PluginData data, List<ScreenField> modifiableFields)
  //  {
  //    if (!isShowing ())
  //      show ();
  //
  //    int editPosition = findField ("EDIT", data);
  //    int commandPosition = findField ("Command ===>", data);
  //    int scrollPosition = findField ("Scroll ===>", data);
  //
  //    if (editPosition > 0 && commandPosition > editPosition
  //        && scrollPosition > commandPosition + 1)
  //    {
  //      DocumentPage page = DocumentPage.createPage (data, modifiableFields);
  //
  //      if (page.rightColumn > largestColumn)
  //      {
  //        largestColumn = page.rightColumn;
  //        datasetColumnsText.setText (largestColumn + "");
  //      }
  //
  //      StringBuilder text = new StringBuilder ();
  //
  //      int count = 0;
  //      for (String line : page.lines)
  //        text.append (String.format ("%s %s%n", page.numbers.get (count++), line));
  //
  //      if (page.hasEnd && text.length () > 0)
  //        text.deleteCharAt (text.length () - 1);
  //
  //      if (page.hasBeginning)
  //      {
  //        textArea.setText (text.toString ());
  //        totalLines = page.lines.size ();
  //        datasetNameText.setText (page.datasetName);
  //        memberNameText.setText (page.memberName);
  //      }
  //      else
  //      {
  //        textArea.appendText (text.toString ());
  //        totalLines += page.lines.size ();
  //      }
  //
  //      datasetLinesText.setText (totalLines + "");
  //
  //      if (page.hasEnd)
  //      {
  //        doesAuto = false;
  //        requestFocus ();
  //
  //        if (!page.hasBeginning)
  //        {
  //          ScreenField command = data.getField (commandPosition + 1);
  //          command.change ("m");
  //          data.setKey (AIDCommand.AID_PF7);
  //          textArea.positionCaret (0);
  //        }
  //      }
  //      else
  //      {
  //        doesAuto = true;
  //        data.setKey (AIDCommand.AID_PF8);
  //      }
  //      doesRequest = !doesAuto;
  //    }
  //    else
  //    {
  //      System.out.println ("failed test");
  //      for (int i = 0; i < data.size (); i++)
  //        System.out.printf ("%d [%s]%n", i, data.trimField (i));
  //      doesAuto = false;
  //      doesRequest = true;
  //    }
  //  }

  //  private int findField (String text, PluginData data)
  //  {
  //    for (ScreenField sf : data.screenFields)
  //      if (text.equals (sf.getFieldValue ()))
  //        return sf.sequence;
  //    return -1;
  //  }

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

  public void closing ()
  {
    windowSaver.saveWindow ();
  }
}