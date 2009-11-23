/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package words.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author xavi
 */
public class Corpus {

    public static void main(String [] args) {

        if(args.length<2)
            return;

        try {
        int size = Integer.parseInt(args[0]);
        String file = args[1];

        TextReader txt = new TextReader(file, WordType.LETTERS);
        PrintWriter pf = new PrintWriter(new BufferedWriter(new FileWriter(file + "." + size)));

        String wd = txt.nextWord();
        for(int i=0;i<size && wd!=null;++i,wd = txt.nextWord()) {
            pf.write(wd+" ");
        }

        pf.close();

        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

    }

}
