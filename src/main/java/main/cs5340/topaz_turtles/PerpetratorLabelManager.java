package main.cs5340.topaz_turtles;

import java.util.TreeMap;

/**
 * Manages assigning a label for a given perpetrator organization or individual.
 */
public class PerpetratorLabelManager {

    private TreeMap<Slot, TreeMap<Object, Integer>> labelMaps;
    private TreeMap<Slot, TreeMap<Integer, Object>> idMaps;
    private int idCounter = 0;

    private static PerpetratorLabelManager instance;

    public static PerpetratorLabelManager getInstance() {
        if (instance == null)
            instance = new PerpetratorLabelManager();

        return instance;
    }

    private PerpetratorLabelManager() {
        labelMaps = new TreeMap<Slot, TreeMap<Object, Integer>>();
        idMaps = new TreeMap<Slot, TreeMap<Integer, Object>>();

        for (Slot s : Slot.values()) {
            labelMaps.put(s, new TreeMap<Object, Integer>());
            idMaps.put(s, new TreeMap<Integer, Object>());
        }
    }

    public int addLabel(Slot slot, Object label) {
        TreeMap<Object, Integer> labelMap = labelMaps.get(slot);
        TreeMap<Integer, Object> idMap = idMaps.get(slot);

        if (labelMap.get(label) == null) {
            int id = idCounter;
            labelMap.put(label, id);
            idMap.put(id, label);
            idCounter++;
        }

        return labelMap.get(label);
    }

    public Object getLabelFromId(Slot slot, int id) {
        TreeMap<Integer, Object> idMap = idMaps.get(slot);
        return idMap.get(id);
    }
}
