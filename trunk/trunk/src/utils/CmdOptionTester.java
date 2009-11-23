/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package words.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import words.Model.Unit;
import words.Model.Action;
import words.Run.Mode;
import words.utils.CmdOptions.Option;

/**
 *
 * @author xavi
 */
public class CmdOptionTester {

    public Unit testUnit(CmdOptions parser, Option unit) {
        Unit u = Unit.CHAR;

        String uValue = (String) parser.getOptionValue(unit);
        if ((uValue != null) && (uValue.equalsIgnoreCase("syl"))) {
            u = Unit.SYLLABLE;
        }

        return u;
    }

    public Action testAction(CmdOptions parser, Option action,String [] vactions) {
        Action act = Action.DEV;

        String actionValue = ((String) parser.getOptionValue(action));

        
        
        ArrayList<String> actions = new ArrayList<String>(Arrays.asList(vactions));
        //actions.addAll(vactions);

        if(!(actions.contains(actionValue)))
        {
            boolean first = true;
            System.err.print("BAD USAGE. Action must be defined: {-a | --action} { ");
            for(String st : actions) {
                if(!first) System.err.print("| ");
                System.err.print(st+" ");
            }
            System.err.println("}");
            System.exit(0);
        }

        if (actionValue.equalsIgnoreCase("train")) {
            act = Action.TRAIN;
        }
        if (actionValue.equalsIgnoreCase("dev")) {
            act = Action.DEV;
        }
        if (actionValue.equalsIgnoreCase("test")) {
            act = Action.TEST;
        }
        if (actionValue.equalsIgnoreCase("prob")) {
            act = Action.PROB;
        }
        if (actionValue.equalsIgnoreCase("size")) {
            act = Action.SIZE;
        }
        if (actionValue.equalsIgnoreCase("order")) {
            act = Action.ORDER;
        }
        if (actionValue.equalsIgnoreCase("rank")) {
            act = Action.RANK;
        }

        return act;
    }

    public Mode testMode(CmdOptions parser, Option mode) {
        Mode act = Mode.NGRAM;
        String modeValue = ((String) parser.getOptionValue(mode));
        if ((modeValue == null) || (!(((modeValue.equalsIgnoreCase("hmm") || (modeValue.equalsIgnoreCase("hmm#"))) || modeValue.equalsIgnoreCase("ngram")) || (modeValue.equalsIgnoreCase("dmc") || (modeValue.equalsIgnoreCase("dictngram")||(modeValue.equalsIgnoreCase("vlmc"))))))) {
            System.err.println("BAD USAGE. Mode must be defined: {-n | --mode} {ngram | dictngram | dmc | hmm | vlmc}");
            System.exit(0);
        }

        if (modeValue.equalsIgnoreCase("ngram")) {
            act = Mode.NGRAM;
        }
        if (modeValue.equalsIgnoreCase("dictngram")) {
            act = Mode.COMBINED;
        }
        if (modeValue.equalsIgnoreCase("dmc")) {
            act = Mode.DMC;
        }
        if (modeValue.equalsIgnoreCase("hmm")) {
            act = Mode.HMM;
        }
        if (modeValue.equalsIgnoreCase("hmm#")) {
            act = Mode.HMMEnd;
        }
        if (modeValue.equalsIgnoreCase("vlmc")) {
            act = Mode.VLMC;
        }


        return act;
    }

    public String testCorpus(CmdOptions parser, Option corpus) {
        return testCorpus(parser, corpus, false);
    }

