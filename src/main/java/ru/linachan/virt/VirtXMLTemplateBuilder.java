package ru.linachan.virt;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.linachan.virt.schema.VirtNetwork;
import ru.linachan.virt.schema.VirtTemplate;

import java.io.IOException;
import java.io.StringWriter;

public class VirtXMLTemplateBuilder {

    private final static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private final static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private final static XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    private static Logger logger = LoggerFactory.getLogger(VirtXMLTemplateBuilder.class);

    private Document document;

    public VirtXMLTemplateBuilder(VirtTemplate instanceTemplate, String instanceName) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        document = documentBuilder.parse(getClass().getResourceAsStream("/libvirt_template.xml"));

        prepareTemplate(instanceTemplate);
        setInstanceName(instanceName);
    }

    private Object xpath(String expression, QName resultType) throws XPathExpressionException {
        XPath xpath = X_PATH_FACTORY.newXPath();
        XPathExpression expr = xpath.compile(expression);
        return expr.evaluate(document, resultType);
    }

    private NodeList xpath(String expression) throws XPathExpressionException {
        return (NodeList) xpath(expression, XPathConstants.NODESET);
    }

    public Node xpathFirst(String expression) throws XPathExpressionException {
        NodeList result = xpath(expression);

        if (result.getLength() > 0)
            return result.item(0);

        return null;
    }

    private void prepareTemplate(VirtTemplate instanceTemplate) throws XPathExpressionException {
        Node memory = xpathFirst("//domain/memory");
        if (memory != null)
            memory.setTextContent(String.valueOf(instanceTemplate.memory * 1024));

        Node vcpu = xpathFirst("//domain/vcpu");
        if (vcpu != null)
            vcpu.setTextContent(String.valueOf(instanceTemplate.vcpu));

        Node devices = xpathFirst("//domain/devices");
        for (VirtNetwork networkData: instanceTemplate.network) {
            Element network = document.createElement("interface");
            Element source = document.createElement("source");

            switch (networkData.type) {
                case BRIDGE:
                    network.setAttribute("type", "bridge");
                    source.setAttribute("bridge", (networkData.source != null) ? networkData.source : "br0");
                    break;
                case HOST_ONLY:
                default:
                    network.setAttribute("type", "host-only");
                    source.setAttribute("network", (networkData.source != null) ? networkData.source : "default");
                    break;
            }

            Element model = document.createElement("model");
            model.setAttribute("type", "virtio");

            if (networkData.mac != null) {
                Element mac = document.createElement("mac");
                mac.setAttribute("address", networkData.mac);

                network.appendChild(mac);
            }

            network.appendChild(source);
            network.appendChild(model);

            devices.appendChild(network);
        }
    }

    private void setInstanceName(String instanceName) throws XPathExpressionException {
        Node result = xpathFirst("//domain/name");

        if (result != null) {
            result.setTextContent(instanceName);
        }
    }

    public String toString() {
        try {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            logger.error("Unable to generate XML template: {}", e.getMessageAndLocation());
            return null;
        }
    }
}
