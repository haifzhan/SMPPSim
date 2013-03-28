package com.seleniumsoftware.examples;

import java.net.ServerSocket;
import java.util.logging.Logger;

public class CallbackServer {
	
	private static Logger logger = Logger.getLogger("com.seleniumsoftware.examples");
	private CallbackHandler[] callbackHandlers;
	private ServerSocket ss;
	private int connections;
	
	public static void main (String [] args) throws Exception {
		logger.info("Starting example Callback Server..");
		CallbackReceiver receiver = new CallbackReceiver();
		CallbackServer server = new CallbackServer(10,3333,receiver);
	}
	
	public CallbackServer(int connections, int port, CallbackReceivable receiver) throws Exception {
		this.connections = connections;
		Thread callbackThread[] = new Thread[connections];
		int threadIndex = 0;
		callbackHandlers = new CallbackHandler[connections];
		try {
			ss = new ServerSocket(port, 10);
			logger.info("Example Callback Server is listening on port 3333");
		} catch (Exception e) {
			logger.severe("Exception creating CallbackServer server: " + e.toString());
			e.printStackTrace();
			throw e;
		}
		for (int i = 0; i < connections; i++) {
			CallbackHandler ch = new CallbackHandler(ss,receiver);
			callbackHandlers[threadIndex] = ch;
			callbackThread[threadIndex] = new Thread(callbackHandlers[threadIndex], "CH" + threadIndex);
			callbackThread[threadIndex].start();
			threadIndex++;
		}
	}
	
	public void stop() {
		for (int i = 0; i < connections; i++) {
			CallbackHandler ch = (CallbackHandler) callbackHandlers[i];
			ch.setRunning(false);
		}		
	}
}