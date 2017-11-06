package main.cs5340.topaz_turtles;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static ArrayList<Document> all_docs = new ArrayList<Document>();

    /**
     * This method parses a file, and is probably only going to be used for testing.
     */
    public static void parseFile(String filename){
        String file_contents = "";
        String dev = "DEV-MUC3-[\\d]*";
        String tst1 = "TST1-MUC3-[\\d]*";
        String tst2 = "TST1-MUC3-[\\d]*";
        Pattern dev_pattern = Pattern.compile(dev);
        Pattern tst1_pattern = Pattern.compile(tst1);
        Pattern tst2_pattern = Pattern.compile(tst2);
        Document to_add = new Document("");
        try {
            Scanner scanner = new Scanner(new File(filename));
            while(scanner.hasNext()){
                String[] line = scanner.nextLine().split("\\s+");
                for (String s : line) {
                    Matcher dev_matcher = dev_pattern.matcher(s);
                    Matcher tst1_matcher = tst1_pattern.matcher(s);
                    Matcher tst2_matcher = tst2_pattern.matcher(s);
                    if(dev_matcher.matches()){
                        to_add.setCompleteText(file_contents);
                        if(file_contents != null){
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s);
                        file_contents = "";
                        break;
                    }
                    else if(tst1_matcher.matches()){
                        to_add.setCompleteText(file_contents);
                        if(!file_contents.equals("")){
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s);
                        file_contents = "";
                        break;
                    }
                    else if(tst2_matcher.matches()){
                        to_add.setCompleteText(file_contents);
                        if(file_contents != null){
                            all_docs.add(to_add);
                        }
                        to_add = new Document(s);
                        file_contents = "";
                        break;
                    }
                    else{
                        file_contents += s + " ";
                    }
                }
            }
            to_add.setCompleteText(file_contents);
            all_docs.add(to_add);
            scanner.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<Document> getAllDocs(){ return all_docs; }

}
