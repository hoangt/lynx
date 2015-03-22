package lynx.main;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import lynx.data.Design;
import lynx.graphics.Gui;
import lynx.interconnect.NocInterconnect;
import lynx.clustering.NocClustering;
import lynx.log.MyLogger;
import lynx.nocmapping.NocMapping;
import lynx.xml.XmlDesign;
import lynx.verilog.VerilogOut;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public final static void main(String[] args) throws Exception {

        // draw fcns
        Gui gui = new Gui(null);

        @SuppressWarnings("unused")
        // MyLogger log = new MyLogger(Level.INFO);
        MyLogger parentLog = new MyLogger(Level.ALL);

        // read XML design
        Design design = XmlDesign.readXMLDesign("designs/tarjan_test.xml");
        design.update();

        // add NoC circuitry - NoC and translators
        NocInterconnect.addNoc(design, "designs/noc.xml");

        // cluster design into SCCs
        NocClustering.clusterDesign(design);

        // find possible locations on the NoC
        long startTime = System.nanoTime();
        NocMapping.findMappings(design);
        long endTime = System.nanoTime();
        DecimalFormat secondsFormat = new DecimalFormat("#.00");
        log.info("Elapsed Time = " + secondsFormat.format((endTime - startTime) / 1e9) + " seconds");

        gui.setDesign(design);

        // write out XML design
        XmlDesign.writeXMLDesign(design, "designs/out.xml");

        // write out verilog design
        VerilogOut.writeVerilogDesign(design);

    }

}
