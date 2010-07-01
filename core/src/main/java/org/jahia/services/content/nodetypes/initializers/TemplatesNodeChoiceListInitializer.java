package org.jahia.services.content.nodetypes.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 1, 2010
 * Time: 3:37:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplatesNodeChoiceListInitializer implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(TemplatesNodeChoiceListInitializer.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        if (node == null) {
            node = (JCRNodeWrapper) context.get("contextParent");
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();

        try {
            JCRNodeWrapper site = node.resolveSite();
            final JCRSessionWrapper session = site.getSession();
            final QueryManager queryManager = session.getWorkspace().getQueryManager();
            QueryResult result = queryManager.createQuery("select * from [jnt:masterTemplate] as n where isdescendantnode(n,['"+site.getPath()+"'])", Query.JCR_SQL2).execute();
            final NodeIterator iterator = result.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper tpl = (JCRNodeWrapper) iterator.next();
                vs.add(new ChoiceListValue(tpl.getName(), null, session.getValueFactory().createValue(tpl.getIdentifier(),
                        PropertyType.WEAKREFERENCE)));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get template",e);
        }


        return vs;
    }
}
