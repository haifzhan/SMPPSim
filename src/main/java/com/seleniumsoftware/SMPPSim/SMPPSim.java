/**
 * **************************************************************************
 * SMPPSim.java
 *
 * Copyright (C) Selenium Software Ltd 2006
 *
 * This file is part of SMPPSim.
 *
 * SMPPSim is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * SMPPSim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SMPPSim; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * @author martin.woolley@seleniumsoftware.com http://www.seleniumsoftware.com
 * http://www.seleniumsoftware.com $Header:
 * /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/SMPPSim.java,v
 * 1.1 2012/07/24 14:48:59 martin Exp $
 * ***************************************************************************
 *
 * This server simulates the basic behaviour of a Logical Aldiscom SMSC with
 * respect to SMPP based interaction between an application and the SMSC. Its
 * intended purpose is to allow basic testing of an SMPP application. It is not
 * an SMSC. It is not a full and comprehensive implementation. With this
 * utility, sufficient testing, to be confident that the main message types are
 * implemented correctly can take place without the need for access to a real
 * SMSC. Once testing with this simulator is completed, final testing should
 * take place using such an SMSC.
 *
 *
 * ================================================================================================================
 *
 * V1.00 16/06/2001
 */
package com.seleniumsoftware.SMPPSim;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.seleniumsoftware.SMPPSim.pdu.*;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;
import com.seleniumsoftware.SMPPSim.util.Utilities;

import java.io.*;
import java.net.*;
import java.util.*;
import org.slf4j.Logger;
//import java.util.logging.*;
import org.slf4j.LoggerFactory;

public class SMPPSim
{
    private static final String version = "2.6.9";
    // HTTP parameter names
    private static final String MESSAGEPARAM = "message";
    private static final String SOURCEMSISDNPARAM = "mo_msisdn";
    private static final String DESTMSISDNPARAM = "mt_msisdn";
    private static byte[] http200Response;
    private static final String http200Message = "HTTP/1.1 200\r\n\r\nDELIVER_SM invoked OK";
    private static byte[] http400Response;
    private static final String http400Message = "HTTP/1.1 400\r\n\r\n";
    // Misc constants and variables
    private static boolean loopback = false;
    private static boolean esme_to_esme = false;
    private static int boundReceiverCount = 0;
    // Byte Stream Callback
    private static boolean callback = false;
    private static String callback_target_host;
    private static int callback_port;
    private static String callback_id;
    // Queue configuration
    private static int inbound_queue_capacity;
    private static int outbound_queue_capacity;
    private static int delayed_iqueue_period = 60;
    private static int delayed_inbound_queue_max_attempts;
    private static int messageStateCheckFrequency;
    private static int maxTimeEnroute;
    private static int percentageThatTransition;
    private static int percentageDelivered;
    private static int percentageUndeliverable;
    private static int percentageAccepted;
    private static int percentageRejected;
    private static int discardFromQueueAfter;
    private static long delayReceiptsBy;
    // Message ID allocation
    private static long start_at = 0;
    private static String mid_prefix = "";
    // Logging
    //private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");
    private static Logger logger = LoggerFactory.getLogger(SMPPSim.class);
    // Other
    private static int smppPort;
    private static int HTTPPort;
    private static int HTTPThreads;
    private static String docroot;
    private static String authorisedFiles;
    private static String injectMoPage;
    private static int maxConnectionHandlers;
    private static String smscid;
    private static String[] systemids;
    private static String[] passwords;
    private static boolean outbind_enabled;
    private static String esme_ip_address;
    private static int esme_port;
    private static String esme_systemid;
    private static String esme_password;
    private static Socket receivesocket;
    private static String rcv_address;
    private static String deliverFile;
    private static int deliverMessagesPerMin = 0;
    private static String connectionHandlerClassName;
    private static String protocolHandlerClassName;
    private static String lifeCycleManagerClassName;
    // USSD
    private static boolean deliver_sm_includes_ussd_service_op = false;
    private static int[] messageTypes =
    {
        PduConstants.BIND_RECEIVER, PduConstants.BIND_RECEIVER_RESP, PduConstants.BIND_TRANSMITTER,
        PduConstants.BIND_TRANSMITTER_RESP, PduConstants.BIND_TRANSCEIVER, PduConstants.BIND_TRANSCEIVER_RESP, PduConstants.OUTBIND, PduConstants.UNBIND,
        PduConstants.UNBIND_RESP, PduConstants.SUBMIT_SM, PduConstants.SUBMIT_SM_RESP, PduConstants.DELIVER_SM, PduConstants.DELIVER_SM_RESP,
        PduConstants.QUERY_SM, PduConstants.QUERY_SM_RESP, PduConstants.CANCEL_SM, PduConstants.CANCEL_SM_RESP, PduConstants.REPLACE_SM,
        PduConstants.REPLACE_SM_RESP, PduConstants.ENQUIRE_LINK, PduConstants.ENQUIRE_LINK_RESP, PduConstants.SUBMIT_MULTI, PduConstants.SUBMIT_MULTI_RESP,
        PduConstants.GENERIC_NAK
    };
    // PDU Capture
    private static boolean captureSmeBinary;
    private static String captureSmeBinaryToFile;
    private static boolean captureSmppsimBinary;
    private static String captureSmppsimBinaryToFile;
    private static boolean captureSmeDecoded;
    private static String captureSmeDecodedToFile;
    private static boolean captureSmppsimDecoded;
    private static String captureSmppsimDecodedToFile;
    private static boolean dlr_tlr_required;
    private static boolean dlr_opt_tlv_required;
    private static short dlr_tlv_tag;
    private static short dlr_tlv_len;
    private static byte[] dlr_tlv_value;
    
