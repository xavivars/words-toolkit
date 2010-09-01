/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package words.aligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;
import words.utils.Int;

/**
 *
 * @author xavi
 */
public class ParagraphStatistics {

    public ParagraphStatistics() {
        this(false);
    }

    public ParagraphStatistics(boolean print) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String line = reader.readLine();

            ArrayList<Paragraph> pars;
            int npars = 0;
            int nlines = 0;
            int nwords = 0;
            int nchars = 0;

            if (print) {
                while (line != null) {
                    if (line.startsWith("e@0@p@")) {
                        System.out.println("Par[" + (++npars) + "]: " + nlines + "-" + nwords + "-" + nchars);
                        nlines = nwords = nchars = 0;
                    } else if (line.startsWith("e@0@l@")) {
                        ++nlines;
                    } else {
                        ++nwords;
                        if (line.endsWith(" - Word")) {
                            line.replaceAll(" - Word", "");
                            nchars += line.length();
                        }
                    }
                    line = reader.readLine();
                }
            } else {
                pars = new ArrayList<Paragraph>();
                Paragraph p = new Paragraph();
                int id = 1;
                while (line != null) {
                    if (line.startsWith("e@0@p@")) {
                        p.id = id;
                        pars.add(p);
                        p = new Paragraph();
                        p.lines = nlines;
                        p.words = nwords;
                        p.chars = nchars;
                        nlines = nwords = nchars = 0;
                    } else if (line.startsWith("e@0@l@")) {
                        ++nlines;
                    } else {
                        ++nwords;
                        if (line.endsWith(" - Word")) {
                            line.replaceAll(" - Word", "");
                            nchars += line.length();
                        }
                    }
                    line = reader.readLine();
                }
                p.id = id;
                pars.add(p);
            }

        } catch (IOException ioe) {
            System.err.println("ERROR: Problemes amb 'stdin'");
        }

    }

    public static void main(String[] args) {
        boolean print = (args.length > 0);

        new ParagraphStatistics(print);
    }

    private class Paragraph {
        public int id;
        public int lines;
        public int words;
        public int chars;

        public Paragraph() {
            id=lines=words=chars=0;
        }
    }
}