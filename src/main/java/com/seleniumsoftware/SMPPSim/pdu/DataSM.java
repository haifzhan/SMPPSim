package com.seleniumsoftware.SMPPSim.pdu;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;

/****************************************************************************
 * DataSM
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
 * @author Jean-Cedric Desrochers
 * http://www.woolleynet.com
 * http://www.seleniumsoftware.com
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/DataSM.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

public class DataSM extends Request implements Demarshaller, Marshaller {

	private static Logger logger = Logger
			.getLogger("com.seleniumsoftware.smppsim");

	// Mandatory PDU attributes
	private String service_type;

	private int source_addr_ton;

	private int source_addr_npi;

	private String source_addr;

	private int dest_addr_ton;

	private int dest_addr_npi;

	private String destination_addr;

	private int esm_class;

	private int registered_delivery_flag;

	private int data_coding;

	// Optional PDU attributes
	private HashMap<Short, Tlv> optionalsByTag = new HashMap<Short, Tlv>();

	// output 
	ByteArrayOutputStream out;

	public DataSM() {
	}

	public DataSM(DataSM msg) {
		// message header fields except message length
		setCmd_id(PduConstants.DATA_SM);
		setCmd_status(PduConstants.ESME_ROK);
		setSeq_no(msg.getSeq_no());
		// Set message length to zero since actual length will not be known until the object is
		// converted back to a message complete with null terminated strings
		setCmd_len(0);
		// message body
		service_type = msg.getService_type();
		dest_addr_ton = msg.getSource_addr_ton();
		dest_addr_npi = msg.getSource_addr_npi();
		destination_addr = msg.getSource_addr();
		source_addr_ton = msg.getDest_addr_ton();
		source_addr_npi = msg.getDest_addr_npi();
		source_addr = msg.getDestination_addr();
		esm_class = msg.getEsm_class();
		registered_delivery_flag = msg.getRegistered_delivery_flag();
		data_coding = msg.getData_coding();
		// optionnal
		for (Iterator<Short> it = msg.getOptionnalTags().iterator(); it
				.hasNext();) {
			Short tag = it.next();
			Tlv opt = msg.getOptionnal(tag.shortValue());
			optionalsByTag.put(tag, opt);
		}
	}

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);

		// now set mandatory attributes of this specific PDU type
		// service_type
		int inx = 16;
		try {
			service_type = PduUtilities.getStringValueNullTerminated(request,
					inx, 6, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. service_type is incorrect");
			throw (e);
		}

		// source_addr_ton
		inx = inx + service_type.length() + 1;
		try {
			source_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. source_addr_ton is incorrect");
			throw (e);
		}

		// source_addr_npi
		inx = inx + 1;
		try {
			source_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. source_addr_npi is incorrect");
			throw (e);
		}

		// source_addr
		inx = inx + 1;
		try {
			source_addr = PduUtilities.getStringValueNullTerminated(request,
					inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger.severe("DATA_SM PDU is malformed. source_addr is incorrect");
			throw (e);
		}

		// dest_addr_ton
		inx = inx + source_addr.length() + 1;
		try {
			dest_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. dest_addr_ton is incorrect");
			throw (e);
		}

		// dest_addr_npi
		inx = inx + 1;
		try {
			dest_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. dest_addr_npi is incorrect");
			throw (e);
		}

		// dest_addr
		inx = inx + 1;
		try {
			destination_addr = PduUtilities.getStringValueNullTerminated(
					request, inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. destination_addr is incorrect");
			throw (e);
		}

		// esm_class
		inx = inx + destination_addr.length() + 1;
		try {
			esm_class = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger.severe("DATA_SM PDU is malformed. esm_class is incorrect");
			throw (e);
		}

		// registered_delivery
		inx = inx + 1;
		try {
			registered_delivery_flag = PduUtilities.getIntegerValue(request,
					inx, 1);
		} catch (Exception e) {
			logger
					.severe("DATA_SM PDU is malformed. registered_delivery_flag is incorrect");
			throw (e);
		}

		// data_coding
		inx = inx + 1;
		try {
			data_coding = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger.severe("DATA_SM PDU is malformed. data_coding is incorrect");
			throw (e);
		}

		// Now process optional parameters if there are any
		inx = inx + 1;
		short tag;
		short oplen;
		byte[] value;
		while (getCmd_len() > inx) {
			try {
				tag = (short) PduUtilities.getIntegerValue(request, inx, 2);
			} catch (Exception e) {
				logger.severe("DATA_SM PDU is malformed. TLV tag at position "
						+ inx + " is incorrect");
				throw (e);
			}
			inx = inx + 2;
			try {
				oplen = (short) PduUtilities.getIntegerValue(request, inx, 2);
			} catch (Exception e) {
				logger
						.severe("DATA_SM PDU is malformed. TLV length at position "
								+ inx + " is incorrect");
				throw (e);
			}
			inx = inx + 2;
			value = new byte[oplen];
			for (int i = 0; i < oplen; i++) {
				value[i] = request[inx];
				inx++;
			}
			Tlv opt = new Tlv(tag, oplen, value);
			optionalsByTag.put(new Short(tag), opt);
		}
	}

	public byte[] marshall() throws Exception {
		out = new ByteArrayOutputStream();
		out.reset();
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_len(), 4));
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_id(), 4));
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_status(), 4));
		out.write(PduUtilities.makeByteArrayFromInt(getSeq_no(), 4));

		out.write(PduUtilities.stringToNullTerminatedByteArray(service_type));
		out.write(PduUtilities.makeByteArrayFromInt(source_addr_ton, 1));
		out.write(PduUtilities.makeByteArrayFromInt(source_addr_npi, 1));
		out.write(PduUtilities.stringToNullTerminatedByteArray(source_addr));
		out.write(PduUtilities.makeByteArrayFromInt(dest_addr_ton, 1));
		out.write(PduUtilities.makeByteArrayFromInt(dest_addr_npi, 1));
		out.write(PduUtilities
				.stringToNullTerminatedByteArray(destination_addr));
		out.write(PduUtilities.makeByteArrayFromInt(esm_class, 1));
		out.write(PduUtilities
				.makeByteArrayFromInt(registered_delivery_flag, 1));
		out.write(PduUtilities.makeByteArrayFromInt(data_coding, 1));

		for (Iterator<Tlv> it = optionalsByTag.values().iterator(); it
				.hasNext();) {
			Tlv opt = it.next();
			out.write(PduUtilities.makeByteArrayFromInt(opt.getTag(), 2));
			out.write(PduUtilities.makeByteArrayFromInt(opt.getLen(), 2));
			out.write(opt.getValue());
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
	public int getRegistered_delivery_flag() {
		return registered_delivery_flag;
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

	public boolean hasOptionnal(short aTag) {
		return optionalsByTag.containsKey(new Short(aTag));
	}

	public Tlv getOptionnal(short aTag) {
		return optionalsByTag.get(new Short(aTag));
	}

	public List<Short> getOptionnalTags() {
		return new ArrayList<Short>(optionalsByTag.keySet());
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
	public void setRegistered_delivery_flag(int i) {
		registered_delivery_flag = i;
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

	public void setOptionnal(Tlv opt) {
		optionalsByTag.put(new Short(opt.getTag()), opt);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString() + "," + "service_type=" + service_type + ","
				+ "source_addr_ton=" + source_addr_ton + ","
				+ "source_addr_npi=" + source_addr_npi + "," + "source_addr="
				+ source_addr + "," + "dest_addr_ton=" + dest_addr_ton + ","
				+ "dest_addr_npi=" + dest_addr_npi + "," + "dest_addr="
				+ destination_addr + "," + "esm_class=" + esm_class + ","
				+ "registered_delivery_flag=" + registered_delivery_flag + ","
				+ "data_coding=" + data_coding);

		if (optionalsByTag.size() > 0) {
			for (Iterator<Tlv> it = optionalsByTag.values().iterator(); it
					.hasNext();) {
				sb.append(",").append(it.next().toString());
			}
		}
		return sb.toString();
	}
}
