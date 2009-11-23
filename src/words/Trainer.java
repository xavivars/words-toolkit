/*
 * Copyright (C) 2008
 *
 * Author:
 *  Xavier Ivars i Ribes <xavi@infobenissa.com>
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
import words.Run.Mode;

public class Trainer {

    private Dictionary dict;
    private Ngram ngrams;
    private DMM dmc;
    private HMMPre hmm;
    private HMM hmmEnd;
    private PST vlmc;
    private boolean ngramTrained;
    private boolean dictTrained;
    private boolean dmcTrained;
    private boolean hmmTrained;
    private boolean vlmcTrained;
    private int ngramSize;
    private int hmmIter;
    private String filename;
    private Unit unit;
    private int split;

    public Trainer() {
        this(5, 5, 5);
    }

    public Trainer(int i, int s, int h) {
        this.dictTrained = false;
        this.ngramTrained = false;
        this.dmcTrained = false;
        this.hmmTrained = false;
        this.dict = null;
        this.ngrams = null;
        this.dmc = null;
        this.hmm = null;
        this.hmmEnd = null;
        this.vlmc = null;
        this.ngramSize = i;
        this.hmmIter = h;
        this.split = s;
        this.unit = Unit.CHAR;
    }

    public void setUnit(Unit u) {
        this.unit = u;
    }

    public Unit getUnit() {
        return unit;
    }

    public Dictionary getDictionary() {
        return dict;
    }

    public Ngram getNgram() {
        return ngrams;
    }

    public DMM getDMC() {
        return dmc;
    }

    public HMMPre getHMM() {
        return hmm;
    }

    public HMM getHMMEnd() {
        return hmmEnd;
    }

    public PST getVLMC() {
        return vlmc;
    }

    public void train(String f) {
        this.filename = f;
    }

    private void trainDict() {
        System.out.println("Training...");
        dict = new Dictionary();
        System.out.print("... dict");
        dict.train(filename);
        System.out.println(" OK");
        this.dictTrained = true;
    }

    private void trainNgram() {
        ngrams = new Ngram(this.ngramSize);
        ngrams.setUnit(this.unit);
        System.out.print("... ngram");
        ngrams.train(filename);
        System.out.println(" OK");

        this.ngramTrained = true;
    }

    private void trainDMC() {
        dmc = new DMM(this.split);
        dmc.setUnit(this.unit);
        System.out.print("... dmc");
        dmc.train(filename);
        System.out.println(" OK");

        this.dmcTrained = true;
    }

    private void trainHMM() {
        if(hmm == null) {
            hmm = new HMMPre(this.ngramSize);
            System.out.print("... hmm ");
            hmm.train(filename);

            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... smoothing "+i+" ");
                hmm.expectationMaximization(filename);
            }
            System.out.println(" OK");
        }
        this.hmmTrained = true;
    }

    private void trainHMMEnd() {
        if(hmmEnd == null) {
            hmmEnd = new HMM(this.ngramSize);
            System.out.print("... hmmEnd ");
            hmmEnd.train(filename);

            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... smoothing "+i+" ");
                hmmEnd.expectationMaximization(filename);
            }
            System.out.println(" OK");
        }
    }

    private void trainCleanHMMEnd() {
        if(hmmEnd == null) {
            hmmEnd = new HMM(this.ngramSize);
            System.out.print("... hmmEnd ");
            hmmEnd.train(filename);
            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... initial "+i+" ");
                hmmEnd.expectationMaximization(filename);
            }
            System.out.println(" OK");
        }
        hmmEnd.clean();
        this.hmmTrained = true;
    }

    private void trainCleanHMM() {
        if(hmm == null) {
            hmm = new HMMPre(this.ngramSize);
            System.out.print("... hmm ");
            hmm.train(filename);
            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... initial "+i+" ");
                hmm.expectationMaximization(filename);
            }
            System.out.println(" OK");
        }
        hmm.clean();
        this.hmmTrained = true;
    }

    private void trainReestimatedHMMEnd() {
        if(hmmEnd == null) {
            hmmEnd = new HMM(this.ngramSize);
            System.out.print("... hmmEnd ");
            hmmEnd.train(filename);
            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... initial smoothing "+i+" ");
                hmmEnd.expectationMaximization(filename);
            }
            System.out.println(" OK");
            hmmEnd.clean();
        }
        hmmEnd.restart();
        for(int i = 1; i <= hmmIter; ++i) {
            System.out.print("\n... reestimating smoothing "+i+" ");
            hmmEnd.expectationMaximization(filename);
        }
        System.out.println(" OK");
        this.hmmTrained = true;
    }

    private void trainReestimatedHMM() {
        if(hmm == null) {
            hmm = new HMMPre(this.ngramSize);
            System.out.print("... hmm ");
            hmm.train(filename);
            for(int i = 1; i <= hmmIter; ++i) {
                System.out.print("\n... initial smoothing "+i+" ");
                hmm.expectationMaximization(filename);
            }
            System.out.println(" OK");
            hmm.clean();
        }
        hmm.restart();
        for(int i = 1; i <= hmmIter; ++i) {
            System.out.print("\n... reestimating smoothing "+i+" ");
            hmm.expectationMaximization(filename);
        }
        System.out.println(" OK");
        this.hmmTrained = true;
    }

    private void trainVLMC() {
        vlmc = new PST();
        System.out.print("... dmc");
        vlmc.train(filename);
        System.out.println(" OK");

        this.vlmcTrained = true;
    }

    public void load(String fileDict, String fileOther, Mode mode) {
        if(fileDict!=null) {
            dict = new Dictionary();
            dict.getText(fileDict);
        }

        if (mode == Mode.DMC) {
            dmc = new DMM();
            dmc.load(fileOther);
        } else if (mode == Mode.HMM) {
            hmm = new HMMPre();
            hmm.load(fileOther);
        } else if (mode == Mode.HMMEnd) {
            hmmEnd = new HMM();
            hmmEnd.load(fileOther);
        } else if (mode == Mode.VLMC) {
            vlmc = new PST();
            vlmc.load(fileOther);
        } else {
            ngrams = new Ngram(this.ngramSize);
            ngrams.getText(fileOther);
        }
    }

    public void saveTraining(String fileDict, String fileNgrams, String fileDMC, String fileHMM, String fileVLMC, boolean clear) {
        if (fileDict != null) {

            trainDict();
            dict.save(fileDict);
            if (clear) {
                dict = null;
                dictTrained = false;
            }
        }

        if (fileNgrams != null) {

            trainNgram();
            ngrams.save(fileNgrams);
            if (clear) {
                ngrams = null;
                ngramTrained = false;
            }
        }

        if (fileDMC != null) {

            trainDMC();
            dmc.save(fileDMC);
            if (clear) {
                dmc = null;
                ngramTrained = false;
            }
        }

        if (fileHMM != null) {
            trainHMM();
            hmm.save(fileHMM);
            //trainCleanHMM();
            //hmm.save(fileHMM+".clean");
            //trainReestimatedHMM();
            //hmm.save(fileHMM+".reestimated");

            if (clear) {
                hmm = null;
                hmmTrained = false;
            }

            trainHMMEnd();
            hmmEnd.save(fileHMM+".end");
            trainCleanHMMEnd();
            hmmEnd.save(fileHMM+".clean.end");
            trainReestimatedHMMEnd();
            hmmEnd.save(fileHMM+".reestimated.end");

        }

        if (fileVLMC != null) {

            trainVLMC();
            vlmc.save(fileVLMC);
            if (clear) {
                vlmc = null;
                vlmcTrained = false;
            }
        }
    }

    public boolean isTrained() {
        return (this.dictTrained && this.ngramTrained);
    }

    public void loadParams(String filename) {
        // to-do
    }

    public void saveParams(String filename) {
        // to-do        
    }
}
