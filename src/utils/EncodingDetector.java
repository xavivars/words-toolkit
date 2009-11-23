/*
 * Copyright (C) 2008
 *
 * Author:
 *  Xavier Ivars i Ribes <xavi@infobenissa.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package words.utils;

public class EncodingDetector {
    
    public static String UTF8 = "UTF-8";
    public static String ASCII = "ASCII";
    public static String ISO88591 = "ISO-8859-1";
    
    public static String getEncoding(byte [] bytes) {
        int comptador = 0;
        
        int i = 0;
        short b = '\0';
        boolean ascii = true;
       
        for (i = 0; i<bytes.length; i++)
        {
            b = (short)(0xFF & bytes[i]);
           
            if (comptador > 0)
            {
                if ((b >> 6) != 0x2)
                {
                        return ISO88591;
                }
                else
                {
                        comptador--;
                }
            }
            else if ((b & 0x80)>0)
            {
                ascii = false;
                if ((b >> 5) == 0x6)
                {
                        comptador = 1;
                }
                else if ((b >> 4) == 0xE)
                {
                        comptador = 2;
                }
                else if ((b >> 3) == 0x1E)
                {
                        comptador = 3;
                }
                else
                {
                        return ISO88591;
                }
            }
	}
        
        return ((ascii) ? ASCII : UTF8);        
    }

    public static String getEncoding(String f) {
        return getEncoding(Functions.file2bytes(f));
    }
}
