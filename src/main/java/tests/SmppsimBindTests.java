package tests;
import tests.exceptions.*;
import junit.framework.*;
import java.util.logging.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;

public class SmppsimBindTests extends TestCase {

	String smppAccountName = "smppclient";
	String smppPassword = "password";
	String smppSystemType = "tests";
	String smppAddressRange = "[0-9]";
	boolean txBound;
	boolean rxBound;
	Session session;
	String smppHost = "localhost";
	int smppPort = 2775;
	private static Logger logger = Logger.getLogger("smppsim.tests");

	public SmppsimBindTests() {
	}

	public void test001BindTransmitter() throws BindTransmitterException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
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
		// Now unbind and disconnect ready for the next test
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	public void test002UnBind() throws BindTransmitterException {
		// First bind so that we can test unbinding
		Response resp = null;
		Connection conn;
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
		txBound = true;
		// Now on to the unbind test

		try {
			assertTrue(
				"BindTransmitter test must have failed so cannot unbind",
				txBound);
			logger.info("Connection is currently=" + conn);
			UnbindResp response = session.unbind();
			logger.info("Connection after unbind is=" + conn);
			assertEquals(
				"Unbind failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
			fail("Failed to unbind transmitter session " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test003BindTransmitterInvalidSystemID()
		throws BindTransmitterException {
		logger.info(
			"Attempting to establish transmitter session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId("banana");
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
		// Response object is null!
		assertEquals(
			"BindTransmitter authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		// Unbind and disconnect only if this test failed (and we bound)ready for the next test
		if (resp.getCommandStatus() == Data.ESME_ROK) {
			try {
				session.unbind();
				logger.info("Done unbind");
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
				logger.warning(
					"Unbind operation failed for TX session. "
						+ e.getMessage());
			}
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test004BindTransmitterInvalidPassword()
		throws BindTransmitterException {
		logger.info(
			"Attempting to establish transmitter session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
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
		// Response object is null!
		assertEquals(
			"BindTransmitter authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		txBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind twice with no intermediate unbind
	 * Expected: Operation returns response with cmd_status=ESME_RALYBND
	 */

	//	public void test005BindTransmitterTwice()
	//		throws BindTransmitterException {
	//			// TODO add debug to API to see if state checking really disabled and whether
	//			// state violation occuring
	//		Response resp = null;
	//		Connection conn;
	//		BindRequest breq;
	//		Session.getDebug().activate();
	//		try {
	//			conn = new TCPIPConnection(smppHost, smppPort);
	//			session = new Session(conn);
	//			breq = new BindTransmitter();
	//			breq.setSystemId(smppAccountName);
	//			breq.setPassword(smppPassword);
	//			breq.setInterfaceVersion((byte) 0x34);
	//			breq.setSystemType(smppSystemType);
	//			resp = session.bind(breq);
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//			fail(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//			throw new BindTransmitterException(
	//				"Exception whilst setting up or executing bind transmitter. "
	//					+ e.getMessage());
	//		}
	//		assertEquals(
	//			"BindTransmitter authentication should have passed: response was not ESME_ROK",
	//			Data.ESME_ROK,
	//			resp.getCommandStatus());
	//		txBound = true;
	//		try {
	//			session.disableStateChecking();
	//			logger.info("Disabled state checking ready for second bind attempt");
	//			// try to bind again
	//			resp = session.bind(breq);
	//			// TODO resp is null. Is this an API bug?
	//			logger.info("Executed second bind, resp="+resp);
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//			fail(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//			throw new BindTransmitterException(
	//				"Exception whilst setting up or executing 2nd bind transmitter. "
	//					+ e.getMessage());
	//		}
	//		assertEquals(
	//			"BindTransmitter authentication should have failed: response was not ESME_RALYBND",
	//			Data.ESME_RALYBND,
	//			resp.getCommandStatus());
	//		txBound = true;
	//
	//		// Now unbind and disconnect ready for the next test
	//		try {
	//			UnbindResp response = session.unbind();
	//		} catch (Exception e) {
	//			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
	//			logger.warning(
	//				"Unbind operation failed for TX session. " + e.getMessage());
	//		}
	//		Session.getDebug().deactivate();
	//	}

	public void test011BindReceiver() throws BindReceiverException {
		logger.info("Attempting to establish receiver session");
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
			breq.setAddressRange(smppAddressRange);
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
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for RX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test013BindReceiverInvalidSystemID()
		throws BindReceiverException {
		logger.info(
			"Attempting to establish transmitter session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId("banana");
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindReceiverException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindReceiver authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		txBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test014BindReceiverInvalidPassword()
		throws BindReceiverException {
		logger.info(
			"Attempting to establish receiver session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindReceiver();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
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
		// Response object is null!
		assertEquals(
			"BindReceiver authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		rxBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for RX session. " + e.getMessage());
		}
	}

	public void test021BindTransceiver() throws BindTransceiverException {
		logger.info("Attempting to establish Transceiver session");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind Transceiver. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransceiver failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established Transceiver session successfully");
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect systemid
	 * Expected: Operation returns response with cmd_status=ESME_RINVSYSID = 0x0F
	 */

	public void test023BindTransceiverInvalidSystemID()
		throws BindTransceiverException {
		logger.info(
			"Attempting to establish tranceiver session with invalid systemid");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId("banana");
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTranceiver authentication should have failed: response was not ESME_RINVSYSID",
			Data.ESME_RINVSYSID,
			resp.getCommandStatus());
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}

	/**
	 *
	 * Condition: Bind with incorrect password
	 * Expected: Operation returns response with cmd_status=ESME_RINVPASWD = 0x0E
	 */

	public void test034BindTransceiverInvalidPassword()
		throws BindTransceiverException {
		logger.info(
			"Attempting to establish transceiver session with invalid password");
		Response resp = null;
		Connection conn;
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId(smppAccountName);
			breq.setPassword("banana");
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			breq.setAddressRange(smppAddressRange);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			throw new BindTransceiverException(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
		}
		// Response object is null!
		assertEquals(
			"BindTransceiver authentication should have failed: response was not ESME_RINVPASWD",
			Data.ESME_RINVPASWD,
			resp.getCommandStatus());
		rxBound = true;
		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TXRX session. " + e.getMessage());
		}
	}

}