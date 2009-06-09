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

/**
 * @author Martin Algesten
 */
public abstract class AbstractMessagePart implements MessagePart {

  /**
   * Calculates the ASN.1 DER represenation of the length given.
   * @param length the length.
   * @return if length &lt; 0x80, the length straight off. If more than 0x80,
   *         the first byte is 0x80 | [number of bytes required to represent
   *         length] and then bytes high byte first.
   */
  protected int[] calculateDerLength(int length) {

    int[] tmp = new int[16]; // arbitrary buffer length;
    int index = tmp.length; // start from back.

    while (length != 0) {
      tmp[--index] = length & 0xff;
      length = length >> 8;
    }

    tmp[--index] = 0x80 | (tmp.length - index);
    int[] result = new int[tmp.length - index];
    System.arraycopy(tmp, index, result, 0, result.length);

    return result;

  }

  /**
   * Creates a der part of a message with the type first, then der length and
   * then the actual data.
   * @param derType the byte identifier of the data type.
   * @param wrappedData the data.
   * @return [derType][derLength][data]
   */
  protected int[] wrap(int derType, int[] wrappedData) {

    int[] derLength = calculateDerLength(wrappedData.length);
    int[] tmp = new int[1 + derLength.length + wrappedData.length];
    tmp[0] = derType;
    System.arraycopy(derLength, 0, tmp, 1, derLength.length);
    System.arraycopy(wrappedData, 0, tmp, derLength.length + 1,
        wrappedData.length);
    return tmp;
  }

  /**
   * @param src the source bytes.
   * @param srcPos the start position in source.
   * @param dest the destination.
   * @param destPos the start position in destination.
   * @param length how many bytes must be copy.
   */
  public static void arraycopy(byte[] src, int srcPos, int[] dest, int destPos, int length) {
    for (int i = 0; i < length; i++) {
      dest[destPos + i] = 0xff & src[srcPos + i];
    }
  }

  /**
   * @param src the source bytes.
   * @param srcPos the start position in source.
   * @param dest the destination.
   * @param destPos the start position in destination.
   * @param length how many bytes must be copy.
   */
  public static void arraycopy(int[] src, int srcPos, byte[] dest, int destPos, int length) {
    for (int i = 0; i < length; i++) {
      dest[destPos + i] = (byte) src[srcPos + i];
    }
  }

}
