/****************************************************************************
 * MoMessagePool.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/MoMessagePool.java,v 1.1 2012/07/24 14:49:00 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;

import com.seleniumsoftware.SMPPSim.exceptions.InvalidHexStringlException;
import com.seleniumsoftware.SMPPSim.pdu.*;
import com.seleniumsoftware.SMPPSim.util.Utilities;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class MoMessagePool {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	private Vector<DeliverSM> messages;

	private BufferedReader messagesReader;

	private String source_addr;

	private String destination_addr;

	private byte[] short_message;

	private int data_coding = 0;

	private int recno = 0;

	public MoMessagePool() {
	}

	public MoMessagePool(String filename) {
		String record = null;
		DeliverSM msg;
		messages = new Vector<DeliverSM>();
		try {
			messagesReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException fnfe) {
			logger.warning("MoMessagePool: file not found: " + filename);
			fnfe.printStackTrace();
		}

		do {
			try {
				record = messagesReader.readLine();
				String therecord;
				if (record == null)
					therecord = "null";
				else
					therecord = record;
				logger.finest("Read from file:<" + therecord + ">");
				if (record != null) {
					msg = new DeliverSM();
					try {
						getMessageAttributes(record);
					} catch (Exception e) {
						logger
								.warning("Error processing delivery_messages file, record number"
										+ (recno + 1));
						logger.warning(e.getMessage());
						e.printStackTrace();
						continue;
					}
					msg.setSource_addr(source_addr);
					msg.setDestination_addr(destination_addr);
					msg.setShort_message(short_message);
					msg.setData_coding(data_coding);
					messages.add(msg);
					recno++;
					logger.finest("Added delivery_message: " + source_addr
							+ "," + destination_addr + "," + short_message);
				}
			} catch (Exception e) {
				logger.warning("Error processing delivery_messages file");
				logger.info(e.getMessage());
				e.printStackTrace();
			}
		} while (record != null);
		logger.finest("loaded " + recno + " delivery messages");
	}

	private void getMessageAttributes(String rec) throws Exception {
		int commaIX1;
		int commaIX2;
		String msg = "";
		commaIX1 = rec.indexOf(",");
		if (commaIX1 != -1) {
			source_addr = rec.substring(0, commaIX1);
			commaIX2 = rec.indexOf(",", commaIX1 + 1);
			if (commaIX2 != -1) {
				destination_addr = rec.substring(commaIX1 + 1, commaIX2);
				msg = rec.substring(commaIX2 + 1, rec.length());
				data_coding = 0;
				if (!msg.startsWith("0x")) 
					short_message = msg.getBytes();
				else {
					try {
						short_message = Utilities.getByteArrayFromHexString(msg.substring(2));
						data_coding = 4; // binary
					} catch (InvalidHexStringlException e) {
						logger.warning("Invalid hex string in MO service input file: <"+msg+">. Used as plain text instead.");
						short_message = msg.getBytes();
					}
				}
			} else {
				throw new Exception(
						"Invalid delivery message file format: record "
								+ (recno + 1));
			}
		} else {
			throw new Exception("Invalid delivery message file format: record "
					+ recno);
		}

	}

	protected DeliverSM getMessage() {
		int messageIX = (int) (Math.random() * recno);
		logger.finest("Selected delivery_message #" + messageIX);
		DeliverSM dsm = new DeliverSM();
		DeliverSM selected = messages.elementAt(messageIX);
		;
		dsm = (DeliverSM) selected.clone();
		return dsm;
	}

}