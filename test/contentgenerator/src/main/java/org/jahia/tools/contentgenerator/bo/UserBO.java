package org.jahia.tools.contentgenerator.bo;

import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Element;


public class UserBO {
	private String name;

	private String password;

	private String jcrPath;

	private String jcrDate;

	private String email;

	public UserBO(String name, String password, String dateJcr, String pathJcr) {
		this.name = name;
		this.password = password;
		// TODO: hard-coded date
		this.jcrDate = "2011-06-20T12:14:03.385-04:00"; // dateJcr;
		this.email = this.name + "@example.com";
		this.jcrPath = pathJcr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPathJcr() {
		return jcrPath;
	}

	public void setPathJcr(String pathJcr) {
		this.jcrPath = pathJcr;
	}

	public String getDateJcr() {
		return jcrDate;
	}

	public void setDateJcr(String dateJcr) {
		this.jcrDate = dateJcr;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Element getJcrXml() {
		Element root = new Element("dj");
		root = ContentGeneratorService.getInstance().addJcrAttributes(root, this.jcrDate);
		root.setAttribute("primaryType", "jnt:usersFolder");

		Element subElement1 = new Element("gi");
		subElement1 = ContentGeneratorService.getInstance().addJcrAttributes(subElement1,this.jcrDate);
		subElement1.setAttribute("primaryType", "jnt:usersFolder");
		root.addContent(subElement1);

		Element subElement2 = new Element("ej");
		subElement2 = ContentGeneratorService.getInstance().addJcrAttributes(subElement2,this.jcrDate);
		subElement2.setAttribute("primaryType", "jnt:usersFolder");
		subElement1.addContent(subElement2);

		Element userElement = new Element(this.name);
		userElement.setAttribute("email", this.email);
		userElement.setAttribute("emailNotificationsDisabled", Boolean.FALSE.toString());
		userElement.setAttribute("accountLocked", Boolean.FALSE.toString(), ContentGeneratorCst.NS_J);
		userElement.setAttribute("checkinDate", this.jcrDate, ContentGeneratorCst.NS_J);
		userElement.setAttribute("email", this.email, ContentGeneratorCst.NS_J);
		userElement.setAttribute("external", Boolean.FALSE.toString(), ContentGeneratorCst.NS_J);
		userElement.setAttribute("firstName", this.name + " firstname", ContentGeneratorCst.NS_J);
		userElement.setAttribute("lastName", this.name + " lastname", ContentGeneratorCst.NS_J);
		userElement.setAttribute("lastPublished", this.jcrDate, ContentGeneratorCst.NS_J);
		userElement.setAttribute("lastPublishedBy", "root", ContentGeneratorCst.NS_J);
		userElement.setAttribute("organization", "Organization", ContentGeneratorCst.NS_J);
		// TODO: hash
		userElement.setAttribute("password", this.password, ContentGeneratorCst.NS_J); // W6ph5Mm5Pz8GgiULbPgzG37mj9g
		// TODO : picture
		userElement.setAttribute("picture", this.jcrPath + "/files/profile/publisher.png", ContentGeneratorCst.NS_J); // 

		userElement.setAttribute("published", Boolean.TRUE.toString(), ContentGeneratorCst.NS_J);
		userElement.setAttribute("created", this.jcrDate, ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("createdBy", "root", ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("lastModified", this.jcrDate, ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("firstName", this.name + " firstname", ContentGeneratorCst.NS_J);

		userElement.setAttribute("lastModified", this.jcrDate, ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("lastModifiedBy", "", ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("mixinTypes", "jmix:accessControlled", ContentGeneratorCst.NS_JCR);
		userElement.setAttribute("primaryType", "jnt:user", ContentGeneratorCst.NS_JCR);
		
		userElement.setAttribute("lastLoginDate", this.jcrDate);

		// userElement.setAttribute("password.history.1242739225417", null);
		userElement.setAttribute("preferredLanguage", "en");

		subElement2.addContent(userElement);
		return root;
	}
}
