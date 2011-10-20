import org.jahia.services.content.*

import javax.jcr.*
import javax.jcr.query.Query

def sysout = out;

sysout << "socialActivity patch : Start \n"

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:socialActivity]", Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        int i = 0;
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
			sysout << "processing  ${next.path}"
            if (next.hasProperty("j:targetNode") && next.getProperty("j:targetNode").getString().length() > 0
                    && !next.getProperty("j:targetNode").getString().startsWith("/")) {
                String copyPath = next.parent.path;
				String copiedNode = copyPath + "/" + next.name + "c"
				String originalName = next.name
                String path = "";
				path = (String) session.getNodeByUUID(next.getProperty("j:targetNode").getString()).getPath();
				if (!path.equals("")) {
				     i ++;
                    session.getNode(next.path).copy(copyPath,next.name + "c");
                    session.save();
                    session.removeItem(next.path);
                    session.save();
                    JCRNodeWrapper n = session.getNode(copiedNode)
                    n.setProperty("j:targetNode",path);
					session.save();
					sysout << " - targetNode modified to : ${path}"
				}
           }
		   sysout << "\n"

        }
        sysout << "socialActivity patch : ${i} node(s) modified.\n"
        session.save();
        sysout << "socialActivity patch : session saved.\n"
    }

	
});
sysout << "socialActivity patch : End \n"