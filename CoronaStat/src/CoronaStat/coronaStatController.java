package CoronaStat;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.TreeMap;

public class coronaStatController {

    // Anglický názov krajiny -> ISO code krajiny
    private static TreeMap<String,String> countryMap = new TreeMap<>();
    // Dátum -> Result
    private static TreeMap<String,Result> data = new TreeMap<>();

    private String selectedCountry = "";
    private String selectedDate = "";

    @FXML private TableView<Result> tableView;
    @FXML private ComboBox<String> countrySelect;
    @FXML private DatePicker datePicker;

    @FXML protected void initialize() {
        loadCountries();
        initCountrySelect();
        initDatePicker();
    }

    /*
        Country select ComboBox
    */

    // Inicializácia ComboBoxu
    private void initCountrySelect() {
        countrySelect.getItems().clear();
        countrySelect.setVisibleRowCount(20);

        countrySelect.getItems().add("Global");
        for(String key: countryMap.keySet()) {
            countrySelect.getItems().add(key);
        }

        // Nastavenie default krajiny na Slovensko
        countrySelect.getSelectionModel().select("Slovakia");
        selectedCountry="Slovakia";
        downloadNewCountryData();
        displayData();

        // Stiahnutie a zobrazenie nových dát pri zmene vybranej krajiny
        countrySelect.getSelectionModel().selectedItemProperty().addListener((v,oldV,newV)->{
            selectedCountry = newV;
            downloadNewCountryData();
            displayData();
        });
    }

    // Načítanie krajín z JSON súboru
    void loadCountries() {
        countryMap.clear();

        String responseString = null;
        try {
            responseString = Files.readString(Paths.get("src/countries.json"));
        } catch (Exception e) { System.out.println(e.getMessage()); }

        parseCountries(responseString);
    }

    // Parsovanie krajín do mapy countryMap
    private static void parseCountries(String responseString) {
        JSONObject jsonObject = new JSONObject(responseString);

        for(String key: jsonObject.keySet())
            countryMap.put(key,jsonObject.getString(key));

    }

    /*
        DatePicker
    */

    // Inicializácia DatePickera
    private void initDatePicker() {
        // Obmezdenie datePickera na dátumy, pre ktoré je dostupný CovidAPI
        // minDate je 2020-01-22 (najstarší dátum v CovidAPI)
        // maxDate sa zistí z posledného stiahnutého Resultu

        String[] maxDateAvailable = data.get(data.lastKey()).getDate().split("-");
        int maxYear = Integer.parseInt(maxDateAvailable[0]);
        int maxMonth = Integer.parseInt(maxDateAvailable[1]);
        int maxDay = Integer.parseInt(maxDateAvailable[2]);

        LocalDate maxDate = LocalDate.of(maxYear, maxMonth, maxDay);
        LocalDate minDate = LocalDate.of(2020, 1, 22);
        datePicker.setDayCellFactory(d -> new DateCell() {
                    @Override public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(item.isAfter(maxDate) || item.isBefore(minDate));
                    }});

        // Delete/Backspace vyresetuje datePicker
        datePicker.getEditor().setOnKeyPressed(e->{
            switch (e.getCode()) {
                case BACK_SPACE:
                case DELETE:
                    datePicker.setValue(null);
                    datePicker.getEditor().clear();
            }
        });

        // Zobrazenie špecifického dátumu vždy pri zmene dataPickera
        datePicker.valueProperty().addListener(e->{
            selectedDate = (datePicker.getValue()==null) ? "" : datePicker.getValue().toString();
            displayData();
        });
    }

    /*
        CovidAPI data handling
    */

    // Vloží data do tabuľky, kontroluje či má ukázať všetky dátumy alebo len jeden špecifický
    private void displayData() {
        tableView.getItems().clear();

        if (selectedDate.equals("")) {
            for(String key : data.keySet())
                tableView.getItems().add(data.get(key));
        }
        else {
            tableView.getItems().add(data.get(selectedDate));
        }

        tableView.getSortOrder().add(tableView.getColumns().get(0));
        tableView.scrollTo(0);
    }

    // Upraví URL na správnu formu a zavolá downloadData
    private void downloadNewCountryData() {
        String requestString = "https://covidapi.info/api/v1";

        if (selectedCountry.equals("Global")) requestString += "/global/count";
        else requestString += "/country/" + countryMap.get(selectedCountry);

        downloadData(requestString);
    }

    // Sťahuje dáta z CovidAPI
    private void downloadData(String requestString) {
        data.clear();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(requestString)).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(coronaStatController::parseData)
                .join();
    }

    // Parsuje dáta z CovidAPI do mapy data
    private static String parseData(String responseString) {
        JSONObject results = new JSONObject(responseString).getJSONObject("result");

        for (String key: results.keySet()) {
            JSONObject oneResult = results.getJSONObject(key);
            String date = key;
            int confirmed = oneResult.getInt("confirmed");
            int deaths = oneResult.getInt("deaths");
            int recovered = oneResult.getInt("recovered");

            data.put(date, new Result(date, confirmed, deaths, recovered));
        }

        return null;
    }
}


