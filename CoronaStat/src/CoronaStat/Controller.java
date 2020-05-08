package CoronaStat;

import javafx.collections.ObservableList;
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
    private TextField countryCode;
    @FXML
    private TextField month;
    @FXML
    private TextField day;
    private boolean blackSheep;

    @FXML
    protected void initialize() {
        findResults();
    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    @FXML
    void findResults() {
        tableView.getItems().clear();
        resultHashMap.clear();
        String path = buildURI();
        downloadData(path);
    }

    String buildURI() {
        String uri = "https://covidapi.info/api/v1/";
        boolean isSpecifiedDate = !(month.getText().equals("") || day.getText().equals(""));
        blackSheep = false;
        if (countryCode.getText().equalsIgnoreCase("")) {
            uri += "global";
            if (!isSpecifiedDate) {
                uri += "/count";
            } else blackSheep = true;
        } else {
            String upperCaseCode = countryCode.getText().toUpperCase();
            uri += "country/" + upperCaseCode;
        }

        if (isSpecifiedDate) {
            uri += "/2020-" + month.getText() + "-" + day.getText();
        }
        return uri;
    }

    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    void downloadData(String path) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(path)).build();

        if (!blackSheep)
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jsonParse)
                    .join();
        else client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParseThatOneBlackSheep)
                .join();


        for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));
    }

    private static String jsonParse(String responseString) {
//        JSONArray results = new JSONArray("[" + responseString + "]");
//        JSONArray resultDates = results.getJSONObject(0).getJSONObject("result").names();

        JSONObject result = new JSONObject(responseString);
        JSONArray resultDates = result.getJSONObject("result").names();

        String[] resultDatesSorted = new String[resultDates.length()];

        for (int i = 0; i < resultDates.length(); i++) {
            resultDatesSorted[i] = resultDates.get(i).toString();
        }

        Arrays.sort(resultDatesSorted);

        for (String date : resultDatesSorted) {
            JSONObject resultOnDate = result.getJSONObject("result").getJSONObject(date);

//            String date = day;
            String confirmed = String.valueOf(resultOnDate.getInt("confirmed"));
            String deaths = String.valueOf(resultOnDate.getInt("deaths"));
            String recovered = String.valueOf(resultOnDate.getInt("recovered"));

            resultHashMap.put(date, new Result(date, confirmed, deaths, recovered));
        }
        return null;
    }

    private static String jsonParseThatOneBlackSheep(String responseString) {
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


