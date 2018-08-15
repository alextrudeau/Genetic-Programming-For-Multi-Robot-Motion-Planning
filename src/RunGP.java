import static org.epochx.stats.StatField.*;
import org.epochx.gp.model.*;
import org.epochx.gp.model.JohnMuirTrail;
import org.epochx.life.*;
import org.epochx.op.selection.TournamentSelector;
import org.epochx.representation.CandidateProgram;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import org.epochx.tools.random.MersenneTwisterFast;

import org.epochx.core.CrossoverManager;
import org.epochx.core.ElitismManager;
import org.epochx.core.MutationManager;
import org.epochx.core.PoolSelectionManager;
import org.epochx.core.ReproductionManager;
import org.epochx.core.RunManager;
import java.awt.*;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class RunGP {
	public static void main(String args[]){
		System.out.println("RUN GP");
		
		FileWriter writer = null;
		
		try {
			writer = new FileWriter("individualGPResults.csv");
			writer.append("# Nodes, # Robots, GP Time, PSW Time, # Leaves, # Branch Nodes, Avg Destination Distance, Total Swaps, # Internal Nodes\n");
			
			for(int i = 0; i < 150; i++) {
				System.out.println("Example: " + i);
				GPModel model = new RobotGP();
				model.setPopulationSize(2000);
				model.setNoGenerations(100);
				model.setCrossoverProbability(0.8);
				model.setMutationProbability(0.1);
				Life.get().addGenerationListener(new GenerationAdapter(){
					public void onGenerationEnd() {
						Stats.get().print(GEN_NUMBER,
								GEN_FITNESS_MIN,
								GEN_FITTEST_PROGRAM);
					}
				});
				model.run();
				RobotGP gpModel = (RobotGP) model;
				String csvEntry = gpModel.getPSWIndividualTime();
				writer.append(csvEntry);
				
				if(i == 149) {
					gpModel.displaySingleExample();
				}
			}
			writer.close();
		}
		catch(Exception ex) {
			
		}
	}
	
	public static Robot newRobot(AdjacencyList adjList, GraphPanel gp, int currNodeNum, int objectiveNodeNum){
		GraphNode objNode = adjList.nodeL[objectiveNodeNum];
    	GraphNode currNode = adjList.nodeL[currNodeNum];
    	Robot newBot = new Robot(currNode, objNode, gp.nodes.size());
    	newBot.path = adjList.Dijkstra(currNode, objNode);
    	newBot.path.remove(0);
        currNode.robot = newBot;
        newBot.priority = objNode.value;
        gp.robotL.add(newBot);
        currNode.robot = newBot;
        newBot.adjList = adjList;
        
        return newBot;
	}
}