    private static final int DELAY = 5000;
    private static final int PERIOD = 5;
    private static final int MINUTE = 60*1000;
    
    

    public static void main(String args[]) throws Exception
    {
        System.out.println("SMPPSim is starting....");
        if((args == null) || (args.length != 2))
        {
            showUsage();
            return;
        }
        
        //for logging counters every X minutes
//        Timer timer = new Timer();
//        timer.schedule(new Counter(), DELAY, PERIOD*MINUTE);
//        timer.schedule(new Counter(), DELAY, 3000);
        
        
        
        

        //Load logback.xml file
        configure(args[0]);

        Properties props = new Properties();
        // load the given properties : smppsim.props
        InputStream is = new FileInputStream(args[1]);
        props.load(is);
        initialise(props);


        showLegals();
        showConfiguration();


        SMPPSim SMPPSim = new SMPPSim();
        try
        {
            Smsc smsc = Smsc.getInstance();
            smsc.setInbound_queue_capacity(inbound_queue_capacity);
            smsc.setOutbound_queue_capacity(outbound_queue_capacity);
            smsc.start();
        }
        catch(Exception e)
        {
            logger.error("Exception during start up " + e.getMessage());
            logger.error("Exception is of type: " + e.getClass().getName());
        }
    }

    //configure SLF4J logback file
    public static void configure(String path)
    {
        try
        {
            JoranConfigurator configurator = new JoranConfigurator();
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            configurator.setContext(loggerContext);
            loggerContext.reset();
            configurator.doConfigure(path);
        }
        catch(JoranException e)
        {
            System.err.println("logback.xml file failed loading...");
        }
    }

//    private static void showUsage()
//    {
//        System.out.println("Invalid or missing arguments:");
//        System.out.println("Usage:");
//        System.out.println("java -Djava.util.logging.config.file=<logging.properties file> com/seleniumsoftware/SMPPSim/SMPPSim <properties file>");
//        System.out.println("");
//        System.out.println("Example:");
//        System.out.println("java -Djava.util.logging.config.file=conf\\logging.properties com/seleniumsoftware/SMPPSim/SMPPSim conf\\props.win");
//        System.out.println("");
//        System.out.println("Run terminated");
//    }
    private static void showUsage()
    {
        System.out.println("Invalide or missing arguments");
        System.out.println("There are 2 arguments:  logback.xml file path and smppsim.props");
        System.out.println("java -jar smppsim.jar conf/logback.xml conf/smppsim.props");
    }

