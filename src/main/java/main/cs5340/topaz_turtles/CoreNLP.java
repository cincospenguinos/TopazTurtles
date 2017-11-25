package main.cs5340.topaz_turtles;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to help manage use of the Stanford CoreNLP tools.
 */
public class CoreNLP {
    private static StanfordCoreNLP pipeline;

    public static StanfordCoreNLP getPipeline() {
        if (pipeline == null) {
            init();
        }

        return pipeline;
    }

    private static void init() {
//        InputStream input = CoreNLP.class.getClass().getResourceAsStream("/nlp.properties");
        Properties props = new Properties();
        props.setProperty("annotaters", "ner");

        System.out.println("Instantiate pipeline...");
        pipeline = new StanfordCoreNLP(props);
        System.out.println("Done!");
    }
}
