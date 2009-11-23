/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package words;

/**
 *
 * @author xavi
 */
public class Temp {

    public Temp() {
        
    }

    public static void mainx(String [] args) {
        Dictionary d = new Dictionary();

        d.train("/home/xavi/Documents/Informàtica/doctorat/tesina/tests_xarrada/eng.test");

        System.out.println("Total: "+d.getTotalWords()+" ["+d.getSize()+"]");

    }

    public static void mainaa(String [] args) {
        DMM dmc = new DMM(4);
        HMM hmm = new HMM();
        hmm.load("/home/xavi/Documents/tesina/svn/trunk/models/en/HMM.100000.xml");
        dmc.load("/home/xavi/Documents/tesina/svn/trunk/models/en/DMC4.100000.xml");

        String [] words = {"adrenaline","contributions","something","estimation","moonlighting","convention","yesterday","outsiders"};

        for(String wd : words) {
            System.out.println(wd + " & " + dmc.split(wd) + " & " + hmm.split(wd));
        }
    }

    public static void main(String [] args) {
        PST v = new PST();
        PST v2 = new PST(0.1);

        v.train("/home/xavi/Documents/corpus/dev/latimes94.txt.train");
        v2.train("/home/xavi/Documents/corpus/dev/latimes94.txt.train");

        v.save("/home/xavi/Documents/vlmc.xml");
        v2.save("/home/xavi/Documents/vlmc_01.xml");

        System.out.println("v. "+v.getMaxDepth());
        System.out.println("v2 "+v2.getMaxDepth());
    }

    public static void main2 (String [] args) {

        Ngram n3 = new Ngram(3);
        Ngram n5 = new Ngram(5);
        Ngram n7 = new Ngram(7);
        HMM hmm = new HMM();
        DMM dmc = new DMM(4);
        PST v1 = new PST();
        PST v01 = new PST(0.1);



        n3.load("/home/xavi/Documents/tesina/svn/trunk/models/en/Ngram3.100000000.xml");
        n5.load("/home/xavi/Documents/tesina/svn/trunk/models/en/Ngram5.100000000.xml");
        n7.load("/home/xavi/Documents/tesina/svn/trunk/models/en/Ngram7.100000000.xml");
        hmm.load("/home/xavi/Documents/tesina/svn/trunk/models/en/HMM.100000000.xml");
        dmc.load("/home/xavi/Documents/tesina/svn/trunk/models/en/DMC4.100000000.xml");
        v1.load("/home/xavi/Documents/tesina/svn/trunk/models/en/VLMC.100000000.xml");
        v01.load("/home/xavi/Documents/tesina/svn/trunk/models/en/VLMC_01.100000000.xml");

        System.out.println("N3 size: "+n3.getSize());
        System.out.println("N5 size: "+n5.getSize());
        System.out.println("N7 size: "+n7.getSize());
        System.out.println("DMC size: "+dmc.getSize());
        System.out.println("HMM size: "+hmm.getSize());
        System.out.println("V1 size: "+v1.getSize());
        System.out.println("V01 size: "+v01.getSize());

    }

}
