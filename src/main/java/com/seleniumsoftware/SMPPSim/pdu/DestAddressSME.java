/****************************************************************************
 * DestAddressSME.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/DestAddressSME.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

public class DestAddressSME extends DestAddress {
	private int sme_ton;
	private int sme_npi;
	private String sme_address;

	/**
	 * @return
	 */
	public String getSme_address() {
		return sme_address;
	}

	/**
	 * @return
	 */
	public int getSme_npi() {
		return sme_npi;
	}

	/**
	 * @return
	 */
	public int getSme_ton() {
		return sme_ton;
	}

	/**
	 * @param string
	 */
	public void setSme_address(String string) {
		sme_address = string;
	}

	/**
	 * @param i
	 */
	public void setSme_npi(int i) {
		sme_npi = i;
	}

	/**
	 * @param i
	 */
	public void setSme_ton(int i) {
		sme_ton = i;
	}

	public String toString() {
		return super.toString()
			+ ","
			+ "sme_ton="
			+ sme_ton
			+ ","
			+ "sme_npi="
			+ sme_npi
			+ ","
			+ "sme_address="
			+ sme_address;
	}
}