    private static void initialise(Properties props) throws Exception
    {
        http200Response = http200Message.getBytes();
        http400Response = http400Message.getBytes();

        maxConnectionHandlers = Integer.parseInt(props.getProperty("SMPP_CONNECTION_HANDLERS"));
        smppPort = Integer.parseInt(props.getProperty("SMPP_PORT"));
        connectionHandlerClassName = props.getProperty("CONNECTION_HANDLER_CLASS");
        protocolHandlerClassName = props.getProperty("PROTOCOL_HANDLER_CLASS");
        lifeCycleManagerClassName = props.getProperty("LIFE_CYCLE_MANAGER");
        String systemid_list = props.getProperty("SYSTEM_IDS", "");
        String password_list = props.getProperty("PASSWORDS", "");
        systemids = systemid_list.split(",");
        passwords = password_list.split(",");
        if(systemids.length != passwords.length)
        {
            logger.error("Number of SYSTEM_IDS elements is not the same as the number of PASSWORDS elements");
            throw new Exception("Number of SYSTEM_IDS elements is not the same as the number of PASSWORDS elements");
        }
        outbind_enabled = Boolean.valueOf(props.getProperty("OUTBIND_ENABLED")).booleanValue();
        if(outbind_enabled)
        {
            esme_ip_address = props.getProperty("OUTBIND_ESME_IP_ADDRESS", "127.0.0.1");
            String ep = props.getProperty("OUTBIND_ESME_PORT");
            try
            {
                esme_port = Integer.parseInt(ep);
            }
            catch(NumberFormatException nfe)
            {
                logger.error("ESME_PORT has invalid value " + ep + " - defaulting to 2776");
                esme_port = 2776;
            }
        }
        esme_systemid = props.getProperty("OUTBIND_ESME_SYSTEMID", "smppclient1");
        esme_password = props.getProperty("OUTBIND_ESME_PASSWORD", "password");
        HTTPPort = Integer.parseInt(props.getProperty("HTTP_PORT"));
        HTTPThreads = Integer.parseInt(props.getProperty("HTTP_THREADS"));
        docroot = props.getProperty("DOCROOT");
        authorisedFiles = props.getProperty("AUTHORISED_FILES");
        injectMoPage = props.getProperty("INJECT_MO_PAGE");
        smscid = props.getProperty("SMSCID");
        deliverMessagesPerMin = Integer.parseInt(props.getProperty("DELIVERY_MESSAGES_PER_MINUTE"));
        if(deliverMessagesPerMin > 0)
        {
            deliverFile = props.getProperty("DELIVER_MESSAGES_FILE");
        }
        else
        {
            deliverFile = "N/A";
        }

        Smsc smsc = Smsc.getInstance();
        smsc.setDecodePdus(Boolean.valueOf(props.getProperty("DECODE_PDUS_IN_LOG")).booleanValue());

        setLoopback(Boolean.valueOf(props.getProperty("LOOPBACK")).booleanValue());

        setEsme_to_esme(Boolean.valueOf(props.getProperty("ESME_TO_ESME")).booleanValue());

        if(isLoopback() && isEsme_to_esme())
        {
            logger.error("It is not valid to enable both LOOPBACK and ESME_TO_ESME routing. Please deselect one or both of these options");
            throw new Exception();
        }

        inbound_queue_capacity = getIntProperty(props, "INBOUND_QUEUE_MAX_SIZE", 1000);
        outbound_queue_capacity = getIntProperty(props, "OUTBOUND_QUEUE_MAX_SIZE", 1000);
        messageStateCheckFrequency = getIntProperty(props, "MESSAGE_STATE_CHECK_FREQUENCY", 10000);
        maxTimeEnroute = getIntProperty(props, "MAX_TIME_ENROUTE", 2000);
        delayReceiptsBy = getLongProperty(props, "DELAY_DELIVERY_RECEIPTS_BY", 0);
        percentageThatTransition = getIntProperty(props, "PERCENTAGE_THAT_TRANSITION", 75);
        percentageDelivered = getIntProperty(props, "PERCENTAGE_DELIVERED", 90);
        percentageUndeliverable = getIntProperty(props, "PERCENTAGE_UNDELIVERABLE", 6);
        percentageAccepted = getIntProperty(props, "PERCENTAGE_ACCEPTED", 2);
        percentageRejected = getIntProperty(props, "PERCENTAGE_REJECTED", 2);
        discardFromQueueAfter = getIntProperty(props, "DISCARD_FROM_QUEUE_AFTER", 60000);
        delayed_iqueue_period = 1000 * getIntProperty(props, "DELAYED_INBOUND_QUEUE_PROCESSING_PERIOD", 60);
        delayed_inbound_queue_max_attempts = getIntProperty(props, "DELAYED_INBOUND_QUEUE_MAX_ATTEMPTS", 10);

        setCaptureSmeBinary(Boolean.valueOf(props.getProperty("CAPTURE_SME_BINARY")).booleanValue());
        setCaptureSmeBinaryToFile(props.getProperty("CAPTURE_SME_BINARY_TO_FILE"));
        setCaptureSmppsimBinary(Boolean.valueOf(props.getProperty("CAPTURE_SMPPSIM_BINARY")).booleanValue());
        setCaptureSmppsimBinaryToFile(props.getProperty("CAPTURE_SMPPSIM_BINARY_TO_FILE"));
        setCaptureSmeDecoded(Boolean.valueOf(props.getProperty("CAPTURE_SME_DECODED")).booleanValue());
        setCaptureSmeDecodedToFile(props.getProperty("CAPTURE_SME_DECODED_TO_FILE"));
        setCaptureSmppsimDecoded(Boolean.valueOf(props.getProperty("CAPTURE_SMPPSIM_DECODED")).booleanValue());
        setCaptureSmppsimDecodedToFile(props.getProperty("CAPTURE_SMPPSIM_DECODED_TO_FILE"));

        // Byte stream callbacks
        callback = Boolean.valueOf(props.getProperty("CALLBACK")).booleanValue();
        if(callback)
        {
            callback_target_host = props.getProperty("CALLBACK_TARGET_HOST");
            callback_port = Integer.parseInt(props.getProperty("CALLBACK_PORT"));
            callback_id = props.getProperty("CALLBACK_ID");
        }

        // Message ID allocations

        String mid_start = props.getProperty("START_MESSAGE_ID_AT");
        if(mid_start == null || mid_start.equals(""))
        {
            mid_start = "0";
        }
        if(mid_start.equalsIgnoreCase("random"))
        {
            start_at = (long) (Math.random() * 10000000);
        }
        else
        {
            start_at = Long.parseLong(mid_start);
        }

        mid_prefix = props.getProperty("MESSAGE_ID_PREFIX");
        if(mid_prefix == null)
        {
            mid_prefix = "";
        }

        // Misc
        byte[] s = new byte[smscid.length()];
        s = smscid.getBytes();
        smsc.setSMSC_SYSTEMID(s);

        // delivery receipt misc TLV

        String dlr_tlv = props.getProperty("DELIVERY_RECEIPT_TLV");
        if(dlr_tlv == null || dlr_tlv.equals(""))
        {
            dlr_tlr_required = false;
        }
        else
        {
            String tlv_parts[] = dlr_tlv.split("/");
            if(tlv_parts.length != 3)
            {
                logger.error("DELIVERY_RECEIPT_TLV property is in invalid format");
                throw new Exception("DELIVERY_RECEIPT_TLV property is in invalid format");
            }
            else
            {
                String tag_hex = tlv_parts[0];
                String len_hex = tlv_parts[1];
                String value_hex = tlv_parts[2];
                byte[] dlr_tlv_tag_bytes = Utilities.getByteArrayFromHexString(tag_hex);
                dlr_tlv_tag = Utilities.getShortValue(dlr_tlv_tag_bytes, 0, dlr_tlv_tag_bytes.length);
                byte[] dlr_tag_len_bytes = Utilities.getByteArrayFromHexString(len_hex);
                dlr_tlv_len = Utilities.getShortValue(dlr_tag_len_bytes, 0, dlr_tag_len_bytes.length);
                dlr_tlv_value = Utilities.getByteArrayFromHexString(value_hex);
                dlr_tlr_required = true;
            }
        }

        // delivery receipt V3.4 optional TLVs

        String dlr_opt_tlv = props.getProperty("DELIVERY_RECEIPT_OPTIONAL_PARAMS");
        if(dlr_opt_tlv == null || dlr_opt_tlv.equals(""))
        {
            dlr_opt_tlv_required = true;
        }
        else
        {
            dlr_opt_tlv_required = Boolean.parseBoolean(dlr_opt_tlv);
        }

        // USSD

        deliver_sm_includes_ussd_service_op = Boolean.valueOf(props.getProperty("DELIVER_SM_INCLUDES_USSD_SERVICE_OP")).booleanValue();

    }

