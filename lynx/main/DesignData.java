package lynx.main;

import lynx.data.Design;
import lynx.data.Noc;
import lynx.nocmapping.Mapping;

/**
 * This singleton class holds all design info for the flow when using an NoC
 * 
 * @author Mohamed
 *
 */
public class DesignData {

    /**
     * The user-entered design
     */
    Design design;

    /**
     * The specified NoC
     */
    Noc noc;

    /**
     * Design post-clustering
     */
    Design clusteredDesign;

    /**
     * the design mapping from clustered Module to router
     */
    Mapping nocMapping;

    /**
     * Singleton of design data
     */
    private static DesignData instance = null;

    private DesignData() {
        this.design = null;
        this.clusteredDesign = null;
        this.noc = null;
        this.nocMapping = null;
    }

    public static DesignData getInstance() {
        if (instance == null)
            instance = new DesignData();
        return instance;
    }

    public final Design getDesign() {
        return design;
    }

    public final void setDesign(Design design) {
        this.design = design;
        design.update();
    }

    public final Noc getNoc() {
        return noc;
    }

    public final void setNoc(Noc noc) {
        this.noc = noc;
    }

    public final Design getClusteredDesign() {
        return clusteredDesign;
    }

    public final void setClusteredDesign(Design clusteredDesign) {
        this.clusteredDesign = clusteredDesign;
    }

    public final Mapping getNocMapping() {
        return nocMapping;
    }

    public final void setNocMapping(Mapping nocMapping) {
        this.nocMapping = nocMapping;
    }

}