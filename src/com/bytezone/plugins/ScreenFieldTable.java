package com.bytezone.plugins;

import com.bytezone.dm3270.plugins.PluginField;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ScreenFieldTable extends TableView<PluginField>
{
  Callback<TableColumn<PluginField, Integer>, TableCell<PluginField, Integer>> rightJustified;
  Callback<TableColumn<PluginField, String>, TableCell<PluginField, String>> centreJustified;

  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public ScreenFieldTable ()
  {
    setStyle ("-fx-font-size: 11; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    createJustifications ();

    addIntegerColumn ("Sequence", "Seq", 50, Justification.RIGHT);
    addIntegerColumn ("Row", "Row", 50, Justification.RIGHT);
    addIntegerColumn ("Column", "Column", 50, Justification.RIGHT);
    addIntegerColumn ("Length", "Length", 80, Justification.RIGHT);
    addStringColumn ("Modifiable", "Modifiable", 80, Justification.CENTER);
    addStringColumn ("Visible", "Visible", 80, Justification.CENTER);
    addStringColumn ("Altered", "Altered", 80, Justification.CENTER);
    addStringColumn ("Format", "Format", 50, Justification.CENTER);
    addStringColumn ("FieldValue", "Field values", 600, Justification.LEFT);
  }

  private void addStringColumn (String id, String heading, int width,
      Justification justification)
  {
    TableColumn<PluginField, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<> (id));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setCellFactory (centreJustified);
  }

  private void addIntegerColumn (String id, String heading, int width,
      Justification justification)
  {
    TableColumn<PluginField, Integer> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<> (id));
    getColumns ().add (column);

    if (justification == Justification.RIGHT)
      column.setCellFactory (rightJustified);
  }

  private void createJustifications ()
  {
    rightJustified =
        new Callback<TableColumn<PluginField, Integer>, TableCell<PluginField, Integer>> ()
        {
          @Override
          public TableCell<PluginField, Integer>
              call (TableColumn<PluginField, Integer> p)
          {
            TableCell<PluginField, Integer> cell = new TableCell<PluginField, Integer> ()
            {
              @Override
              public void updateItem (Integer item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : getItem () == null ? "0" : //
                    String.format ("%,d", getItem ()));
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center-right;");
            return cell;
          }
        };

    centreJustified =
        new Callback<TableColumn<PluginField, String>, TableCell<PluginField, String>> ()
        {
          @Override
          public TableCell<PluginField, String> call (TableColumn<PluginField, String> p)
          {
            TableCell<PluginField, String> cell = new TableCell<PluginField, String> ()
            {
              @Override
              public void updateItem (String item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center;");
            return cell;
          }
        };
  }
}