package com.bytezone.plugins;

import com.bytezone.dm3270.plugins.PluginField;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ScreenFieldTable extends TableView<PluginField>
{
  public ScreenFieldTable ()
  {
    setStyle ("-fx-font-size: 11; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    TableColumn<PluginField, Integer> sequence = new TableColumn<> ("Seq");
    sequence.setPrefWidth (50);
    sequence.setCellValueFactory (new PropertyValueFactory<> ("sequence"));

    TableColumn<PluginField, Integer> row = new TableColumn<> ("Row");
    row.setPrefWidth (50);
    row.setCellValueFactory (new PropertyValueFactory<> ("row"));

    TableColumn<PluginField, Integer> column = new TableColumn<> ("Column");
    column.setPrefWidth (50);
    column.setCellValueFactory (new PropertyValueFactory<> ("column"));

    TableColumn<PluginField, Integer> length = new TableColumn<> ("Length");
    length.setPrefWidth (80);
    length.setCellValueFactory (new PropertyValueFactory<> ("length"));

    TableColumn<PluginField, String> modifiable = new TableColumn<> ("Modifiable");
    modifiable.setPrefWidth (80);
    modifiable.setCellValueFactory (new PropertyValueFactory<> ("modifiable"));

    TableColumn<PluginField, String> visible = new TableColumn<> ("Visible");
    visible.setPrefWidth (80);
    visible.setCellValueFactory (new PropertyValueFactory<> ("visible"));

    TableColumn<PluginField, String> altered = new TableColumn<> ("Altered");
    altered.setPrefWidth (80);
    altered.setCellValueFactory (new PropertyValueFactory<> ("altered"));

    TableColumn<PluginField, String> format = new TableColumn<> ("Format");
    format.setPrefWidth (50);
    format.setCellValueFactory (new PropertyValueFactory<> ("format"));

    TableColumn<PluginField, String> fieldValue = new TableColumn<> ("Field value");
    fieldValue.setPrefWidth (600);
    fieldValue.setCellValueFactory (new PropertyValueFactory<> ("fieldValue"));

    getColumns ().setAll (sequence, row, column, length, format, visible, modifiable,
                          altered, fieldValue);

    Callback<TableColumn<PluginField, Integer>,      //
    TableCell<PluginField, Integer>> rightJustified =
        new Callback<TableColumn<PluginField, Integer>,      //
        TableCell<PluginField, Integer>> ()
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
                setText (empty ? null : getItem () == null ? "0" :      //
                    String.format ("%,d", getItem ()));
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center-right;");
            return cell;
          }
        };

    Callback<TableColumn<PluginField, String>,      //
    TableCell<PluginField, String>> centreJustified =
        new Callback<TableColumn<PluginField, String>,      //
        TableCell<PluginField, String>> ()
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

    sequence.setCellFactory (rightJustified);
    row.setCellFactory (rightJustified);
    column.setCellFactory (rightJustified);
    length.setCellFactory (rightJustified);
    modifiable.setCellFactory (centreJustified);
    visible.setCellFactory (centreJustified);
    altered.setCellFactory (centreJustified);
    format.setCellFactory (centreJustified);
  }
}