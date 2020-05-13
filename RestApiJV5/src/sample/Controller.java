package sample;

import com.sun.security.jgss.GSSUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    @FXML private TableView<Result> tableView;
    @FXML private TextField CommandPane;
    private static String link = "https://covidapi.info/api/v1/global";

    @FXML protected void initialize() {
        this.downloadData();
    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    void addResult(Result result) {
        ObservableList<Result> data = this.tableView.getItems();

        data.add(result);
    }

    void downloadData() {
        resultHashMap.clear();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        try {
            request = HttpRequest.newBuilder().uri(URI.create(link)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jasonParser)
                    .join();
        }catch (Exception ex) {
            System.out.println("CHYBA: " + ex.getMessage());
        }


        for (String date : resultHashMap.keySet()) {
            addResult(resultHashMap.get(date));
        }

        this.CommandPane.setText("");
    }

    private static String jasonParser(String responseString) {

        JSONArray results = new JSONArray("[" + responseString + "]");

        JSONArray resultDates = results.getJSONObject(0).getJSONObject("result").names();

        String[] resultDateSorted = new String[resultDates.length()];

        for(int i = 0; i < resultDates.length(); i++) {
            resultDateSorted[i] = resultDates.get(i).toString();
        }

        if (link.matches(".*global.*")) {

            Object confirmed = results.getJSONObject(0).getJSONObject("result").get("confirmed");
            Object dates = results.getJSONObject(0).get("date");
            Object deaths = results.getJSONObject(0).getJSONObject("result").get("deaths");
            Object recovered = results.getJSONObject(0).getJSONObject("result").get("recovered");

            resultHashMap.put(dates.toString(), new Result(dates.toString(), confirmed.toString(), deaths.toString(), recovered.toString()));

        } else {
            Arrays.sort(resultDateSorted);
            for (String res : resultDateSorted) {
                JSONObject result = results.getJSONObject(0).getJSONObject("result").getJSONObject(res);
                String date = res;
                String confirmed =String.valueOf(result.getInt("confirmed"));
                String deaths =String.valueOf(result.getInt("deaths"));
                String recovered =String.valueOf(result.getInt("recovered"));

                resultHashMap.put(res, new Result(res, confirmed, deaths, recovered));


            }
        }
        return null;
    }


    public void execute(ActionEvent event) {

        this.tableView.getItems().clear();

        Parser parserString = new Parser(this.CommandPane.getText());


        String country = parserString.getCountry();


        if (country == null) {
            return;
        }

        String date = null;


        if (parserString.getYear() != null) {
            date = parserString.getYear() + "-" + parserString.getMonth() + "-" + parserString.getDay();
        }


        if (country.length() != 3) {
            this.getCountryFullName(country);
        }else {
            link = "https://covidapi.info/api/v1/country/" + country ;
        }


        if (date != null) {
            link += "/" + date;
        }

        this.downloadData();

    }

    private void getCountryFullName(String fullName) {


        String startLink = "https://restcountries.eu/rest/v2/name/";

        HttpClient client2 = HttpClient.newHttpClient();

        HttpRequest request;

        try {
            request = HttpRequest.newBuilder().uri(URI.create(startLink + fullName + "?fullText=true")).build();
            client2.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::jasonISOparser)
                    .join();
        }catch (Exception ex) {
            System.out.println("CHYBA: " + ex.getMessage());
        }

    }

    private static String jasonISOparser(String responseString) {

        JSONArray results = new JSONArray(responseString);


        String obj = results.getJSONObject(0).get("alpha3Code").toString();


        link = "https://covidapi.info/api/v1/country/" + obj;

        return null;

    }


    public void getHelp(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeight(300);
        alert.setTitle("Legend");
        alert.setHeaderText(null);
        alert.setContentText("Ako zadat prikaz: \n\n" +
                "1) Nazov krajiny (musi zacat velkym pismenom): Slovakia\n\n" +
                "2) Nazov krajiny + datum (YYYY.MM.DD): Slovakia 2020.03.12\n\n" +
                "3) Kod krajiny (alpha3): SVK\n\n" +
                "4) Kod krajiny (alpha3) + datum (YYYY.MM.DD): SVK 2020.10.09");

        alert.showAndWait();

        this.CommandPane.setText("");
    }

    public void getGlobal(ActionEvent event) {
        this.tableView.getItems().clear();
        link = "https://covidapi.info/api/v1/global";
        this.downloadData();
    }
}
