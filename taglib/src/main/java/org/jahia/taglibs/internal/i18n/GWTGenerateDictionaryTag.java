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
package org.jahia.taglibs.internal.i18n;

import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.bin.Jahia;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * Create a resource bundle dictionary
 */
@SuppressWarnings("serial")
public class GWTGenerateDictionaryTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(GWTGenerateDictionaryTag.class);

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        // print output
        try {
            ServletRequest request = pageContext.getRequest();
            Locale currentLocale = request.getLocale();
            RenderContext renderContext = (RenderContext) pageContext.findAttribute("renderContext");
            if (renderContext != null) {
                addMandatoryGwtMessages(renderContext.getUILocale(), currentLocale);
            } else {
                // we fall back to JahiaData for the administration interface, where this tag is also used.
                JahiaData jahiaData = (JahiaData) pageContext.findAttribute("org.jahia.data.JahiaData");
                if (jahiaData != null) {
                    addMandatoryGwtMessages(jahiaData.getProcessingContext().getUILocale(), currentLocale);
                } else {
                    addMandatoryGwtMessages(null, currentLocale);
                }
            }
            out.append("<script type='text/javascript'>\n");
            out.append(generateJahiaGwtDictionary());
            out.append("</script>\n");
        } catch (IOException e) {
            logger.error(e, e);
        }
        return EVAL_PAGE;
    }

    /**
     * Add mandatory messages
     *
     * @param uiLocale      current UI locale
     * @param currentLocale
     */
    private void addMandatoryGwtMessages(Locale uiLocale, Locale currentLocale) {
        addGwtDictionaryMessage("workInProgressTitle", getJahiaInternalResource("org.jahia.admin.workInProgressTitle", uiLocale, currentLocale));
        addGwtDictionaryMessage("workInProgressProgressText", getJahiaInternalResource("org.jahia.admin.workInProgressProgressText", uiLocale, currentLocale));
        addGwtDictionaryMessage("fm_copyright", Jahia.COPYRIGHT_TXT + " " + Jahia.VERSION + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber());
        addGwtDictionaryMessage("fm_copyright", Jahia.COPYRIGHT_TXT + " " + Jahia.VERSION + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber());
        addGwtDictionaryMessage("fm_newdir", getJahiaInternalResourceValue("label.createFolder"));
        addGwtDictionaryMessage("fm_newdirname", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.newDirName.label"));
        addGwtDictionaryMessage("fm_newcontent", getJahiaInternalResourceValue("label.newContent"));
        addGwtDictionaryMessage("fm_newpage", getJahiaInternalResourceValue("label.newPage"));
        addGwtDictionaryMessage("fm_newmashup", getJahiaInternalResourceValue("label.newMashup"));
        addGwtDictionaryMessage("fm_newrssmashup", getJahiaInternalResourceValue("label.newRssMashup"));
        addGwtDictionaryMessage("fm_newgadgetmashup", getJahiaInternalResourceValue("label.newGoogleGadgetMashup"));
        addGwtDictionaryMessage("fm_copy", getJahiaInternalResourceValue("label.copy"));
        addGwtDictionaryMessage("fm_info_name", getJahiaInternalResourceValue("label.name"));
        addGwtDictionaryMessage("fm_workflow_status", getJahiaInternalResourceValue("label.status"));
        addGwtDictionaryMessage("fm_cut", getJahiaInternalResourceValue("label.cut"));
        addGwtDictionaryMessage("fm_paste", getJahiaInternalResourceValue("label.paste"));
        addGwtDictionaryMessage("fm_pasteref", getJahiaInternalResourceValue("label.pasteReference"));
        addGwtDictionaryMessage("fm_lock", getJahiaInternalResourceValue("label.lock"));
        addGwtDictionaryMessage("fm_unlock", getJahiaInternalResourceValue("label.unlock"));
        addGwtDictionaryMessage("fm_remove", getJahiaInternalResourceValue("label.remove"));
        addGwtDictionaryMessage("fm_rename", getJahiaInternalResourceValue("label.rename"));
        addGwtDictionaryMessage("fm_zip", getJahiaInternalResourceValue("label.zip"));
        addGwtDictionaryMessage("fm_unzip", getJahiaInternalResourceValue("label.unzip"));
        addGwtDictionaryMessage("fm_download", getJahiaInternalResourceValue("label.download"));
        addGwtDictionaryMessage("fm_preview", getJahiaInternalResourceValue("label.preview"));
        addGwtDictionaryMessage("fm_downloadMessage", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label"));
        addGwtDictionaryMessage("fm_upload", getJahiaInternalResourceValue("label.upload"));
        addGwtDictionaryMessage("fm_webfolder", getJahiaInternalResourceValue("label.openIEFolder"));
        addGwtDictionaryMessage("fm_webfolderMessage", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.webFolderMessage.label"));
        addGwtDictionaryMessage("fm_thumbs", getJahiaInternalResourceValue("label.thumbs"));
        addGwtDictionaryMessage("fm_icons_detailed", getJahiaInternalResourceValue("label.icons.detailed"));
        addGwtDictionaryMessage("fm_list", getJahiaInternalResourceValue("label.list"));
        addGwtDictionaryMessage("fm_crop", getJahiaInternalResourceValue("label.crop"));
        addGwtDictionaryMessage("fm_resize", getJahiaInternalResourceValue("label.resize"));
        addGwtDictionaryMessage("fm_rotate", getJahiaInternalResourceValue("label.rotate"));
        addGwtDictionaryMessage("fm_rotateLeft", getJahiaInternalResourceValue("label.rotateLeft"));
        addGwtDictionaryMessage("fm_rotateRight", getJahiaInternalResourceValue("label.rotateRight"));
        addGwtDictionaryMessage("fm_refresh", getJahiaInternalResourceValue("label.refresh"));
        addGwtDictionaryMessage("fm_newcategory", getJahiaInternalResourceValue("label.newCategory"));
        addGwtDictionaryMessage("fm_copying", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.copying.label"));
        addGwtDictionaryMessage("fm_cutting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.cutting.label"));
        addGwtDictionaryMessage("fm_downloading", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.downloading.label"));
        addGwtDictionaryMessage("fm_locking", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.locking.label"));
        addGwtDictionaryMessage("fm_mounting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.mounting.label"));
        addGwtDictionaryMessage("fm_newfoldering", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.newfoldering.label"));
        addGwtDictionaryMessage("fm_pasting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pasting.label"));
        addGwtDictionaryMessage("fm_pastingref", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pastingref.label"));
        addGwtDictionaryMessage("fm_removing", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.removing.label"));
        addGwtDictionaryMessage("fm_renaming", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.renaming.label"));
        addGwtDictionaryMessage("fm_unlocking", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unlocking.label"));
        addGwtDictionaryMessage("fm_unmounting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unmounting.label"));
        addGwtDictionaryMessage("fm_unzipping", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unzipping.label"));
        addGwtDictionaryMessage("fm_webfoldering", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.webfoldering.label"));
        addGwtDictionaryMessage("fm_zipping", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.zipping.label"));
        addGwtDictionaryMessage("fm_confArchiveName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.archiveName.label"));
        addGwtDictionaryMessage("fm_confMultiRemove", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.multiRemove.label"));
        addGwtDictionaryMessage("fm_confNewName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.newName.label"));
        addGwtDictionaryMessage("fm_confOverwrite", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.overwrite.label"));
        addGwtDictionaryMessage("fm_confRemove", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.remove.label"));
        addGwtDictionaryMessage("fm_confUnlock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unlock.label"));
        addGwtDictionaryMessage("fm_confUnmount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unmount.label"));
        addGwtDictionaryMessage("fm_failCopy", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.copy.label"));
        addGwtDictionaryMessage("fm_failCrop", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.crop.label"));
        addGwtDictionaryMessage("fm_failCut", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.cut.label"));
        addGwtDictionaryMessage("fm_failDelete", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.delete.label"));
        addGwtDictionaryMessage("fm_failDownload", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.download.label"));
        addGwtDictionaryMessage("fm_failMount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.mount.label"));
        addGwtDictionaryMessage("fm_failNewdir", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.newDir.label"));
        addGwtDictionaryMessage("fm_failPaste", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.paste.label"));
        addGwtDictionaryMessage("fm_failPasteref", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.pasteref.label"));
        addGwtDictionaryMessage("fm_failRename", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.rename.label"));
        addGwtDictionaryMessage("fm_failResize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.resize.label"));
        addGwtDictionaryMessage("fm_failRotate", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.rotate.label"));
        addGwtDictionaryMessage("fm_failSaveSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.saveSearch.label"));
        addGwtDictionaryMessage("fm_failUnlock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unlock.label"));
        addGwtDictionaryMessage("fm_failUnmount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmount.label"));
        addGwtDictionaryMessage("fm_failUnmountLock1", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock1.label"));
        addGwtDictionaryMessage("fm_failUnmountLock2", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock2.label"));
        addGwtDictionaryMessage("fm_failUnzip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unzip.label"));
        addGwtDictionaryMessage("fm_failWebfolder", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.webfolder.label"));
        addGwtDictionaryMessage("fm_failZip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.zip.label"));
        addGwtDictionaryMessage("fm_warningLock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.warning.lock.label"));
        addGwtDictionaryMessage("fm_warningSystemLock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.warning.systemLock.label"));
        addGwtDictionaryMessage("fm_selection", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.selection.label"));
        addGwtDictionaryMessage("fm_deselect", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.deselect.label"));
        addGwtDictionaryMessage("fm_width", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.width.label"));
        addGwtDictionaryMessage("fm_height", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.height.label"));
        addGwtDictionaryMessage("fm_ratio", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.ratio.label"));
        addGwtDictionaryMessage("fm_newname", getJahiaInternalResourceValue("label.rename"));
        addGwtDictionaryMessage("fm_mount", getJahiaInternalResourceValue("label.mount"));
        addGwtDictionaryMessage("fm_unmount", getJahiaInternalResourceValue("label.unmount"));
        addGwtDictionaryMessage("fm_mountpoint", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label"));
        addGwtDictionaryMessage("fm_mountDisclaimerLabel", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer.label"));
        addGwtDictionaryMessage("fm_mountDisclaimer", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer"));
        addGwtDictionaryMessage("fm_serveraddress", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.serveraddress.label"));
        addGwtDictionaryMessage("fm_fileMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.fileMenu.label"));
        addGwtDictionaryMessage("fm_editMenu", getJahiaInternalResourceValue("label.edit"));
        addGwtDictionaryMessage("fm_remoteMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.remoteMenu.label"));
        addGwtDictionaryMessage("fm_imageMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.imageMenu.label"));
        addGwtDictionaryMessage("fm_viewMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.viewMenu.label"));
        addGwtDictionaryMessage("fm_browse", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.browse.label"));
        addGwtDictionaryMessage("fm_saveSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.saveSearch.label"));
        addGwtDictionaryMessage("fm_saveSearchName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.saveSearchName.label"));
        addGwtDictionaryMessage("fm_search", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.search.label"));
        addGwtDictionaryMessage("fm_filters", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.filters.label"));
        addGwtDictionaryMessage("fm_mimes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mimes.label"));
        addGwtDictionaryMessage("fm_nodes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.nodes.label"));
        addGwtDictionaryMessage("fm_information", getJahiaInternalResourceValue("label.information"));
        addGwtDictionaryMessage("fm_properties", getJahiaInternalResourceValue("label.properties"));
        addGwtDictionaryMessage("fm_portlets", getJahiaInternalResourceValue("label.portletList"));
        addGwtDictionaryMessage("fm_roles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.roles.label"));
        addGwtDictionaryMessage("fm_modes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.modes.label"));
        addGwtDictionaryMessage("fm_authorizations", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label"));
        addGwtDictionaryMessage("fm_alreadyExists", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.alreadyExists.label"));
        addGwtDictionaryMessage("fm_usages", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.label"));
        addGwtDictionaryMessage("fm_info_name", getJahiaInternalResourceValue("label.in"));
        addGwtDictionaryMessage("fm_info_path", getJahiaInternalResourceValue("label.path"));
        addGwtDictionaryMessage("fm_info_size", getJahiaInternalResourceValue("label.size"));
        addGwtDictionaryMessage("fm_info_lastModif", getJahiaInternalResourceValue("label.lastModif"));
        addGwtDictionaryMessage("fm_info_lock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label"));
        addGwtDictionaryMessage("fm_info_nbFiles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label"));
        addGwtDictionaryMessage("fm_info_nbFolders", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label"));
        addGwtDictionaryMessage("fm_info_totalSize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label"));
        addGwtDictionaryMessage("fm_save", getJahiaInternalResourceValue("label.save"));
        addGwtDictionaryMessage("fm_saveAndNew", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.properties.saveAndNew.label"));
        addGwtDictionaryMessage("fm_restore", getJahiaInternalResourceValue("label.restore"));
        addGwtDictionaryMessage("fm_page", getJahiaInternalResourceValue("label.page"));
        addGwtDictionaryMessage("fm_language", getJahiaInternalResourceValue("label.language"));
        addGwtDictionaryMessage("fm_workflow", getJahiaInternalResourceValue("label.workflowState"));
        addGwtDictionaryMessage("fm_versioned", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label"));
        addGwtDictionaryMessage("fm_live", getJahiaInternalResourceValue("label.live"));
        addGwtDictionaryMessage("fm_staging", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label"));
        addGwtDictionaryMessage("fm_notify", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label"));
        addGwtDictionaryMessage("fm_uploadFiles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.uploadFile.label"));
        addGwtDictionaryMessage("fm_autoUnzip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.autoUnzip.label"));
        addGwtDictionaryMessage("fm_addFile", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.addFile.label"));
        addGwtDictionaryMessage("fm_cancel", getJahiaInternalResourceValue("label.cancel"));
        addGwtDictionaryMessage("fm_ok", getJahiaInternalResourceValue("label.ok"));
        addGwtDictionaryMessage("fm_checkUploads", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.checkUploads.label"));
        addGwtDictionaryMessage("fm_thumbFilter", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"));
        addGwtDictionaryMessage("fm_thumbSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"));
        addGwtDictionaryMessage("fm_thumbSortName", getJahiaInternalResourceValue("label.in"));
        addGwtDictionaryMessage("fm_thumbSortSize", getJahiaInternalResourceValue("label.size"));
        addGwtDictionaryMessage("fm_thumbSortLastModif", getJahiaInternalResourceValue("label.lastModif"));
        addGwtDictionaryMessage("fm_invertSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.invertSort.label"));
        addGwtDictionaryMessage("fm_column_type", getJahiaInternalResourceValue("label.type"));
        addGwtDictionaryMessage("fm_column_locked", getJahiaInternalResourceValue("label.lock"));
        addGwtDictionaryMessage("fm_column_name", getJahiaInternalResourceValue("label.name"));
        addGwtDictionaryMessage("fm_column_path", getJahiaInternalResourceValue("label.path"));
        addGwtDictionaryMessage("fm_column_size", getJahiaInternalResourceValue("label.size"));
        addGwtDictionaryMessage("fm_column_date", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.date.label"));
        addGwtDictionaryMessage("fm_column_provider", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.provider.label"));
        addGwtDictionaryMessage("fm_repository_savedSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"));
        addGwtDictionaryMessage("fm_repository_portletDefinitionRepository", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("fm_select_portlet", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.description.label"));
        addGwtDictionaryMessage("fm_repository_myRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myRepository.label"));
        addGwtDictionaryMessage("fm_repository_usersRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.usersRepository.label"));
        addGwtDictionaryMessage("fm_repository_myExternalRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternalRepository.label"));
        addGwtDictionaryMessage("fm_repository_sharedRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedRepository.label"));
        addGwtDictionaryMessage("fm_repository_websiteRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteRepository.label"));
        addGwtDictionaryMessage("fm_repository_myMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_sharedMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_websiteMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_siteRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.siteRepository.label"));
        addGwtDictionaryMessage("fm_repository_globalRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"));
        addGwtDictionaryMessage("fm_repository_categoryRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.categoryRepository.label"));
        addGwtDictionaryMessage("fm_repository_tagRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.tagRepository.label"));
        addGwtDictionaryMessage("fm_portlet_ready", getJahiaInternalResourceValue("message.portletReady"));
        addGwtDictionaryMessage("fm_portlet_deploy", getJahiaInternalResourceValue("label.deployNewPortlet"));
        addGwtDictionaryMessage("fm_portlet_preparewar", getJahiaInternalResourceValue("label.portletPrepareWar"));
        addGwtDictionaryMessage("fm_login", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.login.label"));
        addGwtDictionaryMessage("fm_logout", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.logout.label"));
        addGwtDictionaryMessage("fm_username", getJahiaInternalResourceValue("label.username"));
        addGwtDictionaryMessage("fm_password", getJahiaInternalResourceValue("label.password"));
        addGwtDictionaryMessage("fm_import", getJahiaInternalResourceValue("label.import"));
        addGwtDictionaryMessage("fm_importfile", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.importfile.label"));
        addGwtDictionaryMessage("fm_export", getJahiaInternalResourceValue("label.export"));
        addGwtDictionaryMessage("fm_exportlink", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.exportlink.label"));
        addGwtDictionaryMessage("ae_principal", getJahiaInternalResourceValue("label.user"));
        addGwtDictionaryMessage("ae_restore_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.restoreInheritance.label"));
        addGwtDictionaryMessage("ae_inherited_from", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.inheritedFrom.label"));
        addGwtDictionaryMessage("ae_inherited", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.inherited.label"));
        addGwtDictionaryMessage("ae_restore_all_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"));
        addGwtDictionaryMessage("ae_break_all_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.breakAllInheritance.label"));
        addGwtDictionaryMessage("ae_remove", getJahiaInternalResourceValue("label.remove"));
        addGwtDictionaryMessage("ae_save", getJahiaInternalResourceValue("label.save"));
        addGwtDictionaryMessage("ae_restore", getJahiaInternalResourceValue("label.restore"));
        addGwtDictionaryMessage("um_adduser", getJahiaInternalResourceValue("org.jahia.engines.users.SelectUG_Engine.newUsers.label"));
        addGwtDictionaryMessage("um_addgroup", getJahiaInternalResourceValue("org.jahia.engines.users.SelectUG_Engine.newGroups.label"));
        addGwtDictionaryMessage("mw_mashups", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("mw_select_portlet_def", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("mw_ok", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.ok.label"));
        addGwtDictionaryMessage("mw_params", getJahiaInternalResourceValue("label.parameters"));
        addGwtDictionaryMessage("mw_edit_params", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.edit.label"));
        addGwtDictionaryMessage("mw_prop_load_error", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.props.load.error.label"));
        addGwtDictionaryMessage("mw_modes_permissions", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.label"));
        addGwtDictionaryMessage("mw_modes_permissions_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.description.label"));
        addGwtDictionaryMessage("mw_modes_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.adduser.label"));
        addGwtDictionaryMessage("mw_modes_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.addgroup.label"));
        addGwtDictionaryMessage("mw_roles_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.adduser.label"));
        addGwtDictionaryMessage("mw_roles_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.addgroup.label"));
        addGwtDictionaryMessage("mw_roles_perm", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.label"));
        addGwtDictionaryMessage("mw_roles_perm_desc", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"));
        addGwtDictionaryMessage("mw_finish", getJahiaInternalResourceValue("label.finish"));
        addGwtDictionaryMessage("mw_save_as", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
        addGwtDictionaryMessage("mw_name", getJahiaInternalResourceValue("label.portletName"));
        addGwtDictionaryMessage("mw_description", getJahiaInternalResourceValue("label.portletDescription"));
        addGwtDictionaryMessage("mw_finish_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
        addGwtDictionaryMessage("mw_no_role", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.any"));
        addGwtDictionaryMessage("wizard_button_cancel", getJahiaInternalResourceValue("label.cancel"));
        addGwtDictionaryMessage("wizard_button_finish", getJahiaInternalResourceValue("label.finish"));
        addGwtDictionaryMessage("wizard_button_next", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.next"));
        addGwtDictionaryMessage("wizard_button_prev", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.prev"));
        addGwtDictionaryMessage("wizard_steps_of", getJahiaInternalResourceValue("label.of"));
        addGwtDictionaryMessage("wizard_steps_current", getJahiaInternalResourceValue("label.step"));
        addGwtDictionaryMessage("wizard_header_title", getJahiaInternalResourceValue("org.jahia.engines.wizard.title"));
        addGwtDictionaryMessage("add_content_wizard_column_label", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.column.label"));
        addGwtDictionaryMessage("add_content_wizard_column_name", getJahiaInternalResourceValue("label.user"));
        addGwtDictionaryMessage("add_content_wizard_card_defs_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.defsCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_defs_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.defsCard.title"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_props", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.error.props"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_title", getJahiaInternalResourceValue("label.error"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_save", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.error.save"));
        addGwtDictionaryMessage("add_content_wizard_card_form_success_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.success"));
        addGwtDictionaryMessage("add_content_wizard_card_form_success_save", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.success.save"));
        addGwtDictionaryMessage("add_content_wizard_card_form_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_form_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.title"));
        addGwtDictionaryMessage("add_content_wizard_card_name_node_name", getJahiaInternalResourceValue("label.user"));
        addGwtDictionaryMessage("add_content_wizard_card_name_node_type", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeType"));
        addGwtDictionaryMessage("add_content_wizard_card_name_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_name_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.title"));
        addGwtDictionaryMessage("add_content_wizard_title", getJahiaInternalResourceValue("label.addContent"));
        addGwtDictionaryMessage("add_content_wizard_title", getJahiaInternalResourceValue("label.addContent"));
        addGwtDictionaryMessage("em_repository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"));
        addGwtDictionaryMessage("em_drag", getJahiaInternalResourceValue("org.jahia.jcr.edit.drag.label"));
        addGwtDictionaryMessage("em_contentlist", getJahiaInternalResourceValue("label.content"));
        addGwtDictionaryMessage("em_savetemplate", getJahiaInternalResourceValue("org.jahia.jcr.edit.savetemplate.label"));
        addGwtDictionaryMessage("em_content", getJahiaInternalResourceValue("label.content"));
        addGwtDictionaryMessage("em_area", getJahiaInternalResourceValue("org.jahia.jcr.edit.area.label"));
        addGwtDictionaryMessage("publication_currentStatus", getJahiaInternalResourceValue("org.jahia.jcr.publication.currentStatus"));
        addGwtDictionaryMessage("publication_path", getJahiaInternalResourceValue("label.path"));
        addGwtDictionaryMessage("publication_publicationAllowed", getJahiaInternalResourceValue("org.jahia.jcr.publication.publicationAllowed"));
        addGwtDictionaryMessage("publication_publicationComments", getJahiaInternalResourceValue("org.jahia.jcr.publication.publicationComments"));
        addGwtDictionaryMessage("publication_publish", getJahiaInternalResourceValue("label.publish"));
        addGwtDictionaryMessage("publication_status_modified", getJahiaInternalResourceValue("label.modified"));
        addGwtDictionaryMessage("publication_status_notyetpublished", getJahiaInternalResourceValue("org.jahia.jcr.publication.status_notyetpublished"));
        addGwtDictionaryMessage("publication_status_published", getJahiaInternalResourceValue("label.published"));
        addGwtDictionaryMessage("publication_unpublished_text", getJahiaInternalResourceValue("label.content.unpublished"));
        addGwtDictionaryMessage("publication_published_text", getJahiaInternalResourceValue("message.content.published"));
        addGwtDictionaryMessage("publication_unpublished_title", getJahiaInternalResourceValue("label.content.unpublished"));
        addGwtDictionaryMessage("publication_published_title", getJahiaInternalResourceValue("message.content.published"));
        addGwtDictionaryMessage("ece_content", getJahiaInternalResourceValue("label.content"));
        addGwtDictionaryMessage("ece_layout", getJahiaInternalResourceValue("org.jahia.jcr.edit.layout.tab"));
        addGwtDictionaryMessage("ece_metadata", getJahiaInternalResourceValue("label.metadata"));
        addGwtDictionaryMessage("ece_classification", getJahiaInternalResourceValue("org.jahia.jcr.edit.classification.tab"));
        addGwtDictionaryMessage("ece_options", getJahiaInternalResourceValue("label.options"));
        addGwtDictionaryMessage("ece_rights", getJahiaInternalResourceValue("label.rights"));
        addGwtDictionaryMessage("ece_categories", getJahiaInternalResourceValue("org.jahia.jcr.edit.categories.tab"));
        addGwtDictionaryMessage("ece_tags", getJahiaInternalResourceValue("org.jahia.jcr.edit.tags.tab"));
        addGwtDictionaryMessage("ece_publication", getJahiaInternalResourceValue("label.publication"));
    }

    /**
     * Get admin message
     *
     * @param resourceName
     * @param uiLocale      current UI locale
     * @param currentLocale
     * @return
     */
    private String getJahiaInternalResource(String resourceName, Locale uiLocale, Locale currentLocale) {
        if (uiLocale != null) {
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, uiLocale);
        } else {
            // for any reason the jData wasn't loaded correctly
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, currentLocale);
        }
    }

}