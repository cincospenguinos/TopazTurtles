package main.cs5340.topaz_turtles;

/**
 * A different slot that can exist inside of the template for each document.
 */
public enum Slot {
    ID, INCIDENT, WEAPON, PERP_INDIV, PERP_ORG, TARGET, VICTIM;

    public String toString() {
        switch(this) {
            case PERP_INDIV:
                return "PERP INDIV";
            case PERP_ORG:
                return "PERP ORG";
            default:
                return super.toString();
        }
    }
}
