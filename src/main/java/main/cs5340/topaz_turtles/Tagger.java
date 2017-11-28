package main.cs5340.topaz_turtles;

<<<<<<< HEAD

import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.*;

import java.io.IOException;
import java.util.Set;

/**
 * This class isn't used because there is little functionality.  The functionality
 * was put into the Document class.
 */
public class Tagger {

    public static void tag(String input_sentence)throws IOException, ClassNotFoundException{

        Sentence sent = new Sentence(input_sentence);
        Tree t = sent.parse();
        Set<Tree> sub_trees = t.subTrees();
        String [] strings = {"NP", "exploded"};
        CaseFrame c = new CaseFrame(strings);
        String entity = c.getEntity();

        for(Tree tree : sub_trees){
            System.out.println(tree.toString());
            String[] arr = tree.toString().split("\\s+");
            System.out.println(arr.toString());
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < arr.length; i++){
                if(arr[i].toUpperCase().contains((entity.toUpperCase()))){
                    for(int j = i-1; j >= 0; j--){
                        if(arr[j].contains(("NP"))){
                            break;
                        }
                        if(!Phrase.in(arr[j])){
                            if(arr[j].toUpperCase().contains("THE")) { continue; }
                            if(arr[j].toUpperCase().contains("A")) { continue; }
                            if(arr[j].toUpperCase().contains("IN")) { continue; }
                            if(arr[j].toUpperCase().contains("AN")) { continue; }
                            if(arr[j].toUpperCase().contains("OF")) { continue; }
                            else {
                                builder.append(arr[j] + " ");
                            }
                        }
                    }
                    break;
                }
            }
            if(!builder.toString().toUpperCase().equals("")){
                System.out.println("got it yo");
            }
            else{
                System.out.println("something went wrong");
            }
        }


//        List<String> tags = sent.posTags();
//        for(String s : tags){
//            System.out.println(s);
//        }

//        Set<Tree> set = t.subTrees();
//        for(Tree s : set){
//            System.out.println(s.toString());
//        }
//        t.pennPrint();
    }

=======
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
>>>>>>> 181642c6eab74d0ff86abc6a97e2896f2144c4a5
}
