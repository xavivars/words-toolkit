package words;

import java.io.*;
import words.utils.*;
import words.hmm.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;

/**
 * Restart Hidden Markov Model
 * A HMM where transitions are symbol-driven and
 * every state can be reached with a single string or word (its label).
 */
public class HMM extends HMMPre {

    /**
     * Default constructor
     */
    public HMM() {
		super();
    }

    /**
     * Size constructor
     * @parameter s max-size of string at build stage
     */
    public HMM(int s) {
        super(s);
    }


    /**
     * Add a string N times to the HMM and update transition counters
     * @param word the string to be added
     * @param t times the string has to be added
     */
    @Override
    public void addWord(String word, int t) {
        int len = word.length();
        String aux = "";
        // Pes relatiu per a cada paraula:
        // tenim 2^(len-1) maneres de dividir una paraula
        //double weight = Math.pow(2, len - 1);
        // normalitzem per a totes les paraules
        //weight = weight / Math.pow(2,len);
        double weight = 0.5 * t;


        // hi ha 2^(len-1) * (len+2) segments possibles
        I.addTimes(weight * (len + 2));
        I.addRestart(EOW, weight);
        for (int i = 0; i < len; ++i) {
            State q = I;
            double w = (i == 0) ? 2 * weight : weight;
            for (int j = i; j < len; ++j) {
                Character c = word.charAt(j);
                w *= 0.5;
                q.addRestart(c, w);
                aux = q.getName();
                q = safeNext(q, c);
                q.setName(aux + c);
                q.addTimes(w);
            }
            q.addRestart(EOW, w);
        }
    }

    /**
     * The probability that the HMM generates a word
     * @param word a string
     * @return the probabilty that the HMM generates word
     */
    @Override
    public double wordProb(String word) {
        double p = 0.0;
        Value<State> alpha = initial(1.0);
        for (int t = 0; t < word.length(); ++t) {
            alpha = forward(alpha, word.charAt(t));
        }
        for (State q : alpha.keySet()) {
            p += alpha.getValue(q) * q.prRestart(EOW);
        }
        if(p==0) {
            p = Math.pow(EPSILON, word.length()+1);
        }
        return p;
    }

    /**
     * Training phase: DRAFT VERSION !!!!!!
     */
    @Override
    public void trainWord(String word,
            Value<State> times,
            HashMap<State, Value<Character>> restart) {
        int len = word.length();
        double wordProb;
        Value<State> alpha = initial(1.0);
        Value<State> beta;
        ArrayList<Value<State>> alphas = new ArrayList<Value<State>>();

        alphas.add(alpha); //System.out.println("alpha(0)= \n" + alpha);
        for (int t = 0; t < len; ++t) {
            Character c = word.charAt(t);
            alpha = forward(alpha, c); //System.out.println("alpha(" + (t+1) + ")= \n" + alpha);
            alphas.add(alpha);
        }
        beta = initial(1.0);
        wordProb = alpha.get(I);//System.out.println("wordProb="+wordProb); //print();
        for (int t = len - 1; t >= 0; --t) {
            Character c = word.charAt(t);   // System.out.println("beta(" + (t+1) + ")= \n" + beta);
            alpha = alphas.get(t);
            for (State i : alpha.keySet()) {
                double alphai = alpha.get(i);
                State j = i.next(c);
                double restartValue = beta.containsKey(I) ? clip(alphai * i.prRestart(c) * beta.get(I) / wordProb) : 0;
                if (j != null && beta.containsKey(j)) {
                    double nextValue =
                            clip(alphai * i.prNext(c) * beta.get(j) / wordProb);  //System.out.println(i+"->"+j+"("+beta.get(j)+")");
                    times.addValue(j, nextValue); //System.out.println(j + " addTimes = " + nextValue);
                }
                restart.get(i).addValue(c, restartValue); //System.out.println(i + " addRestart["+c+"]=" + restartValue);
                times.addValue(I, restartValue);
            }
            if (t > 0) {  // compute new beta
                Value<State> output = new Value<State>();
                for (State i : alpha.keySet()) {
                    State j = i.next(c);
                    if (j != null) {
                        output.addValue(i, beta.getValue(j) * i.prNext(c));
                    }
                    output.addValue(i, beta.getValue(I) * i.prRestart(c));
                }
                beta = output;
            }
        }
    }

