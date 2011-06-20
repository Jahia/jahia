package org.jahia.tools.contentgenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.GroupBO;
import org.jahia.tools.contentgenerator.bo.UserBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Element;

public class UserGroupService {

	private static final Logger logger = Logger.getLogger(UserGroupService.class.getName());

	public Element generateJcrGroups(Integer nbUsers, Integer nbGroups) throws ParserConfigurationException {
		List<GroupBO> groups = generateUsersAndGroups(nbUsers, nbGroups);

		logger.info("Users and groups generated, creation of Jcr document...");
		Element groupsNode = new Element("groups");
		String jcrDate = ContentGeneratorService.getInstance().getDateForJcrImport(null);
		// groupsNode =
		// ContentGeneratorService.getInstance().addJcrAttributes(groupsNode,
		// jcrDate);

		Element siteAdminNode = new Element("site-administrators");
		siteAdminNode.setAttribute("mixinTypes", "jmix:systemNode", ContentGeneratorCst.NS_JCR);
		siteAdminNode.setAttribute("primaryType", "jnt:group", ContentGeneratorCst.NS_JCR);

		Element jmembers = new Element("members", ContentGeneratorCst.NS_J);
		jmembers.setAttribute("primaryType", "jnt:member", ContentGeneratorCst.NS_JCR);

		Element rootUser = new Element("root");
		rootUser.setAttribute("member", "/users/root", ContentGeneratorCst.NS_J);
		rootUser.setAttribute("primaryType", "jnt:member", ContentGeneratorCst.NS_JNT);
		jmembers.addContent(rootUser);

		siteAdminNode.addContent(jmembers);

		Element sitePrivileged = new Element("site-privileged");
		// sitePrivileged =
		// ContentGeneratorService.getInstance().addJcrAttributes(groupsNode,
		// jcrDate);
		sitePrivileged.setAttribute("mixinTypes", "systemNode", ContentGeneratorCst.NS_JMIX);
		sitePrivileged.setAttribute("primaryType", "jnt:group", ContentGeneratorCst.NS_JNT);

		Element siteAdminGroup = new Element("site-administrators___2");
		// @TODO: get siteKey
		siteAdminGroup.setAttribute("member", "/sites/mySite/groups/site-administrators", ContentGeneratorCst.NS_J);
		siteAdminGroup.setAttribute("primaryType", "jnt:member", ContentGeneratorCst.NS_JCR);
		sitePrivileged.addContent(siteAdminGroup);
		groupsNode.addContent(siteAdminNode);
		// setContent(sitePrivileged);

		for (Iterator<GroupBO> iterator = groups.iterator(); iterator.hasNext();) {
			GroupBO group = iterator.next();
			List<UserBO> users = group.getUsers();

			Element groupNode = group.getJcrXml();
			for (Iterator<UserBO> iterator2 = users.iterator(); iterator2.hasNext();) {
				UserBO userBO = iterator2.next();
				// @todo: format
				groupNode.addContent(userBO.getJcrXml());
			}
			groupsNode.addContent(groupNode);
		}
		return groupsNode;
	}

	public List<GroupBO> generateUsersAndGroups(Integer nbUsers, Integer nbGroups) {
		logger.info(nbUsers + " users and " + nbGroups + " groups are going to be generated");

		List<GroupBO> groups = new ArrayList<GroupBO>();
		String jcrDate = ContentGeneratorService.getInstance().getDateForJcrImport(null);

		// 1 - getNbUsersPerGroups
		Integer nbUsersPerGroup = getNbUsersPerGroup(nbUsers, nbGroups);

		// 2 - getNbusersLastGroup
		Integer nbUsersLastGroup = getNbUsersLastGroup(nbUsers, nbGroups);

		// 3 cptGroup = 1
		int cptGroups = 1;
		while (cptGroups <= nbGroups.intValue()) {
			logger.info("Group " + cptGroups);
			// is this the last group?
			if (cptGroups == nbGroups.intValue()) {
				nbUsersPerGroup = nbUsersLastGroup;
			}

			List<UserBO> users = new ArrayList<UserBO>();
			int cptUsers = 1;
			while (cptUsers <= nbUsersPerGroup.intValue()) {
				logger.info("User " + cptUsers);
				String dateJcr = ContentGeneratorService.getInstance().getDateForJcrImport(null);
				// @TODO: create pathJCR with username
				String pathJcr = "/ab/cd/ef";
				UserBO user = new UserBO("user" + cptUsers, "user" + cptUsers, dateJcr, pathJcr);
				users.add(user);
				cptUsers++;
			}

			GroupBO group = new GroupBO("group" + cptGroups, users, jcrDate);
			group.setUsers(users);
			groups.add(group);
			cptGroups++;
		}
		return groups;
	}

	public String getPathForUsername(String username) {
		/*
		 * StringBuilder builder = new StringBuilder();
		 * 
		 * int userNameHashcode = Math.abs(username.hashCode()); String
		 * firstFolder = getFolderName(userNameHashcode).toLowerCase();
		 * userNameHashcode = Math.round(userNameHashcode/100); String
		 * secondFolder = getFolderName(userNameHashcode).toLowerCase();
		 * userNameHashcode = Math.round(userNameHashcode/100); String
		 * thirdFolder = getFolderName(userNameHashcode).toLowerCase(); return
		 * builder
		 * .append(usersRootNode).append("/").append(firstFolder).append("/"
		 * ).append(secondFolder).append(
		 * "/").append(thirdFolder).append("/").append
		 * (JCRContentUtils.escapeLocalNodeName( username)).toString();
		 */
		return null;
	}

	private String getFolderName(int userNameHashcode) {
		int i = (userNameHashcode % 100);
		return Character.toString((char) ('a' + Math.round(i / 10))) + Character.toString((char) ('a' + (i % 10)));
	}

	public Integer getNbUsersPerGroup(Integer nbUsers, Integer nbGroups) {
		Integer nbUsersPerGroup = nbUsers / nbGroups;
		return nbUsersPerGroup;
	}

	public Integer getNbUsersLastGroup(Integer nbUsers, Integer nbGroups) {
		Integer nbUsersLastGroup = nbUsers % nbGroups;
		if (nbUsersLastGroup.equals(Integer.valueOf(0))) {
			nbUsersLastGroup = getNbUsersPerGroup(nbUsers, nbGroups);
		}
		return nbUsersLastGroup;
	}
}
