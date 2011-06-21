package org.jahia.tools.contentgenerator.bo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Element;

public class GroupBO {
	private String name;

	private List<UserBO> users;

	private String dateJcr;

	public GroupBO(String name, List<UserBO> users, String dateJcr) {
		this.name = name;
		this.users = users;
		this.dateJcr = dateJcr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNbUsers() {
		return Integer.valueOf(users.size());
	}

	public List<UserBO> getUsers() {
		return users;
	}

	public void setUsers(List<UserBO> users) {
		this.users = users;
	}

	/**
	 * returns user names contained in this group
	 * 
	 * @return user names
	 */
	public List<String> getUserNames() {
		List<String> userNames = new ArrayList<String>();
		for (Iterator<UserBO> iterator = users.iterator(); iterator.hasNext();) {
			UserBO user = iterator.next();
			userNames.add(user.getName());
		}
		return userNames;
	}

	public Element getJcrXml() {

		Element groupNode = new Element(this.name);
		groupNode.setAttribute("primaryType", "jnt:group", ContentGeneratorCst.NS_JCR);

		Element users = new Element("members", ContentGeneratorCst.NS_J);
		users.setAttribute("primaryType", "jnt:members", ContentGeneratorCst.NS_JCR);

		for (Iterator<UserBO> iterator = this.users.iterator(); iterator.hasNext();) {
			UserBO user = iterator.next();
			Element userNode = new Element(user.getName());
			userNode.setAttribute("member", user.getPathJcr(), ContentGeneratorCst.NS_J);
			userNode.setAttribute("primaryType", "jnt:member", ContentGeneratorCst.NS_JCR);
			groupNode.addContent(userNode);
		}
		return groupNode;
	}
}
