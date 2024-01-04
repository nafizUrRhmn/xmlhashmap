package com.practice;

import com.practice.errorhandler.XmlErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlReader {

    private String readFromInputStream(String fileName)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();

        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(new FileInputStream("src/resource/"+fileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public Object processXml(String xmlFileName, String xsdFileName) throws SAXException, IOException {
        Document doc;

        String xmlData = readFromInputStream(xmlFileName);
        String schemaFileData = readFromInputStream(xsdFileName);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xmlData)));

        } catch (Exception e) {
            e.printStackTrace();
            return new RuntimeException("Invalid XML format");
        }

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.parse(new InputSource(new StringReader(schemaFileData)));

        } catch (Exception e) {
            e.printStackTrace();
            return new RuntimeException("Invalid XSD format");
        }
        // create a SchemaFactory capable of understanding WXS schemas
        //todo:: schema validation has a bug. need to be fixed
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        Source schemaFile = new StreamSource(schemaFileData);
//        Schema schema = factory.newSchema(schemaFile);
//        Validator validator = schema.newValidator();
//        XmlErrorHandler xsdErrorHandler = new XmlErrorHandler();
//        validator.setErrorHandler(xsdErrorHandler);
//        validator.validate(new DOMSource(doc));
//        if (xsdErrorHandler.getExceptions().size() > 0) {
//            List<String> errorMessages = new ArrayList<>();
//            for (SAXParseException x : xsdErrorHandler.getExceptions()) {
//                errorMessages.add(x.getMessage());
//            }
//            return errorMessages;
//        }
        doc.getDocumentElement().normalize();
        Map<String, String> map = new HashMap<>();
        traverseXML(doc.getDocumentElement(), map, "", -1, null);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        return map;
    }

    private Integer traverseXML(Node node, Map<String, String> nodeMap, String parent,
                                Integer index, String currentParentNodeArray) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String nodeName = node.getNodeName();
            String key = parent.isEmpty() ? nodeName : parent + "." + nodeName;

            if (node.hasChildNodes()) {
                NodeList nodeList = node.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node childNode = nodeList.item(i);
                    if (key.endsWith("LL")) {
                        if (currentParentNodeArray != null && currentParentNodeArray.equals(key)) {
                            int count = index + 1;
                            nodeMap.put(key + ".size", count + "");
                        } else {
                            index = 0;
                        }
                        currentParentNodeArray = key;
                    }
                    index = traverseXML(childNode, nodeMap, key, index, currentParentNodeArray);
                }
            }
        } else {
            String value = node.getTextContent().trim();
            if (parent != null && value != null && value.length() > 0) {
                String key;
                System.out.println("parent" + parent);
                System.out.println("index" + index);
                System.out.println("currentParent" + currentParentNodeArray);
                if (index >= 0 && parent.contains(currentParentNodeArray)) {
                    int indexValue = parent.indexOf("LL") + 2;
                    key = parent.substring(0, indexValue) + '[' + index + ']' + parent.substring(indexValue);
                } else {
                    key = parent;
                }
                if (nodeMap.containsKey(key)) {
                    index++;
                    int indexValue = parent.indexOf("LL") + 2;
                    key = parent.substring(0, indexValue) + '[' + index + ']' + parent.substring(indexValue);
                    System.out.println(key);
                }
                nodeMap.put(key, value);
            }
        }
        return index;
    }

}
