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

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Coder {

    protected HashMap<String, Pair<String, Double>> coder;
    protected HashMap<String, String> decoder;

    public Coder(Huffman hf) {
        coder = new HashMap<String, Pair<String, Double>>();
        decoder = new HashMap<String, String>();

        ArrayList<Pair<String, Pair<String, Double>>> llista;

        llista = hf.getCodes();

        for (Pair<String, Pair<String, Double>> p : llista) {
            coder.put(p.getFirst(), p.getSecond());
            decoder.put(p.getSecond().getFirst(), p.getFirst());
        }
    }

    public Coder() {
    }

    public String encode(String str) {
        long l;
        long ara;
        String ret;
        l = System.currentTimeMillis();
        ret = encode(str,1);
        ara = System.currentTimeMillis();
        System.out.println("TIME: "+(ara-l));
        System.out.println("----------------");
        return ret;
    }

    public String encodeAll(String str) {
        return encode(str, -1);
    }

    public abstract String encode(String s,int n);

    public String decode(String str) {
        String ret = "";

        String[] ss = str.split("-");

        int i = 0;
        String aux = "";
        while (i < ss.length) {
            aux += ss[i];

            if (decoder.containsKey(aux)) {
                ret += decoder.get(aux);
                aux = "";
            }
            ++i;
        }

        return ret;
    }
}
