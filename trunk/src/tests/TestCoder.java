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

import words.utils.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestCoder {

    private BufferedReader br;
    
    public TestCoder() {
        br = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public Coder testNgram(String[] args) {
        HuffmanNgrams hf = new HuffmanNgrams(255);
        hf.setMinOrder(1);
        hf.setMaxSize(65000);
        //hf.getText(args[1]);
        hf.getText("ordenat.xml");
        hf.createCodes();

        CoderNgramsDP cd = new CoderNgramsDP(hf);

        System.out.println("Coder generat...");

        if (args.length > 2) {
            System.out.println("Codificant " + args[2]);
            if (args.length > 3) {
                cd.encodeAll(args[2]);
            } else {
                cd.encode(args[2]);
            }
        }

        return cd;
    }

    public Coder testNgramBT(String[] args) {
        HuffmanNgrams hf = new HuffmanNgrams(255);
        hf.setMinOrder(1);
        hf.setMaxSize(65000);
        //hf.getText(args[1]);
        hf.getText("ordenat.xml");
        hf.createCodes();

        CoderNgrams cd = new CoderNgrams(hf);

        System.out.println("Coder generat...");

        if (args.length > 2) {
            System.out.println("Codificant " + args[2]);
            if (args.length > 3) {
                cd.encodeAll(args[2]);
            } else {
                cd.encode(args[2]);
            }
        }

        return cd;
    }
    
    public Coder testWords(String[] args) {
        HuffmanWords hf = new HuffmanWords(255);
        //hf.getText(args[0]);
        hf.getText("dict.xml");
        hf.createCodes();

        CoderWords cd = new CoderWords(hf);

        System.out.println("Coder generat...");

        if (args.length > 2) {
            System.out.println("Codificant " + args[2]);
            if (args.length > 3) {
                cd.encodeAll(args[2]);
            } else {
                cd.encode(args[2]);
            }
        }

        return cd;
    }

    private void test(Coder cd,Coder cd2,Coder cd3) {
        cd.encode("cero");
        cd3.encode("cero");
        cd2.encode("cero");
        
        cd.encode("coche");
        cd3.encode("coche");
        cd2.encode("coche");
        
        
        cd.encode("avión");
        cd3.encode("avión");
        cd2.encode("avión");
        
        cd.encode("gente");
        cd3.encode("gente");
        cd2.encode("gente");
        
        cd.encode("mismo");
        cd3.encode("mismo");
        cd2.encode("mismo");
        
        cd.encode("yo");
        cd3.encode("yo");
        cd2.encode("yo");
        
        cd.encode("esperanza");
        cd3.encode("esperanza");
        cd2.encode("esperanza");

        cd.encode("matemáticas");
        cd3.encode("matemáticas");
        cd2.encode("matemáticas");
        
        cd.encode("entrada");
        cd3.encode("entrada");
        cd2.encode("entrada");
        
        cd.encode("camión");
        cd3.encode("camión");
        cd2.encode("camión");
        
        cd.encode("libro");
        cd3.encode("libro");
        cd2.encode("libro");
        
        cd.encode("configuración");
        cd3.encode("configuración");
        cd2.encode("configuración");
        
        cd.encode("puerta");
        cd3.encode("puerta");
        cd2.encode("puerta");
        
        cd.encode("dicotomía");
        cd3.encode("dicotomía");
        cd2.encode("dicotomía");
        
        cd.encode("prototipado");
        cd3.encode("prototipado");
        cd2.encode("prototipado");
        
        cd.encode("biología");
        cd3.encode("biología");
        cd2.encode("biología");

    }

    public static void main(String[] args) {

        TestCoder tc = new TestCoder();

        Coder words = tc.testWords(args);
        Coder ngrams = tc.testNgram(args);
        Coder ngramsBT = tc.testNgramBT(args);
        
        
        tc.test(words,ngrams,ngramsBT);
        /*
        String str = tc.readLine();
        while(str!=null) {
            if(!str.isEmpty()) {
                words.encode(str);
                ngrams.encode(str);
                str = tc.readLine();
            } else {
                str=null;
            }
        }*/
        
    //cd.encodeAll("australopitecus");
    //cd.encodeAll("supercalifragilisticoespialidoso");
    //cd.encodeAll("supercalifragilístico");
    //cd.encodeAll("ymu");
        /*try {
    String filename = args[1];

    WordScanner scanner = new WordScanner(0);
    String word;
    scanner.scanFile(filename, EncodingDetector.UTF8);

    int i = 0;
    while ((word = scanner.nextWord()) != null && (++i<50)) {
    //System.out.println(word.toLowerCase());
    cd.encode(word.toLowerCase());
    //System.out.println();
    }

    } catch (IOException iOException) {
    }*/

    }
    
    public String readLine() {
        try {
            return br.readLine();
        } catch (IOException ex) {
            return null;
        }
    }
}
