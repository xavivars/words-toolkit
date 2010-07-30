/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package words.aligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author xavi
 */
public class ParagraphStatistics {

    public ParagraphStatistics() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String line = reader.readLine();

            int npars=0;
            int nlines=0;
            int nwords=0;
            int nchars=0;
            
            while(line!=null) {

                // analitzem la línia

                // EOP

                if(line.startsWith("e@0@p@")) {

                    System.out.println("Par["+(++npars)+"]: "+nlines+"-"+nwords+"-"+nchars);

                    // reset counters
                    nlines = nwords = nchars = 0;
                    
                } else if(line.startsWith("e@0@l@")) {
                    ++nlines;
                } else {
                    ++nwords;
                    if(line.endsWith(" - Word")) {
                        line.replaceAll(" - Word","");
                        nchars += line.length();
                    }
                }

                line = reader.readLine();
            }

        } catch(IOException ioe) {
            System.err.println("ERROR: Problemes amb 'stdin'");
        }

    }

    public static void main(String[] args) {
        new ParagraphStatistics();
    }
}
