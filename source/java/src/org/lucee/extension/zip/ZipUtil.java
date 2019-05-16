package org.lucee.extension.zip;

import java.io.IOException;
import java.util.zip.ZipEntry;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;

public class ZipUtil {
	public static Resource toResource(Resource targetDir, ZipEntry entry) throws IOException {
		return toResource(targetDir, entry.getName());
	}

	public static Resource toResource(Resource targetDir, String fileName) throws IOException {
		Resource target = targetDir.getRealResource(fileName);

		// in case a file is outside the target directory, we copy it to the target directory
		if (!target.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
			target = targetDir.getRealResource(CFMLEngineFactory.getInstance().getListUtil().last(fileName, "\\/", true));
		}
		return target;
	}
}
