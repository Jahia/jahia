/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//

package org.jahia.services.htmleditors;

import org.jahia.registries.ServicesRegistry;
import java.io.Serializable;

/**
 * <p>Title: HtmlEditor Interface</p>
 * <p>Description: Interface for Html Editor</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia S.A.R.L</p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class JahiaHtmlEditor implements Serializable, HtmlEditor, Comparable {

    private static final org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger (JahiaHtmlEditor.class);

    private String id;
    private String displayName;
    private String includeFile;
    private String compatibilityTesterClass;
    private String baseDirectory;
    private boolean enableCSS = false;
    private int rank;

    public JahiaHtmlEditor( String id,
                            String displayName,
                            String baseDirectory,
                            String includeFile,
                            String compatibilityTesterClass,
                            boolean enableCSS,
                            int rank ){
        this.id = id;
        this.displayName = displayName;
        this.baseDirectory = baseDirectory;
        this.includeFile = includeFile;
        this.compatibilityTesterClass = compatibilityTesterClass;
        this.enableCSS = enableCSS;
        this.rank = rank;
    }

    /**
     * Returns the unique identifier.
     *
     * @return the unique identifier
     */
    public String getId(){
        return this.id;
    }

    /**
     * Returns the visual Display Name
     *
     * @return the visual Display name
     */
    public String getDisplayName(){
        return this.displayName;
    }

    /**
     * Returns the base directory relative to
     *
     * <jahia-home>/htmleditors
     *
     * @return
     */
    public String getBaseDirectory(){
        return this.baseDirectory;
    }

    /**
     * Returns the path to the file containing the HTML Editor.
     * This file will be included in Content Editor View ( Jahia Update Engine ).
     * The path must be relative to Jahia's HtmlEditor root path:
     *
     * <jahia-home>/htmleditors
     *
     * @return
     */
    public String getIncludeFile(){
        return this.includeFile;
    }

    /**
     * Returns true if this Editor can run given Client Capabilities.
     *
     * @param clientCapabilities
     * @return true if this Editor can run for the given ClientCapabilities
     *
     * @todo implement the check, actually return true.
     */
    public boolean isClientCapable(ClientCapabilities clientCapabilities){
        if (compatibilityTesterClass != null) {
            try {
                Class compatibilityKlass = Class.forName(
                    compatibilityTesterClass);
                Object objInstance = compatibilityKlass.newInstance();
                if (!(objInstance instanceof EditorCompatibilityTesteable)) {
                    logger.error("The class " + compatibilityTesterClass + " is not an HTML editor compatibility class !");
                    return false;
                }
                EditorCompatibilityTesteable compatibilityTester = (EditorCompatibilityTesteable) objInstance;
                return compatibilityTester.isCompatible(clientCapabilities);
            } catch (ClassNotFoundException cnfe) {
                logger.error("Error while testing HTML editor compatibility with current user's browser platform", cnfe);
            } catch (InstantiationException ie) {
                logger.error("Error while creating instance of HTML editor compatibility tester", ie);
            } catch (IllegalAccessException iae) {
                logger.error("Error while creating instance of HTML editor compatibility tester", iae);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns true if this Editor is authorized for a given site.
     *
     * @param siteID
     * @return true if the given site has autorization to use this Editor
     */
    public boolean isSiteAuthorized(int siteID){
        boolean res = ServicesRegistry.getInstance()
                       .getHtmlEditorsService()
                       .isSiteAutorized(siteID,this.getId());
        return res;
    }

    /**
     * Returns true if this Editor support CSS.
     *
     * @return true if CSS selection should be displayed for this CSS
     */
    public boolean enableCSS(){
        return this.enableCSS;
    }

    /**
     * @return an int representing the ranking specified in the HTML editor's
     * configuration file.
     */
    public int getRank() {
        return this.rank;
    }

    public int compareTo(Object o) throws ClassCastException {
        // first criteria for sorting is ranking (if available), then display
        // name
        JahiaHtmlEditor right = (JahiaHtmlEditor) o;
        if ((getRank() != -1) && (right.getRank() != -1)) {
            return new Integer(getRank()).compareTo(new Integer(right.getRank()));
        }
        return getDisplayName().compareTo(right.getDisplayName());
    }

}