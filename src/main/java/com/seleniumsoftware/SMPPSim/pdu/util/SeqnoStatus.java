/*
 * Created on 25 Nov 2010
 *
 */
package com.seleniumsoftware.SMPPSim.pdu.util;

public class SeqnoStatus {

	private int seq_no;
	
	private int command_status;
	
	public SeqnoStatus(int seq_no, int command_status) {
		this.seq_no = seq_no;
		this.command_status = command_status;
	}
	
	public int getSeq_no() {
		return seq_no;
	}
	public void setSeq_no(int seqNo) {
		seq_no = seqNo;
	}
	public int getCommand_status() {
		return command_status;
	}
	public void setCommand_status(int commandStatus) {
		command_status = commandStatus;
	}
	
}
