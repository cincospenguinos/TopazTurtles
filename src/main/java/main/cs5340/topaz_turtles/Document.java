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

    /**
     * All of the information that this document will hold/update as needed.
     */
    private int totalWords;
    private double probRelArson, probRelAttack, probRelBombing, probRelKidnapping, probRelRobbery;

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

        totalWords = completeText.split("\\s+").length;
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
                double probRel = getProbRel(feature);

                if (probRel > 0.0)
                    return probRel;
                else {
                    probRelArson = discoverProbRel(DocumentFeature.PROB_REL_ARSON);
                    probRelAttack = discoverProbRel(DocumentFeature.PROB_REL_ATTACK);
                    probRelBombing = discoverProbRel(DocumentFeature.PROB_REL_BOMBING);
                    probRelKidnapping = discoverProbRel(DocumentFeature.PROB_REL_KIDNAPPING);
                    probRelRobbery = discoverProbRel(DocumentFeature.PROB_REL_ROBBERY);

                    return getProbRel(feature);
                }
            default:
                return null;
        }
    }

    /**
     * Returns string version of this document. This method uses the guesses to generate the string.
     *
     * @return String
     */
    public String toString() {
        String res = "";
        for (Map.Entry e : guesses.entrySet())
            res += e.getKey() + ": " + e.getValue() + "\n";

        return res;
    }

    /**
     * Generates and returns an ARFF formatted String to be put into an ARFF file.
     *
     * @return String ARFF string
     */
    public String getArrfLine() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < DocumentFeature.values().length - 1; i++) {
            builder.append(getFeatureValue(DocumentFeature.values()[i]));
            builder.append(", ");
        }

        builder.append(getFeatureValue(DocumentFeature.values()[DocumentFeature.values().length - 1]));

        return builder.toString();
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

    private double getProbRel(DocumentFeature featureProbRel) {
        switch(featureProbRel) {
            case PROB_REL_ARSON:
                if (probRelArson <= 0.0);
                    discoverProbRel(featureProbRel);

                return probRelArson;
            case PROB_REL_ATTACK:
                if (probRelAttack <= 0.0);
                    discoverProbRel(featureProbRel);

                return probRelAttack;
            case PROB_REL_BOMBING:
                if (probRelBombing <= 0.0);
                    discoverProbRel(featureProbRel);

                return probRelBombing;
            case PROB_REL_KIDNAPPING:
                if (probRelKidnapping <= 0.0);
                    discoverProbRel(featureProbRel);

                return probRelKidnapping;
            case PROB_REL_ROBBERY:
                if (probRelRobbery <= 0.0);
                    discoverProbRel(featureProbRel);

                return probRelRobbery;
            default:
                return -1.0;
        }
    }

    /**
     * Returns the probRel using the document feature provided.
     *
     * @param featureProbRel - ProbRel document feature.
     * @return double probability
     */
    private double discoverProbRel(DocumentFeature featureProbRel) {
        DataMuseWord[] words = Main.getRelatedWordsToEachIncident().get(featureProbRel.getIncidentType());
        int relatedWords = 0;

        for (DataMuseWord w : words) {
            Scanner s = new Scanner(completeText);

            while(s.hasNext()) {
                if (s.next().equalsIgnoreCase(w.word))
                    relatedWords++;
            }

            s.close();
        }

        return ((double) relatedWords) / ((double) totalWords);
    }
}
