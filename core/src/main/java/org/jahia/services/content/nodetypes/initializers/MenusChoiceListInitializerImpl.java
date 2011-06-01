package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 7, 2010
 * Time: 9:49:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class MenusChoiceListInitializerImpl implements ChoiceListInitializer{
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MenusChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        Set<ChoiceListValue> set = new  TreeSet<ChoiceListValue>();
        String nodetype = "jnt:navMenu";
        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            if (node == null) {
                node = (JCRNodeWrapper) context.get("contextParent");
            }
            JCRNodeWrapper site = node.getResolveSite();

            QueryResult result = qm.createQuery(
                    "select * from [" + nodetype + "] as n where isdescendantnode(n,['" +site.getPath()+"'])", Query.JCR_SQL2).execute();
            final NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapperImpl nodeWrapper = (JCRNodeWrapperImpl) ni.nextNode();
                String displayName = nodeWrapper.getDisplayableName();
                set.add(new ChoiceListValue(displayName, new HashMap<String, Object>(), new ValueImpl(
                        nodeWrapper.getIdentifier(), PropertyType.STRING, false)));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        listValues.addAll(set);
        return listValues;

    }
}
