/*
 * Eprog.java
 *
 * Created on 09.03.2010, 20:58:24
 *

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: Eprog.java,v 1.13 2010/05/24 09:52:40 cvs Exp $
 Log  : $Log: Eprog.java,v $
 Log  : Revision 1.13  2010/05/24 09:52:40  cvs
 Log  : Stand 24.05.10
 Log  :
 Log  : Revision 1.12  2010/05/23 12:09:21  cvs
 Log  : Stand 23.05.10
 Log  :
 Log  : Revision 1.11  2010/05/22 16:18:17  cvs
 Log  : Stand 22.05.10
 Log  :
 Log  : Revision 1.10  2010/05/21 16:23:10  cvs
 Log  : Stand 20.05.10
 Log  :
 Log  : Revision 1.9  2010/05/20 22:20:39  cvs
 Log  : Stand 20.05.10
 Log  :
 Log  : Revision 1.8  2010/05/19 17:04:05  cvs
 Log  : Stand 19.05.10
 Log  :
 Log  : Revision 1.7  2010/05/18 18:05:22  cvs
 Log  : Stand 18.05.10
 Log  :
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 *
 */

package com.gui;

import com.utils.EprogTTy;
import com.utils.EpromUtils;
import com.utils.EpromTable;
import com.utils.Logging;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;


/**
 * 
 * @author egerlai
 */
public class Eprog extends javax.swing.JFrame  {
     
     EpromTable mTable;  /* Master */
     EpromTable sTable;  /* Slave */
     EpromUtils master                      = new EpromUtils();
     EpromUtils slave                       = new EpromUtils();

     final int ScreenWidth                  = 1058;
     final int ScreenHeight                 = 768;
     final int chkSumBytes                  = 8;    /* bytes for CheckSum */
     final int blockLen                     = 32;   /* block Size */

     public static  Logging  log            = null;
     private static boolean Debug           = false;
     private static String  LogLevel        = "3";

     HashMap<String, EpromType> EpromMap    = EpromMap = new HashMap<String, EpromType>();
     String actualEprom                     = "C";

     EprogTTy eprog;
     private static String EprogPort        = "/dev/ttyUSB1";
     
     private JDialog aboutBox               = null;
     private int progBarVal                 = 0;

     private int readCompleteBytes          = 0;
     private String kplStr                  = "";
     private SwingWorker<String, Void> worker;


     /** Creates new form Eprog
      * @throws UnsupportedEncodingException 
      */

