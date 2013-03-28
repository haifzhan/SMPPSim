/****************************************************************************
 * DeliverSM.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/DeliverSM.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.seleniumsoftware.SMPPSim.SMPPSim;
import com.seleniumsoftware.SMPPSim.Smsc;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;

public class DeliverSM extends Response implements Marshaller, Cloneable {

	private Smsc smsc = Smsc.getInstance();

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	private long created;

	private String service_type = "";

	private int source_addr_ton = 0;

	private int source_addr_npi = 0;

	private String source_addr = "";

	private int dest_addr_ton = 0;

	private int dest_addr_npi = 0;

	private String destination_addr = "";

	private int esm_class = 0;

	private int protocol_ID = 0;

	private int priority_flag = 0;

	private String schedule_delivery_time = "";

	private String validity_period = "";

	private int registered_delivery_flag = 0;

	private int replace_if_present_flag = 0;

	private int data_coding = 0;

	private int sm_default_msg_id = 0;

	private int sm_length = 0;

	private byte[] short_message = new byte[160];

	// optional parameters as strings
	private String string_user_message_reference = "";

	private String string_source_port = "";

	private String string_destination_port = "";

	private String string_sar_msg_ref_num = "";

	private String string_sar_total_segments = "";

	private String string_sar_segment_seqnum = "";

	private String string_user_response_code = "";

	private String string_privacy_indicator = "";

	private String string_payload_type = "";

	private String string_message_payload = "";

	private String string_callback_num = "";

	private String string_source_subaddress = "";

	private String string_dest_subaddress = "";

	private String string_language_indicator = "";

	private String string_ussd_service_op = "";

	// optional parameters in correct form
	private short user_message_reference;

	private short source_port;

	private short destination_port;

	private short sar_msg_ref_num;

	private short sar_total_segments; // short not byte to avoid sign problems

	private short sar_segment_seqnum;

	private short user_response_code;

	private short privacy_indicator;

	private short payload_type;

	private String message_payload;

	private String callback_num;

	private byte[] source_subaddress;

	private byte[] dest_subaddress;

	private short language_indicator;

	private byte ussd_service_op;

	// Vendor specific optional parameters

	private ArrayList<Tlv> vs_ops = new ArrayList<Tlv>();

	public DeliverSM() {
		// message header fields except message length
		setCmd_id(PduConstants.DELIVER_SM);
		setCmd_status(PduConstants.ESME_ROK);
		setSeq_no(smsc.getNextSequence_No());
		// Set message length to zero since actual length will not be known until the object is
		// converted back to a message complete with null terminated strings
		setCmd_len(0);
		created = System.currentTimeMillis();
	}

	public DeliverSM(SubmitSM msg) {
		setCommonAttributes(msg);
		dest_addr_ton = msg.getSource_addr_ton();
		dest_addr_npi = msg.getSource_addr_npi();
		destination_addr = msg.getSource_addr();
		if (destination_addr == null)
			destination_addr = "";
		source_addr_ton = msg.getDest_addr_ton();
		source_addr_npi = msg.getDest_addr_npi();
		source_addr = msg.getDestination_addr();
		if (source_addr == null)
			source_addr = "";
	}

	public void esmeToEsmeDerivation(SubmitSM msg) {
		setCommonAttributes(msg);
		dest_addr_ton = msg.getDest_addr_ton();
		dest_addr_npi = msg.getDest_addr_npi();
		destination_addr = msg.getDestination_addr();
		if (destination_addr == null)
			destination_addr = "";
		source_addr_ton = msg.getSource_addr_ton();
		source_addr_npi = msg.getSource_addr_npi();
		source_addr = msg.getSource_addr();
		if (source_addr == null)
			source_addr = "";
	}

	private void setCommonAttributes(SubmitSM msg) {
		// message header fields except message length
		setCmd_id(PduConstants.DELIVER_SM);
		setCmd_status(PduConstants.ESME_ROK);
		setSeq_no(smsc.getNextSequence_No());
		// Set message length to zero since actual length will not be known until the object is
		// converted back to a message complete with null terminated strings
		setCmd_len(0);
		// message body
		service_type = msg.getService_type();
		if (service_type == null)
			service_type = "";
		esm_class = msg.getEsm_class();
		protocol_ID = msg.getProtocol_ID();
		priority_flag = msg.getPriority_flag();
		schedule_delivery_time = msg.getSchedule_delivery_time();
		if (schedule_delivery_time == null)
			schedule_delivery_time = "";
		validity_period = msg.getValidity_period();
		if (validity_period == null)
			validity_period = "";
		registered_delivery_flag = msg.getRegistered_delivery_flag();
		replace_if_present_flag = msg.getReplace_if_present_flag();
		data_coding = msg.getData_coding();
		sm_default_msg_id = msg.getSm_default_msg_id();
		short_message = msg.getShort_message();
		if (short_message == null)
			sm_length = 0;
		else
			sm_length = short_message.length;
		created = System.currentTimeMillis();
	}

	public byte[] marshall() throws Exception {
		out.reset();
		super.prepareHeaderForMarshalling();
		logger.finest("Prepared header for marshalling");
		out.write(PduUtilities.stringToNullTerminatedByteArray(service_type));
		logger.finest("marshalled service_type");
		out.write(PduUtilities.makeByteArrayFromInt(source_addr_ton, 1));
		logger.finest("marshalled source_addr_ton");
		out.write(PduUtilities.makeByteArrayFromInt(source_addr_npi, 1));
		logger.finest("marshalled source_addr_npi");

		out.write(PduUtilities.stringToNullTerminatedByteArray(source_addr));
		logger.finest("marshalled source_addr");

		out.write(PduUtilities.makeByteArrayFromInt(dest_addr_ton, 1));
		logger.finest("marshalled dest_addr_ton");
		out.write(PduUtilities.makeByteArrayFromInt(dest_addr_npi, 1));
		logger.finest("marshalled dest_addr_npi");

		out.write(PduUtilities.stringToNullTerminatedByteArray(destination_addr));

		//		// start temp code
		//		
		//		byte [] danish_dest_address = { 0x53, 0x74, 0x6F, 0x65, 0x62, (byte) 0xE6, 0x6C, 0x74, 0x00 };
		//		out.write(danish_dest_address);
		//		destination_addr="HARD CODED!";
		//		logger.info("marshalled hard code destination_addr for delivery receipt");
		//		// end temp code

		logger.finest("marshalled destination_addr");

		out.write(PduUtilities.makeByteArrayFromInt(esm_class, 1));
		logger.finest("marshalled esm_class");
		out.write(PduUtilities.makeByteArrayFromInt(protocol_ID, 1));
		logger.finest("marshalled protocol_ID");
		out.write(PduUtilities.makeByteArrayFromInt(priority_flag, 1));
		logger.finest("marshalled priority_flag");
		out.write(PduUtilities.stringToNullTerminatedByteArray(""));
		logger.finest("marshalled schedule_delivery_time");
		// schedule_delivery_time is null for this PDU
		out.write(PduUtilities.stringToNullTerminatedByteArray(""));
		logger.finest("marshalled validity_period");
		// validity_period is null for this PDU
		out.write(PduUtilities.makeByteArrayFromInt(registered_delivery_flag, 1));
		logger.finest("marshalled registered_delivery_flag");
		out.write(PduUtilities.makeByteArrayFromInt(0, 1));
		// replace_if_present is null for this PDU
		logger.finest("marshalled replace_if_present");
		out.write(PduUtilities.makeByteArrayFromInt(data_coding, 1));
		logger.finest("marshalled data_coding");
		out.write(PduUtilities.makeByteArrayFromInt(0, 1));
		// sm_default_msg_id is null for this PDU
		logger.finest("marshalled sm_default_msg_id");
		out.write(PduUtilities.makeByteArrayFromInt(sm_length, 1));
		logger.finest("marshalled sm_length");
		out.write(short_message);
		logger.finest("marshalled short_message");
		// include optional TLV parameters if there are any
		if (string_user_message_reference != null && !string_user_message_reference.equals("")) {
			user_message_reference = Short.parseShort(string_user_message_reference);
			out.write(PduUtilities.makeShortTLV(PduConstants.USER_MESSAGE_REFERENCE_TAG, user_message_reference));
		}
		logger.finest("marshalled user_message_reference");

		if (string_source_port != null && !string_source_port.equals("")) {
			source_port = Short.parseShort(string_source_port);
			out.write(PduUtilities.makeShortTLV(PduConstants.SOURCE_PORT, source_port));
		}
		logger.finest("marshalled source_port");

		if (string_destination_port != null && !string_destination_port.equals("")) {
			destination_port = Short.parseShort(string_destination_port);
			out.write(PduUtilities.makeShortTLV(PduConstants.DESTINATION_PORT, destination_port));
		}
		logger.finest("marshalled destination_port");

		if (string_sar_msg_ref_num != null && !string_sar_msg_ref_num.equals("")) {
			sar_msg_ref_num = Short.parseShort(string_sar_msg_ref_num);
			out.write(PduUtilities.makeShortTLV(PduConstants.SAR_MSG_REF_NUM, sar_msg_ref_num));
		}
		logger.finest("marshalled sar_msg_ref_num");

		if (string_sar_total_segments != null && !string_sar_total_segments.equals("")) {
			sar_total_segments = Short.parseShort(string_sar_total_segments);
			out.write(PduUtilities.makeByteTLV(PduConstants.SAR_TOTAL_SEGMENTS, sar_total_segments));
		}
		logger.finest("marshalled sar_total_segments");

		if (string_sar_segment_seqnum != null && !string_sar_segment_seqnum.equals("")) {
			sar_segment_seqnum = Short.parseShort(string_sar_segment_seqnum);
			out.write(PduUtilities.makeByteTLV(PduConstants.SAR_SEGMENT_SEQNUM, sar_segment_seqnum));
		}
		logger.finest("marshalled sar_segment_seqnum");

		if (string_user_response_code != null && !string_user_response_code.equals("")) {
			user_response_code = Short.parseShort(string_user_response_code);
			out.write(PduUtilities.makeByteTLV(PduConstants.USER_RESPONSE_CODE, user_response_code));
		}
		logger.finest("marshalled user_response_code");

		if (string_privacy_indicator != null && !string_privacy_indicator.equals("")) {
			privacy_indicator = Short.parseShort(string_privacy_indicator);
			out.write(PduUtilities.makeByteTLV(PduConstants.PRIVACY_INDICATOR, privacy_indicator));
		}
		logger.finest("marshalled privacy_indicator");

		if (string_payload_type != null && !string_payload_type.equals("")) {
			payload_type = Short.parseShort(string_payload_type);
			out.write(PduUtilities.makeByteTLV(PduConstants.PAYLOAD_TYPE, payload_type));
		}
		logger.finest("marshalled payload_type");
		if (string_message_payload != null && !string_message_payload.equals("")) {
			message_payload = string_message_payload;
			out.write(PduUtilities.makeCOctetStringTLV(PduConstants.MESSAGE_PAYLOAD, message_payload.getBytes()));
		}
		logger.finest("marshalled message_payload");

		if (string_callback_num != null && !string_callback_num.equals("")) {
			// min 4 bytes long so pad with spaces if necessary
			if (string_callback_num.length() < 4)
				string_callback_num = (string_callback_num + "    ").substring(0, 4);
			callback_num = string_callback_num;
			out.write(PduUtilities.makeCOctetStringTLV(PduConstants.CALLBACK_NUM, callback_num.getBytes()));
		}
		logger.finest("marshalled callback_num");

		if (string_source_subaddress != null && !string_source_subaddress.equals("")) {
			// min 2 chars starting with 128 (only sub_address tag supported at present)
			source_subaddress = new byte[string_source_subaddress.length() + 1];
			source_subaddress[0] = (byte) 0x80;
			System.arraycopy(string_source_subaddress.getBytes(), 0, source_subaddress, 1, string_source_subaddress.length());
			out.write(PduUtilities.makeCOctetStringTLV(PduConstants.SOURCE_SUBADDRESS, source_subaddress));
		}
		logger.finest("marshalled source_subaddress");

		if (string_dest_subaddress != null && !string_dest_subaddress.equals("")) {
			// min 2 chars starting with 128 (only sub_address tag supported at present)
			dest_subaddress = new byte[string_dest_subaddress.length() + 1];
			dest_subaddress[0] = (byte) 0x80;
			System.arraycopy(string_dest_subaddress.getBytes(), 0, dest_subaddress, 1, string_dest_subaddress.length());
			out.write(PduUtilities.makeCOctetStringTLV(PduConstants.DEST_SUBADDRESS, dest_subaddress));
		}
		logger.finest("marshalled dest_subaddress");

		if (string_language_indicator != null && !string_language_indicator.equals("")) {
			language_indicator = Short.parseShort(string_language_indicator);
			out.write(PduUtilities.makeByteTLV(PduConstants.LANGUAGE_INDICATOR, language_indicator));
		}
		logger.finest("marshalled language_indicator");

		if (string_ussd_service_op != null && !string_ussd_service_op.equals("")) {
			ussd_service_op = Byte.parseByte(string_ussd_service_op);
			out.write(PduUtilities.makeByteTLV(PduConstants.USSD_SERVICE_OP, ussd_service_op));
		}

		for (Tlv t : vs_ops) {
			out.write(PduUtilities.makeRawTLV(t.getTag(), t.getValue()));
		}
		byte[] response = out.toByteArray();
		int l = response.length;
		response = PduUtilities.setPduLength(response, l);
		return response;
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

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("CloneNotSupportedException!");
		}
	}

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		String string_value = "";
		string_value = super.toString() + "," + "service_type=" + service_type + "," + "source_addr_ton=" + source_addr_ton + "," + "source_addr_npi="
				+ source_addr_npi + "," + "source_addr=" + source_addr + "," + "dest_addr_ton=" + dest_addr_ton + "," + "dest_addr_npi=" + dest_addr_npi + ","
				+ "destination_addr=" + destination_addr + "," + "esm_class=" + esm_class + "," + "protocol_ID=" + protocol_ID + "," + "priority_flag="
				+ priority_flag + "," + "schedule_delivery_time=" + schedule_delivery_time + "," + "validity_period=" + validity_period + ","
				+ "registered_delivery_flag=" + registered_delivery_flag + "," + "replace_if_present_flag=" + replace_if_present_flag + "," + "data_coding="
				+ data_coding + "," + "sm_default_msg_id=" + sm_default_msg_id + "," + "sm_length=" + sm_length + "," + "short_message="
				+ new String(short_message);

		return string_value;
	}

	public String getOptParamsAsString() {
		String params = "";
		for (Tlv tlv : vs_ops) {
			if (params.length() > 0) {
				params = params + ",";
			}
			params = params + "TLV=" + tlv.getTag() + "/" + tlv.getLen() + "/"
					+ PduUtilities.byteArrayToHexString(tlv.getValue());
		}
		return params;
	}

	public void setString_callback_num(String string_callback_num) {
		this.string_callback_num = string_callback_num;
	}

	public void setString_dest_subaddress(String string_dest_subaddress) {
		this.string_dest_subaddress = string_dest_subaddress;
	}

	public void setString_destination_port(String string_destination_port) {
		this.string_destination_port = string_destination_port;
	}

	public void setString_language_indicator(String string_language_indicator) {
		this.string_language_indicator = string_language_indicator;
	}

	public void setString_message_payload(String string_message_payload) {
		this.string_message_payload = string_message_payload;
	}

	public void setString_payload_type(String string_payload_type) {
		this.string_payload_type = string_payload_type;
	}

	public void setString_privacy_indicator(String string_privacy_indicator) {
		this.string_privacy_indicator = string_privacy_indicator;
	}

	public void setString_sar_msg_ref_num(String string_sar_msg_ref_num) {
		this.string_sar_msg_ref_num = string_sar_msg_ref_num;
	}

	public void setString_sar_segment_seqnum(String string_sar_segment_seqnum) {
		this.string_sar_segment_seqnum = string_sar_segment_seqnum;
	}

	public void setString_sar_total_segments(String string_sar_total_segments) {
		this.string_sar_total_segments = string_sar_total_segments;
	}

	public void setString_source_port(String string_source_port) {
		this.string_source_port = string_source_port;
	}

	public void setString_source_subaddress(String string_source_subaddress) {
		this.string_source_subaddress = string_source_subaddress;
	}

	public void setString_user_message_reference(String string_user_message_reference) {
		this.string_user_message_reference = string_user_message_reference;
	}

	public void setString_user_response_code(String string_user_response_code) {
		this.string_user_response_code = string_user_response_code;
	}

	public void setUser_message_reference(short user_message_reference) {
		this.user_message_reference = user_message_reference;
	}

	public void setUser_response_code(short user_response_code) {
		this.user_response_code = user_response_code;
	}

	public void addVsop(short tag, short len, byte[] value) {
		Tlv t = new Tlv(tag, len, value);
		vs_ops.add(t);
	}

	public void removeVsop(short tag) {
		int i = 0;
		for (Tlv tlv : vs_ops) {
			if (tlv.getTag() == tag) {
				vs_ops.remove(i);
				return;
			}
			i++;
		}
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public short getUssd_service_op() {
		return ussd_service_op;
	}

	public void setUssd_service_op(byte ussdServiceOp) {
		string_ussd_service_op = Byte.toString(ussdServiceOp);
		ussd_service_op = ussdServiceOp;
	}

	public ArrayList<Tlv> getVs_ops() {
		return vs_ops;
	}

}