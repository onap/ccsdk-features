package org.onap.ccsdk.features.lib.doorman.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUtil {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(DataUtil.class);

    @SuppressWarnings("unchecked")
    public static Object get(Object struct, String compositeKey) {
        if (struct == null || compositeKey == null) {
            return null;
        }

        List<Object> keys = splitCompositeKey(compositeKey);
        Object currentValue = struct;
        String currentKey = "";

        for (Object key : keys) {
            if (key instanceof Integer) {
                if (!(currentValue instanceof List)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '"
                            + currentKey + "', but '" + currentKey + "' is not a list");
                }

                Integer keyi = (Integer) key;
                List<Object> currentValueL = (List<Object>) currentValue;

                if (keyi >= currentValueL.size()) {
                    return null;
                }

                currentValue = currentValueL.get(keyi);
                if (currentValue == null) {
                    return null;
                }

                currentKey += "[" + key + "]";
            } else {
                if (!(currentValue instanceof Map)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '"
                            + currentKey + "', but '" + currentKey + "' is not a map");
                }

                currentValue = ((Map<String, Object>) currentValue).get(key);
                if (currentValue == null) {
                    return null;
                }

                currentKey += "." + key;
            }
        }
        return currentValue;
    }

    @SuppressWarnings("unchecked")
    public static void set(Object struct, String compositeKey, Object value) {
        if (struct == null) {
            throw new IllegalArgumentException("Null argument: struct");
        }

        if (compositeKey == null || compositeKey.length() == 0) {
            throw new IllegalArgumentException("Null or empty argument: compositeKey");
        }

        if (value == null) {
            return;
        }

        List<Object> keys = splitCompositeKey(compositeKey);
        Object currentValue = struct;
        String currentKey = "";

        for (int i = 0; i < keys.size() - 1; i++) {
            Object key = keys.get(i);

            if (key instanceof Integer) {
                if (!(currentValue instanceof List)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '"
                            + currentKey + "', but '" + currentKey + "' is not a list");
                }

                Integer keyi = (Integer) key;
                List<Object> currentValueL = (List<Object>) currentValue;
                int size = currentValueL.size();

                if (keyi >= size) {
                    for (int k = 0; k < keyi - size + 1; k++) {
                        currentValueL.add(null);
                    }
                }

                Object newValue = currentValueL.get(keyi);
                if (newValue == null) {
                    Object nextKey = keys.get(i + 1);
                    if (nextKey instanceof Integer) {
                        newValue = new ArrayList<>();
                    } else {
                        newValue = new HashMap<>();
                    }
                    currentValueL.set(keyi, newValue);
                }

                currentValue = newValue;
                currentKey += "[" + key + "]";

            } else {
                if (!(currentValue instanceof Map)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '"
                            + currentKey + "', but '" + currentKey + "' is not a map");
                }

                Object newValue = ((Map<String, Object>) currentValue).get(key);
                if (newValue == null) {
                    Object nextKey = keys.get(i + 1);
                    if (nextKey instanceof Integer) {
                        newValue = new ArrayList<>();
                    } else {
                        newValue = new HashMap<>();
                    }
                    ((Map<String, Object>) currentValue).put((String) key, newValue);
                }

                currentValue = newValue;
                currentKey += "." + key;
            }
        }

        Object key = keys.get(keys.size() - 1);
        if (key instanceof Integer) {
            if (!(currentValue instanceof List)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '"
                        + currentKey + "', but '" + currentKey + "' is not a list");
            }

            Integer keyi = (Integer) key;
            List<Object> currentValueL = (List<Object>) currentValue;
            int size = currentValueL.size();

            if (keyi >= size) {
                for (int k = 0; k < keyi - size + 1; k++) {
                    currentValueL.add(null);
                }
            }

            currentValueL.set(keyi, value);

        } else {
            if (!(currentValue instanceof Map)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '" + currentKey
                        + "', but '" + currentKey + "' is not a map");
            }

            ((Map<String, Object>) currentValue).put((String) key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static void delete(Object struct, String compositeKey) {
        if (struct == null) {
            throw new IllegalArgumentException("Null argument: struct");
        }

        if (compositeKey == null) {
            throw new IllegalArgumentException("Null argument: compositeKey");
        }

        List<Object> keys = splitCompositeKey(compositeKey);
        Object currentValue = struct;
        String currentKey = "";

        for (int i = 0; i < keys.size() - 1; i++) {
            Object key = keys.get(i);

            if (key instanceof Integer) {
                if (!(currentValue instanceof List)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '"
                            + currentKey + "', but '" + currentKey + "' is not a list");
                }

                Integer keyi = (Integer) key;
                List<Object> currentValueL = (List<Object>) currentValue;

                if (keyi >= currentValueL.size()) {
                    return;
                }

                currentValue = currentValueL.get(keyi);
                if (currentValue == null) {
                    return;
                }

                currentKey += "[" + key + "]";

            } else {
                if (!(currentValue instanceof Map)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '"
                            + currentKey + "', but '" + currentKey + "' is not a map");
                }

                currentValue = ((Map<String, Object>) currentValue).get(key);
                if (currentValue == null) {
                    return;
                }

                currentKey += "." + key;
            }
        }

        Object key = keys.get(keys.size() - 1);
        if (key instanceof Integer) {
            if (!(currentValue instanceof List)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '"
                        + currentKey + "', but '" + currentKey + "' is not a list");
            }

            Integer keyi = (Integer) key;
            List<Object> currentValueL = (List<Object>) currentValue;

            if (keyi < currentValueL.size()) {
                currentValueL.remove(keyi);
            }

        } else {
            if (!(currentValue instanceof Map)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '" + currentKey
                        + "', but '" + currentKey + "' is not a map");
            }

            ((Map<String, Object>) currentValue).remove(key);
        }
    }

    private static List<Object> splitCompositeKey(String compositeKey) {
        if (compositeKey == null) {
            return Collections.emptyList();
        }

        String[] ss = compositeKey.split("\\.");
        List<Object> ll = new ArrayList<>();
        for (String s : ss) {
            if (s.length() == 0) {
                continue;
            }

            int i1 = s.indexOf('[');
            if (i1 < 0) {
                ll.add(s);
            } else {
                if (!s.endsWith("]")) {
                    throw new IllegalArgumentException(
                            "Invalid composite key: " + compositeKey + ": No matching ] found");
                }

                String s1 = s.substring(0, i1);
                if (s1.length() > 0) {
                    ll.add(s1);
                }

                String s2 = s.substring(i1 + 1, s.length() - 1);
                try {
                    int n = Integer.parseInt(s2);
                    if (n < 0) {
                        throw new IllegalArgumentException(
                                "Invalid composite key: " + compositeKey + ": Index must be >= 0: " + n);
                    }

                    ll.add(n);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid composite key: " + compositeKey + ": Index not a number: " + s2);
                }
            }
        }

        return ll;
    }
}
