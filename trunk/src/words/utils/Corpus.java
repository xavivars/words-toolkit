/*
 * Copyright (C) 2009
 *
 * Authors:
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
package words.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author xavi
 */
public class Corpus {

    public static void main(String [] args) {

        if(args.length<2)
            return;

        try {
        int size = Integer.parseInt(args[0]);
        String file = args[1];

        TextReader txt = new TextReader(file, WordType.LETTERS);
        PrintWriter pf = new PrintWriter(new BufferedWriter(new FileWriter(file + "." + size)));

        String wd = txt.nextWord();
        for(int i=0;i<size && wd!=null;++i,wd = txt.nextWord()) {
            pf.write(wd+" ");
        }

        pf.close();

        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

    }

}
