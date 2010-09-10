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
import java.util.logging.Level;
import java.util.logging.Logger;
import words.utils.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;


public class Ngram extends Model {

    Map<String, Int> ngrams;
    private int loadedOrder;
    double numWords;
    private double[] lambda;
    private Syllabicator syl;
    private int maxSize;
    private boolean flexible;

    private Unit unit;

    public Ngram() {
        this.ngrams = new TreeMap<String, Int>();
        this.order = 5;
        this.lambda = null;
        this.unit = Unit.CHAR;
        this.syl = new Syllabicator();
        this.maxSize = -1;
        this.flexible = false;
        this.totalProb = 0;
        this.loadedOrder = 0;
    }

    public Ngram(int o) {
        this();
        if (o > 0) {
            this.order = o;
        }
    }

    public void setMaxSize(int i) {
        if (i > 0) {
            this.maxSize = i;
        }
    }

    public void setMaxSize(int i, boolean b) {
        this.setMaxSize(i);
        this.flexible = true;
    }

    public void setUnit(Unit u) {
        this.unit = u;
    }

    public void setOrder(int i) {
        if ((i > 0) && (i < this.order)) {
            this.order = i;
        }
    }

    public double getOrder() {
        return order;
    }

    private void addEntry(String s) {
        if (ngrams.containsKey(s)) {
            ngrams.get(s).incValue();
        } else {
            ngrams.put(s, new Int(1));
        }
        numWords++;
    }

    public void load(String filename) {
        super.load(filename);

        // change order if needed
        Set<String> words = ngrams.keySet();
        order = loadedOrder;
    }

    /**
     * Extracts all k-grams in a word upto maximal order. For instance, if word =
     * "ma" and order = 3 0-grams: "" (three empty strings, to normalize
     * 1-grams). 1-grams: "m a $" ($ being end-of-word). 2-grams: "#m ma a$" (#
     * being used to differentiate from 1-gram m). 3-grams: "##m #ma ma$"
     * 
     * @remark do NOT add 1-gram "#" or 1-gram normalization will be wrong.
     * @param word
     *            the word to be added.
     */
    public void addWord(String word) {
        if (word.length() < 1) {
            System.err.println("Cannot extract n-grams from " + word);
            System.exit(1);
        }


        if (this.unit == Unit.CHAR) {
            word = word + EOW;
            String s = new String();
            while (s.length() < order) {
                s += BOW;
            }
            for (int last = 0; last < word.length(); ++last) {
                s = tail(s) + word.charAt(last);
                for (int first = 0; first <= s.length(); ++first) {
                    addEntry(s.substring(first));
                }
            }
        } else {
            String nword = syl.split(word);
            nword = nword + "-" + EOW;


            String toadd = "";
            for (int i = 0; i < order; i++) {
                toadd += BOW + "-";
            }

            int numsyl = syl.countSyllables(nword);
            nword = toadd + nword;
            String neww;
            for (int i = 0; i < numsyl;) {
                neww = syl.getSyl(nword, ++i, (int)(i + order - 1));

                for (int first = 0; first <= order; ++first) {
                    addEntry(syl.getSyl(neww, first, (int)order));
                }

            }

        }
    }

    /**
     * @param s a k-gram
     * @return the (k-1)-gram obtained by removing its first character.
     */
    private String tail(String s) {
        return s.substring(1);
    }

    /**
     * @param s a k-gram
     * @return the (k-1)-gram obtained by removing its last character.
     */
    private String head(String s) {
        return s.substring(0, s.length() - 1);
    }

    /**
     * Saves model to a XML
     * @param filename
     */
    public void save(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<ngrams prob=\"" + totalProb + "\">");

            Set<String> entries = ngrams.keySet();

