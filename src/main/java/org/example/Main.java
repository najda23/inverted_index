package org.example;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class Main {

    private static final Set<String> stopwords = Set.of(
            "der", "die", "das", "und", "ein", "eine", "ist", "in", "am", "zu", "mit", "auf", "für"
    );

    private static final String URL = "jdbc:mysql://htl-projekt.com:3306/2024_4by_lejdifusha_inverted_index";  // Replace 'your_database_name' with your actual DB name
    private static final String USER = "lejdifusha";
    private static final String PASSWORD = "!Insy_2023$";

    public static void add(String filename) throws Exception {
        String text = Files.readString(Paths.get(filename));
        String title = new File(filename).getName();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Save document
            PreparedStatement insertDoc = conn.prepareStatement(
                    "INSERT INTO documents (title, text) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDoc.setString(1, title);
            insertDoc.setString(2, text);
            insertDoc.executeUpdate();

            ResultSet keys = insertDoc.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Kein Dokument-ID erhalten!");
            int docId = keys.getInt(1);

            // Tokenize, filter, and count
            Map<String, Integer> wordCount = new HashMap<>();
            for (String word : text.toLowerCase().split("\\W+")) {
                if (word.length() > 1 && !stopwords.contains(word)) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
            // Save inverted index
            PreparedStatement insertIndex = conn.prepareStatement(
                    "INSERT INTO inverted_index (word, document_id, frequency) VALUES (?, ?, ?)");
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                insertIndex.setString(1, entry.getKey());
                insertIndex.setInt(2, docId);
                insertIndex.setInt(3, entry.getValue());
                insertIndex.addBatch();
            }
            insertIndex.executeBatch();
        }
    }




        }
    }
}




