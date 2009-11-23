package words;

import java.util.Set;
import words.Run.Mode;
import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.*;

/**
 *
 * @author xavi
 */
public class Known {

    public Known() {
    }

    public static void main(String[] args) {

        CmdOptions parser = new CmdOptions();
        CmdOptionTester optionTester = new CmdOptionTester();

        // dict file
        Option dict = parser.addStringOption('d', "dict");

        // ngram file
        Option ngram = parser.addStringOption('n', "ngram");

        // dmc file
        Option dmc = parser.addStringOption('k', "dmc");

        // hmm file
        Option hmm = parser.addStringOption('h', "hmm");

        // vlmc file
        Option vlmc = parser.addStringOption('t', "vlmc");

        // n-gram size
        Option length = parser.addIntegerOption('l', "length");

        // verbose option
        Option verbose = parser.addBooleanOption('v', "verbose");


        try {

            Trainer tr;
            Developer dv;

            parser.parse(args);

            String fileDict = optionTester.testDict(parser, dict, false, true, true);
            String fileNgrams = optionTester.testNgram(parser, ngram, false, true, false);
            String fileDMC = optionTester.testDMC(parser, dmc, false, true, false);
            String fileHMM = optionTester.testHMM(parser, hmm, false, true, false);
            String fileVLMC = optionTester.testHMM(parser, vlmc, false, true, false);
            boolean v = optionTester.testBoolean(parser, verbose);

            Dictionary d = new Dictionary();
            d.load(fileDict);
            Set<String> wds = d.getWords();

            System.out.println("Total words: "+d.getTotalWords());

            if (fileNgrams != null) {
                int lengthValue = optionTester.testInteger(parser, length, 5);
                tr = new Trainer(lengthValue, 0, 0);
                tr.load(null, fileNgrams, Mode.NGRAM);
                dv = new DevNgram(tr);
                for (String wd : wds) {
                    dv.addKnownProb(wd);
                }
                //dv.setLambda(x);

                if(v) {
                    System.out.println("Ngrams total prob: "+dv.getKnownProb());
                    System.out.println("Ngrams lambda par: "+dv.getLambda());
                }

                dv = null;
                tr = null;
            }

            if (fileDMC != null) {
                tr = new Trainer();
                tr.load(null, fileDMC, Mode.DMC);
                dv = new DevDMC(tr);
                for (String wd : wds) {
                    dv.addKnownProb(wd);
                }
                //dv.setLambda(x);

                if(v) {
                    System.out.println("DMC total prob: "+dv.getKnownProb());
                    System.out.println("DMC lambda par: "+dv.getLambda());
                }

                dv = null;
                tr = null;
            }

            if (fileHMM != null) {
                tr = new Trainer();
                tr.load(null, fileHMM, Mode.HMMEnd);
                dv = new DevHMM(tr);
                for (String wd : wds) {
                    dv.addKnownProb(wd);
                }
                //dv.setLambda(x);

                if(v) {
                    System.out.println("HMM total prob: "+dv.getKnownProb());
                    System.out.println("HMM lambda par: "+dv.getLambda());
                }

                dv = null;
                tr = null;
            }

            if (fileVLMC != null) {
                tr = new Trainer();
                tr.load(null, fileVLMC, Mode.VLMC);
                dv = new DevVLMC(tr);
                for (String wd : wds) {
                    dv.addKnownProb(wd);
                }
                //dv.setLambda(x);

                if(v) {
                    System.out.println("VLMC total prob: "+dv.getKnownProb());
                    System.out.println("VLMC lambda par: "+dv.getLambda());
                }

                dv = null;
                tr = null;
            }

        } catch (IllegalOptionValueException ex) {
            System.err.println("illegall option value exception: " + ex.getMessage());
        } catch (UnknownOptionException ex) {
            System.err.println("unknown option value exception: " + ex.getMessage());
        }

    }
}

