package com.seleniumsoftware.examples;

import java.net.ServerSocket;
import java.util.logging.Logger;

public class CallbackReceiver implements CallbackReceivable {

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.examples");
	
	public void sent(byte[] pdu) {
		hexDump("SMPPSim sent to ESME:", pdu, pdu.length);
		if (pduIsDeliverSm(pdu)) {
			logger.info("(PDU type was DeliverSM)");
		} else
			logger.warning("Unrecognised PDU type");
	}

	public void received(byte[] pdu) {
		hexDump("SMPPSim received from ESME", pdu, pdu.length);
		if (pduIsSubmitSm(pdu)) {
			logger.info("(PDU type was SubmitSm)");
		} else if (pduIsDataSm(pdu)) {
			logger.info("(PDU type was DataSm)");
		} else if (pduIsBindRx(pdu)) {
			logger.info("(PDU type was BindRX)");
		} else if (pduIsBindTx(pdu)) {
			logger.info("(PDU type was BindTX)");
		} else if (pduIsBindTrx(pdu)) {
			logger.info("(PDU type was BindTRX)");
		} else if (pduIsEnquireLink(pdu)) {
			logger.info("(PDU type was Enquire_Link)");
		} else if (pduIsUnbind(pdu)) {
			logger.info("(PDU type was Unbind)");
		} else
			logger.warning("Unrecognised PDU type");
	}

	private boolean pduIsSubmitSm(byte[] pdu) {
		return (pdu[16] == (byte) 4);
	}

	private boolean pduIsDataSm(byte[] pdu) {
		return (pdu[16] == (byte) 0x00000103);
	}

	private boolean pduIsBindRx(byte[] pdu) {
		return (pdu[16] == (byte) 1);
	}

	private boolean pduIsBindTx(byte[] pdu) {
		return (pdu[16] == (byte) 2);
	}

	private boolean pduIsBindTrx(byte[] pdu) {
		return (pdu[16] == (byte) 9);
	}

	private boolean pduIsDeliverSm(byte[] pdu) {
		return (pdu[16] == (byte) 5);
	}

	private boolean pduIsEnquireLink(byte[] pdu) {
		return (pdu[16] == (byte) 0x15);
	}

	private boolean pduIsUnbind(byte[] pdu) {
		return (pdu[16] == (byte) 6);
	}

	public static void hexDump(String title, byte[] m, int l) {
		int p = 0;
		StringBuffer line = new StringBuffer();
		logger.info(title);
		logger.info("Hex dump (" + l + ") bytes:");
		for (int i = 0; i < l; i++) {
			if ((m[i] >= 0) & (m[i] < 16))
				line.append("0");
			line.append(Integer.toString(m[i] & 0xff, 16).toUpperCase());
			if ((++p % 4) == 0) {
				line.append(":");
			}
			if (p == 16) {
				p = 0;
				logger.info(line.toString());
				line = new StringBuffer();
			}
		}
		if (p != 16) {
			logger.info(line.toString());
		}
		logger.info("====================================");
	}

	
}