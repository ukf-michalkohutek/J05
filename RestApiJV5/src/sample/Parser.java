package sample;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String inputText;
    private String country = null;
    private String year = null;
    private String month = null;
    private String day = null;


    public Parser(String inputText) {
        this.inputText = inputText;

        this.inputText = this.inputText.replaceAll("\\s+", " ").trim();

        this.parse();
    }

    public String getCountry() {
        return this.country;
    }

    public String getYear() {
        return this.year;
    }

    public String getMonth() {
        return this.month;
    }

    public String getDay() {
        return this.day;
    }

    private void parse() {
        Matcher matcher;



        String REGEX = "([A-Z][a-z]+|[A-Z]{3})(?:\\s([0-9]{4})\\.([0][1-9]|[1][0-2])\\.([0][1-9]|[1|2][0-9]|[3][0-1]))?";

        Pattern inputPattern = Pattern.compile(REGEX);

        if (inputPattern.matcher(this.inputText).matches()) {
            matcher = inputPattern.matcher(this.inputText);

            if (matcher.find()) {
                this.country = matcher.group(1);
                this.year = matcher.group(2);
                this.month = matcher.group(3);
                this.day = matcher.group(4);
            }
        }


    }
}
