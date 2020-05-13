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

        /*
            Riesenie bonusovej ulohy

            Regularny vyraz na parsovanie zadaneho textu
            V regexe zachytavam 4 grupy (krajina, rok, mesiac a den).
            Podla zadaneho textu mi jednotlive grupy mozu vratit dane podslovo alebo null.

            ********************************************************
            ******* Datum sa zadava vo formate YYYY.MM.DD **********
            ********************************************************


            Priklad 1 -> validny text:
                zadany text: SVK 2020.05.12
                grupa 1: SVK
                grupa 2: 2020
                grupa 3: 05
                grupa 4: 12

            Priklad 2 -> chyba datum:
                zadany text: SVK
                    grupa 1: SVK
                    grupa 2: null
                    grupa 3: null
                    grupa 4: null


            Priklad 3 -> chybny datum:
                zadany text: SVK 2020.13.05
                    grupa 1: SVK
                    grupa 2-4: null


            Mensia legenda: ?: -> tymto ignorujem (grupu) tu velku zatvorku kde je rok, mesiac a den
                            | -> alebo
                            + -> 1 alebo viac znakov
                           {3} -> musia byt 3 znaky
                           \\ -> escape znak
                           \\. -> znak 'bodky' lebo bez \\ to znamena hocico
                           [A-Z] -> Jeden znak z tohoto intervalu
                           \\s -> jedna medzera

             Pokial chcete zadavat krajinu ako kod tak pouzi ISO 3166-1 Alpha 3 kod :
                https://sk.wikipedia.org/wiki/ISO_3166-1


           !! Samozrejme vela veci sa da osetrit. Ako napr ked zadany kod krajiny nebude velkymi pismena tak regex padne.
           - Datumy by sa museli zvlast osetrit lebo napr regex kludne zobere aj datum 2020.04.31 avsak april nema 31 dni
           - Rok by sa musel tiez osetrit: Regex kludne zobere aj 9999.

         */

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
