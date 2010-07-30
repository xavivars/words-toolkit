package words.aligner;

import java.io.IOException;
import java.lang.Number;
import words.utils.Pair;
import words.utils.Pair;
import words.utils.TextScanner;
import words.utils.TextScanner;
import words.utils.TextScanner.Type;

/**
 *
 * @author xavi
 */
public class WordLiner {

    public static void main(String[] args) {
        TextScanner scanner = new TextScanner();
        Pair<String, Type> wd;
        int line = 0;
        int par = 0;
        try {

            while ((wd = scanner.nextTypedWord()) != null) {
                switch (wd.getSecond()) {
                    case Word:
                    case Number:
                    case Abbrv:
                        System.out.println(wd.getFirst() + " - " + wd.getSecond());
                        break;
                    case Mixed:
                        if (wd.getFirst().equalsIgnoreCase("e@0@l")) {
                            System.out.println(wd.getFirst() + "@" + (++line));
                        } else {
                            if (wd.getFirst().equalsIgnoreCase("e@0@p")) {
                                System.out.println(wd.getFirst() + "@" + (++par));
                            } else {
                                System.out.println(wd.getFirst() + " - " + wd.getSecond());
                            }
                        }
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("malament del tot");
            e.printStackTrace();
        }
    }
}
