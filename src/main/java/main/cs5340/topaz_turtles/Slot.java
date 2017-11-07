package main.cs5340.topaz_turtles;

/**
 * A different slot that can exist inside of the template for each document.
 */
public enum Slot {
    ID, INCIDENT, WEAPON, PERP_INDIV, PERP_ORG, TARGET, VICTIM;

    /**
     * Returns a Slot if the string provided matches a slot.
     *
     * @param str - String to compare
     * @return Slot or null if none is found.
     */
    public static Slot fromString(String str) {
        for (Slot s : Slot.values())
            if (s.toString().equalsIgnoreCase(str))
                return s;

        return null;
    }

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

    /**
     * All the slots that we're using machine learning to solve.
     *
     * @return Slot[] of all slots that we're using machine learning to solve.
     */
    public static Slot[] machineLearningSlots() {
        Slot[] slots = new Slot[] { INCIDENT, PERP_INDIV, PERP_ORG };
        return slots;
    }
}
