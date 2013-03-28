package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.io.IOException;
import java.net.*;
import java.util.logging.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;

public class SmppsimDeliverSmTests extends TestCase {

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
	int smppAltPort2 = 2777;
	private static Logger logger = Logger.getLogger("smppsim.tests");

	public SmppsimDeliverSmTests() {
	}

	/*
	 * Condition: Basic deliver_sm. Test will block until it receives a PDU. Requires an instance
	 * of SMPPSim running in delivery mode.
	 * Expected: No exceptions. ESME_ROK response.
	 */

	public void test001DeliverSM()
		throws DeliverSmFailedException, BindReceiverException, SocketException {
		logger.info("Attempting to establish receiver session");
		Response resp = null;
		Connection conn;
		// get a receiver session
		try {
			conn = new TCPIPConnection(smppHost, smppAltPort2);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange((byte) 1, (byte) 1, smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
			throw new BindReceiverException(
				"Exception whilst setting up or executing bind receiver. "
					+ e.getMessage());
		}
		assertEquals(
			"BindReceiver failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established receiver session successfully");

		// now wait for a message
		PDU pdu = null;
		DeliverSM deliversm;
		Response response;
		boolean gotDeliverSM = false;
		while (!gotDeliverSM) {
			try {
				// wait for a PDU
				pdu = session.receive();
				if (pdu != null) {
					if (pdu instanceof DeliverSM) {
						deliversm = (DeliverSM) pdu;
						gotDeliverSM = true;
						logger.info("Received DELIVER_SM");
						response = ((Request)pdu).getResponse();
						session.respond(response);

					} else {
						if (pdu instanceof EnquireLinkResp) {
							logger.finer("EnquireLinkResp received");
						} else {
							logger.warning(
								"Unexpected PDU of type: "
									+ pdu.getClass().getName()
									+ " received - discarding");
							fail(
								"Unexpected PDU type received"
									+ pdu.getClass().getName());
						}
					}
				}
			} catch (SocketException e) {
				fail("Connection has dropped for some reason");
			} catch (IOException ioe) {
				fail("IOException: " + ioe.getMessage());
			} catch (NotSynchronousException nse) {
				fail("NotSynchronousException: " + nse.getMessage());
			} catch (PDUException pdue) {
				fail("PDUException: " + pdue.getMessage());
			} catch (TimeoutException toe) {
				fail("TimeoutException: " + toe.getMessage());
			} catch (WrongSessionStateException wsse) {
				fail("WrongSessionStateException: " + wsse.getMessage());
			}
		}
		// Now unbind and disconnect ready for the next test
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for RX session. " + e.getMessage());
		}
	}

}