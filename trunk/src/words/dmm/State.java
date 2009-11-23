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
package words.dmm;

import words.utils.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author xavi
 */
public class State {

    private String name;
    private String cloneName;
    private ArrayList<Connection> connections;
    private HashMap<String, Int> transitionCounter;
    private long counter;
    private Connection masterConnection;
    private static int nextState = 0;
    private HashMap<String, Double> probs;
    private double defaultProb;
    private int maxConnections;

    public State(String n) {
        this.defaultProb = 0;
        this.probs = null;
        this.name = n;
        this.cloneName = this.name + "_";
        this.connections = new ArrayList<Connection>();
        this.counter = 0;
        this.masterConnection = null;
        this.transitionCounter = new HashMap<String, Int>();
        this.maxConnections = -1;
    }

    /**
     * @return name for the cloned state
     */
    public String getCloneName() {
        return "" + (++nextState);
    }

    /**
     * @return max number of connections from current state
     */
    public int getMaxConnections() {
        if(maxConnections < 0) {
            maxConnections=this.connections.size();
        }
        
        return maxConnections;
    }

    /**
     * Sets max number of connections
     * @param i
     */
    public void setMaxConnections(int i) {
        maxConnections = i;
    }

    /**
     * Updates times a connection is visited
     * @return number of visits
     */
    public long visit() {
        return ++counter;
    }

    /**
     * @return times a connection is visited
     */
    public long getVisits() {
        return counter;
    }

    /**
     * @param tg
     * @return next state, given a string
     */
    public State next(String tg) {
        State st = null;
        Connection cn = findConnection(tg);
        if (cn != null) {
            st = cn.getTo();
        }

        if (st == null) {
            st = masterConnection.getTo();
        }
        return st;
    }

    public boolean hasConnection(String tg) {
        Connection cn = findConnection(tg);

        return (cn!=null);
    }

    /**
     * Adds a transition from current state, given a tag
     * @param tg
     */
    public void addTransition(String tg) {
        if (transitionCounter.containsKey(tg)) {
            transitionCounter.get(tg).incValue();
        } else {
            transitionCounter.put(tg, new Int(1));
        }
    }

    /**
     * @param tg
     * @return total of transitions from current state, given a tag
     */
    public int getTransitions(String tg) {
        int ret = 0;
        if (transitionCounter.containsKey(tg)) {
            ret = transitionCounter.get(tg).getValue();
        }

        return ret;
    }

    /**
     * Sets number of transitions from current state, given a tag
     * @param tg
     * @param counter
     */
    public void setTransitions(String tg, Int counter) {
        transitionCounter.put(tg, counter);
    }

    /**
     * @return total of transition counters
     */
    public int getAllTransitions() {
        int ret = 0;

        Collection<Int> coll = transitionCounter.values();

        for (Int i : coll) {
            ret += i.getValue();
        }

        return ret;
    }

    /**
     * @return name of current state
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a connection from current state to a target state, given a tag
     * @param tg tag
     * @param st target state
     * @return true if a new connection is created
     */
    public boolean addConnection(String tg, State st) {
        return addConnection(tg, st, false);
    }

    public boolean addConnection(String tg, State st, boolean force) {
        boolean ret = false;

        Connection conn = findConnection(tg);

        if (conn == null) {
            ret = true;

            conn = new Connection(tg, st);
            connections.add(conn);
        } else {
            if (force) {
                ret = true;

                connections.remove(conn);

                conn = new Connection(tg, st);
                connections.add(conn);
            }
        }

        return ret;
    }

    /**
     * Adds a master connection to a target state
     * @param st
     * @return if a master connection has been created
     */
    public boolean addMasterConnection(State st) {
        boolean ret = false;

        if (masterConnection == null) {
            masterConnection = new Connection("fallback", st);
            ret = true;
        }

        return ret;
    }

