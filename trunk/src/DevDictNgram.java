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
import words.utils.Classificacio;
import words.utils.TextReader;
import words.utils.WordType;

public class DevDictNgram extends Developer {

    protected double [] lambdas = {1000000,1,0.000001};
    
    public DevDictNgram(Trainer tr) {
        super(tr);
        String [] n = {"b.1000000","c.1","d.0000001","a.dict","e.ngram"};
        this.names = n;
    }

    @Override
    protected void paramTest(String w, Classificacio[] m) {
        
        if (this.frequency || !(calculat.containsKey(w))) {
        
            calculat.put(w,true);
            
            double resdict = (dict.getWord(w)>5)?1:0;
            double resngram = ngrams.wordProb(w)*(1+w.length());
            double resultat = 0.0;

            for (int i = 0; i < lambdas.length; ++i) {
                
                
                // calculem el resultat ponderat
                resultat = resdict + this.lambdas[i] * resngram;
               // normalitzem la probabilitat dels ngrames
                resultat = resultat / (1 + this.lambdas[i]);


                m[i].add(w,resultat);
            }
          
            m[lambdas.length]=m[lambdas.length].add(w,resdict);
            m[lambdas.length+1]=m[lambdas.length+1].add(w,resngram);
            
        }
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
            
            mapes = new Classificacio[lambdas.length+2];

            for (int i = 0; i < lambdas.length+2; ++i) {
                mapes[i] = new Classificacio();
            }

            while ((word = scanner.nextWord()) != null) {
                paramTest(word.toLowerCase(), mapes);
            }
        } catch (IOException ex) {
            
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
