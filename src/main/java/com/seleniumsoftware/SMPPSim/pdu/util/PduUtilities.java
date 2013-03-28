/*******************************************************************************
 * SmppPduUtilities.java
 * 
 * Copyright (C) Selenium Software Ltd 2006
 * 
 * This file is part of SMPPSim.
 * 
 * SMPPSim is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * SMPPSim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SMPPSim; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * @author martin@seleniumsoftware.com http://www.woolleynet.com
 *         http://www.seleniumsoftware.com $Header:
 *         /var/cvsroot/SMPPSim2/src/java/com/woolleynet/SMPPSim/pdu/util/PduUtilities.java,v
 *         1.5 2005/02/18 15:32:29 martin Exp $
 ******************************************************************************/
package com.seleniumsoftware.SMPPSim.pdu.util;

import com.seleniumsoftware.SMPPSim.pdu.*;

import java.io.UnsupportedEncodingException;
import java.util.logging.*;

public class PduUtilities {
	/*
	 * This class contains utility methods that do things like create byte
	 * arrays from java types in the form of the required SMPP data type @author
	 * martin
	 */
	/*
	 * From the SMPP spec: 0 0 0 0 0 0 0 0 0 SMSC Default Alphabet : Whatever 0
	 * 0 0 0 0 0 0 0 1 IA5 (CCITT T.50)/ASCII (ANSI X3.4) : Basic, ASCII 0 0 0 0
	 * 0 0 0 1 1 Latin 1 (ISO-8859-1) : Basic, ISO8859_1 0 0 0 0 0 0 1 0 1 JIS
	 * (X 0208-1990) : Extended, EUC_JP 0 0 0 0 0 0 1 1 0 Cyrllic (ISO-8859-5) :
	 * Basic, ISO8859_5 0 0 0 0 0 0 1 1 1 Latin/Hebrew (ISO-8859-8) :
	 * Extended,ISO8859_8 0 0 0 0 0 1 0 0 0 UCS2 (ISO/IEC-10646) : Unicode.
	 * Assume UTF-8. Basic, UTF8 0 0 0 0 0 1 0 0 1 Pictogram Encoding : Not
	 * supported. 0 0 0 0 0 1 0 1 0 ISO-2022-JP (Music Codes) : Extended,
	 * ISO2022JP 0 0 0 0 0 1 1 0 1 Extended Kanji JIS(X 0212-1990) : Extended,
	 * EUC_JP 0 0 0 0 0 1 1 1 0 KS C 5601 : Extended (Korean), EUC_KR
	 */
	private static String[] encodings = { "default", "ASCII", null, // binary
			"ISO8859_1", null, // binary
			"EUC_JP", "ISO8859_5", "ISO8859_8", "UTF-16BE", null, // pictograms
																	// not
																	// supported
			"ISO2022JP", null, // reserved
			null, // reserved
			"EUC_JP", "EUC_KR" };

	private static Logger logger = Logger.getLogger("com.seleniumsoftware.smppsim");

	public static int getRandomSubmitError() {
		return PduConstants.SUBMIT_SM_ERRORS[(int) (Math.random() * PduConstants.submitsm_error_count)];
	}

	public static String getStringValueNullTerminated(byte[] pdu, int pduIndex, int maxlength, int type)
			throws Exception {
		boolean nullReached = false;
		int target_len = 0;
		int pdu_len = pdu.length;
		int startPoint = pduIndex;

		if (type == PduConstants.C_OCTET_STRING_TYPE) {
			while ((!nullReached) && (pduIndex <= pdu_len) && (target_len <= maxlength)) {
				if (pdu[pduIndex] == (byte) 0x00) {
					nullReached = true;
				} else {
					target_len++;
				}
				pduIndex++;
			}
			if (target_len <= maxlength) {
				byte[] result = new byte[target_len];
				System.arraycopy(pdu, startPoint, result, 0, target_len);
				return new String(result);
			} else {
				throw new Exception(" Error: string size (" + target_len + ") exceeds maximum specified (" + maxlength
						+ ")");
			}
		}
		if (type == PduConstants.OCTET_STRING_TYPE) {
			while ((pduIndex <= pdu_len) && (target_len <= maxlength)) {
				target_len++;
				pduIndex++;
			}
			if (target_len <= maxlength) {
				byte[] result = new byte[target_len];
				System.arraycopy(pdu, startPoint, result, 0, target_len);
				return new String(result);
			} else {
				throw new Exception(" Error: string size (" + target_len + ") exceeds maximum specified (" + maxlength
						+ ")");
			}
		}
		throw new Exception(" Error: string size (" + target_len + ") exceeds maximum specified (" + maxlength + ")");
	}

	// only applies to octet-strings

	public static String getStringValueFixedLength(byte[] pdu, int pduIndex, int actualLength) throws Exception {

		int pdu_len = pdu.length;
		int startPoint = pduIndex;

		if ((startPoint + actualLength) > pdu_len) {
			throw new Exception(" Error: specified size of octet-string (" + actualLength
					+ ") exceeds available data in PDU (" + (pdu_len - startPoint) + ")");

		}

		byte[] result = new byte[actualLength];
		System.arraycopy(pdu, startPoint, result, 0, actualLength);
		return new String(result);
	}

