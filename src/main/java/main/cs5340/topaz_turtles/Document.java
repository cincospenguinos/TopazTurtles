package main.cs5340.topaz_turtles;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Represents a single document from our dataset.
 */
public class Document {

    private String filename; // Name of this document
    private String completeText; // The complete text of the document

    private TreeMap<Slot, String> guesses; // The guesses we are putting together
    private TreeMap<Slot, String> goldStandard; // The actual answers for the document

    public Document(String _filename, String filepath) {
        filename = _filename;
        getFullText(filepath);

        guesses = new TreeMap<Slot, String>();
        goldStandard = new TreeMap<Slot, String>();

        for (Slot s : Slot.values()) {
            guesses.put(s, "");
            goldStandard.put(s, "");
        }

        guesses.put(Slot.ID, filename);
    }

    /**
     * Returns value of the feature requested. It is expected that the user of this method
     * knows what the return value is supposed to be for that specific feature, and casts it
     * accordingly.
     *
     * @param feature - Feature to look at
     * @return Object of some sort, or null if none is found.
     */
    public Object getFeatureValue(DocumentFeature feature) {
        switch (feature) {
            case PROB_REL_ARSON:
            case PROB_REL_ATTACK:
            case PROB_REL_BOMBING:
            case PROB_REL_KIDNAPPING:
            case PROB_REL_ROBBERY:
                break;
        }
        return null; // TODO: This
    }

    /**
     * Returns string version of this document. This method uses the guesses to generate the string.
     *
     * @return String
     */
    public String toString() {
        String res = "";
        for (Map.Entry e : guesses.entrySet())
            res += e.getKey() + " " + e.getValue();

        return res;
    }

    /**
     * Helper method. Grabs the full text from the file found in the path provided. Terminates
     * if no such file exists.
     *
     * @param filepath - path of the file
     */
    private void getFullText(String filepath) {
        try {
            Scanner scanner = new Scanner(new File(filepath));

            while(scanner.hasNextLine()) {
                completeText += scanner.nextLine() + "\n";
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file \"" + filepath + "\"");
            System.exit(1);
        }
    }
}
