package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.io.IOException;
import java.net.*;
import java.util.logging.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;
import com.logica.smpp.util.*;

public class SmppsimSubmitSmTests extends TestCase {

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

	public SmppsimSubmitSmTests() {
	}

	/*
	 * Condition: Basic submit_sm with no special parameters such as delivery receipts requested
	 * Expected: No exceptions. ESME_ROK response.
	 */

	public void test001SubmitSM()
		throws SubmitSmFailedException, BindTransmitterException, SocketException {
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
			logger.info("Message submitted....");
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

		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
			logger.info("Unbound...");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/*
	 * Condition: submit_sm with "bad" MSISDN. Must run against SMPPSim with TestProtocolHandler1 .
	 * This protocol handler will treat destAddresses that are non-numeric as invalid and
	 * return ESME_RINVDSTADR
	 * Expected: No exceptions. ESME_RINVDSTADR response.
	 */

	public void test002SubmitSMBadDestAddress()
		throws SubmitSmFailedException, BindTransmitterException, SocketException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		// get a transmitter session
		try {
			conn = new TCPIPConnection(smppHost, smppAltPort1);
			session = new Session(conn);
			// Session.getDebug().activate();
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

		// now submit a mesage
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setDestAddr("ABCDE");
			request.setShortMessage(
				"SUBMIT_SM test with invalid MSISDN using JUnit");
			//			request.setRegisteredDelivery(om.getRegistered_delivery_flag());
			//			request.setDataCoding((byte) 0);

			// send the request

			request.assignSequenceNumber(true);
			response = session.submit(request);
			assertEquals(
				"SUBMIT_SM response incorrect: response was not ESME_RINVDSTADR",
				Data.ESME_RINVDSTADR,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new SubmitSmFailedException();
		}

		// now submit another mesage to see if this is still working
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setDestAddr(destAddress);
			request.setShortMessage(
				"SUBMIT_SM test with valid MSISDN using JUnit");
			//			request.setRegisteredDelivery(om.getRegistered_delivery_flag());
			//			request.setDataCoding((byte) 0);

			// send the request

			request.assignSequenceNumber(true);
			response = session.submit(request);
			assertEquals(
				"SUBMIT_SM response incorrect: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new SubmitSmFailedException();
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
	public void test003SubmitSMRequestDeliveryReceipt()
		throws
			SubmitSmFailedException,
			BindTransmitterException,
			BindReceiverException,
			SocketException {
		logger.info("Attempting to establish transceiver session");
		Response resp = null;
		Connection conn;
		Session session;
		// get a transceiver session
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransciever();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			fail(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transceiver. "
					+ e.getMessage());
		}
		assertEquals(
			"BindTransmitter failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established transceiver session successfully");

		// now send a message, requesting a delivery receipt
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setDestAddr(destAddress);
			request.setShortMessage("SUBMIT_SM test using JUnit");
			request.setRegisteredDelivery((byte) 1);
			//			request.setDataCoding((byte) 0);

			// send the request

			request.assignSequenceNumber(true);
			response = session.submit(request);
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

		// Now wait for the delivery receipt

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
						response = ((Request) pdu).getResponse();
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
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/*
	 * Condition: submit_sm without a source address
	 * Expected: No exceptions. ESME_ROK response.
	 */

	public void test004SubmitSmNoSourceAddress()
		throws SubmitSmFailedException, BindTransmitterException, SocketException {
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
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr("");
			request.setDestAddr(destAddress);
			request.setShortMessage(
				"SUBMIT_SM test using JUnit. No source address.");
			// send the request
			request.assignSequenceNumber(true);
			response = session.submit(request);
			logger.info("Message submitted....");
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
		// Now unbind and disconnect ready for the next test
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/*
	 * Condition: submit_sm without a destination address
	 * Expected: No exceptions. ESME_RINVDSTADR response.
	 */

	public void test005SubmitSmNoDestAddress()
		throws SubmitSmFailedException, BindTransmitterException, SocketException {
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
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setDestAddr("");
			request.setShortMessage(
				"SUBMIT_SM test using JUnit. No destination address.");
			// send the request
			request.assignSequenceNumber(true);
			response = session.submit(request);
			logger.info("Message submitted....");
			assertEquals(
				"SUBMIT_SM failed: response was not ESME_RINVDSTADR",
				Data.ESME_RINVDSTADR,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new SubmitSmFailedException();
		}
		// Now unbind and disconnect ready for the next test
		try {
			session.unbind();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/*
	 * Condition: submit_sm with all mandatory and many optional parameters of various types
	 * Expected: No exceptions. ESME_ROK response.
	 */

	public void test006SubmitSMwithOptionalParameters()
		throws SubmitSmFailedException, BindTransmitterException, SocketException {
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
		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;
			// set values
			byte [] bb = new byte [6];
			bb[0] = (byte) 0x01;
			bb[1] = (byte) 0x02;
			bb[2] = (byte) 0x03;
			bb[3] = (byte) 0x04;
			bb[4] = (byte) 0x05;
			bb[5] = (byte) 0x06;
			request.setCallbackNum(new ByteBuffer(bb));

			bb[5] = (byte) 0x01;
			bb[4] = (byte) 0x02;
			bb[3] = (byte) 0x03;
			bb[2] = (byte) 0x04;
			bb[1] = (byte) 0x05;
			bb[0] = (byte) 0x06;
			request.setCallbackNumAtag(new ByteBuffer(bb));
			
			request.setCallbackNumPresInd((byte) 0x01);
			request.setDataCoding((byte) 3); //ISO-8859-1
			request.setDestAddr(destAddress);
			request.setDestAddrSubunit((byte) 1); // MS display
			request.setDestinationPort((short) 1234);
			byte [] sub = new byte[10];
			sub[0] = (byte) 128;
			sub[1] = 0x31;
			sub[2] = 0x32;
			sub[3] = 0x33;
			sub[4] = 0x32;
			sub[5] = 0x31;
			sub[6] = 0x32;
			sub[7] = 0x33;
			sub[8] = 0x32;
			sub[9] = 0x31;
			request.setDestSubaddress(new ByteBuffer(sub));
			request.setDisplayTime((byte) 1);
			request.setEsmClass((byte) 128); // reply path set
			request.setItsReplyType((byte) 2);
			request.setItsSessionInfo((short)0x0101);
			request.setLanguageIndicator((byte) 1); // english
			// request.setMessagePayload();
			request.setMoreMsgsToSend((byte) 1);
			request.setMsMsgWaitFacilities((byte) 129); // fax waiting
			request.setMsValidity((byte) 3);
			request.setNumberOfMessages((byte) 99);
			// request.setPayloadType();
			request.setPriorityFlag((byte) 3);
			request.setPrivacyIndicator((byte) 3);
			request.setProtocolId((byte) 31);
			request.setReplaceIfPresentFlag((byte) 1);
			request.setSarMsgRefNum((short) 2);
			request.setSarSegmentSeqnum((short) 3);
			request.setSarTotalSegments((short) 4);
			request.setScheduleDeliveryTime("050525161013000+");
			request.setServiceType(smppServiceType);
			request.setShortMessage("SUBMIT_SM test including optional parameters");		
			request.setSmDefaultMsgId((byte)0);
			request.setSmsSignal((short) 10);
			request.setSourceAddr(srcAddress);
			request.setSourceAddrSubunit((byte) 1);
			request.setSourcePort((short) 15);
			sub[0] = (byte) 128;
			sub[1] = 0x33;
			sub[2] = 0x32;
			sub[3] = 0x31;
			sub[4] = 0x32;
			sub[5] = 0x33;
			sub[6] = 0x32;
			sub[7] = 0x31;
			sub[8] = 0x32;
			sub[9] = 0x33;
			request.setSourceSubaddress(new ByteBuffer(sub));
			request.setUserMessageReference((short) 16);
			request.setUserResponseCode((byte) 255);
			request.setUssdServiceOp((byte) 16);
			request.setValidityPeriod("050530161013000+");

			// send the request
			request.assignSequenceNumber(true);
			response = session.submit(request);
			logger.info("Message submitted....");
			assertEquals(
				"SUBMIT_SM failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.warning("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			e.printStackTrace();
			throw new SubmitSmFailedException();
		}

		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
			logger.info("Unbound...");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception: " + e.getMessage(), e);
			logger.warning(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

}