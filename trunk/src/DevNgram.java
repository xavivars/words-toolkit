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

import java.io.IOException;
import java.util.ArrayList;
import words.utils.*;

public class DevNgram extends Developer {

    private Int[] orders;

    public DevNgram(Trainer tr) {
        super(tr);

        int or = (int)tr.getNgram().order;
        ArrayList<Int> ords = new ArrayList<Int>();
        ArrayList<String> nms = new ArrayList<String>();

        for (int i = 2; i <= or; i++) {
            ords.add(new Int(i));
            nms.add("" + i + "-gram");
        }

        orders = (Int []) ords.toArray(new Int[0]);
        names = (String []) nms.toArray(new String[0]);
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
            
            mapes = new Classificacio[orders.length];

            for (int i = 0; i < orders.length; ++i) {
                mapes[i] = new Classificacio();
            }

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

            //if(dict.getWord(w)>5) {
            
                for (int i = 0; i < orders.length; ++i) {

                    double resultat = ngrams.wordProb(w,orders[i].getValue()) * (1 + w.length() * lambda);

                    m[i].add(w, resultat);
                }
            //}

        }
    }

    /**
     * Obtains probability of a word
     * @param w word
     */
    @Override
    public double prob(String w) {
        return ngrams.wordProb(w);
    }

    @Override
    public int getSize() {
        return ngrams.getSize();
    }
}
