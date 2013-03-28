/****************************************************************************
 * PduConstants.java
 *
 * Copyright (C) Martin Woolley 2001
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
 * $Header: /var/cvsroot/SMPPSim2/distribution/2.6.9/SMPPSim/src/java/com/seleniumsoftware/SMPPSim/pdu/PduConstants.java,v 1.1 2012/07/24 14:48:59 martin Exp $
 ****************************************************************************/

package com.seleniumsoftware.SMPPSim.pdu;

public class PduConstants {

	// SMPP data types
	public static final int C_OCTET_STRING_TYPE = 0;

	public static final int OCTET_STRING_TYPE = 1;

	public static final int INTEGER_TYPE = 2;

	// SUBMIT_MULTI destination type flags
	public static final int SME_ADDRESS = 1;

	public static final int DISTRIBUTION_LIST_NAME = 2;

	// SMPP command IDs
	public static final int BIND_RECEIVER = 0x00000001;

	public static final int BIND_RECEIVER_RESP = 0x80000001;

	public static final int BIND_TRANSMITTER = 0x00000002;

	public static final int BIND_TRANSMITTER_RESP = 0x80000002;

	public static final int BIND_TRANSCEIVER = 0x00000009;

	public static final int BIND_TRANSCEIVER_RESP = 0x80000009;

	public static final int OUTBIND = 0x0000000B;

	public static final int UNBIND = 0x00000006;

	public static final int UNBIND_RESP = 0x80000006;

	public static final int SUBMIT_SM = 0x00000004;

	public static final int SUBMIT_SM_RESP = 0x80000004;

	public static final int DELIVER_SM = 0x00000005;

	public static final int DELIVER_SM_RESP = 0x80000005;

	public static final int QUERY_SM = 0x00000003;

	public static final int QUERY_SM_RESP = 0x80000003;

	public static final int CANCEL_SM = 0x00000008;

	public static final int CANCEL_SM_RESP = 0x80000008;

	public static final int REPLACE_SM = 0x00000007;

	public static final int REPLACE_SM_RESP = 0x80000007;

	public static final int ENQUIRE_LINK = 0x00000015;

	public static final int ENQUIRE_LINK_RESP = 0x80000015;

	public static final int SUBMIT_MULTI = 0x00000021;

	public static final int SUBMIT_MULTI_RESP = 0x80000021;

	public static final int GENERIC_NAK = 0x80000000;

	public static final int DATA_SM = 0x00000103;

	public static final int DATA_SM_RESP = 0x80000103;

	// TLV tags
	public static final short PAYLOAD_TYPE = 0x0019;

	public static final short PRIVACY_INDICATOR = 0x0201;

	public static final short USER_MESSAGE_REFERENCE_TAG = 0x0204;

	public static final short NETWORK_ERROR_CODE_TAG = 0x0423;

	public static final short USER_RESPONSE_CODE = 0x0205;

	public static final short SOURCE_PORT = 0x020A;

	public static final short DESTINATION_PORT = 0x020B;

	public static final short SAR_MSG_REF_NUM = 0x020C;

	public static final short LANGUAGE_INDICATOR = 0x020D;

	public static final short SAR_TOTAL_SEGMENTS = 0x020E;

	public static final short SAR_SEGMENT_SEQNUM = 0x020F;

	public static final short SOURCE_SUBADDRESS = 0x0202;

	public static final short DEST_SUBADDRESS = 0x0203;

	public static final short CALLBACK_NUM = 0x0381;

	public static final short MESSAGE_PAYLOAD = 0x0424;

	public static final short USSD_SERVICE_OP = 0x0501;

	public static final short RECEIPTED_MESSAGE_ID = 0x001E;
	
	public static final short MESSAGE_STATE = 0x0427;


	// Query Status values
	public static final byte ENROUTE = (byte) 0x01;

	public static final byte DELIVERED = (byte) 0x02;

	public static final byte EXPIRED = (byte) 0x03;

	public static final byte DELETED = (byte) 0x04;

	public static final byte UNDELIVERABLE = (byte) 0x05;

	public static final byte ACCEPTED = (byte) 0x06;

	public static final byte UNKNOWN = (byte) 0x07;

	public static final byte REJECTED = (byte) 0x08;

	// SMSC Error Codes
	public static final int ESME_ROK = 0x00000000; // No Error

	public static final int ESME_RINVMSGLEN = 0x00000001;

	// Message Length is invalid
	public static final int ESME_RINVCMDLEN = 0x00000002;

	// Command Length is invalid
	public static final int ESME_RINVCMDID = 0x00000003;

	// Invalid Command ID
	public static final int ESME_RINVBNDSTS = 0x00000004;

	// Incorrect BIND Status for given command
	public static final int ESME_RALYBND = 0x00000005;

	// ESME Already in Bound State
	public static final int ESME_RINVPRTFLG = 0x00000006;

	// Invalid Priority Flag
	public static final int ESME_RINVREGDLVFLG = 0x00000007;

	// Invalid Registered Delivery Flag
	public static final int ESME_RSYSERR = 0x00000008; // System Error

	// Reserved = 0x00000009 Reserved
	public static final int ESME_RINVSRCADR = 0x0000000A;

