/*
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: Logging.java,v 1.3 2010/05/13 11:56:02 cvs Exp $
 Log  : $Log: Logging.java,v $
 Log  : Revision 1.3  2010/05/13 11:56:02  cvs
 Log  : Images hinzu
 Log  :
 Log  : Revision 1.6  2010/04/15 16:05:11  cvs
 Log  : Stand 15.04.2010
 Log  :
 Log  : Revision 1.5  2010/04/13 14:33:54  cvs
 Log  : Stand 13.04.2010
 Log  :
 Log  : Revision 1.4  2010/04/09 09:54:37  cvs
 Log  : Stand 09.04.2010
 Log  :
 Log  : Revision 1.3  2010/04/08 15:18:46  cvs
 Log  : Stand 08.04.2010
 Log  :
 Log  : Revision 1.2  2010/04/07 10:08:40  cvs
 Log  : Stand 06.04.2010
 Log  :
 Log  : Revision 1.1  2010/04/06 08:55:38  cvs
 Log  : Stand 06.04.2010
 Log  :
 Log  : Revision 1.2  2010/04/03 14:38:42  cvs
 Log  : Stand 03.04.2010
 Log  :
 Log  : Revision 1.1  2010/04/02 16:44:16  cvs
 Log  : Include Log to Screen
 Log  :
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 *
 */

package com.utils;

import java.util.*;
import java.text.*;
import java.io.*;
import javax.swing.JTextArea;


/**
 *
 * @author monitor
 */
public class Logging {
    
        private int logsize          = 1000000;
        private boolean debug        = false;
        private boolean log2screen   = false;
        private boolean log2file     = false;
        private int loglevel         = 3; // 0 Keine , 1 err , 2 warn , 3 info , 4 trace ,5 StdOut
	private FileWriter LOG;
	private String logpath       = ".";
	private String logfile       = "logfile.log";
        private JTextArea LogScreen;

        /**
         *
         */
        public Logging() {
	 open();
	 info("Init Logging");
	}

        /**
         *
         * @param logfile
         */
        public Logging(String logfile) {
		 this.logfile = logfile;
		 open();
		 if(debug) { info("Using Logfile "+logfile+" with Size "+logsize); }
	}

        /**
         *
         * @param logfile
         * @param logsize
         */
        public Logging(String logfile,int logsize) {
		 this.logfile = logfile;
		 this.logsize = logsize;
		 open();
		 if(debug) { info("Using Logfile "+logfile+" with Size "+logsize);}
	}

        /**
         * 
         * @param logfile
         * @param logsize
         * @param debug
         */
        public Logging(String logfile,int logsize,boolean debug) {
		 this.logfile = logfile;
		 this.logsize = logsize;
                 this.debug   = debug;
		 open();
		 if(debug) { info("Using Logfile "+logfile+" with Size "+logsize+" Debug:="+debug); }
	}

    /**
     * 
     * @param logfile
     * @param logsize
     * @param debug
     * @param loglevel
     */
    public Logging(String logfile,int logsize,boolean debug,int loglevel) {
		 this.logfile  = logfile;
		 this.logsize  = logsize;
                 this.debug    = debug;
                 this.loglevel = loglevel;
		 open();
		 if(debug) { info("Using Logfile "+logfile+" with Size "+logsize+" Debug:="+debug+" LogLevel="+loglevel); }
	}

    public Logging(JTextArea LogScreen) {
        this.LogScreen = LogScreen;
        this.log2screen = true;
    }

  
    /* Public */

    /**
     *
     * @param LogLevel
     */
    public void setLogLevel(int LogLevel) {
       this.loglevel = LogLevel;
       info("Loglevel set to "+this.loglevel);
    }

    /**
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
       this.debug = debug;
       info("Debug set to "+this.debug);
    }


    /**
     *
     * @return
     */
    private String getDate() {
            SimpleDateFormat formatter = new SimpleDateFormat ("dd.MM.yyyy' 'HH:mm:ss ");
	    Date currentTime = new Date();
            return(formatter.format(currentTime));
    }
    
