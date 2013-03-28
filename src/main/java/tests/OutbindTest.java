package tests;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.*;

import com.logica.smpp.*;
import com.logica.smpp.pdu.*;
import com.seleniumsoftware.SMPPSim.pdu.PduConstants;
import com.seleniumsoftware.SMPPSim.pdu.util.PduUtilities;
import com.seleniumsoftware.SMPPSim.util.LoggingUtilities;

public class OutbindTest {

	String smppAccountName = "smppclient";

	String smppPassword = "password";

	String smppSystemType = "tests";

	String smppAddressRange = "[0-9]";

	boolean txBound;

	boolean rxBound;

	Session session;

	String smppHost = "localhost";

	int smppPort = 2775;

	int outbindPort = 2776;

	byte[] packetLen = new byte[4];
	
	byte[] message;

	private static Object mutex = new Object();

	private static Logger logger = Logger.getLogger("smppsim.tests");

	public static void main(String args[]) throws Exception {
		OutbindTest ot = new OutbindTest();
		ot.start();
	}

	public void start() throws Exception {
		logger.info("Outbind test starting.....");
		logger.info("Waiting for outbind.....");
		boolean outbind = false;
		ServerSocket ss = new ServerSocket(outbindPort);
		while (!outbind) {
			Socket socket = ss.accept();
			logger.info("Accepted a connection");
			InputStream is = socket.getInputStream();
			logger.info("Received client connection");
			boolean done = false;
			int i = readPacketInto(is);
			LoggingUtilities.hexDump("PDU", message, i);
			int cmd = PduUtilities.getIntegerValue(message, 4, 4);
			if (cmd == PduConstants.OUTBIND)
				outbind = true;
		}
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
		}
		logger.info("Established receiver session successfully");

		boolean got_all_mo = false;
		int mo_count=0;
		while (!got_all_mo) {
			PDU mo = session.receive(2000);
			if (mo != null) {
				logger.info(mo.debugString());
				mo_count++;
				Response response = ((Request)mo).getResponse();
				session.respond(response);
			} else
				got_all_mo = true;			
		}
		logger.info("Received "+mo_count+" MO messages");
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning("Unbind operation failed for RX session. "
					+ e.getMessage());
		}
		logger.info("End of test");
		System.exit(0);
	}

	private static final int getBytesAsInt(byte i_byte) {
		return i_byte & 0xff;
	}

	private int readPacketInto(InputStream is) throws IOException {
		int len;
		packetLen[0] = (byte) is.read();
		packetLen[1] = (byte) is.read();
		packetLen[2] = (byte) is.read();
		packetLen[3] = (byte) is.read();
		len =
			(getBytesAsInt(packetLen[0]) << 24)
				| (getBytesAsInt(packetLen[1]) << 16)
				| (getBytesAsInt(packetLen[2]) << 8)
				| (getBytesAsInt(packetLen[3]));

		if (packetLen[3] == -1) {
			logger.warning("packetLen[3] == -1, throwing EOFException");
			throw new EOFException();
		}

		logger.finest("Reading " + len + " bytes");
		message = new byte[len];
		message[0] = packetLen[0];
		message[1] = packetLen[1];
		message[2] = packetLen[2];
		message[3] = packetLen[3];
		for (int i = 4; i < len; i++)
			message[i] = (byte) is.read();
		return len;
	}


}