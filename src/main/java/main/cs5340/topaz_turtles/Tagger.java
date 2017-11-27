package main.cs5340.topaz_turtles;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;

public class Tagger {

    public static void tag()throws IOException, ClassNotFoundException{
        MaxentTagger tagger = new MaxentTagger(
                "stanford-postagger-2017-06-09/models/english-bidirectional-distsim.tagger");

        String sample = "This is a sample text";

        String tagged = tagger.tagString(sample);

        System.out.println(tagged);
    }
}
