/****************************************************************************
 * Pdu.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/Pdu.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

import java.io.Serializable;
import java.util.logging.Logger;

abstract public class Pdu implements Serializable {

	static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	// need this for RMI use
	public Pdu() {
		
	}
	// All PDUs have a header

	private int cmd_len;
	private int cmd_id;
	private int cmd_status;
	private int seq_no;

	public boolean equals(Object o) {
		if (o instanceof Pdu) {
			Pdu p = (Pdu) o;
			if (p.getSeq_no() == this.seq_no)
				return true;
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public int getCmd_id() {
		return cmd_id;
	}

	/**
	 * @return
	 */
	public int getCmd_len() {
		return cmd_len;
	}

	/**
	 * @return
	 */
	public int getCmd_status() {
		return cmd_status;
	}

	/**
	 * @return
	 */
	public int getSeq_no() {
		return seq_no;
	}

	/**
	 * @param i
	 */
	public void setCmd_id(int i) {
		cmd_id = i;
	}

	/**
	 * @param i
	 */
	public void setCmd_len(int i) {
		cmd_len = i;
	}

	/**
	 * @param i
	 */
	public void setCmd_status(int i) {
		cmd_status = i;
	}

	/**
	 * @param i
	 */
	public void setSeq_no(int i) {
		seq_no = i;
	}
	
	public String toString() {
		return "cmd_len="+cmd_len+","+
		"cmd_id="+cmd_id+","+
		"cmd_status="+cmd_status+","+
		"seq_no="+seq_no;
	}
}