	// Invalid Source Address
	public static final int ESME_RINVDSTADR = 0x0000000B;

	// Invalid Dest Addr
	public static final int ESME_RINVMSGID = 0x0000000C;

	// Message ID is invalid
	public static final int ESME_RBINDFAIL = 0x0000000D; // Bind Failed

	public static final int ESME_RINVPASWD = 0x0000000E; // Invalid Password

	public static final int ESME_RINVSYSID = 0x0000000F;

	// Invalid System ID
	// Reserved = 0x00000010 Reserved
	public static final int ESME_RCANCELFAIL = 0x00000011;

	// Cancel SM Failed
	// Reserved = 0x00000012 Reserved
	public static final int ESME_RREPLACEFAIL = 0x00000013;

	// Replace SM Failed
	public static final int ESME_RMSGQFUL = 0x00000014;

	// Message Queue Full
	public static final int ESME_RINVSERTYP = 0x00000015;

	// Invalid Service Type
	// Reserved = 0x00000016-0x00000032
	public static final int ESME_RINVNUMDESTS = 0x00000033;

	// Invalid number of destinations
	public static final int ESME_RINVDLNAME = 0x00000034;

	// Invalid Distribution List name
	// Reserved = 0x00000035-0x0000003F
	public static final int ESME_RINVDESTFLAG = 0x00000040;

	// Destination flag is invalid (submit_multi)
	// Reserved = 0x00000041 Reserved
	public static final int ESME_RINVSUBREP = 0x00000042;

	// Invalid 'submit with replace' request
	// (i.e. submit_sm with replace_if_present_flag set)
	public static final int ESME_RINVESMCLASS = 0x00000043;

	// Invalid esm_class field data
	public static final int ESME_RCNTSUBDL = 0x00000044;

	// Cannot Submit to Distribution List
	public static final int ESME_RSUBMITFAIL = 0x00000045;

	// submit_sm or submit_multi failed
	// Reserved = 0x00000046-0x00000047 Reserved
	public static final int ESME_RINVSRCTON = 0x00000048;

	// Invalid Source address TON
	public static final int ESME_RINVSRCNPI = 0x00000049;

	// Invalid Source address NPI
	public static final int ESME_RINVDSTTON = 0x00000050;

	// Invalid Destination address TON
	public static final int ESME_RINVDSTNPI = 0x00000051;

	// Invalid Destination address NPI
	// Reserved = 0x00000052 Reserved
	public static final int ESME_RINVSYSTYP = 0x00000053;

	// Invalid system_type field
	public static final int ESME_RINVREPFLAG = 0x00000054;

	// Invalid replace_if_present flag
	public static final int ESME_RINVNUMMSGS = 0x00000055;

	// Invalid number of messages
	// Reserved = 0x00000056-0x00000057 Reserved
	public static final int ESME_RTHROTTLED = 0x00000058;

	// Throttling error (ESME has exceeded allowed message
	// limits)
	// Reserved = 0x00000059-0x00000060 Reserved
	public static final int ESME_RINVSCHED = 0x00000061;

	// Invalid Scheduled Delivery Time
	public static final int ESME_RINVEXPIRY = 0x00000062;

	// Invalid message validity period (Expiry time)
	public static final int ESME_RINVDFTMSGID = 0x00000063;

	// Predefined Message Invalid or Not Found
	public static final int ESME_RX_T_APPN = 0x00000064;

	// ESME Receiver Temporary App Error Code
	public static final int ESME_RX_P_APPN = 0x00000065;

	// ESME Receiver Permanent App Error Code
	public static final int ESME_RX_R_APPN = 0x00000066;

	// ESME Receiver Reject Message Error Code
	public static final int ESME_RQUERYFAIL = 0x00000067;

	// query_sm request failed
	// Reserved = 0x00000068-0x000000BF Reserved
	public static final int ESME_RINVOPTPARSTREAM = 0x000000C0;

	// Error in the optional part of the PDU Body.
	public static final int ESME_ROPTPARNOTALLWD = 0x000000C1;

	// Optional Parameter not allowed
	public static final int ESME_RINVPARLEN = 0x000000C2;

	// Invalid Parameter Length.
	public static final int ESME_RMISSINGOPTPARAM = 0x000000C3;

	// Expected Optional Parameter missing
	public static final int ESME_RINVOPTPARAMVAL = 0x000000C4;

	// Invalid Optional Parameter Value
	// Reserved = 0x000000C5-0x000000FD Reserved
	public static final int ESME_RDELIVERYFAILURE = 0x000000FE;

	// Delivery Failure (used for data_sm_resp)
	public static final int ESME_RUNKNOWNERR = 0x000000FF; // Unknown Error

	public static final int submitsm_error_count = 17;

	public static final int[] SUBMIT_SM_ERRORS = { 0x00000006, 0x00000007,
			0x00000008, 0x0000000A, 0x0000000B, 0x00000014, 0x00000042,
			0x00000043, 0x00000045, 0x00000048, 0x00000049, 0x00000050,
			0x00000051, 0x00000054, 0x00000058, 0x00000061, 0x00000062 };

	public static final String SERVICE_TYPE_WAP = "WAP";

}