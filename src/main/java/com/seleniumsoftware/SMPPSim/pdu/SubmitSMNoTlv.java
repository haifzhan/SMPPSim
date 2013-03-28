/****************************************************************************
 * SubmitSM.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/SubmitSMNoTlv.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import java.util.logging.Logger;

import com.seleniumsoftware.SMPPSim.exceptions.InvalidHexStringlException;
import com.seleniumsoftware.SMPPSim.pdu.util.*;
import com.seleniumsoftware.SMPPSim.util.LoggingUtilities;
import com.seleniumsoftware.SMPPSim.util.Utilities;

public class SubmitSMNoTlv extends Request implements Demarshaller {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	// Mandatory PDU attributes

	private String service_type;

	private int source_addr_ton;

	private int source_addr_npi;

	private String source_addr = "";

	private int dest_addr_ton;

	private int dest_addr_npi;

	private String destination_addr = "";

	private int esm_class;

	private int protocol_ID;

	private int priority_flag;

	private String schedule_delivery_time = "";

	private String validity_period = "";

	private int registered_delivery_flag;

	private int replace_if_present_flag;

	private int data_coding;

	private int sm_default_msg_id;

	private int sm_length;

	private byte[] short_message;

	public static void main(String args[]) throws Exception {
		String msg_hex = "000000D5 00000004 00000000 0000432E 00 01 01 32 31 32 36 33 30 33 35 39 38 30 39 00 01 01 33 39 33 32 30 38 39 36 34 30 36 38 00 40 00 01 00 00 00 00 10 80 00 00 15 00 00 00 00 00 00 14 F3 00 01 00 04 00 8C 0B 00 03 20 03 01 05 04 15 8A 00 00 30 01 00 08 00 4C 00 6F 00 56 00 45 02 01 00 00 48 1C 01 00 00 00 00 00 00 00 18 00 00 00 00 00 00 00 00 3C 00 00 00 00 00 00 00 00 E7 00 00 00 00 00 00 00 03 85 80 00 00 00 00 20 00 06 04 C0 00 00 00 00 38 03 84 06 40 00 00 00 00 1C 01 CC 07 40 00 00 00 00 16 00 E8 0D 40 00 00 00 00 32 00 78 09 40 00 00 00 00 65 08 38 19 C0 00 00 00 00 C1 04 78 33 60 00 00 00 01 81 0A 78 72 20 00";
		byte [] bytes = Utilities.getByteArrayFromHexString(msg_hex);
		SubmitSMNoTlv req = new SubmitSMNoTlv();
		req.demarshall(bytes);
		LoggingUtilities.logDecodedPdu(req);
	}

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);

		// now set mandatory attributes of this specific PDU type
		int inx = 16;
		service_type = PduUtilities.getStringValueNullTerminated(request, inx, 6, PduConstants.C_OCTET_STRING_TYPE);
		inx = inx + service_type.length() + 1;
		source_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		source_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		source_addr = PduUtilities.getStringValueNullTerminated(request, inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		inx = inx + source_addr.length() + 1;
		dest_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		dest_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		destination_addr = PduUtilities.getStringValueNullTerminated(request, inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		inx = inx + destination_addr.length() + 1;
		esm_class = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		protocol_ID = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		priority_flag = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		schedule_delivery_time = PduUtilities.getStringValueNullTerminated(request, inx, 17, PduConstants.C_OCTET_STRING_TYPE);
		inx = inx + schedule_delivery_time.length() + 1;
		validity_period = PduUtilities.getStringValueNullTerminated(request, inx, 17, PduConstants.C_OCTET_STRING_TYPE);
		inx = inx + validity_period.length() + 1;
		registered_delivery_flag = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		replace_if_present_flag = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		data_coding = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		sm_default_msg_id = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		sm_length = PduUtilities.getIntegerValue(request, inx, 1);
		inx = inx + 1;
		short_message = new byte[sm_length];
		if (sm_length > 0) {
			System.arraycopy(request, inx, short_message, 0, sm_length);
		}

	}

	/**
	 * @return
	 */
	public int getData_coding() {
		return data_coding;
	}

	/**
	 * @return
	 */
	public int getDest_addr_npi() {
		return dest_addr_npi;
	}

	/**
	 * @return
	 */
	public int getDest_addr_ton() {
		return dest_addr_ton;
	}

	/**
	 * @return
	 */
	public String getDestination_addr() {
		return destination_addr;
	}

	/**
	 * @return
	 */
	public int getEsm_class() {
		return esm_class;
	}

	/**
	 * @return
	 */
	public int getPriority_flag() {
		return priority_flag;
	}

	/**
	 * @return
	 */
	public int getProtocol_ID() {
		return protocol_ID;
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
	public int getReplace_if_present_flag() {
		return replace_if_present_flag;
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
	public String getService_type() {
		return service_type;
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
	 * @param i
	 */
	public void setData_coding(int i) {
		data_coding = i;
	}

	/**
	 * @param i
	 */
	public void setDest_addr_npi(int i) {
		dest_addr_npi = i;
	}

	/**
	 * @param i
	 */
	public void setDest_addr_ton(int i) {
		dest_addr_ton = i;
	}

	/**
	 * @param string
	 */
	public void setDestination_addr(String string) {
		destination_addr = string;
	}

	/**
	 * @param i
	 */
	public void setEsm_class(int i) {
		esm_class = i;
	}

	/**
	 * @param i
	 */
	public void setPriority_flag(int i) {
		priority_flag = i;
	}

	/**
	 * @param i
	 */
	public void setProtocol_ID(int i) {
		protocol_ID = i;
	}

	/**
	 * @param i
	 */
	public void setRegistered_delivery_flag(int i) {
		registered_delivery_flag = i;
	}

	/**
	 * @param i
	 */
	public void setReplace_if_present_flag(int i) {
		replace_if_present_flag = i;
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
	public void setService_type(String string) {
		service_type = string;
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

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString() + "," + "service_type=" + service_type + "," + "source_addr_ton=" + source_addr_ton + "," + "source_addr_npi="
				+ source_addr_npi + "," + "source_addr=" + source_addr + "," + "dest_addr_ton=" + dest_addr_ton + "," + "dest_addr_npi=" + dest_addr_npi + ","
				+ "dest_addr=" + destination_addr + "," + "esm_class=" + esm_class + "," + "protocol_ID=" + protocol_ID + "," + "priority_flag="
				+ priority_flag + "," + "schedule_delivery_time=" + schedule_delivery_time + "," + "validity_period=" + validity_period + ","
				+ "registered_delivery_flag=" + registered_delivery_flag + "," + "replace_if_present_flag=" + replace_if_present_flag + "," + "data_coding="
				+ data_coding + "," + "sm_default_msg_id=" + sm_default_msg_id + "," + "sm_length=" + sm_length + "," + "short_message=");
		if (sm_length > 0)
			sb.append(new String(short_message));
		return sb.toString();
	}

}