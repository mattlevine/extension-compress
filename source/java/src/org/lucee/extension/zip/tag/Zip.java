/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
package org.lucee.extension.zip.tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lucee.extension.zip.ZipParamAbstr;
import org.lucee.extension.zip.ZipParamContent;
import org.lucee.extension.zip.ZipParamSource;
import org.lucee.extension.zip.ZipUtil;
import org.lucee.extension.zip.filter.DirectoryResourceFilter;
import org.lucee.extension.zip.filter.FileResourceFilter;
import org.lucee.extension.zip.filter.OrResourceFilter;
import org.lucee.extension.zip.filter.UDFFilter;
import org.lucee.extension.zip.filter.WildcardPatternFilter;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.UDF;
import lucee.runtime.util.Creation;
import lucee.runtime.util.IO;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;

public final class Zip extends BodyTagImpl {

	private String action = "zip";
	private String charset;
	private Resource destination;
	private LinkedList<String> entryPathList;
	private String[] entryPaths;
	private Resource file;
	private LinkedList<ResourceFilter> filters;
	private ResourceFilter filter;
	private String pattern;
	private String patternDelimiters;
	private String name;
	private boolean overwrite;
	private String prefix;
	private String password;
	private boolean recurse = true;
	private boolean showDirectory;
	private boolean storePath = true;
	private String variable;
	private List<ZipParamAbstr> params;
	private Set<String> alreadyUsed;
	private Resource source;
	private int compressionMethod = Zip4jConstants.COMP_DEFLATE;
	private int encryption = Zip4jConstants.ENC_NO_ENCRYPTION;
	private int aes = Zip4jConstants.AES_STRENGTH_256;
	private int deflate = Zip4jConstants.DEFLATE_LEVEL_NORMAL;
	private static int id = 0;

