package com.poixson.commonmc.tools.scripting.exceptions;


public class JSFunctionNotFoundException extends NullPointerException {
	private static final long serialVersionUID = 1L;



	public JSFunctionNotFoundException(final String fileName, final String funcName, final Object funcObj) {
		super(
			String.format(
				"Function '%s' not found in script '%s' actual: %s",
				funcName, fileName,
				(funcObj == null ? "<null>" : funcObj.getClass().toString())
			)
		);
	}

	public JSFunctionNotFoundException(final String fileName, final String funcName) {
		super(
			String.format(
				"Function '%s' not found in script '%s'",
				funcName, fileName
			)
		);
	}



}
