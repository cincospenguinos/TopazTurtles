package main.cs5340.topaz_turtles;

/**
 * What type of incident a given file outlines.
 */
public enum IncidentType {
    ARSON, ATTACK, BOMBING, KIDNAPPING, ROBBERY;

    public static IncidentType fromString(String s) {
        for (IncidentType t : IncidentType.values())
            if (t.toString().equalsIgnoreCase(s))
                return t;

        return null;
    }
}