	public static int getIntegerValue(byte[] msg, int start, int len) throws Exception {
		if (len > 4) {
			logger.warning("Invalid length (" + len + ") for integer conversion");
			throw new Exception("Invalid length (" + len + ") for integer conversion");
		}
		int inx = start;
		byte[] ba = new byte[4];
		int newInt = 0x00000000;
		int l = len - 1;
		int ia = 3;

		for (int i = l; i >= 0; i--) {
			ba[ia] = msg[inx + i];
			ia--;
		}
		for (int i = ia; i >= 0; i--) {
			ba[i] = 0x00;
		}

		newInt |= ((((int) ba[0]) << 24) & 0xff000000);
		newInt |= ((((int) ba[1]) << 16) & 0x00ff0000);
		newInt |= ((((int) ba[2]) << 8) & 0x0000ff00);
		newInt |= (((int) ba[3]) & 0x000000ff);

		return newInt;
	}

	public static byte[] makeByteArrayFromInt(int i, int numBytes) {
		byte[] result = new byte[numBytes];
		int shiftBits = (numBytes - 1) * 8;

		for (int j = 0; j < numBytes; j++) {
			result[j] = (byte) (i >>> shiftBits);
			shiftBits -= 8;
		}
		return result;
	}

	public static short getShortValue(byte msb, byte lsb) throws Exception {
		short newShort = 0x0000;

		newShort |= ((((int) msb) << 8) & 0x0000ff00);
		newShort |= (((int) lsb) & 0x000000ff);

		return newShort;
	}

	public static byte[] stringToNullTerminatedByteArray(String input) {
		int len;
		byte[] output;
		if (input == null) {
			output = new byte[1];
			output[0] = 0x00;
		} else {
			len = input.length();
			output = new byte[len + 1];
			for (int i = 0; i < len; i++) {
				output[i] = (byte) input.charAt(i);
			}
			output[len] = (byte) 0x00;
		}
		return output;
	}

	public static byte[] setPduLength(byte[] pdu, int l) {
		byte[] len = makeByteArrayFromInt(l, 4);
		System.arraycopy(len, 0, pdu, 0, 4);
		return pdu;
	}

	public static String byteArrayToHexString(byte in[]) {

		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String hexchars[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(hexchars[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(hexchars[(int) ch]);
			i++;
		}
		String result = new String(out);
		return result;

	}

	public static byte[] makeShortTLV(short t, short value) {
		logger.finest("makeShortTLV(" + t + "," + value + ")");
		byte[] tlv = new byte[6];
		byte[] tag = makeByteArrayFromInt(t, 2);
		logger.finest("makeShortTLV: made tag bytes");
		byte[] len = makeByteArrayFromInt(2, 2);
		logger.finest("makeShortTLV: made length bytes");
		byte[] val = makeByteArrayFromInt(value, 2);
		logger.finest("makeShortTLV: made value bytes");
		System.arraycopy(tag, 0, tlv, 0, 2);
		logger.finest("makeShortTLV: copied tag bytes to result array");
		System.arraycopy(len, 0, tlv, 2, 2);
		logger.finest("makeShortTLV: copied length bytes to result array");
		System.arraycopy(val, 0, tlv, 4, 2);
		logger.finest("makeShortTLV: copied value bytes to result array");
		return tlv;
	}

	public static byte[] makeByteTLV(short t, short value) {
		byte[] tlv = new byte[5];
		byte[] tag = makeByteArrayFromInt(t, 2);
		byte[] len = makeByteArrayFromInt(1, 2);
		byte[] val = makeByteArrayFromInt(value, 1);
		System.arraycopy(tag, 0, tlv, 0, 2);
		System.arraycopy(len, 0, tlv, 2, 2);
		System.arraycopy(val, 0, tlv, 4, 1);
		return tlv;
	}

	public static byte[] makeCOctetStringTLV(short t, byte[] value) {
		logger.finest("making OctetStringTLV:" + byteArrayToHexString(value));
		byte[] tlv = new byte[value.length + 5];
		int l = value.length + 1;
		byte[] tag = makeByteArrayFromInt(t, 2);
		byte[] len = makeByteArrayFromInt(l, 2);
		System.arraycopy(tag, 0, tlv, 0, 2);
		System.arraycopy(len, 0, tlv, 2, 2);
		System.arraycopy(value, 0, tlv, 4, l - 1);
		tlv[tlv.length - 1] = 0x00;
		return tlv;
	}

	public static byte[] makeRawTLV(short t, byte[] value) {
		logger.finest("making RawTLV:" + byteArrayToHexString(value));
		byte[] tlv = new byte[value.length + 4];
		int l = value.length;
		byte[] tag = makeByteArrayFromInt(t, 2);
		byte[] len = makeByteArrayFromInt(l, 2);
		System.arraycopy(tag, 0, tlv, 0, 2);
		System.arraycopy(len, 0, tlv, 2, 2);
		System.arraycopy(value, 0, tlv, 4, l);
		return tlv;
	}

	public static String getJavaEncoding(byte data_coding) {
		if (data_coding >= encodings.length || data_coding < 0)
			return null;
		else
			return encodings[data_coding];
	}

	public byte[] getUnicodeMessageDataWithoutBom(String text_message) throws UnsupportedEncodingException {
		byte[] messageData = null;
		messageData = text_message.getBytes("UTF-16");
		if (messageData.length > 2) {
			int newlen = messageData.length - 2;
			byte[] newMessageData = new byte[newlen];
			System.arraycopy(messageData, 2, newMessageData, 0, newlen);
			messageData = new byte[newlen];
			messageData = newMessageData;
		} else {
			// empty array - a unicode string with just a BOM is basically empty
			messageData = new byte[0];
		}
		return messageData;
	}
}