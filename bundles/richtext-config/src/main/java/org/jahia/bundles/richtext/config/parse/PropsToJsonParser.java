package org.jahia.bundles.richtext.config.parse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Given a flat map of this kind:
 *
 *         Map<String, Object> flatMap = new HashMap<>();
 *         flatMap.put("name", "John Doe");
 *         flatMap.put("age", 30);
 *         flatMap.put("address.city", "New York");
 *         flatMap.put("address.zipCodes[0]", "10001");
 *         flatMap.put("address.zipCodes[1]", "10002");
 *         flatMap.put("country[0]", "US");
 *         flatMap.put("country[1]", "Canada");
 *         flatMap.put("contacts[0].type", "email");
 *         flatMap.put("contacts[0].value", "john.doe@example.com");
 *         flatMap.put("contacts[1].type", "phone");
 *         flatMap.put("contacts[1].value", "123-456-7890");
 *         flatMap.put("contacts[2].sub[0].type", "phone");
 *         flatMap.put("contacts[2].sub[0].value", "123-456-7890");
 *         flatMap.put("jcrestapi.grants[0].node.withPermission", "my-access");
 *         flatMap.put("jcrestapi.grants[1].node.withPermission", "api-access");
 *
 * it produces a json object.
 */
public class PropsToJsonParser {

    private int currentIndex;
    private Object current;

    public JSONObject parse(Map<String, Object> flatMap) {
        JSONObject jsonObject = new JSONObject();
        current = jsonObject;

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String[] segments = entry.getKey().split("\\.");
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                handleSegment(segment, entry.getValue(), segments.length - 1 == i);
            }
            current = jsonObject;
            currentIndex = -1;
        }

        return jsonObject;
    }

    private void handleSegment(String segment, Object value, boolean endSegment) {
        if (segment.indexOf('[') != -1) {
            int bracketIndex = segment.indexOf('[');
            int bracketEndIndex = segment.indexOf(']');
            int index = Integer.parseInt(segment.substring(bracketIndex + 1, bracketEndIndex));
            String arrayKey = segment.substring(0, bracketIndex);

            if (current instanceof JSONObject) {
                handleArrayInObject((JSONObject) current, arrayKey, value, endSegment, index);
            } else if (current instanceof JSONArray) {
                handleArrayInArray((JSONArray) current, arrayKey, value, endSegment, index);
            }
        } else {
            if (current instanceof JSONObject) {
                handleObjectInObject((JSONObject) current, segment, value, endSegment);
            } else if (current instanceof JSONArray) {
                handleObjectInArray((JSONArray) current, segment, value, endSegment);
            }
        }
    }

    private void handleArrayInObject(JSONObject o, String segment, Object value, boolean endSegment, int localIndex) {
        JSONArray array = o.has(segment) ? o.getJSONArray(segment) : new JSONArray();

        if (endSegment) {
            array.put(localIndex, value);
        }

        o.put(segment, array);
        current = array;
        currentIndex = localIndex;
    }

    private void handleArrayInArray(JSONArray a, String segment, Object value, boolean endSegment, int localIndex) {
        Object o = a.length() > currentIndex ? a.get(currentIndex) : JSONObject.NULL;
        JSONArray arr = new JSONArray();

        if (o == JSONObject.NULL) {
            o = new JSONObject();
        }

        if (!((JSONObject)o).has(segment)) {
            ((JSONObject)o).put(segment, arr);
        }

        arr = ((JSONObject)o).getJSONArray(segment);

        if (endSegment) {
            arr.put(localIndex, value);
        }

        a.put(currentIndex, o);
        current = arr;
        currentIndex = localIndex;
    }

    private void handleObjectInObject(JSONObject o, String segment, Object value, boolean endSegment) {
        if (endSegment) {
            o.put(segment, handleEndSegment(segment, value));
        } else {
            JSONObject obj = o.has(segment) ? o.getJSONObject(segment) : new JSONObject();
            o.put(segment, obj);
            current = obj;
        }
    }

    private void handleObjectInArray(JSONArray a, String segment, Object value, boolean endSegment) {
        Object o = a.length() > currentIndex ? a.get(currentIndex) : new JSONObject();

        if (o == JSONObject.NULL) {
            o = new JSONObject();
        }

        if (endSegment) {
            ((JSONObject)o).put(segment, handleEndSegment(segment, value));
            current = o;
        } else {
            JSONObject n = new JSONObject();
            ((JSONObject)o).put(segment, n);
            current = n;
        }

        a.put(currentIndex, o);
    }

    private Object handleEndSegment(String segment, Object value) {
        if (segment.equals("pattern") || !((String)value).contains(",")) {
            return value;
        }

        return new JSONArray(((String)value).split("\\s*,\\s*"));
    }
}
