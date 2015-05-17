package com.bytezone.plugins;

import java.util.prefs.Preferences;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.ScreenField;

public class DebugStage extends Stage
{
  private final ScreenFieldTable table;
  private final ObservableList<ScreenField> dataRecords = FXCollections
      .observableArrayList ();

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver = new WindowSaver (prefs, this, "PluginDebug");

  private final CheckBox hideEmptyCB = new CheckBox ("Hide empty & protected");
  private boolean hideEmpty;
  private final Button hideButton = new Button ("Hide Window");

  public DebugStage ()
  {
    setTitle ("Field Details Plugin");

    table = new ScreenFieldTable ();
    setOnCloseRequest (e -> hide ());

    BorderPane root = new BorderPane ();
    BorderPane innerPane = new BorderPane ();

    HBox hbox = new HBox ();
    hideButton.setPrefWidth (150);
    hbox.setAlignment (Pos.CENTER_RIGHT);
    hbox.setPadding (new Insets (0, 10, 10, 10));         // trbl

    hideButton.setOnAction (e -> {
      closing ();
      hide ();
    });
    hbox.getChildren ().add (hideButton);

    root.setCenter (innerPane);
    root.setBottom (hbox);

    hideEmpty = prefs.getBoolean ("PluginHideEmpty", false);
    hideEmptyCB.setSelected (hideEmpty);

    final HBox checkBoxes = new HBox ();
    checkBoxes.setSpacing (15);
    checkBoxes.setPadding (new Insets (10, 10, 0, 10));    // trbl
    checkBoxes.getChildren ().addAll (hideEmptyCB);

    innerPane.setCenter (table);
    innerPane.setBottom (checkBoxes);

    Scene scene = new Scene (root, 1000, 500);      // width, height
    setScene (scene);

    setOnCloseRequest (e -> closing ());
    windowSaver.restoreWindow ();
  }

  private void change (ScreenFieldTable table, FilteredList<ScreenField> filteredData)
  {
    // get the previously selected line
    ScreenField selectedRecord = table.getSelectionModel ().getSelectedItem ();

    // update the booleans
    hideEmpty = hideEmptyCB.isSelected ();

    // change the filter predicate
    filteredData.setPredicate (screenField -> //
        (screenField.isModifiable || !screenField.getFieldValue ().isEmpty () || !hideEmpty));

    // restore the previously selected item (if it is still visible)
    if (selectedRecord != null)
    {
      table.getSelectionModel ().select (selectedRecord);
      table.requestFocus ();
    }
  }

  public void closing ()
  {
    prefs.putBoolean ("PluginHideEmpty", hideEmpty);
    windowSaver.saveWindow ();
  }

  public void showData (PluginData data)
  {
    dataRecords.setAll (data.screenFields);
    FilteredList<ScreenField> filteredData = new FilteredList<> (dataRecords, p -> true);

    // change the filter predicate
    filteredData.setPredicate (screenField -> //
        (screenField.isModifiable || !screenField.getFieldValue ().isEmpty () || !hideEmpty));

    SortedList<ScreenField> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (table.comparatorProperty ());

    ChangeListener<? super Boolean> changeListener =
        (observable, oldValue, newValue) -> change (table, filteredData);

    hideEmptyCB.selectedProperty ().addListener (changeListener);
    table.setItems (sortedData);

    if (!isShowing ())
      show ();

    requestFocus ();
    hideButton.requestFocus ();
  }
}