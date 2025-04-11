# inverted_index
Diese Java-Anwendung erstellt einen invertierten Index aus einer Reihe von lokalen Textdateien und speichert die Ergebnisse in einer MySQL-Datenbank. 
Sie verarbeitet bis zu 10 Textdateien, tokenisiert deren Inhalt, entfernt gängige deutsche Stoppwörter und erstellt einen durchsuchbaren Index für spätere 
Abfragen oder Analysen.


+ Erstelle eine Tabelle documents (id, title, text)

+ Schreibe eine Java-Programm, das einen Inverted Index selbst erstellt.

    ++ Schreibe eine Funktion add("name.txt"), die eine Text-Datei speichert und zum Index hinzufügt.
    ++ Der Inverted Index soll in einer separaten MySQL-Tabelle gespeichert werden
    ++ Überlege dir, was du speichern willst
    ++ Überlege dir, welche Stopwörter du filterst

+ Füge 10 sinvolle Dokumente zu mind. 500 Wörter zu deinem Index hinzu.

    ++ Lange Rezepte
    ++ Kurze Bücher
    ++ Reportagen und Zeitungsartikel
    ++ Tutorials
    ++ Produktbeschreibungen

+ Jedes mal, wenn ein Dokument hinzugefügt wird, soll der Index upgedated werden.

+ Erstelle eine Funktion search("use some words"), mit der man suchen kann und sieht, wo in welchem Dokument das Wort gefunden wird.

    ++ Die Suche nutzt natürlich deinen Index.

+ Überlege dir einen Algorithmus, der das Ranking der Ergebnisse steuert.

## Bonus:
+ Erstelle einen nativen Full-Text-Index und vergleiche die Ergebnisse.

# License
This project is licensed under the SEW5 License - see the [LICENSE](license) file for details.
