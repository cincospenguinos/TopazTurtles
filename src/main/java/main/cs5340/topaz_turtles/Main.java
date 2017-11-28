package main.cs5340.topaz_turtles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

<<<<<<< HEAD
=======
import com.google.gson.JsonArray;
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import javax.json.JsonObject;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The main class of the application.
 * <p>
 * TODO: Extract all locations and keep them in a TreeSet
 * TODO: Figure out named entity recognition with the Stanford library and extract those and include them as a feature
 */
public class Main {

    public static final String LOCAL_DATA_FILEPATH = ".topaz_turtles_data/";
    public static final String RELATED_WORDS_FILEPATH = LOCAL_DATA_FILEPATH + "related_words.json";
    public static final String DATASET_FILEPATH = "dataset/";
    public static final String TEXT_FILEPATH = DATASET_FILEPATH + "texts/";

    private static TreeMap<IncidentType, DataMuseWord[]> relatedWordsToEachIncident;
    private static TreeSet<String> weapon_words;
    private static TreeSet<String> locations;
    private static TreeSet<String> individuals;
    private static ArrayList<CaseFrame> caseFrames;

    public static void main(String[] args) {
        if (args.length == 0 || args.length > 1) {
            System.out.println("Usage: infoextract <textfile>");
            System.exit(0);
        }
<<<<<<< HEAD
        caseFrames = new ArrayList<CaseFrame>();

//        try{
//            Tagger.tag("The bomb exploded and killed senators");
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
=======
//        CoreNLP.getPipeline();
        try {
            Tagger.tag();
        }
        catch(Exception e){
            e.printStackTrace();
        }
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5

        if (args[0].equalsIgnoreCase("TRAIN")) {
            weapon_words = new TreeSet<String>();
            setup(true);
            System.out.println("Classifiers trained.");
            System.exit(1);
        } else
            setup(false);

<<<<<<< HEAD
        parseCaseFramesFile("caseFrames.txt");
        parseCaseFramesFile("other.txt");
        ArrayList<Document> documents = parseFile(args[0]);
        for (Document d : documents) {
            System.out.println(d.getId());
//            d.setSlot(Slot.WEAPON, d.extractWeapon(caseFrames));
            String weap = d.lookForWeapon(d.getFullText(), caseFrames);  // This may be the better option here...
            String w = d.extractWeapon(caseFrames);
            d.setSlot(Slot.WEAPON, weap);
            System.out.println(d.getTaggedText());
            System.out.println(d.getFullText());
        }

=======
        ArrayList<Document> documents = parseFile(args[0]);
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5
//        for(Document d : documents){
//            getWeaponWords(d.getFullText());
//            fillSlots(d);
//            System.out.println(d);
//        }
<<<<<<< HEAD
        System.out.println("done.");
=======

//        for(Document d : documents){
//            getWeaponWords(d.getFullText());
//            fillSlots(d);
//            System.out.println(d);
//        }
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5
    }

