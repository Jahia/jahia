/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.rules;

import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Interface used by rule facts.
 * The particularity of this Updateable extension is that it can provide a list of new facts after the update, they
 * will be insert in the rule engine as new facts just after the update, even if the update is delayed for some reasons.
 */
public interface UpdateableWithNewFacts extends Updateable {

    /**
     * Do the update operation in the JCR repository
     * @param s the context node
     * @param delayedUpdates list of delayed updates that can be fill by the implementation ( for example when the node is locked ), the update need to be performed later.
     * @param newFacts list of new facts that need to be insert in the rule engine as a result of the update ( so they can trigger other rules )
     * @throws RepositoryException in case of JCR-related errors
     */
    void doUpdate(JCRSessionWrapper s, List<Updateable> delayedUpdates, List<Object> newFacts) throws RepositoryException;
}
