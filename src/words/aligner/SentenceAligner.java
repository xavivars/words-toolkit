package words.aligner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 *
 * @author xavi
 */
public class SentenceAligner {

    BufferedReader reader;

   /**
     * Constructs a new SentenceAligner for a particular text file
     * @param file the file to scan
     */
    public SentenceAligner(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new SentenceAligner for a particular reader
     * @param rd
     */
    public SentenceAligner(BufferedReader rd) {
        reader = rd;
    }

    public SentenceAligner() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public String [] readLines() {
        ArrayList<String> llista = new ArrayList<String>();

        return (String []) llista.toArray();
    }

    public static void main(String[] args) throws IOException {

    }
}
