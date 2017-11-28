package main.cs5340.topaz_turtles;

import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import edu.stanford.nlp.simple.Sentence;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main class of the application.
 */
public class Main {

    private static final String LOCAL_DATA_FILEPATH = ".topaz_turtles_data/";
    private static final String RELATED_WORDS_FILEPATH = LOCAL_DATA_FILEPATH + "related_words.json";
    private static final String LOCATIONS_FILEPATH = LOCAL_DATA_FILEPATH + "locationsMasterList.json";
    private static final String DATASET_FILEPATH = "dataset/";
    private static final String TEXT_FILEPATH = DATASET_FILEPATH + "texts/";
    private static final String ORGANIZATIONS_FILEPATH = LOCAL_DATA_FILEPATH + "organizations.json";

    private static TreeMap<IncidentType, DataMuseWord[]> relatedWordsToEachIncident;
    private static TreeSet<String> locationsMasterList;
    private static TreeSet<String> organizationsMasterList;
    private static ArrayList<CaseFrame> caseFrames;


    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: infoextract <textfile>");
            System.exit(0);
        }

        setup();

        ArrayList<Document> documents = extractDocsFromFile(args[0]);
        for(Document d : documents) {
            fillSlots(d);
            System.out.println(d);
        }
    }

    /**
     * Extracts all documents from a document.
     *
     * @return all documents included in the file
     */
    private static ArrayList<Document> extractDocsFromFile (String filename) {
        ArrayList<Document> documents = new ArrayList<Document>();
        String docIdRegexString = "(DEV|TST[\\d]*)-MUC\\d+-[\\d]+";
        Pattern docIdPattern = Pattern.compile(docIdRegexString);
        StringBuilder builder = new StringBuilder();

        try {
            Scanner s = new Scanner(new File(filename));

            while(s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.trim().equals("")) {
                    builder.append(line);
                    builder.append("\n");
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String fullTextOfFile = builder.toString();

        String[] docs = fullTextOfFile.split(docIdRegexString);
        ArrayList<String> stringIds = new ArrayList<String>();

        Matcher idMatcher = docIdPattern.matcher(fullTextOfFile);
        while(idMatcher.find()) {
            stringIds.add(idMatcher.group());
        }

        for (int i = 0; i < stringIds.size(); i++) {
            Document d = new Document(stringIds.get(i), docs[i + 1]);
            documents.add(d);
        }

        return documents;
    }

    /**
     * Fills the various slots on the document.
     *
     * @param d - Document to make guesses on
     */
    private static void fillSlots(Document d) {
        for (Slot slot : Slot.values()) {
            ArrayList<Document> aSingleDoc;
            String vectorFileName = LOCAL_DATA_FILEPATH + d.getId() + ".vector";
            String predictionFileName = LOCAL_DATA_FILEPATH + d.getId() + ".prediction";
            String modelFileName = LOCAL_DATA_FILEPATH + "DEV-" + slot.toString().replace(" ", "_") + ".models";

            switch(slot) {
                case INCIDENT:
                    aSingleDoc = new ArrayList<Document>();
                    aSingleDoc.add(d);

                    generateVectorFile(aSingleDoc, vectorFileName, slot);
                    try {
                        String exec = "./predict " + vectorFileName + " " + modelFileName + " " + predictionFileName;
                        Process p = Runtime.getRuntime().exec(exec);
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
                    aSingleDoc = new ArrayList<Document>();
                    aSingleDoc.add(d);

                    generateVectorFile(aSingleDoc, vectorFileName, slot);
                    try {
                        String exec = "./predict " + vectorFileName + " " + modelFileName + " " + predictionFileName;
                        Process p = Runtime.getRuntime().exec(exec);
                        int exitCode = p.waitFor();

                        if (exitCode != 0) {
                            System.err.println("exit code for " + d.getId() + " was " + exitCode);
                            return;
                        }

                        Scanner s = new Scanner(new File(predictionFileName));
                        int perpOrgId = Integer.parseInt(s.next());
                        s.close();

                        String perpOrg = (String) PerpetratorLabelManager.getInstance().getLabelFromId(Slot.PERP_ORG, perpOrgId);

                        if (perpOrg != null)
                            d.setSlot(Slot.PERP_ORG, perpOrg);
                        else
                            d.setSlot(Slot.PERP_ORG, "");
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
                    String weap = d.lookForWeapon(d.getFullText(), caseFrames);
                    d.setSlot(Slot.WEAPON, weap);
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
        PerpetratorLabelManager perpetratorLabelManager = PerpetratorLabelManager.getInstance();

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
                    case CONTAINS_LOCATION:
                        Set<String> locs = new TreeSet<String>();
                        locs.addAll(getTagValues(d, "location"));

                        for (String l : locationsMasterList) {
                            boolean isTrue = locs.contains(l);
                            id = manager.addFeature(libLinearFeature, l);
                            libLinearFeatureVector.put(id, isTrue);
                        }
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
                    case CONTAINS_ORGANIZATION:
                        Set<String> orgs = new TreeSet<String>();
                        orgs.addAll(getTagValues(d, "organization"));

                        for (String l : organizationsMasterList) {
                            boolean isTrue = orgs.contains(l);
                            id = manager.addFeature(libLinearFeature, l);
                            libLinearFeatureVector.put(id, isTrue);
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

                    break;
                case PERP_INDIV:
                    // TODO: This
                    break;
                case PERP_ORG:
                    String perpetratorOrg = d.getGoldStandardValue(Slot.PERP_ORG);
                    int perpetratorOrgId = perpetratorLabelManager.addLabel(Slot.PERP_ORG, perpetratorOrg);
                    vectorFileBuilder.append(perpetratorOrgId);
                    break;
            }

            // Append the entire libLinearFeatureVector to the line and call it a day
            vectorFileBuilder.append(" ");

            for (Map.Entry<Integer, Boolean> e : libLinearFeatureVector.entrySet()) {
                if (e.getValue()) {
                    vectorFileBuilder.append(e.getKey());
                    vectorFileBuilder.append(":1 ");
                }
            }

            vectorFileBuilder.append("\n");
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
    private static void setup() {
        // Setup local data directory
        File path = new File(LOCAL_DATA_FILEPATH);

        if (!path.exists()) {
            System.err.println("Creating local data directory...");
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
                System.err.println("Creating related words file...");
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

        // Gather up all of the locations in the entire dataset
        File locationsFile = new File(LOCATIONS_FILEPATH);
        if (!locationsFile.exists()) {
            locationsMasterList = new TreeSet<String>();

            ArrayList<Document> allDocs = getAllDocsStartsWith("DEV");
            allDocs.addAll(getAllDocsStartsWith("TST"));

            System.err.println("Gathering all locations from all docs...");
            for (Document d : allDocs) {
                locationsMasterList.addAll(getTagValues(d, "location"));
            }

            Gson gson = new Gson();

            try {
                System.err.println("Creating locationsMasterList file...");
                PrintWriter writer = new PrintWriter(LOCATIONS_FILEPATH, "UTF-8");
                writer.print(gson.toJson(locationsMasterList));
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
                Scanner s = new Scanner(locationsFile);
                while (s.hasNextLine())
                    builder.append(s.nextLine());
                s.close();

                Type setType = new TypeToken<TreeSet<String>>(){}.getType();
                locationsMasterList = gson.fromJson(builder.toString().trim(), setType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // Gather up all of the potential organizations in the entire dataset
        File organizationsFile = new File(ORGANIZATIONS_FILEPATH);
        if (!organizationsFile.exists()) {
            organizationsMasterList = new TreeSet<String>();

            ArrayList<Document> allDocs = getAllDocsStartsWith("DEV");
            allDocs.addAll(getAllDocsStartsWith("TST"));

            System.err.println("Gathering all organizations from all docs...");
            for (Document d : allDocs)
                organizationsMasterList.addAll(getTagValues(d, "organization"));

            Gson gson = new Gson();

            try {
                System.err.println("Creating organizations file...");
                PrintWriter writer = new PrintWriter(ORGANIZATIONS_FILEPATH, "UTF-8");
                writer.print(gson.toJson(organizationsMasterList));
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
                Scanner s = new Scanner(organizationsFile);
                while (s.hasNextLine())
                    builder.append(s.nextLine());
                s.close();

                Type setType = new TypeToken<TreeSet<String>>(){}.getType();
                organizationsMasterList = gson.fromJson(builder.toString().trim(), setType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        File incidentModelsFile = new File(LOCAL_DATA_FILEPATH + "DEV-INCIDENT.models"); // If this file exists, we already have trained classifiers
        if (!incidentModelsFile.exists()) {
            System.err.println("Generating classifiers...");
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

        // Extract gold standards to use for PERP_ORG labels
        ArrayList<Document> allDocs = getAllDocsStartsWith("DEV");
        allDocs.addAll(getAllDocsStartsWith("TST"));
        setGoldStandards(allDocs);

        for (Document d : allDocs) {
            PerpetratorLabelManager.getInstance().addLabel(Slot.PERP_ORG, d.getGoldStandardValue(Slot.PERP_ORG));
        }

        // Grab the case frames
        if (caseFrames == null)
            caseFrames = new ArrayList<CaseFrame>();

        try {
            Scanner scanner = new Scanner(new File("caseFrames.txt"));
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

    /**
     * Uses NER to grab whatever type of tag desired and returns that collection
     *
     * @param document - The document to extract tags from
     * @param tagType - What tag to grab
     * @return List of String
     */
    private static List<String> getTagValues(Document document, String tagType) {
        List<String> locs = new LinkedList<String>();
        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(document.getFullText());

        for (Sentence s : doc.sentences()) {
            List<String> nerTags = s.nerTags();
            StringBuilder locationsBuilder = new StringBuilder();
            boolean foundLocation = false;

            for (int i = 0; i < nerTags.size(); i++) {
                String tag = nerTags.get(i);

                if (tag.equalsIgnoreCase(tagType)) {
                    foundLocation = true;
                    locationsBuilder.append(s.word(i));
                    locationsBuilder.append(" ");
                } else if (foundLocation){
                    locs.add(locationsBuilder.toString().trim());
                    locationsBuilder = new StringBuilder();
                    foundLocation = false;
                }
            }
        }

        return locs;
    }
}
