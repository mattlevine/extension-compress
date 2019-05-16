/**
 *
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

import java.io.File;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

public class UDFFilter extends UDFFilterSupport implements ResourceAndResourceNameFilter {

	public UDFFilter(UDF udf) throws PageException {
		super(udf);
	}

	public boolean accept(String path) {
		args[0] = path;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		try {
			return engine.getCastUtil().toBooleanValue(udf.call(engine.getThreadPageContext(), args, true));

		}
		catch (PageException e) {
			throw engine.getExceptionUtil().createPageRuntimeException(e);
		}
	}

	@Override
	public boolean accept(Resource file) {
		return accept(file.getAbsolutePath());
	}

	@Override
	public boolean accept(Resource parent, String name) {
		String path = parent.getAbsolutePath();
		if (path.endsWith(File.separator)) path += name;
		else path += File.separator + name;
		return accept(path);
	}

	@Override
	public String toString() {
		return "UDFFilter:" + udf;
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(Object filter) throws PageException {
		if (filter instanceof UDF) return createResourceAndResourceNameFilter((UDF) filter);
		return createResourceAndResourceNameFilter(CFMLEngineFactory.getInstance().getCastUtil().toString(filter));
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(UDF filter) throws PageException {
		return new UDFFilter(filter);
	}

	public static ResourceAndResourceNameFilter createResourceAndResourceNameFilter(String pattern) {

		if (!CFMLEngineFactory.getInstance().getStringUtil().isEmpty(pattern, true)) return new WildcardPatternFilter(pattern, "|");

		return null;
	}
}