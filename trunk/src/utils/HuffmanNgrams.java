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

import java.util.Map;
import java.util.Set;
import words.Ngram;

public class HuffmanNgrams extends Huffman {

    private int order;
    private boolean limitOrder;
    private int minOrder;
    private int ngramSize;
    private int maxSize;

    public int getMaxSize() {
        return maxSize;
    }

    public void setCountAll() {
        this.countAll = true;
    }

    public void setMaxSize(int maxSize) {
        if (maxSize < 1) {
            return;
        }
        this.maxSize = maxSize;
    }

    public int getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(int minOrder) {
        if (minOrder < 1) {
            return;
        }
        this.minOrder = minOrder;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        if (order < 1) {
            return;
        }
        this.order = order;
        this.limitOrder = true;
    }

    public HuffmanNgrams() {
        super();
        version = "HuffmanNgrams";
        this.limitOrder = false;
        this.order = -1;
        this.minOrder = 1;
        this.maxSize = 0;
        this.ngramSize = 0;
    }

    public HuffmanNgrams(int d) {
        this();
        this.degree = d;
    }

    public void addEntry(String w, int c, int l) {
        int n = (strings.containsKey(w) ? strings.get(w).add(c).getValue() : c);

        strings.put(w, new Int(n));
        ++ngramSize;

        count.get(0).add(c);

        Int aux = new Int(0);

        if (l >= count.size()) {
            aux.add(c);
            count.insertElementAt(aux, l);
        } else {
            if (count.get(l) != null) {
                count.get(l).add(c);
            } else {
                count.insertElementAt(new Int(c), l);
            }
        }

    }

    public boolean testSizes(int s) {
        boolean ret = true;

        if (this.maxSize > 0) {
            if (s > this.minOrder) {
                if (ngramSize >= maxSize) {
                    ret = false;
                }
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        HuffmanNgrams hf = new HuffmanNgrams(255);
        hf.setMinOrder(1);
        hf.setMaxSize(65000);
        hf.getText("../huffman/ordenat.xml");
        //hf.getText(args[0]);
        hf.createCodes();
    //hf.printXML();
    }

    public void getText(String s) {
        Ngram rd;
        if (this.limitOrder) {
            rd = new Ngram(this.order);
        } else {
            rd = new Ngram();
        }

        rd.setMaxSize(this.maxSize, true);
        rd.getText(s);

        Map<String,Int> map = rd.getMap();
        Set<String> keys = map.keySet();
        for(String key : keys) {
            addEntry(key, map.get(key).getValue(), key.length());
        }
    }
}
