/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.jahia.data.beans.CategoryBean;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Interface for wrappers for javax.jcr.property to allow more data format.
 *
 * @author : toto
 */
public interface JCRPropertyWrapper extends Property, JCRItemWrapper {
    /**
     * @deprecated use getNode instead
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    CategoryBean getCategory() throws ValueFormatException, RepositoryException;

    void addValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValues(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    Value getRealValue() throws ValueFormatException,RepositoryException;

    Value[] getRealValues() throws ValueFormatException,RepositoryException;

    JCRNodeWrapper getContextualizedNode() throws ValueFormatException, RepositoryException;

    String getLocale() throws RepositoryException;

}
