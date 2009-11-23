/*
 * Copyright (C) 2009
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.utils.Classificacio;
import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;
import words.utils.TextReader;
import words.utils.WordType;
import words.pst.*;

/**
 *
 * @author xavi
 */
public class PST extends Model {

    State I;
    double ratio;
    private static double EPSILON = 1e-5;

    public void setRatio(double d) {
        ratio = d;
    }

    public PST(double d) {
        I = new State("");
        ratio = d;
    }

    public PST() {
        I = new State("");
        ratio = 1;
    }

    public void addWord(String w) {
        w = this.BOW + w + this.EOW;
        int length = w.length();

        String pref;
        State current;

        for (int i = 0; i < length; ++i) {
            pref = w.substring(0, i + 1);
            current = I;
            I.visit();
            for (int j = i; j >= 0; --j) {
                current = current.safeNext(pref.charAt(j));
            }
        }
    }

    public int times(String w) {
        int ret = 0;

        State current = I;

        for (int i = w.length(); i > 0; --i) {
            current = current.next(w.charAt(i - 1));
            if (current == null) {
                break;
            }
        }
        if (current != null) {
            ret = current.getTimes();
        }

        return ret;
    }

    public void printTree() {
        I.printTree();
    }

    public double condProb(String w) {
        int t = times(w);
        double ret = EPSILON;

        if (t != 0) {
            String u = "";
            if (w.length() > 1) {
                u = w.substring(0, w.length() - 1);
            }
            ret = t / (double) times(u);
        }

        return ret;
    }

    private String context(String w) {
        return this.context(w,null);
    }

    private String context(String w, Character c) {

        String ret = "";
        int len = w.length();

        if (len > 0) {

            State act = I;
            int i = len - 1;

            for (; i >= 0; i--) {

                act = act.existingNext(w.charAt(i));

                if (act == null) {
                    break;
                }
            }

            ++i;

            ret = w.substring(i, len);

            if(c!=null)
                ret = (times(ret+c) > 0) ? ret : context(w.substring(1), c);
        }

        return ret;
    }

    public double wordProbDeb(String w) {
        double ret = 1;

        w = this.BOW + w + this.EOW;



        int len = w.length();
        String tmp = "";
        String ctxt;

        tmp += w.charAt(0);

        for (int i = 1; i < len; i++) {

            char c = w.charAt(i);

            ctxt = context(tmp,c);

            tmp = ctxt + c;
            double d = condProb(tmp);

            ret *= d;

            System.out.println(ctxt + " - " + c + " = " + d);
        }

        return ret;
    }

    @Override
    public double wordProb(String w) {
        double ret = 1;

        w = this.BOW + w + this.EOW;

        int len = w.length();
        String tmp = "" + w.charAt(0);

        for (int i = 1; i < len; i++) {
            char c =  w.charAt(i);
            tmp = context(tmp,c) + c;
            ret *= condProb(tmp);
        }

        return ret;
    }

    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {

        if (tag.equals("state")) {
            String stname = attributes.getValue("name");
            int tmes = Integer.parseInt(attributes.getValue("times"));
            boolean del = Boolean.parseBoolean(attributes.getValue("deleted"));

            State st = I;
            int len = stname.length();

            for (int i = len; i > 0; --i) {
                st = st.safeNext(stname.charAt(i - 1), false);
            }

            st.setTimes(tmes);
            if (del) {
                st.delete();
            }

        }
        if (tag.equals("pst")) {
            totalProb = Double.parseDouble(attributes.getValue("prob"));
        }

    }

