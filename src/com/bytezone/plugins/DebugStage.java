package com.bytezone.plugins;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;
import com.bytezone.dm3270.utilities.WindowSaver;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DebugStage extends Stage
{
  private final ScreenFieldTable table;
  private final ObservableList<PluginField> dataRecords =
      FXCollections.observableArrayList ();

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver = new WindowSaver (prefs, this, "PluginDebug");

  private final CheckBox hideEmptyCB = new CheckBox ("Hide empty & protected");
  private boolean hideEmpty;
  private final Button btnHide = new Button ("Hide Window");

  public DebugStage ()
  {
    setTitle ("Field Details Plugin");

    table = new ScreenFieldTable ();
    setOnCloseRequest (e -> hide ());

    BorderPane root = new BorderPane ();

    btnHide.setPrefWidth (150);
    btnHide.setOnAction (e -> closing ());

    hideEmpty = prefs.getBoolean ("PluginHideEmpty", false);
    hideEmptyCB.setSelected (hideEmpty);

    final HBox checkBoxes = new HBox ();
    checkBoxes.setSpacing (15);
    checkBoxes.getChildren ().addAll (hideEmptyCB);

    AnchorPane anchorPane = new AnchorPane ();
    AnchorPane.setLeftAnchor (checkBoxes, 10.0);
    AnchorPane.setBottomAnchor (checkBoxes, 10.0);
    AnchorPane.setTopAnchor (checkBoxes, 10.0);
    AnchorPane.setTopAnchor (btnHide, 10.0);
    AnchorPane.setBottomAnchor (btnHide, 10.0);
    AnchorPane.setRightAnchor (btnHide, 10.0);
    anchorPane.getChildren ().addAll (checkBoxes, btnHide);

    root.setCenter (table);
    root.setBottom (anchorPane);

    Scene scene = new Scene (root, 1000, 500);// width, height
    setScene (scene);

    setOnCloseRequest (e -> closing ());
    windowSaver.restoreWindow ();
  }

  private void change (ScreenFieldTable table, FilteredList<PluginField> filteredData)
  {
    // get the previously selected line
    PluginField selectedRecord = table.getSelectionModel ().getSelectedItem ();

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
    hide ();
  }

  public void showData (PluginData data)
  {
    dataRecords.setAll (data.screenFields);
    FilteredList<PluginField> filteredData = new FilteredList<> (dataRecords, p -> true);

    // change the filter predicate
    filteredData.setPredicate (screenField -> //
    (screenField.isModifiable || !screenField.getFieldValue ().isEmpty () || !hideEmpty));

    SortedList<PluginField> sortedData = new SortedList<> (filteredData);
    sortedData.comparatorProperty ().bind (table.comparatorProperty ());

    ChangeListener<? super Boolean> changeListener =
        (observable, oldValue, newValue) -> change (table, filteredData);

    hideEmptyCB.selectedProperty ().addListener (changeListener);
    table.setItems (sortedData);

    if (!isShowing ())
      show ();

    requestFocus ();
    btnHide.requestFocus ();
  }
}