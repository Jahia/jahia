/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.tools;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/**
 *
 * @version 1.0
 */
public class ReadLicense
{
    private static final String LICENSE_FILENAME = "jahia.license";


    //-------------------------------------------------------------------------
    public static void main (String args[])
    {
        System.out.println ("\nLicense key reader, version 1.0");
        System.out.println ("(c) Jahia Ltd 2002\n\n");

        try {
            File file = new File (LICENSE_FILENAME);

            FileInputStream fstream = new FileInputStream (file);
            DataInputStream stream = new DataInputStream (fstream);

            // create the raw byte stream from the file size by removing the
            // checksum bytes and it's offset.
            int streamSize = (new Long(file.length() - 10)).intValue();
            byte[] bytes = new byte[streamSize];
            int index = 0;

            short crcOffset = stream.readShort ();
            // read the <crcOffset> bytes before the crc info
            for (int i=0; i<crcOffset; i++) {
                bytes[index] = stream.readByte();
                index++;
            }

            long storedChecksum = stream.readLong ();
            System.out.println (" stored checksum = "+Long.toHexString (storedChecksum));

            // read the remaining bytes of the file.
            try {
                while (index < bytes.length) {
                    bytes[index] = stream.readByte();
                    index++;
                }
            }
            catch (EOFException ex) {
            }

            // free memory of unused objects
            fstream.close();
            stream = null;
            fstream = null;
            file = null;

            //DisplayBytes (bytes);

            // compute the stream CRC
            CRC32 crc = new CRC32();
            crc.update (bytes);

//            long streamChecksum = crc.getValue ();
            //System.out.println (" stream checksum = "+Long.toHexString (streamChecksum));

            //Read the  license info
            ByteArrayInputStream byteStream = new ByteArrayInputStream (bytes);
            stream = new DataInputStream (byteStream);
            int offset = 0;



            //System.out.println ("License info :");
			
			try {
	            // license type
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int licenseType = stream.readInt();
	            System.out.println ("  - license type   = "+licenseType);
			} catch ( Exception t ){
	            System.out.println ("  - error reading license type");
				return;					
			}

			try {
	            // page limit
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int pageLimit = stream.readInt();
	            System.out.println ("  - page limit     = "+pageLimit);
			} catch ( Exception t ){
	            System.out.println ("  - error reading page limit");
				return;					
			}

			try{
	            // page template limit
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int templateLimit = stream.readInt();
	            System.out.println ("  - template limit = "+templateLimit);
			} catch ( Exception t ){
	            System.out.println ("  - error reading page template limit");
				return;					
			}

			try {
	            // user limit
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int userLimit = stream.readInt();
	            System.out.println ("  - user limit     = "+userLimit);
			} catch ( Exception t ){
	            System.out.println ("  - error reading user limit");
				return;					
			}

	        try {
	            // site limit
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int siteLimit = stream.readInt();
	            System.out.println ("  - site limit     = "+siteLimit);
			} catch ( Exception t ){
	            System.out.println ("  - error reading site limit");
				return;					
			}

			try {
	            // license id
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int nbBytes = stream.readInt();
	            byte[] stringAsBytes = new byte[nbBytes];
	            stream.read(stringAsBytes);
	            String licenseID = new String(stringAsBytes,"UTF-16");
	            System.out.println ("  - license id     = "+licenseID);
			} catch ( Exception t ){
	            System.out.println ("  - error reading license type");
				return;					
			}

			try {	
	            // build number
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            int buildNumber = stream.readInt();
	            System.out.println ("  - build number   = "+buildNumber);
			} catch ( Exception t ){
	            System.out.println ("  - error reading build number");
				return;					
			}

			try {
	            // release number
	            offset = stream.readInt ();
	            stream.skipBytes(offset);
	            double releaseNumber = stream.readDouble();
	            System.out.println ("  - release number = "+releaseNumber);
			} catch ( Exception t ){
	            System.out.println ("  - error reading release number");
				return;					
			}

            System.out.println ("\nLicense file successfully readed.");

        }
        catch (IOException ex) {
            //System.out.println ("ERROR : I/O exception while reading the file.");
            return;
        }

    }


}
