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
import java.util.TreeMap;


    public class Classificacio extends TreeMap<Double, ArrayList<String>> {

        public Classificacio() {
            super();
        }

        public Classificacio add(String w, Double d) {
            ArrayList<String> al = null;
            if (this.containsKey(d)) {
                this.get(d).add(w);
            } else {
                al = new ArrayList<String>();
                al.add(w);
                this.put(d, al);
            }
            return this;
        }
    }
