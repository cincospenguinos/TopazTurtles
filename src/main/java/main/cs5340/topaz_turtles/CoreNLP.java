package main.cs5340.topaz_turtles;

import edu.stanford.nlp.pipeline.*;
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
        Properties props = new Properties();
        props.setProperty("annotaters", "pos");
        pipeline = new StanfordCoreNLP(props);
    }
}
