package lynx.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lynx.data.Bundle;
import lynx.data.Design;
import lynx.data.MyEnums.*;
import lynx.data.DesignModule;
import lynx.data.Module;
import lynx.data.Parameter;
import lynx.data.Port;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlDesign {

    private static final Logger log = Logger.getLogger(XmlDesign.class.getName());

    /**
     * Read an XML file describing a design, parse it and create a List of
     * modules that describes it.
     * 
     * @param designPath
     *            a string containing the path of the design
     * @return List a list of modules parsed from the xml file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Design readXMLDesign(String designPath) throws ParserConfigurationException, SAXException,
            IOException {

        // Get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load and Parse the XML document
        // document contains the complete XML as a Tree
        Document document = builder.parse(ClassLoader.getSystemResourceAsStream(designPath));

        // Iterating through the nodes and extracting the data
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        String designName = document.getDocumentElement().getAttribute("name");

        Design design = new Design(designName);

        log.info("Parsing Design " + designName + " from file " + designPath);

        // will only parse modules in the first pass
        // could also disconnect them from my DOM after processing
        for (int i = 0; i < nodeList.getLength(); i++) {

            // We have encountered an xml tag
            Node node = nodeList.item(i);

            if (node instanceof Element) {

                switch (node.getNodeName()) {
                case "module":
                    DesignModule mod = parseModule(node, design);
                    design.addModule(mod);
                    break;
                case "port":
                    Port intPort = parsePort(node, design, false);
                    design.addPort(intPort);
                    break;
                case "parameter":
                    Parameter par = parseParameter(node, design);
                    design.addParameter(par);
                    break;
                }

            }
        }

        // Now that all modules are created
        // parse connections and wires in second pass
        for (int i = 0; i < nodeList.getLength(); i++) {

            // We have encountered a <Module> tag
            Node node = nodeList.item(i);

            if (node instanceof Element) {

                switch (node.getNodeName()) {
                case "connection":
                    parseConnection(node, design);
                    break;
                case "wire":
                    parseWire(node, design);
                    break;
                }
            }
        }

        return design;
    }

    private static void parseWire(Node node, Design design) {
        String topPort = node.getAttributes().getNamedItem("top").getNodeValue();

        String[] sub = node.getAttributes().getNamedItem("sub").getNodeValue().split("\\.");
        assert sub.length == 2 : "Port name must be in \"Module.Port\" format, this is wrong: " + sub;
        String subMod = sub[0];
        String subPort = sub[1];

        // fetch the ports
        Port topPor = design.getPortByName(topPort);
        Port subPor = design.getModuleByName(subMod).getPortByName(subPort);

        // add connection
        topPor.addConnection(subPor);
    }

    private static void parseConnection(Node node, Design design) {

        String[] start = node.getAttributes().getNamedItem("start").getNodeValue().split("\\.");
        assert start.length == 2 : "Port name must be in \"Module.Port\" format, this is wrong: " + start;
        String startMod = start[0];
        String startPort = start[1];

        String[] end = node.getAttributes().getNamedItem("end").getNodeValue().split("\\.");
        assert end.length == 2 : "Port name must be in \"Module.Port\" format, this is wrong: " + end;
        String endMod = end[0];
        String endPort = end[1];

        // fetch the ports
        Port startPor = design.getModuleByName(startMod).getPortByName(startPort);
        Port endPor = design.getModuleByName(endMod).getPortByName(endPort);

        // add connection (connection is added both at the origin
        // and destination, but each of them must have a different direction)
        startPor.addConnection(endPor);
        endPor.addConnection(startPor);

    }

    private static DesignModule parseModule(Node node, Design design) {
        String modName = node.getAttributes().getNamedItem("name").getNodeValue();
        String modType = node.getAttributes().getNamedItem("type").getNodeValue();

        DesignModule mod = new DesignModule(modType, modName);

        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node cNode = childNodes.item(j);

            // Identifying the child tag of Module encountered
            if (cNode instanceof Element) {

                // child tags can be either ports or parameters
                switch (cNode.getNodeName()) {
                case "parameter":
                    Parameter par = parseParameter(cNode, mod);
                    mod.addParameter(par);
                    break;
                case "port":
                    Port por = parsePort(cNode, mod, false);
                    mod.addPort(por);
                    break;
                case "bundle":
                    Bundle bun = parseBundle(cNode, mod);
                    mod.addBundle(bun);
                    break;
                default:
                    assert false : "Unexpected tag \"" + cNode.getNodeName() + "\"";
                }
            }
        }
        return mod;
    }

    private static Bundle parseBundle(Node node, Module mod) {
        Bundle bun = new Bundle();

        // loop over the ports in a bundle
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node cNode = childNodes.item(j);

            // Identifying the child tag of Module encountered
            if (cNode instanceof Element) {
                if (cNode.getNodeName().equals("port")) {

                    // parse this port and add it to module
                    Port por = parsePort(cNode, mod, true);
                    mod.addPort(por);

                    // now add this port to the bundle
                    switch (por.getType()) {
                    case DATA:
                        bun.addDataPort(por);
                        break;
                    case VALID:
                        bun.setValidPort(por);
                        break;
                    case READY:
                        bun.setReadyPort(por);
                        break;
                    case DST:
                        bun.setDstPort(por);
                        break;
                    default:
                        assert false : "\"" + por.getType()
                                + "\" is unexpected in a bundle, only data/valid/ready allowed";
                    }
                }
            }
        }

        return bun;
    }

    private static Parameter parseParameter(Node node, Module mod) {
        String name = node.getAttributes().getNamedItem("name").getNodeValue();
        String value = node.getAttributes().getNamedItem("value").getNodeValue();
        return new Parameter(name, value);
    }

    private static Port parsePort(Node node, Module mod, boolean bundled) {
        String pname = node.getAttributes().getNamedItem("name").getNodeValue();
        Direction direction = node.getAttributes().getNamedItem("direction").getNodeValue().equals("input") ? Direction.INPUT
                : Direction.OUTPUT;
        PortType type = PortType.UNKNOWN;
        if (node.getAttributes().getNamedItem("type") != null) {
            String typeString = node.getAttributes().getNamedItem("type").getNodeValue();
            type = typeString.equals("data") ? PortType.DATA : typeString.equals("valid") ? PortType.VALID : typeString
                    .equals("ready") ? PortType.READY : PortType.UNKNOWN;
        }
        int width = Integer.parseInt(node.getAttributes().getNamedItem("width").getNodeValue());
        int arrayWidth = 1; // leave this attribute optional
        if (node.getAttributes().getNamedItem("array_width") != null)
            arrayWidth = Integer.parseInt(node.getAttributes().getNamedItem("arrayWidth").getNodeValue());
        return new Port(pname, direction, width, arrayWidth, type, mod, bundled);
    }

    /**
     * Write a list of modules to an xml output file
     * 
     * @param design
     *            a Design object
     * @param outputFileName
     *            string containing path of output file
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static void writeXMLDesign(Design design, String outputFileName) throws ParserConfigurationException,
            TransformerException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // root element is called <design>
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("design");
        rootElement.setAttribute("name", design.getType());
        doc.appendChild(rootElement);

        log.info("Writing Design " + design.getName() + " to " + outputFileName);

        writeModulesAndConnections(doc, rootElement, design);

        writeParameters(doc, rootElement, design);

        writeTopLevelPorts(doc, rootElement, design);

        writeWires(doc, rootElement, design);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outputFileName));

        transformer.transform(source, result);
    }

    private static void writeWires(Document doc, Element rootElement, Design design) {
        for (Port por : design.getPorts().values()) {
            for (Port wire : por.getConnections()) {
                Element wireElement = doc.createElement("wire");
                wireElement.setAttribute("top", por.getName());
                wireElement.setAttribute("sub", wire.getFullNameDot());
                rootElement.appendChild(wireElement);
            }
        }
    }

    private static void writeTopLevelPorts(Document doc, Element rootElement, Design design) {
        for (Port intPort : design.getPorts().values()) {
            Element intPorElement = doc.createElement("port");
            intPorElement.setAttribute("direction", intPort.getDirectionString());
            intPorElement.setAttribute("name", intPort.getName());
            intPorElement.setAttribute("width", Integer.toString(intPort.getWidth()));
            if (intPort.getArrayWidth() != 1)
                intPorElement.setAttribute("array_width", Integer.toString(intPort.getArrayWidth()));
            rootElement.appendChild(intPorElement);
        }
    }

    private static void writeModulesAndConnections(Document doc, Element rootElement, Design design) {

        List<Module> modList = design.getAllModules();

        List<Element> connectionElements = new ArrayList<Element>();

        // loop over modules
        for (Module mod : modList) {

            // new <module> tag
            Element modElement = doc.createElement("module");
            // set name and type - both are required
            modElement.setAttribute("type", mod.getType());
            modElement.setAttribute("name", mod.getName());

            writeParameters(doc, modElement, mod);

            connectionElements.addAll(writePortsAndFindConnections(doc, modElement, mod));

            if (mod instanceof DesignModule)
                writeBundles(doc, modElement, (DesignModule) mod);

            rootElement.appendChild(modElement);
        }

        for (Element conElement : connectionElements) {
            rootElement.appendChild(conElement);
        }
    }

    private static void writeBundles(Document doc, Element modElement, DesignModule mod) {

        for (Bundle bun : mod.getBundles()) {
            Element bunElement = doc.createElement("Bundle");

            for (Port por : bun.getAllPorts()) {
                writePort(doc, bunElement, por);
            }

            modElement.appendChild(bunElement);
        }

    }

    private static List<Element> writePortsAndFindConnections(Document doc, Element modElement, Module mod) {

        List<Element> connectionElements = new ArrayList<Element>();

        // loop over ports
        Map<String, Port> porList = mod.getPorts();
        for (Port por : porList.values()) {

            if (!por.isBundled())
                writePort(doc, modElement, por);

            // find the list of connections
            // only go over input ports and find connections
            if (por.getDirection().equals("input"))
                for (Port con : por.getConnections()) {
                    Element conElement = doc.createElement("connection");
                    conElement.setAttribute("start", con.getFullNameDot());
                    conElement.setAttribute("end", por.getFullNameDot());
                    connectionElements.add(conElement);
                }
        }

        return connectionElements;
    }

    private static void writePort(Document doc, Element parentElement, Port por) {
        Element porElement = doc.createElement("port");
        porElement.setAttribute("name", por.getName());
        porElement.setAttribute("direction", por.getDirectionString());
        porElement.setAttribute("width", Integer.toString(por.getWidth()));
        if (por.getType() != PortType.UNKNOWN)
            porElement.setAttribute("type", por.getTypeString());
        if (por.getArrayWidth() != 1)
            porElement.setAttribute("arrray_width", Integer.toString(por.getArrayWidth()));
        parentElement.appendChild(porElement);
    }

    private static void writeParameters(Document doc, Element modElement, Module mod) {
        // loop over parameters
        for (Parameter par : mod.getParameters()) {
            Element parElement = doc.createElement("parameter");
            parElement.setAttribute("name", par.getName());
            parElement.setAttribute("value", par.getValue());
            modElement.appendChild(parElement);
        }

    }

}