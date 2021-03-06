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

import java.io.IOException;
import words.utils.Classificacio;
import words.utils.EncodingDetector;
import words.utils.TextReader;
import words.utils.WordType;

public class DevHMMPre extends Developer {

    public DevHMMPre(Trainer tr) {

        super(tr);

        names = new String[1];
        names[0] = "hmm";
    }

    @Override
    public void adjust(String filename) {
        this.adjust(filename, false);
    }

    @Override
    public void adjust(String filename, boolean freq) {
        try {

            this.frequency = freq;

            TextReader scanner = new TextReader(filename, WordType.LETTERS);
            String word;

            mapes = new Classificacio[1];

            mapes[0] = new Classificacio();

            while ((word = scanner.nextWord()) != null) {
                paramTest(word.toLowerCase(), mapes);
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Obtains probability of a word.
     * @param w word
     * @param m map where ranking is stored
     */
    @Override
    protected void paramTest(String w, Classificacio[] m) {
        if (this.frequency || !(calculat.containsKey(w))) {

            calculat.put(w, true);

            //if (dict.getWord(w) > 5) {

                double resultat = hmm.wordProb(w) * (1 + w.length() * lambda);

                m[0].add(w, resultat);
            //}
        }
    }

        /**
     * Obtains probability of a word
     * @param w word
     */
    @Override
    public double prob(String w) {
        return hmm.wordProb(w);
    }

    public double logViterbiLikelihood(String fileName) {
        TextReader txreader = new TextReader(fileName, WordType.LETTERS);
        String word;
        double value = 0.0;
        try {
            while ((word = txreader.nextWord()) != null) {
                
                double d = hmm.probViterbi(word);
                value -= Math.log(d);
                System.out.println(hmm.split(word)+" ("+d+")");
            }
        } catch (java.io.IOException x) {
            System.err.println(x);
        }
        return value;
    }

    public double probViterbi(String word) {
        return hmm.probViterbi(word);
    }

    public String split(String word){
        return hmm.split(word);
    }

    @Override
    public int getSize() {
        return hmm.getSize();
    }
}