	@Override
	public void release() {
		super.release();
		action = "zip";
		charset = null;
		destination = null;
		entryPathList = null;
		entryPaths = null;
		file = null;
		filter = null;
		filters = null;
		name = null;
		overwrite = false;
		prefix = null;
		recurse = true;
		showDirectory = false;
		source = null;
		storePath = true;
		variable = null;
		pattern = null;
		patternDelimiters = null;
		password = null;
		compressionMethod = Zip4jConstants.COMP_DEFLATE;
		aes = Zip4jConstants.AES_STRENGTH_256;
		encryption = Zip4jConstants.ENC_NO_ENCRYPTION;
		deflate = Zip4jConstants.DEFLATE_LEVEL_NORMAL;

		if (params != null) params.clear();
		if (alreadyUsed != null) alreadyUsed.clear();
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action.trim().toLowerCase();
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param strDestination the destination to set
	 * @throws ExpressionException
	 * @throws PageException
	 */
	public void setDestination(String strDestination) throws PageException {
		this.destination = engine.getResourceUtil().toResourceExistingParent(pageContext, strDestination);
		if (!destination.exists()) destination.mkdirs();

		if (!destination.isDirectory()) throw engine.getExceptionUtil().createApplicationException("destination [" + strDestination + "] is not a existing directory");

	}

	/**
	 * @param entryPath the entryPath to set
	 */
	public void setEntrypath(String entryPath) {
		if (Util.isEmpty(entryPath, true)) return;

		entryPath = entryPath.trim();
		entryPath = entryPath.replace('\\', '/');

		if (entryPath.startsWith("/")) entryPath = entryPath.substring(1);
		if (entryPath.endsWith("/")) entryPath = entryPath.substring(0, entryPath.length() - 1);
		if (entryPathList == null) entryPathList = new LinkedList<String>();
		this.entryPathList.add(entryPath);
	}

	/**
	 * @param file the file to set
	 * @throws ExpressionException
	 */
	public void setFile(String file) {
		this.file = engine.getResourceUtil().toResourceNotExisting(pageContext, file);
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Object filter) throws PageException {

		if (filter instanceof UDF) this.setFilter((UDF) filter);
		else if (filter instanceof String) this.setFilter((String) filter);
	}

	public void setFilter(UDF filter) throws PageException {
		_setFilter(UDFFilter.createResourceAndResourceNameFilter(filter));
	}

	public void setFilter(String pattern) {
		this.pattern = pattern;
	}

	void _setFilter(ResourceFilter rf) throws PageException {
		if (filters == null) filters = new LinkedList<ResourceFilter>();
		filters.add(rf);
	}

	public void setFilterdelimiters(String patternDelimiters) {

		this.patternDelimiters = patternDelimiters;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @param recurse the recurse to set
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * @param showDirectory the showDirectory to set
	 */
	public void setShowdirectory(boolean showDirectory) {
		this.showDirectory = showDirectory;
	}

	/**
	 * @param strSource the source to set
	 * @throws PageException
	 */
	public void setSource(String strSource) throws PageException {
		source = engine.getResourceUtil().toResourceExisting(pageContext, strSource);
	}

	/**
	 * @param storePath the storePath to set
	 */
	public void setStorepath(boolean storePath) {
		this.storePath = storePath;
	}

	public void setCompressionmethod(String compressionMethod) throws PageException {
		if (Util.isEmpty(compressionMethod, true)) return;
		compressionMethod = compressionMethod.trim();

		if ("deflate".equalsIgnoreCase(compressionMethod) || "deflatenormal".equalsIgnoreCase(compressionMethod)) {
			this.compressionMethod = Zip4jConstants.COMP_DEFLATE;
			this.deflate = Zip4jConstants.DEFLATE_LEVEL_NORMAL;
		}
		else if ("deflateFast".equalsIgnoreCase(compressionMethod)) {
			this.compressionMethod = Zip4jConstants.COMP_DEFLATE;
			this.deflate = Zip4jConstants.DEFLATE_LEVEL_FAST;
		}
		else if ("deflateFastest".equalsIgnoreCase(compressionMethod)) {
			this.compressionMethod = Zip4jConstants.COMP_DEFLATE;
			this.deflate = Zip4jConstants.DEFLATE_LEVEL_FASTEST;
		}
		else if ("deflateMaximum".equalsIgnoreCase(compressionMethod)) {
			this.compressionMethod = Zip4jConstants.COMP_DEFLATE;
			this.deflate = Zip4jConstants.DEFLATE_LEVEL_MAXIMUM;
		}
		else if ("deflateUtra".equalsIgnoreCase(compressionMethod)) {
			this.compressionMethod = Zip4jConstants.COMP_DEFLATE;
			this.deflate = Zip4jConstants.DEFLATE_LEVEL_ULTRA;
		}
		else if ("aesenc".equalsIgnoreCase(compressionMethod)) this.compressionMethod = Zip4jConstants.COMP_AES_ENC;
		else if ("store".equalsIgnoreCase(compressionMethod)) this.compressionMethod = Zip4jConstants.COMP_STORE;
		else throw engine.getExceptionUtil().createApplicationException("compression method [" + compressionMethod + "] is invalid,"
				+ " valid values are [deflate(=deflateNormal),deflateFast,deflateFastest,deflateMaximum,deflateUtra,aesenc,store]");
	}

	public void setEncryptionalgorithm(String encryption) throws PageException {
		if (Util.isEmpty(encryption, true)) return;
		encryption = encryption.trim();

		if ("aes".equalsIgnoreCase(encryption) || "aes256".equalsIgnoreCase(encryption)) {
			this.encryption = Zip4jConstants.ENC_METHOD_AES;
			this.aes = Zip4jConstants.AES_STRENGTH_256;
		}
		else if ("aes192".equalsIgnoreCase(encryption)) {
			this.encryption = Zip4jConstants.ENC_METHOD_AES;
			this.aes = Zip4jConstants.AES_STRENGTH_192;
		}
		else if ("aes128".equalsIgnoreCase(encryption)) {
			this.encryption = Zip4jConstants.ENC_METHOD_AES;
			this.aes = Zip4jConstants.AES_STRENGTH_128;
		}
		else if ("standard".equalsIgnoreCase(encryption)) {
			this.encryption = Zip4jConstants.ENC_METHOD_STANDARD;
		}
		else throw engine.getExceptionUtil().createApplicationException("encryption [" + encryption + "] is invalid," + " valid values are [aes(=aes256),aes128,standard]");

	}

	/**
	 * @param variable the variable to set
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	@Override
	public int doStartTag() throws PageException {
		// filter
		if (!Util.isEmpty(this.pattern)) {
			_setFilter(new WildcardPatternFilter(pattern, Util.isEmpty(patternDelimiters) ? "," : patternDelimiters));
		}
		return EVAL_BODY_INCLUDE;
	}

	private void actionDelete() throws PageException, IOException, ZipException {
		required("file", file, true);

		IO io = engine.getIOUtil();

		// Resource existing = pageContext.getConfig().getTempDirectory().getRealResource(getTempName());
		// io.copy(file, existing);
		// file.delete();
		try {

			ZipFile zip = getZip(file, password);

			String path, name;
			int index;
			boolean accept;

			if (filter == null && recurse && (entryPaths == null || entryPaths.length == 0))
				throw engine.getExceptionUtil().createApplicationException("define at least one restriction, can't delete all the entries from a zip file");

			Iterator<FileHeader> it = zip.getFileHeaders().iterator();
			FileHeader fh;
			List<FileHeader> removes = new ArrayList<FileHeader>();
			while (it.hasNext()) {
				fh = it.next();
				accept = false;
				path = fh.getFileName().replace('\\', '/');
				index = path.lastIndexOf('/');

				if (!recurse && index > 0) accept = true;

				// dir=index==-1?"":path.substring(0,index);
				name = path.substring(index + 1);

				if (filter != null && !filter.accept(file.getRealResource(name))) accept = true;
				if (!entryPathMatch(path)) accept = true;

				if (!accept) removes.add(fh);// zip.removeFile(fh);

				// add(out, in.getInputStream(fh), fh.getFileName(),
				// Zip4jUtil.dosToJavaTme(fh.getLastModFileTime()), true);
			}

			it = removes.iterator();
			while (it.hasNext()) {
				zip.removeFile(it.next());
			}

		}
		finally {
			// existing.delete();
		}

	}

	private void actionList() throws PageException, IOException, ZipException {
		required("file", file, true);
		required("name", name);

		lucee.runtime.type.Query query = engine.getCreationUtil()
				.createQuery(new String[] { "name", "size", "type", "dateLastModified", "directory", "crc", "compressedSize", "comment" }, 0, "query");
		pageContext.setVariable(name, query);

		ZipFile zip = getZip(file, password);
		Iterator<FileHeader> it = zip.getFileHeaders().iterator();

		try {
			FileHeader fh;
			String path, name, dir;
			int row = 0, index;
			while (it.hasNext()) {
				fh = it.next();

				if (!showDirectory && fh.isDirectory()) continue;

				path = fh.getFileName().replace('\\', '/');
				index = path.lastIndexOf('/');
				if (!recurse && index > 0) continue;

				dir = index == -1 ? "" : path.substring(0, index);
				name = path.substring(index + 1);

				if (filter != null && !filter.accept(file.getRealResource(name))) continue;

				if (!entryPathMatch(dir)) continue;
				// if(entryPath!=null && !(dir.equalsIgnoreCase(entryPath) ||
				// StringUtil.startsWithIgnoreCase(dir,entryPath+"/"))) ;///continue;

				row++;
				query.addRow();
				query.setAt("name", row, path);
				query.setAt("size", row, engine.getCastUtil().toDouble(fh.getUncompressedSize())); // TODO do better
				query.setAt("type", row, fh.isDirectory() ? "Directory" : "File");
				query.setAt("dateLastModified", row, engine.getCreationUtil().createDateTime(Zip4jUtil.dosToJavaTme(fh.getLastModFileTime())));
				query.setAt("crc", row, engine.getCastUtil().toDouble(fh.getCrc32()));
				query.setAt("compressedSize", row, engine.getCastUtil().toDouble(fh.getCompressedSize()));
				query.setAt("comment", row, fh.getFileComment());
				query.setAt("directory", row, dir);
				// zis.closeEntry();
			}
		}
		finally {
			// Util.closeEL(zip);
		}
	}

	private boolean entryPathMatch(String dir) {
		if (entryPaths == null || entryPaths.length == 0) return true;

		for (String ep: entryPaths) {
			if (dir.equalsIgnoreCase(ep) || dir.equalsIgnoreCase("/" + ep) || engine.getStringUtil().startsWithIgnoreCase(dir, ep + "/")
					|| engine.getStringUtil().startsWithIgnoreCase(dir, "/" + ep + "/"))
				return true;
		}
		return false;
	}

	private void actionRead(boolean binary) throws ZipException, IOException, PageException {
		required("file", file, true);
		required("variable", variable);
		required("entrypath", entryPaths);
		ZipFile zip = getZip(file, password);

		if (entryPaths.length > 1) throw engine.getExceptionUtil().createApplicationException("you can only read one entry!");

		String entryPath = entryPaths[0];
		try {
			FileHeader fh = zip.getFileHeader(entryPath);
			if (fh == null) fh = zip.getFileHeader("/" + entryPath);
			if (fh == null) fh = zip.getFileHeader(entryPath + "/");
			if (fh == null) fh = zip.getFileHeader("/" + entryPath + "/");
			if (fh == null) {
				String msg = engine.getExceptionUtil().similarKeyMessage(names(zip), entryPath, "entry", "zip file", "in the zip file [" + file + "]", true);
				throw engine.getExceptionUtil().createApplicationException("zip file [" + file + "] has no entry with name [" + entryPath + "] " + msg);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			InputStream is = zip.getInputStream(fh);
			engine.getIOUtil().copy(is, baos, true, false);

			if (binary) pageContext.setVariable(variable, baos.toByteArray());
			else {
				if (charset == null) charset = pageContext.getResourceCharset().name();
				pageContext.setVariable(variable, new String(baos.toByteArray(), charset));
			}
		}
		finally {
			// IOUtil.closeEL(zip);
		}

	}

	private Key[] names(ZipFile zip) throws ZipException {
		List<Key> list = new ArrayList<Key>();
		Iterator<FileHeader> it = zip.getFileHeaders().iterator();
		Creation util = engine.getCreationUtil();
		while (it.hasNext()) {
			list.add(util.createKey(it.next().getFileName()));
		}
		return list.toArray(new Key[list.size()]);
	}

	private void actionUnzip() throws PageException, IOException, ZipException {
		required("file", file, true);
		required("destination", destination, false);

		// ZipInputStream zis=null;
		String path;
		Resource target, parent;
		int index;
		try {
			ZipFile zip = getZip(file, password);
			Iterator<FileHeader> it = zip.getFileHeaders().iterator();
			FileHeader fh;
			while (it.hasNext()) {
				fh = it.next();
				path = fh.getFileName().replace('\\', '/');

				index = path.lastIndexOf('/');

				// recurse
				if (!recurse && index != -1) {
					continue;
				}

				target = ZipUtil.toResource(destination, fh.getFileName());

				if (target.exists() && overwrite) target.delete();

				// filter
				if ((filter != null && !filter.accept(target)) || target.exists()) {
					continue;
				}

				// entrypath
				if (!entryPathMatch(path)) {
					continue;
				}
				if (!storePath) target = destination.getRealResource(target.getName());
				if (fh.isDirectory()) {
					target.mkdirs();
				}
				else {
					if (storePath) {
						parent = target.getParentResource();
						if (!parent.exists()) parent.mkdirs();
					}

					if (overwrite || !target.exists()) {
						engine.getIOUtil().copy(zip.getInputStream(fh), target, true);
					}
				}
				target.setLastModified(Zip4jUtil.dosToJavaTme(fh.getLastModFileTime()));
			}
		}
		finally {
			// IOUtil.closeEL(zis);
		}
	}

	private void actionZip() throws PageException, IOException, ZipException {
		required("file", file, false);
		Resource dir = file.getParentResource();

		if (!dir.exists()) {
			throw engine.getExceptionUtil().createApplicationException("directory [" + dir.toString() + "] doesn't exist");
		}

		if ((params == null || params.isEmpty()) && source != null) {
			if (entryPaths != null && entryPaths.length > 1) throw engine.getExceptionUtil().createApplicationException("you can only one set entrypath in this context");
			setParam(new ZipParamSource(source, entryPaths == null ? null : entryPaths[0], filter, prefix, recurse));
		}

		if ((params == null || params.isEmpty())) {
			throw engine.getExceptionUtil().createApplicationException("No source/content specified");
		}

		Resource existing = null;
		try {

			// existing
			if (!overwrite && file.exists()) {
				existing = pageContext.getConfig().getTempDirectory().getRealResource(getTempName());
				engine.getIOUtil().copy(file, existing);
			}
			file.delete();

			ZipFile out = getZip(file, null);// password is only needed to read a zip

			Object[] arr = params.toArray();
			for (int i = arr.length - 1; i >= 0; i--) {
				if (arr[i] instanceof ZipParamSource) actionZip(out, (ZipParamSource) arr[i]);
				else if (arr[i] instanceof ZipParamContent) actionZip(out, (ZipParamContent) arr[i]);
			}

			if (existing != null) {
				ZipFile in = getZip(existing, password);
				try {
					Iterator<FileHeader> it = in.getFileHeaders().iterator();
					FileHeader fh;
					while (it.hasNext()) {
						fh = it.next();
						add(out, in.getInputStream(fh), fh.getFileName(), Zip4jUtil.dosToJavaTme(fh.getLastModFileTime()), true);
					}
				}
				finally {
					// zis.close();
				}
			}

			if (!Util.isEmpty(password)) out.setPassword(password);
		}
		finally {
			// ZipUtil.close(zos);
			if (existing != null) existing.delete();

		}

	}

	private String getTempName() {
		return "tempzip-" + (id++) + ".zip";
	}

	private String getTempName(String ext) {
		return "tempzip-" + (id++) + "." + ext;
	}

	private void actionZip(ZipFile zip, ZipParamContent zpc) throws PageException, IOException, ZipException {
		Object content = zpc.getContent();
		if (engine.getDecisionUtil().isBinary(content)) {
			add(zip, new ByteArrayInputStream(engine.getCastUtil().toBinary(content)), zpc.getEntryPath(), System.currentTimeMillis(), true);

		}
		else {
			String charset = zpc.getCharset();
			if (Util.isEmpty(charset)) charset = pageContext.getResourceCharset().name();
			add(zip, new ByteArrayInputStream(content.toString().getBytes(charset)), zpc.getEntryPath(), System.currentTimeMillis(), true);
		}
	}

	private void actionZip(ZipFile zip, ZipParamSource zps) throws IOException, ZipException, PageException {
		// prefix
		String prefix = zps.getPrefix();
		if (Util.isEmpty(prefix)) prefix = this.prefix;

		if (!Util.isEmpty(prefix)) {
			if (!prefix.endsWith("/")) prefix += "/";
		}
		else prefix = "";

		if (zps.getSource().isFile()) {

			String ep = zps.getEntryPath();
			if (ep == null) ep = zps.getSource().getName();
			if (!Util.isEmpty(prefix)) ep = prefix + ep;
			add(zip, zps.getSource(), ep, zps.getSource().lastModified());
		}
		else {
			// filter
			ResourceFilter f = zps.getFilter();
			if (f == null) f = this.filter;
			if (zps.isRecurse()) {
				if (f != null) f = new OrResourceFilter(new ResourceFilter[] { DirectoryResourceFilter.FILTER, f });
			}
			else {
				if (f == null) f = FileResourceFilter.FILTER;
			}

			addDir(zip, zps.getSource(), prefix, f);
		}
	}

	private File toFile(Resource res) {
		return CFMLEngineFactory.getInstance().getCastUtil().toFile(res, null);
	}

	private void addDir(ZipFile zip, Resource dir, String parent, ResourceFilter filter) throws IOException, ZipException, PageException {
		Resource[] children = filter == null ? dir.listResources() : dir.listResources(filter);
		boolean empty = true;
		if (children != null && children.length > 0) {
			for (int i = 0; i < children.length; i++) {

				if (children[i].isDirectory()) {
					addDir(zip, children[i], parent + children[i].getName() + "/", filter);
					empty = false;
				}
				else {
					add(zip, children[i], parent + children[i].getName(), children[i].lastModified());
					empty = false;
				}
			}
		}
		if (empty) zip.addFolder(parent, null);
	}

	private void add(ZipFile zip, InputStream is, String entryPath, long lastMod, boolean closeInput) throws IOException, ZipException, PageException {
		if (alreadyUsed == null) alreadyUsed = new HashSet<String>();
		else if (alreadyUsed.contains(entryPath)) {
			if (closeInput) Util.closeEL(is);
			return;
		}

		// TODO set lastMod
		try {
			zip.addStream(is, createParam(entryPath));
		}
		finally {
			if (closeInput) Util.closeEL(is);
		}
		alreadyUsed.add(entryPath);
	}

	private void add(ZipFile zip, Resource res, String entryPath, long lastMod) throws IOException, ZipException, PageException {
		if (alreadyUsed == null) alreadyUsed = new HashSet<String>();
		else if (alreadyUsed.contains(entryPath)) {
			return;
		}

		File f = engine.getCastUtil().toFile(res, null);
		if (f == null) {
			add(zip, res.getInputStream(), entryPath, lastMod, true);
			return;
		}

		zip.addFile((File) res, createParam(entryPath));
		alreadyUsed.add(entryPath);
	}

	private String[] splitPathX(String path) {
		int i1 = path.lastIndexOf('/');
		int i2 = path.lastIndexOf('\\');

		int index = i1 > i2 ? i1 : i2;
		if (index == -1) return new String[] { "", path };
		return new String[] { path.substring(0, index), path.substring(index + 1) };
	}

	private void close(InputStream is) {
		// TODO Auto-generated method stub

	}

	/*
	 * private void add(ZipFile zip, File file, String path, long lastMod) throws IOException,
	 * ZipException { if(alreadyUsed==null)alreadyUsed=new HashSet<String>(); else
	 * if(alreadyUsed.contains(path)) return;
	 * 
	 * // TODO set lastMod try { String[] split = splitPath(path); System.out.println("--> path:"+path);
	 * System.out.println(file); if(!Util.isEmpty(split[0],true)) zip.addFolder(split[0],
	 * createParam(path)); zip.addFile(file, createParam(path)); } finally { //if(closeInput)
	 * Util.closeEL(is); } alreadyUsed.add(path); }
	 */

	private ZipParameters createParam(String path) {

		ZipParameters param = new ZipParameters();

		param.setSourceExternalStream(true);

		// compression
		param.setCompressionMethod(compressionMethod);
		if (compressionMethod == Zip4jConstants.COMP_DEFLATE) {
			param.setCompressionLevel(deflate);
		}

		// password
		if (!Util.isEmpty(password)) {
			if (encryption == Zip4jConstants.ENC_NO_ENCRYPTION) encryption = Zip4jConstants.ENC_METHOD_STANDARD;
			param.setPassword(password);
		}

		// encryption
		if (encryption != Zip4jConstants.ENC_NO_ENCRYPTION) {
			param.setEncryptFiles(true);
			param.setEncryptionMethod(encryption);
			if (encryption == Zip4jConstants.ENC_METHOD_AES) {
				param.setAesKeyStrength(aes);
			}
		}

		// path
		param.setFileNameInZip(path);

		// TODO last mod

		return param;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {// print.out("doEndTag"+doCaching+"-"+body);
		if (filters != null && filters.size() > 0) {
			if (filters.size() == 1) filter = filters.getFirst();
			else filter = new OrResourceFilter(filters.toArray(new ResourceFilter[filters.size()]));
		}

		if (entryPathList != null && !entryPathList.isEmpty()) {
			entryPaths = entryPathList.toArray(new String[entryPathList.size()]);
		}

		try {
			if (action.equals("delete")) actionDelete();
			else if (action.equals("list")) actionList();
			else if (action.equals("read")) actionRead(false);
			else if (action.equals("readbinary")) actionRead(true);
			else if (action.equals("unzip")) actionUnzip();
			else if (action.equals("zip")) actionZip();
			else throw engine.getExceptionUtil().createApplicationException("invalid value [" + action + "] for attribute action",
					"values for attribute action are:info,move,rename,copy,delete,read,readbinary,write,append,upload");
		}
		catch (Exception ioe) {
			throw engine.getCastUtil().toPageException(ioe);
		}

		return EVAL_PAGE;
	}

	/**
	 * sets if tag has a body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
		/// this.hasBody=hasBody;
	}

	private ZipFile getZip(Resource file, String password) throws IOException, ZipException, PageException {
		ZipFile zf = new ZipFile(engine.getCastUtil().toFile(file));
		if (!Util.isEmpty(password) && zf.isEncrypted()) {
			zf.setPassword(password);
		}
		return zf;
	}

	/**
	 * throw a error if the value is empty (null)
	 * 
	 * @param attributeName
	 * @param attributValue
	 */
	private void required(String attributeName, String attributValue) throws PageException {
		if (Util.isEmpty(attributValue)) throw engine.getExceptionUtil().createApplicationException("invalid attribute combination for the tag zip",
				"attribute [" + attributeName + "] is required, if action is [" + action + "]");
	}

	private void required(String attributeName, String[] attributValue) throws PageException {
		if (attributValue == null || attributValue.length == 0) throw engine.getExceptionUtil().createApplicationException("invalid attribute combination for the tag zip",
				"attribute [" + attributeName + "] is required, if action is [" + action + "]");
	}

	/**
	 * throw a error if the value is empty (null)
	 * 
	 * @param attributeName
	 * @param attributValue
	 */
	private void required(String attributeName, Resource attributValue, boolean exists) throws PageException {
		if (attributValue == null) throw engine.getExceptionUtil().createApplicationException("invalid attribute combination for the tag zip",
				"attribute [" + attributeName + "] is required, if action is [" + action + "]");

		if (exists && !attributValue.exists()) throw engine.getExceptionUtil().createApplicationException(attributeName + " resource [" + attributValue + "] doesn't exist");
		else if (exists && !attributValue.canRead())
			throw engine.getExceptionUtil().createApplicationException("no access to " + attributeName + " resource [" + attributValue + "]");

	}

	public void setParam(ZipParamAbstr param) {
		if (params == null) {
			params = new ArrayList<ZipParamAbstr>();
			alreadyUsed = new HashSet<String>();
		}
		params.add(param);
	}

	/**
	 * @return the source
	 */
	public Resource getSource() {
		return source;
	}

}