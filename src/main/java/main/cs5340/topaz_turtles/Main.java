package main.cs5340.topaz_turtles;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * The main class of the application.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar [name] [file1] [file2] ...");
            System.exit(0);
        }

        DataMuseWord[] words = DataMuse.getWordsRelatedTo("arson");
        for (DataMuseWord w : words)
            System.out.println(w.word);
    }
}
