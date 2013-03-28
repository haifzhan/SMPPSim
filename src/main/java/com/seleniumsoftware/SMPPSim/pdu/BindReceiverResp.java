/****************************************************************************
 * BindReceiverResp.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/BindReceiverResp.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;

public class BindReceiverResp
	extends Response
	implements Marshaller {

	String system_id;

	public BindReceiverResp() {
	}

	public BindReceiverResp(
		BindReceiver requestMsg,
		String sysid) {
		// message header fields except message length
		setCmd_id(PduConstants.BIND_RECEIVER_RESP);
		setCmd_status(PduConstants.ESME_ROK);
		setSeq_no(requestMsg.getSeq_no());
		// Set message length to zero since actual length will not be known until the object is
		// converted back to a message complete with null terminated strings
		setCmd_len(0);

		// message body
		system_id = sysid;
	}

	public byte [] marshall() throws Exception {
		out.reset();
		super.prepareHeaderForMarshalling();
		out.write(PduUtilities.stringToNullTerminatedByteArray(system_id)); 
		byte [] response = out.toByteArray();
		int l = response.length;
		response = PduUtilities.setPduLength(response,l);
		return response;
	}
	/**
	 * *returns String representation of PDU
	 */
	public String toString() {
		return 	super.toString()+","+
				"system_id="+system_id;
	}

}