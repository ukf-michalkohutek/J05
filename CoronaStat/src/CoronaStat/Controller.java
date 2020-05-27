package CoronaStat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.TreeSet;

public class Controller {

    private static final HashMap<String, Result> results = new HashMap<>();
    private static String country;
    private static String date;

    @FXML private TableView<Result> tableView;
    @FXML private DatePicker dateP;
    @FXML private ComboBox<String> countryC;
    @FXML private Button downloadBtn;

    private static String parseData(String responseString) {
        try {
            if (country.equals("GLOBAL") && date != null) {
                JSONObject obj = new JSONObject(responseString);
                String date = obj.getString("date");
                JSONObject day = obj.getJSONObject("result");
                String confirmed = "" + day.getInt("confirmed");
                String deaths = "" + day.getInt("deaths");
                String recovered = "" + day.getInt("recovered");
                results.put(date, new Result(date, confirmed, deaths, recovered));
            }
            else {
                JSONObject result = new JSONObject(responseString).getJSONObject("result");
                for (String k : result.keySet()) {
                    JSONObject day = result.getJSONObject(k);
                    String confirmed = "" + day.getInt("confirmed");
                    String deaths = "" + day.getInt("deaths");
                    String recovered = "" + day.getInt("recovered");
                    results.put(k, new Result(k, confirmed, deaths, recovered));
                }
            }
        } catch (Exception e) {
            System.out.println("No info.");
        }
        return null;
    }

    @FXML
    protected void initialize() {
        downloadBtn.setOnAction(e -> download());
        countryC.getItems().add("GLOBAL");
        try {
            BufferedReader br = new BufferedReader(new FileReader("src\\countries.json"));
            TreeSet<String> countries = new TreeSet<>();
            String country;
            while ((country = br.readLine()) != null) {
                countries.add(country);
            }
            br.close();

            for (String s : countries) {
                countryC.getItems().add(s);
            }
        } catch (Exception ignored) {
        }
    }

    private void download() {
        date = (dateP.getValue()!=null) ? dateP.getValue().toString() : null;
        country = countryC.getSelectionModel().getSelectedItem();

        if (date == null) {
            if (country.equals("GLOBAL")) downloadData("https://covidapi.info/api/v1/global/count");
            else downloadData("https://covidapi.info/api/v1/country/" + country);
        } else {
            if (country.equals("GLOBAL")) downloadData("https://covidapi.info/api/v1/global/" + date);
            else downloadData("https://covidapi.info/api/v1/country/" + country + "/" + date);
        }
        }



    private void displayData() {
        tableView.getItems().clear();
        for (String key : results.keySet()) {
            tableView.getItems().add(results.get(key));
        }
        tableView.getSortOrder().add(tableView.getColumns().get(0));

        dateP.setValue(null);
    }

    private void downloadData(String requestString) {
        results.clear();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(requestString)).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::parseData)
                .join();

        displayData();
    }
}