    private static void showLegals()
    {
        logger.info("==============================================================");
        logger.info("=  SMPPSim Copyright (C) 2006 Selenium Software Ltd");
        logger.info("=  SMPPSim comes with ABSOLUTELY NO WARRANTY; for details");
        logger.info("=  read the license.txt file that was included in the SMPPSim distribution");
        logger.info("=  This is free software, and you are welcome to redistribute it under");
        logger.info("=  certain conditions; Again, see license.txt for details or read the GNU");
        logger.info("=  GPL license at http://www.gnu.org/licenses/gpl.html");
        logger.info("=  ...... end of legal stuff ......");

    }

    private static void showConfiguration()
    {
        logger.info("==============================================================");
        logger.info("=  ");
        logger.info("=  com.seleniumsoftware.SMPPSim " + version);
        logger.info("=  by Martin Woolley (martin@seleniumsoftware.com)");
        logger.info("=  http://www.seleniumsoftware.com");
        logger.info("=  Running with the following parameters:");
        logger.info("=  SMPP_PORT                               :" + smppPort);
        logger.info("=  SMPP_CONNECTION_HANDLERS                :" + maxConnectionHandlers);
        logger.info("=  CONNECTION_HANDLER_CLASS                :" + connectionHandlerClassName);
        logger.info("=  PROTOCOL_HANDLER_CLASS                  :" + protocolHandlerClassName);
        logger.info("=  LIFE_CYCLE_MANAGER                      :" + lifeCycleManagerClassName);
        logger.info("=  SMPP_CONNECTION_HANDLERS                :" + maxConnectionHandlers);
        logger.info("=  INBOUND_QUEUE_CAPACITY                  :" + inbound_queue_capacity);
        logger.info("=  OUTBOUND_QUEUE_CAPACITY                 :" + outbound_queue_capacity);
        logger.info("=  MESSAGE_STATE_CHECK_FREQUENCY           :" + messageStateCheckFrequency);
        logger.info("=  MAX_TIME_ENROUTE                        :" + maxTimeEnroute);
        logger.info("=  PERCENTAGE_THAT_TRANSITION              :" + percentageThatTransition);
        logger.info("=  PERCENTAGE_DELIVERED                    :" + percentageDelivered);
        logger.info("=  PERCENTAGE_UNDELIVERABLE                :" + percentageUndeliverable);
        logger.info("=  PERCENTAGE_ACCEPTED                     :" + percentageAccepted);
        logger.info("=  PERCENTAGE_REJECTED                     :" + percentageRejected);
        logger.info("=  DISCARD_FROM_QUEUE_AFTER                :" + discardFromQueueAfter);
        logger.info("=  OUTBIND_ENABLED                         :" + outbind_enabled);
        logger.info("=  OUTBIND_ESME_IP_ADDRESS                 :" + esme_ip_address);
        logger.info("=  OUTBIND_ESME_PORT		                :" + esme_port);
        logger.info("=  OUTBIND_ESME_PASSWORD                   :" + esme_password);
        logger.info("=  HTTP_PORT                               :" + HTTPPort);
        logger.info("=  HTTP_THREADS                            :" + HTTPThreads);
        logger.info("=  DOCROOT                                 :" + docroot);
        logger.info("=  AUTHORISED_FILES                        :" + authorisedFiles);
        logger.info("=  INJECT_MO_PAGE                          :" + injectMoPage);
        logger.info("=  SMSCID                                  :" + smscid);
        logger.info("=  DELIVERY_MESSAGES_PER_MINUTE            :" + deliverMessagesPerMin);
        logger.info("=  DELIVER_MESSAGES_FILE                   :" + deliverFile);
        logger.info("=  LOOPBACK                                :" + isLoopback());
        logger.info("=  CAPTURE_REQUESTS_BINARY                 :" + isCaptureSmeBinary());
        logger.info("=  CAPTURE_REQUESTS_BINARY_TO_FILE         :" + captureSmeBinaryToFile);
        logger.info("=  CAPTURE_RESPONSES_BINARY                :" + isCaptureSmppsimBinary());
        logger.info("=  CAPTURE_RESPONSES_BINARY_TO_FILE        :" + captureSmppsimBinaryToFile);
        logger.info("=  CAPTURE_REQUESTS_DECODED                :" + isCaptureSmeDecoded());
        logger.info("=  CAPTURE_REQUESTS_DECODED_TO_FILE        :" + captureSmeDecodedToFile);
        logger.info("=  CAPTURE_RESPONSES_DECODED               :" + isCaptureSmppsimDecoded());
        logger.info("=  CAPTURE_RESPONSES_DECODED_TO_FILE       :" + captureSmppsimDecodedToFile);
        logger.info("=  CALLBACK                                :" + callback);
        if(callback)
        {
            logger.info("=  CALLBACK_TARGET_HOST                    :" + callback_target_host);
            logger.info("=  CALLBACK_PORT                           :" + callback_port);
        }
        if(dlr_tlr_required)
        {
            logger.info("= Delivery receipts will always have optional parameter with TLV=" + dlr_tlv_tag + "/" + dlr_tlv_len + "/"
                    + PduUtilities.byteArrayToHexString(dlr_tlv_value));
        }
        if(dlr_opt_tlv_required)
        {
            logger.info("= Delivery receipts will include standard optional parameters if client supports 3.4 or later");
        }
        logger.info("=  ");
        logger.info("==============================================================");

    }

