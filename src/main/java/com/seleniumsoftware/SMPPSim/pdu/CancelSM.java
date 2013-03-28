/****************************************************************************
 * CancelSM.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/CancelSM.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class CancelSM extends Request implements Demarshaller {

	// PDU attributes

	private String service_type;

	private String original_message_id;

	private int source_addr_ton;

	private int source_addr_npi;

	private String source_addr;

	private int dest_addr_ton;

	private int dest_addr_npi;

	private String destination_addr;

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);
		// now set atributes of this specific PDU type
		int inx = 16;
		try {
			service_type = PduUtilities.getStringValueNullTerminated(request,
					inx, 6, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. service_type is incorrect");
			throw (e);
		}
		inx = inx + service_type.length() + 1;
		try {
			original_message_id = PduUtilities.getStringValueNullTerminated(
					request, inx, 65, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. original_message_id is incorrect");
			throw (e);
		}
		inx = inx + original_message_id.length() + 1;
		try {
			source_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. source_addr_ton is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			source_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. source_addr_npi is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			source_addr = PduUtilities.getStringValueNullTerminated(request,
					inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. source_addr is incorrect");
			throw (e);
		}
		inx = inx + source_addr.length() + 1;
		try {
			dest_addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. dest_addr_ton is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			dest_addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. dest_addr_npi is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			destination_addr = PduUtilities.getStringValueNullTerminated(
					request, inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("CANCEL_SM PDU is malformed. destination_addr is incorrect");
			throw (e);
		}
		inx = inx + destination_addr.length() + 1;
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
	public String getOriginal_message_id() {
		return original_message_id;
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
	 * @param string
	 */
	public void setOriginal_message_id(String string) {
		original_message_id = string;
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

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString() + "," + "service_type=" + service_type + ","
				+ "original_message_id=" + original_message_id + ","
				+ "source_addr_ton=" + source_addr_ton + ","
				+ "source_addr_npi=" + source_addr_npi + "," + "source_addr="
				+ source_addr + "," + "dest_addr_ton=" + dest_addr_ton + ","
				+ "dest_addr_npi=" + dest_addr_npi + "," + "destination_addr"
				+ destination_addr;
	}

}