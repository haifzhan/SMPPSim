/****************************************************************************
 * Response.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/Response.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;
import com.seleniumsoftware.SMPPSim.pdu.util.*;

import java.io.*;

abstract public class Response extends Pdu implements Marshaller {

	transient ByteArrayOutputStream out = new ByteArrayOutputStream();

	public void prepareHeaderForMarshalling() throws Exception {
		out.reset();
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_len(),4));
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_id(),4));
		out.write(PduUtilities.makeByteArrayFromInt(getCmd_status(),4));
		out.write(PduUtilities.makeByteArrayFromInt(getSeq_no(),4));
	}
	
	public byte [] errorResponse(int cmd_id, int cmd_status, int seq_no) throws Exception {
		out.reset();
		setCmd_len(16);
		setCmd_id(cmd_id);
		setCmd_status(cmd_status);
		setSeq_no(seq_no);
		prepareHeaderForMarshalling();
		byte [] response = out.toByteArray();
		return response;
	}
	
	public String toString() {
		return super.toString();
	}

}