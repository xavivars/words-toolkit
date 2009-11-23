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

import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;

/**
 *
 * @author xavi
 */
public class Train {

    public Train(String[] args) {

        CmdOptions parser = new CmdOptions();
        CmdOptionTester optionTester = new CmdOptionTester();

        // corpus file
        Option corpus = parser.addStringOption('c', "corpus");

        // dictionary file
        Option dictionary = parser.addStringOption('d', "dictionary");

        // ngram file
        Option ngram = parser.addStringOption('n', "ngram");

        // dmc file
        Option dmc = parser.addStringOption('k', "dmc");

        // hmm file
        Option hmm = parser.addStringOption('h', "hmm");

        // hmm# file
        Option hmmEnd = parser.addStringOption('#', "hmm#");

        // vlmc file
        Option vlmc = parser.addStringOption('t', "vlmc");

        // y | n
        Option frequency = parser.addBooleanOption('f', "frequency");

        // n-gram size
        Option length = parser.addIntegerOption('l', "length");

        // hmm iters
        Option iter = parser.addIntegerOption('i', "iter");

        // force overwriting
        Option over = parser.addBooleanOption('r', "force");

        // dmc split condition
        Option split = parser.addIntegerOption('s', "split");

        // verbose option
        Option verbose = parser.addBooleanOption('v', "verbose");



        try {
            parser.parse(args);

            boolean force = optionTester.testBoolean(parser, over);
            int splitValue = optionTester.testInteger(parser, split, 5);
            String fileDict = optionTester.testDict(parser, dictionary, force, false,false);
            String fileNgrams = optionTester.testNgram(parser, ngram, force, false,false);
            String fileDMC = optionTester.testDMC(parser, dmc, force, false,false);
            String fileHMM = optionTester.testHMM(parser, hmm, force, false,false);
            String fileHMMEnd = optionTester.testHMMEnd(parser, hmmEnd, force, false,false);
            String fileVLMC = optionTester.testPST(parser, vlmc, force, false,false);
            String text = optionTester.testCorpus(parser, corpus);
            int lengthValue = optionTester.testInteger(parser, length, 5);
            int iterValue = optionTester.testInteger(parser, iter, -1);
            boolean verb = optionTester.testBoolean(parser, verbose);

            Model m;

            if(verb) System.out.println("Verbose mode ON");

            if (fileDict != null) {
                System.out.print("Training dict... ");
                m = new Dictionary();
                m.train(text,verb);
                m.save(fileDict);
                m = null;
                System.out.println("... done");
            }

            if (fileNgrams != null) {
                System.out.print("Training ngrams... ");
                m = new Ngram(lengthValue);
                m.train(text,verb);
                m.save(fileNgrams);
                m = null;
                System.out.println("... done");
            }

            if (fileDMC != null) {
                System.out.print("Training dmc... ");
                m = new DMM(splitValue);
                m.train(text,verb);
                m.save(fileDMC);
                m = null;
                System.out.println("... done");
            }

            if (fileHMM != null) {
                System.out.print("Training hmm... ");
                m = new HMMPre();
                m.train(text,verb);
                ((HMMPre)m).retrain(text,iterValue,verb);
                m.save(fileHMM);
                System.out.println("... done");

                System.out.print("Cleaning hmm... ");
                ((HMMPre)m).clean();
                m.save(fileHMM.replaceAll(".xml", ".cleaned.xml"));
                System.out.println("... done");

                System.out.print("Retraining hmm... ");
                ((HMMPre)m).restart();
                ((HMMPre)m).retrain(text,iterValue,verb);
                m.save(fileHMM.replaceAll(".xml", ".reestimated.xml"));
                m = null;
                System.out.println("... done");
                
            }

            if (fileHMMEnd != null) {
                System.out.print("Training hmm#... ");
                m = new HMM();
                m.train(text,verb);
                ((HMM)m).retrain(text,iterValue,verb);
                m.save(fileHMMEnd);
                System.out.println("... done");
                
                System.out.print("Cleaning hmm#... ");
                ((HMM)m).clean();
                m.save(fileHMMEnd.replaceAll(".xml", ".cleaned.xml"));
                System.out.println("... done");

                System.out.print("Rertaining hmm#... ");
                ((HMM)m).restart();
                ((HMM)m).retrain(text,iterValue,verb);
                m.save(fileHMMEnd.replaceAll(".xml", ".reestimated.xml"));
                m = null;
                System.out.println("... done");
            }

            if(fileVLMC != null) {
                System.out.print("Training vlmc... ");
                m = new PST();
                m.train(text,verb);
                m.save(fileVLMC);
                m = null;
                System.out.println("... done");
            }

        } catch (IllegalOptionValueException ex) {
            System.err.println("illegall option value exception: " + ex.getMessage());
        } catch (UnknownOptionException ex) {
            System.err.println("unknown option value exception: " + ex.getMessage());
        }

    }

    public static void main(String[] args) {

        Train t = new Train(args);
    }
}
