/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package words;

/**
 *
 * @author xavi
 */
public class Version {

    public static final String BUILD = "20091120";
    public static final String VERSION = "0.2";
    public static final boolean STABLE = false;

    public static void main(String [] args) {
        System.out.println("words.TOOLKIT");
        System.out.println("=============");
        System.out.println("");
        if(STABLE)
            System.out.println("Version number: "+VERSION);
        else
            System.out.println("Build ID: "+BUILD);
    }
}
