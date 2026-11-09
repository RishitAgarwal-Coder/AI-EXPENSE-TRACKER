package com.smartspend.util;

/**
 * Lightweight JSON Utility class for formatting Gemini API requests
 * and parsing Gemini API JSON responses.
 * Demonstrates:
 * 1. String processing and JSON structure handling
 * 2. Robust escaping and unescaping
 */
public class JsonUtil {

    /**
     * Constructs a valid JSON payload for Google Gemini API generateContent endpoint.
     */
    public static String buildGeminiRequest(String userPrompt) {
        String escaped = escape(userPrompt);
        return "{\n" +
               "  \"contents\": [{\n" +
               "    \"parts\": [{\n" +
               "      \"text\": \"" + escaped + "\"\n" +
               "    }]\n" +
               "  }]\n" +
               "}";
    }

    /**
     * Extracts the generated text from Gemini API response JSON.
     * Looks for candidates[0].content.parts[0].text
     */
    public static String extractGeminiText(String json) {
        if (json == null || json.isEmpty()) {
            return "No response received from AI service.";
        }

        // Check for error payload first
        if (json.contains("\"error\"")) {
            int msgIdx = json.indexOf("\"message\":");
            if (msgIdx != -1) {
                int start = json.indexOf("\"", msgIdx + 10);
                int end = findClosingQuote(json, start + 1);
                if (start != -1 && end != -1) {
                    return "AI Error: " + unescape(json.substring(start + 1, end));
                }
            }
            return "Error from Gemini API: " + json;
        }

        // Search for "text": "..." inside candidates
        int textMarker = json.indexOf("\"text\":");
        if (textMarker != -1) {
            int startQuote = json.indexOf("\"", textMarker + 7);
            if (startQuote != -1) {
                int endQuote = findClosingQuote(json, startQuote + 1);
                if (endQuote != -1) {
                    String extracted = json.substring(startQuote + 1, endQuote);
                    return unescape(extracted);
                }
            }
        }

        return "Could not parse AI response. Raw output:\n" + json;
    }

    /**
     * Escapes special characters for JSON string formatting.
     */
    public static String escape(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20 || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Unescapes JSON escaped characters.
     */
    public static String unescape(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = text.charAt(i + 1);
                switch (next) {
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    case 'b' -> { sb.append('\b'); i++; }
                    case 'f' -> { sb.append('\f'); i++; }
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case 'u' -> {
                        if (i + 5 < len) {
                            String hex = text.substring(i + 2, i + 6);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 5;
                            } catch (NumberFormatException ignored) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int findClosingQuote(String str, int startIndex) {
        boolean escaped = false;
        for (int i = startIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                escaped = !escaped;
            } else if (c == '"' && !escaped) {
                return i;
            } else {
                escaped = false;
            }
        }
        return -1;
    }
}
