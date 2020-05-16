package CoronaStat;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;

public class Controller {
    @FXML private TableView<Result> tableView;
    @FXML private TextField country;
    @FXML private Text globalText;

    @FXML protected void initialize() {

    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    @FXML public void downloadData() {
        clearTable();
        String coun = country.getText();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/country/"+coun)).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));
    }

    static JSONArray globalstats;
    @FXML public void downloadGlobalData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/global")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::globalJsonParse)
                .join();
    }

    private static String globalJsonParse(String response)  {
        globalstats = new JSONArray("[" + response + "]" );
        System.out.println(globalstats);
        System.out.println("nevedel som to dostat do textu :/");
        return null;
    }

    private static String jsonParse(String responseString) {
        JSONArray results = new JSONArray("[" + responseString + "]");
        JSONArray resultDates = results.getJSONObject(0).getJSONObject("result").names();

        String[] resultDatesSorted = new String[resultDates.length()];

        for (int i = 0; i < resultDates.length(); i++) { resultDatesSorted[i] = resultDates.get(i).toString(); }

        Arrays.sort(resultDatesSorted);

        resultHashMap.clear();

        for (String res : resultDatesSorted) {
            JSONObject result = results.getJSONObject(0).getJSONObject("result").getJSONObject(res);

            String date = res;
            String confirmed = String.valueOf(result.getInt("confirmed"));
            String deaths = String.valueOf(result.getInt("deaths"));
            String recovered = String.valueOf(result.getInt("recovered"));

            resultHashMap.put(res, new Result(res,confirmed,deaths,recovered));
        }

        return null;
    }

    private void clearTable(){
        tableView.getItems().clear();
    }
}


