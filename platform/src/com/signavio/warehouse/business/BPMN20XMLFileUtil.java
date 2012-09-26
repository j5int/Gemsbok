package com.signavio.warehouse.business;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.oryxeditor.server.diagram.basic.BasicDiagramBuilder;
import org.xml.sax.SAXException;

import com.signavio.platform.core.Platform;
import com.signavio.platform.core.PlatformProperties;
import com.signavio.platform.util.fsbackend.FileSystemUtil;

import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
import de.hpi.bpmn2_0.transformation.Diagram2XmlConverter;

public class BPMN20XMLFileUtil {

	public static void storeBPMN20XMLFile(String path, String jsonRep) throws IOException, JSONException, BpmnConverterException, JAXBException, SAXException, ParserConfigurationException, TransformerException {
		PlatformProperties props = Platform.getInstance().getPlatformProperties();
		Map<String, Object> configuration = new HashMap<String, Object>();
		Map<String, Set<String>> metaData = new HashMap<String, Set<String>>();
		metaData.put("Task", new HashSet<String>());
		metaData.get("Task").add("checklist_name");
		metaData.get("Task").add("completion_conditions");
		
		configuration.put("metaData", metaData);		
				
		Diagram2XmlConverter converter = new Diagram2XmlConverter(
				BasicDiagramBuilder.parseJson(jsonRep), 
				Platform.getInstance().getFile("/WEB-INF/xsd/BPMN20.xsd").getAbsolutePath(),
				configuration);
		
		StringWriter xml = converter.getXml();
		
		FileSystemUtil.deleteFileOrDirectory(path);
		FileSystemUtil.createFile(path, xml.toString());
	}
}
