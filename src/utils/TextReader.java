package words.utils;

import java.util.regex.*;
import java.io.*;

/**
 * A simple fast text scanner that reads words from a file.
 */
public class TextReader {

    private BufferedReader reader;// The input reader
    private Matcher matcher;      // Pattern matcher  
    private Pattern pattern;      // A regular expression patttern 

    /**
     * Open a file as reader
     */
    private BufferedReader open(String fileName) {
        String encoding = System.getProperty("file.encoding");

        try {
            if (fileName != null) {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, encoding);
                return new BufferedReader(isr);
            } else {
                return new BufferedReader(new java.io.InputStreamReader(System.in));
            }
        } catch (FileNotFoundException x) {
            System.err.println("Cannot open " + fileName);
            System.exit(1);
        } catch (UnsupportedEncodingException x) {
            System.err.println("Unsuported encoding " + encoding);
            System.exit(1);
        } catch (IOException x) {
            System.err.println("Unable to close an open reader");
        }
        return null;
    }

    /**
     * Constructs a new TextReader 
     * @param type defines a word pattern
     */
    public TextReader(String fileName, WordType type) {
        switch (type) {
            case LETTERS:
                pattern = Pattern.compile("([\\p{L}]+)|([^\\p{L}]*)");
                break;
            case LETTERSorHYPH:
                pattern = Pattern.compile("([\\p{L}-]+)|([^\\p{L}-]*)");
                break;
            case LETTERSorDIGITS:
                pattern = Pattern.compile("([\\p{L}\\p{Digit}]+)|([^\\p{L}\\p{Digit}]*)");
                break;
            case LETTERSorDIGITSorPUNCT:
                pattern = Pattern.compile("([\\p{L}\\p{Digit}\\p{Punct}]+)|" +
                        "([^\\p{L}\\p{Digit}\\p{Punct}]*)");
                break;
            default:
                pattern = Pattern.compile("([\\p{L}-]+)|([^\\p{L}]*)");
        }

        reader = open(fileName);
        try {
            if (reader.ready()) {
                matcher = pattern.matcher(reader.readLine());
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }

    /**
     * Returns the next word in file.
     * @return the next word in the scanned file
     */
    public String nextWord() throws IOException {
        String res = null;
        if (matcher != null) {
            while (res == null) {
                if (matcher.find()) {
                    res = matcher.group(1);
                } else if (reader.ready()) {
                    matcher = pattern.matcher(reader.readLine());
                } else {
                    break;
                }
            }
        }
        return res == null ? null : res.toLowerCase();
    }

    /**
     * Sample main.
     */
    public static void main(String[] args) {
        TextReader reader = new TextReader(args[0], WordType.LETTERS);
        String word;
        try {
            while ((word = reader.nextWord()) != null) {
                System.out.println(word);
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
