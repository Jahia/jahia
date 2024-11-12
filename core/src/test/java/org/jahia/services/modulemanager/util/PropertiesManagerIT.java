/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.modulemanager.util;

import com.google.common.io.Resources;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PropertiesManagerIT {

    @Test
    public void copyJsonValues() throws JSONException, IOException {


        Map<String, String> m = new HashMap<>();
        m.put("a", "aa");
        m.put("b", "bb");
        m.put("c", "cc");
        JSONObject obj = new JSONObject(m);
        JSONArray array = new JSONArray(Arrays.asList("a","b", "c"));
        Map<String, Object> nestedMap = new HashMap<>();

        nestedMap.put("text", "aa");
        nestedMap.put("list", array);
        nestedMap.put("object", obj);
        JSONObject nestedObj = new JSONObject(nestedMap);
        JSONArray nestedList = new JSONArray(Arrays.asList("a",array, nestedObj));



        PropertiesManager config = new PropertiesManager(new HashMap<>());
        // Test injecting an array as value
        PropertiesValues configValues = config.getValues();
        Assert.assertEquals("New config creates an empty object", configValues.toJSON().toString(), readJsonFile("empty").toString());
        configValues.getList("list").updateFromJSON(array);
        Assert.assertEquals("Config updated with an array", configValues.toJSON().toString(), readJsonFile("array").toString());
        //Assert.assertEquals(configValues.toJSON().equals());
        // Test injecting a map as value at root level
        configValues.updateFromJSON(obj);
        Assert.assertEquals("Config updated with an object", configValues.toJSON().toString(), readJsonFile("object").toString());

        // Test injecting an nested array as value
        configValues.getList("nestedList").updateFromJSON(nestedList);
        Assert.assertEquals("Config updated with a nested array", configValues.toJSON().toString(), readJsonFile("nested-array").toString());
        // Test injecting a map as value
        configValues.getValues("nestedObj").updateFromJSON(nestedObj);
        Assert.assertEquals("Config updated with a nested object", configValues.toJSON().toString(), readJsonFile("nested-object").toString());
    }

    private JSONObject readJsonFile(String name) throws IOException, JSONException {
       URL jsonResource =  this.getClass().getClassLoader().getResource(String.format("META-INF/json-results/%s.json", name));
       return new JSONObject(Resources.toString(jsonResource, StandardCharsets.UTF_8));
    }
}
