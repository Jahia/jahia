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
/* Copyright 2006 Taglab Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.jahia.services.security.spnego.message;

import java.util.LinkedList;

/**
 * @author Martin Algesten
 */
public class MechTypeList extends AbstractSequence<Oid> {

  private LinkedList<Oid> mechs = new LinkedList<Oid>();
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected LinkedList<Oid> getParts() {
    return mechs;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected Oid createInstance(ParseState state) {
    return new Oid();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void setParts(LinkedList<Oid> parts) {
    this.mechs = parts;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void parse(ParseState state) {
    state.setPhase("MECH_TYPE_LIST");
    super.parse(state);
  }
  
  /**
   * @return the mechs .
   */
  public LinkedList<Oid> getMechs() {
    return mechs;
  }
  
  /**
   * @param mechs the mechs to set .
   */
  public void setMechs(LinkedList<Oid> mechs) {
    this.mechs = mechs;
  }
  
}