    public Eprog() throws UnsupportedEncodingException {
        setContentPane(new BackGroundPane("src/com/Images/bg.png"));
        initComponents();

        addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
          public void windowClosing(java.awt.event.WindowEvent evt) {
             exitForm(evt);
          }
         });

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-ScreenWidth)/2, (screenSize.height-ScreenHeight)/2, ScreenWidth, ScreenHeight);
        
        log = new Logging(LogScreen);
        log.setDebug(Debug);
        
        MessageOk("Starting....");
        generateEpromList();

        /* Set Tables */
        mTable = new EpromTable(scrPaneMaster," Master ");
        sTable = new EpromTable(scrPaneSlave," Slave ");
        showEpromList(jListEpromType); // call showPromTable automatic !
        /* ToDo , muss weg */
        actualEprom = "7";
        jListEpromType.setSelectedIndex(5);

        jbCancelAction.setEnabled(false);
        MessageOk("Ok");
        log.info("Init EPROG..");
       
        // Init SerialDevice
        eprog = new EprogTTy(EprogPort,progBar,log);
        // We enable it only if TTY found
        setPullDownMenue(eprog.ttyFound);
    }

    private void setPullDownMenue(boolean b) {
        slaveMenue.setEnabled(b);
        masterMenue.setEnabled(b);
        if(b) {
            eprog.setIsCancel(false);
        }
    }

    private void showPromTable() {
        mTable.deleteData();
        sTable.deleteData();
        master.initProm(EpromMap.get(actualEprom).Size);
        slave.initProm(EpromMap.get(actualEprom).Size);
        mTable.showPromTable(mTable,master);
        sTable.showPromTable(sTable,slave);
    }

    /**
      * generate a EpromMap with all supported EpromTypes
      * and the corresponding command.
      *
      */
    private void generateEpromList() {
        EpromType p;
        p = new EpromType("2716",2048,"C",2000);
        EpromMap.put("C",p);

        p = new EpromType("2532",4096,"B",4000);
        EpromMap.put("B",p);

        p = new EpromType("2732",4096,"A",4000);
        EpromMap.put("A",p);

        p = new EpromType("2732A",4096,"9",4000);
        EpromMap.put("9",p);

        p = new EpromType("2764",8192,"8",8000);
        EpromMap.put("8",p);

        p = new EpromType("2764A",8192,"7",8000);
        EpromMap.put("7",p);

        p = new EpromType("27128",16384,"6",16000);
        EpromMap.put("6",p);

        p = new EpromType("27128A",16384,"5",16000);
        EpromMap.put("5",p);

        p = new EpromType("27256 / 21,0 V",32768,"4",32000);
        EpromMap.put("4",p);

        p = new EpromType("27256 / 12,5 V",32768,"3",32000);
        EpromMap.put("3",p);

        p = new EpromType("27512",65536,"2",65000);
        EpromMap.put("2",p);
    }

   /**
    * Show the List in reverse order.
    *
    * @param jComboBox 
    */
    public void showEpromList(javax.swing.JComboBox jComboBox) {
        EpromType p;
        jComboBox.removeAllItems();

        List<String> sortedList = new ArrayList<String>();
        sortedList.addAll(EpromMap.keySet());
        Comparator<String> comparator = Collections.<String>reverseOrder();
        Collections.sort(sortedList,comparator);

        for (int i = 0; i < sortedList.size(); i++) {
            jComboBox.addItem(EpromMap.get(sortedList.get(i)).Type);
        }
    }

    /**
     * Show Message in StatusLine
     * @param message
     */
    public void MessageOk(String message) {
         Message.setForeground(Color.blue);
         Message.setText(message);
         Message.repaint();
    }

    /**
     * Show red Message in StatusLine
     * @param message
     */
    public void MessageErr(String message) {
         Message.setForeground(Color.red);
         Message.setText(message);
         Message.repaint();
    }

   /**
     * Load a binary File into table
     *
     */
   private void loadFile(EpromUtils eu,EpromTable eT) {
      JFileChooser chooser = new JFileChooser();
      String FileName="";
      String FullFileName="";
      int returnVal = chooser.showOpenDialog(new JFrame());
      if(returnVal == JFileChooser.APPROVE_OPTION) {
          FullFileName = chooser.getSelectedFile().getAbsolutePath();
          FileName = chooser.getSelectedFile().getName();
          File f = new File(FullFileName);
          if(f.exists()) {
           MessageOk("Read "+FileName);

           if(f.length() > EpromMap.get(actualEprom).Size) {
            JOptionPane.showMessageDialog(this, "Filesize ("+f.length()+" Bytes) greater then EPromSize "+
                                                 EpromMap.get(actualEprom).Size+" Bytes\n"+
                                                "We only read "+EpromMap.get(actualEprom).Size+" Bytes");
           }
           eT.deleteData();
           eu.initProm(EpromMap.get(actualEprom).Size);
           eu.readFile(FullFileName,f.length());
           eT.deleteCheckSum();
           eT.setFileName(FileName);
           eT.showPromTable(eT, eu);
           eT.setDataColor(new Color(0, 0, 0));
           eT.setCheckSum(eu.getCRC());
          } else {
              JOptionPane.showMessageDialog(this, "File "+FileName+" does not exist!");
          }
      }
    }

    /**
     * Save binary data into a File
     *
     */
     private boolean saveFile(EpromUtils eu,EpromTable eT) {
          JFileChooser chooser = new JFileChooser();
          String FileName="";
          String FullFileName="";
          int returnVal = chooser.showSaveDialog(new JFrame());
          if(returnVal == JFileChooser.APPROVE_OPTION) {
           FullFileName = chooser.getSelectedFile().getAbsolutePath();
           FileName = chooser.getSelectedFile().getName();
           File f = new File(FullFileName);
           if(f.exists()) {
             int resultSave = JOptionPane.showConfirmDialog(this, "File "+FileName+" exists! Overwrite ?", "File Exist!", JOptionPane.YES_NO_OPTION);
             if (resultSave != JOptionPane.YES_OPTION) {
	       return(false);
             }
           }
           MessageOk("Save File "+FullFileName);
           eu.saveFile(FullFileName);
           eT.setFileName(FileName);
          }
        return(true);
     }

     /* Action Methods */
     /**
     * Perform a blank Test
     * @param utils,Table, Master
     */
    private void blankTest(EpromUtils eu,final EpromTable eT,final boolean mMaster) {
        final Color emptyOk  = new Color(0, 200, 0);
        final Color emptynOk = new Color(200, 0, 0);

        setPullDownMenue(false);
        jbCancelAction.setEnabled(true);
        eprog.isReadState = true;

        // Delete old data from Table before BlankTest
        eT.deleteData();
        eu.initProm(EpromMap.get(actualEprom).Size);
        eT.showPromTable(eT, eu);
        eT.deleteFileName();

        worker = new SwingWorker<String, Void>(){
        @Override
            @SuppressWarnings("empty-statement")
         protected void done(){
            eprog.writeString("I");
            eprog.setIsCancel(false);
            progBar.setValue(100);
            if(eprog.readStatus.equals("0")) {
             MessageOk("Eprom is empty !");
             JOptionPane.showMessageDialog(null,"Eprom is empty.");
             eT.setDataColor(emptyOk);
            } else {
                MessageOk("Eprom is not empty!");
                JOptionPane.showMessageDialog(null,"Eprom is not empty!");;
                eT.setDataColor(emptynOk);
            }
            log.info("Blank Test done. Status="+eprog.readStatus);
            setPullDownMenue(true);
            jbCancelAction.setEnabled(false);
         }

        @Override
         protected String doInBackground()  {
            eprog.setCTSprogBar(true);
            progBarVal = 0;
            progBar.setValue(progBarVal);
            eprog.writeString("IIIII");
            eprog.writeString("T");
            eprog.writeString(actualEprom);
            if(mMaster) {
             eprog.writeString("Q");
            } else {eprog.writeString("L");}
            try {
 	        Thread.sleep(EpromMap.get(actualEprom).Delay);
  	    } catch (InterruptedException e) {
                log.error("doInBackGround IntExeption :"+e);
              }
             eprog.writeString("S");
            return("Ok");
          }
        };

        // Execute the SwingWorker; the GUI will not freeze
        worker.execute();
    }

    private void readEprom(final EpromUtils eu,final EpromTable eT,final boolean mMaster) {

         jbCancelAction.setEnabled(true);
         setPullDownMenue(false);
         worker = new SwingWorker<String, Void>(){

         @Override
         protected void done(){
            eprog.writeString("I");
            if ((kplStr.length() == EpromMap.get(actualEprom).Size*2) && eprog.readStatus.equals("0") ){
             log.info("Ok Read "+kplStr.length()+" done without Error.");
             log.debug(kplStr);
             MessageOk("Ok Read "+EpromMap.get(actualEprom).Size+" Bytes.");
             JOptionPane.showMessageDialog(null,"Ok Read "+EpromMap.get(actualEprom).Size+" Bytes.");
             //eu.saveHexFile("eprom.hex", kplStr);
             eT.deleteData();
             eu.hex2bin(EpromMap.get(actualEprom).Size,kplStr);
             eT.showPromTable(eT, eu);
             eT.setDataColor(new Color(0,200,0));
             eT.deleteFileName();
             eu.calcCRC();
             eT.setCheckSum(eu.getCRC());
            } else {
                log.error("Ok Read done with Errors !!!");
                log.debug("readStatus="+eprog.readStatus+ "len="+kplStr.length());
                MessageErr("Error while reading");
                JOptionPane.showMessageDialog(null,"Error while reading");
            }
            setPullDownMenue(true);
            jbCancelAction.setEnabled(false);
         }

        @Override
         protected String doInBackground()  {
            char c;
            int eSize = EpromMap.get(actualEprom).Size*2;
            MessageOk("Start reading .....");
            kplStr            = "";
            readCompleteBytes = 0;
            progBar.setValue(0);
            eprog.serialPort.notifyOnDataAvailable(false);
            eprog.writeString("IIIII");
            eprog.writeString("T");
            eprog.writeString(actualEprom);

            if(mMaster) {
             eprog.writeString("M"); // Start read Master
            } else {
                 eprog.writeString("R"); // Start read Slave
            }
            MessageOk("Read .....");
            while (readCompleteBytes < (EpromMap.get(actualEprom).Size*2)) {
                    try {
                        c = (char) eprog.inputStream.read();
                        kplStr+=c;
                        readCompleteBytes++;
                        progBarVal = (int) ((readCompleteBytes * 1.0 / eSize * 1.0) * 100);
                        progBar.setValue(progBarVal);
                        progBar.setStringPainted(true);
                    } catch (IOException ex) {
                        log.error("Error"+ex);
                    }
            }
            eprog.serialPort.notifyOnDataAvailable(true);
            eprog.setIsCancel(false);
            eprog.isReadState = true;
            eprog.writeString("S");
            return("Ok");
          }
        };

        // Execute the SwingWorker; the GUI will not freeze
        worker.execute();
    }


    private void readCheckSum(final EpromUtils eu,final EpromTable eT,final boolean mMaster) {

         jbCancelAction.setEnabled(true);
         setPullDownMenue(false);

         worker = new SwingWorker<String, Void>(){

         @Override
         protected void done(){
            eprog.writeString("I");
            if (kplStr.length() == chkSumBytes) {
             log.info("Ok CheckSum "+kplStr+" done without Error.");
             MessageOk("Ok CheckSum="+kplStr);
             eT.deleteData();
             eu.initProm(EpromMap.get(actualEprom).Size);
             eT.showPromTable(eT, eu);
             eT.setDataColor(new Color(0,0,0));
             eT.deleteFileName();
             eT.setCheckSum(kplStr);
             JOptionPane.showMessageDialog(null,"The Checksum is "+eT.getCheckSum());
            } else {
                log.error("Ok Read done with Errors !!!");
                log.debug("readStatus="+eprog.readStatus+ "len="+kplStr.length());
                MessageErr("Error while reading");
                JOptionPane.showMessageDialog(null,"Error while reading");
                eT.deleteCheckSum();
            }
            setPullDownMenue(true);
            jbCancelAction.setEnabled(false);
         }

        @Override
         protected String doInBackground()  {
            char c;
            MessageOk("Start reading .....");
            kplStr            = "";
            readCompleteBytes = 0;
            progBar.setValue(0);
            eprog.serialPort.notifyOnDataAvailable(false);
            eprog.writeString("IIIII");
            eprog.writeString("T");
            eprog.writeString(actualEprom);

            if(mMaster) {
             eprog.writeString("O"); // Start read Master
            } else {
                 eprog.writeString("K"); // Start read Slave
            }
            MessageOk("Read .....");
            while (readCompleteBytes < chkSumBytes ) {
                    try {
                        c = (char) eprog.inputStream.read();
                        kplStr+=c;
                        readCompleteBytes++;
                        progBarVal = (int) ((readCompleteBytes * 1.0 / chkSumBytes * 1.0) * 100);
                        progBar.setValue(progBarVal);
                        progBar.setStringPainted(true);
                    } catch (IOException ex) {
                        log.error("Error"+ex);
                    }
            }
            eprog.serialPort.notifyOnDataAvailable(true);
            eprog.setIsCancel(false);
            eprog.isReadState = true;
            //eprog.writeString("S");
            return("Ok");
          }
        };

        // Execute the SwingWorker; the GUI will not freeze
        worker.execute();
    }

    /*
     * Program with 32 Bytes blocks !!!
     */
    private boolean program(final EpromUtils eu,final String alg) {
        JFileChooser chooser = new JFileChooser();

        int resultSave = JOptionPane.showConfirmDialog(this, "Start ?","Start...", JOptionPane.YES_NO_OPTION);
        if (resultSave != JOptionPane.YES_OPTION) {
	   return(false);
        }
       
        jbCancelAction.setEnabled(true);
        setPullDownMenue(false);
        worker = new SwingWorker<String, Void>(){

         @Override
         protected void done(){
            eprog.writeString("I");
            if (eprog.readStatus.equals("0") ){
             log.info("Ok programming done without Error.");
             log.debug(kplStr);
             MessageOk("Ok ");
             JOptionPane.showMessageDialog(null,"Ok programming complete");
            } else {
                log.error("Ok programming done with Errors !!!");
                log.debug("readStatus="+eprog.readStatus);
                MessageErr("Error while reading");
                JOptionPane.showMessageDialog(null,"Error while programming");
            }
            setPullDownMenue(true);
            jbCancelAction.setEnabled(false);
         }

        @Override
         protected String doInBackground()  {
            int    c      = 0;
            int    blocks = eu.prom.length / blockLen;
            byte[] bytes  = new byte[blockLen];
            String hexStr = "";
            MessageOk("Start .....");
            progBar.setValue(0);
            eprog.setCTSprogBar(false);
            //eprog.serialPort.notifyOnDataAvailable(false);
            eprog.writeString("IIIII");
            eprog.writeString("T");
            eprog.writeString(actualEprom);
            eprog.writeString("P");
            eprog.writeString(alg);

            for(int i = 0;i<blocks;i++) {
             if(eprog.getIsCancel()) { break;}
             log.debug("Read bytes="+c);
             System.arraycopy(eu.prom,c,bytes,0,blockLen);
             try {
               hexStr = eu.getHexString(bytes);
             } catch (UnsupportedEncodingException ex) {
               log.error("Error"+ex);
             }
               //for(int x=0;x<blockLen;x++) {
               //    eprog.writeString(hexStr.substring(x,x+1));
                   //log.info("n="+x+" "+hexStr.substring(x, x+1));
               //}
               log.debug("isCTS1="+eprog.serialPort.isCTS());
               eprog.writeString(hexStr);
               log.debug("isCTS2="+eprog.serialPort.isCTS());
               progBarVal = (int) ((i * 1.0 / blocks * 1.0) * 100);
               progBar.setValue(progBarVal);
               progBar.setStringPainted(true);
               log.debug("Program block:"+i+" hexStr="+hexStr);
                try {
 	         Thread.sleep(100);
  	        } catch (InterruptedException e) {
                 log.error("doInBackGround IntExeption :"+e);
                }
             c=c+blockLen;
            }
            eprog.setIsCancel(false);
            eprog.isReadState = true;
            eprog.setCTSprogBar(true);
            eprog.serialPort.notifyOnDataAvailable(true);
            try {
 	         Thread.sleep(100);
  	        } catch (InterruptedException e) {
                 log.error("doInBackGround IntExeption :"+e);
                }
            eprog.writeString("S");
            return("Ok");
          }
        };

        // Execute the SwingWorker; the GUI will not freeze
        worker.execute();
        return(true);
       
    }


    private void hexToBin() {
      JFileChooser chooser = new JFileChooser();
      String FileName="";
      String FullFileName="";
      EpromUtils t = new EpromUtils();
      int returnVal = chooser.showOpenDialog(new JFrame());
      if(returnVal == JFileChooser.APPROVE_OPTION) {
          FullFileName = chooser.getSelectedFile().getAbsolutePath();
          FileName = chooser.getSelectedFile().getName();
          File f = new File(FullFileName);
          if(f.exists()) {

           if((f.length() % 2) == 0) {
             MessageOk("Read "+FileName);
             log.debug("Size ="+f.length() +" Div="+f.length() % 2);
             t.readHexFile(FullFileName, f.length());
             MessageOk("File "+FileName+" save as "+FileName+".bin");
           } else {
               JOptionPane.showMessageDialog(this, "Filesize "+f.length()+" Bytes is not even !");
           }

          } else {
              JOptionPane.showMessageDialog(this, "File "+FileName+" does not exist!");
          }
       }
    }

    private void binToHex() {
      JFileChooser chooser = new JFileChooser();
      String FileName="";
      String FullFileName="";
      EpromUtils t = new EpromUtils();
      int returnVal = chooser.showOpenDialog(new JFrame());
      if(returnVal == JFileChooser.APPROVE_OPTION) {
          FullFileName = chooser.getSelectedFile().getAbsolutePath();
          FileName = chooser.getSelectedFile().getName();
          File f = new File(FullFileName);
          if(f.exists()) {
             MessageOk("Read "+FileName);
             t.readBinFile(FullFileName, f.length());
             MessageOk("File "+FileName+" save as "+FileName+".hex");
           }

          } else {
              JOptionPane.showMessageDialog(this, "File "+FileName+" does not exist!");
       }
    }

    private void exitForm(java.awt.event.WindowEvent evt) {
        System.out.println("Stop");
        if(eprog.ttyFound) {
         eprog.serialPort.close();
        }
        dispose();
        System.exit(0);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        Message = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        LogScreen = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        scrPaneSlave = new javax.swing.JScrollPane();
        scrPaneMaster = new javax.swing.JScrollPane();
        jbCancelAction = new javax.swing.JButton();
        progBar = new javax.swing.JProgressBar();
        jListEpromType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jQuit = new javax.swing.JMenuItem();
        masterMenue = new javax.swing.JMenu();
        masterBlankTest = new javax.swing.JMenuItem();
        masterRead = new javax.swing.JMenuItem();
        masterChkSum = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        masterComparewithSlave = new javax.swing.JMenuItem();
        masterSavetoFile = new javax.swing.JMenuItem();
        masterLoadFile = new javax.swing.JMenuItem();
        slaveMenue = new javax.swing.JMenu();
        slaveBlankTest = new javax.swing.JMenuItem();
        slaveRead = new javax.swing.JMenuItem();
        slaveChkSum = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        progMasterStd = new javax.swing.JRadioButtonMenuItem();
        progMasterIntel = new javax.swing.JRadioButtonMenuItem();
        progMasterFujitsu = new javax.swing.JRadioButtonMenuItem();
        progMasterAMD = new javax.swing.JRadioButtonMenuItem();
        jMenu3 = new javax.swing.JMenu();
        progSlaveStd = new javax.swing.JRadioButtonMenuItem();
        progSlaveIntel = new javax.swing.JRadioButtonMenuItem();
        progSlaveFujitsu = new javax.swing.JRadioButtonMenuItem();
        progSlaveAMD = new javax.swing.JRadioButtonMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        slaveCompareMaster = new javax.swing.JMenuItem();
        slaveSaveToFile = new javax.swing.JMenuItem();
        slaveLoadFile = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jHexToBin = new javax.swing.JMenuItem();
        jBinToHex = new javax.swing.JMenuItem();
        About = new javax.swing.JMenu();
        showAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("EPROG");
        setForeground(java.awt.Color.white);

        Message.setEditable(false);
        Message.setText("jTextField1");
        Message.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        LogScreen.setColumns(20);
        LogScreen.setRows(5);
        LogScreen.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(51, 51, 255)));
        jScrollPane1.setViewportView(LogScreen);

        jPanel1.setBackground(new java.awt.Color(220, 230, 237));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        scrPaneSlave.setBackground(new java.awt.Color(220, 230, 237));
        scrPaneSlave.setBorder(javax.swing.BorderFactory.createTitledBorder("Slave / File"));

        scrPaneMaster.setBackground(new java.awt.Color(220, 230, 237));
        scrPaneMaster.setBorder(javax.swing.BorderFactory.createTitledBorder("Master"));

        jbCancelAction.setText("Cancel Action");
        jbCancelAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(scrPaneMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(scrPaneSlave, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(240, 240, 240)
                        .addComponent(jbCancelAction, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrPaneSlave, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                    .addComponent(scrPaneMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbCancelAction)
                .addContainerGap())
        );

        progBar.setFont(new java.awt.Font("Dialog", 1, 10));
        progBar.setForeground(new java.awt.Color(129, 165, 246));

        jListEpromType.setBackground(new java.awt.Color(153, 204, 255));
        jListEpromType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setEpromType(evt);
            }
        });

        jLabel1.setText("EProm:");

        jMenuBar.setPreferredSize(new java.awt.Dimension(100, 21));

        jMenu1.setMnemonic('F');
        jMenu1.setText("File");

        jQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jQuit.setText("Quit");
        jQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jQuitActionPerformed(evt);
            }
        });
        jMenu1.add(jQuit);

        jMenuBar.add(jMenu1);

        masterMenue.setMnemonic('M');
        masterMenue.setText("Master");

        masterBlankTest.setText("Blank Test");
        masterBlankTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterBlankTestActionPerformed(evt);
            }
        });
        masterMenue.add(masterBlankTest);

        masterRead.setText("Read");
        masterRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterReadActionPerformed(evt);
            }
        });
        masterMenue.add(masterRead);

        masterChkSum.setText("CheckSum");
        masterChkSum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterChkSumActionPerformed(evt);
            }
        });
        masterMenue.add(masterChkSum);
        masterMenue.add(jSeparator2);

        masterComparewithSlave.setText("Compare with Slave");
        masterComparewithSlave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterComparewithSlaveActionPerformed(evt);
            }
        });
        masterMenue.add(masterComparewithSlave);

        masterSavetoFile.setText("Save to File");
        masterSavetoFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterSavetoFileActionPerformed(evt);
            }
        });
        masterMenue.add(masterSavetoFile);

        masterLoadFile.setText("Load File");
        masterLoadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterLoadFileActionPerformed(evt);
            }
        });
        masterMenue.add(masterLoadFile);

        jMenuBar.add(masterMenue);

        slaveMenue.setMnemonic('S');
        slaveMenue.setText("Slave");

        slaveBlankTest.setText("Blank Test");
        slaveBlankTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveBlankTestActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveBlankTest);

        slaveRead.setText("Read");
        slaveRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveReadActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveRead);

        slaveChkSum.setText("CheckSum");
        slaveChkSum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveChkSumActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveChkSum);

        jMenu4.setText("Program with Master-Data");

        buttonGroup2.add(progMasterStd);
        progMasterStd.setSelected(true);
        progMasterStd.setText("Standard");
        progMasterStd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progMasterStdActionPerformed(evt);
            }
        });
        jMenu4.add(progMasterStd);

        buttonGroup2.add(progMasterIntel);
        progMasterIntel.setText("FAST / Intel");
        progMasterIntel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progMasterIntelActionPerformed(evt);
            }
        });
        jMenu4.add(progMasterIntel);

        buttonGroup2.add(progMasterFujitsu);
        progMasterFujitsu.setText("FAST / Fujitsu");
        progMasterFujitsu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progMasterFujitsuActionPerformed(evt);
            }
        });
        jMenu4.add(progMasterFujitsu);

        buttonGroup2.add(progMasterAMD);
        progMasterAMD.setText("FAST / AMD");
        progMasterAMD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progMasterAMDActionPerformed(evt);
            }
        });
        jMenu4.add(progMasterAMD);

        slaveMenue.add(jMenu4);

        jMenu3.setText("Program with Slave-Data");

        buttonGroup1.add(progSlaveStd);
        progSlaveStd.setSelected(true);
        progSlaveStd.setText("Standard");
        progSlaveStd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progSlaveStdActionPerformed(evt);
            }
        });
        jMenu3.add(progSlaveStd);

        buttonGroup1.add(progSlaveIntel);
        progSlaveIntel.setText("FAST / Intel");
        progSlaveIntel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progSlaveIntelActionPerformed(evt);
            }
        });
        jMenu3.add(progSlaveIntel);

        buttonGroup1.add(progSlaveFujitsu);
        progSlaveFujitsu.setText("FAST / Fujitsu");
        progSlaveFujitsu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progSlaveFujitsuActionPerformed(evt);
            }
        });
        jMenu3.add(progSlaveFujitsu);

        buttonGroup1.add(progSlaveAMD);
        progSlaveAMD.setText("FAST / AMD");
        progSlaveAMD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progSlaveAMDActionPerformed(evt);
            }
        });
        jMenu3.add(progSlaveAMD);

        slaveMenue.add(jMenu3);
        slaveMenue.add(jSeparator3);

        slaveCompareMaster.setText("Compare with Master");
        slaveCompareMaster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveCompareMasterActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveCompareMaster);

        slaveSaveToFile.setText("Save to File");
        slaveSaveToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveSaveToFileActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveSaveToFile);

        slaveLoadFile.setText("Load File");
        slaveLoadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slaveLoadFileActionPerformed(evt);
            }
        });
        slaveMenue.add(slaveLoadFile);

        jMenuBar.add(slaveMenue);

        jMenu2.setText("Tools");

        jHexToBin.setText("Hex to Bin");
        jHexToBin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHexToBinActionPerformed(evt);
            }
        });
        jMenu2.add(jHexToBin);

        jBinToHex.setText("Bin to Hex");
        jBinToHex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBinToHexActionPerformed(evt);
            }
        });
        jMenu2.add(jBinToHex);

        jMenuBar.add(jMenu2);

        About.setMnemonic('A');
        About.setText("About");

        showAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        showAbout.setText("Info");
        showAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAboutActionPerformed(evt);
            }
        });
        About.add(showAbout);

        jMenuBar.add(About);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jListEpromType, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1546, 1546, 1546))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Message, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addContainerGap(758, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jListEpromType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Message, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setEpromType(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setEpromType
       EpromType p;
       String Item = jListEpromType.getSelectedItem().toString();
       for(String Proms : EpromMap.keySet() ) {
            p = EpromMap.get(Proms);
            if (p.Type.equals(Item)) {
              actualEprom = p.Cmd;
              MessageOk("Eprom is set to "+p.Type);
              //log.info("Index="+jListEpromType.getSelectedIndex()+ " p"+p.Cmd);
              break;
            }
        }

       mTable.deleteData();
       sTable.deleteData();
       mTable.deleteCheckSum();
       sTable.deleteCheckSum();
       mTable.deleteFileName();
       sTable.deleteFileName();
       slave.initProm(EpromMap.get(actualEprom).Size);
       master.initProm(EpromMap.get(actualEprom).Size);
       mTable.showPromTable(mTable, master);
       sTable.showPromTable(sTable, slave);
       mTable.setDataColor(new Color(0,0,0));
       sTable.setDataColor(new Color(0,0,0));
    }//GEN-LAST:event_setEpromType
 
    private void jQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jQuitActionPerformed
      java.awt.event.WindowEvent noevent = null;
      exitForm(noevent);
    }//GEN-LAST:event_jQuitActionPerformed

    private void slaveLoadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveLoadFileActionPerformed
        loadFile(slave,sTable);
    }//GEN-LAST:event_slaveLoadFileActionPerformed

    private void slaveBlankTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveBlankTestActionPerformed
       MessageOk("Start Slave BlankTest...");
       log.info("Start Slave blank test");
       blankTest(slave,sTable,false);
    }//GEN-LAST:event_slaveBlankTestActionPerformed

    private void masterBlankTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterBlankTestActionPerformed
        MessageOk("Start BlankTest...");
        log.info("Start Master blank test");
        blankTest(master,mTable,true);
    }//GEN-LAST:event_masterBlankTestActionPerformed

    private void slaveReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveReadActionPerformed
        readEprom(slave,sTable,false);
    }//GEN-LAST:event_slaveReadActionPerformed

    private void jbCancelActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionActionPerformed
        worker.cancel(true);
        eprog.setIsCancel(true);
        worker.cancel(true);
        eprog.writeString("I");
        eprog.writeString("I");
        eprog.writeString("S");
        eprog.writeString("I");
       
    }//GEN-LAST:event_jbCancelActionActionPerformed

    private void showAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAboutActionPerformed
        if (aboutBox == null) {
         aboutBox = new About(new JFrame(),true);
         aboutBox.setLocationRelativeTo(this.rootPane);
        }
        aboutBox.setVisible(true);
    }//GEN-LAST:event_showAboutActionPerformed

    private void slaveSaveToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveSaveToFileActionPerformed
        saveFile(slave,sTable);
    }//GEN-LAST:event_slaveSaveToFileActionPerformed

    private void masterSavetoFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterSavetoFileActionPerformed
         saveFile(master,mTable);
    }//GEN-LAST:event_masterSavetoFileActionPerformed

    private void masterReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterReadActionPerformed
        readEprom(master,mTable,true);
    }//GEN-LAST:event_masterReadActionPerformed

    private void masterLoadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterLoadFileActionPerformed
         loadFile(master,mTable);
    }//GEN-LAST:event_masterLoadFileActionPerformed

    private void masterChkSumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterChkSumActionPerformed
        // mTable.setCheckSum("0x0000");
        readCheckSum(master,mTable,true);
    }//GEN-LAST:event_masterChkSumActionPerformed

    private void masterComparewithSlaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterComparewithSlaveActionPerformed
        //slave.chdAdress(0, 22);
        //slave.chdAdress(1, 22);
        //slave.chdAdress(8191, 22);
        //sTable.deleteData();
        //sTable.showPromTable(sTable, slave);
        mTable.setDataColor(Color.black);
        sTable.setDiffMask(master,slave);
        JOptionPane.showMessageDialog(null,"The content "+(sTable.getIsDiff() ? "is not equal!" : "is equal"));
    }//GEN-LAST:event_masterComparewithSlaveActionPerformed

    private void slaveChkSumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveChkSumActionPerformed
        readCheckSum(slave,sTable,false);
    }//GEN-LAST:event_slaveChkSumActionPerformed

    private void slaveCompareMasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slaveCompareMasterActionPerformed
        sTable.setDataColor(Color.black);
        mTable.setDiffMask(slave,master);
        JOptionPane.showMessageDialog(null,"The content "+(mTable.getIsDiff() ? "is not equal!" : "is equal"));
    }//GEN-LAST:event_slaveCompareMasterActionPerformed

    private void jHexToBinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHexToBinActionPerformed
        hexToBin();
    }//GEN-LAST:event_jHexToBinActionPerformed

    private void jBinToHexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBinToHexActionPerformed
        binToHex();
    }//GEN-LAST:event_jBinToHexActionPerformed

    private void progMasterStdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progMasterStdActionPerformed
        program(master,"0");
    }//GEN-LAST:event_progMasterStdActionPerformed

    private void progMasterIntelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progMasterIntelActionPerformed
        program(master,"1");
    }//GEN-LAST:event_progMasterIntelActionPerformed

    private void progMasterFujitsuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progMasterFujitsuActionPerformed
        program(master,"2");
    }//GEN-LAST:event_progMasterFujitsuActionPerformed

    private void progMasterAMDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progMasterAMDActionPerformed
        program(master,"3");
    }//GEN-LAST:event_progMasterAMDActionPerformed

    private void progSlaveStdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progSlaveStdActionPerformed
        program(slave,"0");
    }//GEN-LAST:event_progSlaveStdActionPerformed

    private void progSlaveIntelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progSlaveIntelActionPerformed
        program(slave,"1");
    }//GEN-LAST:event_progSlaveIntelActionPerformed

    private void progSlaveFujitsuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progSlaveFujitsuActionPerformed
        program(slave,"2");
    }//GEN-LAST:event_progSlaveFujitsuActionPerformed

    private void progSlaveAMDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progSlaveAMDActionPerformed
        program(slave,"3");
    }//GEN-LAST:event_progSlaveAMDActionPerformed

    /* Main */
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        Locale.setDefault(Locale.ENGLISH);
        System.out.println("Device is set to 9600 8N1 !");
        CommandLine cmd=null;
        Options options = new Options();
        options.addOption( "h", false, "Show help" );
        options.addOption( "d", false, "Debug on" );
        options.addOption( "l", true, "Loglevel 0-5" );
        options.addOption( "p", true, "Port eg. /dev/ttyS0 or COM1" );

        CommandLineParser parser = new PosixParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
             System.out.println("Exeption="+ex);
        }

        if(cmd.hasOption("h")) {
           HelpFormatter formatter = new HelpFormatter();
           formatter.printHelp( "Eprog", options );
           System.exit(0);
        }

        if(cmd.hasOption("d")) {
           Debug = true;
        }

        if(cmd.hasOption("l")) {
           LogLevel = cmd.getOptionValue("l");
        }

        if(cmd.hasOption("p")) {
           EprogPort = cmd.getOptionValue("p");
        }
     
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Eprog().setVisible(true);
                } catch (UnsupportedEncodingException ex) {
                    log.info("Exeption"+ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu About;
    private javax.swing.JTextArea LogScreen;
    private javax.swing.JTextField Message;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JMenuItem jBinToHex;
    private javax.swing.JMenuItem jHexToBin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox jListEpromType;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem jQuit;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JButton jbCancelAction;
    private javax.swing.JMenuItem masterBlankTest;
    private javax.swing.JMenuItem masterChkSum;
    private javax.swing.JMenuItem masterComparewithSlave;
    private javax.swing.JMenuItem masterLoadFile;
    private javax.swing.JMenu masterMenue;
    private javax.swing.JMenuItem masterRead;
    private javax.swing.JMenuItem masterSavetoFile;
    private javax.swing.JProgressBar progBar;
    private javax.swing.JRadioButtonMenuItem progMasterAMD;
    private javax.swing.JRadioButtonMenuItem progMasterFujitsu;
    private javax.swing.JRadioButtonMenuItem progMasterIntel;
    private javax.swing.JRadioButtonMenuItem progMasterStd;
    private javax.swing.JRadioButtonMenuItem progSlaveAMD;
    private javax.swing.JRadioButtonMenuItem progSlaveFujitsu;
    private javax.swing.JRadioButtonMenuItem progSlaveIntel;
    private javax.swing.JRadioButtonMenuItem progSlaveStd;
    private javax.swing.JScrollPane scrPaneMaster;
    private javax.swing.JScrollPane scrPaneSlave;
    private javax.swing.JMenuItem showAbout;
    private javax.swing.JMenuItem slaveBlankTest;
    private javax.swing.JMenuItem slaveChkSum;
    private javax.swing.JMenuItem slaveCompareMaster;
    private javax.swing.JMenuItem slaveLoadFile;
    private javax.swing.JMenu slaveMenue;
    private javax.swing.JMenuItem slaveRead;
    private javax.swing.JMenuItem slaveSaveToFile;
    // End of variables declaration//GEN-END:variables



    
}
