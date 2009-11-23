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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import words.utils.Classificacio;
import words.utils.TextReader;
import words.utils.WordType;

public abstract class Developer {

    protected Dictionary dict;
    protected Ngram ngrams;
    protected DMM dmc;
    protected HMMPre hmm;
    protected HMM hmmEnd;
    protected PST vlmc;
    protected Classificacio[] mapes;
    protected HashMap<String, Boolean> calculat;
    protected boolean frequency;
    protected String[] names;
    protected int lastLength;
    protected double lastValue;
    protected double lambda;
    private double knownProb;

    public Developer(Trainer tr) {
        this.dict = tr.getDictionary();
        this.ngrams = tr.getNgram();
        this.dmc = tr.getDMC();
        this.hmm = tr.getHMM();
        this.hmmEnd = tr.getHMMEnd();
        this.vlmc = tr.getVLMC();
        calculat = new HashMap<String, Boolean>();
        this.frequency = false;
        lastLength = 0;
        lastValue = 0;
        knownProb = 0;
        lambda = 1;
    }

    protected abstract void paramTest(String w, Classificacio[] m);

    public double getLastTest() {
        return lastValue / lastLength;
    }

    public double getLastValue() {
        return lastValue;
    }

    public int getLastLength() {
        return lastLength;
    }

    public void setLambda(double mxPr) {
        lambda = mxPr / knownProb;
    }

    public double getLambda() {
        return lambda;
    }

    public void addKnownProb(String w) {
        this.knownProb += prob(w);
    }

    public double getKnownProb() {
        return knownProb;
    }

    public double logLikelihood(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        lastValue = 0.0;
        lastLength = 0;
        try {
            while ((word = txreader.nextWord()) != null) {
                ++lastLength;
                double d = prob(word);
                lastValue -= Math.log(d);
                //System.out.println(word+" ("+d+")");
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        return lastValue;
    }

    public void adjust(String filename) {
        this.adjust(filename, false);
    }

    public abstract void adjust(String filename, boolean freq);

    public abstract double prob(String word);

    public abstract int getSize();

    public void printRanking() {
        this.printRanking(".");
    }

    public void printRanking(String path) {
        int i = -1;

        FileWriter f;
        FileWriter r;

        ArrayList<String> a;

        for (Classificacio mapa : mapes) {
            ++i;
            try {


                f = new FileWriter(path + "/dev." + this.names[i] + ".list");
                r = new FileWriter(path + "/dev." + this.names[i] + ".rank");

                for (Double d : mapa.keySet()) {
                    a = mapa.get(d);
                    for (String w : a) {
                        f.write(w + "\n");
                        r.write(w + "\t" + d + "\n");
                    }
                }

                f.close();
                r.close();
            } catch (IOException ex) {
                System.err.println("Error printing " + this.names[i]);
                ex.printStackTrace();
                System.exit(0);
            }
        }
    }
}
