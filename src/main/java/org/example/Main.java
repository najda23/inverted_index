package org.example;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class Main {

    private static final Set<String> stopwords = Set.of(
            "der", "die", "das", "und", "ein", "eine", "ist", "in", "am", "zu", "mit", "auf", "für"
    );

    private static final String DB_URL = "jdbc:mysql://htl-projekt.com:3306/2024_4by_lejdifusha_inverted_index";
    private static final String USER = "lejdifusha";
    private static final String PASSWORD = "!Insy_2023$";

    // Helper method to read a file from a remote URL
    public static String readFromUrl(String urlStr) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(urlStr);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static void add(String url) throws Exception {
        String text = readFromUrl(url);
        String title = url.substring(url.lastIndexOf('/') + 1);  // extract file name from URL

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
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

    public static void main(String[] args) {
        // Replace with your actual GitHub username/repo/branch path
        String baseUrl = "https://raw.githubusercontent.com/najda23/inverted_index/refs/heads/main/text";
        for (int i = 1; i <= 10; i++) {
            String url = baseUrl + i + ".txt";
            try {
                add(url);
                System.out.println("Datei " + url + " wurde erfolgreich hinzugefügt und indexiert.");
            } catch (Exception e) {
                System.err.println("Fehler beim Verarbeiten von " + url);
                e.printStackTrace();
            }
        }
    }
}
