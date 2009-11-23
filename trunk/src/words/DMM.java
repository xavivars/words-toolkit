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

import words.dmm.State;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import words.utils.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;

/**
 *
 * @author xavi
 */
public class DMM extends Model {

    private ArrayList<State> estats;
    private Set<String> alfabet;
    private final int condition1;
    private final int condition2;
    private Unit unit = Unit.CHAR;
    private State first;
    private State current;
    private State construct;
    private HashMap<Pair<String, String>, Pair<String, Int>> connections;
    private HashMap<String, String> fallbacks;
    private HashMap<String, State> hashstats;
    private boolean backoff;
    private boolean start;
    private static double EPSILON = 1e-5;

    /**
     * Default constructor
     */
    public DMM() {
        this(5);
    }

    /**
     * Constructor
     * @param i Split limit
     */
    public DMM(int i) {

        first = new State("fallback");
        first.addMasterConnection(first);
        hashstats = new HashMap<String, State>();
        hashstats.put("fallback", first);
        estats = new ArrayList<State>();
        estats.add(first);
        current = first;
        backoff = false;

        connections = new HashMap<Pair<String, String>, Pair<String, Int>>();
        fallbacks = new HashMap<String, String>();

        this.condition1 = i;
        this.condition2 = i*15;
        alfabet = new HashSet<String>();
    }

    /**
     * Clear DMC
     */
    public void clear() {
        first = null;
        estats.clear();
        current = null;
        connections.clear();
        fallbacks.clear();
    }

    /**
     * Unit of the model
     * @param u Char o Syllable
     */
    public void setUnit(Unit u) {
        this.unit = u;
    }

    /**
     * sets backoff
     */
    public void setBackOff(boolean b) {
        backoff = b;
    }

    /**
     * sets start char
     */
     public void setStart(boolean b) {
         start = b;
     }

    /**
     * The number of states in the DMC
     * @return the size of the model.
     */
    public int getStatesSize() {
        return estats.size();
    }

    /**
     * The order of Markov model associated to the model
     */
    public double getOrder() {
        return Math.log(estats.size()) / Math.log(alfabet.size());
    }

    /**
     * The current state of the DMC
     * @return current state
     */
    public State getCurrentState() {
        return current;
    }

    /**
     * Next state given a tag
     * @param tag
     * @return next state
     */
    public State nextState(String tag) {
        current = current.next(tag);

        return current;
    }

    /**
     * Next state given a tag, during training
     * It can split current state in two new states
     * @param tag
     * @return next state
     */
    public State nextStateTraining(String tag) {

        State next = null;
        State st = null;

        int numTrans = 0;
        int otherTrans = 0;
        int totalTrans = 0;
        double ratio = 0;

        current.addTransition(tag);
        next = current.next(tag);

        // transitions from current state given a tag
        numTrans = current.getTransitions(tag);

        // total transitions from current state
        totalTrans = next.getAllTransitions();

        otherTrans = totalTrans - numTrans;

        // split current state if numTrans is greater than limit
        // and otherTrans is also greater than limit
        if ((numTrans > condition1) && (otherTrans > condition2)) {

            st = new State(next.getCloneName());

            // overwrite connection with tag to new state
            current.addConnection(tag, st, true);

            // propagate all connection
            st.addMasterConnection(next.getMasterConnection().getTo());
            for (Connection cn : next.allConnections()) {
                st.addConnection(cn.getTag(), cn.getTo());
            }

            // split next state transition counter to new state
            ratio = (double) (((double) numTrans) / ((double) totalTrans));
            st.copyCounters(next, ratio);
            estats.add(st);
            hashstats.put(st.getName(), st);

            // next state is now the new one
            next = st;
        }

        if(this.backoff && !current.hasConnection(tag))
            next = current.next(tag).next(tag);
        return next;
    }

    /**
     * Train model given a word
     * @param str word to train
     */
    public void addWord(String str) {
        int i = 0;
        current = first;
        str = fullWord(str);
        for (char c : str.toCharArray()) {
            alfabet.add(""+c);
            current = nextStateTraining("" + c);
        }
    }

