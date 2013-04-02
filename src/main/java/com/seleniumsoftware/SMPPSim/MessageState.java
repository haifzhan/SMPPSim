/****************************************************************************
 * MessageState.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/MessageState.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.pdu.*;

import java.text.ParseException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageState {
//	private static Logger logger = Logger
//			.getLogger("com.seleniumsoftware.smppsim");

    private static Logger logger = LoggerFactory.getLogger(MessageState.class);
	// key
	private String message_id;

	private int source_addr_ton;

	private int source_addr_npi;

	private String source_addr;

	// Original pdu (only SUBMIT_SM currently)
	SubmitSM pdu;

	// other attributes
	private byte state;
	
	private int err;

	private boolean responseSent = false;

	private long submit_time;

	private long final_time;

	private SmppTime finalDate;

	private SmppTime validity_period;

	private boolean intermediate_notification_requested = false;

	public MessageState() {
	}

	public MessageState(SubmitSM pdu, String id) {
		message_id = id;
		source_addr_ton = pdu.getSource_addr_ton();
		source_addr_npi = pdu.getSource_addr_npi();
		source_addr = pdu.getSource_addr();
		this.pdu = pdu;
		// All messages start with state ENROUTE
		state = PduConstants.ENROUTE;
		byte rd = (byte) pdu.getRegistered_delivery_flag();
		if ((rd & (byte) 0x10) == 0x10)
			intermediate_notification_requested = true;
		submit_time = System.currentTimeMillis();
		try {
			if (!pdu.getValidity_period().equals(""))
				validity_period = new SmppTime(pdu.getValidity_period());
			else {
				//logger.info("Validity period is not set: defaulting to 5 minutes from now");
				long now = System.currentTimeMillis() + 300000;
				String st = SmppTime.dateToSMPPString(new Date(now));
				validity_period = new SmppTime(st);
				//logger.info("Generated default validity period=" + st);
			}
		} catch (ParseException e) {
			logger.error("Could not parse validity period : using default of 5 minutes");
			long vtime = System.currentTimeMillis() + 300000;
			Date vdate = new Date(vtime);
			try {
				validity_period = new SmppTime(SmppTime.dateToSMPPString(vdate));
			} catch (ParseException e2) {
				logger.error("Internal error: could not set default validity period due to parse error");
			}
		}
	}

	public boolean equals(Object other) {
//		logger.info("MessageState.equals:"+other.getClass().getName());
		if (other instanceof MessageState) {
			MessageState ms = (MessageState) other;
//			logger.info("MessageState equality1:" + this.keyToString());
//			logger.info("MessageState equality2:" + ms.keyToString());
			if (ms.getMessage_id().equals(message_id)
					&& ms.getSource_addr_ton() == source_addr_ton
					&& ms.getSource_addr_npi() == source_addr_npi
					&& ms.getSource_addr().equals(source_addr))
				return true;
		}
		return false;
	}

	public int hashCode() {
		// An int derived from message_id plus the last 3 digits of source_addr
		// should surely always be unique. Shouldn't it?
		int sal = source_addr.length();
		String key = message_id + source_addr.substring(sal - 3, sal);
		byte[] bytearray = key.getBytes();
		int l = bytearray.length;
		int h = 0;
		for (int i = 0; i < l; i++) {
			h = h + bytearray[i] * 31 ^ (l - (i + 1));
		}
		return h;
	}

	/**
	 * @return
	 */
	public String getMessage_id() {
		return message_id;
	}

	/**
	 * @return
	 */
	public String getSource_addr() {
		return source_addr;
	}

	/**
	 * @return
	 */
	public int getSource_addr_npi() {
		return source_addr_npi;
	}

	/**
	 * @return
	 */
	public int getSource_addr_ton() {
		return source_addr_ton;
	}

	/**
	 * @return
	 */
	public byte getState() {
		return state;
	}

	/**
	 * @param string
	 */
	public void setMessage_id(String string) {
		message_id = string;
	}

	/**
	 * @param string
	 */
	public void setSource_addr(String string) {
		source_addr = string;
	}

	/**
	 * @param i
	 */
	public void setSource_addr_npi(int i) {
		source_addr_npi = i;
	}

	/**
	 * @param i
	 */
	public void setSource_addr_ton(int i) {
		source_addr_ton = i;
	}

	/**
	 * @param b
	 */
	public void setState(byte b) {
		state = b;
	}

	/**
	 * @return
	 */
	public SubmitSM getPdu() {
		return pdu;
	}

	/**
	 * @return
	 */
	public long getSubmit_time() {
		return submit_time;
	}

	/**
	 * @return
	 */
	public SmppTime getValidity_period() {
		return validity_period;
	}

	/**
	 * @param request
	 */
	public void setPdu(SubmitSM request) {
		pdu = request;
	}

	/**
	 * @param date
	 */
	public void setSubmit_time(long date) {
		submit_time = date;
	}

	/**
	 * @param time
	 */
	public void setValidity_period(SmppTime time) {
		validity_period = time;
	}

//	public String toString() {
//		return "message_id=" + message_id + "," + "source_addr_ton="
//				+ source_addr_ton + "," + "source_addr_npi=" + source_addr_npi
//				+ "," + "source_addr=" + source_addr + "," + "PDU=" + pdu + ","
//				+ "state=" + state + "," + "submit_time=" + submit_time + ","
//				+ "final_time=" + final_time + "," + "finalDate=" + finalDate
//				+ "," + "validity_period=" + validity_period;
//	}
        
        public String toString() {
		return "OP:" + source_addr + "  CP:" + pdu.getDestination_addr();
	}

	public String keyToString() {
		return "message_id=" + message_id + "," + "source_addr_ton="
				+ source_addr_ton + "," + "source_addr_npi=" + source_addr_npi
				+ "," + "source_addr=" + source_addr;
	}

	public long getFinal_time() {
		return final_time;
	}

	/**
	 * @return
	 */
	public SmppTime getFinalDate() {
		return finalDate;
	}

	/**
	 * @param l
	 */
	public void setFinal_time(long l) {
		final_time = l;
		String stime = SmppTime.dateToSMPPString(new Date(l));
		try {
			finalDate = new SmppTime(stime);
		} catch (ParseException e) {
			logger.error("ParseException - this should be impossible");
			finalDate = null;
		}
	}

	/**
	 * @return
	 */
	public boolean responseSent() {
		return responseSent;
	}

	/**
	 * @param b
	 */
	public void setResponseSent(boolean b) {
		responseSent = b;
	}

	public boolean isIntermediate_notification_requested() {
		return intermediate_notification_requested;
	}

	public void setIntermediate_notification_requested(
			boolean intermediate_notification_requested) {
		this.intermediate_notification_requested = intermediate_notification_requested;
	}

	public int getErr() {
		return err;
	}

	public void setErr(int err) {
		this.err = err;
	}

}