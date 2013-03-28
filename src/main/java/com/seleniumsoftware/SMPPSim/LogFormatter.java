/****************************************************************************
 * LogFormatter.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/LogFormatter.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;
import java.util.logging.*;
import java.text.*;
import java.util.*;


public class LogFormatter extends java.util.logging.Formatter {

	private SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss S");

	public LogFormatter() {
	}

	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer();
		long logTime = rec.getMillis();
		Date logDate = new Date(logTime);
		String dateTime = (df.format(logDate)+"  ").substring(0,23);
		buf.append(dateTime);
		buf.append(" ");
		buf.append((rec.getLevel().getName()+"      ").substring(0,7));
		buf.append(" ");
		buf.append(rec.getThreadID());
		buf.append(" ");
		buf.append(formatMessage(rec));
		buf.append('\n');
		return buf.toString();
	}
}