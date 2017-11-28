package main.cs5340.topaz_turtles;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single document from our dataset.
 */
public class Document {

    private String fullText = ""; // The complete text of the document
    private ArrayList<Tree> taggedText;

    private int yearPublished;
    private int monthPublished;
    private int dayOfYearPublished;
    private ArrayList<String> potentialLocations;

    private TreeMap<Slot, String> guesses; // The guesses we are putting together
    private TreeMap<Slot, String> goldStandard; // The actual answers for the document

    public Document(String id, String _fullText) {
        fullText = _fullText;
        taggedText = new ArrayList<Tree>();

        guesses = new TreeMap<Slot, String>();
        goldStandard = new TreeMap<Slot, String>();


        for (Slot s : Slot.values()) {
            guesses.put(s, "");
            goldStandard.put(s, "");
        }

        guesses.put(Slot.ID, id);

        potentialLocations = new ArrayList<String>();

        extractDateInformation();
//        extractLocations();
    }

    public Document(String filepath) {
        try {
            Scanner scanner = new Scanner(new File(filepath));

            while (scanner.hasNextLine()) {
                fullText += scanner.nextLine() + "\n";
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file \"" + filepath + "\"");
            System.exit(1);
        }

        guesses = new TreeMap<Slot, String>();
        goldStandard = new TreeMap<Slot, String>();

        for (Slot s : Slot.values()) {
            guesses.put(s, "");
            goldStandard.put(s, "");
        }

        Matcher matcher = Pattern.compile("(DEV|TST[\\d]*)-MUC\\d+-[\\d]+").matcher(fullText);
        if (matcher.find())
            guesses.put(Slot.ID, matcher.group());

        potentialLocations = new ArrayList<String>();

        extractDateInformation();
//        extractLocations();
    }

