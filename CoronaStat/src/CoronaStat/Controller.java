package CoronaStat;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;

public class Controller {
    @FXML private TableView<Result> tableView;
    @FXML private TextField krajina;
    @FXML private Text SvetText;

    @FXML protected void initialize() {
        stiahnutData();
    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();

    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }

    @FXML public void stiahnutData() {
        String krajina =  krajina.getText();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/country/SVK")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)

                .thenApply(Controller::jsonParse)
                .join();

        for (String date : resultHashMap.keySet()) addResult(resultHashMap.get(date));

    }



        static JSONArray globalnastatistika;
        @FXML public void stiahnutSvetData() {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/global")).build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Controller::globalJsonParse)
                    .join();
        }

        private static String globalJsonParse(String odpoved)  {
            globalnastatistika = new JSONArray("[" + odpoved + "]" );
            System.out.println(globalnastatistika);
            return null;
        }

    private static String jsonParse(String odpoved) {
        JSONArray vysledky = new JSONArray("[" + odpoved + "]");

        JSONArray datamvysledkov = vysledky.getJSONObject(0).getJSONObject("výsledky").names();

        String[] resultDatesSorted = new String[datamvysledkov.length()];

        for (int x = 0; x < datamvysledkov.length(); x++) {
            resultDatesSorted[x] = datamvysledkov.get(x).toString(); }

        Arrays.sort(resultDatesSorted);

        for (String res : resultDatesSorted) {
            JSONObject vysledok = vysledky.getJSONObject(0).getJSONObject("vysledok").getJSONObject(res);

            String date = res;

            String deaths = String.valueOf(vysledok.getInt("Úmrtia"));

            String confirmed = String.valueOf(vysledok.getInt("Potvrdenené"));

            String recovered = String.valueOf(vysledok.getInt("Uzdravenie"));

            resultHashMap.put(res, new Result(res,confirmed,recovered,deaths));
        }

        return null;
    }
}


