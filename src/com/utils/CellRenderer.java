/*
 * CellRenderer.java
 *
 * Created on 09.03.2010, 20:58:24
 *

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: CellRenderer.java,v 1.3 2010/05/22 16:18:01 cvs Exp $
 Log  : $Log: CellRenderer.java,v $
 Log  : Revision 1.3  2010/05/22 16:18:01  cvs
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
public class  CellRenderer extends JLabel implements TableCellRenderer {

    public Color color = new Color(0, 0,222);

    public CellRenderer() {}

    CellRenderer(Color color) {
        this.color = color;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,boolean a, boolean b, int row, int column) {
        
        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(JLabel.CENTER);

        this.setFont(new java.awt.Font("Courier New", 0, 14));
	this.setForeground(color);
        this.setText(String.valueOf(value));
	return this;
    }
}

