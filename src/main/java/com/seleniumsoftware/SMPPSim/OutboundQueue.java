/****************************************************************************
 * OutboundQueue.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/OutboundQueue.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************
*/
package com.seleniumsoftware.SMPPSim;
import com.seleniumsoftware.SMPPSim.exceptions.*;
import com.seleniumsoftware.SMPPSim.pdu.SubmitSM;

import java.util.logging.*;
import java.util.*;

/**
 * @author Martin Woolley
 *
 * Queue of MessageState objects
 * 
 * Processed by the State Lifecycle Service
 */
public class OutboundQueue implements Runnable  {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");
	private Smsc smsc = Smsc.getInstance();
	private LifeCycleManager lcm = smsc.getLcm();

	ArrayList<MessageState> queue;

	public OutboundQueue(int maxsize) {
		queue = new ArrayList<MessageState>(maxsize);
	}

	public synchronized void addMessageState(MessageState message)
		throws OutboundQueueFullException {
		logger.finest(
			"OutboundQueue: adding object to queue<"
				+ message.toString()
				+ ">");
		if (queue.size() < smsc.getOutbound_queue_capacity()) {
			queue.add(message);
			logger.fine(
				"Added object to OutboundQueue. Queue now contains "
					+ queue.size()
					+ " object(s)");
			if (message.isIntermediate_notification_requested()) {
				SubmitSM p = message.getPdu();
					// delivery_receipt requested
					logger.info("Intermediate notification requested");
					smsc.prepareDeliveryReceipt(p, message.getMessage_id(), message.getState(),1, 1,message.getErr());
			}
			notifyAll();
		} else
			throw new OutboundQueueFullException(
				"Request to add to OutboundQueue rejected as to do so would exceed max size of "
					+ smsc.getOutbound_queue_capacity());
	}

	public synchronized void setResponseSent(MessageState m) throws MessageStateNotFoundException {
		m.setResponseSent(true);
		updateMessageState(m);
	}

	public synchronized MessageState getMessageState(MessageState m)
		throws MessageStateNotFoundException {
//		logger.info("getMessageState:"+m.keyToString());
		int i = queue.indexOf(m);
//		logger.info("queue pos="+i);
		if (i > -1) {
			MessageState message = queue.get(i);
			return message;
		} else {
			throw new MessageStateNotFoundException();
		}
	}

	public synchronized void updateMessageState(MessageState newMs)
		throws MessageStateNotFoundException {
		int i = queue.indexOf(newMs);
		if (i > -1) {
			queue.set(i, newMs);
		} else {
			throw new MessageStateNotFoundException();
		}
	}

	public MessageState queryMessageState(
		String message_id,
		int ton,
		int npi,
		String addr)
		throws MessageStateNotFoundException {
		MessageState m = new MessageState();
		m.setMessage_id(message_id);
		m.setSource_addr_ton(ton);
		m.setSource_addr_npi(npi);
		m.setSource_addr(addr);
		return getMessageState(m);
	}

	public synchronized void removeMessageState(MessageState m) {
		int i = queue.indexOf(m);
		if (i > -1) {
			MessageState message = queue.remove(i);
			logger.fine(
				"Removed object from OutboundQueue. Queue now contains "
					+ queue.size()
					+ " object(s)");
		} else {
			logger.warning(
				"Attempt to remove non-existent object from OutboundQueue: "
					+ m.toString());
		}
	}

	public synchronized Object[] getAllMessageStates() {
		return (Object[]) queue.toArray();
	}

	protected synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	public void run() {
		// This code processes the contents of the OutboundQueue
		// Each object in the queue is a MessageState object and the purpose of
		// the OutboundQueue is to support QUERY_SM and REPLACE_SM operations.
		//
		// This code is termed the Lifecycle Manager Service.

		logger.info("Starting Lifecycle Service (OutboundQueue)");
		do // process queue forever
			{
			processQueue();
		} while (true);
	}

	private void processQueue() {
		Object[] messages;
		int count;
		MessageState m;
		int oldState;
		long start;
		long finish;
		long duration;
		long sleeptime;
		synchronized (this) {
			while (isEmpty()) {
				try {
					logger.info(
						"Lifecycle Service: OutboundQueue is Empty  - waiting");
					wait();
				} catch (InterruptedException e) {
					logger.log(
						Level.WARNING,
						"Exception in OutboundQueue: " + e.getMessage(),
						e);
				}
			}
			start = System.currentTimeMillis();
			messages = getAllMessageStates();
			// Can allow other threads to compete for lock now since working on array from this point
			notifyAll();
		}
		count = messages.length;
		//logger.info("Assessing state of " + count + " messages in the OutboundQueue");
		for (int i = 0; i < count; i++) {
			m = (MessageState) messages[i];
			if (!m.responseSent()) {
				//logger.finest("Response not yet sent so ignoring this MessageState object for now");
				continue;
			}
			if (lcm.messageShouldBeDiscarded(m)) {
				//logger.finest("Disarding OutboundQueue message " + m.toString());
				removeMessageState(m);
			} else {
				byte currentState = m.getState();
				m = lcm.setState(m);
			}
		}
		finish = System.currentTimeMillis();
		duration = finish - start;
		sleeptime = SMPPSim.getMessageStateCheckFrequency() - duration;
		if (sleeptime > 0) {
			try {
				logger.finest(
					"Lifecycle Service sleeping for "
						+ sleeptime
						+ " milliseconds");
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {
				logger.warning(
					"Exception whilst Lifecycle Service was sleeping "
						+ e.getMessage());
			}
		} else {
			logger.warning(
				"It's taking longer to process the OutboundQueue than MESSAGE_STATE_CHECK_FREQUENCY milliseconds. Recommend this value is increased");
		}
	}

	public int size() {
		return queue.size();
	}
}
