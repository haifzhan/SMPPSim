package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.net.*;
import java.util.logging.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;

public class SmppsimReplaceSmTests extends TestCase {

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

	public SmppsimReplaceSmTests() {
	}

	/*
	 * Condition: Basic cancel_sm sent
	 * 
	 * Expected: No exceptions. ESME_ROK in response.
	 */

	public void test001ReplaceSM()
		throws BindTransmitterException, ReplaceSmFailedException, SubmitSmFailedException, SocketException {
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

			request.setScheduleDeliveryTime("090223170405000+");
			request.setValidityPeriod("090323170405000+");
			request.setRegisteredDelivery((byte)0);
			request.setSmDefaultMsgId((byte)0);
			request.setShortMessage("SUBMIT_SM test using JUnit");
			
			// TODO just noticed that null in a replacesm attribute means "don't change this attribute"

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

		// Now replace the message
		try {
			ReplaceSM request = new ReplaceSM();
			ReplaceSMResp response;

			// set values
			request.setMessageId(messageid);
			request.setSourceAddr(srcAddress);
			// Different values to the originals in all cases
			// Note have to manually inspect log to check this currently
			request.setScheduleDeliveryTime("100223170405000+");
			request.setValidityPeriod("100323170405000+");
			request.setRegisteredDelivery((byte)1);
			request.setSmDefaultMsgId((byte)1);
			request.setShortMessage("REPLACE_SM test using JUnit");

			// send the request
			response = session.replace(request);
			assertEquals(
				"REPLACE_SM failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new ReplaceSmFailedException();
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

}