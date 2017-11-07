package main.cs5340.topaz_turtles;

import java.util.Map;
import java.util.TreeMap;

/**
 * Feature manager for LibLinear machine learning library.
 */
public class LibLinearFeatureManager {

    private static LibLinearFeatureManager instance;

    public static LibLinearFeatureManager getInstance() {
        if (instance == null)
            instance = new LibLinearFeatureManager();

        return instance;
    }

    private int nextId = 1;
    private TreeMap<LibLinearFeature, TreeMap<Object, Integer>> features;

    private LibLinearFeatureManager() {
        features = new TreeMap<LibLinearFeature, TreeMap<Object, Integer>>();

        for (LibLinearFeature l : LibLinearFeature.values())
            features.put(l, new TreeMap<Object, Integer>());
    }

    /**
     * Adds the feature provided matching the feature type. Returns the ID of that feature.
     *
     * @param featureType - Type of feature
     * @param featureValue - The value of the feature
     * @return The ID, an int
     */
    public int addFeature(LibLinearFeature featureType, Object featureValue) {
        if (!features.get(featureType).containsKey(featureValue)) {
            features.get(featureType).put(featureValue, nextId++);
            return nextId - 1;
        } else
            return features.get(featureType).get(featureValue);
    }
}
