/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        super(name, filter, layout, ignoreExceptions);
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