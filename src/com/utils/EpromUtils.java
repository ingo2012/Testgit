/*
 * EpromUtils.java
 * Created on 09.03.2010, 20:58:24
 *
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
Info : $Id: EpromUtils.java,v 1.10 2010/05/22 16:18:01 cvs Exp $
Log  : $Log: EpromUtils.java,v $
Log  : Revision 1.10  2010/05/22 16:18:01  cvs
Log  : Stand 22.05.10
Log  :
Log  : Revision 1.9  2010/05/21 16:23:10  cvs
Log  : Stand 20.05.10
Log  :
Log  : Revision 1.8  2010/05/20 22:20:39  cvs
Log  : Stand 20.05.10
Log  :
Log  : Revision 1.7  2010/05/19 17:04:05  cvs
Log  : Stand 19.05.10
Log  :
Log  : Revision 1.6  2010/05/17 21:28:58  cvs
Log  : Stand 17.05.10
Log  :
Log  : Revision 1.5  2010/05/14 10:02:34  cvs
Log  : Stand 14.05.10
Log  :

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 *
 * 
 * From http://www.rgagnon.com/javadetails/java-0596.html
 */
package com.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class EpromUtils {

    static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };
    public byte[] prom = new byte[128];
    public long crcSum = 0L;

    /**
     *
     * @param raw
     * @return Hex-String
     * @throws UnsupportedEncodingException
     */
    public String getHexString(byte[] raw) throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex, "ASCII");
    }

    /**
     *
     * @param raw
     * @return Hex-String
     * @throws UnsupportedEncodingException
     */
    public String getHexStringByte(byte raw) throws UnsupportedEncodingException {
        byte[] hex = new byte[2];
        int index = 0;

        //for (byte b : raw) {
        int v = raw & 0xFF;
        hex[index++] = HEX_CHAR_TABLE[v >>> 4];
        hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        //}

        return new String(hex, "ASCII");
    }

    /**
     * Init the prom Array
     * @param promSize
     */
    public void initProm(int promSize) {
        prom = new byte[promSize];
        for (int i = 0; i < promSize; i++) {
            prom[i] = (byte) 255;
        } //init with 0xFF
    }

    /**
     * Read binfile
     * @param f
     * @param n Filesize
     */
    public void readBinFile(String f, long n) {
        BufferedInputStream in = null;

        int bufLen = (int) n;
        byte[] bytes = new byte[bufLen];
        String hexStr;

        System.out.println("FName="+f+ "Len="+bufLen);
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            in.read(bytes, 0, bufLen);
            in.close();
            hexStr = getHexString(bytes);
            FileWriter fw = new FileWriter(f+".hex");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(hexStr);
            bw.close();
        } catch (IOException ex) {
            System.err.println("Error=" + ex);
        }
    }

    /**
     * Read Hexfile
     * @param f
     * @param n Filesize
     */
    public void readHexFile(String f, long n) {
        int bufLen = (int) n;
        byte[] bytes = new byte[bufLen];
        String hexStr;

        System.out.println("FName="+f+ "Len="+bufLen);
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
	    hexStr = r.readLine();
            r.close();
            hex2bin((bufLen / 2 ),hexStr);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f+".bin"));
            try {
                out.write(prom, 0, (bufLen / 2 ));
                out.flush();
                out.close();
            } catch (IOException ex) {
                System.out.println("Error=" + ex);
            }
          } catch (IOException ex) {
            System.err.println("Error=" + ex);
        }
    }

    /**
     * Read file into prom buffer
     * @param f
     * @param n Filesize
     */
    public void readFile(String f, long n) {
        BufferedInputStream in = null;
        int bufLen = 0;
        if (n > prom.length) {
            bufLen = prom.length;
        } else {
            bufLen = (int) n;
        }
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            in.read(prom, 0, bufLen);
            in.close();
        } catch (IOException ex) {
            System.err.println("Error=" + ex);

        }
       calcCRC();
    }

    public void calcCRC() {
       crcSum = 0;
       for(int i=0;i<prom.length;i++){
           crcSum = crcSum+ (prom[i] & 255);
       }
    }

    /**
     *
     * @param toFill
     * @param amount
     * @param filler
     * @return String
     */
    public String fill(String toFill, int amount, char filler) {
        String s = "";
        for (int i = 0; i < amount - toFill.length(); i++) {
            s = s + filler;
        }
        toFill = s + toFill;
        return (toFill);
    }

    public String getCRC() {
        String temp = Long.toHexString(crcSum);
        temp = temp.toUpperCase();
        temp = fill(temp, 8, '0');
        return(temp);
    }
    /**
     *
     * @param filename
     */
    public void saveFile(String filename) {
      BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filename));
            try {
                out.write(prom, 0, prom.length);
                out.flush();
                out.close();
            } catch (IOException ex) {
                System.out.println("Error=" + ex);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error=" + ex);
        }
    }

    /**
     * 
     * @param filename
     * @param hexstr
     */
    public void saveHexFile(String filename,String hexstr) {
        
       BufferedWriter out = null;
       try {
          out = new BufferedWriter(new FileWriter(filename));
          out.write(hexstr);
          out.close();
       } catch (IOException ex) {
          System.out.println("Error=" + ex);
      }
    }


    private String asciiToHex(String ascii) {
        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < ascii.length(); i++) {
            System.out.println("i=" + i + " Val" + ascii.charAt(i));
            hex.append(Integer.toHexString(ascii.charAt(i)));
        }

        System.out.println("Value of ascii " + ascii + ": " + hex.toString());

        return hex.toString();
    }

   
    /**
     * Convert a Hex-String to binary
     * @param promSize
     * @param hexdata
     */
    public void hex2bin(int promSize,String hexdata) {
        prom = new byte[promSize];
        String temp;
        int val;
        for (int i = 0; i < hexdata.length(); i += 2) {
            temp = "0x"+hexdata.charAt(i)+hexdata.charAt(i+1);
            val  = Integer.decode(temp);
            prom[i / 2] = (byte) val;
        }
    }

   public void chdAdress(int i, int chCell) {
     System.out.println("i="+i+ " Neu="+chCell+" alt="+prom[i]);
     //System.out.println("chCell " +getHexStringByte(chCell));
     prom[i] = (byte) chCell;

   }

   public void checkAdr(int i, int i0) {
    System.out.println("Adresse "+i+": ="+prom[i] +" Soll="+i0);
   }
}
