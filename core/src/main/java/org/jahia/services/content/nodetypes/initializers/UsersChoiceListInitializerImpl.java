package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Template;

import javax.jcr.query.QueryManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.PropertyType;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Choice list initializer to provide a selection of all users
 *
 * @author : toto
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class UsersChoiceListInitializerImpl implements ChoiceListInitializer {
    public List<ChoiceListValue> getChoiceListValues(ProcessingContext context, ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values) {
        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();

        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            Query q = qm.createQuery("select * from [jnt:user] as user order by user.name", Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
                if (node.getName().equals("guest")) {
                    continue;
                }

                String name = "";
                if (node.hasProperty("j:firstName")) {
                    name += node.getProperty("j:firstName") + " ";
                }
                if (node.hasProperty("j:lastName")) {
                    name += node.getProperty("j:lastName");
                }
                name = name.trim();
                if (name.equals("")) {
                    name = node.getName();
                }
                vs.add(new ChoiceListValue(name, new HashMap<String,Object>(), new ValueImpl(node.getUUID(), PropertyType.WEAKREFERENCE, false)));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return vs;
    }
}
