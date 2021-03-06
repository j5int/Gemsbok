/**
 * Copyright (c) 2009, Signavio GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/**
 * 
 */
package com.signavio.platform.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.signavio.platform.core.PlatformProperties;
import com.signavio.platform.exceptions.InitializationException;

/**
 * Read the properties from the web.xml file
 * @author Bjoern Wagner
 *
 */
public class FsPlatformPropertiesImpl implements PlatformProperties {
	private final String serverName;
	private final String platformUri;
	private final String explorerUri;
	private final String editorUri;
	private final String libsUri;
	private final String supportedBrowserEditor;
	
	private final String rootDirectoryPath;
	private final Map<String, Set<String>> metaData;
	

	public FsPlatformPropertiesImpl(ServletContext context) {
		supportedBrowserEditor = context.getInitParameter("supportedBrowserEditor");
		
		Properties props = new Properties();
		try {
			props.load(this.getClass().getClassLoader().getResourceAsStream("configuration.properties"));
		} catch (IOException e) {
			throw new InitializationException(e);
		}
		
		String tempRootDirectoryPath = props.getProperty("fileSystemRootDirectory");
		System.out.println("ROOT: " +tempRootDirectoryPath );
		if (tempRootDirectoryPath.endsWith(File.separator)) {
			rootDirectoryPath = tempRootDirectoryPath.substring(0, tempRootDirectoryPath.length()-1);
		} else {
			rootDirectoryPath = tempRootDirectoryPath;
		}
		
		serverName = props.getProperty("host");
		platformUri = context.getContextPath() + "/p";
		explorerUri = context.getContextPath() + "/explorer";
		editorUri = context.getContextPath() + "/editor";
		libsUri = context.getContextPath() + "/libs";
		
		Map<String, Set<String>> md = new HashMap<String, Set<String>>();
		
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("bpmn_export.json");
			if (in != null)
			{
				try {
					JSONObject bpmnExportSettings = new JSONObject(new JSONTokener(new InputStreamReader(in)));
					JSONArray a = bpmnExportSettings.optJSONArray("metadata");
					if (a != null)
					{
						for (int i = 0; i < a.length(); i++)
						{
							JSONObject e = a.optJSONObject(i);
							if (e != null)
							{
								String stencil = e.optString("stencil");
								if (stencil != null)
								{
									JSONArray p = e.optJSONArray("properties");
									if (p != null)
									{
										for (int j = 0; j < p.length(); j++)
										{
											String p_name = p.optString(j);
											if (p_name != null)
											{
												Set<String> p_set = md.get(stencil);
												if (p_set == null)
												{
													p_set = new HashSet<String>();
													md.put(stencil, p_set);
												}
												p_set.add(p_name);
											}
										}
									}
								}
							}
						}
					}
							
				}
				finally {
					in.close();
				}
				
			}
		} catch (Exception e) {
			throw new InitializationException(e);
		}
		
		//Now make it read only. We can remove this later if we need to, but we can't put it in later!:
		Map<String, Set<String>> nmd = new HashMap<String, Set<String>>();
		Iterator<Entry<String, Set<String>>> it = md.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Set<String>> entry = it.next();
			nmd.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
		}
		    
		metaData = Collections.unmodifiableMap(nmd);
	}
	
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getServerName()
	 */
	public String getServerName() {
		return serverName;
	}
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getPlatformUri()
	 */
	public String getPlatformUri() {
		return platformUri;
	}
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getExplorerUri()
	 */
	public String getExplorerUri() {
		return explorerUri;
	}
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getEditorUri()
	 */
	public String getEditorUri() {
		return editorUri;
	}
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getLibsUri()
	 */
	public String getLibsUri() {
		return libsUri;
	}	
	/* (non-Javadoc)
	 * @see com.signavio.platform.core.impl.PlatformProperties#getSupportedBrowserEditorRegExp()
	 */
	public String getSupportedBrowserEditorRegExp() {
		return supportedBrowserEditor;
	}

	public Set<String> getAdmins() {
		return new HashSet<String>(0);
	}
	
	public String getRootDirectoryPath() {
		return rootDirectoryPath;
	}
	
	public Map<String, Set<String>> getMetaData() {
		return metaData;
	}
}
