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
package words;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.utils.Classificacio;
import words.utils.Pair;
import words.utils.SAXReader;
import words.utils.TextReader;
import words.utils.WordType;

/**
 *
 * @author xavi
 */
public abstract class Model extends SAXReader {

    public enum Action {
        TRAIN, DEV, TEST, PROB, SIZE, ORDER, RANK
    };

    public enum Unit {
        CHAR, SYLLABLE
    };

    protected static String [] actions = {"train","dev","test","prob","size","order","rank"};

    public final char BOW = '#'; // Begin of word marker.
    public final char EOW = '$'; // End of word marker.

    public abstract double wordProb(String word);

    public abstract String split(String word);
    protected Pair<Double, Integer> lastTest;
    protected double totalProb;
    protected double order;

    public void load(String filename) {
        getText(filename);
    }

    public abstract void save(String filename);

    public abstract void addWord(String word);

    public abstract double getOrder();

    @Override
    public abstract void startElement(final String uri, final String localName,
            final String tag, final Attributes attributes) throws SAXException;

    /**
     * Creates a dictionary parsing a file
     * @param filename path of the file to parse
     */
    public void train(String filename, boolean verb) {
        if (verb) {
            try {
                TextReader scanner = new TextReader(filename, WordType.LETTERS);
                String word;
                int i = 0;
               
                while ((word = scanner.nextWord()) != null) {
                    ++i;
                    addWord(word.toLowerCase());
                    if (i % 100000 == 0) {
                        System.out.print(".");
                    }
                }

                System.out.print(" " + i + " words");
            } catch (IOException ioe) {
                System.err.println("ERROR: cannot read file (" + filename + ")");
            }
        } else {
            train(filename);
        }
    }

    /**
     * Creates a dictionary parsing a file
     * @param filename path of the file to parse
     */
    public void train(String filename) {
        try {
            TextReader scanner = new TextReader(filename, WordType.LETTERS);
            String word;

            while ((word = scanner.nextWord()) != null) {
                addWord(word.toLowerCase());
            }
        } catch (IOException ioe) {
            System.err.println("ERROR: cannot read file (" + filename + ")");
        }
    }

    public void splitFile(String filename) {
        splitFile(filename, (String) null);
    }

    /**
     *
     */
    public void splitFile(String filename, Dictionary d) {
        try {
            TextReader scanner = new TextReader(filename, WordType.LETTERS);
            String word;
            while ((word = scanner.nextWord()) != null) {
                word = word.toLowerCase();
                if (d == null || d.getWord(word) < 2) {
                    System.out.println(split(word));
                }
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
    }

    public void splitFile(String filename, String fileOut) {
        splitFile(filename, fileOut, null);
    }

    /**
     *
     */
    public void splitFile(String filename, String fileOut, Dictionary d) {
        PrintWriter pw;
        try {
            TextReader scanner = new TextReader(filename, WordType.LETTERS);
            String word;
            
            pw = new PrintWriter(new BufferedWriter(new FileWriter(fileOut)));
            while ((word = scanner.nextWord()) != null) {
                word = word.toLowerCase();
                if (d == null || d.getWord(word) < 2) {
                    double pr = wordProb(word);
                    double lg = -Math.log(pr);

                    /* DecimalFormat format = new DecimalFormat("0.000000000000000000000");
                    String spr = format.format(pr);
                    String slg = format.format(lg); */
                    String spr = "" + pr;
                    String slg = "" + lg;

                    pw.println(split(word) + ":\t\t" + spr + "\t\t" + slg + "");
                }
            }
            pw.close();
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
    }

    /**
     * Returns logLikelihood of a file
     * @param fileName
     * @return
     */
    public double test(String fileName, Dictionary d) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;

        double lastValue = 0.0;
        int lastLength = 0;
        try {
            while ((word = txreader.nextWord()) != null) {
                word = word.toLowerCase();
                if (d.getWord(word) < 2) {
                    ++lastLength;
                    lastValue -= Math.log(wordProb(word));
                }
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        lastTest = new Pair<Double, Integer>(lastValue, lastLength);
        return lastValue;
    }

    /**
     * Returns logLikelihood of a file
     * @param fileName
     * @return
     */
    public double test(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;

        double lastValue = 0.0;
        int lastLength = 0;
        try {
            while ((word = txreader.nextWord()) != null) {
                word = word.toLowerCase();
                ++lastLength;
                lastValue -= Math.log(wordProb(word));
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        lastTest = new Pair<Double, Integer>(lastValue, lastLength);
        return lastValue;
    }

    /**
     * @return last logLikelihood-per-word
     */
    public double getLastTest() {
        return lastTest.getFirst() / lastTest.getSecond();
    }

    /**
     * @return last logLikelihood
     */
    public double getLastValue() {
        return lastTest.getFirst();
    }

    /**
     * @return last test size
     */
    public int getLastLength() {
        return lastTest.getSecond();
    }

    /**
     * @return model size
     */
    public abstract int getSize();

    protected void printRanking(Classificacio cls) {
         ArrayList<String> a;
        for (Double d : cls.keySet()) {
                    a = cls.get(d);
                    for (String w : a) {
                        System.out.println(w + "\t" + d);
                    }
        }
    }
}