    /**
     * EM algorithm
     */
    @Override
    public void expectationMaximization(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        Value<State> times = new Value<State>();
        HashMap<State, Value<Character>> restart =
                new HashMap<State, Value<Character>>();
        for (State s : Q) {
            restart.put(s, new Value<Character>());
        }
        try {
            while ((word = txreader.nextWord()) != null) {
                trainWord(word + EOW, times, restart);
            }
            for (State q : Q) {
                if (times.containsKey(q)) {
                    q.setTimes(times.get(q));
                    q.setRestart(restart.get(q));
                } else {
                    q.clean();
                }
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
    }

    /**
     * Viterbi
     * @param word a string
     * @return the best split of word (best path)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String split(String word) {
        word = word+EOW;
        Value<State>[] alpha = new Value[2];
        HashMap<State, String>[] split = new HashMap[2];
        alpha[0] = initial(1.0);
        split[0] = new HashMap<State, String>();
        split[0].put(I, "");
        for (int t = 0; t < word.length(); ++t) {
            Character c = word.charAt(t);
            alpha[1 - t % 2] = new Value<State>();
            split[1 - t % 2] = new HashMap<State, String>();
            for (State q : alpha[t % 2].keySet()) {
                if (q.allows(c)) {
                    String s = split[t % 2].get(q);
                    double val = alpha[t % 2].getValue(q);
                    State next = q.next(c);
                    if (next != null) {
                        alpha[1 - t % 2].setValue(next, val * q.prNext(c));
                        split[1 - t % 2].put(next, s + c);
                    }
                    if (val * q.prRestart(c) > alpha[1 - t % 2].getValue(I)) {
                        alpha[1 - t % 2].setValue(I, val * q.prRestart(c));
                        if (t + 2 < word.length()) {
                            split[1 - t % 2].put(I, s + c + "-");
                        } else {
                            split[1 - t % 2].put(I, s + c);
                        }
                    }
                } else {
                    if(q==I) {
                        String s = split[t % 2].get(q);
                        double val = alpha[t % 2].getValue(q);
                        if (val * EPSILON > alpha[1 - t % 2].getValue(I)) {
                            alpha[1 - t % 2].setValue(I, val * EPSILON);
                            if (t + 2 < word.length()) {
                                split[1 - t % 2].put(I, s + c + "-");
                            } else {
                                split[1 - t % 2].put(I, s + c);
                            }
                        }
                    }
                }
            }
        }
        return BOW+split[word.length() % 2].get(I);
    }

    /**
     * Print HMM states and probabilities to an XML
     */
    @Override
    public void save(String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<hmm prob=\""+totalProb+"\">");
            I.printXML(pw);
            pw.println("</hmm>");

        } catch (IOException ex) {
            System.err.println("Error saving HMM");
        } finally {
            pw.close();
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String tag, final Attributes attributes) throws SAXException {
        if (tag.equals("state")) {
            String stname = attributes.getValue("name");
            double tmes = Double.parseDouble(attributes.getValue("times"));

            State st = I;
            for (int i = 0; i < stname.length(); ++i) {
                st = safeNext(st, stname.charAt(i));
            }

            st.addTimes(tmes);
            st.setName(stname);
            loaded = st;

        }
        if (tag.equals("connection")) {

            Character c = attributes.getValue("char").charAt(0);
            Double d = Double.parseDouble(attributes.getValue("restart"));
            loaded.addRestart(c, d);
        }
        if (tag.equals("hmm")) {
            totalProb = Double.parseDouble(attributes.getValue("prob"));
        }
    }

    public static void main(String [] args) {
                try {
            HMM hmm;

            CmdOptionTester optionTester = new CmdOptionTester();
            CmdOptions parser = new CmdOptions();

            // train, dev
            Option oaction = parser.addStringOption('a', "action");
            // corpus file
            Option ocorpus = parser.addStringOption('c', "corpus");
            // dictionary file
            Option odictionary = parser.addStringOption('d', "dictionary");
            // ngram file
            Option ohmm = parser.addStringOption('h', "hmm");
            // Baum-Welch iterations
            Option oiters = parser.addIntegerOption('i', "iterations");
            // reestimation
            Option oreest = parser.addBooleanOption('r', "reestimate");
            // force overwriting
            Option over = parser.addBooleanOption('f', "force");
            // total prob
            Option ototal = parser.addBooleanOption('t',"total");
            // show split
            Option osplit = parser.addBooleanOption('s',"split");

            // help
            Option help = parser.addBooleanOption('?',"help");
            parser.parse(args);

            if(optionTester.testBoolean(parser,help)) {
                show_help();
                System.exit(0);
            }

            Action action = optionTester.testAction(parser, oaction,HMM.actions);

            if (action == Action.TRAIN) {
            	String corpus = optionTester.testCorpus(parser, ocorpus,true);
                boolean force = optionTester.testBoolean(parser, over);
                String dict = optionTester.testDict(parser, odictionary, false, true, true);
                String hmmf = optionTester.testHMM(parser, ohmm, force, false, true);
                int iters = optionTester.testInteger(parser, oiters, 3);

                hmm = new HMM();
                hmm.train(corpus);

                hmm.retrain(corpus,iters);
                hmm.clean();
                if(optionTester.testBoolean(parser, oreest)) {
                    
                    hmm.retrain(corpus,iters);
                    hmm.clean();
                }

                Dictionary dictionary = new Dictionary();
                dictionary.load(dict);

                Set<String> wds = dictionary.getWords();

                for (String wd : wds) {
                    hmm.totalProb += hmm.wordProb(wd);
                }

                hmm.save(hmmf);
            }
            if (action == Action.TEST) {
                String hmmf = optionTester.testHMM(parser, ohmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
                hmm = new HMM();
                hmm.load(hmmf);

                hmm.test(corpus);
                System.out.println("Total logLikelihood: " + hmm.lastTest.getFirst());
                System.out.println("Total words: " + hmm.lastTest.getSecond());
                System.out.println("======================================\n");
                System.out.println("logLikelihood per word: " + (hmm.lastTest.getFirst()/hmm.lastTest.getSecond()));
            }
            if(action == Action.PROB) {
                String hmmf = optionTester.testHMM(parser, ohmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                boolean split = optionTester.testBoolean(parser, osplit);
                double total = 0, pr = 0;

                hmm = new HMM();
                hmm.load(hmmf);
                String word;
                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    while ((word = txreader.nextWord()) != null) {
                        pr = hmm.wordProb(word);
                        if(split)
                            word = hmm.split(word);
                        System.out.println(word+": "+pr);
                        total += pr;
                    }
                    if(btotal) System.out.println("=================\nTOTAL: "+total);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.RANK) {
                String hmmf = optionTester.testHMM(parser, ohmm, false, true, true);
                String corpus = optionTester.testCorpus(parser, ocorpus,true);
                boolean btotal = optionTester.testBoolean(parser, ototal);
                boolean split = optionTester.testBoolean(parser, osplit);
                double total = 0, pr = 0;

                hmm = new HMM();
                hmm.load(hmmf);
                String word;

                try {
                    TextReader txreader = new TextReader(corpus, WordType.LETTERS);
                    Classificacio cls = new Classificacio();
                    Set<String> calculat = new HashSet<String>();
                    while ((word = txreader.nextWord()) != null) {
                        if(!calculat.contains(word)) {
                            calculat.add(word);
                            cls.add(word,hmm.wordProb(word)*(word.length()+1));
                        }
                    }
                    hmm.printRanking(cls);
                } catch (java.io.IOException x) {
                    System.err.println(x);
                }
            }
            if(action == Action.SIZE) {
                String hmmf = optionTester.testNgram(parser, ohmm, false, true, true);
	            
                hmm = new HMM();
                hmm.load(hmmf);

                System.out.println("Model size: "+hmm.getSize());
            }
            if(action == Action.ORDER) {
                String hmmf = optionTester.testNgram(parser, ohmm, false, true, true);

                hmm = new HMM();
                hmm.load(hmmf);

                System.out.println("Model order: "+hmm.getOrder());
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
}