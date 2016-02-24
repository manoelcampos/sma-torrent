package com.manoelcampos.smatorrent;

/**Class to raise exceptions when try to parse a invalid
 * b-encoded string.
 * @author Manoel Campos da Silva Filho*/
public class InvalidBEncodedStringException extends Exception {
	private static final long serialVersionUID = 3629537874306147474L;

	/**Class constructor
	 * @param message The error message to be displayed*/
	public InvalidBEncodedStringException(String message) {
		super(message);
	}
}