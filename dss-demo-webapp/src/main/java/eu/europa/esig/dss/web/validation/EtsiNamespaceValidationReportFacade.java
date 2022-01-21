package eu.europa.esig.dss.web.validation;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import eu.europa.esig.validationreport.ValidationReportFacade;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an alternative {@code ValidationReportFacade}, enforcing the defined namespace prefixes
 *
 */
public final class EtsiNamespaceValidationReportFacade extends ValidationReportFacade {

    /** namespacePrefixMapper attribute name */
    private static final String NAMESPACE_PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";

    /**
     * Default constructor
     */
    private EtsiNamespaceValidationReportFacade() {
    }

    /**
     * Creates a new facade
     *
     * @return {@link EtsiNamespaceValidationReportFacade}
     */
    public static EtsiNamespaceValidationReportFacade newFacade() {
        return new EtsiNamespaceValidationReportFacade();
    }

    @Override
    public Marshaller getMarshaller(boolean validate) throws JAXBException, SAXException, IOException {
        Marshaller marshaller = super.getMarshaller(validate);
        marshaller.setProperty(NAMESPACE_PREFIX_MAPPER, ValidationReportNamespacePrefixMapper.getInstance());
        return marshaller;
    }

    /**
     * This class is used to define namespace prefixes to be used within the ETSI Validation Report
     *
     */
    private final static class ValidationReportNamespacePrefixMapper extends NamespacePrefixMapper {

        /** The map between namespace uris and namespace prefixes */
        private static Map<String, String> prefixMap;

        /** Singleton instance */
        private static ValidationReportNamespacePrefixMapper singleton;

        static {
            prefixMap = new HashMap<>();
            prefixMap.put("http://www.w3.org/2001/XMLSchema", "xs");
            prefixMap.put("http://www.w3.org/2007/XMLSchema-versioning", "vc");
            prefixMap.put("http://www.w3.org/2000/09/xmldsig#", "ds");
            prefixMap.put("urn:oasis:names:tc:dss:1.0:core:schema", "dss");
            prefixMap.put("http://uri.etsi.org/02231/v2#", "tsl");
            prefixMap.put("http://uri.etsi.org/01903/v1.3.2#", "XAdES");
            prefixMap.put("http://uri.etsi.org/19102/v1.2.1#", "vr");
        }

        /**
         * Default constructor
         */
        private ValidationReportNamespacePrefixMapper() {
        }

        /**
         * Returns instance of {@code ValidationReportNamespacePrefixMapper}
         *
         * @return {@link ValidationReportNamespacePrefixMapper}
         */
        private static ValidationReportNamespacePrefixMapper getInstance() {
            if (singleton == null) {
                singleton = new ValidationReportNamespacePrefixMapper();
            }
            return singleton;
        }

        @Override
        public String getPreferredPrefix(String s, String s1, boolean b) {
            return prefixMap.getOrDefault(s, s1);
        }

    }

}
