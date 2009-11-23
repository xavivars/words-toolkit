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
package words.utils;

import java.text.DecimalFormat;

public class Probability {

    public static double logprob2ppl(double prob) {
        return Math.pow(10, -prob);
    }

    public static void show(String word, Double prob, Double entropy, Double perplexity) {

        DecimalFormat deci4 = new DecimalFormat("0.0000");

        double logprob = Math.log10(prob);
        double size = (word.length() + 1);
        double lg2ppl = logprob2ppl(logprob / size);
        double lg2ppl1 = logprob2ppl(logprob / (size - 1));

        System.out.print("word: " + word);
        System.out.print("prob= " + prob);
        System.out.print(" logprob= " + deci4.format(logprob));
        if (entropy != null) {
            System.out.print(" entropy= " + deci4.format(entropy));
        }
        if (perplexity != null) {
            System.out.print(" entropy= " + deci4.format(perplexity));
        }
        System.out.print(" ppl= " + deci4.format(lg2ppl));
        System.out.println(" ppl1= " + deci4.format(lg2ppl1));
    }

    public static void show(String word, Double prob) {
        show(word, prob, null, null);
    }
}
