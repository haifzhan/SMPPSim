/****************************************************************************
 * smsc.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/Smsc.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.*;
import com.seleniumsoftware.SMPPSim.pdu.*;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;
import com.seleniumsoftware.SMPPSim.util.*;

import java.util.*;
import java.text.*;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.*;
import org.slf4j.LoggerFactory;

public class Smsc {

	private static Smsc smsc;

	private static Socket callback;

	private static OutputStream callback_stream;

	private static boolean callback_server_online = false;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(OutboundQueue.class);
//	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	private static long message_id = 0;

	private static int sequence_no = 0;

	private static byte[] SMSC_SYSTEMID;

	private static boolean decodePdus;

	private int receiverIndex = 0;

	private StandardConnectionHandler[] connectionHandlers;

	private ServerSocket smpp_ss;

	private HttpHandler[] httpcontrollers;

	private ServerSocket css;

	private MoService ds;

	private Thread deliveryService;

	private InboundQueue iq;

	private OutboundQueue oq;

	private DelayedDrQueue drq;

	private LifeCycleManager lcm;

	private Thread lifecycleService;

	private Thread inboundQueueService;

	private int inbound_queue_capacity = 100;

	private int outbound_queue_capacity = 20000;

	// PDU capture

	private File smeBinaryFile;

	private FileOutputStream smeBinary;

	private File smppsimBinaryFile;

	private FileOutputStream smppsimBinary;

	private File smeDecodedFile;

	private FileWriter smeDecoded;

	private File smppsimDecodedFile;

	private FileWriter smppsimDecoded;

	// Stats

	private Date startTime;

	private String startTimeString;

	private int txBoundCount = 0;

	private int rxBoundCount = 0;

	private int trxBoundCount = 0;

	private long bindTransmitterOK = 0;

	private long bindTransceiverOK = 0;

	private long bindReceiverOK = 0;

	private long bindTransmitterERR = 0;

	private long bindTransceiverERR = 0;

	private long bindReceiverERR = 0;

	private long submitSmOK = 0;

	private long submitSmERR = 0;

	private long submitMultiOK = 0;

	private long submitMultiERR = 0;

	private long deliverSmOK = 0;

	private long deliverSmERR = 0;

	private long querySmOK = 0;

	private long querySmERR = 0;

	private long cancelSmOK = 0;

	private long cancelSmERR = 0;

	private long replaceSmOK = 0;

	private long replaceSmERR = 0;

	private long enquireLinkOK = 0;

	private long enquireLinkERR = 0;

	private long unbindOK = 0;

	private long unbindERR = 0;

	private long genericNakOK = 0;

	private long genericNakERR = 0;

	private long dataSmOK = 0;

	private long dataSmERR = 0;
	
	private long outbindOK = 0;
	
	private long outbindERR = 0;
	
	// outbind
	
	boolean outbind_sent = false;
	
	private Smsc() {
	}

	public static Smsc getInstance() {
		if (smsc == null)
			smsc = new Smsc();
		return smsc;
	}

	public void start() throws Exception {

		startTime = new Date();
		SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		startTimeString = df.format(startTime);
		
		message_id = SMPPSim.getStart_at();

		if (SMPPSim.isCallback()) {
			connectToCallbackServer(new Object());
		}

		if (SMPPSim.isCaptureSmeBinary()) {
			System.out.println("Creating smeBinaryCapture file");
			smeBinaryFile = new File(SMPPSim.getCaptureSmeBinaryToFile());
			smeBinaryFile.delete();
			System.out.println(smeBinaryFile.createNewFile());
			smeBinary = new FileOutputStream(smeBinaryFile);
		}

		if (SMPPSim.isCaptureSmppsimBinary()) {
			smppsimBinaryFile = new File(SMPPSim
					.getCaptureSmppsimBinaryToFile());
			smppsimBinaryFile.delete();
			smppsimBinaryFile.createNewFile();
			smppsimBinary = new FileOutputStream(smppsimBinaryFile);
		}

		if (SMPPSim.isCaptureSmeDecoded()) {
			smeDecodedFile = new File(SMPPSim.getCaptureSmeDecodedToFile());
			smeDecodedFile.delete();
			smeDecodedFile.createNewFile();
			smeDecoded = new FileWriter(smeDecodedFile);
		}

		if (SMPPSim.isCaptureSmppsimDecoded()) {
			smppsimDecodedFile = new File(SMPPSim
					.getCaptureSmppsimDecodedToFile());
			smppsimDecodedFile.delete();
			smppsimDecodedFile.createNewFile();
			smppsimDecoded = new FileWriter(smppsimDecodedFile);
		}

		iq = InboundQueue.getInstance();

		Class cl = Class.forName(SMPPSim.getLifeCycleManagerClassName());
		lcm = (LifeCycleManager) cl.newInstance();

		oq = new OutboundQueue(outbound_queue_capacity);

		Thread smppThread[] = new Thread[SMPPSim.getMaxConnectionHandlers()];
		int threadIndex = 0;
		connectionHandlers = new StandardConnectionHandler[SMPPSim
				.getMaxConnectionHandlers()];
		try {
			smpp_ss = new ServerSocket(SMPPSim.getSmppPort(), 10);
		} catch (Exception e) {
			logger.debug("Exception creating SMPP server: " + e.toString());
			e.printStackTrace();
			throw e;
		}
		for (int i = 0; i < SMPPSim.getMaxConnectionHandlers(); i++) {
			Class c = Class.forName(SMPPSim.getConnectionHandlerClassName());
			StandardConnectionHandler ch = (StandardConnectionHandler) c
					.newInstance();
			ch.setSs(smpp_ss);
			connectionHandlers[threadIndex] = ch;
			smppThread[threadIndex] = new Thread(
					connectionHandlers[threadIndex], "CH" + threadIndex);
			smppThread[threadIndex].start();
			threadIndex++;
		}

		try {
			css = new ServerSocket(SMPPSim.getHTTPPort(), 10);
		} catch (Exception e) {
			logger.error("Exception creating HTTP server: " + e.toString());
			e.printStackTrace();
		}
		Thread cthread[] = new Thread[SMPPSim.getHTTPThreads()];
		httpcontrollers = new HttpHandler[SMPPSim.getHTTPThreads()];
		for (int i = 0; i < SMPPSim.getHTTPThreads(); i++) {
			httpcontrollers[i] = new HttpHandler(new File(SMPPSim.getDocroot()));
			cthread[i] = new Thread(httpcontrollers[i], "HC" + i);
			cthread[i].start();
		}
		if (SMPPSim.getDeliverMessagesPerMin() != 0) {
			ds = new MoService(SMPPSim.getDeliverFile(), SMPPSim
					.getDeliverMessagesPerMin());
		}

		// InboundQueue must always be running to allow for MO messages injected
		// via web interface
		inboundQueueService = new Thread(iq);
		inboundQueueService.start();

		// LifeCycleService (OutboundQueue) must always be running
		lifecycleService = new Thread(oq);
		lifecycleService.start();
		
		if (SMPPSim.getDelayReceiptsBy() > 0) {
			logger.info("Starting delivery receipts delay service....");
			drq = DelayedDrQueue.getInstance();
			Thread t = new Thread(drq);
			t.start();
		}
	}

	public boolean authenticate(String systemid, String password) {
		
		for (int i=0;i<SMPPSim.getSystemids().length;i++) {
			if (SMPPSim.getSystemids()[i].equals(systemid))
				if (SMPPSim.getPasswords()[i].equals(password))
					return true;
				else
					return false;
		}
		return false;		
	}

	public boolean isValidSystemId(String systemid) {
		
		for (int i=0;i<SMPPSim.getSystemids().length;i++) {
			if (SMPPSim.getSystemids()[i].equals(systemid))
				return true;
		}
		return false;		
	}

	public void connectToCallbackServer(Object mutex) {
		CallbackServerConnector cbs = new CallbackServerConnector(mutex);
		Thread cbst = new Thread(cbs);
		cbst.start();
	}

	public synchronized static String getMessageID() {
		long msgID = message_id++;
		String msgIDstr = SMPPSim.getMid_prefix()+Long.toString(msgID);
		return msgIDstr;
	}

	public static int getNextSequence_No() {
		sequence_no++;
		return sequence_no;
	}

	public QuerySMResp querySm(QuerySM q, QuerySMResp r)
			throws MessageStateNotFoundException {
		MessageState m = new MessageState();
		m = oq.queryMessageState(q.getOriginal_message_id(), q
				.getOriginating_ton(), q.getOriginating_npi(), q
				.getOriginating_addr());
		r.setMessage_state(m.getState());
		if (m.getFinalDate() != null)
			r.setFinal_date(m.getFinalDate().getDateString());
		else
			r.setFinal_date("");
		return r;
	}

	public CancelSMResp cancelSm(CancelSM q, CancelSMResp r)
			throws MessageStateNotFoundException, InternalException {
		MessageState m = new MessageState();
		// messageid specified
		if ((!q.getOriginal_message_id().equals(""))) {
			m = oq.queryMessageState(q.getOriginal_message_id(), q
					.getSource_addr_ton(), q.getSource_addr_npi(), q
					.getSource_addr());
			r.setSeq_no(q.getSeq_no());
			oq.removeMessageState(m);
			return r;
		}
		// messageid null (in PDU), service_type specified
		if ((q.getOriginal_message_id().equals(""))
				&& (!q.getService_type().equals(""))) {
			int c = cancelMessages(q.getService_type(), q.getSource_addr_ton(),
					q.getSource_addr_npi(), q.getSource_addr(), q
							.getDest_addr_ton(), q.getDest_addr_npi(), q
							.getDestination_addr());
			logger.info(c + " messages cancelled");
			r.setSeq_no(q.getSeq_no());
			return r;
		}
		// messageid null (in PDU), service_type null also
		if ((q.getOriginal_message_id().equals(""))
				&& (q.getService_type().equals(""))) {
			int c = cancelMessages(q.getSource_addr_ton(), q
					.getSource_addr_npi(), q.getSource_addr(), q
					.getDest_addr_ton(), q.getDest_addr_npi(), q
					.getDestination_addr());
			logger.info(c + " messages cancelled");
			r.setSeq_no(q.getSeq_no());
			return r;
		}
		logger
				.debug("Laws of physics violated. Well laws of logic anyway. Fell through conditions in smsc.cancelSm");
		logger.debug("Request is:" + q.toString());
		throw new InternalException(
				"Laws of physics violated. Well laws of logic anyway. Fell through conditions in smsc.cancelSm");
	}

	private int cancelMessages(String service_type, int source_addr_ton,
			int source_addr_npi, String source_addr, int dest_addr_ton,
			int dest_addr_npi, String destination_addr) {

		Object[] messages = oq.getAllMessageStates();
		MessageState m;
		int s = messages.length;
		int c = 0;
		for (int i = 0; i < s; i++) {
			m = (MessageState) messages[i];
			if (m.getPdu().getService_type().equals(service_type)
					&& m.getPdu().getSource_addr_ton() == source_addr_ton
					&& m.getPdu().getSource_addr_npi() == source_addr_npi
					&& m.getPdu().getSource_addr().equals(source_addr)
					&& m.getPdu().getDest_addr_ton() == dest_addr_ton
					&& m.getPdu().getDest_addr_npi() == dest_addr_npi
					&& m.getPdu().getDestination_addr()
							.equals(destination_addr)) {
				c++;
				oq.removeMessageState(m);
			}
		}
		return c;
	}

	private int cancelMessages(int source_addr_ton, int source_addr_npi,
			String source_addr, int dest_addr_ton, int dest_addr_npi,
			String destination_addr) {
		Object[] messages = oq.getAllMessageStates();
		MessageState m;
		int s = messages.length;
		int c = 0;
		for (int i = 0; i < s; i++) {
			m = (MessageState) messages[i];
			if (m.getPdu().getSource_addr_ton() == source_addr_ton
					&& m.getPdu().getSource_addr_npi() == source_addr_npi
					&& m.getPdu().getSource_addr().equals(source_addr)
					&& m.getPdu().getDest_addr_ton() == dest_addr_ton
					&& m.getPdu().getDest_addr_npi() == dest_addr_npi
					&& m.getPdu().getDestination_addr()
							.equals(destination_addr)) {
				c++;
				oq.removeMessageState(m);
			}
		}
		return c;
	}

	public ReplaceSMResp replaceSm(ReplaceSM q, ReplaceSMResp r)
			throws MessageStateNotFoundException {
		MessageState m = new MessageState();
		m = oq.queryMessageState(q.getMessage_id(), q.getSource_addr_ton(), q
				.getSource_addr_npi(), q.getSource_addr());
		SubmitSM pdu = m.getPdu();
		if (q.getSchedule_delivery_time() != null)
			pdu.setSchedule_delivery_time(q.getSchedule_delivery_time());
		if (q.getValidity_period() != null)
			pdu.setValidity_period(q.getValidity_period());
		pdu.setRegistered_delivery_flag(q.getRegistered_delivery_flag());
		pdu.setSm_default_msg_id(q.getSm_default_msg_id());
		pdu.setSm_length(q.getSm_length());
		pdu.setShort_message(q.getShort_message());
		m.setPdu(pdu);
		oq.updateMessageState(m);
		logger.info("MessageState replaced with " + m.toString());
		r.setSeq_no(q.getSeq_no());
		return r;
	}

	public synchronized void receiverUnbound() {
		SMPPSim.decrementBoundReceiverCount();
		SMPPSim.showReceiverCount();
		if (SMPPSim.getBoundReceiverCount() == 0) {
			stopMoService();
		}
	}

	public int getReceiverBoundCount() {
		return SMPPSim.getBoundReceiverCount();
	}

	public StandardConnectionHandler selectReceiver(String address) {
		boolean gotReceiver = false;
		int receiversChecked = 0;
		logger.debug("Smsc: selectReceiver");
		do {
			receiverIndex = getNextReceiverIndex();
			if ((connectionHandlers[receiverIndex].isBound())
					&& (connectionHandlers[receiverIndex].isReceiver())
					&& (connectionHandlers[receiverIndex]
							.addressIsServicedByReceiver(address))) {
				gotReceiver = true;
			} else {
				receiversChecked++;
			}
		} while ((!gotReceiver)
				&& (receiversChecked <= SMPPSim.getMaxConnectionHandlers()));

		logger.debug("Smsc: Using SMPPReceiver object #" + receiverIndex);
		if (gotReceiver) {
			return connectionHandlers[receiverIndex];
		} else {
			//logger.error("Smsc: No receiver for message address to "+ address);
			return null;
		}
	}

	public void doLoopback(SubmitSM smppmsg) throws InboundQueueFullException {
		DeliverSM newMessage = new DeliverSM(smppmsg);
		iq.addMessage(newMessage);
	}

	public void doLoopback(DataSM smppmsg) throws InboundQueueFullException {
		DataSM newMessage = new DataSM(smppmsg);
		iq.addMessage(newMessage);
	}

	public void doEsmeToEsmeDelivery(SubmitSM smppmsg) throws InboundQueueFullException {
		DeliverSM newMessage = new DeliverSM();
		newMessage.esmeToEsmeDerivation(smppmsg);
		iq.addMessage(newMessage);
	}

	public void outbind() {
		try {
			Socket s = new Socket(SMPPSim.getEsme_ip_address(),SMPPSim.getEsme_port());
			OutputStream out = s.getOutputStream();
			Outbind outbind = new Outbind(SMPPSim.getEsme_systemid(),SMPPSim.getEsme_password());
			byte [] outbind_bytes = outbind.marshall();
			LoggingUtilities.hexDump(": OUTBIND:", outbind_bytes, outbind_bytes.length);
			if (smsc.isDecodePdus())
				LoggingUtilities.logDecodedPdu(outbind);
			out.write(outbind_bytes);
			out.flush();
			out.close();
			s.close();
			outbindOK++;
			outbind_sent = true;
		} catch (Exception e) {
			logger.error("Attempted outbind failed. Check IP address and port are correct for outbind. Exception of type "+e.getClass().getName());
			outbindERR++;
		}
	}
	
	public synchronized void prepareDeliveryReceipt(SubmitSM smppmsg, String messageID, byte state, int sub, int dlvrd, int err) {
		int esm_class=4;
		if (state == PduConstants.ENROUTE) {
			esm_class = 32;
		}
		DeliveryReceipt receipt = new DeliveryReceipt(smppmsg,esm_class,messageID,state);
		Date rightNow = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
		String dateAsString = df.format(rightNow);
		receipt.setMessage_id(messageID);
		String s = "000" + sub;
		int l = s.length();
		receipt.setSub(s.substring(l - 3, l));
		s = "000" + dlvrd;
		l = s.length();
		receipt.setDlvrd(s.substring(l - 3, l));
		receipt.setSubmit_date(dateAsString);
		receipt.setDone_date(dateAsString);
		String err_string = "000" + err;
		err_string = err_string.substring(err_string.length()-3,err_string.length());
		receipt.setErr(err_string);
		
		if (SMPPSim.isDlr_tlr_required()) {
			receipt.addVsop(SMPPSim.getDlr_tlv_tag(), SMPPSim.getDlr_tlv_len(), SMPPSim.getDlr_tlv_value());
		}
		
		logger.debug("sm_len=" + smppmsg.getSm_length() + ",message="
				+ smppmsg.getShort_message());
		if (smppmsg.getSm_length() > 19)
			receipt.setText(new String(smppmsg.getShort_message(),0, 20));
		else
			if (smppmsg.getSm_length() > 0)
				receipt.setText(new String(smppmsg.getShort_message(),0,
						smppmsg.getSm_length()));
		receipt.setDeliveryReceiptMessage(state);
		try {
			if (SMPPSim.getDelayReceiptsBy() <= 0) {
				iq.addMessage(receipt);
			} else {
				drq.delayDeliveryReceipt(receipt);
			}
		} catch (InboundQueueFullException e) {
			//logger.error("Failed to create delivery receipt because the Inbound Queue is full");
		}
	}

	private synchronized int getNextReceiverIndex() {
		if (receiverIndex == (SMPPSim.getMaxConnectionHandlers() - 1)) {
			receiverIndex = 0;
		} else {
			receiverIndex++;
		}
		return receiverIndex;
	}

	public byte[] processDeliveryReceipt(DeliveryReceipt smppmsg)
			throws Exception {
		byte[] message;
		logger.debug(": DELIVER_SM (receipt)");
		message = smppmsg.marshall();
		LoggingUtilities.hexDump("DELIVER_SM (receipt):", message,
				message.length);
		return message;
	}

	public synchronized void setMoServiceRunning() {
		SMPPSim.incrementBoundReceiverCount();
		SMPPSim.showReceiverCount();
		if ((SMPPSim.getDeliverMessagesPerMin() > 0) && (!ds.moServiceRunning)) {
			ds.moServiceRunning = true;
			deliveryService = new Thread(ds, "MO");
			deliveryService.start();
		}
	}

	public void stopMoService() {
		if (ds != null) {
			if (ds.moServiceRunning) {
				logger.info("Stopping MO service");
				ds.moServiceRunning = false;
			}
		}
	}

	public void stop() {
		// TODO implement stop action
	}

	public void writeBinarySme(byte[] request) throws IOException {
		if (SMPPSim.isCaptureSmeBinary()) {
			smeBinary.write(request);
			smeBinary.flush();
		}
	}

	public void writeBinarySmppsim(byte[] response) throws IOException {
		if (SMPPSim.isCaptureSmppsimBinary()) {
			smppsimBinary.write(response);
			smppsimBinary.flush();
		}
	}

	public void writeDecodedSme(String request) throws IOException {
		if (SMPPSim.isCaptureSmeDecoded()) {
			smeDecoded.write(request + "\n");
			smeDecoded.flush();
		}
	}

	public void writeDecodedSmppsim(String response) throws IOException {
		if (SMPPSim.isCaptureSmppsimDecoded()) {
			smppsimDecoded.write(response + "\n");
			smppsimDecoded.flush();
		}
	}

	/**
	 * @return
	 */
	public static byte[] getSMSC_SYSTEMID() {
		return SMSC_SYSTEMID;
	}

	/**
	 * @param bs
	 */
	public static void setSMSC_SYSTEMID(byte[] bs) {
		SMSC_SYSTEMID = bs;
	}

	/**
	 * @return
	 */
	public static long getMessage_id() {
		return message_id;
	}

	/**
	 * @return
	 */
	public static int getSequence_no() {
		return sequence_no;
	}

	/**
	 * @param l
	 */
	public static void setMessage_id(long l) {
		message_id = l;
	}

	/**
	 * @param i
	 */
	public static void setSequence_no(int i) {
		sequence_no = i;
	}

	/**
	 * @return
	 */
	public ServerSocket getCss() {
		return css;
	}

	/**
	 * @return
	 */
	public static boolean isDecodePdus() {
		return decodePdus;
	}

	/**
	 * @param b
	 */
	public static void setDecodePdus(boolean b) {
		decodePdus = b;
	}

	/**
	 * @return
	 */
	public InboundQueue getIq() {
		return iq;
	}

	/**
	 * @return
	 */
	public OutboundQueue getOq() {
		return oq;
	}

	/**
	 * @return
	 */
	public int getInbound_queue_capacity() {
		return inbound_queue_capacity;
	}

	/**
	 * @return
	 */
	public int getInbound_queue_size() {
		return iq.size();
	}

	public int getPending_queue_size() {
		return iq.pending_size();
	}

	/**
	 * @return
	 */
	public int getOutbound_queue_capacity() {
		return outbound_queue_capacity;
	}

	/**
	 * @return
	 */
	public int getOutbound_queue_size() {
		return oq.size();
	}

	/**
	 * @param i
	 */
	public void setInbound_queue_capacity(int i) {
		inbound_queue_capacity = i;
	}

	/**
	 * @param i
	 */
	public void setOutbound_queue_capacity(int i) {
		outbound_queue_capacity = i;
	}

	/**
	 * @return
	 */
	public LifeCycleManager getLcm() {
		return lcm;
	}

	/**
	 * @return
	 */
	public String getStartTimeString() {
		return startTimeString;
	}

	/**
	 * @return
	 */
	public int getRxBoundCount() {
		return rxBoundCount;
	}

	/**
	 * @return
	 */
	public int getTrxBoundCount() {
		return trxBoundCount;
	}

	/**
	 * @return
	 */
	public int getTxBoundCount() {
		return txBoundCount;
	}

	/**
	 * @param i
	 */
	public void setRxBoundCount(int i) {
		logger.info("Set RxBoundCount to " + i);
		rxBoundCount = i;
	}

	/**
	 * @param i
	 */
	public void setTrxBoundCount(int i) {
		trxBoundCount = i;
	}

	/**
	 * @param i
	 */
	public void setTxBoundCount(int i) {
		txBoundCount = i;
	}

	public void incTxBoundCount() {
		txBoundCount++;
	}

	public void incRxBoundCount() {
		rxBoundCount++;
	}

	public void incTrxBoundCount() {
		trxBoundCount++;
	}

	public void incBindTransmitterOK() {
		bindTransmitterOK++;
	}

	public void incBindTransmitterERR() {
		bindTransmitterERR++;
	}

	public void incBindTransceiverOK() {
		bindTransceiverOK++;
	}

	public void incBindTransceiverERR() {
		bindTransceiverERR++;
	}

	public void incBindReceiverOK() {
		bindReceiverOK++;
	}

	public void incBindReceiverERR() {
		bindReceiverERR++;
	}

	public void incSubmitSmOK() {
		submitSmOK++;
	}

	public void incSubmitSmERR() {
		submitSmERR++;
	}

	public void incSubmitMultiOK() {
		submitMultiOK++;
	}

	public void incSubmitMultiERR() {
		submitMultiERR++;
	}

	public void incDeliverSmOK() {
		deliverSmOK++;
	}

	public void incDeliverSmERR() {
		deliverSmERR++;
	}

	public void incQuerySmOK() {
		querySmOK++;
	}

	public void incQuerySmERR() {
		querySmERR++;
	}

	public void incCancelSmOK() {
		cancelSmOK++;
	}

	public void incCancelSmERR() {
		cancelSmERR++;
	}

	public void incReplaceSmOK() {
		replaceSmOK++;
	}

	public void incReplaceSmERR() {
		replaceSmERR++;
	}

	public void incDataSmOK() {
		dataSmOK++;
	}

	public void incDataSmERR() {
		dataSmERR++;
	}

	public void incEnquireLinkOK() {
		enquireLinkOK++;
	}

	public void incEnquireLinkERR() {
		enquireLinkERR++;
	}

	public void incUnbindOK() {
		unbindOK++;
	}

	public void incUnbindERR() {
		unbindERR++;
	}

	public void incGenericNakOK() {
		genericNakOK++;
	}

	public void incGenericNakERR() {
		genericNakERR++;
	}

	/**
	 * @return
	 */
	public long getBindReceiverERR() {
		return bindReceiverERR;
	}

	/**
	 * @return
	 */
	public long getBindReceiverOK() {
		return bindReceiverOK;
	}

	/**
	 * @return
	 */
	public long getBindTransceiverERR() {
		return bindTransceiverERR;
	}

	/**
	 * @return
	 */
	public long getBindTransceiverOK() {
		return bindTransceiverOK;
	}

	/**
	 * @return
	 */
	public long getBindTransmitterERR() {
		return bindTransmitterERR;
	}

	/**
	 * @return
	 */
	public long getBindTransmitterOK() {
		return bindTransmitterOK;
	}

	/**
	 * @return
	 */
	public long getCancelSmERR() {
		return cancelSmERR;
	}

	/**
	 * @return
	 */
	public long getCancelSmOK() {
		return cancelSmOK;
	}

	/**
	 * @return
	 */
	public long getDeliverSmERR() {
		return deliverSmERR;
	}

	/**
	 * @return
	 */
	public long getDeliverSmOK() {
		return deliverSmOK;
	}

	/**
	 * @return
	 */
	public long getEnquireLinkERR() {
		return enquireLinkERR;
	}

	/**
	 * @return
	 */
	public long getEnquireLinkOK() {
		return enquireLinkOK;
	}

	/**
	 * @return
	 */
	public long getGenericNakERR() {
		return genericNakERR;
	}

	/**
	 * @return
	 */
	public long getGenericNakOK() {
		return genericNakOK;
	}

	/**
	 * @return
	 */
	public long getQuerySmERR() {
		return querySmERR;
	}

	/**
	 * @return
	 */
	public long getQuerySmOK() {
		return querySmOK;
	}

	/**
	 * @return
	 */
	public long getReplaceSmERR() {
		return replaceSmERR;
	}

	/**
	 * @return
	 */
	public long getReplaceSmOK() {
		return replaceSmOK;
	}

	/**
	 * @return
	 */
	public long getSubmitMultiERR() {
		return submitMultiERR;
	}

	/**
	 * @return
	 */
	public long getSubmitMultiOK() {
		return submitMultiOK;
	}

	/**
	 * @return
	 */
	public long getSubmitSmERR() {
		return submitSmERR;
	}

	/**
	 * @return
	 */
	public long getSubmitSmOK() {
		return submitSmOK;
	}

	/**
	 * @return
	 */
	public long getDataSmERR() {
		return dataSmERR;
	}

	/**
	 * @return
	 */
	public long getDataSmOK() {
		return dataSmOK;
	}

	/**
	 * @return
	 */
	public long getUnbindERR() {
		return unbindERR;
	}

	/**
	 * @return
	 */
	public long getUnbindOK() {
		return unbindOK;
	}

	public synchronized void sent(byte[] pdu) {
		byte[] type = PduUtilities.makeByteArrayFromInt(2, 1);
		callback(pdu, type);
	}

	public synchronized void received(byte[] pdu) {
		byte[] type = PduUtilities.makeByteArrayFromInt(1, 1);
		callback(pdu, type);
	}

	private void callback(byte[] pdu, byte[] type) {
		logger.debug("callback - start of operation");
		if (pdu == null)
			return;
		if (type == null)
			return;
		byte[] result = new byte[pdu.length + 9];
		byte[] id = new byte[4];
		try {
			id = SMPPSim.getCallback_id().getBytes("ASCII");
		} catch (Exception e) {
			;
		}
		byte[] length = PduUtilities.makeByteArrayFromInt(pdu.length + 9, 4);
		System.arraycopy(length, 0, result, 0, 4);
		System.arraycopy(type, 0, result, 4, 1);
		System.arraycopy(id, 0, result, 5, 4);
		System.arraycopy(pdu, 0, result, 9, pdu.length);
		try {
			callback_stream.write(result);
			logger.info("callback - written stream to callback connection");
		} catch (IOException ioe) {
			// assume connection gone
			logger.info("Reconnecting to callback server");
			boolean sentOK = false;
			Object callbackMutex = new Object();
			connectToCallbackServer(callbackMutex);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			// and send again
			synchronized (callbackMutex) {
				while (!sentOK) {
					try {
						callback_stream.write(result);
						sentOK = true;
					} catch (IOException ioe2) {
						connectToCallbackServer(callbackMutex);
					}
				}
			}
		}
		logger.debug("callback - end of operation");
	}

	public static synchronized boolean isCallback_server_online() {
		return callback_server_online;
	}

	public static synchronized void setCallback_server_online(
			boolean callback_server_online) {
		smsc.callback_server_online = callback_server_online;
	}

	public static synchronized Socket getCallback() {
		return callback;
	}

	public static synchronized void setCallback(Socket callback) {
		smsc.callback = callback;
	}

	public static synchronized OutputStream getCallback_stream() {
		return callback_stream;
	}

	public static synchronized void setCallback_stream(
			OutputStream callback_stream) {
		smsc.callback_stream = callback_stream;
	}

	public boolean isOutbind_sent() {
		return outbind_sent;
	}

	public void setOutbind_sent(boolean outbind_sent) {
		this.outbind_sent = outbind_sent;
	}

	public long getOutbindERR() {
		return outbindERR;
	}

	public void setOutbindERR(long outbindERR) {
		this.outbindERR = outbindERR;
	}

	public long getOutbindOK() {
		return outbindOK;
	}

	public void setOutbindOK(long outbindOK) {
		this.outbindOK = outbindOK;
	}

}