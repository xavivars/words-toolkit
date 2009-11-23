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
package words.tests;

import words.*;


public class DMCTester {

    public static void main(String[] args) {
        /* DynamicMarkov dm = new DynamicMarkov();
        
        dm.setStatus(DynamicMarkov.Status.DEV);
        
        //dm.train(args[0]+"$");
        dm.train("100100100100$");
        dm.train("100100$");
        dm.train("100100100100$");
        dm.train("100100$");
        dm.train("100100100100$");
        dm.train("100100$");
        dm.train("100100100$");
        dm.train("100100100100$");
        dm.print(); */
        
        System.out.println("Temps:"+System.currentTimeMillis());
        DMM markov = null;
        markov = new DMM();
        markov.getText(args[0]);
        System.out.println("File parsed");
        System.out.println("Temps:"+System.currentTimeMillis());
        markov.makeMarkov();
        System.out.println("Markov built");
        System.out.println("Temps:"+System.currentTimeMillis());
        
        markov.save(args[1]);
        System.out.println("Markov written");
        System.out.println("Temps:"+System.currentTimeMillis());
        
        
        
    }
}
