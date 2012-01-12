package ru.hh.school.stdlib;

import java.util.HashMap;
import java.util.Map;

public class Substitutor3000 {
    private final Map<String, String> internalMap = new HashMap<String, String>();
    private int timeSleep = 0;

    protected String transformValueString(final String valueString) {
        if (valueString == null) {
            return "";
        }

        final StringBuilder result = new StringBuilder();

        int fromIndex = 0;

        int openingBraceIndex = valueString.indexOf("${", fromIndex);
        while (openingBraceIndex != -1) {
            result.append(valueString, fromIndex, openingBraceIndex);
            int closingBraceIndex = valueString.indexOf("}", fromIndex);
            if (closingBraceIndex != -1) {
                String nextPiece = internalMap.get(valueString.substring(openingBraceIndex + 2, closingBraceIndex));
                if (nextPiece != null) {
                    result.append(nextPiece);
                }
                fromIndex = closingBraceIndex + 1;
            } else {
                result.append(valueString.substring(openingBraceIndex));
                fromIndex = valueString.length();
            }
            openingBraceIndex = valueString.indexOf("${", fromIndex);
        }
        result.append(valueString, fromIndex, valueString.length());

        return result.toString();
    }
    
    public synchronized void put(String key, String value) {
        internalMap.put(key, value);
    }

    public synchronized String get(String key) {
        return transformValueString(internalMap.get(key));
    }
}