    /**
     * Loads a DMC from an XML
     * @param uri
     * @param localName
     * @param tag
     * @param attributes
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {
        if (tag.equals("dmc")) {
            this.clear();
        }
        if (tag.equals("state")) {
            String stname = attributes.getValue("name");
            String fallstate = attributes.getValue("fallback");

            construct = new State(stname);

            if (first == null) {
                first = construct;
                current = construct;
            }

            fallbacks.put(stname, fallstate);
            estats.add(construct);
            hashstats.put(stname, construct);
        }
        if (tag.equals("connection")) {
            String cntag = attributes.getValue("tag");
            alfabet.add(cntag);
            String cncounter = attributes.getValue("counter");
            String cnto = attributes.getValue("to");
            String stname = construct.getName();
            Int i = new Int(Integer.parseInt(cncounter));
            connections.put(new Pair(stname, cntag), new Pair(cnto, i));
        }
    }

    /**
     * Creates a markov model from data in hashmaps
     */
    public void makeMarkov() {
        //System.out.println("Estats: " + estats.size());
        State st = null;
        State to = null;

        // fallback connections
        Set<String> fallset = fallbacks.keySet();
        //System.out.println("Fallbacks: " + fallset.size());
        Iterator<String> fallit = fallset.iterator();

        while (fallit.hasNext()) {
            String str = fallit.next();
            String strto = fallbacks.get(str);

            st = findState(str);
            to = findState(strto);

            if ((st != null) && (to != null)) {
                st.addMasterConnection(to);
            }
        }

        // normal connections
        Set<Pair<String, String>> set = connections.keySet();
        //System.out.println("Connections: " + set.size());
        Iterator<Pair<String, String>> it = set.iterator();
        while (it.hasNext()) {
            Pair<String, String> pr = it.next();
            Pair<String, Int> sc = connections.get(pr);

            String stname = pr.getFirst();
            String cntag = pr.getSecond();
            String cnto = sc.getFirst();
            Int cncounter = sc.getSecond();

            st = findState(stname);
            to = findState(cnto);
            if ((st != null) && (to != null)) {
                st.addConnection(cntag, to);
                st.setTransitions(cntag, cncounter);
            }
        }

        for (State est : estats) {
            est.setMaxConnections(alfabet.size()+1);
        }
    }

    /**
     * @param word
     * @return splitted word
     */
    public String split(String word) {

        StringBuilder ret = new StringBuilder(word.length() + 2);
        int i = 0;
        

        current = first;
        int ns = word.length();
        word = fullWord(word);
        int max = word.length() - 1;
        ns = max - ns;
        for (char c : word.toCharArray()) {

            if ((i > ns && i < max) && (!current.hasConnection(""+c))) {
                ret.append('-');
            }

            String after = "";
            if(backoff && !current.hasConnection(""+c)) {
                current = nextState("" + c);
                if(!current.hasConnection(""+c)&&(i+1)<max)
                    after = "-";
            }

            current = nextState("" + c);

            ret.append(c+after);
            ++i;
        }

        return ret.toString().replaceAll("--","-");
    }

    /**
     * @param word a string
     * @return the probability of the word
     */
    public double wordProb(String word) {
        double ret = 1;

        if(backoff) {
            int i = 0;
            current = first;
            word = fullWord(word);
            for (char c : word.toCharArray()) {
                if(!alfabet.contains(""+c)) ret *= EPSILON;

                if(!current.hasConnection(""+c)) {
                    current = nextState("" + c);
                }
                ret *= current.prob("" + c);
                current = nextState("" + c);
            }
        } else {
            int i = 0;
            current = first;
            word = fullWord(word);
            for (char c : word.toCharArray()) {
                if(!alfabet.contains(""+c)) ret *= EPSILON;
                
                ret *= current.prob("" + c);
                current = nextState("" + c);
            }
        }
        return ret;
    }

    /**
     * @param stname name of State
     * @return State given a name
     */
    public State findState(String stname) {
        return hashstats.get(stname);
    }

