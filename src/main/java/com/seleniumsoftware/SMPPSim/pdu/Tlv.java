/****************************************************************************
 * Tlv.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/Tlv.java,v 1.1 2012/07/24 14:48:58 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;
import java.io.Serializable;

import com.seleniumsoftware.SMPPSim.pdu.util.*;

public class Tlv implements Serializable {
	
	private short tag;
	private short len;
	private byte [] value;
	
	public Tlv() {
		
	}
	
	public Tlv(short t, short l, byte[] v) {
		tag = t;
		len = l;
		value = v;
	}
	
	/**
	 * @return Returns the len.
	 */
	public short getLen() {
		return len;
	}
	/**
	 * @param len The len to set.
	 */
	public void setLen(short len) {
		this.len = len;
	}
	/**
	 * @return Returns the tag.
	 */
	public short getTag() {
		return tag;
	}
	/**
	 * @param tag The tag to set.
	 */
	public void setTag(short tag) {
		this.tag = tag;
	}
	/**
	 * @return Returns the value.
	 */
	public byte[] getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public String toString() {
		return "tag="+tag+",len="+len+",value=0x"+PduUtilities.byteArrayToHexString(value);
	}
}