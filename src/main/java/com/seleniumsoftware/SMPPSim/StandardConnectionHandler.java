/****************************************************************************
 * StandardConnectionHandler.java
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/StandardConnectionHandler.java,v 1.1 2012/07/24 14:49:00 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim;
import java.io.*;
import java.net.*;

import com.seleniumsoftware.SMPPSim.util.LoggingUtilities;
import org.slf4j.LoggerFactory;

public class StandardConnectionHandler implements Runnable {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(StandardConnectionHandler.class);
    
//	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");
	private Smsc smsc = Smsc.getInstance();
	private StandardProtocolHandler handler;
	boolean isConnected = false;
	byte[] response;
	byte[] packetLen = new byte[4];
	byte[] message;
	private ServerSocket ss;
	// reference to relevant server socket set up in SMPPSim
	InputStream is = null;
	OutputStream os = null;
	Socket socket = null;

	public StandardConnectionHandler() {
		// default constructor only included so that configurable StandardConnectionHandler selection is possible
	}

	public StandardConnectionHandler(ServerSocket useThisServerSocket) {
		ss = useThisServerSocket;
	}

	public void run() {
		runThread();
	}

	private void runThread() {

		do // process connections forever
			{
			do // {accept connection, create protocol handler, {read PDU,handle it}, close connection}
				{
				try {
					logger.info("StandardConnectionHandler waiting for connection");
					socket = ss.accept();
					logger.info(
						"StandardConnectionHandler accepted a connection");
					isConnected = true;
					is = socket.getInputStream();
					os = socket.getOutputStream();
					Class c =
						Class.forName(SMPPSim.getProtocolHandlerClassName());
					handler = (StandardProtocolHandler) c.newInstance();
					handler.setConnection(this);
					logger.info(
						"Protocol handler is of type " + handler.getName());
				} catch (Exception exception) {
					logger.error(
						"Exception processing connection: "
							+ exception.getMessage());
					logger.error(
						"Exception is of type: "
							+ exception.getClass().getName());
					exception.printStackTrace();
					try {
						socket.close();
					} catch (Exception e) {
						logger.error(
							"Could not close socket following exception");
						e.printStackTrace();
					}
				}
			} while (!isConnected);

			do // until UNBIND or state violation
				{
				try {
					logger.debug("at start of main loop");
					readPacketInto(is);
					smsc.writeBinarySme(message);
					if(SMPPSim.isCallback() && smsc.isCallback_server_online()) {
					   smsc.sent(response);
					} 
					logger.debug("read packet");
					handler.processMessage(message);
				} catch (SocketException se) {
					logger.info(
						"Socket exception: probably connection closed by client without UNBIND");
					se.printStackTrace();
					handler.getSession().setBound(false);
					if (handler.getSession().isReceiver())
						smsc.receiverUnbound();
					isConnected = false;
				} catch (Exception exception) {
					logger.info(exception.getMessage());
					exception.printStackTrace();
					try {
						socket.close();
					} catch (Exception e) {
						logger.error(
							"Could not close socket following exception");
						e.printStackTrace();
					}
					handler.getSession().setBound(false);
					isConnected = false;
				}
			} while (isConnected);
			logger.debug("leaving connection handler main loop");
		}
		while (true);
	}

	protected boolean isBound() {
		// we only have a ProtocolHandler if this StandardConnectionHandler is connected
		if (isConnected) {
			return handler.getSession().isBound();
		} else {
			return false;
		}
	}

	protected boolean isReceiver() {
		return handler.getSession().isReceiver();
	}

//TODO Review why addressIsServicedByReceiver is in the StandardConnectionHandler class?!
	protected boolean addressIsServicedByReceiver(String address) {
		if (isConnected) {
			return handler.addressIsServicedByReceiver(address);
		} else {
			return false;
		}
	}

	private static final int getBytesAsInt(byte i_byte) {
		return i_byte & 0xff;
	}

	private int readPacketInto(InputStream is) throws IOException {
		logger.debug("starting readPacketInto");
		// Read the length of the incoming packet...
		int len;

		logger.debug("reading cmd_len");
		packetLen[0] = (byte) is.read();
		packetLen[1] = (byte) is.read();
		packetLen[2] = (byte) is.read();
		packetLen[3] = (byte) is.read();
		logger.debug("Got cmd_len");

		//put that into the packet header
		len =
			(getBytesAsInt(packetLen[0]) << 24)
				| (getBytesAsInt(packetLen[1]) << 16)
				| (getBytesAsInt(packetLen[2]) << 8)
				| (getBytesAsInt(packetLen[3]));

		if (packetLen[3] == -1) {
			logger.error("packetLen[3] == -1, throwing EOFException");
			throw new EOFException();
		}

		logger.debug("Reading " + len + " bytes");

		message = new byte[len];
		message[0] = packetLen[0];
		message[1] = packetLen[1];
		message[2] = packetLen[2];
		message[3] = packetLen[3];
		for (int i = 4; i < len; i++)
			message[i] = (byte) is.read();
		logger.debug("exiting readPacketInto");
		return len;
	}

	protected void writeResponse(byte[] response) throws IOException {
		os.write(response);
		os.flush();
		smsc.writeBinarySmppsim(response);
	}
	
	public void closeConnection()
		throws IOException {
		os.flush();
		os.close();
		socket.close();
		isConnected = false;
	}
	/**
	 * @return
	 */
	public StandardProtocolHandler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 */
	public void setHandler(StandardProtocolHandler handler) {
		this.handler = handler;
	}

	/**
	 * @param socket
	 */
	public void setSs(ServerSocket socket) {
		ss = socket;
	}
	/**
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * @param b
	 */
	public void setConnected(boolean b) {
		isConnected = b;
	}

}
