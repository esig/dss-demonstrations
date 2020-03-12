package eu.europa.esig.dss.web.ws;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.diagnostic.jaxb.XmlAbstractToken;
import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlOrphanCertificateToken;
import eu.europa.esig.dss.diagnostic.jaxb.XmlOrphanRevocationToken;
import eu.europa.esig.dss.diagnostic.jaxb.XmlRevocation;
import eu.europa.esig.dss.diagnostic.jaxb.XmlSignature;
import eu.europa.esig.dss.diagnostic.jaxb.XmlSignerData;
import eu.europa.esig.dss.diagnostic.jaxb.XmlTimestamp;
import eu.europa.esig.dss.diagnostic.jaxb.XmlTimestampedObject;
import eu.europa.esig.dss.enumerations.TimestampedObjectType;
import eu.europa.esig.dss.web.config.CXFConfig;

public abstract class AbstractRestIT extends AbstractIT {

	private static class XmlTimestampedObjectDeserializer extends StdDeserializer<XmlTimestampedObject> {

		private static final long serialVersionUID = -5743323649165950906L;

		protected XmlTimestampedObjectDeserializer() {
			super(XmlTimestampedObject.class);
		}

		@Override
		public XmlTimestampedObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			ObjectMapper mapper = (ObjectMapper) jp.getCodec();
			ObjectNode root = (ObjectNode) mapper.readTree(jp);
			JsonNode categoryNode = root.get("Category");
			TimestampedObjectType category = TimestampedObjectType.valueOf(categoryNode.textValue());
			JsonNode tokenNode = root.get("Token");

			XmlTimestampedObject timestampedObject = new XmlTimestampedObject();
			timestampedObject.setCategory(category);

			XmlAbstractToken token = null;
			switch (category) {
			case SIGNATURE:
				token = new XmlSignature();
				break;
			case CERTIFICATE:
				token = new XmlCertificate();
				break;
			case REVOCATION:
				token = new XmlRevocation();
				break;
			case TIMESTAMP:
				token = new XmlTimestamp();
				break;
			case SIGNED_DATA:
				token = new XmlSignerData();
				break;
			case ORPHAN_CERTIFICATE:
				token = new XmlOrphanCertificateToken();
				break;
			case ORPHAN_REVOCATION:
				token = new XmlOrphanRevocationToken();
				break;
			default:
				throw new InvalidFormatException(jp, "Unsupported category value " + category, category, TimestampedObjectType.class);
			}

			token.setId(tokenNode.textValue());
			timestampedObject.setToken(token);
			return timestampedObject;
		}

	}
	
	protected JacksonJsonProvider jacksonJsonProvider() {
		JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
		ObjectMapper objectMapper = new CXFConfig().objectMapper();
		
		SimpleModule mod = new SimpleModule("XmlTimestampedObjectDeserializerModule");
	    mod.addDeserializer(XmlTimestampedObject.class, new XmlTimestampedObjectDeserializer());
	    objectMapper.registerModule(mod);
	    
		jacksonJsonProvider.setMapper(objectMapper);
		return jacksonJsonProvider;
	}

}
