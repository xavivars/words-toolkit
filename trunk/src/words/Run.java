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

import words.Model.Unit;
import words.Model.Action;
import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.*;

public class Run {

    public enum Mode {

        NGRAM {
            @Override
            public String toString() { return "NGRAM";}
        }, 
        COMBINED {
            @Override
            public String toString() { return "COMBINED";}
        },
        DMC {
            @Override
            public String toString() { return "DMC";}
        },
        HMM {
            @Override
            public String toString() { return "HMM";}
        },
        HMMEnd {
            @Override
            public String toString() { return "HMMEnd"; }
        },
        VLMC {
            @Override
            public String toString() { return "VLMC"; }
        }
    };

    private void train(String corpus, String dict, String ngrams, String dmc, String hmm, String vlmc,int length, int split,int iters, Unit unit) {
        Trainer tr = new Trainer(length, split,iters);
        tr.setUnit(unit);
        tr.train(corpus);
        tr.saveTraining(dict, ngrams, dmc, hmm, vlmc, true);
    }

    private void dev(String corpus, String dict, String otherMode, int length, Mode mode, boolean freq) {
        dev(corpus, dict, otherMode, length, -1, mode, freq);
    }

    private void dev(String corpus, String dict, String otherMode, int length, int iter,Mode mode, boolean freq) {
        Trainer tr = new Trainer(length, 0,iter);
        tr.load(dict, otherMode, mode);

        Developer dev = null;
        if (mode == Mode.COMBINED) {
            dev = new DevDictNgram(tr);
        } else if (mode == Mode.NGRAM) {
            dev = new DevNgram(tr);
        } else if (mode == Mode.DMC) {
            dev = new DevDMC(tr);
        } else if (mode == Mode.HMM) {
            dev = new DevHMMPre(tr);
        } else if (mode == Mode.VLMC) {
            dev = new DevVLMC(tr);
        }
        if (dev != null) {
            dev.adjust(corpus, freq);
            dev.printRanking();
        }
    }

    private void test(String corpus, String dict, String file,int length,Mode mode,boolean freq){
        Trainer tr = new Trainer(length, 0,-1);
        tr.load(dict, file, mode);

        Developer dev = null;
        if (mode == Mode.COMBINED) {
            dev = new DevDictNgram(tr);
        } else if (mode == Mode.NGRAM) {
            dev = new DevNgram(tr);
        } else if (mode == Mode.DMC) {
            dev = new DevDMC(tr);
        } else if (mode == Mode.HMM) {
            dev = new DevHMMPre(tr);
        } else if (mode == Mode.HMMEnd) {
            dev = new DevHMM(tr);
        }

        if (dev != null) {
            System.out.println("MODE "+mode);
            System.out.println("Param size: "+dev.getSize());
            System.out.println("logprob: "+dev.logLikelihood(corpus));
            System.out.println("logprob/words: "+dev.getLastTest() + "("+dev.getLastLength()+")");
        }
    }