    public SMPPSim()
    {
    }

    private static int getIntProperty(Properties props, String name, int defaultValue)
    {
        String x = props.getProperty(name);
        int value = defaultValue;
        if(x == null || x.equals(""))
        {
            logger.error(name + " not specified. Defaulting to " + defaultValue);
            return defaultValue;
        }
        else
        {
            try
            {
                value = Integer.parseInt(x);
                return value;
            }
            catch(NumberFormatException e)
            {
                logger.error(name + " has invalid value " + x + ". Defaulting to " + defaultValue);
                return defaultValue;
            }
        }

    }

    private static long getLongProperty(Properties props, String name, long defaultValue)
    {
        String x = props.getProperty(name);
        long value = defaultValue;
        if(x == null || x.equals(""))
        {
            logger.error(name + " not specified. Defaulting to " + defaultValue);
            return defaultValue;
        }
        else
        {
            try
            {
                value = Long.parseLong(x);
                return value;
            }
            catch(NumberFormatException e)
            {
                logger.error(name + " has invalid value " + x + ". Defaulting to " + defaultValue);
                return defaultValue;
            }
        }

    }

    /**
     * @return
     */
    public static String getProtocolHandlerClassName()
    {
        return protocolHandlerClassName;
    }

    /**
     * @return
     */
    public static int getBoundReceiverCount()
    {
        return boundReceiverCount;
    }

