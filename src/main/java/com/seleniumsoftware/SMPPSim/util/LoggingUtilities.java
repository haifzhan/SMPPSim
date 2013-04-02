/****************************************************************************
 * LoggingUtilities.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/util/LoggingUtilities.java,v 1.1 2012/07/24 14:49:00 martin Exp $
 ****************************************************************************/
package com.seleniumsoftware.SMPPSim.util;
import com.seleniumsoftware.SMPPSim.SMPPSim;
import com.seleniumsoftware.SMPPSim.pdu.*;

import java.util.StringTokenizer;
import java.util.logging.*;
import org.slf4j.LoggerFactory;


public class LoggingUtilities {

//	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingUtilities.class);
	public static void hexDump(String title, byte[] m, int l) 
        {
          //empty method, do nothing!
        }
//	public static void hexDump(String title, byte[] m, int l) {
//		int p = 0;
//		StringBuffer line = new StringBuffer();
//		logger.info(title);
//		logger.info("Hex dump (" + l + ") bytes:");
//		for (int i = 0; i < l; i++) {
//			if ((m[i] >= 0) & (m[i] < 16))
//				line.append("0");
//			line.append(Integer.toString(m[i] & 0xff, 16).toUpperCase());
//			if ((++p % 4) == 0) {
//				line.append(":");
//			}
//			if (p == 16) {
//				p = 0;
//				logger.info(line.toString());
//				line = new StringBuffer();
//			}
//		}
//		if (p != 16) {
//			logger.info(line.toString());
//		}
//	}

	public static void logDecodedPdu(Pdu p) 
        {
            //empty, do nothing!
        }
        
//	public static void logDecodedPdu(Pdu p) {
//		// Split into max 80 character lines around comma delimited boundaries
//		int i=0;
//		String pdustring = p.toString();
//		StringBuffer line=new StringBuffer();
//		String token="";
//		StringTokenizer st = new StringTokenizer(pdustring,",");
//		while (st.hasMoreElements()) {
//			token = st.nextToken();
//			if ((line.length() + token.length())<79) {
//				if (i > 0) {
//					line.append(",");
//				}
//				line.append(token);
//				i++;
//			} else {
//				logger.info(line.toString());
//				line = new StringBuffer();
//				line.append(token);
//				i = 1;
//			}			
//		}
//		if (line.length() > 0)
//			logger.info(line.toString());			
//	}

}