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

import java.text.DecimalFormat;
import java.util.HashMap;

public class LlistaNgrames implements Comparable<LlistaNgrames> {

        String llista;
        String traduccio;
        int mida;
        double prob;

        public LlistaNgrames(char c) {
            llista = "" + c;
            mida = 0;
            traduccio = null;
            prob = 0;
            //prob = 1;
        }

        public LlistaNgrames() {
            mida = 0;
            prob = 0;
            traduccio = null;
            llista = "";
            //prob = 1;
        }

        private LlistaNgrames dup() {
            LlistaNgrames n = new LlistaNgrames();
            n.llista = this.llista;
            n.mida = this.mida;
            n.prob = this.prob;
            n.traduccio = this.traduccio;
            return n;
        }

        public LlistaNgrames junt(char c) {
            LlistaNgrames n = this.dup();
            n.llista = "" + this.llista + c ;

            return n;
        }

        public LlistaNgrames separat(char c) {
            LlistaNgrames n = this.dup();

            n.llista = "" + this.llista + "-" + c;

            return n;
        }

        public void print() {
            if (this.traduccio != null) {
                System.out.println(this.llista + " ([" + mida + "]: " + traduccio + " ["+prdouble(prob)+"])");
            }
        }

        public void printProbs(HashMap<String,Pair<String,Double>> trads) {
            String [] ln = getLlista();

            this.print();

            for(String s : ln) {
                System.out.print("\t\t");
                System.out.print(s+": "+trads.get(s).getFirst());
                System.out.print(" ["+LlistaNgrames.length(trads.get(s).getFirst())+"]: ");
                System.out.println(prdouble(Math.log(trads.get(s).getSecond())));
            }
            System.out.println();
        }

        public String prdouble(double d) {
            DecimalFormat myFormatter = new DecimalFormat("00.000000");
            return myFormatter.format(Math.abs(d));
        }

        public static int length(String t) {
            int m = 1;
            if (t != null) {
                for (int i = 0; i < t.length(); i++) {
                    if (t.charAt(i) == '-') {
                        ++m;
                    }
                    if (t.charAt(i) == '|') {
                        ++m;
                    }
                }
            }

            return m;
        }

        public void addProb(double p) {
            this.prob += Math.log(p);
            //this.prob *= p;
        }


        public void setTrad(String t, int l, double p) {
            this.setTrad(t, l);
            prob = p;
        }

        public void setTrad(String t,int l) {
            traduccio = t;
            mida = l;
        }

        @Override
        public int compareTo(LlistaNgrames o) {
            if (this.traduccio == null) {
                return 1;
            }

            if (o.traduccio == null) {
                return -1;
            }

            int m = -(o.mida - this.mida);
            if(m>0) {
                return 1;
            } else {
                if (m==0) {
                    double d = (o.prob - this.prob);
                    return (d > 0) ? 1 : ((d == 0) ? 0 : -1);
                } else {
                    return -1;
                }
            }
        }

        public String[] getLlista() {
            return llista.split("-");
        }

        public String getLast() {
            String [] l = llista.split("-");
            return l[l.length-1];
        }
        
        public String getString() {
            return llista;
        }

        public boolean addCode(String st,String td) {
            
            int tl = LlistaNgrames.length(td);
            /*
             int sl = st.length();

            if(sl>1) {
                if(tl>1){
                    if(tl>=sl) {
                        return false;
                    }
                }
            }
               */
            if(traduccio==null)
                traduccio = td;
            else
                traduccio += "|" + td;

            mida += tl;

            return true;
        }
        
        public double getProb() {
            return prob;
        }
        
        public int getMida() {
            return mida;
        }
        
        public String getTrad() {
            return traduccio;
        }
        
        
        public void setTrad(String org,String str,double pr, int md) {
            llista = org;
            traduccio = str;
            mida = md;
            prob = pr;
        }
        
    }
