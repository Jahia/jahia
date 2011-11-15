import org.jahia.services.content.*

import javax.jcr.*

def sysout = out;

sysout << "Start granting component usage permission to privileged role\n"

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
	public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
		if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission("/permissions/editMode/useComponent", session).getPath(), "privileged", session)) {
			session.save();
			sysout << "Permission granted\n"
		} else {
			sysout << "Role already has the permission\n"
		}
				
		return null;
    }
});

sysout << "... done granting component usage permission to privileged role.\n"