    @Override
    public void save(String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<pst prob=\"" + totalProb + "\">");
            I.printXML(pw);
            pw.println("</pst>");

        } catch (IOException ex) {
            System.err.println("Error saving PST");
        } finally {
            pw.close();
        }
    }

    @Override
    public String split(String word) {
        return word;
    }

    @Override
    public void train(String filename, boolean verb) {
        if (verb) {
            super.train(filename, verb);
            printTree();

            System.out.println("===========================");

            //this.save("/home/xavi/Desktop/before.xml");
            while (prune()) {
                printTree();
                System.out.println("===========================");
            }
            //this.save("/home/xavi/Desktop/middle.xml");
            clean();
            printTree();
            System.out.println("===========================");
            //this.save("/home/xavi/Desktop/after.xml");
        } else {
            this.train(filename);
        }
    }

    @Override
    public void train(String filename) {
        super.train(filename);
        while (prune()) {
        }
        clean();
    }

    /**
     * Removes unaccessible states
     */
    private void clean() {
        Set<State> terminal = I.getTerminal();

        //System.out.println("BEFORE ANYTHING");
        //I.status("");

        I.prepareClean();

        //System.out.println("after prepareClean");
        //I.status("");

        Set<Character> alphabet = I.getChars();

        String name = null;

        for(State st : I.next.values()) {
            st.need();
        }

        for(State st : terminal) {
            name = st.getFullName();
            for(Character c : alphabet) {
                State needed = getPos(name+c);
                if(needed != null)
                    needed.need();
            }
        }

        //System.out.println("after needed");
        //I.status("");

        I.clean();

        //System.out.println("after cleaning");
        //I.status("");
    }

    /**
     *
     */
    public State getPos(String w) {
        State current = I;
        int len = w.length();

        for (int i = (len-1); i >= 0; --i) {
            current = current.next(w.charAt(i));
            if(current == null) break;
        }

        return current;
    }

    /**
     * Try to join as much unnecessary states as possible
     * @return success joining states
     */
    public boolean prune() {

        boolean ret = false;
        int red = 0;

        Set<Character> alphabet = I.next.keySet();
        int n = 0;
        for (Character c : alphabet) {
            n += I.next(c).getTimes();
        }

        double K = (2 * alphabet.size() + 4) * (Math.log(n) / Math.log(2)) * ratio;
        double sum = 0;

        Set<State> terminal = I.getTerminal();

        for (State t : terminal) {
            sum = 0;
            String u = t.getFullName();
            String w = u.substring(1, u.length());
            for (Character c : alphabet) {


                double uc = condProb(u + c);
                double wc = condProb(w + c);
                if ((uc > 0) && (wc > 0)) {
                    sum += uc * (Math.log(uc / wc) / Math.log(2)) * times(u);
                }
            }

            // si l'estat es irrelevant l'eliminem
            if (sum < K) {
                t.delete();
                ret = true;
                red++;
            }
        }

        return ret;
    }

    public int getSize() {
        return I.getParameterSize();
    }

    public double getOrder() {
        return Math.log(I.getNodes())/Math.log(I.getChars().size())+1;
    }

    public int getMaxDepth() {
        return I.getMaxDepth();
    }

    public void deb_train(String f) {
        super.train(f);
        printTree();
        while (prune()) {
        }
        //printTree();
        System.out.println("SIZE before: "+getSize());
        clean();
        System.out.println("SIZE after: "+getSize());
        //printTree();
    }

    public void allProbs(String w) {
        String ctxt = context(w);

        Set<Character> cs = I.getChars();

        for (Character c : cs) {
            System.out.println(ctxt + c + ": " + times(ctxt + c));
        }
    }

    public static void main(String[] args) {
        try {
            PST pst;

            CmdOptionTester optionTester = new CmdOptionTester();
            CmdOptions parser = new CmdOptions();

            // train, dev
            Option oaction = parser.addStringOption('a', "action");
            // corpus file
            Option ocorpus = parser.addStringOption('c', "corpus");
            // dictionary file
            Option odictionary = parser.addStringOption('d', "dictionary");
            // ngram file
            Option opst = parser.addStringOption('p', "pst");
            // force overwriting
            Option over = parser.addBooleanOption('f', "force");
            // total prob
            Option ototal = parser.addBooleanOption('t',"total");
            // prune parameter
            Option oratio = parser.addDoubleOption('r', "prune");

            // help
            Option help = parser.addBooleanOption('?',"help");
            parser.parse(args,Locale.ENGLISH);

            if(optionTester.testBoolean(parser,help)) {
                show_help();
                System.exit(0);
            }

            Action action = optionTester.testAction(parser, oaction, Ngram.actions);

            if (action == Action.TRAIN) {
            	String corpus = optionTester.testCorpus(parser, ocorpus,true);
                boolean force = optionTester.testBoolean(parser, over);
                String dict = optionTester.testDict(parser, odictionary, false, true, true);
                String pstf = optionTester.testPST(parser, opst, force, false, true);
                double rt = optionTester.testDouble(parser, oratio, 1);

                pst = new PST(rt);
                pst.train(corpus);
                Dictionary dictionary = new Dictionary();
                dictionary.load(dict);

                Set<String> wds = dictionary.getWords();

                for (String wd : wds) {
                    pst.totalProb += pst.wordProb(wd);
                }

                pst.save(pstf);
            }
            if (action == Action.TEST) {
                String pstf = optionTester.testPST(parser, opst, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
                
                pst = new PST();
                pst.load(pstf);

                pst.test(corpus);
                System.out.println("Total logLikelihood: " + pst.lastTest.getFirst());
                System.out.println("Total words: " + pst.lastTest.getSecond());
                System.out.println("======================================\n");
                System.out.println("logLikelihood per word: " + (pst.lastTest.getFirst()/pst.lastTest.getSecond()));
            }
            if(action == Action.PROB) {
                String pstf = optionTester.testPST(parser, opst, false, true, true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                double total = 0, pr = 0;
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
	        

                pst = new PST();
                pst.load(pstf);
                
                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    while ((word = txreader.nextWord()) != null) {
                        pr = pst.wordProb(word);
                        System.out.println(word+": "+pr);
                        total += pr;
                    }
                    if(btotal) System.out.println("=================\nTOTAL: "+total);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.RANK) {
                String pstf = optionTester.testPST(parser, opst, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);

                

                pst = new PST();
                pst.load(pstf);

                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    Classificacio cls = new Classificacio();
                    Set<String> calculat = new HashSet<String>();
                    while ((word = txreader.nextWord()) != null) {
                        if(!calculat.contains(word)) {
                            calculat.add(word);
                            cls.add(word,pst.wordProb(word)*(word.length()+1));
                        }
                    }
                    pst.printRanking(cls);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.SIZE) {
                String pstf = optionTester.testPST(parser, opst, false, true, true);
	            
                pst = new PST();
                pst.load(pstf);

                System.out.println("Model size: "+pst.getSize());
            }
            if(action == Action.ORDER) {
                String pstf = optionTester.testPST(parser, opst, false, true, true);
	        
                pst = new PST();
                pst.load(pstf);

                System.out.println("Model order: "+pst.getOrder());
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
        System.err.println("\twords.PST -a train -d dictionary_file -c { corpus_file | - } -r prune_ratio [-f] -p output_model_file");
        System.err.println("-------------------------");
        System.err.println("Prob. calculation");
        System.err.println("\twords.PST -a prob -p model_file [-t]");
        System.err.println("-------------------------");
        System.err.println("logLikelihood calculation");
        System.err.println("\twords.PST -a test -p model_file");
        System.err.println("-------------------------");
        System.err.println("Model size");
        System.err.println("\twords.PST -a size -p model_file");
        System.err.println("-------------------------");
        System.err.println("Model order");
        System.err.println("\twords.PST -a order -p model_file");
        System.err.println("-------------------------");
        System.err.println("Help");
        System.err.println("\twords.PST -?");
        System.err.println("=========================");
        // TO-DO explain options
    }

    public static void main4(String [] args) {
        PST v = new PST();

        v.load(args[0]);

        System.out.println("Before "+v.getSize());

        for(int i=1;i<args.length;i++) {
            System.out.println(args[i]+": "+v.wordProb(args[i]));
        }
        v.save(args[0]+".expanded");
        v.clean();
        v.save(args[0]);
        System.out.println("After  "+v.getSize());


        for(int i=1;i<args.length;i++) {
            System.out.println(args[i]+": "+v.wordProb(args[i]));
        }

        v.save(args[0]+"opt");
    }

    public static void main3(String[] args) {
        PST v = new PST();

        v.load("/home/xavi/Documents/tesina/tests_en/vlmc.xml");

        System.out.println("------------ TINU-x ------------");
        v.allProbs("tinu");
        System.out.println("------------- INU-x ------------");
        v.allProbs("inu");
        System.out.println("--------------------------------");

        System.out.println("Nuclear" + v.wordProbDeb("nuclear"));
        System.out.println("Nuclear" + v.wordProb("nuclear"));
        System.out.println("Antinuclear" + v.wordProbDeb("antinuclear"));
        System.out.println("Antinuclear" + v.wordProb("antinuclear"));
    }

    public static void main2(String[] args) {
        PST v = new PST();

        v.train("/home/xavi/Documents/tesina/svn/trunk/tests/ab/ab.txt",true);
        System.out.println("a: " + v.wordProbDeb("a"));
        System.out.println("ab: " + v.wordProbDeb("ab"));
        System.out.println("abb: " + v.wordProbDeb("abb"));
    }

    public static void main_depth() {
        PST v = new PST();
        PST x = new PST(0.1);

        v.load("/home/xavi/Documents/tesina/svn/trunk/models/en/VLMC.100000000.xml");
        x.load("/home/xavi/Documents/tesina/svn/trunk/models/en/VLMC_01.100000000.xml");

        System.out.println("Normal: "+v.getMaxDepth());
        System.out.println("Plus: "+x.getMaxDepth());
    }

    public static void main_en(String[] args) {
        Dictionary d = new Dictionary();
        Ngram n = new Ngram(1);
        PST v = new PST();

        d.load("/home/xavi/Documents/tesina/svn/trunk/models/en/Diccionari.10.xml");
        n.load("/home/xavi/Documents/tesina/svn/trunk/models/en/Ngram1.10.xml");
        v.load("/home/xavi/Documents/tesina/svn/trunk/models/en/VLMC.10.xml");

        Set<String> s = d.getDict().keySet();

        for(String word : s) {
            System.out.println("word: " + word);
            //System.out.println(v.wordProbDeb(word));
            System.out.println(n.wordProb(word));
            System.out.println(v.wordProb(word));
            System.out.println("======================");
        }
    }
}

