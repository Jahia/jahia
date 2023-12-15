package org.jahia.bundles.richtext.config.parse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Parser {

    private enum PolicyType {
        ALLOW, DISALLOW;
    }

    private static Map<String, Pattern> PATTERNS;

    static {
        PATTERNS = new HashMap<>();
        PATTERNS.put("NUMBER_OR_PERCENT", Pattern.compile("[0-9]+%?"));
        PATTERNS.put("ONSITE_URL", Pattern.compile("(?:[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]+|\\#(\\w)+)"));
        PATTERNS.put("HTML_ID", Pattern.compile("[a-zA-Z0-9\\:\\-_\\.]+"));
        PATTERNS.put("OFFSITE_URL", Pattern.compile("\\s*(?:(?:ht|f)tps?://|mailto:)[\\p{L}\\p{N}]"
                        + "[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*+\\s*"));
        PATTERNS.put("HTML_CLASS", Pattern.compile("[a-zA-Z0-9\\s,\\-_]+"));
        PATTERNS.put("NUMBER", Pattern.compile("[+-]?(?:(?:[0-9]+(?:\\.[0-9]*)?)|\\.[0-9]+)"));
        PATTERNS.put("NAME", Pattern.compile("[a-zA-Z0-9\\-_\\$]+"));
        PATTERNS.put("ALIGN", Pattern.compile("(?i)center|left|right|justify|char"));
        PATTERNS.put("VALIGN", Pattern.compile("(?i)baseline|bottom|middle|top"));
        PATTERNS.put("PARAGRAPH", Pattern.compile("(?:[\\p{L}\\p{N},'\\.\\s\\-_\\(\\)]|&[0-9]{2};)*"));
    }

    public abstract String parseToJsonString(JSONObject json);

    public static PolicyFactory parseToPolicy(JSONObject json) {
        HtmlPolicyBuilder builder = new HtmlPolicyBuilder();
        JSONObject filtering = json.getJSONObject("htmlFiltering");

        handleAllow(filtering, builder);
        handleDisallow(filtering, builder);

        return builder.toFactory();
    }

    private static void handleAllow(JSONObject filtering, HtmlPolicyBuilder builder) {
        handleAttributes(filtering, builder, PolicyType.ALLOW);
        handleElements(filtering, builder, PolicyType.ALLOW);
        handleProtocols(filtering, builder, PolicyType.ALLOW);
    }

    private static void handleDisallow(JSONObject filtering, HtmlPolicyBuilder builder) {
        if (filtering.has("disallow")) {
            JSONObject dis = filtering.getJSONObject("disallow");
            handleAttributes(dis, builder, PolicyType.DISALLOW);
            handleElements(dis, builder, PolicyType.DISALLOW);
            handleProtocols(dis, builder, PolicyType.DISALLOW);
        }
    }

    private static void handleProtocols(JSONObject filtering, HtmlPolicyBuilder builder, PolicyType policyType) {
        if (filtering.has("protocols")) {
            Object p = filtering.get("protocols");

            if (!(p instanceof JSONArray)) {
                p = new JSONArray(new String[]{(String) p});
            }

            if (policyType == PolicyType.ALLOW) {
                builder.allowUrlProtocols(jsonArrayToArray((JSONArray) p));
            } else {
                builder.disallowUrlProtocols(jsonArrayToArray((JSONArray) p));
            }
        }
    }

    private static void handleElements(JSONObject filtering, HtmlPolicyBuilder builder, PolicyType policyType) {
        if (filtering.has("elements")) {
            JSONArray elems = filtering.getJSONArray("elements");

            elems.forEach(jsonObject -> {
                Object name = ((JSONObject)jsonObject).get("name");

                if (!(name instanceof JSONArray)) {
                    name = new JSONArray(new String[]{(String) name});
                }

                if (policyType == PolicyType.ALLOW) {
                    builder.allowElements(jsonArrayToArray((JSONArray) name));
                } else {
                    builder.disallowElements(jsonArrayToArray((JSONArray) name));
                }
            });
        }
    }

    private static void handleAttributes(JSONObject filtering, HtmlPolicyBuilder builder, PolicyType policyType) {
        if  (filtering.has("attributes")) {
            JSONArray attr = filtering.getJSONArray("attributes");

            attr.forEach(jsonObject -> {
                boolean isGlobal = !((JSONObject)jsonObject).has("elements");
                Object name = ((JSONObject)jsonObject).get("name");

                if (!(name instanceof JSONArray)) {
                    name = new JSONArray(new String[]{(String) name});
                }

                HtmlPolicyBuilder.AttributeBuilder attrBuilder;

                if (policyType == PolicyType.ALLOW) {
                    attrBuilder = builder.allowAttributes(jsonArrayToArray((JSONArray) name));
                } else {
                    attrBuilder = builder.disallowAttributes(jsonArrayToArray((JSONArray) name));
                }

                if (((JSONObject)jsonObject).has("pattern")) {
                    String pattern = ((JSONObject)jsonObject).getString("pattern");
                    Pattern p = PATTERNS.containsKey(pattern) ? PATTERNS.get(pattern) : Pattern.compile(pattern);
                    attrBuilder.matching(p);
                }

                if (isGlobal) {
                    attrBuilder.globally();
                } else {
                    Object elems = ((JSONObject)jsonObject).get("elements");

                    if (!(elems instanceof JSONArray)) {
                        elems = new JSONArray(new String[]{(String) elems});
                    }

                    attrBuilder.onElements(jsonArrayToArray((JSONArray) elems));
                }
            });
        }
    }

    private static String[] jsonArrayToArray(JSONArray array) {
        String[] a = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            a[i] = array.getString(i);
        }
        return a;
    }
}
