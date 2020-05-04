package CoronaStat;
import javafx.beans.property.SimpleStringProperty;

public class Result {
    private SimpleStringProperty date = new SimpleStringProperty("");
    private int confirmed;
    private int deaths;
    private int recovered;

    public Result() { this("",0,0,0); }

    public Result(String date, int confirmed, int deaths, int recovered) {
        setDate(date);
        setConfirmed(confirmed);
        setDeaths(deaths);
        setRecovered(recovered);
    }

    // Gettery
    public String getDate() { return date.get(); }
    public int getConfirmed() { return confirmed; }
    public int getDeaths() { return deaths; }
    public int getRecovered() { return recovered; }

    // Settery
    public void setDate(String date) { this.date.set(date); }
    public void setConfirmed(int confirmed) { this.confirmed=confirmed; }
    public void setDeaths(int deaths) { this.deaths=deaths; }
    public void setRecovered(int recovered) { this.recovered=recovered; }
}
