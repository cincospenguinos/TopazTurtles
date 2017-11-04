package main.cs5340.topaz_turtles;

import sun.font.CoreMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a single document from our dataset.
 */
public class Document {

    private String filename; // Name of this document
    private String completeText; // The complete text of the document
    private Date datePublished; // The date this document was published

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

    public boolean containsWordInText(String word) {
        Scanner s = new Scanner(completeText);

        while (s.hasNextLine())
            if (s.nextLine().contains(word.toUpperCase()))
                return true;

            s.close();

        return false;
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
        Calendar cal;

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
            case NUM_YEAR:
                if (datePublished == null && !extractDateOccurred())
                    return null;

                cal = Calendar.getInstance();
                cal.setTime(datePublished);

                return cal.get(Calendar.MONTH);
            case NUM_DAY_OF_YEAR:
                if (datePublished == null && !extractDateOccurred())
                    return null;

                cal = Calendar.getInstance();
                cal.setTime(datePublished);

                return cal.get(Calendar.DAY_OF_YEAR);
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
     * Sets the gold standard for this document from the file path provided.
     *
     * @param answerFilePath - answer key file
     */
    public void setGoldStandard(String answerFilePath) {
        File f = new File(answerFilePath);

        if (f.exists()) {
            try {
                Scanner s = new Scanner(f);

                Slot slot = null;
                while (s.hasNextLine()) {
                    String line = s.nextLine();

                    if (line.trim().equals(" "))
                        continue;

                    String[] lineSplit = line.split(":");

                    if (lineSplit.length == 1) {
                        goldStandard.put(slot, goldStandard.get(slot) + lineSplit[0].trim());
                    } else {
                        slot = Slot.fromString(lineSplit[0]);
                        goldStandard.put(slot, goldStandard.get(slot) + lineSplit[1].trim());
                    }
                }

                s.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Could not find file " + answerFilePath + "!");
        }
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

    private boolean extractDateOccurred() {
        Scanner s = new Scanner(completeText);
        s.nextLine();
        s.nextLine();

        Scanner otherS = new Scanner(s.nextLine());
        String dateString = "";

        while (otherS.hasNext()) {
            String str = otherS.next();

            if (Character.isDigit(str.charAt(0))) {
                dateString += str + " ";
                dateString += otherS.next() + " ";
                try {
                    dateString += otherS.next();
                } catch (NoSuchElementException e) {
                    System.out.println("Herp");
                    e.printStackTrace();
                }
            }
        }

        otherS.close();
        s.close();

        try {
            datePublished = new SimpleDateFormat("dd MMM yy").parse(dateString);
            return true;
        } catch (ParseException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public String getFilename() {
        return filename;
    }

    public String getGoldStandardValue(Slot slot) {
        return goldStandard.get(slot);
    }

    public void setSlot(Slot slot, Object slotValue) {
        switch(slot) {
            case INCIDENT:
                if (!((slotValue instanceof IncidentType) || (slotValue instanceof String)))
                    throw new RuntimeException("INCIDENT slot requires an IncidentType or String!");

                guesses.put(slot, slotValue.toString());
        }
    }
}
