/****************************************************************************
 * SubmitMulti.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/SubmitMulti.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;
import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class SubmitMulti extends Request implements Demarshaller {

	// PDU attributes

	private String service_type;
	private int source_addr_ton;
	private int source_addr_npi;
	private String source_addr;
	private int number_of_dests;
	private DestAddress[] dest_addresses;
	private int esm_class;
	private int protocol_ID;
	private int priority_flag;
	private String schedule_delivery_time;
	private String validity_period;
	private int registered_delivery_flag;
	private int replace_if_present_flag;
	private int data_coding;
	private int sm_default_msg_id;
	private int sm_length;
	private byte [] short_message;

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);
		// now set atributes of this specific PDU type
		int inx = 16;
		try {
		service_type =
			PduUtilities.getStringValueNullTerminated(
				request,
				inx,
				6,
				PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. service_type is incorrect");
			throw (e);
		}	
		inx = inx + service_type.length() + 1;
		try {
		source_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. source_addr_ton is incorrect");
			throw (e);
		}	
		inx = inx + 1;
		try {
		source_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. source_addr_npi is incorrect");
			throw (e);
		}	
		inx = inx + 1;
		try {
		source_addr =
			PduUtilities.getStringValueNullTerminated(
				request,
				inx,
				21,
				PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. source_addr is incorrect");
			throw (e);
		}	
		inx = inx + source_addr.length() + 1;
		try {
		number_of_dests = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. number_of_dests is incorrect");
			throw (e);
		}	
		dest_addresses = new DestAddress[number_of_dests];
		inx = inx + 1;
		int dest_flag;
		for (int i = 0; i < number_of_dests; i++) {
			try {
			dest_flag = PduUtilities.getIntegerValue(request, inx, 1);
			} catch (Exception e) {
				logger
						.severe("SUBMIT_MULTI PDU is malformed. dest_flag at "+inx+" is incorrect");
				throw (e);
			}	
			inx++;
			if (dest_flag == PduConstants.SME_ADDRESS) {
				DestAddressSME sme = new DestAddressSME();
				sme.setDest_flag(dest_flag);
				try {
				sme.setSme_ton(PduUtilities.getIntegerValue(request, inx, 1));
				} catch (Exception e) {
					logger
							.severe("SUBMIT_MULTI PDU is malformed. sme ton at position "+inx+" is incorrect");
					throw (e);
				}	
				inx++;
				try {
				sme.setSme_npi(PduUtilities.getIntegerValue(request, inx, 1));
				} catch (Exception e) {
					logger
							.severe("SUBMIT_MULTI PDU is malformed. sme_npi at position "+inx+" is incorrect");
					throw (e);
				}	
				inx++;
				try {
				sme.setSme_address(
					PduUtilities.getStringValueNullTerminated(
						request,
						inx,
						21,
						PduConstants.C_OCTET_STRING_TYPE));
				} catch (Exception e) {
					logger
							.severe("SUBMIT_MULTI PDU is malformed. sme_address at position "+inx+" is incorrect");
					throw (e);
				}	
				inx = inx + sme.getSme_address().length() + 1;
				dest_addresses[i] = sme;
			} else if (dest_flag == PduConstants.DISTRIBUTION_LIST_NAME) {
				DestAddressDL dl = new DestAddressDL();
				dl.setDest_flag(dest_flag);
				try {
				dl.setDl_name(
					PduUtilities.getStringValueNullTerminated(
						request,
						inx,
						21,
						PduConstants.C_OCTET_STRING_TYPE));
				} catch (Exception e) {
					logger
							.severe("SUBMIT_MULTI PDU is malformed. dist_list_name at position "+inx+" is incorrect");
					throw (e);
				}	
				inx = inx + dl.getDl_name().length() + 1;
				dest_addresses[i] = dl;
			} else
				throw new Exception("Invalid dest_flag(" + dest_flag + ")");
		}
		try {
		esm_class = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. esm_class at position "+inx+" is incorrect");
			throw (e);
		}	
		inx = inx + 1;
		try {
		protocol_ID = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. protocol_id at position "+inx+" is incorrect");
			throw (e);
		}	
		inx = inx + 1;
		try {
		priority_flag = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. priority_flag at position "+inx+" is incorrect");
			throw (e);
		}	
		inx = inx + 1;
		try {
		schedule_delivery_time =
			PduUtilities.getStringValueNullTerminated(
				request,
				inx,
				17,
				PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. schedule_delivery_time at position "+inx+" is incorrect");
			throw (e);
		}	
		inx = inx + schedule_delivery_time.length() + 1;
		try {
		validity_period =
			PduUtilities.getStringValueNullTerminated(
				request,
				inx,
				17,
				PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("SUBMIT_MULTI PDU is malformed. validity_period at position "+inx+" is incorrect");
			throw (e);
		}	
		inx = inx + validity_period.length() + 1;
		registered_delivery_flag =
			PduUtilities.getIntegerValue(request, inx, 1);
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
			System.arraycopy(request, inx,	short_message, 0, sm_length);
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
	public DestAddress[] getDest_addresses() {
		return dest_addresses;
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
	public int getNumber_of_dests() {
		return number_of_dests;
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
	public byte [] getShort_message() {
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
	 * @param addresses
	 */
	public void setDest_addresses(DestAddress[] addresses) {
		dest_addresses = addresses;
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
	public void setNumber_of_dests(int i) {
		number_of_dests = i;
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
	public void setShort_message(byte [] msg) {
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

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString()+","
			+ ","
			+ "service_type="
			+ service_type
			+ ","
			+ "source_addr_ton="
			+ source_addr_ton
			+ ","
			+ "source_addr_npi="
			+ source_addr_npi
			+ ","
			+ "source_addr="
			+ source_addr
			+ ","
			+ "number_of_dests="
			+ number_of_dests
			+ ","
			+ destsToString(dest_addresses)
			+ ","
			+ "esm_class="
			+ esm_class
			+ ","
			+ "protocol_ID="
			+ protocol_ID
			+ ","
			+ "priority_flag="
			+ priority_flag
			+ ","
			+ "schedule_delivery_time="
			+ schedule_delivery_time
			+ ","
			+ "validity_period="
			+ validity_period
			+ ","
			+ "registered_delivery_flag="
			+ registered_delivery_flag
			+ ","
			+ "replace_if_present_flag="
			+ replace_if_present_flag
			+ ","
			+ "data_coding="
			+ data_coding
			+ ","
			+ "sm_default_msg_id="
			+ sm_default_msg_id
			+ ","
			+ "sm_length="
			+ sm_length
			+ ","
			+ "short_message="
			+ new String(short_message);
	}

	public String destsToString(DestAddress[] da) {
		int i = da.length;
		StringBuffer sb = new StringBuffer();

		for (int n = 0; n < i; n++) {
			sb.append("[" + n + "]");
			sb.append(da[n].toString());
			sb.append(",");
		}
		return sb.toString();
	}
}