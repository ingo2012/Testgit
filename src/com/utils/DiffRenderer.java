/*
 * Eprog.java
 *
 * Created on 20.05.2010, 18:58
 *

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: DiffRenderer.java,v 1.1 2010/05/22 16:18:01 cvs Exp $
 Log  : $Log: DiffRenderer.java,v $
 Log  : Revision 1.1  2010/05/22 16:18:01  cvs
 Log  : Stand 22.05.10
 Log  :
 Log  : Revision 1.10  2010/05/21 16:23:10  cvs
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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author monitor
 */
public class  DiffRenderer extends JLabel implements TableCellRenderer {

   
    public Color color = new Color(0, 0,0);
    public boolean arr[][] = null;

    public DiffRenderer() {
      int x;
      int y;
      arr = new boolean[9][1024];

      for(x=1;x<9;x++) {
          for(y=1;y<1024;y++) {
              arr[x][y] = false;
          }
      }
      arr[3][5] = true;
      arr[8][1023] = true;
    }

    DiffRenderer(Color color) {
        this.color = color;
    }

    DiffRenderer(boolean[][] arr) {
        this.arr = arr;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,boolean a, boolean b, int row, int column) {

        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(JLabel.CENTER);

        this.setFont(new java.awt.Font("Courier New", 0, 14));
	this.setForeground(color);

        // System.out.println("row="+row+ "col="+column);
        
        if(arr[column][row]) {
            this.setForeground(Color.RED);
            this.setFont(new java.awt.Font("Courier New", 1, 14));
        }

        
        this.setText(String.valueOf(value));
	return this;
    }
}

