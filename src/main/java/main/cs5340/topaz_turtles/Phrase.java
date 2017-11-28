package main.cs5340.topaz_turtles;

public enum Phrase {
    NP, VP, PP, S, CC, CD, DT, EX, FW, IN, JJ, JJR, JJS, LS, MD, NN, NNS, NPS, PDT, POS, PP$, RB, RBR, RBS, RP, SENT,
    SYM, TO, UH, VB, VBD, VBG, VBN, VBP, VBZ, VH, VHD, VHG, VHN, VHP, VHZ, VV, VVD, VVG, VVN, VVP, VVZ, WDT, WP, WP$,
    WRB;

    public static boolean equals(String s) {
        for (Phrase p : values())
            if (p.toString().equalsIgnoreCase(s))
                return true;
        return false;
    }

    public static boolean in(String s) {
        for(Phrase p : values()){
            if(s.contains(p.toString())){
                return true;
            }
        }
        return false;
    }

}
