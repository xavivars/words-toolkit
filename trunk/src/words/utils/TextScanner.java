/**
 * Copyright (C) 2010 Rafael C. Carrasco
 * This code can be distributed or modified
 * under the terms of the GNU General Public License V3.
 */
package words.utils;

import java.io.*;
import java.util.regex.*;

/**
 * A simple fast text scanner that reads words from a file.
 */
public class TextScanner {

    BufferedReader reader;  // input text
    String[] buffer;        // tokens in one line
    int pos;                // next token
    Matcher matcher;        // auxiliary matcher

    public static enum Type {

        Space, Word, Number, Mixed, Abbrv, Other
    }
    final static Pattern space, word, number, mixed, abbrv;  // classes of tokens

    /**
     * Compile patterns
     * D (digit) =  digits, currency symbols and %,ª,º,°,¼,½,¾.‰
     * A (alphanumeric) = D class plus letters.
     * C (connector) = punctuation (non-alphanumeric characters)
     * that can be part of a word or number.
     * S (separator) = punctuation never found in words or numbers.
     * Unicodes used:
     * B7 (middle dot), 2122 (trade mark), A7 (paragraph),
     * AE (registered), A9 (copyright), AA (femenine ordinal),
     * BA (masculine ordinal), BO (degree sign), BC-BE (vulgar fractions)
     */
    static {
        String D = "[\\p{Nd}\\p{Nl}\\p{No}\\p{Sc}%‰ªº°§]";
        String A = "[\\p{L}©®™\\p{Nd}\\p{Nl}\\p{Sc}%‰ªº°¼½¾§]";
        String C = "[-&'+/@\\_~·,.]";           // backslash is scaped
        String S = "[^-&'+/@\\_~·,.\\p{L}\\p{Nd}\\p{Nl}\\p{Sc}%‰ªº°¼½¾]";
        /**
         * Whitespace and separators
         */
        space =
                Pattern.compile("(" + S + "(" + S + "|" + C + ")*)|(" + C + "(" + S + "|" + C + ")+)");

        /**
         * A word is a sequence of letters with single (intern) connectors.
         */
        word =
                Pattern.compile("[©]?\\p{L}+([-&'+/@\\_|~·]\\p{L}+)*[®™]?");

        /**
         * A number is a sequence of digits ended by a right-separator plus, optionally,
         * currency symbols (before), commas and dots (intern), and percentage symbol (after).
         */
        number =
                Pattern.compile("[§\\p{Sc}]?[\\p{Nd}\\p{Nl}\\p{No}]+([-',./][\\p{Nd}\\p{Nl}\\p{No}]+)*[%‰ªº°¼½¾\\p{Sc}]?");

        /**
         * A mixed token contains digits and letters as in "1st", "A4 paper" or "22-XII-1492"
         */
        mixed =
                Pattern.compile("[§]?[\\p{L}\\p{Nd}\\p{Nl}\\p{No}]+([-&'+/@\\_|~·][\\p{L}\\p{Nd}\\p{Nl}\\p{No}]+)*");

        /**
         * Abreviations as D.ª, 12.ª, or F.A.O
         */
        abbrv =
                Pattern.compile("(\\p{Upper}(\\.\\p{Upper})+)|([\\p{L}\\p{Nd}]{1,2}\\.[ªº])");
    }

    /**
     * Constructs a new TextScanner for a particular text file
     * @param file the file to scan
     */
    public TextScanner(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new TextScanner for a particular reader
     * @param rd
     */
    public TextScanner(BufferedReader rd) {
        reader = rd;
    }

    public TextScanner() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public Pair<String, Type> nextTypedWord() throws IOException {
        Pair<String, Type> res = null;
        while (res == null) {
            if (buffer == null || pos == buffer.length) {
                String line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        pos = 0;
                        buffer = space.split(" " + line + " ", 0);
                        // System.out.println(java.util.Arrays.toString(buffer));
                    }
                } else {
                    return null;
                }
            } else {
                String token = buffer[pos];
                matcher = word.matcher(token);
                if (matcher.matches()) {
                    res = new Pair<String, Type>(matcher.group(0), Type.Word);
                } else if ((matcher = number.matcher(token)).matches()) {
                    res = new Pair<String, Type>(matcher.group(0), Type.Number);
                } else if ((matcher = mixed.matcher(token)).matches()) {
                    res = new Pair<String, Type>(matcher.group(0), Type.Mixed);
                } else if ((matcher = abbrv.matcher(token)).matches()) {
                    res = new Pair<String, Type>(matcher.group(0), Type.Abbrv);
                } else if (token.length() > 0) {
                    res = new Pair<String, Type>(token, Type.Other);
                }
                ++pos;
            }
        }
        return res;
    }

    /**
     * Returns the next word in file.
     * @return the next word in the scanned file
     */
    public String nextWord() throws IOException {
        String res = null;
        while (res == null) {
            if (buffer == null || pos == buffer.length) {
                String line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        pos = 0;
                        buffer = space.split(" " + line + " ", 0);
                        // System.out.println(java.util.Arrays.toString(buffer));
                    }
                } else {
                    return null;
                }
            } else {
                String token = buffer[pos];
                matcher = word.matcher(token);
                if (matcher.matches()) {
                    res = matcher.group(0);
                } else if ((matcher = number.matcher(token)).matches()) {
                    res = "<" + matcher.group(0) + ">";
                } else if ((matcher = mixed.matcher(token)).matches()) {
                    res = "[" + matcher.group(0) + "]";
                    // System.err.println("MIXED(" + token + ")");
                } else if ((matcher = abbrv.matcher(token)).matches()) {
                    res = "<<" + matcher.group(0) + ">>";
                    // System.err.println("ABBRV(" + token + ")");
                } else if (token.length() > 0) {
                    res = "<<<" + token + ">>>";
                }
                ++pos;
            }
        }
        return res;
    }

    /**
     * Sample main.
     */
    public static void main(String[] args) throws IOException {
        TextScanner scanner;
        Pair<String, Type> wd;

        if (args.length > 0) {
            for (String arg : args) {
                System.err.print("\r" + arg);
                scanner = new TextScanner(new File(arg));
                while ((wd = scanner.nextTypedWord()) != null) {
                    if (wd.getSecond() == Type.Word) {
                        System.out.println(wd.getFirst());
                    }
                }
            }
        } else {
            System.err.println("Usage: TextScanner file1 file2 ...");
        }
    }
}
