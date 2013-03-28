/****************************************************************************
 * CallbackServerConnector.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/CallbackServerConnector.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;

import java.io.OutputStream;
import java.net.*;
import java.util.logging.*;

public class CallbackServerConnector implements Runnable {

	private Smsc smsc = Smsc.getInstance();

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	private Object mutex;

	public CallbackServerConnector(Object mutex) {
		this.mutex = mutex;
	}

	public void run() {

		if (SMPPSim.isCallback()) {
			synchronized (mutex) {
				boolean connected = false;
				Socket callback;
				while (!connected) {
					try {
						callback = new Socket(
								SMPPSim.getCallback_target_host(), SMPPSim
										.getCallback_port());
						connected = true;
						smsc.setCallback(callback);
						smsc.setCallback_stream(callback.getOutputStream());
						smsc.setCallback_server_online(true);
						logger.info("Connected to callback server");
					} catch (Exception ce) {
						try {
							logger
									.info("Callback server not accepting connections - retrying");
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}
					}
				}
				mutex.notifyAll();
			}
		}
	}
}