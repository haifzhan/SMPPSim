/****************************************************************************
 * HTTPHandler.java
 *
 * Copyright (C) Selenium Software Ltd 2006
 *
 * This file is part of SMPPSim.
 *
 * SMPPSim is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * SMPPSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SMPPSim; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author martin@seleniumsoftware.com
 * http://www.woolleynet.com
 * http://www.seleniumsoftware.com
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/HttpHandler.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************
 */

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.InvalidHexStringlException;
import com.seleniumsoftware.SMPPSim.pdu.*;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;
import com.seleniumsoftware.SMPPSim.util.Utilities;

import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * Class <code>HttpHandler</code> is a very simple http server which provides
 * basic facilities to control and monitor the SMPPSim server
 * 
 * @author Martin Woolley, www.seleniumsoftware.com
 */

public class HttpHandler implements Runnable {
	private static Logger logger = Logger
			.getLogger("com.seleniumsoftware.smppsim");

	private Smsc smsc = Smsc.getInstance();

	private DeliverSM newMessage;

	private static byte[] http200Response;

	private static final String http200Message = "HTTP/1.1 200\r\n\r\nOK\r\n";

	private static byte[] http400Response;

	private static final String http400Message = "HTTP/1.1 400\r\n\r\nREQUEST NOT UNDERSTOOD BY SERVER\r\n";

	private static byte[] http500Response;

	private static final String http500Message = "HTTP/1.1 500\r\n\r\nAN INTERNAL ERROR HAS OCCURRED IN THE MANAGEMENT SERVER\r\n";

	byte[] response;

	boolean responseOK;

	boolean firstStopRequest = true;

	boolean running = true;

	private SimpleDateFormat sft;

	private ArrayList<String> authorisedFiles;

	private static final String delim = "$$";

	private static final String STARTTIME = "$$starttime$$";

	private static final String MESSAGE = "$$message$$";

	private static final String TXBOUND = "$$txbound$$";

	private static final String RXBOUND = "$$rxbound$$";

	private static final String TRXBOUND = "$$trxbound$$";

	private static final String IQCOUNT = "$$iqcount$$";

	private static final String OQCOUNT = "$$oqcount$$";

	private static final String PQCOUNT = "$$pqcount$$";

	private static final String VERSION = "$$version$$";

	private static final String BINDTRANSMITTER_OK = "$$bindtransmitter_ok$$";

	private static final String BINDTRANSMITTER_ERR = "$$bindtransmitter_err$$";

	private static final String BINDRECEIVER_OK = "$$bindreceiver_ok$$";

	private static final String BINDRECEIVER_ERR = "$$bindreceiver_err$$";

	private static final String BINDTRANSCEIVER_OK = "$$bindtransceiver_ok$$";

	private static final String BINDTRANSCEIVER_ERR = "$$bindtransceiver_err$$";

	private static final String OUTBIND_OK = "$$outbind_ok$$";

	private static final String OUTBIND_ERR = "$$outbind_err$$";

	private static final String SUBMIT_SM_OK = "$$submit_sm_ok$$";

	private static final String SUBMIT_SM_ERR = "$$submit_sm_err$$";

	private static final String SUBMIT_MULTI_OK = "$$submit_multi_ok$$";

	private static final String SUBMIT_MULTI_ERR = "$$submit_multi_err$$";

	private static final String DELIVER_SM_OK = "$$deliver_sm_ok$$";

	private static final String DELIVER_SM_ERR = "$$deliver_sm_err$$";

	private static final String DATA_SM_OK = "$$data_sm_ok$$";

	private static final String DATA_SM_ERR = "$$data_sm_err$$";

	private static final String QUERY_SM_OK = "$$query_sm_ok$$";

	private static final String QUERY_SM_ERR = "$$query_sm_err$$";

	private static final String CANCEL_SM_OK = "$$cancel_sm_ok$$";

	private static final String CANCEL_SM_ERR = "$$cancel_sm_err$$";

	private static final String REPLACE_SM_OK = "$$replace_sm_ok$$";

	private static final String REPLACE_SM_ERR = "$$replace_sm_err$$";

	private static final String ENQUIRE_LINK_OK = "$$enquire_link_ok$$";

	private static final String ENQUIRE_LINK_ERR = "$$enquire_link_err$$";

	private static final String UNBIND_OK = "$$unbind_ok$$";

	private static final String UNBIND_ERR = "$$unbind_err$$";

	private static final String GENERIC_NAK_OK = "$$generic_nak_ok$$";

	private static final String GENERIC_NAK_ERR = "$$generic_nak_err$$";

	private File docRoot;

	private String docRootName;

	private String controlPanelMessage = "";

	// inject mo params

	private static final String SHORT_MESSAGE = "$$short_message$$";

	private static final String FORMAT = "$$format$$";

	private static final String SOURCE_ADDR = "$$source_addr$$";

	private static final String DEST_ADDR = "$$dest_addr$$";

	private static final String SERVICE_TYPE = "$$service_type$$";

	private static final String SOURCE_ADDR_TON = "$$source_addr_ton$$";

	private static final String SOURCE_ADDR_NPI = "$$source_addr_npi$$";

	private static final String DEST_ADDR_TON = "$$dest_addr_ton$$";

	private static final String DEST_ADDR_NPI = "$$dest_addr_npi$$";

	private static final String ESM_CLASS = "$$esm_class$$";

	private static final String PROTOCOL_ID = "$$protocol_id$$";

