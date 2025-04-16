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

    // Read the full content of a file from the given repo URL
    public static String readFromUrl(String urlStr) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(urlStr);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // Add each line to the text
            }
        }
        return content.toString();
    }

    // Add document to the database and update inverted index
    public static void add(String url) throws Exception {
        String text = readFromUrl(url);
        String title = url.substring(url.lastIndexOf('/') + 1);  // extract file name from URL

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            // Insert document into the documents table in the database
            PreparedStatement insertDoc = conn.prepareStatement(
                    "INSERT INTO documents (title, text) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDoc.setString(1, title);
            insertDoc.setString(2, text);
            insertDoc.executeUpdate();

            // Get generated document ID
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
            // Insert words into the inverted index
            PreparedStatement insertIndex = conn.prepareStatement(
                    "INSERT INTO inverted_index (word, document_id, frequency) VALUES (?, ?, ?)");
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                insertIndex.setString(1, entry.getKey());
                insertIndex.setInt(2, docId);
                insertIndex.setInt(3, entry.getValue());
                insertIndex.addBatch();
            }
            insertIndex.executeBatch(); // Save all words in one go
        }
    }

    // Search method using the inverted index
    public static void search(String query) {

        // Split query into words, make lowercase, and remove symbols
        String[] terms = query.toLowerCase().split("\\W+");

        // Filter out short words and stopwords
        List<String> filteredTerms = new ArrayList<>();
        for (String term : terms) {
            if (term.length() > 1 && !stopwords.contains(term)) {
                filteredTerms.add(term);
            }
        }

        // Stores document scores and titles
        Map<Integer, Integer> documentScores = new HashMap<>();
        Map<Integer, String> documentTitles = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

            // Prepare SQL to get matching documents and their word frequency
            PreparedStatement getDocs = conn.prepareStatement(
                    "SELECT ii.document_id, ii.frequency, d.title " +
                            "FROM inverted_index ii JOIN documents d ON ii.document_id = d.id " +
                            "WHERE word = ?");

            // For each word in the query
            for (String term : filteredTerms) {
                getDocs.setString(1, term);
                ResultSet rs = getDocs.executeQuery();

                // Go through all documents containing the word
                while (rs.next()) {
                    int docId = rs.getInt("document_id");
                    int freq = rs.getInt("frequency");
                    String title = rs.getString("title");

                    // Add frequency to score for this document
                    documentScores.put(docId, documentScores.getOrDefault(docId, 0) + freq);
                    // Save document title
                    documentTitles.put(docId, title);
                }
            }

            // Show search results sorted by score (highest first)
            System.out.println("\nResults for the search of: \"" + query + "\"");
            documentScores.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> {
                        int docId = entry.getKey();
                        int score = entry.getValue();
                        System.out.println("Document: " + documentTitles.get(docId) + " (Score: " + score + ")");
                    });

        } catch (SQLException e) {
            System.err.println("Error in search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Native MySQL Full-Text-Search
    public static void nativeFulltextSearch(String query) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

            // Use MySQL MATCH ... AGAINST for fulltext relevance scoring
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT title, MATCH(text) AGAINST (? IN NATURAL LANGUAGE MODE) AS relevance " +
                            "FROM documents WHERE MATCH(text) AGAINST (? IN NATURAL LANGUAGE MODE) " +
                            "ORDER BY relevance DESC"
            );
            stmt.setString(1, query);
            stmt.setString(2, query);

            ResultSet rs = stmt.executeQuery();
            System.out.println("\nResult for native fulltext-search: \"" + query + "\"");
            while (rs.next()) {
                System.out.println(" Document: " + rs.getString("title") + " (Relevance: " + rs.getDouble("relevance") + ")");
            }

        } catch (SQLException e) {
            System.err.println("Error in native-fulltext-search: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        // Repo  URL to get the 10 text files
        String baseUrl = "https://raw.githubusercontent.com/najda23/inverted_index/refs/heads/main/text";
        for (int i = 1; i <= 10; i++) {
            String url = baseUrl + i + ".txt";
            try {
                add(url); // Add document and update index
                System.out.println("Data " + url + " are being searched and indexed.");
            } catch (Exception e) {
                System.err.println("Error in processing " + url);
                e.printStackTrace();
            }
        }

        // Run both search types
        search("generative");
        nativeFulltextSearch("Gender");
    }
}
