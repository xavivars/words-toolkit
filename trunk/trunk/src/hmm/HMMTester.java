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
package words.hmm;

import words.*;


public class HMMTester {

    public static void main(String[] args) {

        String filename = "/home/xavi/Documents/tesina/svn/tests/aba";
        String xml = "/home/xavi/Documents/tesina/tests/hmm.xml";

        HMMPre markov = null;
        long tm = System.currentTimeMillis();
        long ini = tm;

        System.out.println("TIME:"+tm);
        markov = new HMMPre();
        markov.train(filename);
        //markov.print();
        markov = null;
        tm = System.currentTimeMillis() - ini;
        System.out.println("CREATED:"+tm);
        markov = new HMMPre();
        markov.load(xml);
        markov = null;
        tm = System.currentTimeMillis() - ini;
        System.out.println("LOADED:"+tm);
    }
}
