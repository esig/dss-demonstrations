package eu.europa.esig.dss.web.business;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class FileManager<T> {

	public static <T> void writeAsXML(T object, OutputStream out) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = ctx.createMarshaller();
		marshaller.marshal(object, out);
	}
	
	public static <T> T readFromXml(Class<T> type, InputStream is) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(type);
		Unmarshaller u = ctx.createUnmarshaller();
		return (T) u.unmarshal(is);
	}
}
