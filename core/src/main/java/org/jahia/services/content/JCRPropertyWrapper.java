/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Interface for wrappers for javax.jcr.property to allow more data format.
 *
 * @author : toto
 */
public interface JCRPropertyWrapper extends Property, JCRItemWrapper {
    Iterator<JCRPropertyWrapper> EMPTY = new Iterator<JCRPropertyWrapper>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public JCRPropertyWrapper next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {

        }
    };

    void addValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Node value, boolean weak) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    void addValues(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;


    JCRValueWrapper getValue() throws RepositoryException;

    JCRValueWrapper[] getValues() throws RepositoryException;

    /**
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    JCRValueWrapper getRealValue() throws ValueFormatException, RepositoryException;

    /**
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    JCRValueWrapper[] getRealValues() throws ValueFormatException, RepositoryException;

    JCRNodeWrapper getContextualizedNode() throws ValueFormatException, RepositoryException;

    String getLocale() throws RepositoryException;

    boolean removeValue(Value value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException;

    boolean removeValues(Value[] values) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException;

    /**
     * Gets the real <code>Property</code> wrapped by this <code>JCRPropertyWrapper</code>
     *
     * @return the real JCR <code>Property</code>
     */
    Property getRealProperty();

}