    /**
     * @return
     */
    public static String getConnectionHandlerClassName()
    {
        return connectionHandlerClassName;
    }

    /**
     * @return
     */
    public static String getDeliverFile()
    {
        return deliverFile;
    }

    /**
     * @return
     */
    public static int getDeliverMessagesPerMin()
    {
        return deliverMessagesPerMin;
    }

    /**
     * @return
     */
    public static String getDESTMSISDNPARAM()
    {
        return DESTMSISDNPARAM;
    }

    /**
     * @return
     */
    public static String getHttp200Message()
    {
        return http200Message;
    }

    /**
     * @return
     */
    public static byte[] getHttp200Response()
    {
        return http200Response;
    }

    /**
     * @return
     */
    public static String getHttp400Message()
    {
        return http400Message;
    }

    /**
     * @return
     */
    public static byte[] getHttp400Response()
    {
        return http400Response;
    }

    /**
     * @return
     */
    public static int getHTTPPort()
    {
        return HTTPPort;
    }

    /**
     * @return
     */
    public static int getHTTPThreads()
    {
        return HTTPThreads;
    }

    /**
     * @return
     */
    public static boolean isLoopback()
    {
        return loopback;
    }

    /**
     * @return
     */
    public static String getMESSAGEPARAM()
    {
        return MESSAGEPARAM;
    }

    /**
     * @return
     */
    public static String getRcv_address()
    {
        return rcv_address;
    }

    /**
     * @return
     */
    public static Socket getReceivesocket()
    {
        return receivesocket;
    }

    /**
     * @return
     */
    public static int getSmppPort()
    {
        return smppPort;
    }

    /**
     * @return
     */
    public static String getSmscid()
    {
        return smscid;
    }

    /**
     * @return
     */
    public static String getSOURCEMSISDNPARAM()
    {
        return SOURCEMSISDNPARAM;
    }

    /**
     * @return
     */
    public static String getVersion()
    {
        return version;
    }

    /**
     * @return
     */
    public static int[] getMessageTypes()
    {
        return messageTypes;
    }

    /**
     * @param i
     */
    public static void setBoundReceiverCount(int i)
    {
        boundReceiverCount = i;
    }

    /**
     *
     */
    public static void incrementBoundReceiverCount()
    {
        boundReceiverCount++;
    }

    public static void decrementBoundReceiverCount()
    {
        boundReceiverCount--;
    }

    public static void showReceiverCount()
    {
        logger.info(boundReceiverCount + " receivers connected and bound");
    }

    /**
     * @param string
     */
    public static void setConnectionHandlerClassName(String string)
    {
        connectionHandlerClassName = string;
    }

    /**
     * @param string
     */
    public static void setDeliverFile(String string)
    {
        deliverFile = string;
    }

    /**
     * @param i
     */
    public static void setDeliverMessagesPerMin(int i)
    {
        deliverMessagesPerMin = i;
    }

    /**
     * @param bs
     */
    public static void setHttp200Response(byte[] bs)
    {
        http200Response = bs;
    }

    /**
     * @param bs
     */
    public static void setHttp400Response(byte[] bs)
    {
        http400Response = bs;
    }

    /**
     * @param i
     */
    public static void setHTTPPort(int i)
    {
        HTTPPort = i;
    }

    /**
     * @param i
     */
    public static void setHTTPThreads(int i)
    {
        HTTPThreads = i;
    }

    /**
     * @param b
     */
    public static void setLoopback(boolean b)
    {
        loopback = b;
    }

    /**
     * @param string
     */
    public static void setProtocolHandlerClassName(String string)
    {
        protocolHandlerClassName = string;
    }

    /**
     * @param string
     */
    public static void setRcv_address(String string)
    {
        rcv_address = string;
    }

