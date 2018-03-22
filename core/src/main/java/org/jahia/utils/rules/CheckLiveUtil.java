package org.jahia.utils.rules;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.rules.NodeFact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;

/**
 * drools Util class to remove properties from other workspace (for instance
 * j:published) if a node is removed from the other workspace.
 *
 * @author wassek
 */
public class CheckLiveUtil {

	private static final Logger logger = LoggerFactory.getLogger(CheckLiveUtil.class);

	/*
	 * Checks if the node has a specific property with specific value
	 * 
	 * @param node rules NodeFact which contains the node which should be
	 * checked
	 * 
	 * @param propertyName Name of the property
	 * 
	 * @param propertyValue Value of the property to check
	 * 
	 * @return <code>true</code> if property with value exists
	 */
	public static boolean checkProperty(NodeFact node, String propertyName, String propertyValue) {

		String path = "";
		try {
			path = node.getPath();
			JCRSessionWrapper session = JCRSessionFactory.getInstance()
					.getCurrentSystemSession(Constants.EDIT_WORKSPACE, node.getSession().getLocale(), null);
			if (session.itemExists(path)) {
				JCRNodeWrapper defNode = session.getNode(path);
				if (defNode.hasProperty(propertyName)
						&& StringUtils.equals(defNode.getPropertyAsString(propertyName), propertyValue)) {
					return true;
				}
			}
		} catch (RepositoryException ex) {
			logger.error("Exception when checking the " + propertyName + " on node " + path, ex);
		}
		return false;
	}

	/*
	 * Remove a property from a node in a specific workspace
	 * 
	 * @param node rules NodeFact which contains the node which should be
	 * checked
	 * 
	 * @param propertyName Name of the property
	 * 
	 * @param workspace workspace where the property should removed
	 */
	public static void removeProperyInWorkspaceFromNode(NodeFact node, String propertyName, String workspace) {

		try {
			JCRObservationManager.pushEventListenersAvailableDuringPublishOnly();
			JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(workspace,
					node.getSession().getLocale(), null);
			if (session.itemExists(node.getPath())) {
				String otherWorkspace = Constants.EDIT_WORKSPACE.equals(workspace) ? Constants.LIVE_WORKSPACE
						: Constants.EDIT_WORKSPACE;
				JCRSessionWrapper otherSession = JCRSessionFactory.getInstance().getCurrentSystemSession(otherWorkspace,
						node.getSession().getLocale(), null);
				removePropertyFromSubnode(session.getNode(node.getPath()), propertyName, otherSession);
				session.save();
			}
		} catch (RepositoryException ex) {
			logger.error("cannot remove Property " + propertyName, ex);
		} finally {
			JCRObservationManager.popEventListenersAvailableDuringPublishOnly();
		}

	}

	private static void removePropertyFromSubnode(JCRNodeWrapper node, String propertyName,
			JCRSessionWrapper otherSession) throws RepositoryException {
		if (node.hasProperty(propertyName)) {
			node.getProperty(propertyName).remove();
		}
		if (node.hasNodes()) {
			JCRNodeIteratorWrapper it = node.getNodes();
			while (it.hasNext()) {
				JCRNodeWrapper subNode = (JCRNodeWrapper) it.next();
				if (!otherSession.itemExists(subNode.getPath())) {
					removePropertyFromSubnode(subNode, propertyName, otherSession);
				}
			}
		}

	}

}
