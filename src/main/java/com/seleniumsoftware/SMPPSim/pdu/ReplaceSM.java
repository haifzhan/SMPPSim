/****************************************************************************
 * ReplaceSM.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/ReplaceSM.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class ReplaceSM extends Request implements Demarshaller {

	// PDU attributes

	private String message_id;

	private int source_addr_ton;

	private int source_addr_npi;

	private String source_addr;

	private String schedule_delivery_time;

	private String validity_period;

	private int registered_delivery_flag;

	private int sm_default_msg_id;

	private int sm_length;

	private byte[] short_message;

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);
		// now set atributes of this specific PDU type
		int inx = 16;
		try {
			message_id = PduUtilities.getStringValueNullTerminated(request,
					inx, 65, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. message_id is incorrect");
			throw (e);
		}
		inx = inx + message_id.length() + 1;
		try {
			source_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. source_addr_ton is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			source_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. source_addr_npi is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			source_addr = PduUtilities.getStringValueNullTerminated(request,
					inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. source_addr is incorrect");
			throw (e);
		}
		inx = inx + source_addr.length() + 1;
		try {
			schedule_delivery_time = PduUtilities.getStringValueNullTerminated(
					request, inx, 17, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. schedule_delivery_time is incorrect");
			throw (e);
		}
		inx = inx + schedule_delivery_time.length() + 1;
		try {
			validity_period = PduUtilities.getStringValueNullTerminated(
					request, inx, 17, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. validity_period is incorrect");
			throw (e);
		}
		inx = inx + validity_period.length() + 1;
		try {
			registered_delivery_flag = PduUtilities.getIntegerValue(request,
					inx, 1);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. registered_delivery_flag is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			sm_default_msg_id = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. sm_default_msg_id is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			sm_length = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("REPLACE_SM PDU is malformed. sm_length is incorrect");
			throw (e);
		}
		inx = inx + 1;
		if (sm_length > 0) {
			short_message = new byte[sm_length];
			System.arraycopy(request, inx, short_message, 0, sm_length);
		}
	}

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString() + "," + "message_id=" + message_id + ","
				+ "source_addr_ton=" + source_addr_ton + ","
				+ "source_addr_npi=" + source_addr_npi + "," + "source_addr="
				+ source_addr + "," + "schedule_delivery_time="
				+ schedule_delivery_time + "," + "validity_period="
				+ validity_period + "," + "registered_delivery_flag="
				+ registered_delivery_flag + "," + "sm_default_msg_id="
				+ sm_default_msg_id + "," + "sm_length=" + sm_length + ","
				+ "short_message=" + new String(short_message);
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
	public int getRegistered_delivery_flag() {
		return registered_delivery_flag;
	}

	/**
	 * @return
	 */
	public String getSchedule_delivery_time() {
		return schedule_delivery_time;
	}

	/**
	 * @return
	 */
	public byte[] getShort_message() {
		return short_message;
	}

	/**
	 * @return
	 */
	public int getSm_default_msg_id() {
		return sm_default_msg_id;
	}

	/**
	 * @return
	 */
	public int getSm_length() {
		return sm_length;
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
	public String getValidity_period() {
		return validity_period;
	}

	/**
	 * @param string
	 */
	public void setMessage_id(String string) {
		message_id = string;
	}

	/**
	 * @param i
	 */
	public void setRegistered_delivery_flag(int i) {
		registered_delivery_flag = i;
	}

	/**
	 * @param string
	 */
	public void setSchedule_delivery_time(String string) {
		schedule_delivery_time = string;
	}

	/**
	 * @param string
	 */
	public void setShort_message(byte[] msg) {
		short_message = msg;
	}

	/**
	 * @param i
	 */
	public void setSm_default_msg_id(int i) {
		sm_default_msg_id = i;
	}

	/**
	 * @param i
	 */
	public void setSm_length(int i) {
		sm_length = i;
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
	 * @param string
	 */
	public void setValidity_period(String string) {
		validity_period = string;
	}

}