    public String testCorpus(CmdOptions parser, Option corpus, boolean stdin) {

        String corpusValue = (String) parser.getOptionValue(corpus);
        if (corpusValue == null) {
            System.err.println("BAD USAGE. Corpus must be defined: {-c | --corpus} file");
            System.exit(0);
        }

        if(stdin && corpusValue.equalsIgnoreCase("-"))
            return null;

        boolean cpok = true;

        File cpfile = new File(corpusValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (!cpok) {
            System.err.println("BAD USAGE. Corpus file must be an existing file [" + corpusValue + "]");
            System.exit(0);
        }
        return corpusValue;
    }

    public String testDict(CmdOptions parser, Option dictionary, boolean force, boolean read, boolean oblig) {
        String dictionaryValue = (String) parser.getOptionValue(dictionary);
        if (dictionaryValue == null) {
            if (oblig) {
                System.err.println("BAD USAGE. Dictionary file must be defined: {-d | --dict} file");
                System.exit(0);
            } else {
                return null;
            }
        }
        boolean cpok = true;

        File cpfile = new File(dictionaryValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. Dictionary file must be an existing file [" + dictionaryValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                dictionaryValue = null;
            }
        }
        return dictionaryValue;
    }

    public String testNgram(
            CmdOptions parser, Option ngram, boolean force, boolean read, boolean oblig) {
            String ngramValue = (String) parser.getOptionValue(ngram);
        if (ngramValue == null) {

            if (oblig) {
                System.err.println("BAD USAGE. ngram file must be defined: {-d | --dict} file");
                System.exit(0);
            } else {
                return null;
            }
        }

        boolean cpok = true;

        File cpfile = new File(ngramValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. ngram file must be an existing file [" + ngramValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                ngramValue = null;
            }
        }

        return ngramValue;
    }

    public String testDMC(
            CmdOptions parser, Option dmc, boolean force, boolean read, boolean oblig) {
        String dmcValue = (String) parser.getOptionValue(dmc);
        if (dmcValue == null) {
            if (oblig) {
                System.err.println("BAD USAGE. dmc file must be defined: {-k | --dmc} file");
                System.exit(0);
            } else {
                return null;
            }
        }

        boolean cpok = true;

        File cpfile = new File(dmcValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. dmc file must be an existing file [" + dmcValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                dmcValue = null;
            }
        }

        return dmcValue;
    }

    public String testHMM(
            CmdOptions parser, Option hmm, boolean force, boolean read, boolean oblig) {
        String hmmValue = (String) parser.getOptionValue(hmm);
        if (hmmValue == null) {
            if (oblig) {
                System.err.println("BAD USAGE. hmm file must be defined: {-h | --hmm} file");
                System.exit(0);
            } else {
                return null;
            }
        }

        boolean cpok = true;

        File cpfile = new File(hmmValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. hmm file must be an existing file [" + hmmValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                hmmValue = null;
            }
        }
        return hmmValue;
    }

    public String testHMMEnd(
            CmdOptions parser, Option hmm, boolean force, boolean read, boolean oblig) {
        String hmmValue = (String) parser.getOptionValue(hmm);
        if (hmmValue == null) {
            if (oblig) {
                System.err.println("BAD USAGE. hmm# file must be defined: {-# | --hmm#} file");
                System.exit(0);
            } else {
                return null;
            }

        }

        boolean cpok = true;

        File cpfile = new File(hmmValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. hmm# file must be an existing file [" + hmmValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                hmmValue = null;
            }
        }

        return hmmValue;
    }

    public String testPST(
            CmdOptions parser, Option vlmc, boolean force, boolean read, boolean oblig) {
        String vlmcValue = (String) parser.getOptionValue(vlmc);
        if (vlmcValue == null) {
            if (oblig) {
                System.err.println("BAD USAGE. pst file must be defined: {-p | --pst} file");
                System.exit(0);
            } else {
                return null;
            }
        }

        boolean cpok = true;

        File cpfile = new File(vlmcValue);
        if (!cpfile.exists()) {
            cpok = false;
        }

        if (read) {
            if (!cpok) {
                System.err.println("BAD USAGE. vlmc file must be an existing file [" + vlmcValue + "]");
                System.exit(0);
            }
        } else {
            if (cpok && !force) {
                vlmcValue = null;
            }
        }
        return vlmcValue;
    }

    public String testFile(CmdOptions parser, Option opt) {
        return (String) parser.getOptionValue(opt);
    }

    public boolean testBoolean(CmdOptions parser, Option opt) {
        return (((Boolean) parser.getOptionValue(opt)) != null) ? true : false;
    }

    public int testInteger(CmdOptions parser, Option length, int def) {
        int lengthValue = def;

        Integer lengthV = (Integer) parser.getOptionValue(length);
        lengthValue = (lengthV != null) ? lengthV.intValue() : def;

        return lengthValue;
    }

    public double testDouble(CmdOptions parser, Option length, double def) {
        double lengthValue = def;

        Double lengthV = (Double) parser.getOptionValue(length);
        lengthValue = (lengthV != null) ? lengthV.doubleValue() : def;

        return lengthValue;
    }
}
