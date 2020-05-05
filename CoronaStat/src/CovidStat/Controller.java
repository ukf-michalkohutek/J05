package CovidStat;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
import java.util.Locale;

public class Controller {
    @FXML private TableView<Result> tableView;
    @FXML private TextField countryField;
    @FXML private TextField month;
    @FXML private TextField day;
    @FXML private Label toDate;
    private static String s;
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request;
    static HashMap<String, Result> resultHashMap = new HashMap<>();

    private String[] locales = Locale.getISOCountries();

    @FXML protected void initialize() {
        downloadData();
    }

    @FXML
    protected void updatePerson() {
        tableView.getItems().clear();
        resultHashMap.clear();
        String country = "";
        if (countryField.getText().equals("global")) {
            country = "https://covidapi.info/api/v1/global";
            String monthString = "";
            String dayString = "";
            if (month.getText().equals("")) monthString = "";
            else monthString = month.getText();
            if (day.getText().equals("")) dayString = "";
            else dayString = day.getText();
            if (!(month.getText().equals("") || day.getText().equals(""))) country = country + "/2020-" + monthString + "-" + dayString;
            request = HttpRequest.newBuilder().uri(URI.create(country)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::globalParse)
                    .join();
            toDate.setText(s);
        }
        else {
            for (String countryCode : locales) {
                Locale locale = new Locale("", countryCode);
                if ((locale.getDisplayCountry(locale).equals(countryField.getText())) || (locale.getCountry().equals(countryField.getText())) || (locale.getISO3Country().equals(countryField.getText())) ) {
                    country = "https://covidapi.info/api/v1/country/" + locale.getISO3Country();
                    break;
                }
            }
            request = HttpRequest.newBuilder().uri(URI.create(country)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jsonParse)
                    .join();
            for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));
            String date[] = java.time.LocalDate.now().toString().split("-");
            String monthString = date[1];
            String dayString = date[2];
            if (month.getText().equals("")) monthString = "01";
            else monthString = month.getText();
            if (day.getText().equals("")) dayString = "22";
            else dayString = day.getText();
            country = country + "/2020-" + monthString + "-" + dayString;
            request = HttpRequest.newBuilder().uri(URI.create(country)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::simpleParse)
                    .join();
            toDate.setText(s);
        }

    }
    private static String globalParse(String responseString) {
        JSONObject obj = new JSONObject(responseString);
        JSONObject result = obj.getJSONObject("result");
        String confirmed = String.valueOf(result.getInt("confirmed"));
        String deaths = String.valueOf(result.getInt("deaths"));
        String recovered = String.valueOf(result.getInt("recovered"));
        s = "Globálne " + confirmed + " potvrdených, " + deaths + " úmrtí a " + recovered + " uzdravených.";
        return null;
    }

    private static String simpleParse(String responseString) {
        JSONObject obj = new JSONObject(responseString);
        JSONArray date = obj.getJSONObject("result").names();
        JSONObject result = obj.getJSONObject("result").getJSONObject(date.get(0).toString());
        String confirmed = String.valueOf(result.getInt("confirmed"));
        String deaths = String.valueOf(result.getInt("deaths"));
        String recovered = String.valueOf(result.getInt("recovered"));
        s = "V daný deň: " + confirmed + " potvrdených, " + deaths + " úmrtí, a " + recovered + " uzdravených.";
        return null;
    }

    private static String jsonParse(String responseString) {

        JSONArray results = new JSONArray("[" + responseString + "]");
        JSONArray resultDates = results.getJSONObject(0).getJSONObject("result").names();

        String[] resultDatesSorted = new String[resultDates.length()];

        for (int i = 0; i < resultDates.length(); i++) { resultDatesSorted[i] = resultDates.get(i).toString(); }

        Arrays.sort(resultDatesSorted);

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

    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    void downloadData() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/country/SVK")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));

    }
}


