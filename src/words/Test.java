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

import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;

/**
 *
 * @author xavi
 */
public class Test {

    public Test(String[] args) {

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

        // Split file
        Option splitFile = parser.addStringOption('o', "output");


        try {
            parser.parse(args);

            boolean force = optionTester.testBoolean(parser, over);
            int splitValue = optionTester.testInteger(parser, split, 5);
            String fileDict = optionTester.testDict(parser, dictionary, force, true, true);
            String fileNgrams = optionTester.testNgram(parser, ngram, force, true, false);
            String fileDMC = optionTester.testDMC(parser, dmc, force, true, false);
            String fileHMM = optionTester.testHMM(parser, hmm, force, true, false);
            String fileHMMEnd = optionTester.testHMMEnd(parser, hmmEnd, force, true, false);
            String fileVLMC = optionTester.testPST(parser, vlmc, force, true,false);
            String fileSplit = optionTester.testFile(parser, splitFile);
            String text = optionTester.testCorpus(parser, corpus);
            int lengthValue = optionTester.testInteger(parser, length, 5);
            boolean verb = optionTester.testBoolean(parser, verbose);

            Model m;
            Dictionary d;

            if (verb) {
                System.out.println("Verbose mode ON");
            }

            d = new Dictionary();
            d.load(fileDict);

            if (fileNgrams != null) {
                System.out.print("Testing ngrams... ");
                m = new Ngram(lengthValue);
                m.load(fileNgrams);
                m.test(text,d);
                if (fileSplit != null) {
                    m.splitFile(text,fileSplit.replaceAll(".txt", ".ng.txt"),d);
                }
                System.out.println("... done");

                System.out.println("MODEL SIZE: "+m.getSize());
                System.out.println("TEST RESULT: " + m.getLastTest());
                System.out.println("TEST LENGTH: " + m.getLastLength());
                System.out.println("TEST TOTAL: " + m.getLastValue());

                m = null;
            }

            if (fileDMC != null) {
                System.out.print("Testing dmc... ");
                m = new DMM(splitValue);
                m.load(fileDMC);
                m.test(text,d);
                if (fileSplit != null) {
                    m.splitFile(text,fileSplit.replaceAll(".txt", ".dmc.txt"),d);
                }
                System.out.println("... done");

                System.out.println("MODEL SIZE: "+m.getSize());
                System.out.println("TEST RESULT: " + m.getLastTest());
                System.out.println("TEST LENGTH: " + m.getLastLength());
                System.out.println("TEST TOTAL: " + m.getLastValue());

                m = null;
            }

            if (fileHMM != null) {
                System.out.print("Testing hmm... ");
                m = new HMMPre();
                m.load(fileHMM);
                m.test(text,d);
                if (fileSplit != null) {
                    m.splitFile(text, fileSplit.replaceAll(".txt", ".hmm.txt"),d);
                }
                System.out.println("... done");

                System.out.println("MODEL SIZE: "+m.getSize());
                System.out.println("TEST RESULT: " + m.getLastTest());
                System.out.println("TEST LENGTH: " + m.getLastLength());
                System.out.println("TEST TOTAL: " + m.getLastValue());

                m = null;
            }

            if (fileHMMEnd != null) {
                System.out.print("Testing hmm... ");
                m = new HMM();
                m.load(fileHMMEnd);
                m.test(text,d);
                if (fileSplit != null) {
                    m.splitFile(text, fileSplit.replaceAll(".txt", ".hmmEnd.txt"),d);
                }
                System.out.println("... done");

                System.out.println("MODEL SIZE: "+m.getSize());
                System.out.println("TEST RESULT: " + m.getLastTest());
                System.out.println("TEST LENGTH: " + m.getLastLength());
                System.out.println("TEST TOTAL: " + m.getLastValue());

                m = null;
            }

            if (fileVLMC != null) {
                System.out.print("Testing vlmc... ");
                m = new PST();
                m.load(fileVLMC);
                m.test(text,d);
                if (fileSplit != null) {
                    m.splitFile(text,fileSplit.replaceAll(".txt", ".vlmc.txt"),d);
                }
                System.out.println("... done");

                System.out.println("MODEL SIZE: "+m.getSize());
                System.out.println("TEST RESULT: " + m.getLastTest());
                System.out.println("TEST LENGTH: " + m.getLastLength());
                System.out.println("TEST TOTAL: " + m.getLastValue());

                m = null;
            }

            d = null;

        } catch (IllegalOptionValueException ex) {
            System.err.println("illegall option value exception: " + ex.getMessage());
        } catch (UnknownOptionException ex) {
            System.err.println("unknown option value exception: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {

        Test t = new Test(args);
    }
}
