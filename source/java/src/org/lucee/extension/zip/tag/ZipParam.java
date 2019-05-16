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

import javax.servlet.jsp.tagext.Tag;

import org.lucee.extension.zip.ZipParamContent;
import org.lucee.extension.zip.ZipParamSource;
import org.lucee.extension.zip.filter.UDFFilter;
import org.lucee.extension.zip.filter.WildcardPatternFilter;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

public final class ZipParam extends TagImpl {

    private String charset;
    private Object content;
    private String entryPath;
    private ResourceFilter filter;
    private String pattern;
    private String patternDelimiters;
    private String prefix;
    private lucee.commons.io.res.Resource source;
    private Boolean recurse = null;
    private Zip zip;
    private String password;
    private String encryption;

    @Override
    public void release() {
	super.release();
	charset = null;
	content = null;
	entryPath = null;
	filter = null;
	prefix = null;
	source = null;
	recurse = null;
	zip = null;
	pattern = null;
	patternDelimiters = null;
	password = null;
	encryption = null;
    }

    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
	this.charset = charset;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Object content) {
	this.content = content;
    }

    /**
     * @param entryPath the entryPath to set
     */
    public void setEntrypath(String entryPath) {
	this.entryPath = entryPath;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(Object filter) throws PageException {

	if (filter instanceof UDF) this.setFilter((UDF) filter);
	else if (filter instanceof String) this.setFilter((String) filter);
    }

    public void setFilter(UDF filter) throws PageException {

	this.filter = UDFFilter.createResourceAndResourceNameFilter(filter);
    }

    public void setFilter(String pattern) {

	this.pattern = pattern;
    }

    public void setFilterdelimiters(String patternDelimiters) {

	this.patternDelimiters = patternDelimiters;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
	this.prefix = prefix;
    }

    /**
     * @param strSource the source to set
     * @throws PageException
     */
    public void setSource(String strSource) throws PageException {
	Resource zipSrc = getZip().getSource();
	if (zipSrc != null) source = zipSrc.getRealResource(strSource);
	if (source == null || !source.exists()) source = engine.getResourceUtil().toResourceExisting(pageContext, strSource);
    }

    /**
     * @param recurse the recurse to set
     */
    public void setRecurse(boolean recurse) {
	this.recurse = engine.getCastUtil().toBoolean(recurse);
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public void setEncryptionalgorithm(String encryption) {
	this.encryption = encryption;
    }

    @Override
    public int doStartTag() throws PageException {

	if (this.filter == null && !Util.isEmpty(this.pattern)) this.filter = new WildcardPatternFilter(pattern, patternDelimiters);

	if (source != null) {
	    notAllowed("source", "charset", charset);
	    notAllowed("source", "content", content);

	    getZip().setParam(new ZipParamSource(source, entryPath, filter, prefix, recurse()));
	}
	else if (content != null) {
	    required("content", "entrypath", entryPath);
	    notAllowed("content,entrypath", "filter", filter);
	    notAllowed("content,entrypath", "prefix", prefix);
	    notAllowed("content,entrypath", "source", source);
	    notAllowed("content,entrypath", "recurse", recurse);

	    getZip().setParam(new ZipParamContent(content, entryPath, charset));
	}
	else if (filter != null) {
	    notAllowed("filter", "charset", charset);
	    notAllowed("filter", "content", content);
	    notAllowed("filter", "prefix", prefix);
	    notAllowed("filter", "source", source);
	    getZip()._setFilter(filter);

	}
	else if (entryPath != null) {
	    notAllowed("entryPath", "charset", charset);
	    notAllowed("entryPath", "content", content);
	    notAllowed("entryPath", "prefix", prefix);
	    notAllowed("entryPath", "source", source);
	    getZip().setEntrypath(entryPath);
	}
	else if (!Util.isEmpty(password)) {
	    notAllowed("entryPath", "charset", charset);
	    notAllowed("entryPath", "content", content);
	    notAllowed("entryPath", "prefix", prefix);
	    notAllowed("entryPath", "source", source);
	    getZip().setPassword(password);
	    if (!Util.isEmpty(encryption)) getZip().setEncryptionalgorithm(encryption);
	}
	else throw engine.getExceptionUtil().createApplicationException("invalid attribute combination");

	return SKIP_BODY;
    }

    private boolean recurse() {
	return recurse == null ? true : recurse.booleanValue();
    }

    private Zip getZip() throws PageException {
	if (zip != null) return zip;
	Tag parent = getParent();
	while (parent != null && !(parent instanceof Zip)) {
	    parent = parent.getParent();
	}
	if (parent instanceof Zip) {
	    return zip = (Zip) parent;
	}
	throw engine.getExceptionUtil().createApplicationException("Wrong Context, tag ZipParam must be inside a Zip tag");
    }

    private void notAllowed(String combi, String name, Object value) throws PageException {
	if (value != null) throw engine.getExceptionUtil().createApplicationException("attribute [" + name + "] is not allowed in combination with attribute(s) [" + combi + "]");
    }

    @Override
    public void required(String combi, String name, Object value) throws PageException {
	if (value == null) throw engine.getExceptionUtil().createApplicationException("attribute [" + name + "] is required in combination with attribute(s) [" + combi + "]");
    }

    @Override
    public int doEndTag() {
	return EVAL_PAGE;
    }
}