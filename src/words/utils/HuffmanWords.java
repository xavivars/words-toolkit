/*
 * Copyright (C) 2008
 *
 * Author:
 *  Xavier Ivars i Ribes <xavi@infobenissa.com>
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

import java.util.Set;
import words.Dictionary;

public class HuffmanWords extends Huffman {

    private static int MIN = 1;

    public HuffmanWords() {
        version = "HuffmanWords";
    }

    public HuffmanWords(int d) {
        this();
        degree = d;
    }

    public void getText(String d) {
        int tmp = 0;
        Dictionary dict = new Dictionary();
        dict.getText(d);

        Set<String> wds = dict.getWords();

        for (String s : wds) {
            tmp = dict.getWord(s);
            if (tmp > MIN) {
                strings.put(s, new Int(tmp));
                count.get(0).add(tmp);
            }
        }
    }
}
