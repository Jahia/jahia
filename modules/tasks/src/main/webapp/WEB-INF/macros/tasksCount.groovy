import org.jahia.services.content.JCRNodeWrapper
import javax.jcr.query.Query
import org.jahia.services.query.QueryResultWrapper
import javax.jcr.RepositoryException
import org.jahia.utils.Patterns

public String sqlEncode(String s) {
    return s != null && s.contains("'") ? Patterns.SINGLE_QUOTE.matcher(s).replaceAll("''") : s;
}

JCRNodeWrapper n = renderContext.getMainResource().getNode();
try {
    String query;
    if (param1 != null) {
        query = "select * from [jnt:task] as t where isdescendantnode(t,['" + sqlEncode(n.getPath()) + "']) and t.type='" + param1 + "' order by [jcr:created] desc";
    } else {
        query = "select * from [jnt:task] as t where isdescendantnode(t,['" + sqlEncode(n.getPath()) + "']) order by [jcr:created] desc";
    }
    Query q = n.getSession().getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
    QueryResultWrapper queryResult = (QueryResultWrapper) q.execute();
    long count = queryResult.getNodes().getSize();
    if (count > 0) {
        print "(" + String.valueOf(count) + ")";
    }
} catch (RepositoryException e) {
}
