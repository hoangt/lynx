package lynx.verilog;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import lynx.data.Design;
import lynx.data.Module;
import lynx.data.Port;
import lynx.xml.XMLIO;

/**
 * Functions to write design to Verilog
 * 
 * @author Mohamed
 *
 */
public class VerilogOut {

    private static final Logger log = Logger.getLogger(XMLIO.class.getName());

    public static void writeVerilogDesign(Design design) throws FileNotFoundException, UnsupportedEncodingException {

        log.info("Writing out design to " + design.getName() + ".v");

        PrintWriter writer = new PrintWriter("designs/" + design.getName() + ".v", "UTF-8");

        writePreamble(design, writer);

        writeInterface(design, writer);

        writeWires(design, writer);

        writeModules(design, writer);

        writer.close();
    }

    private static void writeWires(Design design, PrintWriter writer) {
        // loop over top-level wires and will probably need to create wires
        // and assign statements for the outputs to avoid reg/wire problems

        writer.println("//wires for the top-level");
        for (Port por : design.getPorts().values()) {
            if (por.getDirection().equals("output")) {
                String widthPart = por.getWidth() > 1 ? " [" + (por.getWidth() - 1) + ":" + "0] " : " ";
                writer.println("wire" + widthPart + por.getName() + "_wire");
            }
        }
        writer.println();

        // loop over all ports in the design, create a wire for each output port
        // it may be feeding multiple input ports -- that's why

        for (Module mod : design.getModules().values()) {
            writer.println("//wires for the outputs in Module " + mod.getName());
            for (Port por : mod.getPorts().values()) {
                if (por.getDirection().equals("output")) {
                    String widthPart = por.getWidth() > 1 ? " [" + (por.getWidth() - 1) + ":" + "0] " : " ";
                    writer.println("wire" + widthPart + por.getFullNameDash() + "_wire");
                }
            }
            writer.println();
        }

    }

    private static void writeModules(Design design, PrintWriter writer) {
        for (Module mod : design.getModules().values()) {

            writer.println(mod.getType() + " " + mod.getName() + " (");

            for (Port por : mod.getPorts().values()) {
                writer.println("\t." + por.getName() + "(),");
            }

            writer.println(");");
            writer.println();
        }
    }

    private static void writeInterface(Design design, PrintWriter writer) {
        writer.println("module " + design.getName());
        writer.println("(");

        int numPorts = design.getPorts().size();

        for (Port intPort : design.getPorts().values()) {
            String widthPart = intPort.getWidth() > 1 ? " [" + (intPort.getWidth() - 1) + ":" + "0] " : " ";
            writer.print("\t" + intPort.getDirection() + widthPart + intPort.getName());
            String arrayWidthPart = intPort.getArrayWidth() > 1 ? " [" + (intPort.getArrayWidth() - 1) + ":" + "0] "
                    : "";
            writer.print(arrayWidthPart);
            if (numPorts-- == 1)
                writer.println("");
            else
                writer.println(",");
        }

        writer.println(");");
        writer.println();
    }

    private static void writePreamble(Design design, PrintWriter writer) {
        writer.println("// auto-generated by noclynx");
        writer.println();
    }

}
