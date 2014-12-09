package lynx.verilog;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import lynx.data.Design;
import lynx.data.Module;
import lynx.data.Port;
import lynx.data.MyEnums.Direction;

/**
 * Functions to write design to Verilog
 * 
 * @author Mohamed
 *
 */
public class VerilogOut {

    private static final Logger log = Logger.getLogger(VerilogOut.class.getName());

    public static void writeVerilogDesign(Design design) throws FileNotFoundException, UnsupportedEncodingException {

        log.info("Writing out design to " + design.getType() + ".v");

        PrintWriter writer = new PrintWriter("designs/" + design.getType() + ".v", "UTF-8");

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
            if (por.getDirection() == Direction.OUTPUT) {
                writeWire(por, writer);
            }
        }
        writer.println();

        // loop over all ports in the design, create a wire for each output port
        // it may be feeding multiple input ports -- that's why

        for (Module mod : design.getAllModules()) {
            writer.println("//wires for the outputs in Module " + mod.getName());
            for (Port por : mod.getPorts().values()) {
                if (por.getDirection() == Direction.OUTPUT) {
                    writeWire(por, writer);
                }
            }
            writer.println();
        }

    }

    private static void writeWire(Port por, PrintWriter writer) {
        String widthPart = getWidthPart(por);
        String arrayWidthPart = getArrayWidthPart(por);
        writer.println("wire" + widthPart + por.getFullNameDash() + "_wire" + arrayWidthPart + ";");
    }

    private static void writeModules(Design design, PrintWriter writer) {
        for (Module mod : design.getAllModules()) {

            writer.println(mod.getType() + " " + mod.getName() + " (");

            for (Port por : mod.getPorts().values()) {
                writer.println("\t." + por.getName() + "(),");
            }

            writer.println(");");
            writer.println();
        }
    }

    private static void writeInterface(Design design, PrintWriter writer) {
        writer.println("module " + design.getType());
        writer.println("(");

        int numPorts = design.getPorts().size();

        for (Port intPort : design.getPorts().values()) {
            String widthPart = getWidthPart(intPort);
            String arrayWidthPart = getArrayWidthPart(intPort);
            writer.print("\t" + intPort.getDirection() + widthPart + intPort.getName() + arrayWidthPart);
            if (numPorts-- == 1)
                writer.println("");
            else
                writer.println(",");
        }

        writer.println(");");
        writer.println();
    }

    private static String getArrayWidthPart(Port por) {
        return por.getArrayWidth() > 1 ? " [0:" + (por.getArrayWidth() - 1) + "]" : "";
    }

    private static String getWidthPart(Port por) {
        return por.getWidth() > 1 ? " [" + (por.getWidth() - 1) + ":" + "0] " : " ";
    }

    private static void writePreamble(Design design, PrintWriter writer) {
        writer.println("// auto-generated by noclynx");
        writer.println();
    }

}
