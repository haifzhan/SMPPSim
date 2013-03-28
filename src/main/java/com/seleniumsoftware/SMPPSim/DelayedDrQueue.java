/****************************************************************************
 * DelayedDrQueue.java
 *
 * Copyright (C) Martin Woolley 2001
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
 ****************************************************************************
 */
package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.InboundQueueFullException;
import com.seleniumsoftware.SMPPSim.pdu.*;

import java.util.logging.*;
import java.util.*;

public class DelayedDrQueue implements Runnable {

	private static DelayedDrQueue drqueue;

	private static Logger logger = Logger
			.getLogger("com.seleniumsoftware.smppsim");

	private Smsc smsc = Smsc.getInstance();

	private InboundQueue iqueue = InboundQueue.getInstance();

	ArrayList<DeliveryReceipt> dr_queue_pdus;

	private static final int period = 5000;
	
	private long delay_ms;

	public static DelayedDrQueue getInstance() {
		if (drqueue == null)
			drqueue = new DelayedDrQueue();
		return drqueue;
	}

	private DelayedDrQueue() {
		dr_queue_pdus = new ArrayList<DeliveryReceipt>();
		delay_ms = SMPPSim.getDelayReceiptsBy();
	}

	public void delayDeliveryReceipt(DeliveryReceipt pdu) {
		synchronized (dr_queue_pdus) {
			if (!dr_queue_pdus.contains(pdu)) {
				logger.finest("DelayedDrQueue: adding object to queue<"
						+ pdu.toString() + ">");
				dr_queue_pdus.add(pdu);
			}
			logger.finest("DelayedDrQueue: now contains " + dr_queue_pdus.size()
					+ " object(s)");
		}
	}

	public void run() {
		// this code periodically processes the contents of the delayed DR queue, moving
		// messages that are old enough to the active inbound queue for attempted delivery.

		logger.info("Starting DelayedDrQueue service....");

		while (true) {
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
			}
			int dcount = dr_queue_pdus.size();
			logger.finest("Processing " + dcount
					+ " messages in the delayed DR queue");

			synchronized (dr_queue_pdus) {
				for (int i = 0; i < dr_queue_pdus.size(); i++) {
					Pdu mo = dr_queue_pdus.get(i);
					try {
						DeliverSM dsm = (DeliverSM) mo;
						long earliest_delivery_time = (dsm.getCreated()+delay_ms);
						long now = System.currentTimeMillis();
						long diff = earliest_delivery_time - now;
						//logger.finest("Considering delivery receipt: "+(diff/1000)+" seconds to go");
						if (earliest_delivery_time < now) {
							iqueue.addMessage(mo);
							dr_queue_pdus.remove(mo);
						}
					} catch (InboundQueueFullException e) {
						// try again next time around
					}
				}
			}
		}
	}

}