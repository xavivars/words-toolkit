package words.hmm;

import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * States in a HMM
 */
public class State {

    private double times;                 // Number of times the state is used
    private Map<Character, State> next;   // Next output symbol and destination state 
    private Value<Character> restart;     // Number of restarts for every character 
    private String name;                  // Name of the state
    /** Precission for real values */
    final public static double EPSILON = 1e-20;

    /**
     * Default constructor
     */
    public State() {
        next = new HashMap<Character, State>();
        restart = new Value<Character>();
    }

    public void setName(String n) {
        if (name == null) {
            name = n;
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Clean contents
     */
    public void clean() {
        times = 0;
        next.clear();
        restart.clear();
    }

    /**
     * Compute destination state
     * @param c the output character
     * @return the destination state when output is c
     */
    public State next(Character c) {
        return next.get(c);
    }

    /**
     * Check if output c is possible
     * @param c a character
     * @return true if c is a possible output
     */
    public boolean allows(Character c) {
        return restart.containsKey(c);
    }

    /**
     * Set number of times
     * @param times the value stored.
     */
    public void setTimes(double times) {
        this.times = times;
    }

    /**
     * @return times visited
     */
    public double getTimes() {
        return times;
    }

    /**
     * Set number of times
     * @param times the value stored.
     */
    public void setRestart(Value<Character> restart) {
        this.restart = restart;
    }

    /**
     * Add a new transition from this state
     * @param c the output character
     * @param s the destination state
     */
    public void setNext(Character c, State s) {
        next.put(c, s);
        if (!restart.containsKey(c)) {
            restart.put(c, 0.0);
        }
    }

    /**
     * Cleans transition probabilities lower than EPSILON
     * @param epsilon
     */
    public void cleanEpsilon(double epsilon, State initial) {

        Set<Entry<Character, State>> chars = next.entrySet();
        HashMap<Character, State> nnext = new HashMap<Character, State>();

        Iterator<Entry<Character, State>> it = chars.iterator();
        while (it.hasNext()) {
            Entry<Character, State> entry = it.next();
            State st = entry.getValue();
            Character c = entry.getKey();
            if (st.times <= epsilon) {
                double tms = st.times;
                st.cleanFollowing();
                this.times -= tms;
                initial.times -= tms;
            } else {
                nnext.put(c, st);
            }
        }

        next = nnext;

        // possibles problemes amb la precissió
        if (this.times < 0) {
            this.times = 0;
        }
    }

    public void repair() {
        for(Character c : restart.keySet()) {
            if(restart.getValue(c)==0) {
                restart.addValue(c, EPSILON);
                this.times += EPSILON;
            }
        }
        for(Character c : next.keySet()) {
            if(restart.getValue(c)==0) {
                restart.addValue(c, EPSILON);
                this.times += EPSILON;
            }
        }
    }

    /**
     * Gives the total transitions from a state
     * @return total amount of transitions
     */
    public int parameterSize() {
        return next.size() + restart.size();
    }

    public int getSize() {
        int ret = restart.size();
        for(State st: next.values()) {
            ret+=st.getSize()+1;
        }
        return ret;
    }

    public int alphabetSize() {
        return restart.size();
    }

    /**
     * Cleans all accessible states from this
     */
    public void cleanFollowing() {

        Set<Entry<Character, State>> chars = next.entrySet();

        Iterator<Entry<Character, State>> it = chars.iterator();
        while (it.hasNext()) {
            State st = it.next().getValue();
            st.cleanFollowing();
        }

        this.times = 0;
        next.clear();
    }

    /**
     * Resets probabilites after a cleaning
     * @return times Times a state is visited
     */
    public double resetProbs() {

        times = 1;

        Iterator<Entry<Character, State>> it = next.entrySet().iterator();

        State st;

        while (it.hasNext()) {
            st = it.next().getValue();
            times += st.resetProbs();
        }

        return times;
    }

    /**
     * The output symbol probability
     * @param c a character
     * @return the probabilty that c is generated as output
     */
    public double prob(Character c) {
        return prNext(c) + prRestart(c);
    }

    /**
     * Restart probability
     * @param c a character
     * @return the probabilty that c is generated as output and 
     * the HMM moves to the initial (or restart) state 
     */
    public double prRestart(Character c) {
        if (restart.containsKey(c) && times > 0) {
            return restart.getValue(c) / times;
        } else {
            return 0;
        }
    }

    /**
     * Transition porbability 
     * @param c a character
     * @return the probabilty that c is generated and 
     * the HMM moves to a state different from the initial state 
     */
    public double prNext(Character c) {
        State s = next.get(c);
        if (s != null && times > 0) {
            return s.times / times;
        } else {
            return 0;
        }
    }

    /**
     * How many times restart is done
     * @param c a Character
     */
    public double timesRestart(Character c) {
        if (restart.containsKey(c)) {
            return restart.getValue(c);
        } else {
            return 0;
        }
    }

    /**
     * How many times a transition is done
     * @param c a Character
     */
    public double timesNext(Character c) {
        State s = next.get(c);
        if (s != null) {
            return s.times;
        } else {
            return 0;
        }
    }

    /**
     * Increase state counter
     * @param x the double to be added to the state counter
     */
    public void addTimes(double x) {
        times += x;
    }

    /**
     * State is used or not
     */
    public boolean isUsed() {
        return (times > 0);
    }

    /**
     * Increase restart counter
     * @param c a character
     * @param x the valur to be added to the restart counter
     */
    public void addRestart(Character c, double x) {
        restart.addValue(c, x);
    }

    /***
     * Only for debugging 
     */
    public void printTree(String prefix) {
        if (times > -1e-30) {
            System.out.println("\"" + prefix + "\" " + times);
            for (Character c : restart.keySet()) {
                System.out.println("\t" + c + " " + prob(c) + " (" + prNext(c) + "," + prRestart(c) + ")");
            }
            for (Character c : next.keySet()) {
                next(c).printTree(prefix + c);
            }
        }
    }

    public void printXML(PrintWriter pw) {
        printXML(pw, "");
    }

    /**
     * Print to an XML
     */
    public void printXML(PrintWriter pw, String prefix) {
        if (times > EPSILON) {
            pw.println("<state name=\"" + prefix + "\" times=\"" + times + "\">");

            for (Character c : restart.keySet()) {
                double tnext = timesNext(c);
                double trest = timesRestart(c);
                if (tnext + trest > EPSILON) {
                    pw.print("\t<connection char=\"" + c + "\"");
                    pw.print(" times=\"" + (tnext + trest) + "\"");
                    pw.print(" next=\"" + tnext + "\"");
                    pw.print(" restart=\"" + trest + "\"");
                    pw.println("/>");
                }
            }
            pw.println("</state>");

            for (Character c : next.keySet()) {
                next(c).printXML(pw, prefix + c);
            }
        }
    }
}
  
