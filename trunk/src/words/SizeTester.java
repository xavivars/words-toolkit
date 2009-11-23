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

import java.util.LinkedList;
import words.utils.Functions;
import words.utils.Pair;

/**
 *
 * @author xavi
 */
public class SizeTester {

    public static void main(String[] args) {
        new SizeTester(args[0], args[1], Integer.parseInt(args[2]));
    }



    @SuppressWarnings("unchecked")
    public SizeTester(String corpus, String path, int max) {

        int[] sizes = {10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
// 60304248
        LinkedList<Pair<String, Model>> m = new LinkedList<Pair<String, Model>>();

        m.add(new Pair("Ngram3", new Ngram(3)));
        m.add(new Pair("Ngram4", new Ngram(4)));
        m.add(new Pair("Ngram5", new Ngram(5)));
        m.add(new Pair("Ngram6", new Ngram(6)));
        m.add(new Pair("Ngram7", new Ngram(7)));
        m.add(new Pair("DMC10", new DMM(10)));
        m.add(new Pair("DMC4", new DMM(4)));
        m.add(new Pair("DMC3", new DMM(3)));
        m.add(new Pair("HMM", new HMM()));
        m.add(new Pair("VLMC", new PST()));

        for (Pair<String, Model> model : m) {
            System.out.print("\t" + model.getFirst());
        }
        System.out.println();

        m.clear();

        for (int size : sizes) {
            System.out.print("" + size);

            String ncorpus = Functions.sizedCorpus(corpus, size);

            m.add(new Pair("VLMC_01",new PST(0.1)));
            /*m.add(new Pair("Ngram1",new Ngram(1)));
            m.add(new Pair("Diccionari",new Dictionary()));
            m.add(new Pair("Ngram3", new Ngram(3)));
            m.add(new Pair("Ngram4", new Ngram(4)));
            m.add(new Pair("Ngram5", new Ngram(5)));
            m.add(new Pair("Ngram6", new Ngram(6)));
            m.add(new Pair("Ngram7", new Ngram(7)));
            m.add(new Pair("DMC5", new DMC(10)));
            m.add(new Pair("DMC4", new DMC(4)));
            m.add(new Pair("DMC3", new DMC(3)));
            m.add(new Pair("HMM", new HMMEnd()));
            m.add(new Pair("VLMC", new VLMC()));*/

            while (!m.isEmpty()) {
                Pair<String, Model> model = m.removeFirst();

                System.err.print("Training..." + model.getFirst() + "." + size + "...\t");
                model.getSecond().train(ncorpus);
                model.getSecond().save(path + "/" + model.getFirst() + "." + size + ".xml");
                System.out.print("\t" + model.getSecond().getSize());
                System.err.println("... done;");

                model = null;
            }
            System.out.println();
            if (size > max) {
                break;
            }

            m.clear();
        }
    }
}
