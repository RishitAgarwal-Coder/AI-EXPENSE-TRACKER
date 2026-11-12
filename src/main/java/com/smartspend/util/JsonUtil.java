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
     * Builds a multi-turn Gemini request: a system instruction plus the recent
     * conversation (role "user" / "model"), so follow-up questions keep context.
     *
     * @param systemInstruction instructions + the user's real financial snapshot
     * @param turns             alternating messages, each String[]{role, text}
     */
    public static String buildGeminiChatRequest(String systemInstruction, java.util.List<String[]> turns) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"systemInstruction\":{\"parts\":[{\"text\":\"").append(escape(systemInstruction)).append("\"}]},");
        sb.append("\"contents\":[");
        for (int i = 0; i < turns.size(); i++) {
            String[] t = turns.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"role\":\"").append(t[0]).append("\",\"parts\":[{\"text\":\"").append(escape(t[1])).append("\"}]}");
        }
        sb.append("],\"generationConfig\":{\"temperature\":0.6,\"maxOutputTokens\":1024}}");
        return sb.toString();
    }

    /**
     * Extracts the generated text from Gemini API response JSON.
     * Joins every text part of candidates[0].content.parts (skipping "thought" parts).
     * Falls back to the simple scanner below if the structured parse fails.
     */
    public static String extractGeminiText(String json) {
        if (json == null || json.isEmpty()) {
            return "No response received from AI service.";
        }

        try {
            Object root = parse(json);
            if (root instanceof java.util.Map<?, ?> map) {
                if (map.get("error") instanceof java.util.Map<?, ?> err && err.get("message") != null) {
                    return "AI Error: " + err.get("message");
                }
                if (map.get("candidates") instanceof java.util.List<?> cands && !cands.isEmpty()
                        && cands.get(0) instanceof java.util.Map<?, ?> cand) {
                    StringBuilder out = new StringBuilder();
                    if (cand.get("content") instanceof java.util.Map<?, ?> content
                            && content.get("parts") instanceof java.util.List<?> parts) {
                        for (Object p : parts) {
                            if (p instanceof java.util.Map<?, ?> part && part.get("text") != null
                                    && !Boolean.TRUE.equals(part.get("thought"))) {
                                out.append(part.get("text"));
                            }
                        }
                    }
                    if (out.length() > 0) return out.toString().trim();
                    Object reason = cand.get("finishReason");
                    if (reason != null) {
                        return "The AI did not return an answer (reason: " + reason + "). Please rephrase your question.";
                    }
                }
                if (map.get("promptFeedback") instanceof java.util.Map<?, ?> fb && fb.get("blockReason") != null) {
                    return "The AI declined this question (reason: " + fb.get("blockReason") + "). Please rephrase it.";
                }
            }
        } catch (RuntimeException ignored) {
            // Malformed JSON - fall through to the simple scanner below
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

    /**
     * Minimal recursive-descent JSON parser.
     * Returns Map (object), List (array), String, Double, Boolean or null.
     */
    public static Object parse(String json) {
        int[] pos = {0};
        Object value = parseValue(json, pos);
        skipWs(json, pos);
        return value;
    }

    private static Object parseValue(String s, int[] pos) {
        skipWs(s, pos);
        if (pos[0] >= s.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        char c = s.charAt(pos[0]);
        switch (c) {
            case '{': {
                java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                pos[0]++;
                skipWs(s, pos);
                if (s.charAt(pos[0]) == '}') { pos[0]++; return map; }
                while (true) {
                    skipWs(s, pos);
                    String key = parseString(s, pos);
                    skipWs(s, pos);
                    expect(s, pos, ':');
                    map.put(key, parseValue(s, pos));
                    skipWs(s, pos);
                    if (s.charAt(pos[0]) == ',') { pos[0]++; continue; }
                    expect(s, pos, '}');
                    return map;
                }
            }
            case '[': {
                java.util.List<Object> list = new java.util.ArrayList<>();
                pos[0]++;
                skipWs(s, pos);
                if (s.charAt(pos[0]) == ']') { pos[0]++; return list; }
                while (true) {
                    list.add(parseValue(s, pos));
                    skipWs(s, pos);
                    if (s.charAt(pos[0]) == ',') { pos[0]++; continue; }
                    expect(s, pos, ']');
                    return list;
                }
            }
            case '"':
                return parseString(s, pos);
            default:
                if (s.startsWith("true", pos[0])) { pos[0] += 4; return Boolean.TRUE; }
                if (s.startsWith("false", pos[0])) { pos[0] += 5; return Boolean.FALSE; }
                if (s.startsWith("null", pos[0])) { pos[0] += 4; return null; }
                int start = pos[0];
                while (pos[0] < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos[0])) >= 0) pos[0]++;
                if (start == pos[0]) throw new IllegalArgumentException("Unexpected character '" + c + "'");
                return Double.parseDouble(s.substring(start, pos[0]));
        }
    }

    private static String parseString(String s, int[] pos) {
        expect(s, pos, '"');
        int end = findClosingQuote(s, pos[0]);
        if (end == -1) throw new IllegalArgumentException("Unterminated string");
        String raw = s.substring(pos[0], end);
        pos[0] = end + 1;
        return unescape(raw);
    }

    private static void skipWs(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) pos[0]++;
    }

    private static void expect(String s, int[] pos, char c) {
        if (pos[0] >= s.length() || s.charAt(pos[0]) != c) {
            throw new IllegalArgumentException("Expected '" + c + "' at " + pos[0]);
        }
        pos[0]++;
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
