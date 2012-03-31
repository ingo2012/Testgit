/*
 * EpromTable.java
 *
 * Created on 09.03.2010, 20:58:24
 *

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: EpromTable.java,v 1.9 2010/05/22 16:18:01 cvs Exp $
 Log  : $Log: EpromTable.java,v $
 Log  : Revision 1.9  2010/05/22 16:18:01  cvs
 Log  : Stand 22.05.10
 Log  :
 Log  : Revision 1.8  2010/05/21 16:23:10  cvs
 Log  : Stand 20.05.10
 Log  :
 
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 *
 */
package com.utils;

import java.awt.Color;
import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author monitor
 */
public class EpromTable {

    String[] columnNames     = {"Adress", "00", "01", "02", "03", "04", "05", "06", "07", "ASCII"};
    Object[][] data          = {};
    private JTable jTable1;
    private JScrollPane scrPane;
    private String pHeadline = "Header";
    private String checkSum  = null;
    private String fName     = null;
    private boolean isDiff   = false;

    DefaultTableModel tModel = new DefaultTableModel(data, columnNames) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    DefaultTableCellRenderer r = new DefaultTableCellRenderer() {

        @Override
        @SuppressWarnings("element-type-mismatch")
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setVerticalAlignment(JLabel.CENTER);
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    };

    /**
     *
     * @param sPane
     * @param pHead
     */
    public EpromTable(JScrollPane sPane,String pHead) {
        pHeadline = pHead;
        scrPane   = sPane;
        jTable1 = new javax.swing.JTable();
        jTable1.setModel(tModel);
        jTable1.setDefaultRenderer(Object.class, r);
        jTable1.setFont(new java.awt.Font("Courier New", 0, 14));
        //jTable1.setAutoResizeMode(0);
        scrPane.setViewportView(jTable1);
        scrPane.setSize(400, 200);
        deleteFileName(); // Headline Table

        // sorter.setModel(tModel );
        jTable1.sizeColumnsToFit(0);
        //jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel col = jTable1.getColumnModel();
        // 1. Col contains Version

        // 2. Col contains rowcount
        col.getColumn(0).setMinWidth(90);
        col.getColumn(0).setMaxWidth(90);
        col.getColumn(0).setWidth(90);

        col.getColumn(2).setMinWidth(30);
        col.getColumn(2).setMaxWidth(30);
        col.getColumn(2).setWidth(30);

        //CellRenderer cellRenderer = new CellRenderer(new Color(0, 222, 0));
        for (int i = 1; i < 10; i++) {
            col.getColumn(i).setMinWidth(30);
            col.getColumn(i).setMaxWidth(30);
            col.getColumn(i).setWidth(30);
        }

        col.getColumn(9).setMinWidth(250);
        col.getColumn(9).setMaxWidth(250);
        col.getColumn(9).setWidth(250);

        CellRenderer cellRenderer = new CellRenderer();
        col.getColumn(9).setCellRenderer(cellRenderer);
    }

    /**
     * 
     * @param color
     */
    public void setDataColor(Color color) {
        CellRenderer cellRenderer = new CellRenderer(color);
        TableColumnModel col = jTable1.getColumnModel();
        for (int i = 1; i < 9; i++) {
           col.getColumn(i).setCellRenderer(cellRenderer);
        }
        jTable1.repaint();
    }

    /**
     * 
     * @param master
     * @param slave 
     */
    public void setDiffMask(EpromUtils master,EpromUtils slave) {
        TableColumnModel col = jTable1.getColumnModel();
        int promAdr     = 0;
        int x           = 0;
        int y           = 0;
        int n           = (master.prom.length / 8)+1;
        boolean[][] arr = new boolean[9][n];
        isDiff          = false;

        for(y=1;y<n;y++) {
         for(x=1;x<9;x++) {
             arr[x][y-1] = true;
             if(master.prom[promAdr] == slave.prom[promAdr]) {
                 arr[x][y-1] = false;
             } else { isDiff = true; }
             promAdr++;
          }
        }
        DiffRenderer diffRenderer = new DiffRenderer(arr);
        for (int i = 1; i < 9; i++) {
           col.getColumn(i).setCellRenderer(diffRenderer);
        }
        jTable1.repaint();
    }

    /**
     * Get the state aof comparison
     * @return
     */
    public boolean getIsDiff() {
        return(isDiff);
    }
    /**
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fName = fileName;
        this.checkSum = null;
        setBorderTitle();
    }

    /**
     *
     */
    public void deleteFileName() {
        this.fName=null;
        this.checkSum = null;
        setBorderTitle();
    }

    public void setCheckSum(String chk) {
        this.checkSum = chk;
        setBorderTitle();
    }

    public String getCheckSum() {
        return(checkSum);
    }

    public void deleteCheckSum() {
        this.checkSum = null;
        setBorderTitle();
    }

    private void setBorderTitle() {
        scrPane.setBorder(javax.swing.BorderFactory.createTitledBorder(" "+pHeadline+(fName != null ? " / File ("+fName+")" : "")+(checkSum != null ? " CheckSum:"+checkSum : "")));
    }

    /**
     *
     * @param table
     * @param eu
     */
    public void showPromTable(EpromTable table, EpromUtils eu) {
        byte ioBuf;
        int i           = 0;
        int n           = -1;
        String content  = "";
        byte[] inbyte   = new byte[8];
        String s[]      = new String[8];
        //table.setBorder(javax.swing.BorderFactory.createTitledBorder(fName));
        for (i = 0; i < eu.prom.length; i++) {
            n++;
            ioBuf = eu.prom[i];
            try {
                s[n] = eu.getHexStringByte(ioBuf);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(EpromTable.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (eu.prom[i] < 33) {
                inbyte[n] = 46; // Chars < 33 are set to "."
            } else {
                inbyte[n] = eu.prom[i];
            }

            if ((n % 7 == 0) && (n > 0)) {
                try {
                    content = new String(inbyte, "ISO-8859-1");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(EpromTable.class.getName()).log(Level.SEVERE, null, ex);
                }
                table.addRow(i, s, content);
                n = -1;
            }

        }
    }

    public void updateCell(int row,int col) {
        tModel.fireTableCellUpdated(row, col);
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

    /**
     *
     * @param i
     * @param s
     * @param Ascii
     */
    public void addRow(int i, String[] s, String Ascii) {
        String t = Integer.toHexString(i);
        t = "0x" + fill(t, 5, '0'); // Fill the Adress with 0..
        tModel.addRow(new Object[]{t, s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], Ascii});
    }

    public void deleteData() {
        tModel.setRowCount(0);
    }


}
