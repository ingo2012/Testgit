/*
 * EprogTTy.java the Serial-Class for Eprog
 * http://www.java-forum.org/allgemeine-java-themen/102161-usb-i2c-adapter-java-problem.html
 * @author egerlai
 * Created on 22.06.2010, 22:10:01
 *

---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 Info : $Id: EprogTTy.java,v 1.5 2010/05/23 12:09:39 cvs Exp $
 Log  : $Log: EprogTTy.java,v $
 Log  : Revision 1.5  2010/05/23 12:09:39  cvs
 Log  : Stand 23.05.10
 Log  :
 Log  : Stand 20.05.10
 
---------------------------------------------------------------------------------
-----------------------       C V S - I n f o    --------------------------------
---------------------------------------------------------------------------------
 * 
 *
 *
 */

package com.utils;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import javax.swing.JProgressBar;


public class EprogTTy implements Runnable, SerialPortEventListener {

    private Logging log;

    Thread readThread;
    public OutputStream outputStream;
    public InputStream inputStream;
    private JProgressBar progBar;
    private String Port                    = "/dev/ttyUSB1";
    public SerialPort serialPort           = null;
    public String readStatus               = "";
    public boolean ttyFound                = false;
    private boolean isCancel               = false;
    public boolean isReadState             = false;
    public boolean isCTSprogBar            = false;
    private int progBarVal                 = 0;

    /**
     *
     * @param EprogPort
     * @param progBar
     * @param log
     */
    public EprogTTy(String EprogPort,JProgressBar progBar,Logging log) {
        this.Port    = EprogPort;
        this.log     = log;
        this.progBar = progBar;
        initEprog(Port);
    }

     /**
     * Initialize the Port
     * @param tty The Port
     */
    private void initEprog(String tty) {
       CommPortIdentifier portId = null;

        try {
            portId = CommPortIdentifier.getPortIdentifier(tty);
            log.info("Opening port " + portId.getName());
            try {
                serialPort = (SerialPort) portId.open("EprogTTy", 2000);
            } catch (PortInUseException e) {
                log.error("Port in use.");
            }

            try {
                outputStream = serialPort.getOutputStream();
            } catch (IOException e) {
                log.error("IOExeption " + e);
            }

            try {
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1 , SerialPort.PARITY_NONE );
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
            } catch (gnu.io.UnsupportedCommOperationException ex) {
                log.error("UnsupportedCommOperationException :" + ex);
            }

            try {
                serialPort.notifyOnOutputEmpty(false);  // muss bei javaxcom auf true
                serialPort.notifyOnCTS(true);
                serialPort.notifyOnDataAvailable(true);
                serialPort.notifyOnParityError(true);
                serialPort.notifyOnDSR(true);
            } catch (Exception e) {
                System.out.println("Error setting event notification"+e);
            }

            try {
                inputStream = serialPort.getInputStream();
            } catch (IOException e) {
                log.error("IOExeption " + e);
            }

            try {
                serialPort.addEventListener((SerialPortEventListener) this);
            } catch (TooManyListenersException e) {
                log.error("TooManyListenersException " + e);
            }

            serialPort.notifyOnDataAvailable(true);
            readThread = new Thread(this);
            readThread.start();
            ttyFound = true;

        } catch (NoSuchPortException ex) {
            log.error("Port " + tty + " not found !");
        }
    }

    public void run() {
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {}
    }

    /**
     * SerialEvent
     * @param event
     */
    public void serialEvent(SerialPortEvent event) {
        int numBytes = 0;
        String result = "";

        if (isCancel == false) {

            switch (event.getEventType()) {

                case SerialPortEvent.BI:
                    log.debug("BI Event");
                    break;

                case SerialPortEvent.OE:
                    log.debug("OE Event");
                    break;

                case SerialPortEvent.FE:
                    log.debug("FE Event");
                    break;

                case SerialPortEvent.PE:
                    log.debug("PE Event");
                    break;

                case SerialPortEvent.CD:
                    log.debug("CD Event");
                    break;

                case SerialPortEvent.CTS:
                    if(isCTSprogBar) {
                     progBarVal = progBar.getValue();
                     progBarVal = progBarVal +8;
                     if(progBarVal>100) { progBarVal =100;}
                     progBar.setValue(progBarVal);
                    }
                    log.debug("CTS Event");
                    //log.debug("lenkplStr="+kplStr.length());
                    break;

                case SerialPortEvent.DSR:
                    log.debug("DSR Event");
                    break;

                case SerialPortEvent.RI:
                    log.debug("RI Event");
                    break;

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    log.debug("OutputBufferEmpty Event");
                    break;

                case SerialPortEvent.DATA_AVAILABLE:
                    log.debug("DATA_AVAILABLE Event");
                    byte[] readBuffer = new byte[60];

                    try {

                        while (inputStream.available() > 0) {
                            numBytes = inputStream.read(readBuffer);
                        }

                        result = new String(readBuffer,0,numBytes);
                        log.debug("NumBytes=" + numBytes + " result=" + result);

                        if(isReadState && (numBytes>0)) {
                          //Pattern pattern = Pattern.compile( "\\s+" );
                          //Matcher matcher = pattern.matcher( result );
                          //readStatus = matcher.replaceAll( "" );
                          //readStatus = new String(readBuffer,0,1);
                          // result.replaceAll("\n", "\r");
                          //result.replaceAll("[_[^\\w]]", "");
                          // readStatus = result;
                          for(int i=0;i<numBytes+1;i++) {
                            log.debug("i="+i+" Buf"+readBuffer[i]);
                            if(readBuffer[i]==48 || readBuffer[i]==49) {
                                readStatus= new String(readBuffer,i,1);
                                break;
                            }
                          }
                          log.debug("readStatus=" + readStatus);
                          isReadState = false; // we read only the first byte !
                        }


                    } catch (IOException e) {
                    }

                    break;
            }
        }
    }

    /**
     * Write String to the Eprommer
     * @param s
     */
    public void writeString(String s) {
        try {
              log.debug("writeString="+s);
              if(serialPort.isCTS()) outputStream.write(s.getBytes());
	    } catch (IOException e) {
                log.error("Error writing "+e);
              }
	try {
	      Thread.sleep(500);  // Be sure data is xferred
	    } catch (Exception e) {
               log.error("Exeption "+e);
            }
    }

    public void setIsCancel(boolean b) {
        isCancel = b;
    }

    public boolean getIsCancel() {
        return(isCancel);
    }

    public void setCTSprogBar(boolean b){
        isCTSprogBar = b;
    }


}
