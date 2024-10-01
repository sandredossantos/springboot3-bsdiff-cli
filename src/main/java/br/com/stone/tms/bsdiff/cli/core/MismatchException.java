package br.com.stone.tms.bsdiff.cli.core;

import java.io.IOException;

@SuppressWarnings("serial")
public class MismatchException extends IOException {
	public MismatchException(String message) {
		super(message);
	}
}