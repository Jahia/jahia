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
package org.springframework.mock.env;

import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Simple {@link PropertySource} implementation for use in testing. Accepts
 * a user-provided {@link Properties} object, or if omitted during construction,
 * the implementation will initialize its own.
 *
 * The {@link #setProperty} and {@link #withProperty} methods are exposed for
 * convenience, for example:
 * <pre class="code">
 * {@code
 *   PropertySource<?> source = new MockPropertySource().withProperty("foo", "bar");
 * }
 * </pre>
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.mock.env.MockEnvironment
 */
public class MockPropertySource extends PropertiesPropertySource {

	/**
	 * {@value} is the default name for {@link MockPropertySource} instances not
	 * otherwise given an explicit name.
	 * @see #MockPropertySource()
	 * @see #MockPropertySource(String)
	 */
	public static final String MOCK_PROPERTIES_PROPERTY_SOURCE_NAME = "mockProperties";

	/**
	 * Create a new {@code MockPropertySource} named {@value #MOCK_PROPERTIES_PROPERTY_SOURCE_NAME}
	 * that will maintain its own internal {@link Properties} instance.
	 */
	public MockPropertySource() {
		this(new Properties());
	}

	/**
	 * Create a new {@code MockPropertySource} with the given name that will
	 * maintain its own internal {@link Properties} instance.
	 * @param name the {@linkplain #getName() name} of the property source
	 */
	public MockPropertySource(String name) {
		this(name, new Properties());
	}

	/**
	 * Create a new {@code MockPropertySource} named {@value #MOCK_PROPERTIES_PROPERTY_SOURCE_NAME}
	 * and backed by the given {@link Properties} object.
	 * @param properties the properties to use
	 */
	public MockPropertySource(Properties properties) {
		this(MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, properties);
	}

	/**
	 * Create a new {@code MockPropertySource} with the given name and backed by the given
	 * {@link Properties} object.
	 * @param name the {@linkplain #getName() name} of the property source
	 * @param properties the properties to use
	 */
	public MockPropertySource(String name, Properties properties) {
		super(name, properties);
	}

	/**
	 * Set the given property on the underlying {@link Properties} object.
	 */
	public void setProperty(String name, Object value) {
		this.source.put(name, value);
	}

	/**
	 * Convenient synonym for {@link #setProperty} that returns the current instance.
	 * Useful for method chaining and fluent-style use.
	 * @return this {@link MockPropertySource} instance
	 */
	public MockPropertySource withProperty(String name, Object value) {
		this.setProperty(name, value);
		return this;
	}

}
