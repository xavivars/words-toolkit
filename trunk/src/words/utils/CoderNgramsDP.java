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

import java.util.ArrayList;
import java.util.Collections;

public class CoderNgramsDP extends Coder{

    public CoderNgramsDP(Huffman hf) {
        super(hf);
    }

    public CoderNgramsDP() {
        super();
    }

    private ArrayList<LlistaNgrames> getAll(String str) {
        ArrayList<LlistaNgrames> llista = new ArrayList<LlistaNgrames>();
        ArrayList<LlistaNgrames> ret = new ArrayList<LlistaNgrames>();
    
        int longitud = str.length();
        String last = "";
        String now = "";
        String sub = "";
        
        if(longitud < 1)
            return ret;
        
        now+=str.charAt(0);
        llista.add(new LlistaNgrames());
        LlistaNgrames actual = new LlistaNgrames();
        LlistaNgrames cp = null;
        int mida;
        double prob;
        boolean canvia;
        String trad;
        Pair<String,Double> pair = this.coder.get(now);
        mida=LlistaNgrames.length(now);
        trad = pair.getFirst();
        prob = pair.getSecond();
        
        actual.setTrad(now,trad,Math.log(prob), mida);
        llista.add(actual);
        
        for (int i = 2; i <= longitud; ++i) {
            now+=str.charAt(i-1);
            actual = null;
            pair = this.coder.get(now);
            
            if(pair!=null) {
                actual = new LlistaNgrames();
                mida = LlistaNgrames.length(pair.getFirst());
                prob = pair.getSecond();
                actual.setTrad(now,pair.getFirst(), Math.log(prob), mida);
            }
            
            for(int j=1;j<i;++j) {
                if(actual==null)
                    canvia = true;
                else
                    canvia = false;
                sub=now.substring(j, i);
                Pair<String,Double> p2 = this.coder.get(sub);
                
                int md = 0;
                double pr = 0;
                LlistaNgrames compara = null;
                
                if(p2!=null) {
                    md = LlistaNgrames.length(p2.getFirst());
                    pr = Math.log(p2.getSecond());
                    compara = llista.get(j);

                    if(pair!=null) {
                        if((md+compara.getMida())<=actual.getMida()) {
                            canvia = true;
                            if((pr+compara.getProb())<=actual.getProb()) {
                                    canvia = false;
                            }
                        }
                    } else {
                        actual = new LlistaNgrames();
                        pair = p2;
                        canvia = true;
                    }
                } else {
                    canvia = false;
                }
                
                if(canvia) {
                    //pair = new Pair<String, Double>("",0);
                    actual.setTrad(compara.getString()+"-"+sub,compara.getTrad()+"-"+p2.getFirst(),compara.getProb()+pr,compara.getMida()+md);
                }
            }
            
            llista.add(actual);
        }
        actual = llista.get(llista.size()-1);
        ret.add(actual);
        
        return ret;
    }
    
    private ArrayList<LlistaNgrames> getAllBT(String str) {
        ArrayList<LlistaNgrames> llista = new ArrayList<LlistaNgrames>();
        ArrayList<LlistaNgrames> ret = new ArrayList<LlistaNgrames>();
        LlistaNgrames aux = null;
        int longitud = str.length();
        for (int i = 0; i < longitud; ++i) {
            //System.out.println("Lletra "+i+": "+str.charAt(i));
            llista = addChar(llista, str.charAt(i));
        }

        //System.out.println("Ultims càlculs...");

        for (LlistaNgrames ln : llista) {
            aux = cod(ln);
            if(aux!=null) {
                aux.addProb(coder.get(aux.getLast()).getSecond());
                ret.add(aux);
            }
        }

        return ret;
    }

    private LlistaNgrames cod(LlistaNgrames ln) {
        String st = ln.getLast();
        LlistaNgrames ret = null;

        if (coder.containsKey(st)) {
            if(ln.addCode(st,coder.get(st).getFirst()))
                ret = ln;
        }

        return ret;
    }

    private ArrayList<LlistaNgrames> addChar(ArrayList<LlistaNgrames> llista, char c) {
        ArrayList<LlistaNgrames> llistaBuida = new ArrayList<LlistaNgrames>();

        if (llista.isEmpty()) {
            LlistaNgrames ln = new LlistaNgrames(c);
            llistaBuida.add(ln);
        } else {
            for (LlistaNgrames ln : llista) {
                if(coder.containsKey(ln.getLast()+c)) {
                    llistaBuida.add(ln.junt(c));
                }

                LlistaNgrames aux = cod(ln);
                if(aux!=null) {
                    double p = coder.get(aux.getLast()).getSecond();
                    LlistaNgrames tmp = aux.separat(c);
                    tmp.addProb(p);
                    llistaBuida.add(tmp);
                }
            }
        }

        return llistaBuida;
    }

    @Override
    public String encode(String str, int num) {
        String ret = "";

        ArrayList<LlistaNgrames> llista = getAll(str);

        //System.out.println("Ordenant...");
        Collections.sort(llista);
        //Collections.reverse(llista);

        //System.out.println("");
        //System.out.println(str + " (" + llista.size() + "/"+((int)Math.pow(2,str.length()-1))+"): ");
        int i = 0;
        for (LlistaNgrames ln : llista) {
            ln.printProbs(coder);
            if (num > 0) {
                if (++i >= num) {
                    break;
                }
            }
        }

        return ret;
    }

}
