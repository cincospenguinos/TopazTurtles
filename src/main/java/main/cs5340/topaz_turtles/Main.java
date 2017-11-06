package main.cs5340.topaz_turtles;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * The main class of the application.
 *
 * TODO: Extract all locations and keep them in a TreeSet
 * TODO: Figure out named entity recognition with the Stanford library and extract those and include them as a feature
 */
public class Main {

    public static final String LOCAL_DATA_FILEPATH = ".topaz_turtles_data/";
    public static final String RELATED_WORDS_FILEPATH = LOCAL_DATA_FILEPATH + "related_words.json";
    public static final String DATASET_FILEPATH = "dataset/";
    public static final String TEXT_FILEPATH = DATASET_FILEPATH + "texts/";

    private static TreeMap<IncidentType, DataMuseWord[]> relatedWordsToEachIncident;
    private static TreeSet<String> locations;
    private static TreeSet<String> individuals;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar [name] [file1] [file2] ...");
            System.exit(0);
        }
        CoreNLP.getPipeline();

        setup();

        for (String file : args) {
            Document d = getDocument(file);

            if (d == null) {
                System.err.println(file + " does not exist!");
                continue;
            }

            // Make guesses for each of the different slots
            fillSlots(d);

            // Print out what they want us to
            System.out.println(d);
        }
    }

    /**
     * Fills the various slots on the document.
     *
     * @param d - Document to make guesses on
     */
    private static void fillSlots(Document d) {
        for (Slot slot : Slot.values()) {
            switch(slot) {
                case INCIDENT:
                case PERP_INDIV:
                case PERP_ORG:
                    ArrayList<Document> aSingleDoc = new ArrayList<Document>();
                    aSingleDoc.add(d);

                    String vectorFileName = LOCAL_DATA_FILEPATH + d.getFilename() + ".vector";
                    String predictionFileName = LOCAL_DATA_FILEPATH + d.getFilename() + ".prediction";
                    String modelFileName = LOCAL_DATA_FILEPATH + "DEV-" + slot.toString().replace(" ", "_") + ".models";

                    // TODO: Fix this so that slot is used instead of INCIDENT
                    generateVectorFile(aSingleDoc, vectorFileName, Slot.INCIDENT);
                    try {
                        String exec = "./predict " + vectorFileName + " " + modelFileName + " " + predictionFileName;
                        Process p = Runtime.getRuntime().exec(exec);
                        int exitCode = p.waitFor();

                        if (exitCode != 0) {
                            System.err.println("exit code for " + d.getFilename() + " was " + exitCode);
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

                    // These are where Quinn will do his work
                case TARGET:
                    break;
                case VICTIM:
                    break;
                case WEAPON:
                    break;
            }
        }
    }

    /**
     * Generates a single vector file using all of the docs provided, with the given filename,
     * for the slot provided.
     *
     * @param docs - Documents to generate vector file from
     * @param filename - name of vector file
     * @param slot - What slot to fill
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
                switch(libLinearFeature) {
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
            switch(slot) {
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
        File[] answerFiles = new File(DATASET_FILEPATH + "answers/").listFiles();

        for (int i = 0; i < answerFiles.length; i++) {
            File f = answerFiles[i];

            for (Document d : docs) {
                if (f.getName().contains(d.getFilename())) {
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
     * @param filePath
     * @return
     */
    private static Document getDocument(String filePath) {
        File f = new File(filePath);

        if (!f.exists()) {
            return null;
        } else {
            String[] split = filePath.split("\\/");
            String fileName = split[split.length - 1];
            return new Document(fileName, filePath);
        }
    }

    /**
     * Function that runs first thing. Use this to generate any files you will need, grab any data
     * you may need to have put together, etc.
     */
    private static void setup() {
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

                Type treeType = new TypeToken<TreeMap<IncidentType, DataMuseWord[]>>(){}.getType();
                relatedWordsToEachIncident = gson.fromJson(builder.toString().trim(), treeType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

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

    public static TreeMap<IncidentType, DataMuseWord[]> getRelatedWordsToEachIncident() {
        return relatedWordsToEachIncident;
    }
}
