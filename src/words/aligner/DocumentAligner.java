/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package words.aligner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import words.utils.Pair;
import words.utils.StopWords;

/**
 *
 * @author xavi
 */
public class DocumentAligner {

    private BufferedReader reader;
    private ArrayList<Document> documents;
    private ArrayList<Alignment> paragraph_alignments;
    private Distances paragraph_distances;
    // VALUES FROM Gale&Church
    private static int SUBSTITUTION = 1;
    private static int DELETION = 450;
    private static int INSERTION = 450;
    private static int MELDING = 440;
    private static int CONTRACTION = 230;
    private static int EXPANSION = 230;

    private static int BIG_DISTANCE = 5000;

    public DocumentAligner(String file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new SentenceAligner for a particular text file
     * @param file the file to scan
     */
    public DocumentAligner(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Constructs a new SentenceAligner for a particular reader
     * @param rd
     */
    public DocumentAligner(BufferedReader rd) {
        reader = rd;
    }

    public DocumentAligner() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void documentFingerPrint() {
        if (documents == null) {
            documents = new ArrayList<Document>();
        }
        Document document = null;
        try {
            String line = reader.readLine();
            while (line != null) {
                if (document == null) {
                    document = new Document(1);
                }

                if(line.isEmpty()) {
                    line = reader.readLine();
                    continue;
                }

                if (line.startsWith("e@0@p")) {
                    document.addParagraph();
                } else if (line.startsWith("e@0@l")) {
                    document.addLine();
                } else if (line.startsWith("e@0@d")) {
                    document.close();
                    documents.add(document);
                    document = new Document(documents.size() + 1);
                } else {
                    if (line.endsWith(" - Word")) {
                        line = line.replaceAll(" - Word", "");
                    }
                    document.addWord(line);
                }
                line = reader.readLine();
            }

            if (document != null) {
                document.close();
                documents.add(document);
            }
        } catch (IOException ex) {
        }
    }

    public void print() {
        for (Document d : documents) {
            System.out.println(d.toString());
        }
    }

    public static void main(String[] args) {
        DocumentAligner da = null;
        try {
            if (args.length > 0) {
                da = new DocumentAligner(args[0]);
            } else {
                da = new DocumentAligner();
            }
        } catch (IOException ioe) {
            System.err.println("Error obrint el fitxer");
        }

        da.documentFingerPrint();

        System.out.println("Paragraphs");
        da.showParagraphs();

        System.out.println("-----------\n\n");

        System.out.println("Gale&Church");
        da.paragraphDistance(false);
        da.showAlignments();

        System.out.println("Custom distance");
        da.paragraphDistance(true);
        da.showAlignments();
    }

    public void showParagraphs() {
        for(Document d : documents) {
            int i = 0;
            System.out.println("Document "+ d.getId());
            for(FingerPrint p : d.paragraphs) {
                System.out.println(""+(++i)+": "+p);
            }
        }
    }

    public void showAlignments() {
        int cl = -1; int cr = -1;
        Alignment al = null;
        for(int num = paragraph_alignments.size()-1;num>=0;--num) {
            al = paragraph_alignments.get(num);
            if(al.check(cl, cr) || cl < 0) {
                cl = al.getCurrentLeft();
                cr = al.getCurrentRight();
                System.out.println(""+cl+"-"+cr+" ["+al.getDistance()+"]");
                cl = al.getPreviousLeft();
                cr = al.getPreviousRight();
            }
        }
        System.out.println(""+cl+"-"+cr);
    }

    public void paragraphDistance(boolean custom) {

        HashSet<String> done = new HashSet<String>();
        for (Document d1 : documents) {
            done.add(d1.toString());
            // comparem cada document amb la resta dels documents
            for (Document d2 : documents) {
                if (d2 == d1) {
                    continue;
                }
                if (done.contains(d2.toString())) {
                    continue;
                }

                computeParagraphDistance(d1, d2,custom);
            }
        }

    }

    private class Alignment {
        int current_left;
        int current_right;
        int previous_left;
        int previous_right;
        int distance;

        public Alignment(int cl, int cr,int pl,int pr,int d) {
            current_left = cl;
            current_right = cr;
            previous_left = pl;
            previous_right = pr;
            distance = d;
        }

        public boolean check(int l,int r) {
            return ((l == current_left) && (r == current_right));
        }
        
        public int getCurrentLeft() { return current_left; }
        public int getCurrentRight() { return current_right; }
        public int getPreviousLeft() { return previous_left; }
        public int getPreviousRight() { return previous_right; }
        public int getDistance() { return distance; }

    }

    private class Distances {

        HashMap<Pair<Integer, Integer>, Integer> dists;

        public Distances() {
            dists = new HashMap<Pair<Integer, Integer>, Integer>();
        }

        public boolean has(int i1, int i2) {
            return dists.containsKey(new Pair<Integer, Integer>(i1, i2));
        }

        public void set(int i1, int i2, int d) {
            dists.put(new Pair<Integer, Integer>(i1, i2), d);
        }

        public int get(int i1, int i2) {
            Integer ret = dists.get(new Pair<Integer, Integer>(i1, i2));
            return (ret != null) ? ret : Integer.MAX_VALUE;
        }
    }

    private void computeParagraphDistance(Document d1, Document d2,boolean custom) {
        int i = 0, j = 0;
        paragraph_distances = new Distances();
        paragraph_alignments = new ArrayList<Alignment>();
        for(i=-1;i<d1.paragraphs.size();++i) {
            for(j=-1;j<d2.paragraphs.size();++j)
                if(i<=0 || j<=0)
                    paragraph_distances.set(i, j, 0);
        }
        int dmin = Integer.MAX_VALUE;
        FingerPrint p1=null, p2=null, p3=null, p4=null;
        for (i=0;i<=d1.paragraphs.size();++i) {
            for (j=0;j<=d2.paragraphs.size();++j) {

                if(i>0) p1 = d1.paragraphs.get(i-1); else p1 = null;
                if(j>0) p2 = d2.paragraphs.get(j-1); else p2 = null;
                if(i>1) p3 = d1.paragraphs.get(i-2).dup().add(p1); else p3 = null;
                if(j>1) p4 = d2.paragraphs.get(j-2).dup().add(p2); else p4 = null;
                // opcio 1-1
                int dist11 = (i>0 && j > 0) ?
                    paragraph_distances.get(i - 1, j - 1) + paragraph_distance(p1, p2, custom) + SUBSTITUTION :
                    Integer.MAX_VALUE;

                // opcio 1-0
                int dist10 = (i>0) ?
                    paragraph_distances.get(i - 1, j)+ paragraph_distance(p1, null, custom) + DELETION :
                    Integer.MAX_VALUE;

                // opcio 0-1
                int dist01 = (j>0) ?
                    paragraph_distances.get(i, j - 1) + paragraph_distance(null, p2, custom) + INSERTION :
                    Integer.MAX_VALUE;

                // opcio 2-1
                int dist21 = (i>1 && j>0) ?
                    paragraph_distances.get(i - 2, j - 1)+ paragraph_distance(p3, p2, custom) + CONTRACTION :
                    Integer.MAX_VALUE;

                // opcio 1-2
                int dist12 = (i>0 && j>1) ?
                    paragraph_distances.get(i - 1, j - 2) + paragraph_distance(p1, p4, custom) + EXPANSION :
                    Integer.MAX_VALUE;

                // opcio 2-2
                int dist22 = (i>1 && j > 1) ?
                    paragraph_distances.get(i - 2, j - 2) + paragraph_distance(p3, p4, custom) + MELDING:
                    Integer.MAX_VALUE;
                
                dmin = dist11;

                // find the minimum distance
                if (dist10 < dmin) {
                    dmin = dist10;
                }
                if (dist01 < dmin) {
                    dmin = dist01;
                }
                if (dist21 < dmin) {
                    dmin = dist21;
                }
                if (dist12 < dmin) {
                    dmin = dist12;
                }
                if (dist22 < dmin) {
                    dmin = dist22;
                }

                int left = -1;
                int right = -1;

                if(dmin == Integer.MAX_VALUE) {
                    paragraph_distances.set(i, j, 0);
                } else if (dmin == dist11) { // substitution
                    paragraph_distances.set(i, j, dmin);
                    left = i-1;
                    right = j-1;
                } else if (dmin == dist10) { // deletion
                    paragraph_distances.set(i, j, dmin);
                    left = i-1;
                    right = j;
                } else if (dmin == dist01) { // insertion
                    paragraph_distances.set(i, j, dmin);
                    left = i;
                    right = j-1;
                } else if (dmin == dist12) { // expansion
                    paragraph_distances.set(i, j, dmin);
                    left = i -1;
                    right = j -2;
                } else if (dmin == dist21) { // contraction
                    paragraph_distances.set(i, j, dmin);
                    left = i - 2;
                    right = j -1;
                } else if (dmin == dist22) { // melding
                    paragraph_distances.set(i, j, dmin);
                    left = i - 2;
                    right = j -2;
                }
                if(left >= 0)
                    paragraph_alignments.add(new Alignment(i,j,left,right,dmin));
            }
            dmin = Integer.MAX_VALUE;
            j = 0;
        }
    }

    private double mitjana(double d1, double d2) {
        return (d1 + d2) / 2;
    }

    /**
     * See Abramowitz, M. and Stegun, I. (1964), 26.2.17 p. 932
     * @param d
     * @return area under a normal distribution
     */
    private double pnorm(double d) {
        double ret = 0;

        double t = 1 / (1 + 0.2316419 * d);

        // 1/sqrt(2*pi) == 0.389423

        ret = 1 - 0.389423 * Math.exp(-d * d / 2)
                * ((((1.330274429 * t - 1.821255978) * t + 1.781477937) * t
                - 0.356563782) * t + 0.319381530);


        return ret;
    }

    private int normal_distance(int i1, int i2) {
        // de l'article de Gale&Church
        double ret = 0;

        double s2 = 6.8;

        if (i1 == 0 && i2 == 0) {
            return 0;
        }

        double mean = mitjana(i1, i2);
        double delta = Math.abs(i1 - i2) / (Math.sqrt(mean * s2));

        ret = 2 * (1 - pnorm(delta));

        if (ret > 0) {
            return ((int) (-100 * Math.log(ret)));
        } else {
            return BIG_DISTANCE;
        }
    }

    private int paragraph_distance(FingerPrint fp1, FingerPrint fp2, boolean custom) {
        int ret = 0;

        if(fp1 == null && fp2 == null) return 0;

        if(fp1 == null) {
            ret = normal_distance(0, fp2.chars);
        } else if(fp2 == null) {
            ret = normal_distance(fp1.chars,0);
        } else {
            if(custom) {
                double r = 0.85*normal_distance(fp1.chars, fp2.chars);
                r += 0.05 * normal_distance(fp1.stop*100,fp2.stop*100);
                r += 0.05 * normal_distance(fp1.words*20,fp2.words*20);
                r += 0.05 * normal_distance(fp1.articles*100,fp2.articles*100);
                ret = (int)Math.round(r);
            } else {
                ret = normal_distance(fp1.chars, fp2.chars);
            }
        }

        return ret;
    }

    private int distance(FingerPrint fp1, FingerPrint fp2) {

        int ret = 0;

        if (fp1.lines > 0 && fp2.lines > 0) {
            if (fp1.pars > 0 && fp2.pars > 0) {
                // document distance
            } else {
                // paragraph distance
                ret = normal_distance(fp1.chars, fp2.chars);
            }
        } else {
            ret = normal_distance(fp1.chars, fp2.chars);
            // line distance
        }

        return ret;
    }

    /**
     * per tal d'alinear dos documents
     * cal en primer lloc saber el paregut que tenen els dos paragrafs en si
     * mateix
     * x1      == y1
     * x1      == y1 + y2
     * x1 + x2 == y1
     * x1 + x2 == y1 + y2
     */
    private class Document {

        private FingerPrint fp;
        private ArrayList<FingerPrint> paragraphs;
        private ArrayList<FingerPrint> lines;
        private ArrayList<String> buffer;
        private TreeSet<Character> vocals;
        private int lastLine;
        private int lastParagraph;

        public Document(int id) {
            vocals = new TreeSet<Character>();
            vocals.add('a');
            vocals.add('á');
            vocals.add('e');
            vocals.add('é');
            vocals.add('i');
            vocals.add('í');
            vocals.add('o');
            vocals.add('ó');
            vocals.add('u');
            vocals.add('ú');
            vocals.add('ü');

            buffer = new ArrayList<String>();
            paragraphs = new ArrayList<FingerPrint>();
            lines = new ArrayList<FingerPrint>();
            fp = new FingerPrint(id);

            lastLine = lastParagraph = 0;
        }

        public FingerPrint getFingerPrint() {
            return fp;
        }

        public String getId() {
            return ""+fp.id;
        }

        @Override
        public String toString() {
            return fp.toString();
        }

        public void addWord(String wd) {
            buffer.add(wd);
        }

        public void addLine() {
            if (buffer.size() > 0) {
                FingerPrint fing = new FingerPrint(lines.size() + 1);

                fing.words = buffer.size();
                for (String wd : buffer) {
                    int vc = countVowels(wd);
                    int sz = wd.length();

                    fing.chars += sz;
                    fing.vowels += vc;
                    fing.cons += (sz - vc);
                    fing.stop += (StopWords.spanish(wd)) ? 1 : 0;
                    fing.articles += (StopWords.spanish_articles(wd)) ? 1 : 0;
                }
                lines.add(fing);
                buffer.clear();
            }
        }

        public void addParagraph() {
            if (lines.size() > lastLine || (buffer.size() > 0)) {
                addLine();
                FingerPrint fing = new FingerPrint(paragraphs.size() + 1);
                int i = lastLine;
                for (; i < lines.size(); ++i) {
                    fing.add(lines.get(i));
                    fing.lines++;
                }
                paragraphs.add(fing);
                lastLine = i;
                //System.err.println("Par added: "+paragraphs.size());
            }
        }

        public void close() {
            if (paragraphs.size() > 0 || (lines.size() > lastLine || (buffer.size() > 0))) {
                addParagraph();
                for (FingerPrint fing : paragraphs) {
                    fp.add(fing);
                    fp.lines += fing.lines;
                }
                fp.pars = paragraphs.size();
            }
        }

        public int countVowels(String text) {
            int count = 0;

            for (Character c : text.toCharArray()) {
                if (vocals.contains(c)) {
                    count++;
                }
            }

            return count;
        }
    }

    private class FingerPrint {

        public int words;
        public int chars;
        public int vowels;
        public int cons;
        public int lines;
        public int pars;
        public int stop;
        public int id;
        public int articles;

        public FingerPrint(int i) {
            pars = lines = words = chars = vowels = cons = stop = articles = 0;
            id = i;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + pars + "-" + lines + "-" + words + "-" 
                    + chars + "-" + vowels + "-" + cons + "-" + stop + "-" + articles;
        }

        public FingerPrint add(FingerPrint fp) {
            this.words += fp.words;
            this.chars += fp.chars;
            this.cons += fp.cons;
            this.vowels += fp.vowels;
            this.stop += fp.stop;
            this.articles += fp.articles;
            return this;
        }

        public FingerPrint dup() {
            FingerPrint tmp = new FingerPrint(this.id);

            tmp.words = this.words;
            tmp.chars = this.chars;
            tmp.cons = this.cons;
            tmp.vowels = this.vowels;
            tmp.stop = this.stop;
            tmp.articles = this.articles;

            return tmp;
        }
    }

    private class Group {

        Set<Integer> ids;
        int id;

        public Group(int i) {
            id = i;
            ids = new HashSet<Integer>();
        }

        public void add(int i) {
            ids.add(i);
        }

        public boolean contains(int i) {
            return ids.contains(i);
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();

            for (Integer i : ids) {
                if(ret.length()>0) ret.append(","+i); else ret.append(i);
            }

            return ret.toString();
        }
    }
}
