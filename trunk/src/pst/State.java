/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package words.pst;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import words.utils.Functions;

/**
 *
 * @author xavi
 */
public class State {
    public int times;
    public String prefix;
    public String suffix;
    public String name;
    public Map<Character,State> next;
    private State parent;
    private boolean deleted;
    private boolean needed;

    public State() {
        this("");
    }

    public State(String x) {
        this(x,"");
    }

    public State(String x,String pr) {
        prefix = pr + x;
        suffix = Functions.reverse(prefix);
        name = x;
        next = new HashMap<Character, State>();
        parent = null;
        deleted = false;
        needed = true;
    }

    public Set<State> getTerminal() {
        Set<State> ret = new HashSet<State>();

        if(!deleted) {
            for(State st : next.values()) {
                if(st.terminal()) {
                    ret.add(st);
                } else {
                    ret.addAll(st.getTerminal());
                }
            }
        }

        return ret;
    }

    public String getName() {
        return name;
    }

    public Character getCharName() {
        return ((name!=null)?name.charAt(0):null);
    }

    public String getFullName() {
        String ret;

        if(parent!=null)
            ret = name + parent.getFullName();
        else
            ret = name;

        return ret;
    }


    /**
     * @return true if node is terminal
     */
    public boolean terminal() {
        boolean empty = true;
        for(State st : next.values()) {
            if(!st.deleted) { empty = false; break; }
        }

        return (!deleted && empty);
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
     * Compute destination state if not pruned
     * @param c the output character
     * @return the destination state when output is c
     */
    public State existingNext(Character c) {
        State st = next.get(c);

        if(st!=null && st.deleted)
            st = null;

        return st;
    }

    /**
     * Sets parent of a state
     * @param st
     */
    public void setParent(State st) {
        parent = st;
    }

    /**
     * @param c an output symbol
     * @param v the state is visited
     * @return the destination state when output is c
     */
    public State safeNext(Character c,boolean v) {
        if (next(c) == null) {
            State st = new State(""+c);
            st.setParent(this);
            next.put(c,st);
        }
        State nx = next(c);
        if(v) nx.visit();
        return nx;
    }

    /**
     * @param c an output symbol
     * @return the destination state when output is c
     */
    public State safeNext(Character c) {
        return this.safeNext(c,true);
    }

    public void remove(Character ch) {
        next.remove(ch);
    }

    public int visit() {
        return ++times;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int tm) {
        times = tm;
    }

    public State getParent() {
        return parent;
    }

    public void delete() {
        deleted = true;
    }

    public ArrayList<State> children() {
        ArrayList<State> ret = new ArrayList<State>();

        for(State st : next.values()) {
            if(!st.deleted)
                ret.add(st);
        }

        return ret;
    }

    public void status(String tabs) {
        System.out.println(tabs + ": (" + name + ") "+ ((deleted)?"deleted ":"") + ((needed)?"needed ":""));
        for(State st : next.values()) {
            st.status(tabs + "  ");
        }
    }

    public void printTree() {
        System.out.println("--[ ]");
        ArrayList<State> childs = children();
        int s = childs.size();
        int i = 1;
        for (State st : childs) {
            if(i<s) {
                st.printTree("","",false);
            } else {
                st.printTree("","",true);
            }
            ++i;
        }
    }

    public void printTree(String tabs, String sf, boolean last) {
        char lst = (last)?'=':'+';
        String parlast = (last)?"    ":"   |";
        System.out.println(tabs+parlast.substring(0,parlast.length()-1)+lst+"--["+name+"] "+name+Functions.reverse(sf)+"/? ("+times+")");
        ArrayList<State> childs = children();
        int s = childs.size();
        int i = 1;
        for (State st : childs) {
            if(i<s) {
                st.printTree(tabs+parlast,sf+name,false);
            } else {
                st.printTree(tabs+parlast,sf+name,true);
            }
            ++i;
        }
        if(!last) System.out.println(tabs+parlast);
    }

    public void printTree(String tabs,String sf) {
        printTree(tabs,sf,false);
    }

    public int getParameterSize() {
        int total = 1;

        for(State st: next.values()) {
            total += st.getParameterSize();
        }

        return total;
    }

    public int getNodes() {
        int ret = 1;

        List<State> sts = this.children();

        for(State st : sts) {
            ret += st.getNodes();
        }

        return ret;
    }

    public int getMaxDepth() {
        int ret = 0, tmp = 0;

        ArrayList<State> al = children();

        for(State st: al) {
            tmp=st.getMaxDepth();
            if(tmp > ret)
                ret = tmp;
        }

        return ret+1;
    }

    public Set<Character> getChars() {
        return next.keySet();
    }

    public void prepareClean() {

        if(deleted) {
            needed = false;
        }

        for(State st : next.values()) {
            st.prepareClean();
        }
    }

    public void need() {
        needed = true;
        State st = parent;

        while(st!=null){
            st.needed=true;
            st = st.parent;
        }
    }

    public void clean() {
        State st = null;
        List<Character> toRemove = new ArrayList<Character>();
        for(Character c : next.keySet()) {
            st = next.get(c);
            if(st.deleted && !st.needed) {
                st.forceClean();
                st = null;
                toRemove.add(c);
            } else {
                st.clean();
            }
        }

        for(Character c : toRemove) {
            next.remove(c);
        }
    }

    public void forceClean() {
        State st = null;
        List<Character> toRemove = new ArrayList<Character>();
        for(Character c : next.keySet()) {
            st = next.get(c);
            st.forceClean();
            st = null;
            toRemove.add(c);
        }

        for(Character c : toRemove) {
            next.remove(c);
        }
    }

    /**
     * Print to an XML
     */
    public void printXML(PrintWriter pw) {
        this.printXML(pw,"");
    }

    public void printXML(PrintWriter pw,String parent) {
            pw.println("<state name=\"" + name + parent + "\" times=\"" + times +
                    "\" deleted=\"" + deleted + "\" />");

            for (State st : next.values()) {
                st.printXML(pw,name + parent);
            }
    }
}
