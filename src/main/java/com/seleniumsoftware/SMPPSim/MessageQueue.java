/****************************************************************************
 * MessageQueue.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/MessageQueue.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************
*/
package com.seleniumsoftware.SMPPSim;
import com.seleniumsoftware.SMPPSim.pdu.*;

import java.util.*;
import java.util.logging.*;

public class MessageQueue {
	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");
	private String queueName;
	private Vector<Pdu> queue;
	private Object message = new Object();

	public MessageQueue(String name) {
		queueName = name;
		queue = new Vector<Pdu>();
	}

	protected synchronized void addMessage(Pdu message) {
		logger.finest(
			"MessageQueue(" + queueName + ") : adding message to queue");
		queue.add(message);
		notifyAll();
	}

	protected synchronized Object getMessage() {
		while (isEmpty()) {
			try {
				logger.finest(
					"MessageQueue("
						+ queueName
						+ "):  waiting for message from queue");
				wait();
			} catch (InterruptedException e) {
				logger.warning(
					"Exception in MessageQueue("
						+ queueName
						+ ") : "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
		message = queue.firstElement();
		queue.remove(message);
		notifyAll();
		return message;
	}

	protected synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

}
