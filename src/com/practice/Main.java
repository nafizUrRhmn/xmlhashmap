package com.practice;

import org.xml.sax.SAXException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, SAXException {
    XmlReader xmlReader = new XmlReader();
    xmlReader.processXml("bookstore.xml", "bookstore.xsd");
    }
}
