/*
 * BackGroundPane.java
 * @author monitor

 * Created on 13.05.2010, 20:58:24
 *
 *  setContentPane(new BackGroundPane("src/com/Images/bg.png"));
 *  initComponents();
 *  .......
 *
 *

--------------------------------------------------------------------------------
-----------------------       C V S - I n f o    -------------------------------
--------------------------------------------------------------------------------
 Info : $Id: BackGroundPane.java,v 1.4 2010/05/22 16:18:17 cvs Exp $
 Log  : $Log: BackGroundPane.java,v $
 Log  : Revision 1.4  2010/05/22 16:18:17  cvs
 Log  : Stand 22.05.10
 Log  :
 Log  : Revision 1.3  2010/05/21 16:23:10  cvs
 Log  : Stand 20.05.10
 Log  :
 Log  : Revision 1.2  2010/05/14 10:02:34  cvs
 Log  : Stand 14.05.10
 Log  :

--------------------------------------------------------------------------------
-----------------------       C V S - I n f o    -------------------------------
--------------------------------------------------------------------------------
 *
 */


package com.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.swing.JPanel;



/**
 * Set the BackgroundPane
 * @author monitor
 */
public class BackGroundPane extends JPanel {
		Image img = null;

		BackGroundPane(String imagefile) {
			if (imagefile != null) {
				MediaTracker mt = new MediaTracker(this);
				img = Toolkit.getDefaultToolkit().getImage(imagefile);
				mt.addImage(img, 0);
				try {
					mt.waitForAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

    @Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(img,0,0,this.getWidth(),this.getHeight(),this);
		}
}
