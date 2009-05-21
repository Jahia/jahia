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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;

/**
 * Query Object Model QOMNode interpreter interface
 *
 * User: hollis
 * Date: 9 nov. 2007
 * Time: 12:09:58
 * To change this template use File | Settings | File Templates.
 */
public interface QueryObjectModelInterpreter {

    public void accept(AndImpl node) throws JahiaException;

    public void accept(ComparisonImpl node) throws JahiaException;

    public void accept(FullTextSearchImpl node) throws JahiaException;

    public void accept(NotImpl node) throws JahiaException;

    public void accept(OrImpl node) throws JahiaException;

    public void accept(ChildNodeImpl node) throws JahiaException;

    public void accept(DescendantNodeImpl node) throws JahiaException;

    public void accept(BindVariableValueImpl node) throws JahiaException;

    public void accept(ColumnImpl node) throws JahiaException;

    public void accept(LiteralImpl node) throws JahiaException;

    public void accept(PropertyValueImpl node) throws JahiaException;

    public void accept(OrderingImpl node) throws JahiaException;

    public void accept(SelectorImpl node) throws JahiaException;

    public void accept(QueryObjectModelImpl queryObjectModel) throws JahiaException;

}
