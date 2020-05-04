

1. Pridaný ComboBox na vyberanie krajín. Dáta sa aktualizujú automaticky po vybratí krajiny.
   Krajiny dostupné pre CovidAPI sú ziskáne z "https://github.com/backtrackbaba/covid-api/blob/master/data/country_name_to_iso.json".
   Tento JSON je stiahnutý do src, pretože som nemohol nájsť link. Ručne som ho ešte upravil, lebo stále obsahoval pár chybných krajín.
   Uvažoval som aj nad použítím API "https://restcountries.eu/" pre získanie krajín, ale pre veľa z nich (asi 50) neboli v CovidAPI žiadne štatistiky.

2. Pridaný DatePicker na vyberanie informácii zo špecifického dátumu. Dáta sa aktualizujú automaticky po vybratí krajiny.
   DatePicker je obmedzený na dátumy dostupné pre CovidAPI.
   DatePicker nie je editovateľný, ale dá sa premazať, keď je vo focuse, stlačením DELETE alebo BACKSPACE.

   Pri zmene dátumu sa len vyberá dáta podľa dátumu z TreeMap data, a nesťahuje sa nanovo z CovidAPI. Nové dáta sa stahujú len pri zmene krajiny.
   Program je takto pri menení dátumov omnoho rýchlejší a šetrí aj CovidAPI servér.
   Dáta sú rovnaké ako keby sme ich stiahli napr. z "https://covidapi.info/api/v1/country/SVK/2020-03-03".

3. Pridaná funkcia zobrazenia globálnych štatistík, výberom "Global" na začiatku ComboBoxu countrySelect.

4. Parametre confirmed, deaths a recovered v classe Result zmenené na int.
   Dôvod je, že ak boli dané ako String alebo SimpleStringProperty, v tabuľke sa pri zoradení daných stlpcov zoraďovalo abecedne a nie logicky podľa číšeľ.

5. Info v tabuľke sa automaticky pri zmene krajiny usporiadava podľa dátumu, od najnovšieho po najstarší.
