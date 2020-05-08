package CovidStat;

import javafx.beans.property.SimpleStringProperty;
public class Result {
    private final SimpleStringProperty date = new SimpleStringProperty();
    private final SimpleStringProperty confirmed = new SimpleStringProperty();
    private final SimpleStringProperty deaths = new SimpleStringProperty();
    private final SimpleStringProperty recovered = new SimpleStringProperty();

    public Result (String date, String confirmed, String deaths, String recovered)
    {
        setDate(date);
        setConfirmed(confirmed);
        setDeaths(deaths);
        setRecovered(recovered);
    }
    public Result() {
        this("","","","");
    }

    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public String getConfirmed() {
        return confirmed.get();
    }

    public SimpleStringProperty confirmedProperty() {
        return confirmed;
    }

    public String getDeaths() {
        return deaths.get();
    }

    public SimpleStringProperty deathsProperty() {
        return deaths;
    }

    public String getRecovered() {
        return recovered.get();
    }

    public SimpleStringProperty recoveredProperty() {
        return recovered;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setConfirmed(String confirmed) {
        this.confirmed.set(confirmed);
    }

    public void setDeaths(String deaths) {
        this.deaths.set(deaths);
    }

    public void setRecovered(String recovered) {
        this.recovered.set(recovered);
    }
}
