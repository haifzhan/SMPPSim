/****************************************************************************
 * MoService.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/MoService.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************
 */

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.InvalidHexStringlException;
import com.seleniumsoftware.SMPPSim.pdu.*;

import java.util.logging.*;

public class MoService implements Runnable {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	private Smsc smsc = Smsc.getInstance();

	private int messagesPerMin;

	boolean moServiceRunning = false;

	String deliveryFile;

	MoMessagePool messages;

	public MoService(String filename, int deliverMessagesPerMin) {
		deliveryFile = filename;
		messagesPerMin = deliverMessagesPerMin;
	}

	public void run() {
		logger.info("Starting MO Service....");
		try {
			messages = new MoMessagePool(deliveryFile);
		} catch (Exception e) {
			logger.warning("Exception creating MoMessagePool. "
					+ e.getMessage());
			e.printStackTrace();
		}

		try {
			runMoService();
		} catch (Exception e) {
			logger.warning("MO Service threw an Exception:" + e.getMessage()
					+ ". It's game over");
		}
	}

	private void runMoService() throws Exception {
		long timer = 0;
		long actualTime = 0;
		int sleepMS;
		sleepMS = (int) (60000 / messagesPerMin);
		int count = 0;
		int minCount = 0;
		DeliverSM newMessage;

		timer = System.currentTimeMillis();

		while (moServiceRunning) {
			newMessage = messages.getMessage();
			newMessage.setSm_length(newMessage.getShort_message().length);
			newMessage.setSeq_no(smsc.getNextSequence_No());
			logger.finest("MoService: DeliverSM object:"
					+ newMessage.toString());
			smsc.getIq().addMessage(newMessage);
			count++;
			minCount++;
			if (minCount == messagesPerMin) {
				actualTime = System.currentTimeMillis() - timer;
				logger.info(count + " MO messages inserted in InboundQueue. "
						+ minCount + " per minute target, actual time "
						+ actualTime + " ms");
				logger.info("drift = " + (actualTime - 60000));
				timer = System.currentTimeMillis();
				minCount = 0;
			}
			try {
				logger.finest("MO Service is sleeping for " + sleepMS
						+ " milliseconds");
				Thread.sleep(sleepMS);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} // while loop
	}

}
