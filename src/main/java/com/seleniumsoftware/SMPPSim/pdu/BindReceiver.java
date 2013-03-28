/****************************************************************************
 * BindReceiver.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/BindReceiver.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class BindReceiver extends Request implements Demarshaller {

	// PDU attributes

	private String system_id;

	private String password;

	private String system_type;

	private int interface_version;

	private int addr_ton;

	private int addr_npi;

	private String address_range;

	public BindReceiver() {
	}

	public void demarshall(byte[] request) throws Exception {

		// demarshall the header
		super.demarshall(request);
		// now set atributes of this specific PDU type
		int inx = 16;
		try {
			system_id = PduUtilities.getStringValueNullTerminated(request, inx,
					16, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. system_id is incorrect");
			throw (e);
		}
		inx = inx + system_id.length() + 1;
		try {
			password = PduUtilities.getStringValueNullTerminated(request, inx,
					9, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. password is incorrect");
			throw (e);
		}
		inx = inx + password.length() + 1;
		try {
			system_type = PduUtilities.getStringValueNullTerminated(request,
					inx, 13, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. system_type is incorrect");
			throw (e);
		}
		inx = inx + system_type.length() + 1;
		try {
			interface_version = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. interface_version is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			addr_ton = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. addr_ton is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			addr_npi = PduUtilities.getIntegerValue(request, inx, 1);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. addr_npi is incorrect");
			throw (e);
		}
		inx = inx + 1;
		try {
			address_range = PduUtilities.getStringValueNullTerminated(request,
					inx, 41, PduConstants.C_OCTET_STRING_TYPE);
		} catch (Exception e) {
			logger
					.severe("BIND_RECEIVER PDU is malformed. address_range is incorrect");
			throw (e);
		}
	}

	/**
	 * @return
	 */
	public int getAddr_npi() {
		return addr_npi;
	}

	/**
	 * @return
	 */
	public int getAddr_ton() {
		return addr_ton;
	}

	/**
	 * @return
	 */
	public String getAddress_range() {
		return address_range;
	}

	/**
	 * @return
	 */
	public int getInterface_version() {
		return interface_version;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	public String getSystem_id() {
		return system_id;
	}

	/**
	 * @return
	 */
	public String getSystem_type() {
		return system_type;
	}

	/**
	 * @param i
	 */
	public void setAddr_npi(int i) {
		addr_npi = i;
	}

	/**
	 * @param i
	 */
	public void setAddr_ton(int i) {
		addr_ton = i;
	}

	/**
	 * @param string
	 */
	public void setAddress_range(String string) {
		address_range = string;
	}

	/**
	 * @param i
	 */
	public void setInterface_version(int i) {
		interface_version = i;
	}

	/**
	 * @param string
	 */
	public void setPassword(String string) {
		password = string;
	}

	/**
	 * @param string
	 */
	public void setSystem_id(String string) {
		system_id = string;
	}

	/**
	 * @param string
	 */
	public void setSystem_type(String string) {
		system_type = string;
	}

	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString() + "," + "system_id=" + system_id + ","
				+ "password=" + password + "," + "system_type=" + system_type
				+ "," + "interface_version=" + interface_version + ","
				+ "addr_ton=" + addr_ton + "," + "addr_npi=" + addr_npi + ","
				+ "address_range=" + address_range;
	}
}