	private static final String PRIORITY_FLAG = "$$priority_flag$$";

	private static final String SCHEDULE_DELIVERY_TIME = "$$schedule_delivery_time$$";

	private static final String VALIDITY_PERIOD = "$$validity_period$$";

	private static final String REGISTERED_DELIVERY_FLAG = "$$registered_delivery_flag$$";

	private static final String REPLACE_IF_PRESENT_FLAG = "$$replace_if_present_flag$$";

	private static final String DATA_CODING = "$$data_coding$$";

	private static final String SM_DEFAULT_MESSAGE_ID = "$$sm_default_message_id$$";

	private static final String SM_LENGTH = "$$sm_length$$";

	private static final String USER_MESSAGE_REFERENCE = "$$user_message_reference$$";

	private static final String SOURCE_PORT = "$$source_port$$";

	private static final String DESTINATION_PORT = "$$destination_port$$";

	private static final String SAR_MSG_REF_NUM = "$$sar_msg_ref_num$$";

	private static final String SAR_TOTAL_SEGMENTS = "$$sar_total_segments$$";

	private static final String SAR_SEGMENT_SEQNUM = "$$sar_segment_seqnum$$";

	private static final String USER_RESPONSE_CODE = "$$user_response_code$$";

	private static final String PRIVACY_INDICATOR = "$$privacy_indicator$$";

	private static final String PAYLOAD_TYPE = "$$payload_type$$";

	private static final String MESSAGE_PAYLOAD = "$$message_payload$$";

	private static final String CALLBACK_NUM = "$$callback_num$$";

	private static final String SOURCE_SUBADDRESS = "$$source_subaddress$$";

	private static final String DEST_SUBADDRESS = "$$dest_subaddress$$";

	private static final String LANGUAGE_INDICATOR = "$$language_indicator$$";

	private static final String TLV1_TAG = "$$tlv1_tag$$";

	private static final String TLV2_TAG = "$$tlv2_tag$$";

	private static final String TLV3_TAG = "$$tlv3_tag$$";

	private static final String TLV4_TAG = "$$tlv4_tag$$";

	private static final String TLV5_TAG = "$$tlv5_tag$$";

	private static final String TLV6_TAG = "$$tlv6_tag$$";

	private static final String TLV7_TAG = "$$tlv7_tag$$";

	private static final String TLV1_LEN = "$$tlv1_len$$";

	private static final String TLV2_LEN = "$$tlv2_len$$";

	private static final String TLV3_LEN = "$$tlv3_len$$";

	private static final String TLV4_LEN = "$$tlv4_len$$";

	private static final String TLV5_LEN = "$$tlv5_len$$";

	private static final String TLV6_LEN = "$$tlv6_len$$";

	private static final String TLV7_LEN = "$$tlv7_len$$";

	private static final String TLV1_VAL = "$$tlv1_val$$";

	private static final String TLV2_VAL = "$$tlv2_val$$";

	private static final String TLV3_VAL = "$$tlv3_val$$";

	private static final String TLV4_VAL = "$$tlv4_val$$";

	private static final String TLV5_VAL = "$$tlv5_val$$";

	private static final String TLV6_VAL = "$$tlv6_val$$";

	private static final String TLV7_VAL = "$$tlv7_val$$";

	private String short_message = "Hello from SMPPSim";

	private boolean shortMessageInHex = false;

	private String source_addr = "4477665544";

	private String dest_addr = "";

	private String service_type = "";

	private String source_addr_ton = "1";

	private String source_addr_npi = "1";

	private String dest_addr_ton = "1";

	private String dest_addr_npi = "1";

	private String esm_class = "0";

	private String protocol_id = "";

	private String priority_flag = "";

	private String schedule_delivery_time = "";

	private String validity_period = "";

	private String registered_delivery_flag = "0";

	private String replace_if_present_flag = "0";

	private String data_coding = "0";

	private String sm_default_message_id = "";

	private String sm_length = "";

	private String user_message_reference = "";

	private String source_port = "";

	private String destination_port = "";

	private String sar_msg_ref_num = "";

	private String sar_total_segments = "";

	private String sar_segment_seqnum = "";

	private String user_response_code = "";

	private String privacy_indicator = "";

	private String payload_type = "";

	private String message_payload = "";

	private String callback_num = "";

	private String source_subaddress = "";

	private String dest_subaddress = "";

	private String language_indicator = "";

	private String tlv1_tag = "";

	private String tlv2_tag = "";

	private String tlv3_tag = "";

	private String tlv4_tag = "";

	private String tlv5_tag = "";

	private String tlv6_tag = "";

	private String tlv7_tag = "";

	private String tlv1_len = "";

	private String tlv2_len = "";

	private String tlv3_len = "";

	private String tlv4_len = "";

	private String tlv5_len = "";

	private String tlv6_len = "";

	private String tlv7_len = "";

	private String tlv1_val = "";

	private String tlv2_val = "";

	private String tlv3_val = "";

	private String tlv4_val = "";

	private String tlv5_val = "";

	private String tlv6_val = "";

	private String tlv7_val = "";

	public HttpHandler(File dr) {
		this.docRoot = dr;
		this.docRootName = dr.getName() + "/";
		sft = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		authorisedFiles = new ArrayList<String>();
		String af = SMPPSim.getAuthorisedFiles();
		StringTokenizer st = new StringTokenizer(af, ",");
		while (st.hasMoreElements()) {
			authorisedFiles.add(st.nextToken());
		}
		logger.finest("Creating HttpHandler for docRoot " + docRootName);
	}

