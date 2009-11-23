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
package words;

import java.io.*;
import java.util.*;

import words.utils.*;
import org.xml.sax.*;

public class Dictionary extends Model {

    private Map<String, Int> dict;
    private String language;

    public Dictionary() {
        this.language = "unknown";
        this.dict = new TreeMap<String, Int>();
        this.order = -1;
    }

    public Dictionary(String l) {
        this();
        this.language = l;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String l) {
        if (l != null) {
            this.language = l;
        } else {
            this.language = "unknown";
        }
    }

    public double getOrder() {

        if(order<0) {
            Set<String> wds = dict.keySet();
            for(String wd : wds) {
                if(wd.length()>order)
                    order = wd.length();
            }
        }

        return order;
    }

    public void addWord(String w) {
        dict.put(w, (dict.containsKey(w) ? dict.get(w) : new Int(0)).add(1));
    }

    public void addEntry(String w, int c) {
        dict.put(w, (dict.containsKey(w) ? dict.get(w).add(c) : new Int(c)));
    }

    public int getWord(String w) {
        return (dict.containsKey(w) ? dict.get(w).getValue() : 0);
    }

    public boolean hasWord(String w) {
        return (dict.containsKey(w));
    }

    public void save(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (!this.language.equalsIgnoreCase("unknown")) {
                pw.println("<dictionary lang=\"" + this.language + "\">");
            } else {
                pw.println("<dictionary>");
            }
            Set<String> entries = dict.keySet();

            for (String entry : entries) {
                pw.println("\t<entry word=\"" + entry + "\" count=\"" + dict.get(entry) + "\" />");
            }

            pw.write("</dictionary>");

            pw.close();

        } catch (IOException ioe) {
            System.err.println("Error saving dictionary");
        }
    }

    /**
     *
     */
    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {

        if (tag.equals("entry")) {
            String word = attributes.getValue("word");
            int count = Integer.parseInt(attributes.getValue("count"));

            this.addEntry(word, count);
        }
    }

    public Set<String> getWords() {
        return dict.keySet();
    }

    public Map<String, Int> getDict() {
        return dict;
    }

    @Override
    public String split(String word) {
        return BOW + word + EOW;
    }

    @Override
    public double wordProb(String word) {
        return (hasWord(word) ? 1 : 0);
    }

    public static void main(String[] args) {
        Dictionary dict = new Dictionary(args[0]);

        dict.train(args[1]);
        dict.save(args[2]);
    }

    @Override
    public int getSize() {
        return dict.size();
    }

    public int getTotalWords() {
        int ret = 0;

        Set<String> wds = dict.keySet();

        for(String word : wds) {
            ret += dict.get(word).getValue();
        }

        return ret;
    }
}