    /* ErrorLog */
    /**
     *
     * @param Message
     */
    public void error(String Message) {
       String tmp = getDate()+" [ERROR] : "+Message;
       if(loglevel>0)  {
		if(loglevel>4)  { System.out.println(tmp); }
		try {
                    if(log2file) {
		     LOG.write(tmp+"\n");
		     LOG.flush();
                    }
		} catch (IOException e) {
			 System.out.println("Error: write to"+this.logfile);
		 }
          if(log2screen) { LogScreen.insert(tmp+"\n",0); }
       }
     }

    public void debug(String Message) {
       String tmp = getDate()+" [DEBUG] : "+Message;
       if(debug)  {
		if(loglevel>4)  { System.out.println(tmp); }
		try {
                    if(log2file) {
		     LOG.write(tmp+"\n");
		     LOG.flush();
                    }
		} catch (IOException e) {
			 System.out.println("Error: write to"+this.logfile);
		 }
          if(log2screen) { LogScreen.insert(tmp+"\n",0); }
       }
     }

    /* ErrorLog */
    /**
     *
     * @param Message
     */
    public void warn(String Message) {
       String tmp = getDate()+"  [WARN] : "+Message;
       if(loglevel>1)  {
		if(loglevel>4)  { System.out.println(tmp); }
		try {
                    if(log2file) {
		     LOG.write(tmp+"\n");
		     LOG.flush();
                    }
		} catch (IOException e) {
			 System.out.println("Error: write to"+this.logfile);
		 }
           if(log2screen) { LogScreen.insert(tmp+"\n",0); }
       }
     }

    /**
     *
     * @param Message
     */
    public void info(String Message) {
       String tmp = getDate()+" [INFO]    : "+Message;
       if(loglevel>2)  {
		if(loglevel>4)  { System.out.println(tmp); }
		try {
                    if(log2file) {
		     LOG.write(tmp+"\n");
		     LOG.flush();
                    }
		} catch (IOException e) {
			 System.out.println("Error: write to"+this.logfile);
		 }
           if(log2screen) { LogScreen.insert(tmp+"\n",0); }
       }
     }

    /**
     *
     * @param Message
     */
    public void trace(String Message) {
       String tmp = getDate()+" [TRACE] : "+Message;
       if(loglevel>3)  {
		System.out.println(tmp);
		try {
                    if(log2file) {
		     LOG.write(tmp+"\n");
		     LOG.flush();
                    }
		} catch (IOException e) {
			 System.out.println("Error: write to"+this.logfile);
		 }
        if(log2screen) { LogScreen.insert(tmp+"\n",0); }
       }
     }


    private void open() {
	// File exist, check size and rename
	 File file = new File(this.logfile);
	 File oldfile = new File(this.logfile+".old");
	 // System.out.println(Logging.class.getName());
	 if(file.exists()) {
 	  if(file.length()>this.logsize) {
	   file.renameTo(oldfile);
	  }
	 }

 	 try {
             LOG = new FileWriter(this.logfile,true);
             this.log2file = true;
         } catch (IOException e) {
	     System.out.println("Fehler beim Erstellen der Datei");
	 }

	 // Error to LogFile too
         setSTDERR2Log();
         
      }

    /**
     * Setzt STDERR auf das LogFile
     */
    public void setSTDERR2Log() {
     	 try {
 	     System.setErr(new PrintStream(new FileOutputStream(this.logfile,true)));
	 } catch (FileNotFoundException e) {
	    e.printStackTrace();
	 }
    }

    /**
     * Setzt STDERR auf das LogFile
     */
    public void setSTDERR2Stderr() {
      System.setErr(System.err);
    }


    /**
     * 
     */
    public void close() {
     try {
      info("Log closed");
      LOG.flush();
      LOG.close();
      this.log2file=false;
      this.log2screen=false;
    } catch (IOException e) {
       System.out.println("Fehler beim Schliessen der Datei");
    }
   }

}
