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


/**
 * This class represents an IP Range, which are represented by an IP address and
 * and a subnet mask. The standards describing modern routing protocols often
 * refer to the extended-network-prefix-length rather than the subnet mask. The
 * prefix length is equal to the number of contiguous one-bits in the
 * traditional subnet mask. This means that specifying the network address
 * 130.5.5.25 with a subnet mask of 255.255.255.0 can also be expressed as
 * 130.5.5.25/24. The /<prefix-length> notation is more compact and easier to
 * understand than writing out the mask in its traditional dotted-decimal
 * format.<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;130.5.5.25&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <b>10</b>000010 . 00000101 . 00000101 . 00011001<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;255.255.255.0&nbsp;&nbsp;
 * 11111111 . 11111111 . 11111111 . 00000000<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<--extended-network-prefix --><br/>
 * or<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;130.5.5.25/24&nbsp;&nbsp;
 * <b>10</b>000010 . 00000101 . 00000101 . 00011001<br/>
 * </code>
 * <br/>
 * This class supports both standards : the extended network prefix and the
 * subnet mask.
 *
 * @author  Fulco Houkes
 * @version 1.0
 *
 * @see IPNumber
 */
public class IPRange implements Cloneable
{

    /** IP address */
    private IPNumber mIPAddress = null;

    /** IP subnet mask */
    private IPNumber mIPSubnetMask = null;

