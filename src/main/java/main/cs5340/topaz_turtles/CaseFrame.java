package main.cs5340.topaz_turtles;

import java.util.ArrayList;

public class CaseFrame {

    private String frame_slot;
    private ArrayList<String> frame_contents;
    private String extraction;

    public CaseFrame(String[] single_frame_contents) {
        frame_contents = new ArrayList<String>();
        frame_slot = "";
        extraction = "";
        for(String s : single_frame_contents){
            frame_contents.add(s);
        }

        StringBuilder builder = new StringBuilder();
        for(String s : single_frame_contents){
            builder.append(s + " ");
        }
        frame_slot = builder.toString().trim();
    }

    /**
     * This method gets the word you're using in the case frame to look with
     * EX: this method would return "infected" in the case frame [ VP, infected ]
     * @return
     */
    public String getEntity(){
        StringBuilder builder = new StringBuilder();
        builder.append(" ");
        for(String s : frame_contents){
            if(!Phrase.equals(s)){
                builder.append(s + " ");
            }
        }
        return builder.toString().trim();
    }

    public String[] getframe_contents(){ return frame_contents.toArray(new String[0]); }

    public String getExtranction(){ return this.extraction; }
    public void setExtraction(String s) { extraction = s; }

    public String getFrame_slot(){ return this.frame_slot; }

}
