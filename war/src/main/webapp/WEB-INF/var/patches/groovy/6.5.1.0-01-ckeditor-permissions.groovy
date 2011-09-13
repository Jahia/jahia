import org.jahia.services.content.*

import javax.jcr.*

def sysout = out;

sysout << "Start renaming CKEditor permissions\n"

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
	public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
		JCRNodeWrapper permissions = session.getNode("/permissions/wysiwyg-editor-toolbar");
		sysout << "Found node " + permissions.getPath() + "\n"
		
		if (permissions.hasNode("full")) {
			session.checkout(permissions);
			JCRNodeWrapper perm = permissions.getNode("full");
			sysout << "Renaming " + perm.getPath() + " ...\n"
			perm.rename("view-full-wysiwyg-editor");
			sysout << "... renamed to " + perm.getPath() + "\n"
		} else {
			sysout << "Node " + permissions.getPath() + "/full not found. Skipping.\n"
		}
		
		if (permissions.hasNode("basic")) {
			session.checkout(permissions);
			JCRNodeWrapper perm = permissions.getNode("basic");
			sysout << "Renaming " + perm.getPath() + " ...\n"
			perm.rename("view-basic-wysiwyg-editor");
			sysout << "... renamed to " + perm.getPath() + "\n"
		} else {
			sysout << "Node " + permissions.getPath() + "/basic not found. Skipping.\n"
		}
		
		if (permissions.hasNode("light")) {
			session.checkout(permissions);
			JCRNodeWrapper perm = permissions.getNode("light");
			sysout << "Renaming " + perm.getPath() + " ...\n"
			perm.rename("view-light-wysiwyg-editor");
			sysout << "... renamed to " + perm.getPath() + "\n"
		} else {
			sysout << "Node " + permissions.getPath() + "/light not found. Skipping.\n"
		}
		
		session.save();
				
		return null;
    }
});

sysout << "... done renaming CKEditor permissions.\n"
