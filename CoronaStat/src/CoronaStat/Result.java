package CoronaStat;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;

public class Result {
    private final SimpleStringProperty date = new SimpleStringProperty("");
    private final SimpleLongProperty confirmed = new SimpleLongProperty();
    private final SimpleLongProperty deaths = new SimpleLongProperty();
    private final SimpleLongProperty recovered = new SimpleLongProperty();

    public Result(String date, long confirmed, long deaths, long recovered){
        setDate(date);
        setConfirmed(confirmed);
        setDeaths(deaths);
        setRecovered(recovered);
    }

    //setters
    public void setDate(String date) {this.date.set(date);}
    public void setConfirmed(long confirmed) {this.confirmed.set(confirmed);}
    public void setDeaths(long deaths) {this.deaths.set(deaths);}
    public void setRecovered(long recovered) {this.recovered.set(recovered);}

    //getters
    public String getDate(){return this.date.get();}
    public long getConfirmed(){return this.confirmed.get();}
    public long getDeaths(){return this.deaths.get();}
    public long getRecovered(){return this.recovered.get();}

}