    /*    private void test(String[] args) {
    Trainer tr = new Trainer();
    tr.load(args[1], args[2]);
    tr.loadParams(args[3]);
    } */
    public static void main(String[] args) {

        Run r = new Run();
        CmdOptionTester optionTester = new CmdOptionTester();

        CmdOptions parser = new CmdOptions();

        Option debug = parser.addBooleanOption('g', "debug");

        // train, dev
        Option action = parser.addStringOption('a', "action");

        // corpus file
        Option corpus = parser.addStringOption('c', "corpus");

        // dictionary file
        Option dictionary = parser.addStringOption('d', "dictionary");

        // ngram file
        Option ngram = parser.addStringOption('n', "ngram");

        // dmc file
        Option dmc = parser.addStringOption('k', "dmc");

        // dmc file
        Option hmm = parser.addStringOption('h', "hmm");

        // vlmc file
        Option vlmc = parser.addStringOption('t', "vlmc");

        // dev mode {pure,combined,dmc,hmm}
        Option mode = parser.addStringOption('m', "mode");

        // y | n
        Option frequency = parser.addBooleanOption('f', "frequency");

        // n-gram size
        Option length = parser.addIntegerOption('l', "length");

        // n-gram size
        Option iter = parser.addIntegerOption('i', "iter");

        // char, syll
        Option unit = parser.addStringOption('u', "unit");

        // force overwriting
        Option over = parser.addBooleanOption('r', "force");

        // dmc split condition
        Option split = parser.addIntegerOption('s', "split");

        //Option syllable = parser.addStringOption('s',"syllable");

        try {
            parser.parse(args);

            boolean debugValue = optionTester.testBoolean(parser, debug);

            if (debugValue) {
                int i = 0;
                for (String arg : args) {
                    System.out.println("Arg " + (++i) + ":" + arg);
                }

                System.out.println("-------------------");
            }

            Action actionValue = optionTester.testAction(parser, action,Model.actions);
            String corpusValue = optionTester.testCorpus(parser, corpus);
            int lengthValue = optionTester.testInteger(parser, length, 5);
            int iterValue = optionTester.testInteger(parser, iter, -1);
            
            if (actionValue == Action.TRAIN) {
                boolean force = optionTester.testBoolean(parser, over);
                int splitValue = optionTester.testInteger(parser, split, 5);
                String dictValue = optionTester.testDict(parser, dictionary, force,false,true);
                String ngramValue = optionTester.testNgram(parser, ngram, force,false,true);
                String dmcValue = optionTester.testDMC(parser, dmc, force,false,true);
                String hmmValue = optionTester.testHMM(parser, hmm, force,false,true);
                String vlmcValue = optionTester.testPST(parser, vlmc, force,false,true);
                Unit u = optionTester.testUnit(parser, unit);
                r.train(corpusValue, dictValue, ngramValue, dmcValue,hmmValue,vlmcValue, lengthValue, splitValue,iterValue, u);
            } else if (actionValue == Action.DEV) {
                Mode modeValue = optionTester.testMode(parser, mode);
                boolean fValue = optionTester.testBoolean(parser, frequency);
                String dictValue = optionTester.testDict(parser, dictionary, false,true,true);

                if (modeValue == Mode.DMC) {
                    String dmcValue = optionTester.testDMC(parser, dmc, false,true,true);
                    r.dev(corpusValue, dictValue, dmcValue, lengthValue, modeValue, fValue);
                } else if (modeValue == Mode.HMM) {
                    String hmmValue = optionTester.testHMM(parser,hmm,false,true,true);
                    r.dev(corpusValue,dictValue,hmmValue,iterValue,modeValue,fValue);
                } else if (modeValue == Mode.VLMC) {
                    String vlmcValue = optionTester.testPST(parser,vlmc,false,true,true);
                    r.dev(corpusValue,dictValue,vlmcValue,iterValue,modeValue,fValue);

                } else {

                    String ngramValue = optionTester.testNgram(parser, ngram, false,true,true);
                    r.dev(corpusValue, dictValue, ngramValue, lengthValue, modeValue, fValue);
                }
            } else if ( actionValue == Action.TEST ) {
                Mode modeValue = optionTester.testMode(parser, mode);
                boolean fValue = optionTester.testBoolean(parser, frequency);
                String dictValue = optionTester.testDict(parser, dictionary, false,true,true);
                String fileValue = null;

                if (modeValue == Mode.DMC) {
                    fileValue = optionTester.testDMC(parser, dmc, false,true,true);
                } else if (modeValue == Mode.HMM || modeValue == Mode.HMMEnd) {
                    fileValue = optionTester.testHMM(parser,hmm,false,true,true);
                } else if (modeValue == Mode.NGRAM) {
                    fileValue = optionTester.testNgram(parser, ngram, false,true,true);
                }

                if(fileValue!=null) {
                    r.test(corpusValue,dictValue,fileValue,lengthValue,modeValue,fValue);
                }


            }

        } catch (IllegalOptionValueException ex) {
            System.err.println("illegall option value exception: " + ex.getMessage());
        } catch (UnknownOptionException ex) {
            System.err.println("unknown option value exception: " + ex.getMessage());
        }

    }
}
 