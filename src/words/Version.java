/*
 * Copyright (C) 2009
 *
 * Author:
 *  Xavier Ivars i Ribes <xavi.ivars@gmail.com>
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
package words;

/**
 *
 * @author xavi
 */
public class Version {

    public static final String BUILD = "20091120";
    public static final String VERSION = "0.2";
    public static final boolean STABLE = false;

    public static void main(String [] args) {
        System.out.println("words.TOOLKIT");
        System.out.println("=============");
        System.out.println("");
        if(STABLE)
            System.out.println("Version number: "+VERSION);
        else
            System.out.println("Build ID: "+BUILD);
    }
}
