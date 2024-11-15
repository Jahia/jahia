/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import org.jahia.services.render.scripting.bundle.BundleScriptingConfigurationConstants;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Christophe Laprun
 */
public class ScriptEngineUtilsTest {
    @Test
    public void canFactoryProcessViewsShouldThrowIllegalArgExceptionIfFactoryIsNull() {
        try {
            ScriptEngineUtils.canFactoryProcessViews(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void canFactoryProcessViewsShouldThrowIllegalArgExceptionIfExtensionIsNullOrEmpty() {
        try {
            ScriptEngineUtils.canFactoryForExtensionProcessViews(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            ScriptEngineUtils.canFactoryForExtensionProcessViews("", null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void canFactoryProcessViewsShouldReturnFalseIfHeadersAreNullOrEmpty() {
        assertFalse(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), null));
        assertFalse(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), new Hashtable<String, String>()));
    }

    @Test
    public void canFactoryProcessViewsShouldBeRobustWithWeirdInput() {
        Dictionary<String, String> headers = new Hashtable<>(2);

        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, ",,,");
        assertFalse(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));

        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, ",     foo  \t\n,,");
        assertTrue(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));

        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, ",     js  \t\n,,    bar,");
        assertTrue(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    @Test
    public void canFactoryProcessViewsShouldReturnFalseIfNoSupportedScriptLanguage() {
        Dictionary<String, String> headers = new Hashtable<>(2);
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, "js,groovy");

        assertFalse(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    @Test
    public void canFactoryProcessViewsShouldReturnFalseIfBundleIndicatedNoViewsEvenIfItCouldProcessViews() {
        Dictionary<String, String> headers = new Hashtable<>(2);
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, "foo,js,groovy");
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_HAS_VIEWS, "no");

        assertFalse(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    @Test
    public void canFactoryProcessViewsShouldWorkWithExtensions() {
        Dictionary<String, String> headers = new Hashtable<>(2);
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, "foo,js,groovy");
        headers.put("color", "black");

        assertTrue(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    @Test
    public void canFactoryProcessViewsShouldWorkWithLanguageName() {
        Dictionary<String, String> headers = new Hashtable<>(2);
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, "groovy,js,lang");
        headers.put("color", "black");

        assertTrue(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    @Test
    public void canFactoryProcessViewsShouldWorkWithEngineName() {
        Dictionary<String, String> headers = new Hashtable<>(2);
        headers.put(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS, "js,test,groovy");
        headers.put("color", "black");

        assertTrue(ScriptEngineUtils.canFactoryProcessViews(new TestScriptEngineFactory(), headers));
    }

    private static class TestScriptEngineFactory implements ScriptEngineFactory {
        private static final List<String> EXTENSIONS = new ArrayList<>(2);
        private static final List<String> MIME_TYPES = new ArrayList<>(2);
        private static final List<String> NAMES = new ArrayList<>(2);

        static {
            EXTENSIONS.add("foo");
            EXTENSIONS.add("bar");
            MIME_TYPES.add("application/foo");
            MIME_TYPES.add("application/bar");
            NAMES.add("test");
            NAMES.add("test 1.0");
            NAMES.add("foo");
        }

        @Override
        public String getEngineName() {
            return "test";
        }

        @Override
        public String getEngineVersion() {
            return "1.0";
        }

        @Override
        public List<String> getExtensions() {
            return EXTENSIONS;
        }

        @Override
        public List<String> getMimeTypes() {
            return MIME_TYPES;
        }

        @Override
        public List<String> getNames() {
            return NAMES;
        }

        @Override
        public String getLanguageName() {
            return "lang";
        }

        @Override
        public String getLanguageVersion() {
            return "1.0";
        }

        @Override
        public Object getParameter(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMethodCallSyntax(String obj, String m, String... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getOutputStatement(String toDisplay) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProgram(String... statements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScriptEngine getScriptEngine() {
            throw new UnsupportedOperationException();
        }
    }
}
