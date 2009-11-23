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

import words.dmm.State;


    public class Connection {

        public Connection(String tg, State st) {
            this.tag = tg;
            this.to = st;
            this.times = 0;
        }

        public String getTag() {
            return tag;
        }

        public State getTo() {
            return to;
        }

        public int getTimes() {
            return times;
        }

        public void incTimes() {
            ++times;
        }

        public void setTimes(int i) {
            times = i;
        }
        private String tag;
        private State to;
        private int times;
    }