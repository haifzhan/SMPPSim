package com.seleniumsoftware.examples;

public interface CallbackReceivable {
	public void sent(byte pdu[]);
	public void received(byte pdu[]);
}