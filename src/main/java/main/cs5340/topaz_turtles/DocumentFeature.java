package main.cs5340.topaz_turtles;

/**
 * The set of features that a document may have.
 */
public enum DocumentFeature {
    PROB_REL_ARSON, PROB_REL_ATTACK, PROB_REL_BOMBING, PROB_REL_KIDNAPPING, PROB_REL_ROBBERY;

    public IncidentType getIncidentType() {
        switch(this) {
            case PROB_REL_ARSON:
                return IncidentType.ARSON;
            case PROB_REL_ATTACK:
                return IncidentType.ATTACK;
            case PROB_REL_BOMBING:
                return IncidentType.BOMBING;
            case PROB_REL_KIDNAPPING:
                return IncidentType.KIDNAPPING;
            case PROB_REL_ROBBERY:
                return IncidentType.ROBBERY;
            default:
                return null;
        }
    }
}