    /**
     * This method parses a file, and is probably only going to be used for testing.
     */
    public static ArrayList<Document> parseFile(String filename) {
        ArrayList<Document> all_docs = new ArrayList<Document>();
        String file_contents = "";
        String dev = "DEV-MUC3-[\\d]*";
        String tst1 = "TST1-MUC3-[\\d]*";
        String tst2 = "TST2-MUC3-[\\d]*";
        Pattern dev_pattern = Pattern.compile(dev);
        Pattern tst1_pattern = Pattern.compile(tst1);
        Pattern tst2_pattern = Pattern.compile(tst2);
        Document to_add = new Document("", "");
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNext()) {
                String[] line = scanner.nextLine().split("\\s+");
                if (line[0].equals("") && line.length == 1) {
                } else {
                    to_add.addTaggedText(Document.tag(Arrays.toString(line)));
                }
                for (String s : line) {
                    Matcher dev_matcher = dev_pattern.matcher(s);
                    Matcher tst1_matcher = tst1_pattern.matcher(s);
                    Matcher tst2_matcher = tst2_pattern.matcher(s);
                    if (dev_matcher.matches()) {
                        to_add.setFullText(file_contents);
                        if (file_contents != null) {
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s, file_contents);
                        file_contents = "";
                        break;
                    } else if (tst1_matcher.matches()) {
                        to_add.setFullText(file_contents);
                        if (!file_contents.equals("")) {
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s, file_contents);
                        file_contents = "";
                        break;
                    } else if (tst2_matcher.matches()) {
                        to_add.setFullText(file_contents);
                        if (file_contents != null) {
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s, file_contents);
                        file_contents = "";
                        break;
                    } else {
                        file_contents += s + " ";
                    }
                }
            }
            to_add.setFullText(file_contents);
            all_docs.add(to_add);
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return all_docs;
    }

    /**
     * Fills the various slots on the document.
     *
     * @param d - Document to make guesses on
     */
    private static void fillSlots(Document d) {
        for (Slot slot : Slot.values()) {
            switch (slot) {
                case INCIDENT:
                    ArrayList<Document> aSingleDoc = new ArrayList<Document>();
                    aSingleDoc.add(d);

                    String vectorFileName = LOCAL_DATA_FILEPATH + d.getId() + ".vector";
                    String predictionFileName = LOCAL_DATA_FILEPATH + d.getId() + ".prediction";
                    String modelFileName = LOCAL_DATA_FILEPATH + "DEV-" + slot.toString().replace(" ", "_") + ".models";

                    generateVectorFile(aSingleDoc, vectorFileName, slot);
                    try {
                        String exec = "./predict " + vectorFileName + " " + modelFileName + " " + predictionFileName;
                        Process p = Runtime.getRuntime().exec(exec); //TODO: it breaks here for i dont know why...
                        int exitCode = p.waitFor();

                        if (exitCode != 0) {
                            System.err.println("exit code for " + d.getId() + " was " + exitCode);
                            return;
                        }

                        Scanner s = new Scanner(new File(predictionFileName));
                        int incidentTypeOrdinal = Integer.parseInt(s.next());
                        s.close();

                        d.setSlot(Slot.INCIDENT, IncidentType.fromOrdinal(incidentTypeOrdinal));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;

                case PERP_INDIV:
                    break;
                case PERP_ORG:
                    break;
                    // These are where Quinn will do his work
                case TARGET:
                    break;
                case VICTIM:
                    break;
                case WEAPON:
//                    d.setSlot(Slot.WEAPON, d.extractWeapon(caseFrames));
                    d.setSlot(Slot.WEAPON, d.lookForWeapon(d.getFullText(), caseFrames));
                    break;
            }
        }
    }

    /**
     * Generates a single vector file using all of the docs provided, with the given filename,
     * for the slot provided.
     *
     * @param docs     - Documents to generate vector file from
     * @param filename - name of vector file
     * @param slot     - What slot to fill
     */
    private static void generateVectorFile(ArrayList<Document> docs, String filename, Slot slot) {
        StringBuilder vectorFileBuilder = new StringBuilder();
        LibLinearFeatureManager manager = LibLinearFeatureManager.getInstance();

        // For each document...
        for (Document d : docs) {
            TreeMap<Integer, Boolean> libLinearFeatureVector = new TreeMap<Integer, Boolean>(); // Keep a map of key-bool pairs

            // Grab the proper IDs and their values for each LibLinearFeature
            int id = -1;
            for (LibLinearFeature libLinearFeature : LibLinearFeature.values()) {
                switch (libLinearFeature) {
                    case CONTAINS_WORD:
                        for (DataMuseWord[] array : relatedWordsToEachIncident.values()) {
                            for (DataMuseWord w : array) {
                                boolean isTrue = d.containsWordInText(w.word);
                                id = manager.addFeature(libLinearFeature, w.word);
                                libLinearFeatureVector.put(id, isTrue);
                            }
                        }
                        break;
                    case FROM_LOCATION:
                        break;
                    case YEAR:
                        if (d.getYearPublished() != -1) {
                            id = manager.addFeature(libLinearFeature, d.getYearPublished());
                            libLinearFeatureVector.put(id, true);
                        }
                        break;
                    case MONTH:
                        if (d.getMonthPublished() != -1) {
                            id = manager.addFeature(libLinearFeature, d.getYearPublished());
                            libLinearFeatureVector.put(id, true);
                        }
                        break;
                    case DAY_OF_YEAR:
                        if (d.getDayOfYearPublished() != -1) {
                            id = manager.addFeature(libLinearFeature, d.getYearPublished());
                            libLinearFeatureVector.put(id, true);
                        }
                        break;
                }
            }

            // Now append the label we're trying to discover to the string we're building
            switch (slot) {
                case INCIDENT:
                    IncidentType incident = IncidentType.fromString(d.getGoldStandardValue(Slot.INCIDENT));

                    int incidentLabelId;

                    if (incident == null)
                        incidentLabelId = -1;
                    else
                        incidentLabelId = incident.ordinal();

                    vectorFileBuilder.append(incidentLabelId);
                    vectorFileBuilder.append(" ");

                    for (Map.Entry<Integer, Boolean> e : libLinearFeatureVector.entrySet()) {
                        if (e.getValue()) {
                            vectorFileBuilder.append(e.getKey());
                            vectorFileBuilder.append(":1 ");
                        }
                    }

                    vectorFileBuilder.append("\n");
                    break;
                case PERP_INDIV:
                    // TODO: This
                    break;
                case PERP_ORG:
                    // TODO: This
                    break;
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new File(filename));
            writer.print(vectorFileBuilder.toString().trim());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void setGoldStandards(ArrayList<Document> docs) {
        // TODO: Fix this. Dear lord satan please fix this
        File[] answerFiles = new File(DATASET_FILEPATH + "answers/").listFiles();

        for (int i = 0; i < answerFiles.length; i++) {
            File f = answerFiles[i];

            for (Document d : docs) {
                if (f.getName().contains(d.getId())) {
                    d.setGoldStandard(f.getPath());
                }
            }
        }
    }

    private static ArrayList<Document> getAllDocsStartsWith(String str) {
        ArrayList<Document> docs = new ArrayList<Document>();

        for (File f : new File(TEXT_FILEPATH).listFiles()) {
            if (f.getName().startsWith(str)) {
                docs.add(getDocument(f.getPath()));
            }
        }

        return docs;
    }

    /**
     * Gets the document found at the file path provided.
     *
     * @param filePath
     * @return
     */
    private static Document getDocument(String filePath) {
        File f = new File(filePath);

        if (!f.exists()) {
            return null;
        } else {
            return new Document(filePath);
        }
    }

    /**
     * Function that runs first thing. Use this to generate any files you will need, grab any data
     * you may need to have put together, etc.
     */
    private static void setup(boolean createClassifier) {
        // Setup local data directory
        File path = new File(LOCAL_DATA_FILEPATH);

        if (!path.exists()) {
            System.out.println("Creating local data directory...");
            boolean flag = false;
            try {
                flag = path.mkdir();
            } catch (SecurityException e) {
                e.printStackTrace();
                System.exit(1);
            }

            if (!flag) {
                System.err.println("Could not create data directory!");
                System.exit(0);
            }
        }

        // Ensure that the related words file path exists
        File relatedWordsFile = new File(RELATED_WORDS_FILEPATH);
        if (!relatedWordsFile.exists()) {
            relatedWordsToEachIncident = new TreeMap<IncidentType, DataMuseWord[]>();

            for (IncidentType t : IncidentType.values()) {
                DataMuseWord[] words = DataMuse.getWordsRelatedTo(t.toString().toLowerCase());
                relatedWordsToEachIncident.put(t, words);
            }

            Gson gson = new Gson();
            String relatedWordsJson = gson.toJson(relatedWordsToEachIncident);

            try {
                System.out.println("Creating related words file...");
                PrintWriter writer = new PrintWriter(RELATED_WORDS_FILEPATH, "UTF-8");
                writer.print(relatedWordsJson);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            Gson gson = new Gson();

            try {
                StringBuilder builder = new StringBuilder();
                Scanner s = new Scanner(relatedWordsFile);
                while (s.hasNextLine())
                    builder.append(s.nextLine());
                s.close();

                Type treeType = new TypeToken<TreeMap<IncidentType, DataMuseWord[]>>() {
                }.getType();
                relatedWordsToEachIncident = gson.fromJson(builder.toString().trim(), treeType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        if (createClassifier) {
            // Grab all the docs and generate vector files from them
            ArrayList<Document> devDocs = getAllDocsStartsWith("DEV");
            ArrayList<Document> testDocs = getAllDocsStartsWith("TST");

            setGoldStandards(devDocs);
            setGoldStandards(testDocs);

            for (Slot s : Slot.machineLearningSlots()) {
                generateVectorFile(devDocs, LOCAL_DATA_FILEPATH + "DEV-" + s.toString().replace(" ", "_") + ".vector", s);
                generateVectorFile(testDocs, LOCAL_DATA_FILEPATH + "TEST-" + s.toString().replace(" ", "_") + ".vector", s);
            }

            // Generate model files from each of the dev vector files
            for (File f : new File(LOCAL_DATA_FILEPATH).listFiles()) {
                if (f.getName().contains(".vector") && f.getName().startsWith("DEV")) {
                    try {
                        Runtime.getRuntime().exec("./train " + f.getCanonicalPath() + " "
                                + LOCAL_DATA_FILEPATH + f.getName().replace(".vector", ".models"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static TreeMap<IncidentType, DataMuseWord[]> getRelatedWordsToEachIncident() {
        return relatedWordsToEachIncident;
    }

    /**
<<<<<<< HEAD
     * This method creates all the case frames we need from a file.
     * The case frames file should be in the local data filepath.
     * @param filename - the filename containing all of the case frames
     */
    public static void parseCaseFramesFile(String filename) {
        try {
            Scanner scanner = new Scanner(new File(LOCAL_DATA_FILEPATH + filename));
            while (scanner.hasNext()) {
                String[] line = scanner.nextLine().split("\\s+");
                CaseFrame frame = new CaseFrame(line.clone());
                caseFrames.add(frame);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

=======
     * This method takes in a document, gets all POS tags using Standford NLP,
     * and then adds weapon words to the weapons file.
     * @param document
     */
    public static void get_weapons(Document document){
        // TODO: make the POS tagger work.  create a file manually full of weapon words.
        // TODO: also make some case frames for the documents
        String[] weapons = {"rifle", "gun", "sidearm", "glock", "knife", "bomb", "molotov", "grenade", "knucks", "bludgeon",
                "car", "stab", "shoot"};

    }
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5
}