            for (String entry : entries) {

                int length = (this.unit == Unit.CHAR) ? entry.length() : syl.countSyllables(entry);
                String pr = "";
                if (length > 0) {
                    prob(entry);
                }
                pw.println("\t<entry length=\"" + length + "\" string=\"" + entry + "\" count=\"" + ngrams.get(entry) + "\" prob=\"" + pr + "\" />");
            }

            pw.write("</ngrams>");

            pw.close();

        } catch (IOException ioe) {
            System.err.println("Error saving ngrams");
        }
    }

    /**
     * @param n n-gram order.
     * @return Good-Turing Backoff parameter.
     */
    private double lambda(int n) {
        if (lambda == null) {
            lambda = new double[(int)order];
            int[] total = new int[(int)order];
            int[] singles = new int[(int)order];
            for (String word : ngrams.keySet()) {
                if (word.length() > 0) {
                    int k = word.length() - 1;
                    int times = ngrams.get(word).getValue();
                    total[k] += times;
                    if (times == 1) {
                        ++singles[k];
                    }
                }
            }
            for (int k = 0; k < order; ++k) {
                lambda[k] = singles[k] / (double) total[k];
            }
        }
        return lambda[n];
    }

    /**
     * @return the number of words in sample text.
     */
    private int numWords() {
        return ngrams.get("" + EOW).getValue(); // $ is end-of-word.

    }

    /**
     * @param s a k-gram (k > 0)
     * @return the conditional probability of s,
     * normalized to the number of heads.
     */
    private double prob(String s) {
        if (ngrams.containsKey(s)) {
            String h = head(s);
            if (h.endsWith("" + BOW)) // head is not stored
            {
                return ngrams.get(s).getValue() / (double) numWords();
            } else {
                return ngrams.get(s).getValue() /
                        (double) ngrams.get(h).getValue();
            }
        } else {
            if(s.length() > 1) {
                return 0;
            } else {
                return 1.0/ngrams.get("").getValue();
            }
        }
    }

    /**
     * @param s a k-gram
     * @return The conditional probability of the k-gram,
     * normalized to the frequency of its heads
     * and interpolated with lower order models.
     */
    private double backProb(String s) {
        double result;
        if (s.length() > 1) {
            double lam = lambda(s.length() - 1);
            result = (1 - lam) * prob(s) + lam * backProb(tail(s));
        } else {
            result = prob(s);
        }
        return result;
    }

    /**
     * Compute probability of a word.
     * @param word
     * @return the log-probability of the contained n-grams
     */
    public double wordLogProb(String word) {
        double res = 0;

        // afegim el final de la paraula
        if (word.length() < 1) {
            System.err.println("Cannot compute probability of " + word);
            System.exit(1);
        } else {
            word = word + EOW;
        }

        // preparem l'inici dels ngrames
        String s = new String();
        while (s.length() < order) {
            s += BOW;
        }
        for (int last = 0; last < word.length(); ++last) {
            s = tail(s) + word.charAt(last);
            double p = backProb(s);

            if (p == 0) {
                System.err.println(s + " has 0 probability");
                return -100.0;
            } else {
                res += Math.log10(p);
            }
        }
        return res;
    }

    /**
     * Compute probability of a word.
     * @param a word.
     * @return the probability of the contained n-grams.
     */
    public double wordProb(String word) {
        return wordProb(word, (int)order);
    }

    /**
     * Compute probability of a word.
     * @param a word.
     * @return the probability of the contained n-grams.
     */
    public double wordProb(String word, int ord) {

        double res = 1;

        if (word.equalsIgnoreCase("peered")) {
            res = 1;
        }

        // afegim el final de la paraula
        if (word.length() < 1) {
            System.err.println("Cannot compute probability of " + word);
            System.exit(1);
        } else {
            word = word + EOW;
        }

        // preparem l'inici dels ngrames
        String s = new String();
        while (s.length() < ord) {
            s += BOW;
        }
        for (int last = 0; last < word.length(); ++last) {
            s = tail(s) + word.charAt(last);
            double p = backProb(s);

            if (p == 0) {
                System.err.println(s + " has 0 probability");
                return -100.0;
            } else {
                res *= (p);
            }
        }
        return res;
    }

    public void addEntry(String w, int c) {
        int n = (ngrams.containsKey(w) ? ngrams.get(w).add(c).getValue() : c);
        ngrams.put(w, new Int(n));
        numWords += n;
    }

    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {
        if (tag.equals("entry")) {
            String entry = attributes.getValue("string");
            int l = Integer.parseInt(attributes.getValue("length"));
            int count = Integer.parseInt(attributes.getValue("count"));
            boolean add = true;
            if (entry.length() > this.order) {
                add = false;
            }

            if ((this.ngrams.size() > maxSize) && (maxSize > 0)) {
                if ((!flexible) || (l != 1)) {
                    add = false;
                }
            }

            if (add) {
                if(entry.length()>loadedOrder) loadedOrder = entry.length();
                this.addEntry(entry, count);
            }

        }

        if (tag.equals("ngram")) {
            totalProb = Double.parseDouble(attributes.getValue("prob"));
        }
    }

    public Map<String, Int> getMap() {
        return ngrams;
    }

    /**
     * Compute entropy of a word.
     * @param a word.
     * @return the probability of the contained n-grams.
     */
    public double wordEntropy(String word) {
        return wordEntropy(word, (int)order);
    }

    /**
     * Compute entropy of a word.
     * @param a word.
     * @return the probability of the contained n-grams.
     */
    public double wordEntropy(String word, int ord) {
        double res = 1;
        double ent = 0;

        // afegim el final de la paraula
        if (word.length() < 1) {
            System.err.println("Cannot compute probability of " + word);
            System.exit(1);
        } else {
            word = word + EOW;
        }

        // preparem l'inici dels ngrames
        String s = new String();
        while (s.length() < ord) {
            s += BOW;
        }
        for (int last = 0; last < word.length(); ++last) {
            s = tail(s) + word.charAt(last);
            double p = backProb(s);

            if (p == 0) {
                System.err.println(s + " has 0 probability");
                return -100.0;
            } else {
                ent += p * Math.log10(1.0 / p);
                System.out.println("p(" + s + ")=" + p);
                res *= (p);
            }
        }
        return ent;
    }

    public String split(String word) {
        return word + EOW;
    }

