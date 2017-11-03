package main.cs5340.topaz_turtles;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * The main class of the application.
 */
public class Main {

    public static final String LOCAL_DATA_FILEPATH = ".topaz_turtles_data/";
    public static final String RELATED_WORDS_FILEPATH = LOCAL_DATA_FILEPATH + "related_words.json";
    public static final String DATASET_FILEPATH = "dataset/";
    public static final String TEXT_FILEPATH = DATASET_FILEPATH + "texts/";

    private static TreeMap<IncidentType, DataMuseWord[]> relatedWordsToEachIncident;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar [name] [file1] [file2] ...");
            System.exit(0);
        }

        grabNecessaryData();

        ArrayList<Document> devDocs = getAllDocsStartsWith("DEV");
        ArrayList<Document> testDocs = getAllDocsStartsWith("TST");

        generateArffFile("dev", devDocs);
        generateArffFile("test", testDocs);

        ConverterUtils.DataSource source = null;
        try {
            ConverterUtils.DataSource trainDataSource = new ConverterUtils.DataSource(LOCAL_DATA_FILEPATH + "dev.arff");
            ConverterUtils.DataSource testDataSource = new ConverterUtils.DataSource(LOCAL_DATA_FILEPATH + "test.arff");

            Instances trainingData = trainDataSource.getDataSet();
//            trainingData.setClass(attr);
            trainingData.setClassIndex(trainingData.numAttributes() - 1);

            Instances testData = testDataSource.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);

            if (trainingData.classIndex() == -1)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

            Classifier cls = new LinearRegression();
            cls.buildClassifier(trainingData);

            Evaluation eval = new Evaluation(trainingData);
            eval.evaluateModel(cls, testData);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateArffFile(String name, ArrayList<Document> docs) {
        setGoldStandards(docs);

        try {
            PrintWriter writer = new PrintWriter(new File(LOCAL_DATA_FILEPATH + name + ".arff"));

            writer.println("% " + name + ".arff");
            writer.println("% Contains all the various feature vectors for each document.");
            writer.println("% Think of this as a collection of feature vectors.\n");
            writer.println("@RELATION DEV_DOCUMENT\n");

            for (DocumentFeature f : DocumentFeature.values())
                writer.println("@ATTRIBUTE " + f + " NUMERIC"); // TODO: this shouldn't be numeric forever

            writer.print("@ATTRIBUTE class NUMERIC");

            writer.println("\n@DATA");

            for (Document d : docs)
                writer.println(d.getArrfLine());

            writer.flush();
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
            System.err.println(filePath + " does not exist!");
            System.exit(1);
        } else {
            String[] split = filePath.split("\\/");
            String fileName = split[split.length - 1];
            return new Document(fileName, filePath);
        }

        return null;
    }

    private static void setupFilepath() {
        File path = new File(LOCAL_DATA_FILEPATH);

        // Ensure that the local data directory exists
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
    }

    private static void grabNecessaryData() {
        setupFilepath();

        // Ensure that the related words file path exists
        File relatedWordsFile = new File(RELATED_WORDS_FILEPATH);
        if (!relatedWordsFile.exists()) {
            relatedWordsToEachIncident = new TreeMap<IncidentType, DataMuseWord[]>();

            System.out.println("Grabbing related words for each IncidentType...");
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

            System.out.println("Grabbing related words...");
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
            }
        }
    }

    public static TreeMap<IncidentType, DataMuseWord[]> getRelatedWordsToEachIncident() {
        return relatedWordsToEachIncident;
    }
}
