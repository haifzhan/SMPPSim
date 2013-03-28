/****************************************************************************
 * Outbind.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/Outbind.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import com.seleniumsoftware.SMPPSim.Smsc;
import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class Outbind extends Response implements Marshaller {

	private Smsc smsc = Smsc.getInstance();

	// PDU attributes

	private String system_id="";

	private String password="";

	public Outbind(String system_id,String password) {
		// message header fields except message length
		setCmd_id(PduConstants.OUTBIND);
		setCmd_status(PduConstants.ESME_ROK);
		setSeq_no(smsc.getNextSequence_No());
		// Set message length to zero since actual length will not be known until the object is
		// converted back to a message complete with null terminated strings
		setCmd_len(0);
		this.system_id = system_id;
		this.password = password;
	}
	
	public byte[] marshall() throws Exception {
		out.reset();
		super.prepareHeaderForMarshalling();
		logger.finest("Prepared header for marshalling");
		out.write(PduUtilities.stringToNullTerminatedByteArray(system_id));
		logger.finest("marshalled system_id");
		out.write(PduUtilities.stringToNullTerminatedByteArray(password));
		logger.finest("marshalled password");
		byte[] response = out.toByteArray();
		int l = response.length;
		response = PduUtilities.setPduLength(response, l);
		return response;
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
	 * *returns String representation of PDU
	 */
	public String toString() {
		return super.toString() + "," + "system_id=" + system_id + ","
				+ "password=" + password;
	}

}