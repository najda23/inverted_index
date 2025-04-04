package org.example;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class Main {

    private static final Set<String> stopwords = Set.of(
            "der", "die", "das", "und", "ein", "eine", "ist", "in", "am", "zu", "mit", "auf", "für"
    );

    private static final String URL = "jdbc:mysql://htl-projekt.com:3306/your_database_name";  // Replace 'your_database_name' with your actual database name
    private static final String USER = "lejdifusha";
    private static final String PASSWORD = "!Insy_2023$";

}