package sample;

import java.util.Scanner;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;

public class Controller {

    static HashMap<String, Result> data = new HashMap();
    @FXML private TableView<Result> tableView;
    Scanner in = new Scanner(System.in);
    String krajina;

    @FXML protected void initialize() {
        // primitívny výber krajiny
        System.out.println("\033[1;92m"+"Napíš 3-miestny kód krajiny,"+"\n"+"Pre globálne štastiky napíš global");
        krajina = in.nextLine();
        String doplnenie;
        if (krajina.length()==3) {
            krajina = "country/"+krajina;
        } else if (krajina.equals("global")){
            krajina = "global/count";
        } else {
            System.out.println("\033[1;91m"+"Neplatný vstup");
            System.exit(0);
        }
        donwload();
        displaydata();
    }

    public void donwload() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://covidapi.info/api/v1/"+krajina)).build();


        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Controller::parse)
                .join();
    }

    public static String parse(String responseString) {

        JSONObject result = new JSONObject(responseString).getJSONObject("result");

        for (String key: result.keySet()) {
            String date = key;
            int confirmed = result.getJSONObject(date).getInt("confirmed");
            int recovered = result.getJSONObject(date).getInt("recovered");
            int deaths = result.getJSONObject(date).getInt("deaths");

            data.put(date,new Result(date,confirmed,recovered,deaths));
        }
        return null;
    }

    public void displaydata() {
        tableView.getItems().clear();
        for (String key: data.keySet())
            tableView.getItems().add(data.get(key));
        tableView.getSortOrder().add(tableView.getColumns().get(0));
        tableView.scrollTo(9999);
    }
}
