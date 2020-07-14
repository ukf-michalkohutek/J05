package sample;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;

public class Controller {
    @FXML private TextField isocode;
    @FXML private TableView<Result> tableView;
    @FXML private Label vipis;
    @FXML private TextField isocode1;
    @FXML private TextField DATE;
    private iso IsoMap;

    @FXML protected void initialize() {

        IsoMap = new iso();

        //downloadData("country", "SVK");
    }

    static HashMap<String, Result> resultHashMap = new HashMap<>();



    void addResult(Result res) {
        ObservableList<Result> data = tableView.getItems();
        data.add(res);
    }
    @FXML public void isoVstup(){
        String isovstup = isocode.getText();
        isovstup = isovstup.toUpperCase();
        if(isovstup.length() == 3){
            boolean pom = false;
            for(int i = 0; i < isovstup.length();i++){
                char znak = isovstup.charAt(i);
                if(znak >=65 && znak <=90){
                    pom = true;
                } else {
                    vipis.setText("Zle zadane ISO");
                    pom = false;
                    break;
                }
            }

            if(pom){
                for (Map.Entry<String, String> entry : IsoMap.isocountry.entrySet()) {
                    System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    if(isovstup.equals(entry.getKey())){
                        vipis.setText("Zobrazujem hodnoty pre krajinu: "+entry.getValue());
                        downloadData("country", isovstup , "");
                        break;
                    } else {
                        vipis.setText("Zle zadany nazov");
                    }
                }
            }
        } else{
            vipis.setText("Zle zadane ISO");
        }
        System.out.println(isovstup);
    }

    @FXML public void datevstup(){
        String isovstup = isocode1.getText();
        String datum =  DATE.getText();
        boolean pom = false;
        if(isovstup.length() == 3){

            for(int i = 0; i < isovstup.length();i++){
                char znak = isovstup.charAt(i);
                if(znak >=65 && znak <=90){
                    pom = true;
                } else {
                    vipis.setText("Zle zadane ISO");
                    pom = false;
                    break;
                }
            }
        }
        if(datum.length() == 10){
            boolean pom1 = false;
            for(int i = 0; i < datum.length();i++){
                 char cislo = datum.charAt(i);
                    if((i != 4 || i != 7 ) && (cislo >= 48 && cislo <= 57 )){
                        pom1 = true;
                    } else if((i == 4 || i == 7 ) && (cislo == 45 )) {
                        pom1 = true;
                    } else {
                        pom1 = false;
                        vipis.setText("Zle zadany datum");
                        break;
                    }
            }
            if (pom1 && pom) {
                for (Map.Entry<String, String> entry : IsoMap.isocountry.entrySet()) {
                    System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    if(isovstup.equals(entry.getKey())){
                        vipis.setText("Zobrazujem hodnoty pre krajinu: "+entry.getValue());
                        downloadData("country", isovstup, datum);
                        break;
                    } else {
                        vipis.setText("Zle zadany nazov");
                    }
                }
            }


        }
    }

    @FXML public void global(){
        downloadData("global", "", "");
    }

    void downloadData(String type, String vstup, String datum) {
        tableView.getItems().clear();
        String url = "https://covidapi.info/api/v1/";
        if (type.equals("country")) {
            url += type + "/" +vstup;
            if (!datum.equals("")) {
                url += "/" + datum;
            }
        }
        else if(type.equals("global")){
            url += type;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

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
}


