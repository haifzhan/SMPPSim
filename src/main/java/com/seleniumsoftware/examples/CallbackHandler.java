package com.seleniumsoftware.examples;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class CallbackHandler implements Runnable {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.examples");

	ServerSocket ss;

	CallbackReceivable receiver;

	byte[] messageLen = new byte[4];

	byte[] message;

	boolean start_of_message = true;
	boolean running = true;

	int current_length = 0;

	int expected_length;

	public CallbackHandler(ServerSocket ss, CallbackReceivable receiver) {
		this.ss = ss;
		this.receiver = receiver;
	}

	public void run() {
		running = true;
		Socket socket = null;
		boolean isConnected = false;

		InputStream is = null;
		do // process connections forever
		{
			do // {accept connection, create protocol handler, {read PDU,handle
			// it}, close connection}
			{
				try {
					socket = ss.accept();
					isConnected = true;
					is = socket.getInputStream();
					logger.info("CallbackHandler has accepted a connection");
				} catch (Exception exception) {
					logger.warning("Exception processing connection: "
							+ exception.getMessage());
					logger.warning("Exception is of type: "
							+ exception.getClass().getName());
					exception.printStackTrace();
					try {
						socket.close();
					} catch (Exception e) {
						logger
								.warning("Could not close socket following exception");
						e.printStackTrace();
					}
				}
			} while (!isConnected);

			do // until the end of time
			{
				try {
					readPacketInto(is);
					if (isReceived(message))
						receiver.received(message);
					else
						receiver.sent(message);
				} catch (SocketException se) {
					logger
							.info("Socket exception: probably connection closed by client without warning");
					se.printStackTrace();
					isConnected = false;
				} catch (Exception exception) {
					logger.info(exception.getMessage());
					exception.printStackTrace();
					try {
						socket.close();
					} catch (Exception e) {
						logger
								.warning("Could not close socket following exception");
						e.printStackTrace();
					}
					isConnected = false;
				}
			} while (isConnected);
			logger.finest("leaving callback handler main loop");
		} while (running);
	}

	private int readPacketInto(InputStream is) throws IOException {
		logger.finest("starting readPacketInto");
		// Read the length of the incoming message...
		int len;

		logger.finest("reading message length");
		messageLen[0] = (byte) is.read();
		messageLen[1] = (byte) is.read();
		messageLen[2] = (byte) is.read();
		messageLen[3] = (byte) is.read();

		// put that into the message header
		len = (getBytesAsInt(messageLen[0]) << 24)
				| (getBytesAsInt(messageLen[1]) << 16)
				| (getBytesAsInt(messageLen[2]) << 8)
				| (getBytesAsInt(messageLen[3]));

		if (messageLen[3] == -1) {
			logger.warning("messageLen[3] == -1, throwing EOFException");
			throw new EOFException();
		}

		logger.finest("Reading " + len + " bytes");

		message = new byte[len];
		message[0] = messageLen[0];
		message[1] = messageLen[1];
		message[2] = messageLen[2];
		message[3] = messageLen[3];
		for (int i = 4; i < len; i++)
			message[i] = (byte) is.read();
		logger.finest("exiting readPacketInto");
		return len;
	}

	private static final int getBytesAsInt(byte i_byte) {
		return i_byte & 0xff;
	}

	private boolean isReceived(byte[] message) {
		return (message[4] == 0x01);
	}
	
	public void setRunning(boolean state) {
		running = state;
	}
}