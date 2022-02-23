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
package org.jahia.bin.errors;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;

/**
 * A Log4J appender that will log exceptions through the ErrorFileDumper system.
 *
 * User: loom
 * Date: Jul 16, 2010
 * Time: 3:40:31 PM
 */
@Plugin(name = "ExceptionAppender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ExceptionAppender extends AbstractAppender {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExceptionAppender.class);

    private boolean alreadyDumping = false;

    /**
     * Builds ExceptionAppender instances.
     *
     * @param <B> The type to build
     */
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<ExceptionAppender> {

        @Override
        public ExceptionAppender build() {
            return new ExceptionAppender(getName(), getLayout(), getFilter());
        }
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    private ExceptionAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter) {
        super(name, filter, layout, false, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        // first let's prevent re-entry
        if (alreadyDumping || event.getThrown() == null || ErrorFileDumper.isShutdown()) {
            return;
        }

        try {
            alreadyDumping = true;
            ErrorFileDumper.dumpToFile(event.getThrown(), null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            alreadyDumping = false;
        }
    }

    public void close() {
        // do nothing
    }

    public boolean requiresLayout() {
        return false;
    }

}