	public void run() {
		logger.info("Starting HttpHandler thread. Listening on port "
				+ SMPPSim.getHTTPPort() + " for conections");
		byte[] HTTPresponse;
		String request = null;
		http200Response = http200Message.getBytes();
		http400Response = http400Message.getBytes();
		http500Response = http500Message.getBytes();
		BufferedReader is = null;
		OutputStream os = null;
		Socket socket = null;

		do // process connections forever.... or less
		{
			try {
				logger.finest("HttpHandler thread is waiting for connection");
				socket = smsc.getCss().accept();
				logger.finest("accepted connection");
			} catch (IOException e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning("exception accepting connection");
			}
			try {
				is = new BufferedReader(new InputStreamReader(socket
						.getInputStream()));
			} catch (IOException e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning("failure getting input stream");
			}
			try {
				os = socket.getOutputStream();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning("failure getting output stream");
			}
			try {
				request = readRequest(is);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning("failure getting request string");
			}

			response = processRequest(request);

			if (responseOK) {
				try {
					writeResponse(response, os);
				} catch (Exception e) {
				}
			}
			try {
				closeConnection(socket, os);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning("failed closing connection");
			}
		} while (running);
		logger.info("Halting http server thread ");
	}

	private byte[] processRequest(String target) {
		responseOK = true;
		String filename;
		String command;

		logger.finest("in processHTTPRequest");
		logger.finest("HTTP Request=" + target);

		int qmark = target.indexOf("?");
		if (qmark > -1) {
			filename = target.substring(0, qmark);
			command = target.substring(qmark + 1, target.length());
		} else {
			filename = target.substring(0, target.length());
			command = "implicit-refresh";
		}
		logger.finest("Filename=" + filename);

		boolean authorisedFile = false;

		if (authorisedFiles.indexOf(filename) > -1)
			authorisedFile = true;
		else {
			if (filename.equals("/"))
				authorisedFile = true;
			else if (filename.equals("/inject_mo")) {
				authorisedFile = true;
				command = "inject";
			} else
				logger.finest("Unauthorised file <" + filename
						+ "> requested - ignoring request");
		}

		if (!authorisedFile) {
			responseOK = false;
			return http400Response;
		}

		if (command.equalsIgnoreCase("inject")) {
			controlPanelMessage = "";
			return injectMo(target);
		}
		
		if (command.equalsIgnoreCase("stats")) {
			controlPanelMessage = "";
			return stats();
		}

		File requestedFile;
		requestedFile = generateFile(filename);

		if (requestedFile == null) {
			responseOK = false;
			return http400Response;
		}

		if (filename.endsWith(".gif") || filename.endsWith(".jpg")
				|| filename.endsWith(".css"))
			command = "plain-get";

		if (filename.equals(SMPPSim.getInjectMoPage())) {
			logger.finest("The InjectMO page has been requested");
			command = "implicit-refresh";
		}

		logger.finest("Generated requestedFile:" + requestedFile);
		logger.finest("Processing command:" + command);
		try {
			if (command.equalsIgnoreCase("plain-get")) {
				return plainResponse(requestedFile);
			}

			if (command.equalsIgnoreCase("shutdown")) {
				if (firstStopRequest) {
					controlPanelMessage = "<b>Please confirm by selecting Shutdown again or select Refresh to cancel...</b>";
					firstStopRequest = false;
				} else {
					controlPanelMessage = "<b>Shutdown initiated.....</b>";
					firstStopRequest = true;
					smsc.stop();
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException ie) {
							} finally {
								System.exit(0);
							}
						}
					}).start();
				}
				return prepareResponse(requestedFile);
			}

			if (command.equalsIgnoreCase("refresh")
					|| command.equalsIgnoreCase("implicit-refresh")) {
				controlPanelMessage = "";
				firstStopRequest = true;
				return prepareResponse(requestedFile);
			}

		} catch (IOException ioe) {
			logger.log(Level.WARNING, "Exception: " + ioe.getMessage(), ioe);
			logger.warning("IOException preparing http response: "
					+ ioe.getMessage());
			responseOK = false;
			return http500Response;
		}

		responseOK = false;

		return http400Response;
	}

	private File generateFile(String filename) {
		File requestedFile;

		// Build up the path to the requested file in a
		// platform independent way. URL's use '/' in their
		// path, but this platform may not.
		if (filename.indexOf("..") != -1)
			return null;
		requestedFile = new File(docRootName + filename);
		logger.finest("requestedFile=" + requestedFile.getName());
		if (requestedFile.exists() && requestedFile.isDirectory()) {

			logger.finest("Requested file exists and is a directory");
			// If a directory was requested, modify the request
			// to look for the default file in that
			// directory.
			String defaultFile = docRootName + "index.htm";
			logger.finest("defaultFile=" + defaultFile);
			requestedFile = new File(defaultFile);
			try {
				logger.finest("Requested file got set to default of:"
						+ requestedFile.getCanonicalPath());
			} catch (IOException ioe) {
				logger
						.log(Level.WARNING, "Exception: " + ioe.getMessage(),
								ioe);
				logger.warning("IOException: " + ioe.getMessage());
			}
		}

		return requestedFile;
	}

	private byte[] prepareResponse(File requestedFile) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		if (requestedFile.exists()) {
			int fileLen = (int) requestedFile.length();
			BufferedInputStream fileIn = new BufferedInputStream(
					new FileInputStream(requestedFile));
			String contentType = URLConnection
					.guessContentTypeFromStream(fileIn);
			if (requestedFile.getAbsolutePath().endsWith(".htm")) {
				contentType="text/html";
			}
			logger.finest("Content Type thought to be:" + contentType);
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", fileLen,
					contentType);
			bOut.write(headerBytes);
			byte[] buf = new byte[2048];
			int blockLen = 0;
			while ((blockLen = fileIn.read(buf)) != -1) {
				bOut.write(buf, 0, blockLen);
			}
			fileIn.close();
		} else {
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 404 Not Found",
					-1, null);
			bOut.write(headerBytes);
		}
		bOut.flush();
		bOut.close();
		logger.finest("response prepared - just params to deal with now");
		return replaceParams(bOut).getBytes();
	}

	private String replaceParams(ByteArrayOutputStream bOut) {
		logger.finest("In replaceParams");
		String rawFile = bOut.toString();
		// logger.finest(rawFile);
		boolean done = false;
		int paramStart = 0;
		int paramEnd = 0;
		int p = 0;
		String param = "";
		StringBuffer sb = new StringBuffer();

		do {
			paramStart = rawFile.indexOf(delim, p);
			if (paramStart > -1) {
				paramEnd = rawFile.indexOf(delim, paramStart + 2) + 2;
				logger.finest("Found param end at " + paramEnd);
				if ((paramStart > -1) && (paramEnd > -1)) {
					sb.append(rawFile.substring(p, paramStart));
					param = rawFile.substring(paramStart, paramEnd);
					sb.append(getParamValue(param));
					p = paramEnd;
				} else {
					done = true;
				}
			} else {
				done = true;
			}
		} while (!done);
		sb.append(rawFile.substring(p, rawFile.length()));
		return sb.toString();
	}

	private String getParamValue(String paramName) {
		logger.finest("Getting param value for <" + paramName + ">");

		if (paramName.equals(MESSAGE))
			return controlPanelMessage;
		if (paramName.equals(STARTTIME))
			return smsc.getStartTimeString();
		if (paramName.equals(VERSION))
			return SMPPSim.getVersion();
		if (paramName.equals(TXBOUND))
			return Integer.toString(smsc.getTxBoundCount());
		if (paramName.equals(RXBOUND))
			return Integer.toString(smsc.getRxBoundCount());
		if (paramName.equals(TRXBOUND))
			return Integer.toString(smsc.getTrxBoundCount());
		if (paramName.equals(IQCOUNT))
			return Integer.toString(smsc.getInbound_queue_size());
		if (paramName.equals(PQCOUNT))
			return Integer.toString(smsc.getPending_queue_size());
		if (paramName.equals(OQCOUNT))
			return Integer.toString(smsc.getOutbound_queue_size());
		if (paramName.equals(BINDTRANSMITTER_OK))
			return Long.toString(smsc.getBindTransmitterOK());
		if (paramName.equals(BINDTRANSMITTER_ERR))
			return Long.toString(smsc.getBindTransmitterERR());
		if (paramName.equals(BINDRECEIVER_OK))
			return Long.toString(smsc.getBindReceiverOK());
		if (paramName.equals(BINDRECEIVER_ERR))
			return Long.toString(smsc.getBindReceiverERR());
		if (paramName.equals(BINDTRANSCEIVER_OK))
			return Long.toString(smsc.getBindTransceiverOK());
		if (paramName.equals(BINDTRANSCEIVER_ERR))
			return Long.toString(smsc.getBindTransceiverERR());
		if (paramName.equals(OUTBIND_OK))
			return Long.toString(smsc.getOutbindOK());
		if (paramName.equals(OUTBIND_ERR))
			return Long.toString(smsc.getOutbindERR());
		if (paramName.equals(SUBMIT_SM_OK))
			return Long.toString(smsc.getSubmitSmOK());
		if (paramName.equals(SUBMIT_SM_ERR))
			return Long.toString(smsc.getSubmitSmERR());
		if (paramName.equals(SUBMIT_MULTI_OK))
			return Long.toString(smsc.getSubmitMultiOK());
		if (paramName.equals(SUBMIT_MULTI_ERR))
			return Long.toString(smsc.getSubmitMultiERR());
		if (paramName.equals(DELIVER_SM_OK))
			return Long.toString(smsc.getDeliverSmOK());
		if (paramName.equals(DELIVER_SM_ERR))
			return Long.toString(smsc.getDeliverSmERR());
		if (paramName.equals(DATA_SM_OK))
			return Long.toString(smsc.getDataSmOK());
		if (paramName.equals(DATA_SM_ERR))
			return Long.toString(smsc.getDataSmERR());
		if (paramName.equals(QUERY_SM_OK))
			return Long.toString(smsc.getQuerySmOK());
		if (paramName.equals(QUERY_SM_ERR))
			return Long.toString(smsc.getQuerySmERR());
		if (paramName.equals(CANCEL_SM_OK))
			return Long.toString(smsc.getCancelSmOK());
		if (paramName.equals(CANCEL_SM_ERR))
			return Long.toString(smsc.getCancelSmERR());
		if (paramName.equals(REPLACE_SM_OK))
			return Long.toString(smsc.getReplaceSmOK());
		if (paramName.equals(REPLACE_SM_ERR))
			return Long.toString(smsc.getReplaceSmERR());
		if (paramName.equals(ENQUIRE_LINK_OK))
			return Long.toString(smsc.getEnquireLinkOK());
		if (paramName.equals(ENQUIRE_LINK_ERR))
			return Long.toString(smsc.getEnquireLinkERR());
		if (paramName.equals(UNBIND_OK))
			return Long.toString(smsc.getUnbindOK());
		if (paramName.equals(UNBIND_ERR))
			return Long.toString(smsc.getUnbindERR());
		if (paramName.equals(GENERIC_NAK_OK))
			return Long.toString(smsc.getGenericNakOK());
		if (paramName.equals(GENERIC_NAK_ERR))
			return Long.toString(smsc.getGenericNakERR());
		// inject_mo form
		if (paramName.equals(SHORT_MESSAGE)) {
			return short_message;
		}
		if (paramName.equals(FORMAT)) {
			if (shortMessageInHex)
				return "checked";
			else
				return "";
		}
		if (paramName.equals(SOURCE_ADDR))
			return source_addr;
		if (paramName.equals(DEST_ADDR))
			return dest_addr;
		if (paramName.equals(SERVICE_TYPE))
			return service_type;
		if (paramName.equals(SOURCE_ADDR_TON))
			return source_addr_ton;
		if (paramName.equals(SOURCE_ADDR_NPI))
			return source_addr_npi;
		if (paramName.equals(DEST_ADDR_TON))
			return dest_addr_ton;
		if (paramName.equals(DEST_ADDR_NPI))
			return dest_addr_npi;
		if (paramName.equals(ESM_CLASS))
			return esm_class;
		if (paramName.equals(PROTOCOL_ID))
			return protocol_id;
		if (paramName.equals(PRIORITY_FLAG))
			return priority_flag;
		if (paramName.equals(SCHEDULE_DELIVERY_TIME))
			return schedule_delivery_time;
		if (paramName.equals(VALIDITY_PERIOD))
			return validity_period;
		if (paramName.equals(REGISTERED_DELIVERY_FLAG))
			return registered_delivery_flag;
		if (paramName.equals(REPLACE_IF_PRESENT_FLAG))
			return replace_if_present_flag;
		if (paramName.equals(DATA_CODING))
			return data_coding;
		if (paramName.equals(SM_DEFAULT_MESSAGE_ID))
			return sm_default_message_id;
		if (paramName.equals(SM_LENGTH))
			return sm_length;
		if (paramName.equals(USER_MESSAGE_REFERENCE))
			return user_message_reference;
		if (paramName.equals(SOURCE_PORT))
			return source_port;
		if (paramName.equals(DESTINATION_PORT))
			return destination_port;
		if (paramName.equals(SAR_MSG_REF_NUM))
			return sar_msg_ref_num;
		if (paramName.equals(SAR_TOTAL_SEGMENTS))
			return sar_msg_ref_num;
		if (paramName.equals(SAR_SEGMENT_SEQNUM))
			return sar_segment_seqnum;
		if (paramName.equals(USER_RESPONSE_CODE))
			return user_response_code;
		if (paramName.equals(PRIVACY_INDICATOR))
			return privacy_indicator;
		if (paramName.equals(PAYLOAD_TYPE))
			return privacy_indicator;
		if (paramName.equals(MESSAGE_PAYLOAD))
			return message_payload;
		if (paramName.equals(CALLBACK_NUM))
			return callback_num;
		if (paramName.equals(SOURCE_SUBADDRESS))
			return source_subaddress;
		if (paramName.equals(DEST_SUBADDRESS))
			return dest_subaddress;
		if (paramName.equals(LANGUAGE_INDICATOR))
			return language_indicator;
		if (paramName.equals(TLV1_TAG))
			return tlv1_tag;
		if (paramName.equals(TLV2_TAG))
			return tlv2_tag;
		if (paramName.equals(TLV3_TAG))
			return tlv3_tag;
		if (paramName.equals(TLV4_TAG))
			return tlv4_tag;
		if (paramName.equals(TLV5_TAG))
			return tlv5_tag;
		if (paramName.equals(TLV6_TAG))
			return tlv6_tag;
		if (paramName.equals(TLV7_TAG))
			return tlv7_tag;
		if (paramName.equals(TLV1_LEN))
			return tlv1_len;
		if (paramName.equals(TLV2_LEN))
			return tlv2_len;
		if (paramName.equals(TLV3_LEN))
			return tlv3_len;
		if (paramName.equals(TLV4_LEN))
			return tlv4_len;
		if (paramName.equals(TLV5_LEN))
			return tlv5_len;
		if (paramName.equals(TLV6_LEN))
			return tlv6_len;
		if (paramName.equals(TLV7_LEN))
			return tlv7_len;
		if (paramName.equals(TLV1_VAL))
			return tlv1_val;
		if (paramName.equals(TLV2_VAL))
			return tlv2_val;
		if (paramName.equals(TLV3_VAL))
			return tlv3_val;
		if (paramName.equals(TLV4_VAL))
			return tlv4_val;
		if (paramName.equals(TLV5_VAL))
			return tlv5_val;
		if (paramName.equals(TLV6_VAL))
			return tlv6_val;
		if (paramName.equals(TLV7_VAL))
			return tlv7_val;
		return ("Unrecognised Parameter name " + paramName);
	}

	private byte[] injectMo(String message) {
		try {
			newMessage = parseMoInjectForm(message);
			logger.finest("made DeliverSM object");
			smsc.getIq().addMessage(newMessage);
			controlPanelMessage = "Message added to SMPPSim InboundQueue OK";
		} catch (InvalidHexStringlException ihse) {
		} catch (Exception e) {
			logger.warning(e.getMessage());
			e.printStackTrace();
			return http400Response;
		}

		File requestedFile;
		requestedFile = generateFile(SMPPSim.getInjectMoPage());
		try {
			return prepareResponse(requestedFile);
		} catch (Exception e) {
		}

		return null;
	}

	private byte[] stats() {

			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			try {
				byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", -1, null);
				bOut.write(headerBytes);
				byte[] buf = new byte[2048];
				String response = "submittedok="+smsc.getSubmitSmOK()+",deliveredok="+smsc.getDeliverSmOK();
				bOut.write(response.getBytes());
				bOut.flush();
				bOut.close();
				return bOut.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

	}

	private DeliverSM parseMoInjectForm(String HTTPmessage) throws Exception {

		DeliverSM smppmsg = new DeliverSM();
		String tmpMessage = HTTPmessage;
		int i = 0;
		int l = tmpMessage.length();
		Hashtable<String, String> args = new Hashtable<String, String>();
		String token = null;
		String key = null;
		String value = null;

		// get rid of GET etc

		i = tmpMessage.indexOf("?");
		tmpMessage = tmpMessage.substring(i + 1, l);
		logger.finest("HTTP args:" + tmpMessage);

		StringTokenizer st = new StringTokenizer(tmpMessage, " &\t\n", false);
		while (st.hasMoreTokens()) {
			token = st.nextToken();
//			logger.info(token);
			StringTokenizer st2 = new StringTokenizer(token, "=", false);
			while (st2.hasMoreTokens()) {
				key = st2.nextToken();
				if (st2.hasMoreTokens()) {
					value = st2.nextToken();
					if (value != null)
						args.put(key, value);
					else if (key.equals("short_message")) {
						args.put(key, new String(""));
					}
				} else if (key.equals("short_message")) {
					args.put(key, new String(""));
				} else {
					value = null;
				}
			}
		}

		logger.finest("HTTPHandler: extracted args from GET: "
				+ args.toString());

		if (args.containsKey("data_coding")) {
			smppmsg.setData_coding(Integer.parseInt(URLDecoder.decode(args
					.get("data_coding"), "UTF-8")));
			data_coding = args.get("data_coding");
		}
		shortMessageInHex = false;
		if (args.containsKey(("format")))
			shortMessageInHex = true;
		if (args.containsKey("short_message")) {
			short_message = URLDecoder.decode(args.get("short_message"),
					"UTF-8");
			if (!shortMessageInHex) {
				String msg = URLDecoder.decode(args.get("short_message"),
						"UTF-8");
				String encoding = PduUtilities.getJavaEncoding((byte) smppmsg
						.getData_coding());
				if (encoding != null)
					smppmsg.setShort_message(msg.getBytes(encoding));
				else
					smppmsg.setShort_message(msg.getBytes());
			} else {
				smppmsg.setShort_message(makeBinaryMessage(URLDecoder.decode(
						args.get("short_message"), "UTF-8")));
			}
		} else {
			smppmsg.setShort_message(short_message.getBytes());
		}
		if (args.containsKey("source_addr")) {
			smppmsg.setSource_addr(args.get("source_addr"));
			source_addr = args.get("source_addr");
		} else {
			smppmsg.setSource_addr(source_addr);
		}
		if (args.containsKey("destination_addr")) {
			smppmsg.setDestination_addr(args.get("destination_addr"));
			dest_addr = args.get("destination_addr");
		} else {
			smppmsg.setDestination_addr(dest_addr);
		}
		if (args.containsKey("service_type")) {
			smppmsg.setService_type(URLDecoder.decode(args.get("service_type"),
					"UTF-8"));
			service_type = args.get("service_type");
		}
		if (args.containsKey("source_addr_ton")) {
			smppmsg.setSource_addr_ton(Integer.parseInt(URLDecoder.decode(args
					.get("source_addr_ton"), "UTF-8")));
			source_addr_ton = args.get("source_addr_ton");
		}
		if (args.containsKey("source_addr_npi")) {
			smppmsg.setSource_addr_npi(Integer.parseInt(URLDecoder.decode(args
					.get("source_addr_npi"), "UTF-8")));
			source_addr_npi = args.get("source_addr_npi");
		}
		if (args.containsKey("dest_addr_ton")) {
			smppmsg.setDest_addr_ton(Integer.parseInt(URLDecoder.decode(args
					.get("dest_addr_ton"), "UTF-8")));
			dest_addr_ton = args.get("dest_addr_ton");
		}
		if (args.containsKey("dest_addr_npi")) {
			smppmsg.setDest_addr_npi(Integer.parseInt(URLDecoder.decode(args
					.get("dest_addr_npi"), "UTF-8")));
			dest_addr_npi = args.get("dest_addr_npi");
		}
		if (args.containsKey("esm_class")) {
			smppmsg.setEsm_class(Integer.parseInt(URLDecoder.decode(args
					.get("esm_class"), "UTF-8")));
			esm_class = args.get("esm_class");
		}
		if (args.containsKey("protocol_ID")) {
			smppmsg.setProtocol_ID(Integer.parseInt(URLDecoder.decode(args
					.get("protocol_ID"), "UTF-8")));
			protocol_id = args.get("protocol_ID");
		}
		if (args.containsKey("priority_flag")) {
			smppmsg.setPriority_flag(Integer.parseInt(URLDecoder.decode(args
					.get("priority_flag"), "UTF-8")));
			priority_flag = args.get("priority_flag");
		}
		if (args.containsKey("registered_delivery_flag")) {
			smppmsg.setRegistered_delivery_flag(Integer.parseInt(URLDecoder
					.decode(args.get("registered_delivery_flag"), "UTF-8")));
			registered_delivery_flag = args.get("registered_delivery_flag");
		}
		if (args.containsKey("sm_default_msg_id")) {
			smppmsg.setSm_default_msg_id(Integer.parseInt(URLDecoder.decode(
					args.get("sm_default_msg_id"), "UTF-8")));
			sm_default_message_id = args.get("sm_default_msg_id");
		}
		if (args.containsKey("sm_length")) {
			smppmsg.setSm_length(Integer.parseInt(URLDecoder.decode(args
					.get("sm_length"), "UTF-8")));
			sm_length = args.get("sm_length");
		} else {
			smppmsg.setSm_length(smppmsg.getShort_message().length);
			sm_length = "";
		}
		if (args.containsKey("user_message_reference")) {
			smppmsg.setString_user_message_reference(args
					.get("user_message_reference"));
			user_message_reference = args.get("user_message_reference");
		} else {
			user_message_reference = "";
		}
		if (args.containsKey("source_port")) {
			smppmsg.setString_source_port(args.get("source_port"));
			source_port = args.get("source_port");
		} else {
			source_port = "";
		}
		if (args.containsKey("destination_port")) {
			smppmsg.setString_destination_port(args.get("destination_port"));
			destination_port = args.get("destination_port");
		} else {
			destination_port = "";
		}
		if (args.containsKey("sar_msg_ref_num")) {
			smppmsg.setString_sar_msg_ref_num(args.get("sar_msg_ref_num"));
			sar_msg_ref_num = args.get("sar_msg_ref_num");
		} else {
			sar_msg_ref_num = "";
		}
		if (args.containsKey("sar_total_segments")) {
			smppmsg
					.setString_sar_total_segments(args
							.get("sar_total_segments"));
			sar_total_segments = args.get("sar_total_segments");
		} else {
			sar_total_segments = "";
		}
		if (args.containsKey("sar_segment_seqnum")) {
			smppmsg
					.setString_sar_segment_seqnum(args
							.get("sar_segment_seqnum"));
			sar_segment_seqnum = args.get("sar_segment_seqnum");
		} else {
			sar_segment_seqnum = "";
		}
		if (args.containsKey("user_response_code")) {
			smppmsg
					.setString_user_response_code(args
							.get("user_response_code"));
			user_response_code = args.get("user_response_code");
		} else {
			user_response_code = "";
		}
		if (args.containsKey("privacy_indicator")) {
			smppmsg.setString_privacy_indicator(args.get("privacy_indicator"));
			privacy_indicator = args.get("privacy_indicator");
		} else {
			privacy_indicator = "";
		}
		if (args.containsKey("payload_type")) {
			smppmsg.setString_payload_type(args.get("payload_type"));
			payload_type = args.get("payload_type");
		} else {
			payload_type = "";
		}
		if (args.containsKey("message_payload")) {
			smppmsg.setString_message_payload(args.get("message_payload"));
			message_payload = args.get("message_payload");
		} else {
			message_payload = "";
		}
		if (args.containsKey("callback_num")) {
			smppmsg.setString_callback_num(args.get("callback_num"));
			callback_num = args.get("callback_num");
		} else {
			callback_num = "";
		}
		if (args.containsKey("source_subaddress")) {
			smppmsg.setString_source_subaddress(args.get("source_subaddress"));
			source_subaddress = args.get("source_subaddress");
		} else {
			source_subaddress = "";
		}
		if (args.containsKey("dest_subaddress")) {
			smppmsg.setString_dest_subaddress(args.get("dest_subaddress"));
			dest_subaddress = args.get("dest_subaddress");
		} else {
			dest_subaddress = "";
		}
		if (args.containsKey("language_indicator")) {
			smppmsg
					.setString_language_indicator(args
							.get("language_indicator"));
			language_indicator = args.get("language_indicator");
		} else {
			language_indicator = "";
		}
		if (args.containsKey("tlv1_tag")) {
			tlv1_tag = args.get("tlv1_tag");
			tlv1_len = args.get("tlv1_len");
			tlv1_val = args.get("tlv1_val");
			short t = Short.parseShort(tlv1_tag);
			short ln = Short.parseShort(tlv1_len);
			byte[] val = getValueBytes(args.get("tlv1_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv2_tag")) {
			tlv2_tag = args.get("tlv2_tag");
			tlv2_len = args.get("tlv2_len");
			tlv2_val = args.get("tlv2_val");
			short t = Short.parseShort(tlv2_tag);
			short ln = Short.parseShort(tlv2_len);
			byte[] val = getValueBytes(args.get("tlv2_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv3_tag")) {
			tlv3_tag = args.get("tlv3_tag");
			tlv3_len = args.get("tlv3_len");
			tlv3_val = args.get("tlv3_val");
			short t = Short.parseShort(tlv3_tag);
			short ln = Short.parseShort(tlv3_len);
			byte[] val = getValueBytes(args.get("tlv3_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv4_tag")) {
			tlv4_tag = args.get("tlv4_tag");
			tlv4_len = args.get("tlv4_len");
			tlv4_val = args.get("tlv4_val");
			short t = Short.parseShort(tlv4_tag);
			short ln = Short.parseShort(tlv4_len);
			byte[] val = getValueBytes(args.get("tlv4_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv5_tag")) {
			tlv5_tag = args.get("tlv5_tag");
			tlv5_len = args.get("tlv5_len");
			tlv5_val = args.get("tlv5_val");
			short t = Short.parseShort(tlv5_tag);
			short ln = Short.parseShort(tlv5_len);
			byte[] val = getValueBytes(args.get("tlv5_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv6_tag")) {
			tlv6_tag = args.get("tlv6_tag");
			tlv6_len = args.get("tlv6_len");
			tlv6_val = args.get("tlv6_val");
			short t = Short.parseShort(tlv6_tag);
			short ln = Short.parseShort(tlv6_len);
			byte[] val = getValueBytes(args.get("tlv6_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}
		if (args.containsKey("tlv7_tag")) {
			tlv7_tag = args.get("tlv7_tag");
			tlv7_len = args.get("tlv7_len");
			tlv7_val = args.get("tlv7_val");
			short t = Short.parseShort(tlv7_tag);
			short ln = Short.parseShort(tlv7_len);
			byte[] val = getValueBytes(args.get("tlv7_val"), ln);
			smppmsg.addVsop(t, ln, val);
		}

		return smppmsg;
	}

	private byte[] getValueBytes(String value_hex, short ln) throws InvalidHexStringlException {
		byte[] val = new byte[0];
		if (ln > 0) {
			val = makeBinaryMessage(value_hex);
		}
		return val;
	}

	private byte[] makeBinaryMessage(String hex)
			throws InvalidHexStringlException {
		int i = 0;
		String hexNoSpaces = Utilities.removeSpaces(hex);
		hexNoSpaces = hexNoSpaces.toUpperCase();
		int l = hexNoSpaces.length();

		if (!Utilities.isEven(l)) {
			controlPanelMessage = "Error: Hex encoded short message contains an odd number of non-space characters";
			throw new InvalidHexStringlException("Invalid hex string");
		}

		if (!Utilities.validHexChars(hexNoSpaces)) {
			controlPanelMessage = "Error: Hex encoded short message contains non-hexadecimal characters";
			throw new InvalidHexStringlException("Invalid hex string");
		}
		l = l / 2;
		byte[] result = new byte[l];
		while (i < l) {
			String byteAsHex = hexNoSpaces.substring((i * 2), ((i * 2) + 2));
			int b = (int) (Integer.parseInt(byteAsHex, 16) & 0x000000FF);
			if (b < 0)
				b = 256 + b;
			result[i] = (byte) b;
			i++;
		}
		return result;

	}

	private byte[] plainResponse(File requestedFile) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		if (requestedFile.exists()) {
			int fileLen = (int) requestedFile.length();
			BufferedInputStream fileIn = new BufferedInputStream(
					new FileInputStream(requestedFile));
			String contentType = URLConnection
					.guessContentTypeFromStream(fileIn);
			logger.finest("Content Type thought to be:" + contentType);
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", fileLen,
					contentType);
			bOut.write(headerBytes);
			byte[] buf = new byte[2048];
			int blockLen = 0;
			while ((blockLen = fileIn.read(buf)) != -1) {
				bOut.write(buf, 0, blockLen);
			}
			fileIn.close();
		} else {
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 404 Not Found",
					-1, null);
			bOut.write(headerBytes);
		}
		bOut.flush();
		bOut.close();
		return bOut.toByteArray();
	}

	private String readRequest(BufferedReader is) throws Exception {
		String requestLine = is.readLine();
		if ((requestLine == null) || (requestLine.length() < 1)) {
			throw new IOException("could not read request");
		}
		logger.finest("Received HTTP message:" + requestLine);
		StringTokenizer st = new StringTokenizer(requestLine);
		String uri = null;

		try {
			// request method, typically 'GET', but ignored
			st.nextToken();
			// the second token should be the uri
			uri = st.nextToken();
		} catch (NoSuchElementException x) {
			throw new IOException("could not parse request line");
		}
		logger.finest("uri=" + uri);
		return uri;
	}

	private void writeResponse(byte[] response, OutputStream os)
			throws IOException {
		os.write(response);
	}

	private void closeConnection(Socket socket, OutputStream os)
			throws IOException {
		os.flush();
		os.close();
		socket.close();
	}

	/**
	 * @param b
	 */
	public void setRunning(boolean b) {
		running = b;
	}

	private int leastOf(int a, int b, int c, int d, int e) {
		if (a < b && a < c && a < d && a < e)
			return a;
		if (b < a && b < c && b < d && b < e)
			return b;
		if (c < a && c < b && c < d && c < e)
			return c;
		if (d < a && d < b && d < c && d < e)
			return d;
		return e;
	}

	private byte[] createHeaderBytes(String resp, int contentLen,
			String contentType) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

		// Write the first line of the response, followed by
		// the RFC-specified line termination sequence.
		writer.write(resp + "\r\n");

		// If a length was specified, add it to the header
		if (contentLen != -1) {
			writer.write("Content-Type: " + contentType + "\r\n");
		}

		// If a type was specified, add it to the header
		if (contentType != null) {
			writer.write("Content-Type: " + contentType + "\r\n");
		}

		// A blank line is required after the header.
		writer.write("\r\n");
		writer.flush();
		byte[] data = baos.toByteArray();
		writer.close();

		return data;
	}
}