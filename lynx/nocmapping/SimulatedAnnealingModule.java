package lynx.nocmapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import lynx.data.Design;
import lynx.data.Noc;
import lynx.main.ReportData;

public class SimulatedAnnealingModule {

    private static final Logger log = Logger.getLogger(SimulatedAnnealingModule.class.getName());

    private final static int SEED = 1;

    public static void findMappings(Design design, Noc noc) {

        log.info("Figuring out the best location of modules on the NoC using simulated annealing...");

        // time
        long startTime = System.nanoTime();

        // random seed
        Random rand = new Random(SEED);

        int nocNumRouters = noc.getNumRouters();
        int numModules = design.getNumDesignModules();

        assert numModules <= nocNumRouters : "Number of modules in design cannot (currently) exceed the number of routers in NoC";

        boolean[][] currPermMatrix = new boolean[numModules][nocNumRouters];

        for (int i = 0; i < numModules; i++) {
            for (int j = 0; j < nocNumRouters; j++) {
                currPermMatrix[i][j] = false;
            }
        }

        // initial solution is module i mapped to router i
        for (int i = 0; i < numModules; i++) {
            currPermMatrix[i][i] = true;
        }

        Mapping currMapping = new Mapping(currPermMatrix, design, noc);
        double cost = currMapping.computeCost();

        // time
        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1e9;

        // stats
        int totalMoves = 0;
        int takenMoves = 0;

        // annealing params
        double initialTemp = 100;
        double temp = initialTemp;
        double tempFac = 0.99;
        int tempInterval = 100;

        int stable_for = 0;

        List<Double> debugAnnealCost = new ArrayList<Double>();
        List<Double> debugAnnealTemp = new ArrayList<Double>();
        debugAnnealCost.add(cost);

        // start anneal
        while (cost > 1 && stable_for < 5000 && elapsedSeconds < 100) {

            // decrement temperature
            if (totalMoves % tempInterval == 0) {
                temp = temp * tempFac;
                tempInterval = setTempInterval(temp);
            }

            // make a move
            boolean[][] newPermMatrix = annealMove(currPermMatrix, rand);

            // measure its cost
            currMapping = new Mapping(newPermMatrix, design, noc);
            double newCost = currMapping.computeCost();
            double oldCost = cost;
            boolean acceptMove = (((newCost - cost) / cost) < temp / initialTemp);

            log.finest(currMapping.toString());

            if (newCost < cost || acceptMove) {
                currPermMatrix = newPermMatrix;
                cost = newCost;
                takenMoves++;
                log.fine("Cost = " + cost + ", temp = " + temp);
            }

            debugAnnealCost.add(cost);
            debugAnnealTemp.add(temp);

            // how long have we been at this cost?
            stable_for = cost == oldCost ? stable_for + 1 : 0;

            // time
            endTime = System.nanoTime();
            elapsedSeconds = (endTime - startTime) / 1e9;

            // stats
            totalMoves++;
        }

        log.info("Total number of moves = " + takenMoves + "/" + totalMoves);

        log.info("Final mapping cost = " + cost);
        ReportData.getInstance().writeToRpt("map_cost = " + cost);

        // export solution to the design
        currMapping = new Mapping(currPermMatrix, design, noc);
        design.setSingleMapping(currMapping);
        design.setDebugAnnealCost(debugAnnealCost);
        design.setDebugAnnealTemp(debugAnnealTemp);

    }

    private static int setTempInterval(double temp) {
        if (temp < 100 && temp >= 90)
            return 100;
        if (temp < 90 && temp >= 80)
            return 100;
        if (temp < 80 && temp >= 70)
            return 100;
        if (temp < 70 && temp >= 60)
            return 100;
        if (temp < 60 && temp >= 50)
            return 100;
        if (temp < 50 && temp >= 40)
            return 100;
        if (temp < 40 && temp >= 30)
            return 100;
        if (temp < 30 && temp >= 20)
            return 200;
        if (temp < 20 && temp >= 10)
            return 200;
        if (temp < 10 && temp >= 0)
            return 200;
        else
            return 0;
    }

    private static boolean[][] annealMove(boolean[][] currPermMatrix, Random rand) {
        int numModules = currPermMatrix.length;
        int numRouters = currPermMatrix[0].length;

        // pick a module and router at random
        int srcMod = rand.nextInt(numModules);
        int dstRouter = rand.nextInt(numRouters);

        // make sure we're actually making a move
        int srcRouter = getModuleRouter(srcMod, currPermMatrix);
        if (srcRouter == dstRouter) {
            if (dstRouter == numRouters - 1)
                dstRouter--;
            else
                dstRouter++;
        }

        // check if there is already a module at this router
        int dstMod = numModules;
        for (int i = 0; i < numModules; i++) {
            if (currPermMatrix[i][dstRouter])
                dstMod = i;
        }

        log.finer("attempt to move " + srcMod + " to " + dstRouter);
        if (dstMod != numModules)
            log.finer("and swap " + dstMod + " to " + srcRouter);

        boolean[][] newPermMatrix = new boolean[numModules][numRouters];

        // now create the new permuted matrix
        for (int i = 0; i < numModules; i++) {
            for (int j = 0; j < numRouters; j++) {
                if (i == srcMod) {
                    if (j == dstRouter) {
                        newPermMatrix[i][j] = true;
                    } else {
                        newPermMatrix[i][j] = false;
                    }
                } else if (i == dstMod) {
                    if (j == srcRouter) {
                        newPermMatrix[i][j] = true;
                    } else {
                        newPermMatrix[i][j] = false;
                    }
                } else {
                    newPermMatrix[i][j] = currPermMatrix[i][j];
                }
            }
        }

        return newPermMatrix;
    }

    private static int getModuleRouter(int srcMod, boolean[][] currPermMatrix) {
        for (int i = 0; i < currPermMatrix[0].length; i++) {
            if (currPermMatrix[srcMod][i])
                return i;
        }
        return currPermMatrix.length;
    }
}
