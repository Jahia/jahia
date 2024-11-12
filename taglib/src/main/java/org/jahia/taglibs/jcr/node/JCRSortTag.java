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
package org.jahia.taglibs.jcr.node;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.text.Collator;
import java.util.*;

/**
 * User: toto
 * Date: Mar 25, 2010
 * Time: 8:35:48 PM
 */
public class JCRSortTag extends AbstractJCRTag {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRSortTag.class);

    private static final long serialVersionUID = 8801219769991582550L;

    private Collection<JCRNodeWrapper> list;

    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private String properties;

    public void setList(Object o) {
        if (o instanceof Collection) {
            this.list = (Collection) o;
        } else if (o instanceof Iterator) {
            this.list = new ArrayList<JCRNodeWrapper>();
            final Iterator<?> iterator = (Iterator<?>) o;
            while (iterator.hasNext()) {
                JCRNodeWrapper e = (JCRNodeWrapper) iterator.next();
                this.list.add(e);
            }
        }
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public int doEndTag() throws JspException {
        List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>(
                list != null ? list : Collections.<JCRNodeWrapper>emptyList());

        String[] props = Patterns.COMMA.split(properties);
        Collections.sort(res, new NodeComparator(props));

        pageContext.setAttribute(var, res, scope);
        resetState();

        return super.doEndTag();
    }

    static class NodeComparator implements Comparator<JCRNodeWrapper> {
        private String[] props;

        NodeComparator(String[] props) {
            if (props == null) {
                throw new IllegalArgumentException("Should provide a valid array of properties to compare from");
            }
            this.props = props;
        }

        public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
            // first make sure that we return 0 if both are equal
            if (o1.equals(o2)) {
                return 0;
            }

            // if we don't have length, be consistent with equals (i.e. compare paths)
            if (props.length == 0) {
                return o1.getPath().compareToIgnoreCase(o2.getPath());
            }

            // default value is true, if the corresponding property has not been provided at the beginning of the list
            // (templates developed before this change)
            boolean ignoreCase = true;
            int startIndex = 0;

            // if we have an odd number of "properties", the first one is a boolean indicating whether the sorting is case-sensitive or not
            if (props.length % 2 == 1) {
                ignoreCase = Boolean.valueOf(props[0]);
                startIndex = 1;
            }

            int result = 0;
            int power = props.length / 2;
            for (int i = startIndex; i < props.length; i += 2) {
                String prop = props[i];
                String dir = props[i + 1];

                // we use a multiplier for each property in order to denote the importance of a given prop in the ordering
                int powerOf10 = (int) Math.pow(10, power--);
                int multiplier = "desc".equals(dir) ? -powerOf10 : powerOf10;

                String referenceProp = null;
                try {
                    prop = prop.trim();
                    if (prop.length() > 0) {
                        if (prop.contains(";")) {
                            String[] split = Patterns.SEMICOLON.split(prop);
                            prop = split[0];
                            referenceProp = split[1];
                        }

                        final boolean o1HasProp = o1.hasProperty(prop);
                        final boolean o2HasProp = o2.hasProperty(prop);
                        int r = 0;
                        if (!o1HasProp && o2HasProp) {
                            r = -multiplier;
                        } else if (!o2HasProp && o1HasProp) {
                            r = multiplier;
                        } else if (o1HasProp && o2HasProp) {
                            Property p1 = o1.getProperty(prop);
                            Property p2 = o2.getProperty(prop);
                            if (referenceProp != null) {
                                p1 = p1.getNode().getProperty(referenceProp);
                                p2 = p2.getNode().getProperty(referenceProp);
                            }

                            switch (p1.getType()) {
                                case PropertyType.DATE:
                                    r = p1.getDate().compareTo(p2.getDate());
                                    break;
                                case PropertyType.DECIMAL:
                                case PropertyType.LONG:
                                case PropertyType.DOUBLE:
                                    r = Double.compare(p1.getDouble(), p2.getDouble());
                                    break;
                                default:
                                    final Collator collator = Collator.getInstance(Locale.forLanguageTag(o1.getLanguage()));
                                    if (ignoreCase) {
                                        collator.setStrength(Collator.TERTIARY);
                                    } else {
                                        collator.setStrength(Collator.SECONDARY);
                                    }
                                    r = collator.compare(p1.getString(), p2.getString());
                                    break;
                            }
                        }

                        result += r * multiplier;
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }

            }

            return result;
        }

    }

    @Override
    protected void resetState() {
        list = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        properties = null;
        super.resetState();
    }
}
