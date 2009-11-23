package words.tests;

import java.io.IOException;
import words.hmm.Value;
import words.utils.TextReader;
import words.utils.WordType;

/**
 *
 * @author xavi
 */
public class Probs {

    public Probs(String a,String b) {
        try {
            String wda, wdb;
            Value<String> pa = new Value<String>();
            Value<String> pb = new Value<String>();
            TextReader txa = new TextReader(a, WordType.LETTERS);
            TextReader txb = new TextReader(b, WordType.LETTERS);
            int i = 0;
            while ((wda = txa.nextWord()) != null) {
                wdb = txb.nextWord();
                pa.addValue(wda,1);
                pb.addValue(wdb,1);
                ++i;

                //if((i%10)==0) {
                    double v = this.prob("a",i,pa) * (Math.log(prob("a",i,pa)/prob("a",i,pb))/Math.log(2));
                    System.out.println(""+i+"\t"+v);
                //}
            }
        } catch (IOException ex) {
            
        }
    }

    public double prob(String w,int t,Value<String> v) {
        return v.getValue(w)/t;
    }

    static public void main(String [] args) {
        new Probs(args[0],args[1]);
    }
}
