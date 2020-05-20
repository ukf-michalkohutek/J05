package CoronaStat;
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
    @FXML private TextField monthField;
    @FXML private TextField dayField;
    @FXML private Label confirm;
    private static String answer;

    @FXML protected void initialize() {
        downloadData();
    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    private String[] locales = Locale.getISOCountries();
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request;

    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    void downloadData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/country/SVK")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));

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

    private static String countryParse(String responseString) {
        JSONObject obj = new JSONObject(responseString);
        JSONArray date = obj.getJSONObject("result").names();
        JSONObject result = obj.getJSONObject("result").getJSONObject(date.get(0).toString());
        String confirmed = String.valueOf(result.getInt("confirmed"));
        String deaths = String.valueOf(result.getInt("deaths"));
        String recovered = String.valueOf(result.getInt("recovered"));
        answer = "This Day: " + confirmed + " confirmed, " + deaths + " deaths, " + recovered + " recovered";
        return null;
    }

    private static String globalParse(String responseString) {
        JSONObject obj = new JSONObject(responseString);
        JSONObject result = obj.getJSONObject("result");
        String confirmed = String.valueOf(result.getInt("confirmed"));
        String deaths = String.valueOf(result.getInt("deaths"));
        String recovered = String.valueOf(result.getInt("recovered"));
        answer = "Global: " + confirmed + " confirmed, " + deaths + " deaths, " + recovered + " recovered";
        return null;
    }

    @FXML
    protected void setGlobal() {
        countryField.setText("Global");
        monthField.setText("");
        dayField.setText("");
        confirm.setText("------------------------------------------------------------");
    }

    @FXML
    protected void fieldClear() {
        countryField.setText("");
        monthField.setText("");
        dayField.setText("");
        confirm.setText("------------------------------------------------------------");
    }

    @FXML
    protected void updateCountry() {
        tableView.getItems().clear();
        resultHashMap.clear();
        String countryText = "";
        String monthText = "";
        String dayText = "";
        if (countryField.getText().equals("") && monthField.getText().equals("") && dayField.getText().equals("")) {
            countryField.setText("SVK");
            //monthField.setText("1");
            //dayField.setText("22");
        }
        if (countryField.getText().equals("Global")) {
            countryText = "https://covidapi.info/api/v1/global";
            if (monthField.getText().equals("")) {monthText = "";}
            else {monthText = monthField.getText();}
            if (dayField.getText().equals("")) {dayText = "";}
            else {dayText = dayField.getText();}
            if (!(monthField.getText().equals("") || dayField.getText().equals(""))) {countryText = countryText + "/2020-" + monthText + "-" + dayText;}
            request = HttpRequest.newBuilder().uri(URI.create(countryText)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::globalParse)
                    .join();
            confirm.setText(answer);
        }
        else {
            for (String countryCode : locales) {
                Locale locale = new Locale("", countryCode);
                if ((locale.getDisplayCountry(locale).equals(countryField.getText())) || (locale.getCountry().equals(countryField.getText())) || (locale.getISO3Country().equals(countryField.getText())) ) {
                    countryText = "https://covidapi.info/api/v1/country/" + locale.getISO3Country();
                    break;
                }
            }
            request = HttpRequest.newBuilder().uri(URI.create(countryText)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jsonParse)
                    .join();
            for (String date : resultHashMap.keySet()) {addResult(resultHashMap.get(date));}
            String date[] = java.time.LocalDate.now().toString().split("-");
            monthText = date[1];
            dayText = date[2];
            if (monthField.getText().equals("")) {monthText = "01";}
            else {monthText = monthField.getText();}
            if (dayField.getText().equals("")) {dayText = "22";}
            else {dayText = dayField.getText();}
            countryText = countryText + "/2020-" + monthText + "-" + dayText;
            request = HttpRequest.newBuilder().uri(URI.create(countryText)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::countryParse)
                    .join();
            confirm.setText(answer);
        }

    }
}