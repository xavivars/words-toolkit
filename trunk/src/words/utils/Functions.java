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

import java.io.*;
import java.util.*;

public class Functions {

    static public String reverse(String w) {
        return (new StringBuffer(w)).reverse().toString();
    }

    static public String randomName() {
        Random rnd = new Random(System.currentTimeMillis());

        int randomInt = rnd.nextInt();

        Integer i = new Integer(new Integer(Math.abs(randomInt))).hashCode();

        return i.toString();
    }

    static public String bytes2file(byte[] b, String cand) {
        String ret = null;

        try {
            File f = new File(cand);

            if (f.exists()) {
                cand = Functions.getTempDir() + Functions.randomName();
            }
            f = null;
            ret = cand;

            FileOutputStream fw = new FileOutputStream(ret);

            if (b != null) {
                fw.write(b);
            }

            fw.close();

        } catch (IOException ioe) {
            System.err.println("ERROR: bytes2file");
            ioe.printStackTrace(System.err);
        }

        return ret;
    }

    static public byte[] file2bytes(String strIn) {
        try {
            FileInputStream fi = new FileInputStream(strIn);

            ArrayList<Byte> bytes = new ArrayList<Byte>();
            byte c;
            int rc;
            while ((rc = fi.read()) > -1) {
                c = (byte) (rc & 255);
                bytes.add(c);
            }
            fi.close();

            int bsize = bytes.size();
            byte[] rbytes = new byte[bsize];

            for (int i = 0; i < bsize; i++) {
                rbytes[i] = ((Byte) bytes.get(i)).byteValue();
            }

            return rbytes;
        } catch (Exception e) {
            // per gestionar
            System.err.println("ERROR: file2bytes");
            e.printStackTrace(System.err);
        }

        return null;
    }

    static public String getTempDir() {
        String ret = System.getProperty("java.io.tmpdir");

        if (!(ret.endsWith("/") || ret.endsWith("\\"))) {
            ret = ret + System.getProperty("file.separator");
        }

        return ret;
    }

    static public String getCurrentDir(Object ob) {
        String tmpRuta = ob.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.print(".");
        File tmpFile = new java.io.File(tmpRuta);
        tmpRuta = tmpFile.getAbsolutePath() + "/";
        tmpFile.delete();

        return tmpRuta;
    }

    static public String sizedCorpus(String corpus, int size) {
        TextReader scanner = new TextReader(corpus, WordType.LETTERS);
        String word;
        File temp = null;
        PrintWriter pw = null;
        String ncorpus = null;

        try {
            temp = File.createTempFile("size_tester", "." + size);

            // Delete temp file when program exits.
            temp.deleteOnExit();

            pw = new PrintWriter(new BufferedWriter(new FileWriter(temp)));
            int num = -1;

            while (((word = scanner.nextWord()) != null) && ((++num) < size)) {
                pw.println(word);
            }

            pw.close();

            ncorpus = temp.getPath();

        } catch (IOException ioe) {
            System.err.println("ERROR: cannot read file (" + corpus + ")");
        }

        return ncorpus;
    }
}