    /**
     * What it says on the tin.
     *
     * @param word
     * @return
     */
    public boolean containsWordInText(String word) {
        Scanner s = new Scanner(fullText);

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
        for (Map.Entry e : guesses.entrySet()) {
            if (e.getValue().equals(""))
                res += e.getKey() + ": -" + "\n";
            else
                res += e.getKey() + ": " + e.getValue() + "\n";
        }

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
     * Helper method. Extracts the date information for the document.
     */
    private void extractDateInformation() {
        Matcher dateMatcher = Pattern.compile("\\d{1,2} [A-Z]{3} \\d*").matcher(fullText);
        if (dateMatcher.find()) {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yy");

            try {
                Calendar cal = new GregorianCalendar();
                cal.setTime(format.parse(dateMatcher.group()));
                yearPublished = cal.get(Calendar.YEAR);
                monthPublished = cal.get(Calendar.MONTH);
                dayOfYearPublished = cal.get(Calendar.DAY_OF_YEAR);

                return;
            } catch (ParseException e) {
            }
        }

        yearPublished = -1;
        monthPublished = -1;
        dayOfYearPublished = -1;
    }

    private void extractLocations() {
        Annotation annotation = new Annotation(fullText);
        CoreNLP.getPipeline().annotate(annotation);
        System.out.println(annotation);
    }

    public String extractWeapon(ArrayList<CaseFrame> caseFrames) {
        // use the caseFrames to find the weapon words in the document text.
        boolean weapon_set = false;
        String weapon = "";
        for (Tree tree : taggedText) {
            if(weapon_set){
                break;
            }
            Set<Tree> sub_trees = tree.subTrees();
            weapon_set = false;
            for (Tree t : sub_trees) {
                if(weapon_set){
                    break;
                }
                String[] arr = t.toString().split("\\s+");
                StringBuilder builder = new StringBuilder();
                for (CaseFrame frame : caseFrames) {
                    if (weapon_set) {
                        break;
                    }
                    String entity = frame.getEntity();
                    String[] frame_contents = frame.getframe_contents();
                    int entity_position = -1;
                    int k = 0;
                    if (frame_contents.length == 1) {
                        if(weapon_set){
                            break;
                        }
                        for(int i = 0; i < arr.length; i++) {
                            if (arr[i].toUpperCase().contains((entity.toUpperCase()))) {
                                weapon = arr[i];
                                weapon_set = true;
                            }
                        }
//                        weapon = lookForWeapon(Arrays.toString(arr), caseFrames);
//                        if (!weapon.equals("")){
//                            weapon_set = true;
//                        }
                    }
                    else {
                        if(weapon_set){
                            break;
                        }
                        for (k = 0; k < frame_contents.length; k++) {
                            if (frame_contents[k].equals(entity)) {
                                entity_position = k;
                            }
                        }

                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].toUpperCase().contains((entity.toUpperCase()))) {
                                if (entity_position > 0) {
                                    for (int j = i - 1; j >= 0; j--) {
                                        if (arr[j].contains((frame_contents[0]))) {
                                            break;
                                        }
                                        if (!Phrase.in(arr[j])) {
                                            if (arr[j].toUpperCase().contains("THE")) { continue; }
                                            if (arr[j].toUpperCase().contains("A")) { continue; }
                                            if (arr[j].toUpperCase().contains("IN")) { continue; }
                                            if (arr[j].toUpperCase().contains("AN")) { continue; }
                                            if (arr[j].toUpperCase().contains("OF")) { continue; }
                                            if(arr[j].toUpperCase().contains(",")) { continue; }
                                            if(arr[j].toUpperCase().contains(".")) { continue; }
                                            if(arr[j].toUpperCase().contains("-")) { continue; }
                                            else {
                                                builder.append(arr[j] + " ");
                                            }
                                        }
                                    }
                                    break;
                                }
                                // there is something about this that doesn't work, and will require
                                // more hours to debug
                                else {
                                    for (int j = arr.length-1; j > i+1; j--) {
                                        if (j < 0) {
                                            break;
                                        }
                                        if (arr[j].contains((frame_contents[1]))) {
                                            break;
                                        }
                                        if (!Phrase.in(arr[j])) {
                                            if (arr[j].toUpperCase().contains("THE")) { continue; }
                                            if (arr[j].toUpperCase().contains("A")) { continue; }
                                            if (arr[j].toUpperCase().contains("IN")) { continue; }
                                            if (arr[j].toUpperCase().contains("AN")) { continue; }
                                            if (arr[j].toUpperCase().contains("OF")) { continue; }
                                            if(arr[j].toUpperCase().contains(",")) { continue; }
                                            if(arr[j].toUpperCase().contains(".")) { continue; }
                                            if(arr[j].toUpperCase().contains("-")) { continue; }
                                            else {
                                                builder.append(arr[j] + " ");
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if (!builder.toString().toUpperCase().equals("")) {
                            weapon = builder.toString();
                            weapon_set = true;
                        }
                    }
                }
            }
        }

//        String weapon = "";
//        boolean weapon_set = false;
//        for (Tree tree : taggedText) {
//            Set<Tree> sub_trees = tree.subTrees();
//            for (Tree t : sub_trees) {
//                weapon_set = false;
//                for (CaseFrame frame : caseFrames) {
//                    if (weapon_set) {
//                        break;
//                    }
//                    String[] arr = frame.getframe_contents();
//                    String pattern = "\\b";
//
//                    //create the pattern from the frame.
//                    if (arr.length > 1) {
//                        for (int i = 0; i < arr.length - 2; i++) {
//                            if (Phrase.equals(arr[i])) {
//                                pattern += "\\)*(" + arr[i].toUpperCase() + ")+\\)* ";
//                            }
//                        }
//                        String entity = frame.getEntity();
//                        pattern += "(\\(*" + entity.toUpperCase() + "\\)*)";
//                    }
//                    // no else statement here because we're matching the entire string at this point.
//                    Pattern r = Pattern.compile(pattern);
//                    Matcher matcher = r.matcher(t.toString());
//                    if (arr.length == 1 && t.toString().equalsIgnoreCase(frame.getEntity())) {
//                        weapon = t.toString();
//                        weapon_set = true;
//                    } else if (arr.length > 1 && matcher.matches()) {
//                        //weapon = t.toString();
//                        weapon = findWeapon(t.toString(), frame.getframe_contents(), pattern);
//                        weapon_set = true;
//                    }
//                }
//            }
//        }
        return weapon;
    }

    private String findWeapon(String subTree, String[] frame_contents, String pattern) {
        String weapon = "";
        String[] arr = subTree.split("\\(+");
        Pattern r = Pattern.compile(pattern);
        for (int i = 0; i < arr.length; i++) {
            Matcher matcher = r.matcher(arr[i]);
            if (matcher.matches()) {
                // look left, look right and try to find a word in case frame.
                if (!(i - 2 < 0)) {
                    if (!Phrase.equals(arr[i - 1])) {

                    }
                }
                StringBuilder builder = new StringBuilder();
                builder.append(" ");
                for (String str : frame_contents) {
                    if (!Phrase.equals(str)) {
                        builder.append(str + " ");
                    }
                }
                weapon = builder.toString().trim();
            }
        }
        return weapon;
    }

    /**
     * This is the dumb way of finding a weapon:  Is it listed in the file?
     *
     * @param fullText
     * @param caseFrames
     * @return
     */
    public String lookForWeapon(String fullText, ArrayList<CaseFrame> caseFrames) {
        String weapon = "";
        String[] arr = fullText.split("\\s+");
        for (String s : arr) {
            for (CaseFrame frame : caseFrames) {
                String blah = frame.getEntity();
                if (s.equalsIgnoreCase(frame.getEntity())) {
                    weapon = s;
                }
            }
        }
        return weapon;
    }

    /**
     * This method tags a sentence using StanfordNLP parsing
     *
     * @param text
     * @return
     */
    public static Tree tag(String text) {
        Sentence sent = new Sentence(text);
        Tree tree = sent.parse();
        return tree;
    }

    public void pennPrintTree(Tree tree) {
        tree.pennPrint();
    }

    public String getGoldStandardValue(Slot slot) {
        return goldStandard.get(slot);
    }

    public void setSlot(Slot slot, Object slotValue) {
        switch (slot) {
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

    public CharSequence getId() {
        return guesses.get(Slot.ID);
    }

    public String getFullText() {
        return this.fullText;
    }

    public void setFullText(String _text) {
        this.fullText = _text;
    }

    public void addTaggedText(Tree tree) {
        this.taggedText.add(tree);
    }

    public ArrayList<Tree> getTaggedText() {
        return this.taggedText;
    }
}
