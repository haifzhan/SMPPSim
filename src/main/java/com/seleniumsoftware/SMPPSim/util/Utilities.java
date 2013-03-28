/****************************************************************************
 * Utilities.java
 *
 * Copyright (C) Selenium Software Ltd 2006
 *
 * This file is part of SMPPSim.
 *
 * SMPPSim is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * SMPPSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SMPPSim; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author martin@seleniumsoftware.com
 * http://www.woolleynet.com
 * http://www.seleniumsoftware.com
 * 
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.util;

import com.seleniumsoftware.SMPPSim.exceptions.InvalidHexStringlException;

public class Utilities {

	public static byte[] getByteArrayFromHexString(String hex)
			throws InvalidHexStringlException {
		int i = 0;
		String hexNoSpaces = Utilities.removeSpaces(hex);
		hexNoSpaces = hexNoSpaces.toUpperCase();
		int l = hexNoSpaces.length();

		if (!Utilities.isEven(l)) {
			throw new InvalidHexStringlException("Invalid hex string");
		}

		if (!Utilities.validHexChars(hexNoSpaces)) {
			throw new InvalidHexStringlException("Invalid hex string");
		}
		l = l / 2;
		byte[] result = new byte[l];
		while (i < l) {
			String byteAsHex = hexNoSpaces.substring((i * 2), ((i * 2) + 2));
			int b = (int) (Integer.parseInt(byteAsHex, 16) & 0x000000FF);
			if (b < 0)
				b = 256 + b;
			result[i] = (byte) b;
			i++;
		}
		return result;

	}

	public static boolean isEven(int number) {
		int half = number / 2;
		if ((half * 2) == number)
			return true;
		else
			return false;
	}

	public static String removeSpaces(String text) {
		int l = text.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < l; i++) {
			if (text.charAt(i) != ' ')
				sb.append(text.charAt(i));
		}
		return sb.toString();
	}

	public static boolean validHexChars(String hex) {
		int l = hex.length();
		for (int i = 0; i < l; i++) {
			if (!hexChar(hex.charAt(i)))
				return false;
		}
		return true;

	}

	public static boolean hexChar(char h) {
		switch (h) {
		case '0':
			return true;
		case '1':
			return true;
		case '2':
			return true;
		case '3':
			return true;
		case '4':
			return true;
		case '5':
			return true;
		case '6':
			return true;
		case '7':
			return true;
		case '8':
			return true;
		case '9':
			return true;
		case 'A':
			return true;
		case 'B':
			return true;
		case 'C':
			return true;
		case 'D':
			return true;
		case 'E':
			return true;
		case 'F':
			return true;
		}
		return false;
	}

	public static int getIntegerValue(byte[] msg, int start, int len) {
		if (msg == null) {
			return 0;
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

	public static short getShortValue(byte[] msg, int start, int len) {
		if (msg == null) {
			return 0;
		}
		int inx = start;
		byte[] ba = new byte[2];
		short newShort = 0x00000000;
		int l = len - 1;
		int ia = 1;

		for (int i = l; i >= 0; i--) {
			ba[ia] = msg[inx + i];
			ia--;
		}
		for (int i = ia; i >= 0; i--) {
			ba[i] = 0x00;
		}

		newShort |= ((((int) ba[0]) << 8) & 0x0000ff00);
		newShort |= (((int) ba[1]) & 0x000000ff);

		return newShort;
	}

}