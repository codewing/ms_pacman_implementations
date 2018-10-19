package pacman.entries.pacman.wiba.ga;

public abstract class GAExecutor
{

    public static void main(String[] args)
    {
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
        geneticAlgorithm.startEvolution();

        System.out.println(geneticAlgorithm.getChampion().toString());

    }
}
