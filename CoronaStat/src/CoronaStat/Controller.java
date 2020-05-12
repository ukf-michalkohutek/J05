package CoronaStat;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

public class Controller {

    @FXML private TableView<Result> tableView;
    @FXML private TextField countryTextField;
    @FXML private DatePicker datePicker;

    @FXML protected void initialize()
    {
        datePicker.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                datePicker.getEditor().clear();
                datePicker.setValue(null);
            }
        });
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker datePicker) {
                return new DateCell() {
                    @Override public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LocalDate.of(2020, 1, 22)))
                        {
                            setDisable(true);
                        }
                        if (item.isAfter(LocalDate.now().minusDays(1)))
                        {
                            setDisable(true);
                        }
                    }
                };
            }
        });

        downloadData("global/count", null);
    }

    @FXML private void apply(ActionEvent e)
    {
        String target = countryTextField.getText().equalsIgnoreCase("global") || countryTextField.getText().length() < 1 ? "global/count" : "country/" + countryTextField.getText().toUpperCase();
        String targetDate = null;

        LocalDate localDate = datePicker.getValue();
        if (localDate != null)
        {
            targetDate = localDate.getYear() + "-" + String.format("%02d", localDate.getMonth().getValue()) + "-" + String.format("%02d", localDate.getDayOfMonth());
        }

        downloadData(target, targetDate);
    }

    private static Map<String, Result> coronaMap = new HashMap<>();

    private void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    private void clearResults()
    {
        tableView.getItems().clear();
    }

    private void downloadData(String target, String targetDate)
    {
        coronaMap.clear();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(
                URI.create("https://covidapi.info/api/v1/" + target)
        ).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        clearResults();
        if (targetDate == null) {
            for (String date : coronaMap.keySet()) { addResult(coronaMap.get(date)); }
        } else {
            for (String date : coronaMap.keySet()) {
                if (date.equalsIgnoreCase(targetDate)) addResult(coronaMap.get(date));
            }
        }

        TableColumn dateColumn = tableView.getColumns().get(0);
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().setAll(dateColumn);
    }

    private static String jsonParse(String responseString)
    {
        if (responseString.contains("404 Not Found"))
        {
            coronaMap.put("ERROR", new Result("ERROR", 404, 404, 404));
            return null;
        }

        JSONObject jsonObject = new JSONObject(responseString);
        JSONArray resultDates = jsonObject.getJSONObject("result").names();

        String[] resultDatesSorted = new String[resultDates.length()];
        for (int i = 0; i < resultDates.length(); i++) {
            resultDatesSorted[i] = resultDates.get(i).toString();
        }
        Arrays.sort(resultDatesSorted);

        for (String res: resultDatesSorted) {
            JSONObject jsonResult = jsonObject.getJSONObject("result").getJSONObject(res);

            String date = res;
            String confirmed = String.valueOf(jsonResult.getInt("confirmed"));
            String deaths = String.valueOf(jsonResult.getInt("deaths"));
            String recovered = String.valueOf(jsonResult.getInt("recovered"));

            coronaMap.put(date, new Result(date, Long.parseLong(confirmed), Long.parseLong(deaths), Long.parseLong(recovered)));
        }

        return null;
    }

}