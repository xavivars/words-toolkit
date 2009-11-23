package words;

import java.io.*;
import words.utils.*;
import words.hmm.*;
import java.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import words.Run.Mode;

/**
 * Restart Hidden Markov Model
 * A HMM where transitions are symbol-driven and
 * every state can be reached with a single string or word (its label).
 */
public class HMMPre extends Model {

    /** Set of states */
    protected HashSet<State> Q;
    /** Initial (and final) state */
    protected State I;
    /** Precission for real values */
    final public static double EPSILON = 1e-20;
    /** Max-string-size  at build stage **/
    protected  int length;
    /** State for loading */
    protected State loaded;
    protected  int stloaded = 0;

    public final int DEFAULT_ITERATIONS = 3;


    /**
     * Default constructor
     */
    public HMMPre() {
        Q = new HashSet<State>();
        I = new State();
        I.setName("");
        Q.add(I);
        length = -1;
    }

    /**
     * Size constructor
     * @parameter s max-size of string at build stage
     */
    public HMMPre(int s) {
        this();
        if (s > 0) {
            length = s;
        }
    }

    /**
     * The number of states in the HMM
     * @return the size of the model.
     */
    public double getOrder() {
        return Math.log(Q.size())/Math.log(I.alphabetSize());
    }


    /**
     * @param word the string characterizing a state
     * @return the State labelled with this word
     */
    private State state(String word) {
        State q = I;
        for (int t = 0; t < word.length(); ++t) {
            q = q.next(word.charAt(t));
        }
        return q;
    }

    /**
     * @param q a state in the model
     * @param c an output symbol 
     * @return the destination state when output is c  
     */
    protected State safeNext(State q, Character c) {
        State next = q.next(c);
        if (next == null) {
            next = new State();
            Q.add(next);
            q.setNext(c, next);
        }
        return next;
    }

    /**
     * @param input a set of states
     * @param c a character
     * @return the set of states reached from input when the HMM outputs c
     */
    private Set<State> delta(Set<State> input, Character c) {
        HashSet<State> output = new HashSet<State>();
        for (State q : input) {
            State next = q.next(c);
            if (next != null) {
                output.add(next);
            }
            if (q.allows(c)) {
                output.add(I);
            }
        }
        return output;
    }

    /**
     * @param word output string 
     * @return the set of states reached from the intial state
     *  when the HMM outputs word
     */
    public Set<State> delta(String word) {
        Set<State> front = new HashSet<State>();
        front.add(I);
        for (int t = 0; t < word.length(); ++t) {
            Character c = word.charAt(t);
            front = delta(front, c);
        }
        return front;
    }

    /**
     * Add a string N times to the HMM and update transition counters
     * @param word the string to be added
     * @param t times the string has to be added
     */
    public void addWord(String word, int t) {
        int len = word.length();
        double weight = Math.pow(0.5, len - 1);
        String aux = "";
        for (int i = 0; i < len; ++i) {
            State q = I;
            for (int j = i; j < len - 1; ++j) {
                Character c = word.charAt(j);
                q.addRestart(c, weight * t);
                I.addTimes(weight * t);
                aux = q.getName();
                q = safeNext(q, c);
                q.setName(aux + c);
                q.addTimes(weight * (len - 1 - j) * t);
            }
            q.addRestart(word.charAt(len - 1), weight * t);
            I.addTimes(weight * t);
        }
        I.addRestart(EOW, 1.0 * t);
        I.addTimes(1.0 * t);
    }

    /**
     * Add a string to the HMM and update transition counters
     * @param word the string to be added
     */
    public void addWord(String word) {
        this.addWord(word, 1);
    }

