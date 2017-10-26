package main.cs5340.topaz_turtles;

/**
 * The main class of the application.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar [name] [file1] [file2] ...");
            System.exit(0);
        }

        System.out.println("It works!");
    }
}
