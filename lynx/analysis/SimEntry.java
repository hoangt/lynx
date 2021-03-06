package lynx.analysis;

import lynx.data.MyEnums.SimModType;

class SimEntry {

    int currMod;
    int queueDuration;
    int time;
    int endTime;
    int srcRouter;
    int dstRouter;
    int currRouter;
    int data;
    int srcMod;
    int dstMod;

    boolean complete;

    protected SimEntry(String line) {

        currMod = findFieldValue(line, PerfAnalysis.CURRMOD_POS);
        time = findFieldValue(line, PerfAnalysis.TIME_POS);
        queueDuration = 0;
        srcRouter = findFieldValue(line, PerfAnalysis.SRCROUTER_POS);
        dstRouter = findFieldValue(line, PerfAnalysis.DSTROUTER_POS);
        currRouter = findFieldValue(line, PerfAnalysis.CURRROUTER_POS);
        data = findFieldValue(line, PerfAnalysis.DATA_POS);
        // sinks have the sourcemod id as well
        if (PerfAnalysis.findModType(line) == SimModType.SINK) {
            srcMod = findFieldValue(line, PerfAnalysis.SRCMOD_POS);
        } else if (PerfAnalysis.findModType(line) == SimModType.SRC) {
            srcMod = currMod;
        }
        endTime = 0;
        dstMod = -1;
        this.complete = false;
    }

    protected static String hash(int srcMod, int data) {
        return "" + srcMod + "_" + data;
    }

    protected String getHash() {
        return hash(srcMod, data);
    }

    /**
     * update a simentry with arrival time
     * 
     * @param simEntry
     */
    protected void update(SimEntry simEntry) {
        this.endTime = simEntry.time;
        this.dstMod = simEntry.currMod;
        this.complete = true;
    }

    /**
     * Updates a queued simentry with send time and queuetime
     * 
     * @param simEntry
     */
    public void updateQueued(SimEntry simEntry) {
        // previous time was the time at which we were queued
        int sendTime = simEntry.time;
        int queueTime = time;
        this.queueDuration = sendTime - queueTime;
        this.time = sendTime;
    }

    protected int getLatency() {
        return (endTime - time) / PerfAnalysis.CLK_PERIOD;
    }

    protected int getQueueTime() {
        return queueDuration / PerfAnalysis.CLK_PERIOD;
    }

    private int findFieldValue(String line, int fieldPos) {
        String partList[] = line.split(";");
        String field = partList[fieldPos];
        int value = Integer.parseInt(field.split("=")[1].trim());
        return value;
    }

    public int getSinkHash() {
        return time;
    }

}
