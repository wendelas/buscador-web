package net.indexador.negocio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GenericXMLParser extends DefaultHandler {
    private static Logger logger = Logger.getLogger(GenericXMLParser.class);
    private Map<String, String> item;
    private String content;
    private int quantidadeItens;

    public void startDocument() throws SAXException {
	super.startDocument();
    }

    public void endDocument() throws SAXException {
	super.endDocument();
    }

    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) throws SAXException {
	if (qName.equals("page")) {
	    item = new HashMap<String, String>();
	}
    }

    public void characters(char[] ch, int start, int length)
	    throws SAXException {
	if (item != null)
	    content = String.copyValueOf(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName)
	    throws SAXException {
	if (qName.equals("page")) {
	    item = null;
	} else if (qName.equals("title")) {
	    item.put("title", content);
	} else if (qName.equals("timestamp")) {
	    item.put("timestamp", content);
	} else if (qName.equals("username")) {
	    item.put("username", content);
	} else if (qName.equals("minor")) {
	    item.put("minor", content);
	} else if (qName.equals("text")) {
	    item.put("text", content);
	} else if (qName.equals("model")) {
	    item.put("model", content);
	} else if (qName.equals("format")) {
	    item.put("format", content);
	} else if (qName.equals("comment")) {
	    item.put("comment", content);
	}
    }

    public static void main(String[] args) throws FileNotFoundException {
	InputStream is = new FileInputStream(new File(
		"/home/marco/Dropbox/EPAXMLDownload.xml"));
	new GenericXMLParser().parse(is);
    }

    public void parse(InputStream is) {
	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    parser.parse(is, this);
	} catch (Exception e) {
	    logger.error(e);
	}
    }
}