    /**
     * @param socket
     */
    public static void setReceivesocket(Socket socket)
    {
        receivesocket = socket;
    }

    /**
     * @param i
     */
    public static void setSmppPort(int i)
    {
        smppPort = i;
    }

    /**
     * @param string
     */
    public static void setSmscid(String string)
    {
        smscid = string;
    }

    /**
     * @param is
     */
    public static void setMessageTypes(int[] is)
    {
        messageTypes = is;
    }

    /**
     * @return
     */
    public static int getMaxConnectionHandlers()
    {
        return maxConnectionHandlers;
    }

    /**
     * @param i
     */
    public static void setMaxConnectionHandlers(int i)
    {
        maxConnectionHandlers = i;
    }

    /**
     * @return
     */
    public static int getInbound_queue_capacity()
    {
        return inbound_queue_capacity;
    }

    /**
     * @return
     */
    public static int getOutbound_queue_capacity()
    {
        return outbound_queue_capacity;
    }

    /**
     * @return
     */
    public static String getLifeCycleManagerClassName()
    {
        return lifeCycleManagerClassName;
    }

    /**
     * @return
     */
    public static int getMessageStateCheckFrequency()
    {
        return messageStateCheckFrequency;
    }

    /**
     * @return
     */
    public static int getDiscardFromQueueAfter()
    {
        return discardFromQueueAfter;
    }

    /**
     * @return
     */
    public static int getMaxTimeEnroute()
    {
        return maxTimeEnroute;
    }

    /**
     * @return
     */
    public static int getPercentageAccepted()
    {
        return percentageAccepted;
    }

    /**
     * @return
     */
    public static int getPercentageDelivered()
    {
        return percentageDelivered;
    }

    /**
     * @return
     */
    public static int getPercentageRejected()
    {
        return percentageRejected;
    }

    /**
     * @return
     */
    public static int getPercentageThatTransition()
    {
        return percentageThatTransition;
    }

    /**
     * @return
     */
    public static int getPercentageUndeliverable()
    {
        return percentageUndeliverable;
    }

    /**
     * @return
     */
    public static String getAuthorisedFiles()
    {
        return authorisedFiles;
    }

    /**
     * @return
     */
    public static String getInjectMoPage()
    {
        return injectMoPage;
    }

    /**
     * @return
     */
    public static String getDocroot()
    {
        return docroot;
    }

    /**
     * @return
     */
    public static boolean isCaptureSmeBinary()
    {
        return captureSmeBinary;
    }

    /**
     * @return
     */
    public static String getCaptureSmeBinaryToFile()
    {
        return captureSmeBinaryToFile;
    }

    /**
     * @return
     */
    public static boolean isCaptureSmeDecoded()
    {
        return captureSmeDecoded;
    }

    /**
     * @return
     */
    public static String getCaptureSmeDecodedToFile()
    {
        return captureSmeDecodedToFile;
    }

    /**
     * @return
     */
    public static boolean isCaptureSmppsimBinary()
    {
        return captureSmppsimBinary;
    }

    /**
     * @return
     */
    public static String getCaptureSmppsimBinaryToFile()
    {
        return captureSmppsimBinaryToFile;
    }

    /**
     * @return
     */
    public static boolean isCaptureSmppsimDecoded()
    {
        return captureSmppsimDecoded;
    }

    /**
     * @return
     */
    public static String getCaptureSmppsimDecodedToFile()
    {
        return captureSmppsimDecodedToFile;
    }

    /**
     * @param b
     */
    public static void setCaptureSmeBinary(boolean b)
    {
        captureSmeBinary = b;
    }

    /**
     * @param string
     */
    public static void setCaptureSmeBinaryToFile(String string)
    {
        captureSmeBinaryToFile = string;
    }

    /**
     * @param b
     */
    public static void setCaptureSmeDecoded(boolean b)
    {
        captureSmeDecoded = b;
    }

    /**
     * @param string
     */
    public static void setCaptureSmeDecodedToFile(String string)
    {
        captureSmeDecodedToFile = string;
    }

    /**
     * @param b
     */
    public static void setCaptureSmppsimBinary(boolean b)
    {
        captureSmppsimBinary = b;
    }

    /**
     * @param string
     */
    public static void setCaptureSmppsimBinaryToFile(String string)
    {
        captureSmppsimBinaryToFile = string;
    }

    /**
     * @param b
     */
    public static void setCaptureSmppsimDecoded(boolean b)
    {
        captureSmppsimDecoded = b;
    }

    /**
     * @param string
     */
    public static void setCaptureSmppsimDecodedToFile(String string)
    {
        captureSmppsimDecodedToFile = string;
    }

