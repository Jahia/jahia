/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.interceptor;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

/**
 * Abstract property interceptor that does not do any value modifications. To be
 * subclassed for particular usage.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseInterceptor implements PropertyInterceptor {

	public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition)
	        throws VersionException, LockException, ConstraintViolationException,
	        RepositoryException {
		// do nothing
	}

	public Value beforeSetValue(JCRNodeWrapper node, String name,
	        ExtendedPropertyDefinition definition, Value originalValue)
	        throws ValueFormatException, VersionException, LockException,
	        ConstraintViolationException, RepositoryException {
		return originalValue;
	}

	public Value[] beforeSetValues(JCRNodeWrapper node, String name,
	        ExtendedPropertyDefinition definition, Value[] originalValues)
	        throws ValueFormatException, VersionException, LockException,
	        ConstraintViolationException, RepositoryException {
		return originalValues;
	}

	public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
	        throws ValueFormatException, RepositoryException {
		return storedValue;
	}

	public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues)
	        throws ValueFormatException, RepositoryException {
		return storedValues;
	}

}
