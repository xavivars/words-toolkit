/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

import java.text.DecimalFormat;

public class CoderWords extends Coder {

    public CoderWords(Huffman hf) {
        super(hf);
    }

    @Override
    public String encode(String s, int n) {

        if(coder.containsKey(s)) {
        String r = coder.get(s).getFirst();
        printProbs(s);
        return r;
        } else {
            System.out.print("\t\t");
            System.out.println(s + ": NO EXIST");
            return null;
        }
    }

    private void printProbs(String s) {
        //System.out.print("\t\t");
        System.out.print(s + ": " + coder.get(s).getFirst());
        System.out.print(" [" + LlistaNgrames.length(coder.get(s).getFirst()) + "]: ");
        System.out.println(prdouble(Math.log(coder.get(s).getSecond())));

    }

    public String prdouble(double d) {
        DecimalFormat myFormatter = new DecimalFormat("00.000000");
        return myFormatter.format(Math.abs(d));
    }
}
