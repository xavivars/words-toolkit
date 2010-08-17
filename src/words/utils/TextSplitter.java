/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package words.utils;

/**
 *
 * @author xavi
 */
public class TextSplitter extends TextReader{

    /**
     * Constructs a new TextReader
     * @param type defines a word pattern
     */

    public TextSplitter(String str, WordType type) {
        super(str,type);
    }

    public String nextWord(String str) {
        pattern.matcher(str);
        return nextWord();
    }

    /**
     * Returns the next word in file.
     * @return the next word in the scanned file
     */
    @Override
    public String nextWord() {
        String res = null;
        if (matcher != null) {
            while (res == null) {
                if (matcher.find()) {
                    res = matcher.group(1);
                } else {
                    break;
                }
            }
        }
        return res == null ? null : res.toLowerCase();
    }
}