    public static boolean isCallback()
    {
        return callback;
    }

    public static void setCallback(boolean callback)
    {
        SMPPSim.callback = callback;
    }

    public static String getCallback_target_host()
    {
        return callback_target_host;
    }

    public static void setCallback_target_host(String callback_target_host)
    {
        SMPPSim.callback_target_host = callback_target_host;
    }

    public static String getCallback_id()
    {
        return callback_id;
    }

    public static void setCallback_id(String callback_id)
    {
        SMPPSim.callback_id = callback_id;
    }

    public static int getCallback_port()
    {
        return callback_port;
    }

    public static void setCallback_port(int callback_port)
    {
        SMPPSim.callback_port = callback_port;
    }

    public static String getEsme_ip_address()
    {
        return esme_ip_address;
    }

    public static void setEsme_ip_address(String esme_ip_address)
    {
        SMPPSim.esme_ip_address = esme_ip_address;
    }

    public static String getEsme_password()
    {
        return esme_password;
    }

    public static void setEsme_password(String esme_password)
    {
        SMPPSim.esme_password = esme_password;
    }

    public static boolean isOutbind_enabled()
    {
        return outbind_enabled;
    }

    public static void setOutbind_enabled(boolean outbind_enabled)
    {
        SMPPSim.outbind_enabled = outbind_enabled;
    }

    public static int getEsme_port()
    {
        return esme_port;
    }

    public static void setEsme_port(int esme_port)
    {
        SMPPSim.esme_port = esme_port;
    }

    public static long getStart_at()
    {
        return start_at;
    }

    public static void setStart_at(long start_at)
    {
        SMPPSim.start_at = start_at;
    }

    public static String getMid_prefix()
    {
        return mid_prefix;
    }

    public static void setMid_prefix(String mid_prefix)
    {
        SMPPSim.mid_prefix = mid_prefix;
    }

    public static String[] getPasswords()
    {
        return passwords;
    }

    public static String[] getSystemids()
    {
        return systemids;
    }

    public static String getEsme_systemid()
    {
        return esme_systemid;
    }

    public static int getDelayed_iqueue_period()
    {
        return delayed_iqueue_period;
    }

    public static void setDelayed_iqueue_period(int delayed_iqueue_period)
    {
        SMPPSim.delayed_iqueue_period = delayed_iqueue_period;
    }

    public static int getDelayed_inbound_queue_max_attempts()
    {
        return delayed_inbound_queue_max_attempts;
    }

    public static void setDelayed_inbound_queue_max_attempts(int delayed_inbound_queue_max_attempts)
    {
        SMPPSim.delayed_inbound_queue_max_attempts = delayed_inbound_queue_max_attempts;
    }

    public static long getDelayReceiptsBy()
    {
        return delayReceiptsBy;
    }

    public static boolean isEsme_to_esme()
    {
        return esme_to_esme;
    }

    public static void setEsme_to_esme(boolean esme_to_esme)
    {
        SMPPSim.esme_to_esme = esme_to_esme;
    }

    public static boolean isDeliver_sm_includes_ussd_service_op()
    {
        return deliver_sm_includes_ussd_service_op;
    }

    public static void setDeliver_sm_includes_ussd_service_op(boolean deliverSmIncludesUssdServiceOp)
    {
        deliver_sm_includes_ussd_service_op = deliverSmIncludesUssdServiceOp;
    }

    public static boolean isDlr_tlr_required()
    {
        return dlr_tlr_required;
    }

    public static void setDlr_tlr_required(boolean dlrTlrRequired)
    {
        dlr_tlr_required = dlrTlrRequired;
    }

    public static short getDlr_tlv_tag()
    {
        return dlr_tlv_tag;
    }

    public static void setDlr_tlv_tag(short dlrTlvTag)
    {
        dlr_tlv_tag = dlrTlvTag;
    }

    public static short getDlr_tlv_len()
    {
        return dlr_tlv_len;
    }

    public static void setDlr_tlv_len(short dlrTlvLen)
    {
        dlr_tlv_len = dlrTlvLen;
    }

    public static byte[] getDlr_tlv_value()
    {
        return dlr_tlv_value;
    }

    public static void setDlr_tlv_value(byte[] dlrTlvValue)
    {
        dlr_tlv_value = dlrTlvValue;
    }

    public static boolean isDlr_opt_tlr_required()
    {
        return dlr_opt_tlv_required;
    }

    public static void setDlr_opt_tlr_required(boolean dlrOptTlrRequired)
    {
        dlr_opt_tlv_required = dlrOptTlrRequired;
    }
}