    /**
     * Add all words from reader to HMM
     * @param fileName the name of the input file
     */
    public void readRepeat(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERSorDIGITS);
        String word;
        int repeat;
        try {
            while ((word = txreader.nextWord()) != null) {
                repeat = Integer.parseInt(word);
                word = txreader.nextWord();
                addWord(word, repeat);
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
    }

    /**
     * Add all words in one file to HMM
     * @param fileName the name of the input file
     */
    public void retrain(String filename,int iters,boolean verb) {
        for(int i = 1; i <= iters; i++) {
            if(verb) System.out.print("  "+i+"  ");
            expectationMaximization(filename);
        }
        I.repair();
    }


    /**
     * Add all words in one file to HMM
     * @param fileName the name of the input file
     */
    public void retrain(String filename,int iters) {
        for(int i = 1; i <= iters; i++) {
            expectationMaximization(filename);
        }
        I.repair();
    }

    @Override
    public void train(String filename) {
        super.train(filename);
        retrain(filename,DEFAULT_ITERATIONS);
        clean();
    }

    public double logLikelihood(String fileName, double base) {
        double ret = logLikelihood(fileName);

        if (base != Math.E) {
            ret = ret / Math.log(base);
        }

        return ret;
    }

    /**
     * Compute the log-likelihood of text
     * @param fileName the name of the file containing the text
     * @return the log-likelihood of the text
     */
    public double logLikelihood(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        double value = 0.0;
        try {
            while ((word = txreader.nextWord()) != null) {
                double p = wordProb(word);
                if (p > 0) {
                    value -= Math.log(p);
                } else {
                    System.err.println(word + " has null probabilty");
                }
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        return value;
    }

    public double logLikelihoodRepeat(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        int repeat;
        double value = 0.0;
        try {
            while ((word = txreader.nextWord()) != null) {
                repeat = Integer.parseInt(word);
                word = txreader.nextWord();

                double p = wordProb(word);
                if (p > 0) {
                    value -= Math.log(p);
                } else {
                    System.err.println(word + " has null probabilty");
                }
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        return value;
    }

    /**
     * Forward probability: alpha_i = sum_j alpha_j p_ji
     */
    protected Value<State> forward(Value<State> input, Character c) {
        Value<State> output = new Value<State>();
        for (State j : input.keySet()) {
            if (j.allows(c)) {
                double val = input.getValue(j);
                State i = j.next(c);
                if (i != null) {
                    output.setValue(i, val * j.prNext(c));
                }
                output.addValue(I, val * j.prRestart(c));
            } else {
                if(j == I) {
                    output.addValue(I, input.getValue(j) * EPSILON);
                }
            }
        }
        return output;
    }

    /**
     * Backward probability: beta_i = sum_j beta_j p_ij
     */
    private Value<State> backward(Value<State> input, Character c) {
        Value<State> output = new Value<State>();
        for (State i : Q) {
            State j = i.next(c);
            if (j != null) {
                output.addValue(i, input.getValue(j) * i.prNext(c));
            }
            output.addValue(i, input.getValue(I) * i.prRestart(c));
        }
        return output;
    }

    /**
     * Create an initial value for forward/backward probabilities
     * @param value the value for the initial state I
     * @return initial probabilities: value for I and 0 otherwise
     */
    protected Value<State> initial(double value) {
        Value<State> res = new Value<State>();
        res.setValue(I, value);
        return res;
    }

    /**
     * The probability that the HMM generates a word
     * @param word a string
     * @return the probabilty that the HMM generates word
     */
    public double wordProb(String word) {
        Value<State> alpha = initial(1.0);
        for (int t = 0; t < word.length(); ++t) {
            alpha = forward(alpha, word.charAt(t));
        }
        return alpha.getValue(I) * I.prRestart(EOW);
    }

    /**
     * Print HMM states and probabilities to standard output
     */
    public void print() {
        System.out.println("-----------------------");
        I.printTree("");
    }

    /**
     * Collapse to 0.0 if double is too small
     */
    protected double clip(double x) {
        return x < EPSILON ? 0.0 : x;
    }

    public void trainWord(String word, Value<State> times, HashMap<State, Value<Character>> restart) {
        trainWord(word, times, restart, 1);
    }

    /**
     * Training phase: DRAFT VERSION !!!!!!
     */
    public void trainWord(String word, Value<State> times, HashMap<State, Value<Character>> restart, int repeat) {
        int len = word.length();
        double wordProb;
        Value<State> alpha = initial(1.0);
        Value<State> beta;
        ArrayList<Value<State>> alphas = new ArrayList<Value<State>>();

        alphas.add(alpha); // System.out.println("alpha(0)= \n" + alpha);
        for (int t = 0; t < len; ++t) {
            Character c = word.charAt(t);
            alpha = forward(alpha, c); // System.out.println("alpha(" + (t+1) + ")= \n" + alpha);
            alphas.add(alpha);
        }
        beta = initial(I.prRestart(EOW));
        wordProb = alpha.get(I) * I.prRestart(EOW);
        //System.out.println("wordProb="+wordProb); //print();
        for (int t = len - 1; t >= 0; --t) {
            Character c = word.charAt(t); //System.out.println("beta(" + (t+1) + ")= \n" + beta);
            alpha = alphas.get(t);
            for (State i : alpha.keySet()) {
                double alphai = alpha.get(i);
                State j = i.next(c);
                double restartValue = beta.containsKey(I) ? clip(alphai * i.prRestart(c) * beta.get(I) / wordProb) : 0;
                if (j != null && beta.containsKey(j)) {
                    double nextValue = clip(alphai * i.prNext(c) * beta.get(j) / wordProb);
                    // System.out.println(i+"->"+j+"("+beta.get(j)+")");
                    times.addValue(j, nextValue * repeat);
                //System.out.println(j + " addTimes = " + nextValue);
                }
                try {
                restart.get(i).addValue(c, restartValue * repeat);
                } catch (Exception e) {

                    System.err.println("ERROR: "+i.getName());
                    e.printStackTrace();
                    System.exit(0);

                }
                //System.out.println(i + " addRestart["+c+"]=" + restartValue);
                times.addValue(I, restartValue * repeat);
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
        times.addValue(I, 1.0 * repeat);
        restart.get(I).addValue(EOW, 1.0 * repeat);
    }

    /**
     * Train with best-way
     * @param word
     * @param times
     * @param restart
     * @param repeat
     */
    public void trainBest(String word, Value<State> times, HashMap<State, Value<Character>> restart, int repeat) {
        int len = word.length();
        double wordProb;
        Value<State> alpha = initial(1.0);
        Value<State> beta;
        ArrayList<Value<State>> alphas = new ArrayList<Value<State>>();

        alphas.add(alpha); // System.out.println("alpha(0)= \n" + alpha);
        for (int t = 0; t < len; ++t) {
            Character c = word.charAt(t);
            alpha = forward(alpha, c); // System.out.println("alpha(" + (t+1) + ")= \n" + alpha);
            alphas.add(alpha);
        }
        beta = initial(I.prRestart(EOW));
        wordProb = alpha.get(I) * I.prRestart(EOW);
        //System.out.println("wordProb="+wordProb); //print();
        for (int t = len - 1; t >= 0; --t) {
            Character c = word.charAt(t); //System.out.println("beta(" + (t+1) + ")= \n" + beta);
            alpha = alphas.get(t);
            for (State i : alpha.keySet()) {
                double alphai = alpha.get(i);
                State j = i.next(c);
                double restartValue = beta.containsKey(I) ? clip(alphai * i.prRestart(c) * beta.get(I) / wordProb) : 0;
                if (j != null && beta.containsKey(j)) {
                    double nextValue = clip(alphai * i.prNext(c) * beta.get(j) / wordProb);
                    // System.out.println(i+"->"+j+"("+beta.get(j)+")");
                    times.addValue(j, nextValue * repeat);
                //System.out.println(j + " addTimes = " + nextValue);
                }
                restart.get(i).addValue(c, restartValue * repeat);
                //System.out.println(i + " addRestart["+c+"]=" + restartValue);
                times.addValue(I, restartValue * repeat);
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
        times.addValue(I, 1.0 * repeat);
        restart.get(I).addValue(EOW, 1.0 * repeat);
    }

    /**
     * EM algorithm, using only best-way
     */
    public void expectationMaximizationViterbi(String filename) {
        TextReader txreader = new TextReader(filename, WordType.LETTERSorDIGITS);
        String word;
        int repeat;
        Value<State> times = new Value<State>();
        HashMap<State, Value<Character>> restart = new HashMap<State, Value<Character>>();
        for (State s : Q) {  //System.out.println(s);
            restart.put(s, new Value<Character>());
        }
        try {
            while ((word = txreader.nextWord()) != null) {
                repeat = Integer.parseInt(word);
                word = txreader.nextWord();
                trainBest(word, times, restart, repeat);
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
     * EM algorithm with times
     */
    public void expectationMaximizationTimes(String filename) {
        TextReader txreader = new TextReader(filename, WordType.LETTERSorDIGITS);
        String word;
        int repeat;
        Value<State> times = new Value<State>();
        HashMap<State, Value<Character>> restart = new HashMap<State, Value<Character>>();
        for (State s : Q) {  //System.out.println(s);
            restart.put(s, new Value<Character>());
        }
        try {
            while ((word = txreader.nextWord()) != null) {
                repeat = Integer.parseInt(word);
                word = txreader.nextWord();
                trainWord(word, times, restart, repeat);
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
     * EM algorithm
     */
    public void expectationMaximization(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        Value<State> times = new Value<State>();
        HashMap<State, Value<Character>> restart = new HashMap<State, Value<Character>>();
        for (State s : Q) {  //System.out.println(s);
            restart.put(s, new Value<Character>());
        }
        try {
            while ((word = txreader.nextWord()) != null) {
                trainWord(word, times, restart);
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
        } catch (NullPointerException np) {
            np.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Viterbi 
     * @param word a string
     * @return the best split of word (best path)
     */
    @SuppressWarnings("unchecked")
    public String split(String word) {
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
                        if (t + 1 < word.length()) {
                            split[1 - t % 2].put(I, s + c + "-");
                        } else {
                            split[1 - t % 2].put(I, s + c);
                        }
                    }
                } else {
                    if( q == I) {
                        String s = split[t % 2].get(q);
                        double val = alpha[t % 2].getValue(q);
                        if (val * EPSILON > alpha[1 - t % 2].getValue(I)) {
                            alpha[1 - t % 2].setValue(I, val * EPSILON);
                            if (t + 1 < word.length()) {
                                split[1 - t % 2].put(I, s + "-" + c);
                            } else {
                                split[1 - t % 2].put(I, s + c);
                            }
                        }
                    }
                }
            }
        }
        return BOW+split[word.length() % 2].get(I)+EOW;
    }

    /**
     * Viterbi
     * @param word a string
     * @return the best split of word (best path)
     */
    @SuppressWarnings("unchecked")
    public double probViterbi(String word) {
        Value<State>[] alpha = new Value[2];
        HashMap<State, String>[] split = new HashMap[2];
        alpha[0] = new Value<State>();
        alpha[0].setValue(I, 1.0);
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
                        if (t + 1 < word.length()) {
                            split[1 - t % 2].put(I, s + "-" + c);
                        } else {
                            split[1 - t % 2].put(I, s + c);
                        }
                    }
                } else {
                    if( q == I) {
                        String s = split[t % 2].get(q);
                        double val = alpha[t % 2].getValue(q);
                        if (val * EPSILON > alpha[1 - t % 2].getValue(I)) {
                            alpha[1 - t % 2].setValue(I, val * EPSILON);
                            if (t + 1 < word.length()) {
                                split[1 - t % 2].put(I, s + "-" + c);
                            } else {
                                split[1 - t % 2].put(I, s + c);
                            }
                        }
                    }
                }
            }
        }
        return alpha[word.length() % 2].get(I) * I.prRestart(EOW);
    }

    /**
     * Clean
     */
    public void clean() {

        // remove transitions with wordProb < epsilon
        Iterator<State> it = Q.iterator();
        while (it.hasNext()) {
            State st = it.next();
            if (st.isUsed()) {
                st.cleanEpsilon(EPSILON, I);
            }
        }

        // remove inaccessible states
        HashSet<State> newQ = new HashSet<State>();
        it = Q.iterator();
        while (it.hasNext()) {
            State st = it.next();

            if (st.isUsed()) {
                newQ.add(st);
            }
        }
        Q.clear();
        Q = null;
        Q = newQ;
        I.repair();
    }

    /**
     * Restart training probabilities
     *
     */
    public void restart() {
        I.resetProbs();
    }

    /**
     * Print HMM states and probabilities to an XML
     */
    public void save(String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<hmm var=\"pre\">");
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
    }

    /**
     * Gives the total parameters of the model
     * @return total of parameters (double) of the model
     */
    /**@Override
    public int getSize() {

        int total = 0;

        for (State st : Q) {
            total += st.parameterSize();
        }

        return total;
    }*/
    public int getSize() {

        return I.getSize();
    }

    /**
     * Main code for testing
     */
    public static void main(String[] args) {
        main4(args);
    }

    public static void main5(String[] args) {
        Trainer tr = new Trainer(10, 0, -1);
        tr.load("/home/xavi/Documents/tesina/mm/dc.xml", "/home/xavi/Documents/tesina/mm/hmm.xml", Mode.HMM);


        DevHMMPre dev = new DevHMMPre(tr);

        System.out.println(dev.split(args[0]));
        System.out.println(dev.prob(args[0]));
        System.out.println(dev.probViterbi(args[0]));
    }

    public static void main4(String[] args) {

        // args[0] -> diccionari
        // args[1] -> model HMM
        // args[2] -> corpus de test

        Trainer tr = new Trainer(10, 0, -1);
        tr.load(args[0], args[1], Mode.HMM);


        DevHMMPre dev = new DevHMMPre(tr);

        System.out.println("Param size: " + dev.getSize());

        // fent la suma
        System.out.println("logprob: " + dev.logLikelihood(args[2]));

        System.out.println("logprob: " + dev.logViterbiLikelihood(args[2]));


    }

    public static void main3(String[] args) {
        HMMPre M = new HMMPre();
        HMMPre N = new HMMPre();

        String fileName = "/home/xavi/Documents/tesina/test.txt";//args[1];

        M.train(fileName);
        M.print();
        N.train(fileName); //N.print();


        for (int i = 0; i < 5; ++i) {
            M.expectationMaximizationTimes("/home/xavi/Documents/tesina/test2.txt");//args[2]);
        }

        M.print();

        for (int i = 0; i < 5; ++i) {
            N.expectationMaximizationTimes("/home/xavi/Documents/tesina/test2.txt");//args[2]);
        }

        N.clean();
        N.print();

        for (int i = 0; i < 5; ++i) {
            N.expectationMaximizationTimes("/home/xavi/Documents/tesina/test2.txt");//args[2]);
        }
        N.print();
    }

    public static void main2(String[] args) {
        HMMPre M = new HMMPre();
        String fileName = "/home/xavi/Documents/tesina/svn/tests/aba";//args[0];//
        int iterations = 10; //new Integer(args[1]);
        double ppl1 = 0;
        double ppl2 = 0;

        M.train(fileName);
        M.print();
        ppl1 = Probability.logprob2ppl(M.logLikelihood(fileName));
        for (int i = 0; i < iterations; ++i) {
            //System.out.print("- "+(i+1)+" -");
            M.expectationMaximization(fileName);
        //M.print();
        //System.out.println("M.size() + " " + M.wordProb(args[1]));
        }
        ppl2 = Probability.logprob2ppl(M.logLikelihood(fileName));
        System.out.println("\n logLikelihood = " + M.logLikelihood(fileName));
        System.out.println("\n ppl1 = " + ppl1 + "\t ppl2 = " + ppl2);

        fileName = "/home/xavi/Documents/tesina/svn/tests/aba2";
        M = new HMMPre();

        M.readRepeat(fileName);
        M.print();
        ppl1 = Probability.logprob2ppl(M.logLikelihood(fileName));
        for (int i = 0; i < iterations; ++i) {
            //System.out.print("- "+(i+1)+" -");
            M.expectationMaximization(fileName);
        //M.print();
        //System.out.println("M.size() + " " + M.wordProb(args[1]));
        }
        ppl2 = Probability.logprob2ppl(M.logLikelihood(fileName));
        System.out.println("\n logLikelihood = " + M.logLikelihoodRepeat(fileName));
        System.out.println("\n ppl1 = " + ppl1 + "\t ppl2 = " + ppl2);
    }
}