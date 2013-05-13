

/****************************************************************************
 * StandardProtocolHandler.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/StandardProtocolHandler.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.*;
import com.seleniumsoftware.SMPPSim.pdu.*;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;
import com.seleniumsoftware.SMPPSim.util.*;
import org.apache.regexp.*;
import org.slf4j.LoggerFactory;

public class StandardProtocolHandler {
//    private volatile long counter = 0;
    
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
//	 Logger logger = Logger.getLogger(this.getClass().getName());

	Smsc smsc = Smsc.getInstance();

	InboundQueue iqueue = InboundQueue.getInstance();

	Session session = new Session();

	StandardConnectionHandler connection;

	boolean wasUnbindRequest = false;

	boolean wasBindReceiverRequest = false;

	boolean wasInvalidBindState = false;

	boolean failedAuthentication = false;

	byte[] response;

	byte[] packetLen = new byte[4];

	byte[] message;

	RE address_range_regexp = null;

	public StandardProtocolHandler() {
	}

	public String getName() {
		return "StandardProtocolHandler";
	}

	public StandardProtocolHandler(StandardConnectionHandler connection) {
		this.connection = connection;
	}

	static final int getBytesAsInt(byte i_byte) {
		return i_byte & 0xff;
	}

	void processMessage(byte[] message) throws Exception {

		int len = 0;
		int cmd = 0;
		int status = 0;
		if (SMPPSim.isCallback() && smsc.isCallback_server_online())
			smsc.received(message);
		len = PduUtilities.getIntegerValue(message, 0, 4);
		cmd = PduUtilities.getIntegerValue(message, 4, 4);
		status = PduUtilities.getIntegerValue(message, 8, 4);
		wasUnbindRequest = false;
		wasBindReceiverRequest = false;
		wasInvalidBindState = false;
		failedAuthentication = false;

		switch (cmd) {
		case PduConstants.BIND_TRANSMITTER:
			getBindTRNResponse(message, len);
			break;
		case PduConstants.BIND_RECEIVER:
			getBindRCVResponse(message, len);
			break;
		case PduConstants.BIND_TRANSCEIVER:
			getBindTRNCVRResponse(message, len);
			break;
		case PduConstants.SUBMIT_SM:
			getSubmitSMResponse(message, len);
			break;
		case PduConstants.DELIVER_SM_RESP:
			processDeliver_SM_Resp(message, len);
			break;
		case PduConstants.SUBMIT_MULTI:
			getSubmitMultiResponse(message, len);
			break;
		case PduConstants.QUERY_SM:
			getQueryResponse(message, len);
			break;
		case PduConstants.CANCEL_SM:
			getCancelSMResponse(message, len);
			break;
		case PduConstants.REPLACE_SM:
			getReplaceSMResponse(message, len);
			break;
		case PduConstants.ENQUIRE_LINK:
			getEnquireResponse(message, len);
			break;
		case PduConstants.UNBIND:
			getUnbindResponse(message, len);
			break;
		case PduConstants.GENERIC_NAK:
			logger.debug(": Received GENERIC_NAK:");
			getGenericNakResponse(message, len, false);
			break;
		case PduConstants.DATA_SM:
			getDataSMResponse(message, len);
			break;
		case PduConstants.DATA_SM_RESP:
			processData_SM_Resp(message, len);
			break;
		default:
			logger.debug(": Received unrecognised message:");
			LoggingUtilities.hexDump(": UNRECOGNISED MESSAGE:", message, len);
			getGenericNakResponse(message, len, true);
		}

		if ((wasUnbindRequest()) || (wasInvalidBindState())
				|| (failedAuthentication())) {
			logger.debug("closing connection");
			getSession().setBound(false);
			connection.closeConnection();
		}

		if (wasBindReceiverRequest()) {
			smsc.setMoServiceRunning();
			smsc.getIq().notifyReceiverBound();
		}

	}

	void getBindTRNResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": BIND_TRANSMITTER:", message, len);

		byte[] resp_message;

		// make the request object and populate it through demarshalling
		BindTransmitter smppmsg = new BindTransmitter();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
		logger.info(" ");
		// now make the response object
		BindTransmitterResp smppresp = new BindTransmitterResp(smppmsg,
				new String(smsc.getSMSC_SYSTEMID()));

		// Validate the session state
		if (session.isBound()) {
			logger.debug("Session is already bound");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": BIND_TRANSMITTER (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incBindTransmitterERR();
			return;
		}
		
		session.setInterface_version(smppmsg.getInterface_version());

		// Authenticate the account details
		if (smsc.authenticate(smppmsg.getSystem_id(), smppmsg.getPassword())) {
			session.setBound(true);
			session.setTransmitter(true);
			session.setReceiver(false);
			failedAuthentication = false;
			logger.info("New transmitter session bound to SMPPSim");
		} else {
			logger.debug("Bind failed authentication check.");
			smsc.incBindTransmitterERR();
			failedAuthentication = true;
			if (smsc.isValidSystemId(smppmsg.getSystem_id())) {
				smppresp.setCmd_status(PduConstants.ESME_RINVPASWD);
			} else {
				smppresp.setCmd_status(PduConstants.ESME_RINVSYSID);
			}
			session.setBound(false);
			session.setTransmitter(false);
			session.setReceiver(false);
		}

		// ....and turn it back into a byte array

		if (!failedAuthentication)
			resp_message = smppresp.marshall();
		else
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					smppresp.getCmd_status(), smppresp.getSeq_no());

		LoggingUtilities.hexDump(": BIND_TRANSMITTER_RESP:", resp_message,
				resp_message.length);

		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);

		logger.info(" ");
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
		if (!failedAuthentication) {
			smsc.incTxBoundCount();
			smsc.incBindTransmitterOK();
		}
	}

	void getBindRCVResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": BIND_RECEIVER:", message, len);
		byte[] resp_message;
		// make the request object and populate it through demarshalling
		BindReceiver smppmsg = new BindReceiver();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
		logger.info(" ");

		// now make the response object
		BindReceiverResp smppresp = new BindReceiverResp(smppmsg, new String(
				smsc.getSMSC_SYSTEMID()));

		// Validate the session state
		if (session.isBound()) {
			logger.debug("Session is already bound");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": BIND_RECEIVER (ESME_RINVBNDSTS):", resp_message, smppresp);
			smsc.incBindReceiverERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		session.setInterface_version(smppmsg.getInterface_version());

		// Authenticate the account details
		if (smsc.authenticate(smppmsg.getSystem_id(), smppmsg.getPassword())) {
			session.setBound(true);
			session.setTransmitter(false);
			session.setReceiver(true);
			failedAuthentication = false;
			wasBindReceiverRequest = true;
			// As this is a bind_receiver object we need to set the regular
			// expression attribute
			try {
				setAddressRangeRegExp(smppmsg.getAddress_range());
			} catch (RESyntaxException e) {
				logger.debug("Invalid regular expression specified in BIND_RECEIVER address_range attribute");
				e.printStackTrace();
			}
			logger.info("New receiver session bound to SMPPSim");
		} else {
			logger.debug("Bind failed authentication check.");
			failedAuthentication = true;
			if (smsc.isValidSystemId(smppmsg.getSystem_id())) {
				smppresp.setCmd_status(PduConstants.ESME_RINVPASWD);
			} else {
				smppresp.setCmd_status(PduConstants.ESME_RINVSYSID);
			}
			session.setBound(false);
			session.setTransmitter(false);
			session.setReceiver(false);
		}

		// ....and turn it back into a byte array

		if (!failedAuthentication) {
			resp_message = smppresp.marshall();
			smsc.incBindReceiverOK();
			smsc.incRxBoundCount();
		} else {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					smppresp.getCmd_status(), smppresp.getSeq_no());
			smsc.incBindReceiverERR();
		}

		LoggingUtilities.hexDump(": BIND_RECEIVER_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
		if (!failedAuthentication)
			smsc.getIq().deliverPendingMoMessages();

	}

	void getBindTRNCVRResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": BIND_TRANSCEIVER:", message, len);
		byte[] resp_message;
		// make the request object and populate it through demarshalling
		BindTransceiver smppmsg = new BindTransceiver();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object
		BindTransceiverResp smppresp = new BindTransceiverResp(smppmsg,
				new String(smsc.getSMSC_SYSTEMID()));

		// Validate the session state
		if (session.isBound()) {
			logger.debug("Session is already bound");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RALYBND, smppresp.getSeq_no());
			logPdu(": BIND_TRANSCEIVER (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incBindTransceiverERR();
			return;
		}

		session.setInterface_version(smppmsg.getInterface_version());

		// Authenticate the account details
		if (smsc.authenticate(smppmsg.getSystem_id(), smppmsg.getPassword())) {
			session.setBound(true);
			session.setTransmitter(true);
			session.setReceiver(true);
			failedAuthentication = false;
			wasBindReceiverRequest = true;
			// As this is a bind_transceiver object we need to set the regular
			// expression attribute
			try {
				setAddressRangeRegExp(smppmsg.getAddress_range());
			} catch (RESyntaxException e) {
				logger.debug("Invalid regular expression specified in BIND_TRANSCEIVER address_range attribute");
				e.printStackTrace();
			}
			logger.info("New transceiver session bound to SMPPSim");
		} else {
			logger.debug("Bind failed authentication check.");
			failedAuthentication = true;
			if (smsc.isValidSystemId(smppmsg.getSystem_id())) {
				smppresp.setCmd_status(PduConstants.ESME_RINVPASWD);
			} else {
				smppresp.setCmd_status(PduConstants.ESME_RINVSYSID);
			}
			session.setBound(false);
			session.setTransmitter(false);
			session.setReceiver(false);
		}

		// ....and turn it back into a byte array

		if (!failedAuthentication) {
			resp_message = smppresp.marshall();
			smsc.incTrxBoundCount();
			smsc.incBindTransceiverOK();
		} else {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					smppresp.getCmd_status(), smppresp.getSeq_no());
			smsc.incBindTransceiverERR();
		}

		LoggingUtilities.hexDump(": BIND_TRANSCEIVER_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
		if (!failedAuthentication)
			smsc.getIq().deliverPendingMoMessages();
	}

	void getSubmitSMResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": Standard SUBMIT_SM:", message, len);
		byte[] resp_message;
		SubmitSM smppmsg = new SubmitSM();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");
//             logger.info("OP: {}, CP:{}, MSG:{}, COUNTER: {}", new Object[]{smppmsg.getSource_addr(), smppmsg.getDestination_addr(), new String(smppmsg.getShort_message()), ++counter}); 
                ++Counter.counter; // count the number of events that SMPPSim received 
                
		// now make the response object

		SubmitSMResp smppresp = new SubmitSMResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(":SUBMIT_SM_RESP (ESME_RINVBNDSTS):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incSubmitSmERR();
			return;
		}

		// Validation

		if (smppmsg.getDestination_addr().equals("")) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVDSTADR, smppresp.getSeq_no());
			logPdu(":SUBMIT_SM_RESP (ESME_RINVDSTADR):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incSubmitSmERR();
			return;
		}

		// Try to add to the OutboundQueue for lifecycle tracking
		MessageState m = null;
		try {
			m = new MessageState(smppmsg, smppresp.getMessage_id());
			smsc.getOq().addMessageState(m);
		} catch (OutboundQueueFullException e) {
			//logger.debug("OutboundQueue full.");
//			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
//					PduConstants.ESME_RMSGQFUL, smppresp.getSeq_no());
//			logPdu(":SUBMIT_SM_RESP (ESME_RMSGQFUL):", resp_message, smppresp);
//			smsc.incSubmitSmERR();
//			connection.writeResponse(resp_message);
//			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}
		// ....and turn it back into a byte array
		resp_message = smppresp.marshall();

		logPdu(":SUBMIT_SM_RESP:", resp_message, smppresp);
		//logger.info(" ");

		connection.writeResponse(resp_message);
		//logger.info("SubmitSM processing - response written to connection");
		smsc.writeDecodedSmppsim(smppresp.toString());
		// set messagestate responsesent = true
		smsc.getOq().setResponseSent(m);
		smsc.incSubmitSmOK();

		// If loopback is switched on, have an SMPPReceiver object deliver this
		// message back to the client
		if (SMPPSim.isLoopback()) {
			try {
				smsc.doLoopback(smppmsg);
			} catch (InboundQueueFullException e) {
				logger
						.debug("Failed to create loopback DELIVER_SM because the Inbound Queue is full");
			}
		} else {
			if (SMPPSim.isEsme_to_esme()) {
				try {
					smsc.doEsmeToEsmeDelivery(smppmsg);
				} catch (InboundQueueFullException e) {
					logger
							.debug("Failed to create ESME to ESME DELIVER_SM because the Inbound Queue is full");
				}
			}
		}
	}

	void logPdu(String label, byte[] message, Pdu pdu) {
		LoggingUtilities.hexDump(label, message, message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(pdu);
	}

	void getSubmitMultiResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": Standard SUBMIT_MULTI:", message, len);
		byte[] resp_message;
		SubmitMulti smppmsg = new SubmitMulti();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object

		SubmitMultiResp smppresp = new SubmitMultiResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger
					.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": Standard SUBMIT_MULTI (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incSubmitMultiERR();
			return;
		}

		// Validation

		if (smppmsg.getSource_addr().equals("")) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVSRCADR, smppresp.getSeq_no());
			logPdu(": Standard SUBMIT_MULTI (ESME_RINVSRCADR):", resp_message,
					smppresp);
			smsc.incSubmitMultiERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		if (smppmsg.getNumber_of_dests() < 1) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVNUMDESTS, smppresp.getSeq_no());
			logPdu(": Standard SUBMIT_MULTI (ESME_RINVNUMDESTS):",
					resp_message, smppresp);
			smsc.incSubmitMultiERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// ....and turn it back into a byte array
		resp_message = smppresp.marshall();

		LoggingUtilities.hexDump(":SUBMIT_MULTI_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incSubmitMultiOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getUnbindResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": UNBIND:", message, len);
		Unbind smppmsg = new Unbind();
		byte[] resp_message;
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");
		// now make the response object
		UnbindResp smppresp = new UnbindResp(smppmsg);

		// Validate session
		if (!session.isBound()) {
			logger.debug("Invalid bind state. Must be bound for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": UNBIND (ESME_RINVBNDSTS):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incUnbindERR();
			return;
		}

		// ....and turn it back into a byte array

		resp_message = smppresp.marshall();

		if (session.isReceiver()) {
			smsc.receiverUnbound();
		}
		logger.debug("Receiver:" + session.isReceiver() + ",Transmitter:"
				+ session.isTransmitter());
		if (session.isReceiver() && session.isTransmitter())
			smsc.setTrxBoundCount(smsc.getTrxBoundCount() - 1);
		else if (session.isReceiver())
			smsc.setRxBoundCount(smsc.getRxBoundCount() - 1);
		else {
			smsc.setTxBoundCount(smsc.getTxBoundCount() - 1);
		}
		session.setBound(false);
		session.setReceiver(false);
		session.setTransmitter(false);
		wasUnbindRequest = true;
		LoggingUtilities.hexDump(": UNBIND_RESP", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incUnbindOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getQueryResponse(byte[] message, int len) throws Exception {

		LoggingUtilities.hexDump(": QUERY_SM:", message, len);
		QuerySM smppmsg = new QuerySM();
		byte[] resp_message;
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object

		QuerySMResp smppresp = new QuerySMResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger
					.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": QUERY_SM (ESME_RINVBNDSTS):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incQuerySmERR();
			return;
		}

		// Retrieve and set the message state
		try {
			smppresp = smsc.querySm(smppmsg, smppresp);
		} catch (MessageStateNotFoundException e) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RQUERYFAIL, smppresp.getSeq_no());
			logPdu(": QUERY_SM_RESP (ESME_RQUERYFAIL):", resp_message, smppresp);
			smsc.incQuerySmERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}
		// ....and turn it back into a byte array

		resp_message = smppresp.marshall();
		LoggingUtilities.hexDump(":QUERY_SM_RESP:", resp_message,
				resp_message.length);

		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incQuerySmOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getCancelSMResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": CANCEL_SM:", message, len);
		CancelSM smppmsg = new CancelSM();
		byte[] resp_message;
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object

		CancelSMResp smppresp = new CancelSMResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger
					.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": CANCEL_SM_RESP (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			smsc.incCancelSmERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// Retrieve and set the message state
		try {
			smppresp = smsc.cancelSm(smppmsg, smppresp);
		} catch (MessageStateNotFoundException e) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RCANCELFAIL, smppresp.getSeq_no());
			logPdu(": CANCEL_SM_RESP (ESME_RCANCELFAIL):", resp_message,
					smppresp);
			smsc.incCancelSmERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// ....and turn it back into a byte array

		resp_message = smppresp.marshall();
		LoggingUtilities.hexDump(":CANCEL_SM_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incCancelSmOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getReplaceSMResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": REPLACE_SM:", message, len);
		ReplaceSM smppmsg = new ReplaceSM();
		byte[] resp_message;
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object

		ReplaceSMResp smppresp = new ReplaceSMResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger
					.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": REPLACE_SM_RESP (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			smsc.incReplaceSmERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// Update the original message in the OutboundQueue (if it is still
		// there)
		try {
			smppresp = smsc.replaceSm(smppmsg, smppresp);
		} catch (MessageStateNotFoundException e) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RREPLACEFAIL, smppresp.getSeq_no());
			logPdu(": REPLACE_SM_RESP (ESME_RREPLACEFAIL):", resp_message,
					smppresp);
			smsc.incReplaceSmERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// ....and turn it back into a byte array
		resp_message = smppresp.marshall();
		LoggingUtilities.hexDump(":REPLACE_SM_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incReplaceSmOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getEnquireResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": ENQUIRE_LINK:", message, len);
		EnquireLink smppmsg = new EnquireLink();
		byte[] resp_message;
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");
		// now make the response object
		EnquireLinkResp smppresp = new EnquireLinkResp(smppmsg);

		// Validate session
		if (!session.isBound()) {
			logger.debug("Invalid bind state. Must be bound for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(": ENQUIRE_LINK_RESP (ESME_RINVBNDSTS):", resp_message,
					smppresp);
			smsc.incEnquireLinkERR();
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			return;
		}

		// ....and turn it back into a byte array

		resp_message = smppresp.marshall();
		LoggingUtilities.hexDump(":ENQUIRE_LINK_RESP:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
//		logger.info(" ");
		smsc.incEnquireLinkOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void getDataSMResponse(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": Standard DATA_SM:", message, len);
		byte[] resp_message;
		DataSM smppmsg = new DataSM();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");

		// now make the response object

		DataSMResp smppresp = new DataSMResp(smppmsg);

		// Validate session
		if ((!session.isBound()) || (!session.isTransmitter())) {
			logger
					.debug("Invalid bind state. Must be bound as transmitter for this PDU");
			wasInvalidBindState = true;
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVBNDSTS, smppresp.getSeq_no());
			logPdu(":DATA_SM_RESP (ESME_RINVBNDSTS):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incDataSmERR();
			return;
		}

		// Validation

		if (smppmsg.getSource_addr().equals("")) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVSRCADR, smppresp.getSeq_no());
			logPdu(":DATA_SM_RESP (ESME_RINVSRCADR):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incDataSmERR();
			return;
		}

		if (smppmsg.getDestination_addr().equals("")) {
			resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
					PduConstants.ESME_RINVDSTADR, smppresp.getSeq_no());
			logPdu(":DATA_SM_RESP (ESME_RINVDSTADR):", resp_message, smppresp);
			connection.writeResponse(resp_message);
			smsc.writeDecodedSmppsim(smppresp.toString());
			smsc.incDataSmERR();
			return;
		}

		// Try to add to the OutboundQueue for lifecycle tracking
		// MessageState m = null;
		// try {
		// m = new MessageState(smppmsg, smppresp.getMessage_id());
		// smsc.getOq().addMessageState(m);
		// } catch (OutboundQueueFullException e) {
		// logger.debug("OutboundQueue full.");
		// resp_message = smppresp.errorResponse(smppresp.getCmd_id(),
		// PduConstants.ESME_RMSGQFUL, smppresp.getSeq_no());
		// logPdu(":DATA_SM_RESP (ESME_RMSGQFUL):", resp_message, smppresp);
		// smsc.incDataSmERR();
		// connection.writeResponse(resp_message);
		// smsc.writeDecodedSmppsim(smppresp.toString());
		// return;
		// }
		// ....and turn it back into a byte array
		resp_message = smppresp.marshall();

		logPdu(":DATA_SM_RESP:", resp_message, smppresp);
//		logger.info(" ");

		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
		// set messagestate responsesent = true
		// smsc.getOq().setResponseSent(m);
		smsc.incDataSmOK();

		// If loopback is switched on, have an SMPPReceiver object deliver this
		// message back to the client
		if (SMPPSim.isLoopback()) {
			try {
				smsc.doLoopback(smppmsg);
			} catch (InboundQueueFullException e) {
				//logger.debug("Failed to create loopback DELIVER_SM because the Inbound Queue is full");
			}
		}
	}

	void getGenericNakResponse(byte[] message, int len, boolean unrecognised)
			throws Exception {
		byte[] resp_message;
		logger.info(": GENERIC_NAK");
		LoggingUtilities.hexDump("Message warranting GENERIC_NAK response:",
				message, len);
		GenericNak smppmsg = new GenericNak();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
//		logger.info(" ");
		// now make the response object
		GenericNakResp smppresp = new GenericNakResp(smppmsg);

		if (unrecognised)
			smppresp.setCmd_status(PduConstants.ESME_RINVCMDID);

		// ....and turn it back into a byte array

		resp_message = smppresp.marshall();
		LoggingUtilities.hexDump(": returning GENERIC_NAK:", resp_message,
				resp_message.length);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppresp);
		logger.info(" ");
		smsc.incEnquireLinkOK();
		connection.writeResponse(resp_message);
		smsc.writeDecodedSmppsim(smppresp.toString());
	}

	void setAddressRangeRegExp(String address_range) throws RESyntaxException {
		//logger.info("StandardProtocolHandler: setting address range to "+ address_range);
		if (address_range == null || address_range.equals("*") || address_range.equals(""))
			address_range = "[:alnum:]*";
		address_range_regexp = new RE(address_range);
		logger.info("Made RE for " + address_range);
	}

	void processDeliver_SM_Resp(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": DELIVER_SM_RESP:", message, len);
		// turn into an object just in case we decide to do anything with it
		DeliverSMResp smppmsg = new DeliverSMResp();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
		if (smppmsg.getCmd_status() == 0)
			smsc.incDeliverSmOK();
		else
			smsc.incDeliverSmERR();
		
		iqueue.deliveryResult(smppmsg.getSeq_no(), smppmsg.getCmd_status());
		
//		logger.info(" ");
	}

	void processData_SM_Resp(byte[] message, int len) throws Exception {
		LoggingUtilities.hexDump(": DATA_SM_RESP:", message, len);
		// turn into an object just in case we decide to do anything with it
		DataSMResp smppmsg = new DataSMResp();
		smppmsg.demarshall(message);
		if (smsc.isDecodePdus())
			LoggingUtilities.logDecodedPdu(smppmsg);
		smsc.writeDecodedSme(smppmsg.toString());
		logger.info(" ");
	}

	public boolean addressIsServicedByReceiver(String address) {
		boolean result;
		if (address.equals("") || address.equals(" ")) {
			return true;
		} else {
			//logger.info("addressIsServicedByReceiver(" + address + ")");
			result = address_range_regexp.match(address);
			return result;
		}
	}

	/**
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @param handler
	 */
	public void setConnection(StandardConnectionHandler handler) {
		connection = handler;
	}

	/**
	 * @return
	 */
	public boolean wasBindReceiverRequest() {
		return wasBindReceiverRequest;
	}

	/**
	 * @return
	 */
	public boolean wasInvalidBindState() {
		return wasInvalidBindState;
	}

	/**
	 * @return
	 */
	public boolean wasUnbindRequest() {
		return wasUnbindRequest;
	}

	/**
	 * @param b
	 */
	public void setWasBindReceiverRequest(boolean b) {
		wasBindReceiverRequest = b;
	}

	/**
	 * @param b
	 */
	public void setWasInvalidBindState(boolean b) {
		wasInvalidBindState = b;
	}

	/**
	 * @param b
	 */
	public void setWasUnbindRequest(boolean b) {
		wasUnbindRequest = b;
	}

	/**
	 * @return
	 */
	public boolean failedAuthentication() {
		return failedAuthentication;
	}

	/**
	 * @param b
	 */
	public void setFailedAuthentication(boolean b) {
		failedAuthentication = b;
	}

}
