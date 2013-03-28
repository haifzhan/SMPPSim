package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.net.*;
import java.util.logging.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;

public class SmppsimQuerySmTests extends TestCase {

	String smppServiceType = "tests";
	String srcAddress = "12345";
	String destAddress = "4477805432122";
	String smppAccountName = "smppclient";
	String smppPassword = "password";
	String smppSystemType = "tests";
	String smppAddressRange = "[0-9]";
	boolean txBound;
	boolean rxBound;
	Session session;
	String smppHost = "localhost";
	int smppPort = 2775;
	int smppAltPort1 = 2776;
	private static Logger logger = Logger.getLogger("smppsim.tests");

	public SmppsimQuerySmTests() {
	}

	/*
	 * Condition: Basic query_sm sent
	 * 
	 * Expected: No exceptions. ESME_ROK in response.
	 */

	public void test001QuerySM()
		throws BindTransmitterException, QuerySmFailedException, SubmitSmFailedException, SocketException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		// get a transmitter session
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransmitter failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established transmitter session successfully");

		// now send a message

		String messageid = "";
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setDestAddr(destAddress);
			request.setShortMessage("SUBMIT_SM test using JUnit");

			// send the request

			request.assignSequenceNumber(true);
			response = session.submit(request);
			messageid = response.getMessageId();
			assertEquals(
				"SUBMIT_SM failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new SubmitSmFailedException();
		}

		// Now query the message state
		try {
			QuerySM request = new QuerySM();
			QuerySMResp response;

			// set values
			request.setMessageId(messageid);
			request.setSourceAddr(srcAddress);

			// send the request
			response = session.query(request);
			messageid = response.getMessageId();
			assertEquals(
				"QUERY_SM failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuerySmFailedException();
		}

		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}
	
	// TODO test message states using DeterministicLifeCycleManager

}