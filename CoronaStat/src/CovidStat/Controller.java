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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Controller {

    public Label toDate;
    @FXML private TableView<Result> tableView;
    @FXML private TextField countryField;
    @FXML private TextField month;
    @FXML private TextField day;
    private static String s;
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request;
    private String[] locales = Locale.getISOCountries();
    static ArrayList<Result> temp = new ArrayList<Result>();

    @FXML protected void initialize(){
        downloadData();
    }

    void addResult(Result res){
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    void downloadData(){
        HttpClient client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/country/SVK")).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::jsonParse)
                .join();

        for (Result res : temp) addResult(res);

    }

    private static String jsonParse(String responseString){
        JSONArray outerArray =  new JSONArray("[" + responseString + "]");
        JSONArray resultDates = outerArray.getJSONObject(0).getJSONObject("result").names();

        String[] resultDatesSorted = new String[resultDates.length()];

        for (int i = 0; i<resultDates.length();i++ ) resultDatesSorted[i] = resultDates.get(i).toString();
        Arrays.sort(resultDatesSorted);

        for (String res: resultDatesSorted)
        {
            JSONObject result = outerArray.getJSONObject(0).getJSONObject("result").getJSONObject(res);

            String date = res.toString();
            String confirmed = String.valueOf(result.getInt("confirmed"));
            String deaths = String.valueOf(result.getInt("deaths"));
            String recovered = String.valueOf(result.getInt("recovered"));
            Result ress = new Result(date,confirmed,deaths,recovered);
            temp.add(ress);

        }
        return null;
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
        s = "Day: " + date + " " + confirmed + " confirmed, " + deaths + " deaths, a " + recovered + " recovered.";
        return null;
    }

    @FXML
    protected void updatePerson() {
        tableView.getItems().clear();
        temp.clear();
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
            for (Result r : temp) addResult(r);
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
            for (Result r : temp) addResult(r);
            String[] date = java.time.LocalDate.now().toString().split("-");
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

}
