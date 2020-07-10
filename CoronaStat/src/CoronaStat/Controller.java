package CoronaStat;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;

public class Controller {
    @FXML
    private TableView<Result> tableView;
    @FXML
    private TextField country;
    @FXML
    private TextField month;
    @FXML
    private TextField day;
    @FXML

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    @FXML
    protected void initialize() {
        downloadData();
    }

    @FXML
    protected void downloadData() {
        tableView.getItems().clear();
        resultHashMap.clear();
        boolean divnyFormat = false;

        String path = "https://covidapi.info/api/v1/";

        if (country.getText().equalsIgnoreCase("")) {
            //format: {"count":169,"result":{"2020-01-22":{"confirmed":0,"deaths":0,"recovered":0},"2020-01-23":{"confirmed":0,"deaths":0,"recovered":0},
            path += "global";
            if (month.getText().equals("") || day.getText().equals("")) { //ak nie je vyplneny datum
                path += "/count";
            } else {
                // format: "count":1,"date":"2020-07-08","result":{"confirmed":12041480,"deaths":549468,"recovered":6586726}
                divnyFormat = true;
                if (!month.getText().equals("") && !day.getText().equals("")) {
                    path += "/2020-" + month.getText() + "-" + day.getText();
                }
            }
        } else {
            path += "country/" + country.getText().toUpperCase(); //aby vzdy bolo velkymi
            if (!month.getText().equals("") && !day.getText().equals("")) {
                path += "/2020-" + month.getText() + "-" + day.getText();
            }
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(path)).build();

        if (divnyFormat)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jsonParseDivny)
                    .join();
        else client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        for (String date : resultHashMap.keySet()) tableView.getItems().add(resultHashMap.get(date));
    }

    private static String jsonParse(String responseString) {
        JSONObject result = new JSONObject(responseString);
        JSONArray results = result.getJSONObject("result").names();

        String[] resultDatesSorted = new String[results.length()];

        for (int i = 0; i < results.length(); i++) {
            resultDatesSorted[i] = results.get(i).toString();
        }

        Arrays.sort(resultDatesSorted);

        for (String date : resultDatesSorted) {
            JSONObject resultOnDate = result.getJSONObject("result").getJSONObject(date);

            String confirmed = String.valueOf(resultOnDate.getInt("confirmed"));
            String deaths = String.valueOf(resultOnDate.getInt("deaths"));
            String recovered = String.valueOf(resultOnDate.getInt("recovered"));

            resultHashMap.put(date, new Result(date, confirmed, deaths, recovered));
        }
        return null;
    }

    private static String jsonParseDivny(String responseString) {
        JSONObject result = new JSONObject(responseString);
        JSONObject resultOnDate = result.getJSONObject("result");

        String date = result.getString("date");
        String confirmed = String.valueOf(resultOnDate.getInt("confirmed"));
        String deaths = String.valueOf(resultOnDate.getInt("deaths"));
        String recovered = String.valueOf(resultOnDate.getInt("recovered"));

        resultHashMap.put(date, new Result(date, confirmed, deaths, recovered));
        return null;
    }
}


