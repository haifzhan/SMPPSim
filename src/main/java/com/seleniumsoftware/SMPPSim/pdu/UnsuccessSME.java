/****************************************************************************
 * SMPPSubmitMultiRespUnsuccessDest.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/UnsuccessSME.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

public class UnsuccessSME {

	int dest_addr_ton;
	int dest_addr_npi;
	String destination_addr;
	int error_status_code;

	public UnsuccessSME() {
	}

	public UnsuccessSME(
		int dest_addr_ton,
		int dest_addr_npi,
		String destination_addr,
		int error_status_code) {
		this.dest_addr_ton = dest_addr_ton;
		this.dest_addr_npi = dest_addr_npi;
		this.destination_addr = destination_addr;
		this.error_status_code = error_status_code;
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
	public int getError_status_code() {
		return error_status_code;
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
	public void setError_status_code(int i) {
		error_status_code = i;
	}

	public String toString() {
		return "dest_addr_ton="+dest_addr_ton+","+
		"dest_addr_npi="+dest_addr_npi+","+
		"destination_addr="+destination_addr+","+
		"error_status_code="+error_status_code;
	}

}