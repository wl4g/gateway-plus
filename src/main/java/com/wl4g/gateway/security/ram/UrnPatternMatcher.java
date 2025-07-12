package com.wl4g.gateway.security.ram;

import java.util.regex.Pattern;

// TODO: testing
public class UrnPatternMatcher {
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[*?+]");

    public static boolean matches(String pattern, String actualUrn) {
        if (pattern.equals(actualUrn)) {
            return true;
        }
        if (pattern.equals("*")) {
            return true;
        }

        String[] patternParts = pattern.split("[:/]");
        String[] actualParts = actualUrn.split("[:/]");

        if (patternParts.length != actualParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (!matchPart(patternParts[i], actualParts[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchPart(String pattern, String actual) {
        if (pattern.equals("*")) {
            return true;
        }
        if (!WILDCARD_PATTERN.matcher(pattern).find()) {
            return pattern.equals(actual);
        }

        int p = 0, a = 0;
        while (p < pattern.length() && a < actual.length()) {
            char pc = pattern.charAt(p);

            if (pc == '*') {
                if (p == pattern.length() - 1) {
                    return true;
                }
                while (a < actual.length()) {
                    if (matchPart(pattern.substring(p + 1), actual.substring(a))) {
                        return true;
                    }
                    a++;
                }
                return false;
            } else if (pc == '?' || pc == actual.charAt(a)) {
                p++;
                a++;
            } else {
                return false;
            }
        }
        return p == pattern.length() && a == actual.length();
    }
}
