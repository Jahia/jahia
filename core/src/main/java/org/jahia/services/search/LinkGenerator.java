/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * http://www.jahia.com
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ==================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;

import java.util.Objects;

/**
 * A utility class allowing {@link SearchProvider} implementations to decouple search results from JCR nodes by
 * using {@link MatchInfo} identifying a target JCR node that only will be resolved if/when a user activates the
 * link this {@link LinkGenerator} creates. This generator creates links that use the
 * {@link org.jahia.bin.TemplateResolverServlet} to resolve nodes when activated.
 *
 * @author Christophe Laprun
 */
public class LinkGenerator {

    private static final String RESOLVE_CONTEXT = "/resolve/";

    /**
     * Composes a link using the provided {@link MatchInfo} and {@link RenderContext}.
     *
     * @param info    the {@link MatchInfo} identifying the target JCR node
     * @param context the {@link RenderContext} in which the link will be created
     * @return a link targeting the {@link org.jahia.bin.TemplateResolverServlet} for delayed JCR node resolution.
     */
    public static String composeLink(MatchInfo info, RenderContext context) {
        Objects.requireNonNull(info);

        final String contextPath = context != null ? context.getURLGenerator().getContext() + "/cms" : null;
        final String initialPath = StringUtils.isEmpty(contextPath) ? RESOLVE_CONTEXT : context + RESOLVE_CONTEXT;

        return initialPath + info.getWorkspace() + "/" + info.getLang() + "/" + info.getId();
    }

    /**
     * Takes a string version of a composed link and extracts the {@link MatchInfo} that was used to create it.
     *
     * @param link a link previously created using {@link #composeLink(MatchInfo, RenderContext)}
     * @return a {@link MatchInfo} instance containing the information used to previously compose the specified link
     * @throws IllegalArgumentException when the specified link doesn't contain the information needed to extract a
     *                                  {@link MatchInfo} instance or if the format is not the expected one
     */
    public static MatchInfo decomposeLink(String link) {
        Objects.requireNonNull(link);

        final int context = link.indexOf(RESOLVE_CONTEXT);
        if (context != -1) {
            final String[] components = link.substring(context + RESOLVE_CONTEXT.length()).split("/");
            if (components.length != 3) {
                throw new IllegalArgumentException("Wrong format: " + link
                        + " doesn't contain the appropriate (workspace, language, path) components. Components were: "
                        + StringUtils.join(components));
            }

            return new MatchInfo(components[2], components[0], components[1]);
        } else {
            throw new IllegalArgumentException("Wrong format: " + link
                    + " cannot be decomposed as a MatchInfo.");
        }
    }
}
