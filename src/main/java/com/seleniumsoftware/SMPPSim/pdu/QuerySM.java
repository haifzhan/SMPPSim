/****************************************************************************
 * QuerySM.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/QuerySM.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class QuerySM extends Request implements Demarshaller {

	// PDU attributes

	private String original_message_id;

	private int originating_ton;

	private int originating_npi;

	private String originating_addr;

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);
		// now set atributes of this specific PDU type
		int inx = 16;
		try {
			original_message_id = PduUtilities.getStringValueNullTerminated(
					request, inx, 65, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("QUERY_SM PDU is malformed. original_message_id is incorrect");
			throw (e);
		}
		inx = inx + original_message_id.length() + 1;
		try {
			originating_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("QUERY_SM PDU is malformed. originating_ton is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			originating_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("QUERY_SM PDU is malformed. originating_npi is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			originating_addr = PduUtilities.getStringValueNullTerminated(
					request, inx, 21, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("QUERY_SM PDU is malformed. originating_npi is incorrect");
			throw (e);
		}
		inx = inx + originating_addr.length() + 1;
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
	public String getOriginating_addr() {
		return originating_addr;
	}

	/**
	 * @return
	 */
	public int getOriginating_npi() {
		return originating_npi;
	}

	/**
	 * @return
	 */
	public int getOriginating_ton() {
		return originating_ton;
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
	public void setOriginating_addr(String string) {
		originating_addr = string;
	}

	/**
	 * @param i
	 */
	public void setOriginating_npi(int i) {
		originating_npi = i;
	}

	/**
	 * @param i
	 */
	public void setOriginating_ton(int i) {
		originating_ton = i;
	}

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString() + "," + "original_message_id="
				+ original_message_id + "," + "originating_ton="
				+ originating_ton + "," + "originating_npi=" + originating_npi
				+ "," + "originating_addr=" + originating_addr;
	}

}