/**************/
    public static void main(String[] args) {
        try {
            Ngram ngram;

            CmdOptionTester optionTester = new CmdOptionTester();
            CmdOptions parser = new CmdOptions();

            // train, dev
            Option oaction = parser.addStringOption('a', "action");
            // corpus file
            Option ocorpus = parser.addStringOption('c', "corpus");
            // dictionary file
            Option odictionary = parser.addStringOption('d', "dictionary");
            // ngram file
            Option ongram = parser.addStringOption('n', "ngram");
            // n-gram size
            Option olength = parser.addIntegerOption('l', "length");
            // force overwriting
            Option over = parser.addBooleanOption('f', "force");
            // total prob
            Option ototal = parser.addBooleanOption('t',"total");

            // help
            Option help = parser.addBooleanOption('?',"help");
            parser.parse(args);

            if(optionTester.testBoolean(parser,help)) {
                show_help();
                System.exit(0);
            }

            Action action = optionTester.testAction(parser, oaction, Ngram.actions);          

            if (action == Action.TRAIN) {
            	String corpus = optionTester.testCorpus(parser, ocorpus,true);
                boolean force = optionTester.testBoolean(parser, over);
                String dict = optionTester.testDict(parser, odictionary, false, true, true);
                String ngramf = optionTester.testNgram(parser, ongram, force, false, true);
	            int length = optionTester.testInteger(parser, olength, 5);

                ngram = new Ngram(length);
                ngram.train(corpus);
                Dictionary dictionary = new Dictionary();
                dictionary.load(dict);

                Set<String> wds = dictionary.getWords();

                for (String wd : wds) {
                    ngram.totalProb += ngram.wordProb(wd);
                }

                ngram.save(ngramf);
            }
            if (action == Action.TEST) {
                String ngramf = optionTester.testNgram(parser, ongram, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
	            int length = optionTester.testInteger(parser, olength, 5);
                ngram = new Ngram(length);
                ngram.load(ngramf);

                ngram.test(corpus);
                System.out.println("Total logLikelihood: " + ngram.lastTest.getFirst());
                System.out.println("Total words: " + ngram.lastTest.getSecond());
                System.out.println("======================================\n");
                System.out.println("logLikelihood per word: " + (ngram.lastTest.getFirst()/ngram.lastTest.getSecond()));
            }
            if(action == Action.PROB) {
                String ngramf = optionTester.testNgram(parser, ongram, false, true, true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                double total = 0, pr = 0;
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
	            int length = optionTester.testInteger(parser, olength, 5);

                ngram = new Ngram(length);
                ngram.load(ngramf);
                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    while ((word = txreader.nextWord()) != null) {
                        pr = ngram.wordProb(word);
                        System.out.println(word+": "+pr);
                        total += pr;
                    }
                    if(btotal) System.out.println("=================\nTOTAL: "+total);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.RANK) {
                String ngramf = optionTester.testNgram(parser, ongram, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
                int length = optionTester.testInteger(parser, olength, 5);

                ngram = new Ngram(length);
                ngram.load(ngramf);


                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    Classificacio cls = new Classificacio();
                    Set<String> calculat = new HashSet<String>();
                    while ((word = txreader.nextWord()) != null) {
                        if(!calculat.contains(word)) {
                            calculat.add(word);
                            //cls.add(word,ngram.wordProb(word)*(word.length()+1));
                            cls.add(word,Math.pow(ngram.wordProb(word),1.0/(word.length()+1)));
                        }
                    }
                    ngram.printRanking(cls);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.SIZE) {
                String ngramf = optionTester.testNgram(parser, ongram, false, true, true);
	            int length = optionTester.testInteger(parser, olength, 5);
                ngram = new Ngram(length);
                ngram.load(ngramf);

                System.out.println("Model size: "+ngram.getSize());
            }
            if(action == Action.ORDER) {
                String ngramf = optionTester.testNgram(parser, ongram, false, true, true);
	            int length = optionTester.testInteger(parser, olength, 5);
                ngram = new Ngram(length);
                ngram.load(ngramf);

                System.out.println("Model order: "+ngram.order);
            }
        } catch (IllegalOptionValueException ex) {
            show_help();
        } catch (UnknownOptionException ex) {
            System.err.println(ex.getOptionName()+": Unknown option");
            show_help();
        }
    }

    public static void show_help() {
        System.err.println("Usage:");
        System.err.println("=========================");
        System.err.println("Model generation");
        System.err.println("\twords.Ngram -a train -d dictionary_file -c { corpus_file | - } -l ngram_length [-f] -n output_model_file");
        System.err.println("-------------------------");
        System.err.println("Prob. calculation");
        System.err.println("\twords.Ngram -a prob -n model_file [-t]");
        System.err.println("-------------------------");
        System.err.println("logLikelihood calculation");
        System.err.println("\twords.Ngram -a test -n model_file");
        System.err.println("-------------------------");
        System.err.println("Model size");
        System.err.println("\twords.Ngram -a size -n model_file");
        System.err.println("-------------------------");
        System.err.println("Model order");
        System.err.println("\twords.Ngram -a order -n model_file");
        System.err.println("-------------------------");
        System.err.println("Help");
        System.err.println("\twords.Ngram -?");
        System.err.println("=========================");
        // TO-DO explain options
    }

    @Override
    public int getSize() {
        return ngrams.size();
    }
}
