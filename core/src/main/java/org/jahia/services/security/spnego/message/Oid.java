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

import java.util.Arrays;

/**
 * @author Martin Algesten
 */
public class Oid extends AbstractMessagePart {

  public static final int[] OID_SPNEGO = new int[] {0x2b, 0x06, 0x01, 0x05, 0x05, 0x02};

  public static final int[] OID_KERBEROS_MICROSOFT = new int[] {0x2a, 0x86, 0x48, 0x82, 0xf7, 0x12, 0x01, 0x02, 0x02};

  public static final int[] OID_KERBEROS = new int[] {0x2a, 0x86, 0x48, 0x86, 0xf7, 0x12, 0x01, 0x02, 0x02};

  private int[] oid = null;

  /**
   * {@inheritDoc} 
   */
  public int getDerType() {
    return TYPE_OID;
  }

  /**
   * {@inheritDoc} 
   */
  public int[] toDer() {
    return wrap(TYPE_OID, oid);
  }

  /**
   * {@inheritDoc} 
   */
  public void parse(ParseState state) {
    state.setPhase("OID");
    state.expect(TYPE_OID, true, "Expected oid identifier");
    int length = state.parseDerLength();
    oid = new int[length];
    arraycopy(state.getToken(), state.getIndex(), oid, 0, length);
    state.setIndex(state.getIndex() + length);
  }

  /**
   * @return the oid
   */
  public int[] getOid() {
    return oid;
  }

  /**
   * @param oid the oid to set
   */
  public void setOid(int[] oid) {
    this.oid = oid;
  }

  public boolean isSpnego() {
    return Arrays.equals(oid, OID_SPNEGO);
  }

  public boolean isKerberosMicrosoft() {
    return Arrays.equals(oid, OID_KERBEROS_MICROSOFT);
  }

  public boolean isKerberos() {
    return Arrays.equals(oid, OID_KERBEROS);
  }

}
