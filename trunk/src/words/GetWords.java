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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import words.utils.CmdOptionTester;
import words.utils.CmdOptions;
import words.utils.CmdOptions.IllegalOptionValueException;
import words.utils.CmdOptions.Option;
import words.utils.CmdOptions.UnknownOptionException;
import words.utils.TextReader;
import words.utils.WordType;

/**
 *
 * @author xavi
 */
public class GetWords {

    private enum Operation{ EQ, GE, LE , NE, GT, LT };

    public static void main(String [] args) {
        new GetWords(args);
    }

    public GetWords(String [] args) {

        CmdOptions parser = new CmdOptions();
        CmdOptionTester optionTester = new CmdOptionTester();

        // corpus file
        Option corpus = parser.addStringOption('c', "corpus");

        // dictionary file
        Option dictionary = parser.addStringOption('d', "dictionary");

        // output file
        Option output = parser.addStringOption('o', "output");

        // number of ocurrences
        Option number = parser.addIntegerOption('n',"number");

        // operation
        Option oper = parser.addStringOption('p',"operation");


        try {
            parser.parse(args);

            String fileDict = optionTester.testDict(parser, dictionary, false, true, false);
            String text = optionTester.testCorpus(parser, corpus);
            String fileOut = optionTester.testFile(parser, output);
            String stOp = (String)parser.getOptionValue(oper, "eq");

            Operation op = Operation.EQ;

            if(stOp.equalsIgnoreCase("gt")) op = Operation.GT;
            if(stOp.equalsIgnoreCase("lt")) op = Operation.LT;
            if(stOp.equalsIgnoreCase("ge")) op = Operation.GE;
            if(stOp.equalsIgnoreCase("le")) op = Operation.LE;
            if(stOp.equalsIgnoreCase("ne")) op = Operation.NE;
            
            int n = optionTester.testInteger(parser, number, 0);

            Dictionary d = new Dictionary();

            d.load(fileDict);

            TextReader txreader = new TextReader(text, WordType.LETTERS);
            String word;

            try {
                PrintWriter pw;
                if(fileOut != null) {
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(fileOut)));
                } else {
                    pw = new PrintWriter(System.out);
                }
                
                while ((word = txreader.nextWord()) != null) {

                    word = word.toLowerCase();
                    int num = d.getWord(word);

                    switch(op) {
                        case GT:
                            if(num > n) pw.println(""+num+"\t"+word);
                            break;
                        case GE:
                            if(num >= n) pw.println(""+num+"\t"+word);
                            break;
                        case EQ:
                            if(num == n) pw.println(""+num+"\t"+word);
                            break;
                        case NE:
                            if(num != n) pw.println(""+num+"\t"+word);
                            break;
                        case LE:
                            if(num <= n) pw.println(""+num+"\t"+word);
                            break;
                        case LT:
                            if(num < n) pw.println(""+num+"\t"+word);
                            break;
                    }
                }
                pw.close();
                
            } catch (java.io.IOException x) {
                System.err.println(x);
            }
        } catch (IllegalOptionValueException ex) {
            System.err.println("illegall option value exception: " + ex.getMessage());
        } catch (UnknownOptionException ex) {
            System.err.println("unknown option value exception: " + ex.getMessage());
        }
    }
}
