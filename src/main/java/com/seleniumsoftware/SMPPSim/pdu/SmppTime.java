/****************************************************************************
 * SmppTime.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/SmppTime.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

public class SmppTime {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	// SMPP Date components
	private String YY; // last two digits of the year (00-99)
	private String MM; // month (01-12)
	private String DD; // day (01-31)
	private String hh; // hour (00-23)
	private String mm; // minute (00-59)
	private String ss; // second (00-59)
	private String t; // tenths of second (0-9)
	private String nn; // Time difference in quarter hours between local
	// time (as expressed in the first 13 octets) and UTC
	// (Universal Time Constant) time (00-48).
	private String p;
	// "+": Local time is in quarter hours advanced in relation
	// to UTC time.
	// "-": Local time is in quarter hours retarded in relation
	// to UTC time.
	// "R": Local time is relative to the current SMSC time.

	// Note that SMPPSim currently only supports times with p="+" and nn="00"; Any other values are simply
	// ignored.

	private String milli;	// t plus "00"
	
	private String dateString;	// Whole field with nn and p sanitised if necessary
	
	// Equivalent Java date/time
	private Date datetime;
	
	public SmppTime(String smppTime) throws ParseException {
		String st = smppTime;
		// 050219133817000+
		YY = st.substring(0, 2);
		MM = st.substring(2, 4);
		DD = st.substring(4, 6);
		hh = st.substring(6, 8);
		mm = st.substring(8, 10);
		ss = st.substring(10, 12);
		t = st.substring(12, 13);
		nn = st.substring(13, 15);
		if (!nn.equals("00")) {
			logger.warning("SMPPSim currently only supports time component nn=00. Value replaced with 00");
			nn = "00";
		}
		p = st.substring(15, 16);
		if (!p.equals("+")) {
			logger.warning("SMPPSim currently only supports time component p=+. Value replaced with +");
			p = "+";
		}
		milli = t + "00";
		dateString = YY+MM+DD+hh+mm+ss+t+nn+p;
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmssSSS");
		datetime = formatter.parse(dateString);
	}

	public static String dateToSMPPString(Date date) {
		//put the date into the format required for SMPP:
		// YYMMDDhhmmsstnnp where items are as expected and t is tenths of a second, nn is time diff in qtr hours between local
		// time and UTC. p is "+" or "-" indicating the whether the nn difference is < or > UTC.

		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");

		// now append tenths of a second (hard-coded as zero, UTC diff and UTC sign
		return (formatter.format(date) + "000+");
	}

	/**
	 * @return
	 */
	public Date getDatetime() {
		return datetime;
	}

	/**
	 * @return
	 */
	public String getDD() {
		return DD;
	}

	/**
	 * @return
	 */
	public String getHh() {
		return hh;
	}

	/**
	 * @return
	 */
	public String getMm() {
		return mm;
	}

	/**
	 * @return
	 */
	public String getMM() {
		return MM;
	}

	/**
	 * @return
	 */
	public String getNn() {
		return nn;
	}

	/**
	 * @return
	 */
	public String getP() {
		return p;
	}

	/**
	 * @return
	 */
	public String getSs() {
		return ss;
	}

	/**
	 * @return
	 */
	public String getT() {
		return t;
	}

	/**
	 * @return
	 */
	public String getYY() {
		return YY;
	}

	/**
	 * @param date
	 */
	public void setDatetime(Date date) {
		datetime = date;
	}

	/**
	 * @param string
	 */
	public void setDD(String string) {
		DD = string;
	}

	/**
	 * @param string
	 */
	public void setHh(String string) {
		hh = string;
	}

	/**
	 * @param string
	 */
	public void setMm(String string) {
		mm = string;
	}

	/**
	 * @param string
	 */
	public void setMM(String string) {
		MM = string;
	}

	/**
	 * @param string
	 */
	public void setNn(String string) {
		nn = string;
	}

	/**
	 * @param string
	 */
	public void setP(String string) {
		p = string;
	}

	/**
	 * @param string
	 */
	public void setSs(String string) {
		ss = string;
	}

	/**
	 * @param string
	 */
	public void setT(String string) {
		t = string;
	}

	/**
	 * @param string
	 */
	public void setYY(String string) {
		YY = string;
	}

	/**
	 * @return
	 */
	public String getDateString() {
		return dateString;
	}

	/**
	 * @param string
	 */
	public void setDateString(String string) {
		dateString = string;
	}
	
	public String toString() {
		return dateString;
	}

}