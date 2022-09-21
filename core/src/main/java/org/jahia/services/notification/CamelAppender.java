/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification;

import java.io.Serializable;

import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.jahia.services.SpringContextSingleton;

/**
 * Log4j appender that sends the message using {@link CamelNotificationService}.
 *
 * @author Sergiy Shyrkov
 */
@Plugin(name = "CamelAppender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class CamelAppender extends AbstractAppender {

    private CamelNotificationService camelNotificationService;
    private String targetUri;

   /**
     * Builds CamelAppender instances.
     *
     * @param <B> The type to build
     */
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<CamelAppender> {
        @PluginAttribute(value = "targetUri", defaultString = "direct:logs")
        private String targetUri;

        @Override
        public CamelAppender build() {
            final Layout<? extends Serializable> layout = getLayout();
            if (layout == null) {
                AbstractLifeCycle.LOGGER.error("No layout provided for CamelAppender");
                return null;
            }
            CamelAppender appender = new CamelAppender(getName(), layout, getFilter(), isIgnoreExceptions());
            appender.setTargetUri(targetUri);
            return appender;
        }
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    private CamelAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
            final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

	@Override
    public void append(LogEvent event) {
		CamelNotificationService notificationService = getNotificationService();
		if (notificationService != null) {
			// sending formatted message to the configured URI
			notificationService.queueMessagesWithBodyAndHeaders(targetUri,
			        getLayout().toSerializable(event), null);
		}
	}

	public void close() {
		// do nothing
	}

	private CamelNotificationService getNotificationService() {
		if (camelNotificationService == null) {
			SpringContextSingleton springCtx = SpringContextSingleton.getInstance();
			if (springCtx.isInitialized()) {
				camelNotificationService = (CamelNotificationService) springCtx.getContext().getBean(
				        "camelNotificationService");
			}
		}
		return camelNotificationService;
	}

	public boolean requiresLayout() {
		return true;
	}

	public void setTargetUri(String targetUri) {
		this.targetUri = targetUri;
	}

}
