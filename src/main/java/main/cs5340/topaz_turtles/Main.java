package main.cs5340.topaz_turtles;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
        String s = parseFile(file_name);
        System.out.println(s);

        // the new document will have to be created for each appended document.
        // for now it will be the name of the input document for testing purposes.
        //Document doc = new Document(file_name, "/DEV-MUC3-0006");


        System.out.println("It works!");
    }

    /**
     * This method parses a file, and is probably only going to be used for testing.
     */
    public static String parseFile(String filename){
        String ret = "";
        try {
            Scanner scanner = new Scanner(new File(filename));
            while(scanner.hasNext()){
                ret += scanner.nextLine() + "\n";
            }
            scanner.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return ret;
    }

}
