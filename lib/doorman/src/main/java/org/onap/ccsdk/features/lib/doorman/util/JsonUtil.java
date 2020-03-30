package org.onap.ccsdk.features.lib.doorman.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

	public static Object jsonToData(String s) {
		if (s == null) {
			return null;
		}
		return jsonToData(new Current(s));
	}

	private static class Current {

		public String s;
		public int i;
		public int line, pos;

		public Current(String s) {
			this.s = s;
			i = 0;
			line = 1;
			pos = 1;
		}

		public void move() {
			i++;
			pos++;
		}

		public void move(int k) {
			i += k;
			pos += k;
		}

		public boolean end() {
			return i >= s.length();
		}

		public char get() {
			return s.charAt(i);
		}

		public boolean is(String ss) {
			return i < s.length() - ss.length() && s.substring(i, i + ss.length()).equals(ss);
		}

		public boolean is(char c) {
			return i < s.length() && s.charAt(i) == c;
		}

		public void skipWhiteSpace() {
			while (i < s.length() && s.charAt(i) <= 32) {
				char cc = s.charAt(i);
				if (cc == '\n') {
					line++;
					pos = 1;
				} else if (cc == ' ' || cc == '\t') {
					pos++;
				}
				i++;
			}
		}
	}

	public static Object jsonToData(Current c) {
		c.skipWhiteSpace();

		if (c.end()) {
			return "";
		}

		char cc = c.get();
		if (cc == '{') {
			c.move();
			return jsonToMap(c);
		}
		if (cc == '[') {
			c.move();
			return jsonToList(c);
		}
		return jsonToObject(c);
	}

	private static Object jsonToObject(Current c) {
		if (c.is('"')) {
			c.move();
			StringBuilder ss = new StringBuilder();
			while (!c.end() && c.get() != '"') {
				if (c.get() == '\\') {
					c.move();
					char cc = c.get();
					switch (cc) {
						case '\\':
							ss.append('\\');
							break;
						case '"':
							ss.append('\"');
							break;
						case 'n':
							ss.append('\n');
							break;
						case 'r':
							ss.append('\r');
							break;
						case 't':
							ss.append('\t');
							break;
						default:
							throw new IllegalArgumentException("JSON parsing error: Invalid escaped character at (" + c.line + ':' + c.pos + "): " + cc);
					}
				} else {
					ss.append(c.get());
				}
				c.move();
			}
			if (!c.end()) {
				c.move(); // Skip the closing "
			}
			return ss.toString();
		}
		if (c.is("true")) {
			c.move(4);
			return true;
		}
		if (c.is("false")) {
			c.move(5);
			return false;
		}
		if (c.is("null")) {
			c.move(4);
			return null;
		}
		StringBuilder ss = new StringBuilder();
		while (!c.end() && c.get() != ',' && c.get() != ']' && c.get() != '}' && c.get() != '\n' && c.get() != '\t') {
			ss.append(c.get());
			c.move();
		}
		try {
			return Long.valueOf(ss.toString());
		} catch (Exception e1) {
			try {
				return Double.valueOf(ss.toString());
			} catch (Exception e2) {
				throw new IllegalArgumentException("JSON parsing error: Invalid value at (" + c.line + ':' + c.pos + "): " + ss.toString());
			}
		}
	}

	private static List<Object> jsonToList(Current c) {
		List<Object> ll = new ArrayList<>();
		c.skipWhiteSpace();
		while (!c.end() && c.get() != ']') {
			Object value = jsonToData(c);

			ll.add(value);

			c.skipWhiteSpace();
			if (c.is(',')) {
				c.move();
				c.skipWhiteSpace();
			}
		}

		if (!c.end()) {
			c.move(); // Skip the closing ]
		}

		return ll;
	}

	private static Map<String, Object> jsonToMap(Current c) {
		Map<String, Object> mm = new HashMap<>();
		c.skipWhiteSpace();
		while (!c.end() && c.get() != '}') {
			Object key = jsonToObject(c);
			if (!(key instanceof String)) {
				throw new IllegalArgumentException("JSON parsing error: Invalid key at (" + c.line + ':' + c.pos + "): " + key);
			}

			c.skipWhiteSpace();
			if (!c.is(':')) {
				throw new IllegalArgumentException("JSON parsing error: Expected ':' at (" + c.line + ':' + c.pos + ")");
			}

			c.move(); // Skip the :
			Object value = jsonToData(c);

			mm.put((String) key, value);

			c.skipWhiteSpace();
			if (c.is(',')) {
				c.move();
				c.skipWhiteSpace();
			}
		}

		if (!c.end()) {
			c.move(); // Skip the closing }
		}

		return mm;
	}

	public static String dataToJson(Object o, boolean escape) {
		StringBuilder sb = new StringBuilder();
		str(sb, o, 0, false, escape);
		return sb.toString();
	}

	public static String dataToJson(Object o) {
		return dataToJson(o, true);
	}

	@SuppressWarnings("unchecked")
	private static void str(StringBuilder ss, Object o, int indent, boolean padFirst, boolean escape) {
		if (o == null || o instanceof Boolean || o instanceof Number) {
			if (padFirst) {
				ss.append(pad(indent));
			}
			ss.append(o);
			return;
		}

		if (o instanceof String) {
			String s = escape ? escapeJson((String) o) : (String) o;
			if (padFirst) {
				ss.append(pad(indent));
			}
			ss.append('"').append(s).append('"');
			return;
		}

		if (o instanceof Number || o instanceof Boolean) {
			if (padFirst) {
				ss.append(pad(indent));
			}
			ss.append(o);
			return;
		}

		if (o instanceof Map) {
			Map<String, Object> mm = (Map<String, Object>) o;

			if (padFirst) {
				ss.append(pad(indent));
			}
			ss.append("{\n");

			boolean first = true;
			for (String k : mm.keySet()) {
				if (!first) {
					ss.append(",\n");
				}
				first = false;

				Object v = mm.get(k);
				ss.append(pad(indent + 1));
				if (escape) {
					ss.append('"');
				}
				ss.append(k);
				if (escape) {
					ss.append('"');
				}
				ss.append(": ");
				str(ss, v, indent + 1, false, escape);
			}

			ss.append("\n");
			ss.append(pad(indent)).append('}');

			return;
		}

		if (o instanceof List) {
			List<Object> ll = (List<Object>) o;

			if (padFirst) {
				ss.append(pad(indent));
			}
			ss.append("[\n");

			boolean first = true;
			for (Object o1 : ll) {
				if (!first) {
					ss.append(",\n");
				}
				first = false;

				str(ss, o1, indent + 1, true, escape);
			}

			ss.append("\n");
			ss.append(pad(indent)).append(']');
		}
	}

	private static String escapeJson(String v) {
		String s = v.replaceAll("\\\\", "\\\\\\\\");
		s = s.replaceAll("\"", "\\\\\"");
		s = s.replaceAll("\n", "\\\\n");
		s = s.replaceAll("\r", "\\\\r");
		s = s.replaceAll("\t", "\\\\t");
		return s;
	}

	private static String pad(int n) {
		String s = "";
		for (int i = 0; i < n; i++) {
			s += "    ";
		}
		return s;
	}
}
