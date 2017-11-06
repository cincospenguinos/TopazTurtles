package main.cs5340.topaz_turtles;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single document from our dataset.
 */
public class Document {

    private String filename; // Name of this document
    private String completeText; // The complete text of the document

    private int yearPublished;
    private int monthPublished;
    private int dayOfYearPublished;
    private ArrayList<String> potentialLocations;

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

        potentialLocations = new ArrayList<String>();

        extractDateInformation();
        extractLocations();
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
            case NUM_YEAR:
                // TODO: This
            case NUM_DAY_OF_YEAR:
                // TODO: This
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

    /**
     * Helper method. Extracts the date information for the document.
     */
    private void extractDateInformation() {
        Matcher dateMatcher = Pattern.compile("\\d{1,2} [A-Z]{3} \\d*").matcher(completeText);
        if (dateMatcher.find()) {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yy");

            try {
                Calendar cal = new GregorianCalendar();
                cal.setTime(format.parse(dateMatcher.group()));
                yearPublished = cal.get(Calendar.YEAR);
                monthPublished = cal.get(Calendar.MONTH);
                dayOfYearPublished = cal.get(Calendar.DAY_OF_YEAR);

                return;
            } catch (ParseException e) {}
        }

        yearPublished = -1;
        monthPublished = -1;
        dayOfYearPublished = -1;
    }

    private void extractLocations() {
        Annotation annotation = new Annotation(completeText);
        CoreNLP.getPipeline().annotate(annotation);
        System.out.println(annotation);
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

    public int getYearPublished() {
        return yearPublished;
    }

    public int getMonthPublished() {
        return monthPublished;
    }

    public int getDayOfYearPublished() {
        return dayOfYearPublished;
    }
}
