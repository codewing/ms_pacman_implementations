package pacman.entries.pacman.wiba.ga;

import pacman.entries.pacman.wiba.mcts.MCTSParams;
import pacman.game.util.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class GAStorage {

    private static final String header = "Fitness;MAX_PATH_LENGTH;explorationCoefficient;ghostSimulationTimeMS;MIN_VISIT_COUNT";
    private static final String genomeCSVFormat = "%f;%d;%f;%d;%d\n";

    public static void saveGenomeCSV(String filename, ArrayList<Genome> genomes) {
        StringBuilder sb = new StringBuilder();
        sb.append(header);

        Collections.sort(genomes, Comparator.comparingDouble(Genome::getFitness));

        for(Genome genome : genomes) {
            sb.append(toCSVString(genome));
        }

        IO.saveFile(filename, sb.toString(), false);
    }

    public static ArrayList<Genome> loadGenomeCSV (String filename) {
        String fileContent = IO.loadFile(filename);
        ArrayList<String> parsedContent = new ArrayList<>(Arrays.asList(fileContent.split("\n")));
        parsedContent.remove(0); // removes the header

        ArrayList<Genome> genomes = new ArrayList<>();
        for(String line : parsedContent) {
            genomes.add(fromCSVString(line));
        }

        return genomes;
    }

    private static String toCSVString(Genome genome) {
        MCTSParams chromosome = genome.getChromosome();

        return String.format(genomeCSVFormat,
                                genome.getFitness(),
                                chromosome.MAX_PATH_LENGTH,
                                chromosome.explorationCoefficient,
                                chromosome.ghostSimulationTimeMS,
                                chromosome.MIN_VISIT_COUNT);
    }

    private static Genome fromCSVString(String csvLine) {
        String[] split = csvLine.split(";");

        int maxPathLength = Integer.parseInt(split[1]);
        double explorationCoefficient = Double.parseDouble(split[2].replaceAll(",","."));
        int ghostSimulationTimeMS = Integer.parseInt(split[3]);
        int minVisitCount = Integer.parseInt(split[4]);

        MCTSParams params = new MCTSParams(maxPathLength, explorationCoefficient, minVisitCount, ghostSimulationTimeMS);

        Genome g = new Genome(params);
        g.setFitness(Float.parseFloat(split[0].replaceAll(",",".")));

        return g;
    }
}