    /** extended network prefix */
    private int mExtendedNetworkPrefix = 0;


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   ip
     *      String representation of the IP address. The two following formats
     *      are supported :<br/>
     *      <li/>xxx.xxx.xxx.xxx/xxx.xxx.xxx.xxx
     *      <li/>xxx.xxx.xxx.xxx/xx  <- extended network prefix
     *
     * @exception   InvalidIPRangeException
     *      Throws this exception when the specified string doesn't represent
     *      a valid IP address.
     */
    public IPRange (String range)
        throws  InvalidIPRangeException
    {
        parseRange (range);
    }


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   ipAddress
     *      Reference to the IP address number
     * @param   subnetMask
     *      Reference to the subnet mask
     *
     * @exception   InvalidIPRangeException
     *      Throws this exception when the combination of the IP address and the
     *      subnet mask does not define a valid IP range.
     * @exception   InvalidIPNUmberException
     *      Throws this exception if the specified IP address or subnet mask
     *      do ne define a valid IP number.
     */
    public IPRange (IPNumber ipAddress, IPNumber subnetMask)
        throws  InvalidIPRangeException,
                InvalidIPNumberException
    {
        if ((ipAddress == null) || (subnetMask == null)) {
            throw new InvalidIPNumberException ();
        }

        mIPAddress = ipAddress;
        mIPSubnetMask = subnetMask;

        mExtendedNetworkPrefix = computeNetworkPrefixFromMask (subnetMask);
        if (mExtendedNetworkPrefix == -1) {
            throw new InvalidIPRangeException ();
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   ipAddress
     *      The reference on the IP address.
     * @param   extendedNetworkPrefix
     *      The extended network prefix (0-32).
     *
     * @exception   InvalidIPNumberException
     *      Throws this exception if the specified IP address is not valid.
     * @exception   InvalidIPRangeException
     *      Throws this exception if the specified extended network prefix
     *      is not valid.
     */
    public IPRange (IPNumber ipAddress, int extendedNetworkPrefix)
        throws  InvalidIPNumberException,
                InvalidIPRangeException
    {
        if (ipAddress == null) {
            throw new InvalidIPNumberException();
        }

        if ((extendedNetworkPrefix < 0) || (extendedNetworkPrefix > 32)) {
            throw new InvalidIPRangeException();
        }

        mIPAddress = ipAddress;
        mExtendedNetworkPrefix = extendedNetworkPrefix;
        mIPSubnetMask = computeMaskFromNetworkPrefix (extendedNetworkPrefix);
    }

    //-------------------------------------------------------------------------
    /**
     * Private constructor used for cloning.
     *
     * @param   ipAddress
     *      Reference to the IP address number
     * @param   subnetMask
     *      Reference to the subnet mask
     * @param   extendedNetworkPrefix
     *      The extended network prefix (0-32).
     */
    private IPRange (IPNumber ipAddress, IPNumber subnetMask,
                     int extendedNetworkPrefix)
    {
        mIPAddress = ipAddress;
        mIPSubnetMask = subnetMask;
        mExtendedNetworkPrefix = extendedNetworkPrefix;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the encapsulated IP address.
     *
     * @return
     *      The IP address.
     */
    public final IPNumber getIPAddress () {
        return mIPAddress;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the encapsulated subnet mask
     *
     * @return
     *      The IP range's subnet mask.
     */
    public final IPNumber getIPSubnetMask () {
        return mIPSubnetMask;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the extended extended network prefix.
     *
     * @return
     *      Return the extended network prefix.
     */
    public final int getExtendedNetworkPrefix () {
        return mExtendedNetworkPrefix;
    }


    //-------------------------------------------------------------------------
    /**
     * Convert the IP Range into a string representation.
     *
     * @return
     *      Return the string representation of the IP Address following the
     *      common format xxx.xxx.xxx.xxx/xx (IP address/extended network
     *      prefixs).
     */
    public String toString ()
    {
        StringBuffer result = new StringBuffer (mIPAddress.toString());
        result.append ("/");
        result.append (mExtendedNetworkPrefix);

        return result.toString();
    }


    //-------------------------------------------------------------------------
    /**
     * Compare the specified IP range to the encapsulated one.
     *
     * @param   another
     *      The IP range to be compared.
     *
     * @return
     *      Return <code>true</code> if the encapsulated IP range is the same
     *      as the specified one, otherwise return <code>false</code>.
     */
    public boolean equals (Object another) {
        if (this == another) return true;
        
        if (another != null && this.getClass() == another.getClass()) {
            final IPRange range = (IPRange)another;
            return (mIPAddress.equals (range.getIPAddress()) &&
                   (mExtendedNetworkPrefix == range.mExtendedNetworkPrefix));
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Parse the IP range string representation.
     *
     * @param   range
     *      String representation of the IP range.
     *
     * @exception   InvalidIPRangeException
     *      Throws this exception if the specified range is not a valid IP
     *      network range.
     */
    protected void parseRange (String range)
        throws  InvalidIPRangeException
    {
        if (range == null) {
            throw new InvalidIPRangeException (range);
        }

        int index = range.indexOf ("/");
        if (index == -1) {
            throw new InvalidIPRangeException (range);
        }

        try {
            mIPAddress = new IPNumber (range.substring (0, index));

            String subnetStr = range.substring (index+1);

            // try to convert the remaining part of the range into a decimal
            // value.
            try {
                mExtendedNetworkPrefix = Integer.parseInt (subnetStr);
                if ((mExtendedNetworkPrefix < 0) ||
                    (mExtendedNetworkPrefix > 32)) {
                    throw new InvalidIPRangeException (range);
                }

                mIPSubnetMask =
                        computeMaskFromNetworkPrefix (mExtendedNetworkPrefix);
                subnetStr = null;
            }
            catch (NumberFormatException ex) {

                // the remaining part is not a valid decimal value.
                // Check if it's a decimal-dotted notation.
                mIPSubnetMask = new IPNumber (subnetStr);
                subnetStr = null;

                // create the corresponding subnet decimal
                mExtendedNetworkPrefix =
                        computeNetworkPrefixFromMask (mIPSubnetMask);
                if (mExtendedNetworkPrefix == -1) {
                    throw new InvalidIPRangeException (range);
                }
            }

        }
        catch (InvalidIPNumberException ex) {
            throw new InvalidIPRangeException (range);
        }
        catch (IndexOutOfBoundsException ex) {
            throw new InvalidIPRangeException (range);
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Compute the extended network prefix from the IP subnet mask.
     *
     * @param   mask
     *      Reference to the subnet mask IP number.
     * @return
     *      Return the extended network prefix. Return -1 if the specified mask cannot
     *      be converted into a extended prefix network.
     */
    private int computeNetworkPrefixFromMask (IPNumber mask)
    {
        int result = 0;
        int tmp = mask.getIPNumber();

        while ((tmp & 0x00000001) == 0x00000001) {
            result++;
            tmp = tmp >>> 1;
        }

        if (tmp != 0) {
            return -1;
        }
        return result;
    }


    //-------------------------------------------------------------------------
    /**
     * Convert a extended network prefix integer into an IP number.
     *
     * @param   prefix
     *      The network prefix number.
     *
     * @return
     *      Return the IP number corresponding to the extended network prefix.
     */
    private IPNumber computeMaskFromNetworkPrefix (int prefix)
        throws InvalidIPNumberException
    {
        int subnet = 0;
        for (int i=0; i<prefix; i++) {
            subnet = subnet << 1;
            subnet += 1;
        }
        return new IPNumber (subnet);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the specified IP address is in the encapsulated range.
     *
     * @param   address
     *      The IP address to be tested.
     *
     * @return
     *      Return <code>true</code> if the specified IP address is in the
     *      encapsulated IP range, otherwise return <code>false</code>.
     */
    public boolean isIPAddressInRange (IPNumber address)
    {
        int result1 = address.getIPNumber() & mIPSubnetMask.getIPNumber();
        int result2 = mIPAddress.getIPNumber() & mIPSubnetMask.getIPNumber();

        return result1 == result2;
    }


    //-------------------------------------------------------------------------
    /**
     * Clone the encapsulated IP Range.
     *
     * @return
     *      Return a new object representing the encapsulated IP Range.
     */
    public Object clone ()
    {
        return new IPRange ((IPNumber)mIPAddress.clone(),
                            (IPNumber)mIPSubnetMask.clone(),
                            mExtendedNetworkPrefix);
    }
}

