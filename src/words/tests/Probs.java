/*
 * Copyright (C) 2009
 *
 * Authors:
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
package words.tests;

import java.io.IOException;
import words.hmm.Value;
import words.utils.TextReader;
import words.utils.WordType;

/**
 *
 * @author xavi
 */
public class Probs {

    public Probs(String a,String b) {
        try {
            String wda, wdb;
            Value<String> pa = new Value<String>();
            Value<String> pb = new Value<String>();
            TextReader txa = new TextReader(a, WordType.LETTERS);
            TextReader txb = new TextReader(b, WordType.LETTERS);
            int i = 0;
            while ((wda = txa.nextWord()) != null) {
                wdb = txb.nextWord();
                pa.addValue(wda,1);
                pb.addValue(wdb,1);
                ++i;

                //if((i%10)==0) {
                    double v = this.prob("a",i,pa) * (Math.log(prob("a",i,pa)/prob("a",i,pb))/Math.log(2));
                    System.out.println(""+i+"\t"+v);
                //}
            }
        } catch (IOException ex) {
            
        }
    }

    public double prob(String w,int t,Value<String> v) {
        return v.getValue(w)/t;
    }

    static public void main(String [] args) {
        new Probs(args[0],args[1]);
    }
}
