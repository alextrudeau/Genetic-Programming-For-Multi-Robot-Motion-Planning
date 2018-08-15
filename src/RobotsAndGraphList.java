import java.util.Random;

public class RobotsAndGraphList {

	public RobotsAndGraph[] list;
	public int currExample;
	public int numExamples;
	
	public RobotsAndGraphList(int numExamples, int baseSeed, int variableSeed){
		this.currExample = 0;
		this.numExamples = numExamples;
		this.list = new RobotsAndGraph[numExamples];
		Random rand = new Random();
		System.out.println("NUM EXAMPLES: " + numExamples);
		for(int i = 0; i < numExamples; i++){
			RobotsAndGraph newRobotsAndGraph = new RobotsAndGraph();
			newRobotsAndGraph.createRandomGraph(baseSeed + rand.nextInt(variableSeed));
			if(newRobotsAndGraph.robotList.size() > 1){
				this.list[i] = newRobotsAndGraph;
			}
			else{
				System.out.println("UNACCEPTABLE GRAPH");
				i--;
			}
		}
	}
	
}
