/*
 * Copyright (C) 2008
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
package words.utils;

import java.util.ArrayList;


public class HuffmanNode implements Comparable<HuffmanNode> {

        private String str;
        private double prob;
        private String prefix;
        private ArrayList<HuffmanNode> children;

        public HuffmanNode(String s, double d) {
            children = new ArrayList<HuffmanNode>();
            prob = d;
            str = s;
            prefix = "";
        }

        public HuffmanNode(ArrayList<HuffmanNode> list, int group) {
            children = new ArrayList<HuffmanNode>();
            prob = 0;
            Int codi = new Int(group);
            for (int i = 0; i < group; ++i) {
                int last = list.size() - 1;
                HuffmanNode tmp = list.remove(last);
                prob += tmp.prob;
                tmp.addPrefix(codi);
                children.add(tmp);
                codi.decValue();
            }
            str = null;
        }

        public void addPrefix(Int b) {
            if (prefix != null) {
                if(!this.prefix.equalsIgnoreCase(""))
                    this.prefix = b.toString() + "-" + this.prefix;
                else
                    this.prefix = b.toString();
            } else {
                for (HuffmanNode n : children) {
                    n.addPrefix(b);
                }
            }
        }

        public void addPrefix(String s) {
            if (prefix != null) {
                this.prefix = s + this.prefix;
            } else {
                for (HuffmanNode n : children) {
                    n.addPrefix(s);
                }
            }
        }

        public ArrayList<Pair<String,Pair<String,Double>>> getCodes() {
             ArrayList<Pair<String,Pair<String,Double>>> llista;

             llista = new ArrayList<Pair<String, Pair<String,Double>>>();

             if (prefix != null) {
               Pair<String,Pair<String,Double>> s =
                       new Pair<String, Pair<String,Double>>(str,
                        new Pair<String,Double>(prefix,prob));
               llista.add(s);
            } else {
                for (HuffmanNode n : children) {
                    llista.addAll(n.getCodes());
                }
            }

             return llista;
        }

        @Override
        public int compareTo(HuffmanNode o) {
            double d = (o.prob - this.prob);
            return (d > 0) ? 1 : ((d == 0) ? 0 : -1);
        }

        public void print() {
            if (this.str != null) {
                System.out.println(str + ":" + prefix + " (" +prob+")");
            } else {
                for (HuffmanNode n : children) {
                    n.print();
                }
            }
        }

        public void printXML() {
                        if (this.str != null) {
                System.out.println("\t<code plain=\"" + str
                        + "\" length=\"" + str.length()
                        + "\" newlength=\"" + LlistaNgrames.length(prefix)
                        + "\" compressed=\"" + prefix + "\" prob=\"" +prob+"\" />");
            } else {
                for (HuffmanNode n : children) {
                    n.printXML();
                }
            }
        }
    }