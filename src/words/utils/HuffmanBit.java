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
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HuffmanBit extends SAXReader {

    HashMap<String, Int> ngrams;
    Vector<Int> count;
    private int order;
    private boolean limitOrder;
    private int minOrder;
    private int ngramSize;
    private ArrayList<Node> nodes;
    boolean countAll = false;

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
    private int maxSize;

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

    public HuffmanBit() {
        this.limitOrder = false;
        this.order = -1;
        count = new Vector<Int>();
        count.insertElementAt(new Int(0), 0);
        this.minOrder = 1;
        this.maxSize = 0;
        this.ngramSize = 0;
        ngrams = new HashMap<String, Int>();
    }

    public void addEntry(String w, int c, int l) {
        int n = (ngrams.containsKey(w) ? ngrams.get(w).add(c).getValue() : c);

        ngrams.put(w, new Int(n));
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

    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {
        if (tag.equals("entry")) {
            boolean add = true;

            String entry = attributes.getValue("string");
            int howmuch = Integer.parseInt(attributes.getValue("count"));
            int length = Integer.parseInt(attributes.getValue("length"));

            if ((this.limitOrder) && (entry.length() > this.order)) {
                add = false;
            }
            if (!testSizes(length)) {
                add = false;
            }

            if (add) {
                this.addEntry(entry, howmuch, length);
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

    public void createCodes() {
        nodes = null;
        nodes = new ArrayList<Node>();

        Set<String> ks = ngrams.keySet();
        for (String s : ks) {
            Int i = ngrams.get(s);
            double d = i.getValue();
            if (this.countAll) {
                d = d / ((double) count.get(0).getValue());
            } else {
                d = d / ((double) count.get(s.length()).getValue());
            }
            Node n = new Node(s, d);
            nodes.add(n);
        }

        while (nodes.size() > 1) {
            Collections.sort(nodes);
            Node a = nodes.remove(nodes.size() - 1);
            Node b = nodes.remove(nodes.size() - 1);
            Node c = new Node(b, a);
            nodes.add(c);
        }
    }

    public void print() {
        for (Node n : nodes) {
            n.print();
        }
    }

    public static void main(String[] args) {
        HuffmanBit hf = new HuffmanBit();
        hf.setMinOrder(1);
        hf.setMaxSize(240);
        //hf.getText("../huffman/test.xml");
        hf.getText(args[0]);
        hf.createCodes();
        hf.print();
    }

    private class Node implements Comparable<Node> {

        private String str;
        private double prob;
        private String prefix;
        private ArrayList<Node> children;

        public Node(String s, double d) {
            children = new ArrayList<Node>();
            prob = d;
            str = s;
            prefix = "";
        }

        public Node(Node a, Node b) {
            children = new ArrayList<Node>();
            prob = a.prob + b.prob;
            str = null;
            a.addPrefix("0");
            b.addPrefix("1");
            children.add(a);
            children.add(b);
        }

        public void addPrefix(String s) {
            if (prefix != null) {
                this.prefix = s + this.prefix;
            } else {
                for (Node n : children) {
                    n.addPrefix(s);
                }
            }
        }

        @Override
        public int compareTo(Node o) {
            double d = (o.prob - this.prob);
            return (d > 0) ? 1 : ((d == 0) ? 0 : -1);
        }

        public void print() {
            if (this.str != null) {
                System.out.println(str + ":" + prefix);
            } else {
                for (Node n : children) {
                    n.print();
                }
            }

        }
    }
}
