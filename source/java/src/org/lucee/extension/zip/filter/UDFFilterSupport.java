/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.extension.zip.filter;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;
import lucee.runtime.util.Excepton;

public abstract class UDFFilterSupport {

	public static final short TYPE_ANY = 0;
	public static final short TYPE_BOOLEAN = 2;
	public static final short TYPE_STRING = 7;

	protected UDF udf;
	protected Object[] args = new Object[1];

	public UDFFilterSupport(UDF udf) throws PageException {
		this.udf = udf;
		Excepton util = CFMLEngineFactory.getInstance().getExceptionUtil();
		// check UDF return type
		int type = udf.getReturnType();
		if (type != TYPE_BOOLEAN && type != TYPE_ANY)
			throw util.createExpressionException("invalid return type [" + udf.getReturnTypeAsString() + "] for UDF Filter, valid return types are [boolean,any]");

		// check UDF arguments
		FunctionArgument[] args = udf.getFunctionArguments();
		if (args.length > 1) throw util.createExpressionException("UDF filter has to many arguments [" + args.length + "], should have at maximum 1 argument");

		if (args.length == 1) {
			type = args[0].getType();
			if (type != TYPE_STRING && type != TYPE_ANY)
				throw util.createExpressionException("invalid type [" + args[0].getTypeAsString() + "] for first argument of UDF Filter, valid return types are [string,any]");
		}
	}

	@Override
	public String toString() {
		return "UDFFilter:" + udf;
	}
}