    /**
     * Saves DMC into an XML
     * @param filename
     */
    public void save(String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

            pw.println("<dmc prob=\"" + totalProb + "\">");

            for (State st : this.estats) {
                st.writeMarkov(pw);
            }

            pw.println("</dmc>");

        } catch (IOException ex) {
            System.err.println("Error saving DMC");
        } finally {
            pw.close();
        }
    }

    public void print() {

        System.out.println("<dmc>");
        for (State st : this.estats) {
            st.print();
        }
        System.out.println("</dmc>");
    }

    /**
     * @return total amount of parameters of the model
     */
    public int getSize() {
        int total = 0;

        for (State st : estats) {
            total += st.parameterSize();
        }

        return total;
    }

    @Override
    public void load(String filename) {
        getText(filename);
        makeMarkov();
        if(first.hasConnection("#"))
            start = true;
    }

    private String fullWord(String word) {
        if(this.start)
             return word = "#" + word + "$";
        else
             return word = word + "$";
    }

    public static void main(String[] args) {
        try {
            DMM dmm;

            CmdOptionTester optionTester = new CmdOptionTester();
            CmdOptions parser = new CmdOptions();

            // train, dev
            Option oaction = parser.addStringOption('a', "action");
            // corpus file
            Option ocorpus = parser.addStringOption('c', "corpus");
            // dictionary file
            Option odictionary = parser.addStringOption('d', "dictionary");
            // ngram file
            Option odmm = parser.addStringOption('k', "dmm");
            // force overwriting
            Option over = parser.addBooleanOption('f', "force");
            // total prob
            Option ototal = parser.addBooleanOption('t', "total");
            // show split
            Option osplit = parser.addBooleanOption('s', "split");
            // clone limit
            Option olimit = parser.addIntegerOption('l',"limit");
            // backoff model
            Option oback = parser.addBooleanOption('b',"backoff");
            // backoff model
            Option ostart = parser.addBooleanOption('#',"start");

            // help
            Option help = parser.addBooleanOption('?', "help");
            parser.parse(args);

            if (optionTester.testBoolean(parser, help)) {
                show_help();
                System.exit(0);
            }

            Action action = optionTester.testAction(parser, oaction, HMM.actions);

            if (action == Action.TRAIN) {
                String corpus = optionTester.testCorpus(parser, ocorpus, true);
                boolean force = optionTester.testBoolean(parser, over);
                boolean strt = optionTester.testBoolean(parser, ostart);
                boolean back = optionTester.testBoolean(parser, oback);
                String dict = optionTester.testDict(parser, odictionary, false, true, true);
                String dmmf = optionTester.testHMM(parser, odmm, force, false, true);
                int limit = optionTester.testInteger(parser, olimit,5);
                
                dmm = new DMM(limit);
                dmm.setStart(strt);
                dmm.setBackOff(back);

                dmm.train(corpus);

                Dictionary dictionary = new Dictionary();
                dictionary.load(dict);

                

                Set<String> wds = dictionary.getWords();

                for (String wd : wds) {
                    dmm.totalProb += dmm.wordProb(wd);
                }

                dmm.save(dmmf);
            }
            if (action == Action.TEST) {
                String dmmf = optionTester.testHMM(parser, odmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus, true);
                boolean back = optionTester.testBoolean(parser, oback);

                dmm = new DMM();
                dmm.load(dmmf);

                dmm.setBackOff(back);

                dmm.test(corpus);
                System.out.println("Total logLikelihood: " + dmm.lastTest.getFirst());
                System.out.println("Total words: " + dmm.lastTest.getSecond());
                System.out.println("======================================\n");
                System.out.println("logLikelihood per word: " + (dmm.lastTest.getFirst()/dmm.lastTest.getSecond()));
            }
            if (action == Action.PROB) {
                String dmmf = optionTester.testHMM(parser, odmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus, true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                boolean split = optionTester.testBoolean(parser, osplit);
                boolean back = optionTester.testBoolean(parser, oback);

                double total = 0, pr = 0;

                dmm = new DMM();
                dmm.load(dmmf);
                dmm.setBackOff(back);
                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    while ((word = txreader.nextWord()) != null) {
                        pr = dmm.wordProb(word);
                        if (split) {
                            word = dmm.split(word);
                        }
                        System.out.println(word + ": " + pr);
                        total += pr;
                    }
                    if (btotal) {
                        System.out.println("=================\nTOTAL: " + total);
                    }
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if (action == Action.RANK) {
                String dmmf = optionTester.testHMM(parser, odmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus, true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                boolean split = optionTester.testBoolean(parser, osplit);
                boolean back = optionTester.testBoolean(parser, oback);
                 double total = 0, pr = 0;

                dmm = new DMM();
                dmm.load(dmmf);
                dmm.setBackOff(back);
                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    Classificacio cls = new Classificacio();
                    Set<String> calculat = new HashSet<String>();
                    while ((word = txreader.nextWord()) != null) {
                        if(!calculat.contains(word)) {
                            calculat.add(word);
                            cls.add(word,dmm.wordProb(word)*(word.length()+1));
                        }
                    }
                    dmm.printRanking(cls);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if (action == Action.SIZE) {
                String dmmf = optionTester.testNgram(parser, odmm, false, true, true);

                dmm = new DMM();
                dmm.load(dmmf);

                System.out.println("Model size: " + dmm.getSize());
            }
            if (action == Action.ORDER) {
                String dmmf = optionTester.testNgram(parser, odmm, false, true, true);

                dmm = new DMM();
                dmm.load(dmmf);

                System.out.println("Model order: " + dmm.getOrder());
            }
        } catch (IllegalOptionValueException ex) {
            show_help();
        } catch (UnknownOptionException ex) {
            System.err.println(ex.getOptionName() + ": Unknown option");
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
}
