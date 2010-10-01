package words.aligner;

import java.io.File;
import java.io.IOException;
import words.utils.TextScanner;

/**
 *
 * @author xavi
 */
public class WordUnLiner {

    public static void main(String[] args) {
        try {
            TextScanner scanner;
            if (args.length > 0) {
                scanner = new TextScanner(new File(args[0]));
            } else {
                scanner = new TextScanner();
            }

            String wd;
            int par = 0;

            System.out.print((++par)+" ");
            while ((wd = scanner.nextTxtWord()) != null) {
                if (wd.startsWith("e@0@l")) {
                    System.out.println();
                } else {
                    if (wd.startsWith("e@0@p")) {
                        System.out.println("\n");
                        System.out.print((++par)+" ");
                    } else {
                        System.out.print(wd+" ");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("malament del tot");
            e.printStackTrace();
        }
    }
}
