package main.cs5340.topaz_turtles;

import java.util.ArrayList;

/**
 * The main class of the application.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || args.length > 1) {
            System.out.println("Usage: infoextract <textfile>");
            System.exit(0);
        }

        String file_name = args[0];
        Parser.parseFile(file_name);
        ArrayList<Document> all_documents = Parser.getAllDocs();
        for(Document d : all_documents){
            System.out.println("ID: " + d.getFilename());
        }

        System.out.println("It works!");
    }



}
