package com.elibrary.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small, dependency-free JSON reader/writer.
 *
 * The project intentionally avoids pulling in a third-party JSON library
 * (e.g. Gson/Jackson) so the whole application can be compiled and run
 * with nothing but a standard JDK - no build tool or internet access
 * required by the evaluator.
 *
 * Supported value types when parsing: Map&lt;String,Object&gt; (JSON object),
 * List&lt;Object&gt; (JSON array), String, Double, Boolean, or null.
 */
public final class JsonUtil {

    private JsonUtil() {
        // utility class - no instances
    }

    // ---------------------------------------------------------------
    // Writing
    // ---------------------------------------------------------------

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder sb, int indent) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString((String) value, sb);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            writeObject((Map<String, Object>) value, sb, indent);
        } else if (value instanceof List) {
            writeArray((List<Object>) value, sb, indent);
        } else {
            // Fallback: treat as string
            writeString(value.toString(), sb);
        }
    }

    private static void writeObject(Map<String, Object> map, StringBuilder sb, int indent) {
        if (map.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{\n");
        int i = 0;
        int size = map.size();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            indent(sb, indent + 1);
            writeString(entry.getKey(), sb);
            sb.append(": ");
            writeValue(entry.getValue(), sb, indent + 1);
            if (++i < size) {
                sb.append(",");
            }
            sb.append("\n");
        }
        indent(sb, indent);
        sb.append("}");
    }

    private static void writeArray(List<Object> list, StringBuilder sb, int indent) {
        if (list.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            indent(sb, indent + 1);
            writeValue(list.get(i), sb, indent + 1);
            if (i < list.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        indent(sb, indent);
        sb.append("]");
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }

    // ---------------------------------------------------------------
    // Parsing
    // ---------------------------------------------------------------

    public static Object parse(String json) {
        Parser parser = new Parser(json);
        Object result = parser.parseValue();
        parser.skipWhitespace();
        return result;
    }

    private static class Parser {
        private final String text;
        private int pos;

        Parser(String text) {
            this.text = text;
            this.pos = 0;
        }

        void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        Object parseValue() {
            skipWhitespace();
            char c = text.charAt(pos);
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't':
                case 'f': return parseBoolean();
                case 'n': return parseNull();
                default: return parseNumber();
            }
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // consume '{'
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char next = text.charAt(pos);
                if (next == ',') {
                    pos++;
                } else if (next == '}') {
                    pos++;
                    break;
                }
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // consume '['
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                char next = text.charAt(pos);
                if (next == ',') {
                    pos++;
                } else if (next == ']') {
                    pos++;
                    break;
                }
            }
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = text.charAt(pos++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char esc = text.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            String hex = text.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Boolean parseBoolean() {
            if (text.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            } else if (text.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean at position " + pos);
        }

        Object parseNull() {
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at position " + pos);
        }

        Double parseNumber() {
            int start = pos;
            while (pos < text.length() && "-+.eE0123456789".indexOf(text.charAt(pos)) >= 0) {
                pos++;
            }
            return Double.parseDouble(text.substring(start, pos));
        }

        char peek() {
            return text.charAt(pos);
        }

        void expect(char c) {
            if (text.charAt(pos) != c) {
                throw new IllegalArgumentException(
                        "Expected '" + c + "' at position " + pos + " but found '" + text.charAt(pos) + "'");
            }
            pos++;
        }
    }
}
