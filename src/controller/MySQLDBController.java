package sample;
import java.net.URL;
import java.sql.*;
import java.io.IOException;
import java.util.*;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.util.Callback;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleStringProperty;

public class MySQLDBController implements Initializable{
  @FXML
  ComboBox databaseComboBox;
  @FXML
  ListView tableList;
  @FXML
  TableView columnTable;

  public void selectDatabase(ActionEvent e) {
    String dbName = databaseComboBox.getValue().toString();
    MySQLManager.connector.connectDatabase(dbName);

    /* set table list after cleared tables list in ListView */
    tableList.setItems(FXCollections.observableArrayList());
    List<String> queryResult = MySQLSearch.query("SHOW TABLES", "Tables_in_"+dbName);
    for(String table : queryResult) {
      tableList.getItems().add(table);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    /* set database list to ListView */
    databaseComboBox.setItems(FXCollections.observableArrayList());
    List<String> databaseList = MySQLSearch.query("SHOW DATABASES", "Database");
    for(String database : databaseList) {
      databaseComboBox.getItems().add(database);
    }

    /* when clicked table name in ListView */
    tableList.setOnMouseClicked((MouseEvent)-> {
      columnTable.getColumns().clear();
      columnTable.getItems().clear();
      String tableName = tableList.getSelectionModel().getSelectedItem().toString();
      setContents(tableName);
    });
  }

  public void setContents(String tableName) {
    /* set columns */
    List<String> columns = MySQLSearch.query("SHOW COLUMNS FROM "+tableName, "Field");
    for(String column : columns) {
      TableColumn<Record, String> tableColumn = new TableColumn<Record, String>(column);
      tableColumn.setPrefWidth(100);
      tableColumn.setCellValueFactory(new Callback<CellDataFeatures<Record, String>, ObservableValue<String>>() {
        @Override
        public ObservableValue<String> call(CellDataFeatures<Record, String> p) {
          return new SimpleStringProperty(p.getValue().getData(column));
        }
      });
      columnTable.getColumns().add(tableColumn);
    }

    /* set records */
    ArrayList<ArrayList<String>> recordList = MySQLSearch.query("SELECT * FROM "+tableName);
    for(int rowCount=0; rowCount < recordList.size(); rowCount++) {
      Map<String, String> tableRecord = new HashMap<String, String>();
      for(int index=0; index<columns.size(); index++) {
        tableRecord.put(columns.get(index), recordList.get(rowCount).get(index));
      }
      columnTable.getItems().add(new Record().setData(tableRecord));
    }
  }
}
