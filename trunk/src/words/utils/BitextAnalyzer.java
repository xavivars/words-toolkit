/*
 * Copyright (C) 2008
 *
 * Author:
 *  Xavier Ivars i Ribes <xavi.ivars@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package words.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import words.Dictionary;

public class BitextAnalyzer {

    private Dictionary dict;
    private BufferedReader br;
    private HashMap<String, Pair<String, Int>> corrections;
    PrintWriter corpusVerd;
    PrintWriter corpusRoig;
    PrintWriter extraVerd;
    PrintWriter extraRoig;

    public BitextAnalyzer() {
        dict = new Dictionary();
        corrections = new HashMap<String, Pair<String, Int>>();
        try {
            corpusVerd = new PrintWriter("corpusVerd.txt");
            corpusRoig = new PrintWriter("corpusRoig.txt");
            extraVerd = new PrintWriter("extraVerd.txt");
            extraRoig = new PrintWriter("extraRoig.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BitextAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {

        corpusRoig.close();
        corpusVerd.close();
        extraRoig.close();
        extraVerd.close();
    }

    public void analyze(String f) {
        try {
            Scanner console = new Scanner(System.in);


            br = new BufferedReader(new FileReader(f));

            int i = br.read();

            StringBuilder sb = new StringBuilder(20);
            StringBuilder verd = new StringBuilder(20);
            StringBuilder roig = new StringBuilder(20);
            int deleted = 0;
            int added = 0;
            int changed = 0;
            int iguals = 0;
            boolean negre = false;


            while (i >= 0) {
                char c = (char) i;
                if ((((c == ' ' || c == '.') || c == ',') || c == ';') && (sb.length() > 0)) {
                    if (sb.toString().equalsIgnoreCase("<font")) {
                        negre = false;
                        inFont(br, verd, roig);
                        sb.delete(0, sb.length());

                    } else {
                        if (!negre) {
                            negre = true;

                            // desem les paraules roges i verdes
                            if (!(roig.toString().isEmpty() || verd.toString().isEmpty())) {
                                if(!(roig.toString().equalsIgnoreCase(verd.toString()))) {
                                    corpusRoig.println(roig.toString());
                                    corpusVerd.println(verd.toString());    
                                }
                                
                            } else {
                                if (roig.toString().isEmpty()) {
                                    extraVerd.println(verd.toString());
                                } else {
                                    extraRoig.println(roig.toString());
                                }
                            }


                            verd.delete(0, verd.length());
                            roig.delete(0, roig.length());
                        }

                        // hem d'afegir la paraula al diccionari
                        // estem en el mode NEGRE
                        this.dict.addWord(sb.toString());


                        sb.delete(0, sb.length());
                    }

                } else {
                    if (!(((c == ' ' || c == '.') || c == ',') || c == ';')) {
                        sb.append((char) i);
                    }
                }

                i = br.read();
            }
            
            this.close();

            System.out.println("Inicials: " + (iguals + deleted));
            System.out.println("Finals: " + (iguals + added));
            System.out.println("=========================");
            System.out.println("Canviats: " + changed);
            System.out.println("Afegits: " + added);
            System.out.println("Esborrats: " + deleted);

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(BitextAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getWord(BufferedReader br) {

        StringBuilder sb = new StringBuilder(20);
        try {
            int i = br.read();
            char c = (char) i;
            if (i < 0) {
                return null;
            }
            while (!((((c == ' ' || c == '.') || c == ',') || c == ';') || c == '\n') && (i >= 0)) {
                sb.append(c);
                i = br.read();
                c = (char) i;
            }


        } catch (IOException ex) {
            Logger.getLogger(BitextAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    private void inFont(BufferedReader br, StringBuilder verd, StringBuilder roig) {
        try {
            int i = br.read();
            StringBuilder sb = new StringBuilder(20);
            String aux;
            boolean cont = true;

            boolean withWords = false;

            while ((i >= 0) && cont) {
                if ((char) i == '>') {
                    if (sb.toString().endsWith("color=green")) {
                        // estem en el mode verd
                        //
                        if (withWords) {
                            cont = fillWords(br, verd);
                        } else {
                            cont = fillFull(br, verd);
                        }
                    } else {
                        if (sb.toString().endsWith("color=red")) {
                            // estem en el mode roig
                            if (withWords) {
                                cont = fillWords(br, roig);
                            } else {
                                cont = fillFull(br, roig);
                            }
                        }
                    }

                } else {
                    sb.append((char) i);
                    i = br.read();
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(BitextAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean fillFull(BufferedReader br, StringBuilder str) {
        try {

            int i = br.read();
            char c = (char) i;
            StringBuilder sb = new StringBuilder();
            while ((c > 0) && !(sb.toString().endsWith("</font>"))) {
                sb.append(c);
                i = br.read();
                c = (char) i;
            }
            if(sb.toString().endsWith("</font>")) {
                sb.delete(sb.length()-7, sb.length());
            }
            
            
            str.append(
                    sb.toString().trim().replace('\n','ŀ').replace(' ','ß').replace('.','π').replace(',','κ')
                    );


        } catch (IOException ex) {
            Logger.getLogger(BitextAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    public boolean fillWords(BufferedReader br, StringBuilder sb) {
        String aux = getWord(br);
        while ((aux != null) && (!aux.endsWith("</font>"))) {
            sb.append(" ");
            sb.append(aux);
            aux = getWord(br);
        }

        if (aux != null) {
            aux = aux.substring(0, aux.length() - 7);
            sb.append(" ");
            sb.append(aux);
        }
        sb.delete(0, sb.length());

        return false;
    }

    public void analyzeFiles(String s) {
        ArrayList<String> toAnalize = rec(s);

        for (String aux : toAnalize) {
            System.out.println("Analitzant... " + aux);
            analyze(aux);
        }

    }

    public ArrayList<String> rec(String s) {
        ArrayList<String> al = new ArrayList<String>();

        File f = new File(s);

        if (f.isDirectory()) {
            String[] ls = f.list();

            for (String aux : ls) {
                al.addAll(rec(s + "/" + aux));
            }

        } else {
            if (f.isFile() && s.endsWith("words_mixed.html")) {
                al.add(s);
            }
        }
        return al;
    }

    public static void main(String[] args) {
        BitextAnalyzer ba = new BitextAnalyzer();
        ba.analyzeFiles(args[0]);
        //ba.analyzeFiles("/home/xavi/Documents/tesina/error/ocr/13042/");
        ba.close();
    }
}
