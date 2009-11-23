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
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public abstract class Huffman {

    protected HashMap<String, Int> strings;
    protected ArrayList<HuffmanNode> nodes;
    protected Vector<Int> count;
    protected boolean countAll = true;
    protected int degree;
    protected String version;

    public Huffman() {
        version  ="abstract";
        strings = new HashMap<String, Int>();
        count = new Vector<Int>();
        count.insertElementAt(new Int(0), 0);
        degree = 2;
    }

    public ArrayList<Pair<String, Pair<String, Double>>> getCodes() {
        ArrayList<Pair<String, Pair<String, Double>>> llista;

        llista = new ArrayList<Pair<String, Pair<String, Double>>>();

        for (HuffmanNode n : nodes) {
            llista.addAll(n.getCodes());
        }

        return llista;
    }

    public void createCodes() {
        nodes = new ArrayList<HuffmanNode>();

        Set<String> ks = strings.keySet();
        for (String s : ks) {
            Int i = strings.get(s);
            double d = i.getValue();
            if (this.countAll) {
                d = d / ((double) count.get(0).getValue());
            } else {
                d = d / ((double) count.get(s.length()).getValue());
            }
            HuffmanNode n = new HuffmanNode(s, d);
            nodes.add(n);
        }

        while (nodes.size() > 1) {
            int group = getGroupBy();
            Collections.sort(nodes);
            HuffmanNode c = new HuffmanNode(nodes, group);
            nodes.add(c);
        }


    }

    public int getGroupBy() {
        /*
         * Primer agrupa el resto, els que sobren
         * d'agrupar grups complets. Després va
         * agrupant de 'degree' en 'degree'.
         *
         * Ex: degree 7 i total inicial 15
         *
         * en la primera passada,
            total 15
            agruparia 3 en 1
            nou_total 13
         * en la segona passada,
            total 13
            agruparia 7 en 1
            nou_total 7
         * en la tercera passada,
            total 7
            agruparia 7 en 1
            nou_total 1
            fi
         *
         */
        int size = nodes.size() - 1;
        int grau = this.degree - 1;

        if ((size % grau) != 0) {
            size = size % grau;
            ++size;
        } else {
            size = grau + 1;
        }

        return size;
    }

    public void print() {
        for (HuffmanNode n : nodes) {
            n.print();
        }
    }

    public void printXML() {
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        System.out.println("<huffman version=\"" + version + "\"degree=\"\">");
        for (HuffmanNode n : nodes) {
            n.printXML();
        }
        System.out.println("</huffman>");
    }
}
