/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.blogs.actions;

import org.jahia.blogs.ServletResources;

import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;

import org.jahia.data.fields.JahiaFieldDefinition;

import org.jahia.services.version.EntryLoadRequest;

import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentSmallTextSharedLangField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;

import org.jahia.exceptions.JahiaException;

import org.apache.log4j.Logger;

/**
 * Action used to Add a TrackBack ping to a given post
 * 
 * @author Xavier Lawrence
 */
public class AddTrackBackPingAction extends AbstractAction {

	// log4j logger
	static Logger log = Logger.getLogger(AddTrackBackPingAction.class);

	private String postID;
	private String title;
	private String excerpt;
	private String url;
	private String blogName;

	/** Creates a new instance of AddTrackBackPingAction */
	public AddTrackBackPingAction(String postID, String title, String excerpt,
			String url, String blogName) {
		this.postID = postID;
		this.title = title;
		this.excerpt = excerpt;
		this.url = url;
		this.blogName = blogName;
	}

	/**
	 * Adds a TrackBack ping to the given post
	 */
	public Object execute() throws JahiaException {
		// Create commmon resources
		super.init();
		EntryLoadRequest elr = new EntryLoadRequest(
				EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, jParams
						.getEntryLoadRequest().getLocales());
		this.jParams.setSubstituteEntryLoadRequest(elr);

		// Load the Container and check the structure
		final JahiaContainer postContainer = super.getContainer(Integer
				.parseInt(postID));

		if (postContainer == null) {
			throw new JahiaException("Post: " + postID + " does not exist",
					"Container: " + postID + " does not exist",
					JahiaException.ENTRY_NOT_FOUND,
					JahiaException.WARNING_SEVERITY);
		}

		log.debug("Working on post: " + postContainer.getID());

		super.changePage(postContainer.getPageID());

		JahiaContainerDefinition def = JahiaContainerDefinitionsRegistry
				.getInstance().getDefinition(
						jParams.getSiteID(),
						containerNames
								.getValue(BlogDefinitionNames.BLOG_TB_LIST));

		JahiaContainerList trackBacks = postContainer
				.getContainerList(super.containerNames
						.getValue(BlogDefinitionNames.BLOG_TB_LIST));
		JahiaGroup rootGroup = servicesRegistry.getJahiaGroupManagerService()
				.lookupGroup(jParams.getSiteID(),
						JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);

		JahiaUser user = (JahiaUser) rootGroup.getRecursiveUserMembers()
				.iterator().next();
		jParams.setUser(user);
		int listID, aclID;
		if (trackBacks == null || trackBacks.getID() < 1) {
			log.debug("Storing the first TB...");
			listID = aclID = 0;

		} else {
			listID = trackBacks.getID();
			aclID = trackBacks.getAclID();
		}

		JahiaContainer pingContainer = new JahiaContainer(0, jParams
				.getJahiaID(), jParams.getPageID(), listID, 0, aclID, def
				.getID(), 0, EntryLoadRequest.STAGING_WORKFLOW_STATE);

		// Save the new Container
		containerService.saveContainer(pingContainer, postContainer.getID(),
				jParams);

		log.debug("Working on container: " + pingContainer.getID());

		int pageDefID = 0;
		if (jParams.getContentPage().hasActiveEntries()) {
			pageDefID = jParams.getContentPage().getPageTemplateID(jParams);
		} else {
			pageDefID = jParams.getContentPage().getPageTemplateID(elr);
		}

		int siteId = jParams.getJahiaID();
		int pageId = jParams.getPageID();

		JahiaFieldDefinition tbUrlFieldDefinition = trackBacks.getDefinition()
				.findFieldInStructure(
						containerNames.getValue(BlogDefinitionNames.TB_URL)
                );
		ContentSmallTextSharedLangField.createSmallText(siteId, pageId,
				pingContainer.getID(), tbUrlFieldDefinition.getID(),
				pingContainer.getAclID(), 0, url, jParams);

		JahiaFieldDefinition tbPingIPFieldDefinition = trackBacks
				.getDefinition().findFieldInStructure(
						containerNames.getValue(BlogDefinitionNames.TB_PING_IP)
                );
		ContentSmallTextSharedLangField.createSmallText(siteId, pageId,
				pingContainer.getID(), tbPingIPFieldDefinition.getID(),
				pingContainer.getAclID(), 0, ServletResources
						.getCurrentRequest().getRemoteAddr(), jParams);

		if (title != null && title.length() > 0) {
			JahiaFieldDefinition tbTitleFieldDefinition = trackBacks
					.getDefinition().findFieldInStructure(
							containerNames.getValue(BlogDefinitionNames.TB_TITLE)
                    );
			ContentSmallTextSharedLangField.createSmallText(siteId, pageId,
					pingContainer.getID(), tbTitleFieldDefinition.getID(),
					pingContainer.getAclID(), 0, title, jParams);
		}

		if (excerpt != null && excerpt.length() > 0) {
			JahiaFieldDefinition tbExcerptFieldDefinition = trackBacks
					.getDefinition().findFieldInStructure(
							containerNames.getValue(BlogDefinitionNames.TB_EXCERPT)
                    );
			ContentBigTextField.createBigText(siteId, pageId, pingContainer
					.getID(), tbExcerptFieldDefinition.getID(), pingContainer
					.getAclID(), 0, "<html>" + excerpt + "</html>", jParams);
		}

		if (blogName != null && blogName.length() > 0) {
			JahiaFieldDefinition tbBlogNameFieldDefinition = trackBacks
					.getDefinition().findFieldInStructure(
							containerNames
									.getValue(BlogDefinitionNames.TB_BLOG_NAME)
                    );
			ContentSmallTextSharedLangField.createSmallText(siteId, pageId,
					pingContainer.getID(), tbBlogNameFieldDefinition.getID(),
					pingContainer.getAclID(), 0, blogName, jParams);
		}

		if (listID == 0) {
			super.activateContainerList(pingContainer.getListID(), user,
					pingContainer.getPageID());
		}

		ServicesRegistry.getInstance().getJahiaEventService()
				.fireAggregatedEvents();
		
		super.activateContainer(pingContainer.getID(), user);
		super.activateContainer(postContainer.getID(), user);

		log.debug("Trackback added...");
		return Boolean.TRUE;
	}
}
