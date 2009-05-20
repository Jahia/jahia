/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
// $Id$

package org.jahia.security.ip;

import java.util.NoSuchElementException;


/**
 * This class represents an IP number represented by an 32 bits
 * integer value. Dotted-decimal notation divides the 32-bit Internet address
 * into four 8-bit (byte) fields and specifies the value of each field
 * independently as a decimal number with the fields separated by dots :<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;10010001 . 00001010 . 00100010 . 00000011<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;145&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;10&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;34
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;-> 145.10.34.3<br/>
 * </code>
 *
 * <br/><br/>
 *
 * IP numbers are classified into three classes :<br/>
 * <br/>
 *
 * class A:<br/>
 * <br/><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;1&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;7&nbsp;8&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------+------------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |0&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------+------------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <-- network number -->&nbsp;<------- host number --------><br/>
 * </code>
 *
 * <br/><br/>
 *
 * class B:<br/>
 * <br/><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;2&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15&nbsp;16&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------------+------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |10|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------------+------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <----- network number ----->&nbsp;<---- host number -----><br/>
 * </code>
 *
 * <br/><br/>
 *
 * class C:<br/>
 * <br/><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;3&nbsp;
 * &nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;23&nbsp;24
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +---+-----------------------------+-------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |110|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +---+-----------------------------+-------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <------- network  number -------->&nbsp;<-- host number --><br/>
 * </code>
 *
 * <br/><br/>
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class IPNumber implements Cloneable
{

    /** IP address */
    protected int mIPNumber = 0;


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   ip
     *      String representation of the IP address. The format of the ip's
     *      string representation must follow the decimal-dotted notation
     *      xxx.xxx.xxx.xxx.
     *
     * @exception   InvalidIPNumberException
     *      Throws this exception when the specified string doesn't represent
     *      a valid IP address.
     */
    public IPNumber (String ipNumberStr)
        throws  InvalidIPNumberException
    {
        mIPNumber = parseIPNUmber (ipNumberStr);
    }


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   ip
     *      Binary representation of the IP number.
     */
    public IPNumber (int number)
    {
        mIPNumber = number;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the integer representation of the IP address.
     *
     * @return
     *      The IP address.
     */
    public final int getIPNumber () {
        return mIPNumber;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the string representation of the IP Address following the common
     * decimal-dotted notation xxx.xxx.xxx.xxx.
     *
     * @return
     *      Return the string representation of the IP number.
     */
    public String toString ()
    {
        StringBuffer result = new StringBuffer ();
        int temp;

        temp = mIPNumber & 0x000000FF;
        result.append (temp);
        result.append (".");

        temp = (mIPNumber >> 8) & 0x000000FF;
        result.append (temp);
        result.append (".");

        temp = (mIPNumber >> 16) & 0x000000FF;
        result.append (temp);
        result.append (".");

        temp = (mIPNumber >> 24) & 0x000000FF;
        result.append (temp);

        return result.toString();
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the IP number is belongs to a Class A IP number.
     *
     * @return
     *      Return <code>true</code> if the encapsulated IP number belongs to
     *      a class A IP number, otherwise returne <code>false</code>.
     */
    public final boolean isClassA ()
    {
        return (mIPNumber & 0x00000001) == 0;
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the IP number is belongs to a Class B IP number.
     *
     * @return
     *      Return <code>true</code> if the encapsulated IP number belongs to
     *      a class B IP number, otherwise returne <code>false</code>.
     */
    public final boolean isClassB ()
    {
        return (mIPNumber & 0x00000003) == 1;
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the IP number is belongs to a Class C IP number.
     *
     * @return
     *      Return <code>true</code> if the encapsulated IP number belongs to
     *      a class C IP number, otherwise returne <code>false</code>.
     */
    public final boolean isClassC ()
    {
        return (mIPNumber & 0x00000007) == 3;
    }

    //-------------------------------------------------------------------------
    /**
     * Convert a decimal-dotted notation representation of an IP number into
     * an 32 bits interger value.
     *
     * @param   ipNumberStr
     *      Decimal-dotted notation (xxx.xxx.xxx.xxx) of the IP number.
     *
     * @return
     *      Return the 32 bits integer representation of the IP number.
     *
     * @exception   InvalidIPNumberException
     *      Throws this exception if the specified IP number is not compliant
     *      to the decimal-dotted notation xxx.xxx.xxx.xxx.
     */
    protected int parseIPNUmber (String ipNumberStr)
        throws  InvalidIPNumberException
    {
        int result = 0;

        if (ipNumberStr == null) {
            throw new InvalidIPNumberException ();
        }

        try {
            String tmp = ipNumberStr;

            // get the 3 first numbers
            int offset = 0;
            for (int i=0; i<3; i++) {

                // get the position of the first dot
                int index = tmp.indexOf(".");

                // if there is not a dot then the ip string representation is
                // not compliant to the decimal-dotted notation.
                if (index != -1) {

                    // get the number before the dot and convert it into
                    // an integer.
                    String numberStr = tmp.substring (0, index);
                    int number = Integer.parseInt (numberStr);
                    if ((number < 0) || (number > 255)) {
                        throw new InvalidIPNumberException (ipNumberStr);
                    }

                    result += number << offset;
                    offset += 8;
                    tmp = tmp.substring (index+1);
                } else {
                    throw new InvalidIPNumberException (ipNumberStr);
                }
            }

            // the remaining part of the string should be the last number.
            if (tmp.length() > 0) {
                int number = Integer.parseInt (tmp);
                if ((number < 0) || (number > 255)) {
                    throw new InvalidIPNumberException (ipNumberStr);
                }

                result += number << offset;
                mIPNumber = result;
            } else {
                throw new InvalidIPNumberException (ipNumberStr);
            }
        }
        catch (NoSuchElementException ex) {
            throw new InvalidIPNumberException (ipNumberStr);
        }
        catch (NumberFormatException ex) {
            throw new InvalidIPNumberException (ipNumberStr);
        }

        return result;
    }


    //-------------------------------------------------------------------------
    /**
     * Compare the specified IP number to the encapsulated one.
     *
     * @param   another
     *      The IP number to be compared.
     *
     * @return
     *      Return <code>true</code> if the encapsulated IP number is the same
     *      as the specified one, otherwise return <code>false</code>.
     */
    public boolean equals (Object another) {
        if (this == another) return true;
        
        if (another != null && this.getClass() == another.getClass()) {
            return mIPNumber == ((IPNumber)another).mIPNumber;
        }
        return false;
    }

    //-------------------------------------------------------------------------
    /**
     * Clone the encapsulated IP number.
     *
     * @return
     *      Return a new object representing the encapsulated IP number.
     */
    public Object clone ()
    {
        return new IPNumber (mIPNumber);
    }
}

