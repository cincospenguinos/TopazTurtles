package main.cs5340.topaz_turtles;

/**
 * What type of incident a given file outlines.
 */
public enum IncidentType {
    ARSON, ATTACK, BOMBING, KIDNAPPING, ROBBERY;

    public static IncidentType fromString(String s) {
        for (IncidentType t : values())
            if (t.toString().equalsIgnoreCase(s))
                return t;

        return null;
    }

    public static IncidentType fromOrdinal(int ordinal) {
        for (IncidentType t : values())
            if (t.ordinal() == ordinal)
                return t;

        return null;
    }
}
