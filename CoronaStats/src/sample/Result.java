package sample;

public class Result {

    private String date;
    private int confirmed;
    private int recovered;
    private int deaths;

    Result(String date, int confirmed, int recovered, int deaths) {
        this.date = date;
        this.confirmed = confirmed;
        this.recovered = recovered;
        this.deaths = deaths;
    }

    public String getDate() {
        return date;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public int getRecovered() {
        return recovered;
    }

    public int getDeaths() {
        return deaths;
    }
}