    /**
     * @param tg
     * @return connection from current state given a tag
     */
    private Connection findConnection(String tg) {
        Connection conn = null;

        for (int i = 0; i < connections.size(); ++i) {
            if (connections.get(i).getTag().equals(tg)) {
                conn = connections.get(i);
                break;
            }
        }

        return conn;
    }

    /**
     * @return master connection
     */
    public Connection getMasterConnection() {
        return masterConnection;
    }

    /**
     * Copy counters from another state to current state, given a ratio
     * @param st
     * @param ratio
     */
    public void copyCounters(State st, double ratio) {
        for (Entry<String, Int> ent : st.transitionCounter.entrySet()) {
            int nw = (int) Math.round(ent.getValue().getValue() * ratio);
            this.transitionCounter.put(ent.getKey(), new Int(nw));
            st.transitionCounter.put(ent.getKey(), new Int(ent.getValue().getValue() - nw));
        }
    }

    /**
     * @return arraylist with all connections
     */
    public ArrayList<Connection> allConnections() {
        ArrayList<Connection> nar = new ArrayList<Connection>();

        nar.addAll(connections);


        return nar;
    }

    /**
     * @param word
     * @return probability of a string
     */
    public double prob(String word) {
        Double ret = 0.0;

        // probabilities are computed once
        if (this.probs == null) {
            probs = new HashMap<String, Double>();
            double i = 0;
            for (Connection c : connections) {
                i += transitionCounter.get(c.getTag()).getValue();
            }
            ++i;
            for (Connection c : connections) {
                probs.put(c.getTag(), (((double) transitionCounter.get(c.getTag()).getValue()) / i));
            }
            defaultProb = (1.0 / i) * (1.0 / ((double) ( maxConnections - connections.size())));
        }

        ret = probs.get(word);

        // if there isn't a probability computed for a given string, defaultProb
        // (associated with masterConnection) is given
        if (ret == null) {
            ret = defaultProb;
            
        }
        
        return ret.doubleValue();
    }

    /**
     * prints a state to stdout
     */
    public void print() {
        System.out.println("<state name=\"" + this.getName() + "\" fallback=\"" + masterConnection.getTo().getName() + "\">");

        System.out.println("<connections>");
        for (Entry<String, Int> ent : transitionCounter.entrySet()) {
            if (ent.getValue().getValue() > 0) {
                Connection cn = findConnection(ent.getKey());
                if (cn != null) {
                    System.out.println("<connection tag=\"" + ent.getKey() + "\" counter=\"" + ent.getValue().toString() + "\" to=\"" + cn.getTo().getName() + "\" />");
                } else {
                    System.out.println("<connection tag=\"" + ent.getKey() + "\" counter=\"" + ent.getValue().toString() + "\" to=\"" + masterConnection.getTo().getName() + "\" />");
                }
            }
        }

        System.out.println("</connections>");

        System.out.println("</state>");
    }

    /**
     * Prints a state to a given PrintWriter
     * @param pw
     */
    public void writeMarkov(PrintWriter pw) {
        pw.println("<state name=\"" + this.getName() + "\" fallback=\"" + masterConnection.getTo().getName() + "\">");

        pw.println("<connections>");
        for (Entry<String, Int> ent : transitionCounter.entrySet()) {
            if (ent.getValue().getValue() > 0) {
                Connection cn = findConnection(ent.getKey());
                if (cn != null) {
                    pw.println("<connection tag=\"" + ent.getKey() + "\" counter=\"" + ent.getValue().toString() + "\" to=\"" + cn.getTo().getName() + "\" />");
                } else {
                    pw.println("<connection tag=\"" + ent.getKey() + "\" counter=\"" + ent.getValue().toString() + "\" to=\"" + masterConnection.getTo().getName() + "\" />");
                }
            }
        }

        pw.println("</connections>");
        pw.println("</state>");
    }

    /**
     * @return number of parameters stored
     */
    public int parameterSize() {
        return connections.size();
    }

    /**
     * Restart state counter
     */
    public static void init() {
        nextState = 0;
    }
}
