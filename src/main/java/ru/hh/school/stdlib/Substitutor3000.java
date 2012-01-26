package ru.hh.school.stdlib;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Substitutor3000 {
    private final static Pattern patternForBraces = Pattern.compile("\\$\\{[^}]*\\}");
    private final Map<String, String> internalMap = new HashMap<String, String>();
    private int sleepTime = 0;

    public synchronized int getSleepTime() {
        int sleepTime = this.sleepTime;
        this.sleepTime = 0;
        return sleepTime;
    }

    public synchronized void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    protected String transformValueString(final String valueString) {
        if (valueString == null) {
            return "";
        }
        
        int fromIndex = 0;
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = patternForBraces.matcher(valueString);
        while (matcher.find()) {
            result.append(valueString, fromIndex, matcher.start());
            fromIndex = matcher.end();
            final String argToReplace = matcher.group();
            String replacingValue = internalMap.get(argToReplace.substring(2, argToReplace.length() - 1));
            if (replacingValue != null) {
                result.append(replacingValue);
            }
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
