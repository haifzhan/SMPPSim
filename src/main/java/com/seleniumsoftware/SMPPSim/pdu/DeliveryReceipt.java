/****************************************************************************
 * DeliveryReceipt.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/DeliveryReceipt.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import java.util.*;
import java.util.logging.Logger;

import com.seleniumsoftware.SMPPSim.SMPPSim;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;

public class DeliveryReceipt extends DeliverSM {
	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	// PDU fields
	private String message_id = "";

	private String sub = "";

	private String dlvrd = "";

	private String submit_date = "";

	private String done_date = "";

	private String err = "000";

	private String stat = "";

	private String text = "";

	public DeliveryReceipt(SubmitSM msg, int esm_class, String messageID, byte state) {
		super(msg);
		setEsm_class(esm_class);
		setValidity_period("");
		setRegistered_delivery_flag(0);
		deriveUssd_service_op(msg);

		// TLVs we may include in the delivery receipt depending on the client SMPP interface_version
		// if the client supports an earlier version, we'll remove these parameters when the time to deliver comes

		Tlv umr_tlv = msg.getOptional(PduConstants.USER_MESSAGE_REFERENCE_TAG);
		if (umr_tlv != null) {
			addVsop(umr_tlv.getTag(), umr_tlv.getLen(), umr_tlv.getValue());
		}

		addVsop(PduConstants.RECEIPTED_MESSAGE_ID, (short) (messageID.length()+1), PduUtilities.stringToNullTerminatedByteArray(messageID));
		byte [] state_byte = new byte[1];
		state_byte[0] = state;
		addVsop(PduConstants.MESSAGE_STATE, (short) 1, state_byte);
		
		int network_error_code=0x030000; // 0x03 means GSM

		// network_error_code values are not defined in the SMPP spec so two simple values only are hard coded here to support the most basic of testing needs
		
		switch (state) {
			case PduConstants.UNDELIVERABLE:
				// error code of 01 in the last 2 octets
				network_error_code = network_error_code | 0x000001;
				byte [] nec_bytes1 = PduUtilities.makeByteArrayFromInt(network_error_code, 3);
				addVsop(PduConstants.NETWORK_ERROR_CODE_TAG, (short) 3, nec_bytes1);
				break;
			case PduConstants.REJECTED: 
				// error code of 02 in the last 2 octets
				network_error_code = network_error_code | 0x000002;
				byte [] nec_bytes2 = PduUtilities.makeByteArrayFromInt(network_error_code, 3);
				addVsop(PduConstants.NETWORK_ERROR_CODE_TAG, (short) 3, nec_bytes2);
				break;
		}		
	}

	public void setDeliveryReceiptMessage(byte state) {
		// id:IIIIIIIIII sub:SSS dlvrd:DDD submit date:YYMMDDhhmm done
		// date:YYMMDDhhmm stat:DDDDDDD err:E Text: . . . . . . . . .
		Date d = new Date();
		String id = "id:" + message_id;
		String sb = " sub:" + sub;
		String dlv = " dlvrd:" + dlvrd;
		String sdate = " submit date:" + submit_date;
		String ddate = " done date:" + done_date;
		setStateText(state);
		String st = " stat:" + stat;
		String er = " err:" + err;
		String txt = " Text:" + text;
		setShort_message(new String(id + sb + dlv + sdate + ddate + st + er + txt).getBytes());
		setSm_length(getShort_message().length);
	}

	public void setStateText(byte state) {
		if (state == PduConstants.DELIVERED)
			stat = "DELIVRD";
		else if (state == PduConstants.EXPIRED)
			stat = "EXPIRED";
		else if (state == PduConstants.DELETED)
			stat = "DELETED";
		else if (state == PduConstants.UNDELIVERABLE)
			stat = "UNDELIV";
		else if (state == PduConstants.ACCEPTED)
			stat = "ACCEPTD";
		else if (state == PduConstants.UNKNOWN)
			stat = "UNKNOWN";
		else if (state == PduConstants.REJECTED)
			stat = "REJECTD";
		else if (state == PduConstants.ENROUTE)
			stat = "ENROUTE";
		else
			stat = "BADSTAT";
	}

	// 0 = PSSD indication
	// 1 = PSSR indication
	// 2 = USSR request
	// 3 = USSN request
	// 16 = PSSD response
	// 17 = PSSR response
	// 18 = USSR confirm
	// 19 = USSN confirm

	private void deriveUssd_service_op(SubmitSM msg) {
		if (SMPPSim.isDeliver_sm_includes_ussd_service_op()) {
			Tlv ussd_service_op = msg.getUssd_service_op();
			if (ussd_service_op != null && ussd_service_op.getValue().length == 1) {
				byte uso = 0;
				try {
					uso = ussd_service_op.getValue()[0];
					switch (uso) {
					case 0:
						setUssd_service_op((byte) 16); // PSSD response
						break;
					case 1:
						setUssd_service_op((byte) 17); // PSSD response
						break;
					case 2:
						setUssd_service_op((byte) 18); // PSSD response
						break;
					case 3:
						setUssd_service_op((byte) 19); // PSSD response
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * @return
	 */
	public String getDlvrd() {
		return dlvrd;
	}

	/**
	 * @return
	 */
	public String getDone_date() {
		return done_date;
	}

	/**
	 * @return
	 */
	public String getErr() {
		return err;
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
	public String getStat() {
		return stat;
	}

	/**
	 * @return
	 */
	public String getSub() {
		return sub;
	}

	/**
	 * @return
	 */
	public String getSubmit_date() {
		return submit_date;
	}

	/**
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param string
	 */
	public void setDlvrd(String string) {
		dlvrd = string;
	}

	/**
	 * @param string
	 */
	public void setDone_date(String string) {
		logger.finest("Setting done_date=" + string);
		done_date = string;
	}

	/**
	 * @param string
	 */
	public void setErr(String string) {
		err = string;
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
	public void setStat(String string) {
		stat = string;
	}

	/**
	 * @param string
	 */
	public void setSub(String string) {
		sub = string;
	}

	/**
	 * @param string
	 */
	public void setSubmit_date(String string) {
		submit_date = string;
	}

	/**
	 * @param string
	 */
	public void setText(String string) {
		text = string;
	}

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString();
	}
}