package words.aligner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.commons.collections.bag.HashBag;

public class FastAligner {

    private BufferedReader reader;
    ArrayList<Paragraph> d1;
    ArrayList<Paragraph> d2;

    public FastAligner(String file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new SentenceAligner for a particular text file
     * @param file the file to scan
     */
    public FastAligner(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new SentenceAligner for a particular reader
     * @param rd
     */
    public FastAligner(BufferedReader rd) {
        reader = rd;
    }

    public FastAligner() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void parse() {
        d1 = new ArrayList<Paragraph>();
        d2 = new ArrayList<Paragraph>();
        ArrayList<Paragraph> d = d1;
        Paragraph p = new Paragraph();

        try {
            String line = reader.readLine();
            while (line != null) {

                if (line.isEmpty()) {
                    line = reader.readLine();
                    continue;
                }

                if (line.startsWith("e@0@p")) {
                    if (p.mida > 0) {
                        d.add(p);
                        p = new Paragraph();
                    }
                } else if (line.startsWith("e@0@l")) {
                    ++p.frases;
                } else if (line.startsWith("e@0@d")) {
                    d = d2;
                } else {
                    if (line.endsWith(" - Word")) {
                        line = line.replaceAll(" - Word", "");
                    }
                    p.paraules.add(line);
                    p.mida += line.length();
                }
                line = reader.readLine();
            }
        } catch (IOException ex) {

        }
    }

    public static void main (String [] args) {
        FastAligner aligner = new FastAligner();

        aligner.parse();

        aligner.align();
    }

    public void align() {
        
    }

    private class Paragraph {

        public HashBag paraules;
        public int frases;
        public int mida;

        public Paragraph() {
            paraules = new HashBag();
        }
    }
}
