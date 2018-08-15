import static org.epochx.stats.StatField.*;
import org.epochx.gp.model.*;
import org.epochx.gp.op.init.*;
import org.epochx.gp.op.crossover.*;
import org.epochx.gp.op.mutation.*;
import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.life.*;
import org.epochx.op.Initialiser;
import org.epochx.op.selection.TournamentSelector;
import org.epochx.representation.CandidateProgram;
import org.epochx.stats.StatField;
import org.epochx.stats.Stats;
import org.epochx.tools.random.MersenneTwisterFast;
import org.epochx.core.ModifiedRunManager;
import org.epochx.core.RunManager;
import org.epochx.epox.Node;
import org.epochx.epox.lang.*;
import java.io.FileWriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class RobotGP extends GPModel{
	// Control parameters.
	private List<Node> syntax;
	private int maxInitialDepth;
	private int maxProgramDepth;
	
	private ModifiedRunManager run;
	
	private RobotsAndGraph robotsAndGraph;
	private RobotsAndGraphList robotsAndGraphList;
	private RobotsAndGraphList testExamples;
	private TrainingAndTestSet trainingAndTestSet;
	
	public int[] timeStepsArr;
	public int[] testTimeStepsArr;
	public int numRuns;
	public int numGraphs;
	public int timeStepSum;
	public int testTimeStepSum;
	boolean bestTimeChange;
	boolean successfulRun;
	boolean testSuccessfulRun;
	boolean testBestTimeChange;
	int numTest;
	
	public GraphPanel gp;
	
	public FullInitialiser robotInitialiser;
	
	public RobotGP(){
		// Bounding the possible depth of the solution algorithm decision tree
		maxInitialDepth = 2;
		maxProgramDepth = 50;
		
		successfulRun = false;
		
		// numGraphs is the number of graphs the program trains on
		// numRuns is the number of runs over which the program trains on the numGraphs training examples
		numGraphs =  1;
		numRuns = 3;
		
		// These values help control how large the training graphs are. The larger they are, the larger the training graphs
		// These values are essentially the number of times the graph recursively grows
		int trainingBaseSeed = 4;
		int trainingVariableSeed = 7;
		
		// Making the training set
		this.robotsAndGraphList = new RobotsAndGraphList(numGraphs, trainingBaseSeed, trainingVariableSeed);
		//this.robotsAndGraphList.hardCode();
		
		// Keeps track of the number of time steps it takes to solve each training example using the current best algorithm
		timeStepsArr = new int[numGraphs];
		
		bestTimeChange = false;
		
		// Sum of all the time steps needed to solve each training example
		timeStepSum = 0;
		System.out.println("ORIGINAL TIMES");
		
		// Calculating initial upper bound on the number of time steps allowed to solve each training example
		// Current formula is (# nodes in graph)^2 * (# robots)^2
		for(int i = 0; i < numGraphs; i++){
			this.robotsAndGraph = this.robotsAndGraphList.list[i];
			timeStepsArr[i] = this.robotsAndGraph.nodeL.size()*this.robotsAndGraph.nodeL.size()*this.robotsAndGraph.robotList.size()*this.robotsAndGraph.robotList.size();
			System.out.println("GRAPH " + i + " TIME: " + timeStepsArr[i]);
			timeStepSum += timeStepsArr[i];
		}
		
		/*
		// Making the test set
		
		// Giving test set smaller seed values than training set seed values so that the model trains on more complex examples.
		// This creates a greater likelihood of learning an algorithm that can solve the easier test examples
		int testBaseSeed = 4;
		int testVariableSeed = 6;
		
		// Number of test examples
		this.numTest = 100;
		this.testExamples = new RobotsAndGraphList(this.numTest, testBaseSeed, testVariableSeed);
		
		this.testTimeStepsArr = new int[this.numTest];
		
		// Calculating initial upper bound on the number of time steps allowed to solve each test example
		for(int i = 0; i < this.numTest; i++) {
			this.robotsAndGraph = this.testExamples.list[i];
			testTimeStepsArr[i] = this.robotsAndGraph.nodeL.size()*this.robotsAndGraph.nodeL.size()*this.robotsAndGraph.robotList.size()*this.robotsAndGraph.robotList.size();
		}
		*/
		
		// Object that allows us to store and separately handle the training set and test set of graphs
		this.trainingAndTestSet = new TrainingAndTestSet(this.robotsAndGraphList, this.testExamples);
		
		robotInitialiser = new FullInitialiser(this);
		
		// Operators.
		setInitialiser(robotInitialiser);
		setCrossover(new SubtreeCrossover(this));
		setMutation(new SubtreeMutation(this));
		
		// The set of functions and terminals that can be used to build the solution algorithm
		final List<Node> syntax = new ArrayList<Node>();
		//syntax.add(new IfTwoRobotsOnEachOthersPath(this.robotsAndGraphList));
		syntax.add(new IfTwoBotsOnEachOthersPath(this.trainingAndTestSet));
		syntax.add(new IfNeighborIsSurrounded(this.trainingAndTestSet)); // works
		syntax.add(new IfRobotAtBranch(this.trainingAndTestSet)); // works
		syntax.add(new IfRobotAtDestination(this.trainingAndTestSet)); // works
		syntax.add(new IfRobotMovingToBranch(this.trainingAndTestSet));
		//syntax.add(new ModifiedSeqNFunction(2,this.robotsAndGraph));
		//syntax.add(new ModifiedSeqNFunction(3,this.robotsAndGraph));
		syntax.add(new MoveTowardBranchFunction(this.trainingAndTestSet)); // works
		// syntax.add(new MoveToUnvisitedNeighborFunction(this.robotsAndGraphList));
		syntax.add(new MoveToFreeNeighborFunction(this.trainingAndTestSet)); // works
		syntax.add(new MoveTowardObjectiveFunction(this.trainingAndTestSet)); // works
		syntax.add(new StayFunction(this.trainingAndTestSet)); // works
		syntax.add(new IfNeighborOnPathIsFree(this.trainingAndTestSet)); // works
		syntax.add(new IfRobotIsSolved(this.trainingAndTestSet)); // works
		syntax.add(new IfOnPathOfBotInNetwork(this.trainingAndTestSet)); // works
		syntax.add(new IfRobotInNetworkMovingToBranch(this.trainingAndTestSet));
		
		setSyntax(syntax);
	}
	
	public void run() {
		run = new ModifiedRunManager(this);
		
		GPCandidateProgram bestProgram = null;
		double bestFitness = 10000000.0;
		
		boolean nonConvergence = false;
		boolean notSolved = true;
		
		// Iterating through each run
		for (int j = 0; j < numRuns; j++) {
			bestTimeChange = false;
			System.out.println("RUN: " + j);
			run.bestFitness = Double.MAX_VALUE;
			// Execute the run
			run.run(j);
			
			// Get the best program from the run
			GPCandidateProgram currentProgram = (GPCandidateProgram) run.getBestProgram();
			
			// Printing out statistics of the training runs
			//System.out.println("BEST PROGRAM IN RUN:");
			//System.out.println(currentProgram.toString());
			double currentFitness = run.getBestFitness();
			
			//System.out.println("BEST TIMES:");
			for(int i = 0; i < this.robotsAndGraphList.list.length; i++){
				//System.out.println("GRAPH " + i + " TIME: " + timeStepsArr[i]);
			}
			//System.out.println("TIME STEP SUM: " + timeStepSum);
			
			if(currentFitness == 0.0 && bestTimeChange){
				bestFitness = currentFitness;
				bestProgram = currentProgram;
				notSolved = false;
				nonConvergence = false;
			}
			else if(currentFitness < bestFitness){
				if(notSolved){
					bestFitness = currentFitness;
					bestProgram = currentProgram;
					//nonConvergence = false;
				}
				/*
				else if(nonConvergence){
					break;
				}
				else{
					nonConvergence = true;
				}
				*/
			}
			else{
				/*
				if(nonConvergence){
					break;
				}
				else{
					nonConvergence = true;
				}
				*/
			}
		}
		
		if(successfulRun) {
			//System.out.println("SUCCESSFUL RUN OCCURED");
		}
		else {
			//System.out.println("NO SUCCESSFUL RUNS");
		}
		
		// setGraphPanelProgram((GPCandidateProgram) run.getBestProgramOverAllRuns());
		
		// runAgainstTestSet();
		
		// displayTestExamples((GPCandidateProgram) run.getBestProgramOverAllRuns());
	}
	
	public int getTimeStepSum() {
		return timeStepSum;
	}
	
	public int getMaxInitialDepth() {
		return maxInitialDepth;
	}
	
	public void setMaxInitialDepth(final int maxInitialDepth) {
		if (maxInitialDepth >= -1) {
			this.maxInitialDepth = maxInitialDepth;
		} 
		else {
			throw new IllegalArgumentException("maxInitialDepth must be -1 or greater");
		}

		assert (this.maxInitialDepth >= -1);
	}
	
	public int getMaxDepth() {
		return maxProgramDepth;
	}
	
	public void setMaxDepth(final int maxDepth) {
		if (maxDepth >= -1) {
			maxProgramDepth = maxDepth;
		} else {
			throw new IllegalArgumentException("maxProgramDepth must be -1 or greater");
		}

		assert (maxProgramDepth >= -1);
	}
	
	public List<Node> getSyntax() {
		return syntax;
	}
	
	public void setSyntax(final List<Node> syntax) {
		if (syntax != null) {
			this.syntax = syntax;
		} else {
			throw new IllegalArgumentException("syntax must not be null");
		}

		assert (this.syntax != null);
	}
	
	@Override
	public double getFitness(final CandidateProgram p) {
		//System.out.println("NEW PROGRAM");
		final GPCandidateProgram program = (GPCandidateProgram) p;
		//System.out.println(program.toString());
		int globalFitness = 0;
		this.robotsAndGraphList.currExample = 0;
		
		// tempTimeSteps tracks the number of time steps the current program needs to solve each training example
		int tempTimeSteps[] = new int[this.robotsAndGraphList.numExamples];
		for(int i = 0; i < this.robotsAndGraphList.numExamples; i++){
			tempTimeSteps[i] = 0;
		}
		
		// this.robotsAndGraph.startingFormation();
		// Iterating through each training example and running 'program' against it
		for(int j = 0; j < this.robotsAndGraphList.numExamples; j++){
			boolean trainingExampleSolved = false;
			this.robotsAndGraph = this.robotsAndGraphList.list[j];
			// Resetting the example and putting robots in starting formation
			this.robotsAndGraph.startingFormation();
			//this.robotsAndGraph.startingFormation1();
			
			// Array that keeps track of which robots have been solved in the current example
			boolean[] solvedArr = new boolean[this.robotsAndGraph.robotList.size()];
			
			// Initializing this array
			for(int a = 0; a < solvedArr.length; a++) {
				solvedArr[a] = false;
			}
			
			// Iterating through time steps for a given training example until the maximum number of time steps has been reached or the example has been solved
			while (this.robotsAndGraph.currentTimeStep < timeStepsArr[j]) {
				// Iterating through each robot in the training example in a given time step
				for(int i = 0; i < this.robotsAndGraph.robotList.size(); i++){
					this.robotsAndGraph.currentRobot = i;
					//System.out.println("CURRENT ROBOT: " + this.robotsAndGraph.robotList.get(i).objective.value + " ON NODE: " + this.robotsAndGraph.robotList.get(i).currNode.value);
					// Running the current program on a given robot to determine its next move
					program.evaluate();
				}
				
				this.robotsAndGraph.currentTimeStep++;
				tempTimeSteps[j] = this.robotsAndGraph.currentTimeStep;
				
				// Checking to see if robots within the example have been solved (i.e have they reached their destination node)
				for(int k = 0; k < this.robotsAndGraph.robotList.size(); k++){
					if(this.robotsAndGraph.robotList.get(k).currNode == this.robotsAndGraph.robotList.get(k).objective) {
						this.robotsAndGraph.robotList.get(k).gpSolved = true;
						solvedArr[k] = true;
					}
					else {
						solvedArr[k] = this.robotsAndGraph.robotList.get(k).gpSolved;
					}
				}
				
				boolean allSolved = true;
				
				// Checking to see if all robots within the example have been solved. If so, then the training example has been solved
				for(int l = 0; l < this.robotsAndGraph.robotList.size(); l++){
					if(solvedArr[l] == false){
						allSolved = false;
					}
				}
				
				if(allSolved){
					trainingExampleSolved = true;
					break;
				}
			}
			
			int finalDistance;
			
			if(trainingExampleSolved) {
				// When an example is solved, not all robots may be at their objective node. 
				// They may have reached their objective node earlier and been displaced since
				// This is why we don't calculate distance when an example is solved. We just set the fitness to 0
				finalDistance = 0;
			}
			else {
				//System.out.println("FINAL ROBOT POSITIONS");
				finalDistance = 0;
				for(int i = 0; i < this.robotsAndGraph.robotList.size(); i++){
					Robot bot = this.robotsAndGraph.robotList.get(i);
					//System.out.println("ROBOT " + bot.objective.value + " ON NODE: " + bot.currNode.value);
					int distance = (bot.adjList.Dijkstra(bot.currNode, bot.objective).size() - 1);
					// Fitness function is distance^2
					finalDistance += distance*distance;
				}
			}
			
			
			globalFitness += finalDistance;
			// globalFitness += finalDistance*(finalDistance + totalStepsAfterSolved);
			//System.out.println("SUB FITNESS: "  + finalDistance);
			//System.out.println("TOTAL MOVES: "  + totalMoves);
			//System.out.println("TOTAL STEPS AFTER SOLVED: "  + totalStepsAfterSolved);
			//System.out.println("TIME STEPS: " + this.robotsAndGraph.currentTimeStep);
			this.robotsAndGraphList.currExample++;
		}
		
		// The current program solves the training examples
		if(globalFitness == 0){
			successfulRun = true;
			int timeSum = 0;
			// Finding the sum of the time steps needed to solve each of the training examples for the given program
			for(int k = 0; k < this.robotsAndGraphList.numExamples; k++){
				timeSum += tempTimeSteps[k];
			}
			// This means that the current program performs better than the previous best program
			if(timeSum < timeStepSum){
				timeStepsArr = tempTimeSteps;
				timeStepSum = timeSum;
				bestTimeChange = true;
				run.setBestProgram(program);
				run.setBestProgramOverAllRuns(program);
				System.out.println("SOLVED");
				System.out.println("PROGRAM:");
				System.out.println(p.toString());
				System.out.println("TIME: " + this.robotsAndGraph.currentTimeStep);
				System.out.println();
			}
		}
		
		return globalFitness;
	}
	
	public void getTestTimes(GPCandidateProgram program) {
		// Setting this.robotsAndGraphList to robotsAndGraphList set in TrainingAndTestSet
		if(this.trainingAndTestSet.testBool) {
			System.out.println("MADE SWITCH TO TEST");
		}
		this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
		System.out.println("NUM IN TEST SET: " + this.trainingAndTestSet.robotsAndGraphList.numExamples);
		this.robotsAndGraphList.currExample = 0;
		
		int numSuccesses = 0;
		
		// Iterating through the test set of examples
		for(int j = 0; j < this.robotsAndGraphList.numExamples; j++){
			System.out.println("Graph " + j + ":");
			this.robotsAndGraph = this.robotsAndGraphList.list[j];
			// Resetting and putting in starting formation
			this.robotsAndGraph.startingFormation();
			//this.robotsAndGraph.startingFormation1();
			
			System.out.println("Num Nodes: " + this.robotsAndGraph.nodeL.size());
			System.out.println("Num Robots: " + this.robotsAndGraph.robotList.size());
			
			// Array that keeps track of which robots have been solved in the test example
			boolean[] solvedArr = new boolean[this.robotsAndGraph.robotList.size()];
			
			// Initializing this array
			for(int a = 0; a < solvedArr.length; a++) {
				solvedArr[a] = false;
			}
			
			boolean allSolved = true;
			
			// Running the algorithm on the test example time step by time step
			while (this.robotsAndGraph.currentTimeStep < testTimeStepsArr[j]) {
				// Executing the algorithm on each robot one at a time each time step
				for(int i = 0; i < this.robotsAndGraph.robotList.size(); i++){
					this.robotsAndGraph.currentRobot = i;
					//System.out.println("CURRENT ROBOT: " + this.robotsAndGraph.robotList.get(i).objective.value + " ON NODE: " + this.robotsAndGraph.robotList.get(i).currNode.value);
					program.evaluate();
				}
				
				this.robotsAndGraph.currentTimeStep++;
				
				// Going through each robot, checking to see if it has been solved (If it's at its objective node)
				for(int k = 0; k < this.robotsAndGraph.robotList.size(); k++){
					if(this.robotsAndGraph.robotList.get(k).currNode == this.robotsAndGraph.robotList.get(k).objective) {
						this.robotsAndGraph.robotList.get(k).gpSolved = true;
						solvedArr[k] = true;
					}
					else {
						solvedArr[k] = this.robotsAndGraph.robotList.get(k).gpSolved;
					}
				}
				
				allSolved = true;
				
				for(int l = 0; l < this.robotsAndGraph.robotList.size(); l++){
					if(solvedArr[l] == false){
						allSolved = false;
					}
				}
				
				// Test example has been solved by the learned algorithm
				if(allSolved){
					System.out.println("Example Solved");
					this.robotsAndGraph.gpSolvedTime = String.valueOf(this.robotsAndGraph.currentTimeStep);
					numSuccesses += 1;
					break;
				}
			}
			
			if (!allSolved) {
				System.out.println("Example NOT Solved");
			}
			
			// globalFitness += finalDistance*(finalDistance + totalStepsAfterSolved);
			//System.out.println("SUB FITNESS: "  + finalDistance);
			//System.out.println("TOTAL MOVES: "  + totalMoves);
			//System.out.println("TOTAL STEPS AFTER SOLVED: "  + totalStepsAfterSolved);
			System.out.println("TIME STEPS: " + this.robotsAndGraph.currentTimeStep);
			this.robotsAndGraphList.currExample++;
		}
		
		System.out.println(numSuccesses + "/" + this.robotsAndGraphList.numExamples + " Solved");
	}
	
	public void getPSWTestSetTimes() {
		if(this.trainingAndTestSet.testBool) {
			System.out.println("STILL IN TEST SET FOR PSW");
		}
		
		FileWriter writer = null;
		
		try {
			writer = new FileWriter("results.csv");
			writer.append("# Nodes, # Robots, GP Time, PSW Time, # Leaves, # Branch Nodes, Avg Destination Distance, Total Swaps, # Internal Nodes\n");
			
			this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
			
			int numSuccesses = 0;
			
			// Upper bound number of time steps we'll allow PSW to run for for each test example 
			int[] upperBounds = new int[this.robotsAndGraphList.numExamples];
			for(int i = 0; i < upperBounds.length; i++) {
				upperBounds[i] = this.robotsAndGraphList.list[i].nodeL.size()*this.robotsAndGraphList.list[i].nodeL.size()*this.robotsAndGraphList.list[i].robotList.size()*this.robotsAndGraphList.list[i].robotList.size();
			}
			
			// Iterating through the test set of examples
			for(int j = 0; j < this.robotsAndGraphList.numExamples; j++){
				this.robotsAndGraphList.currExample = j;
				this.robotsAndGraph = this.robotsAndGraphList.list[j];
				
				// Resetting and putting in starting formation
				this.robotsAndGraph.startingFormation();
				
				int distSum = 0;
				
				for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++) {
					distSum += this.robotsAndGraph.robotList.get(a).path.size();
				}
				
				float avgDist = (float) distSum / this.robotsAndGraph.robotList.size();
				
				int numBranches = 0;
				
				int totalSwaps = 0;
				
				// Get number of branches
				for(int a = 0; a < this.robotsAndGraph.nodeL.size(); a++) {
					if(this.robotsAndGraph.nodeL.get(a).branch) {
						numBranches++;
					}
				}
				
				boolean exampleSolved = false;
				
				// Iterate through allowable number of time steps
				for(int k = 0; k < upperBounds[j]; k++) {
					// Check if example has been solved yet
					boolean allSolved = true;
		        	
		        	for(int i = 0; i < this.robotsAndGraph.robotList.size(); i++){
		        		if(!this.robotsAndGraph.robotList.get(i).solved){
		        			allSolved = false;
		        		}
		        	}
		        	
		        	if(allSolved){
		        		for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++) {
		        			totalSwaps += this.robotsAndGraph.robotList.get(a).numSwaps;
		        		}
		        		
		        		System.out.println("PSW Solved Graph: " + j + " in " + k + " time steps");
		        		System.out.println("Num Leaves: " + this.robotsAndGraph.numLeaves);
		        		System.out.println("Num Branches: " + numBranches);
		        		System.out.println("Avg Distance: " + avgDist);
		        		this.robotsAndGraph.pswSolvedTime = String.valueOf(k);
		        		System.out.println("Total Swaps: " + totalSwaps);
		        		
		        		writer.append(this.robotsAndGraph.nodeL.size() + "," + this.robotsAndGraph.robotList.size() + "," + this.robotsAndGraph.gpSolvedTime + "," + this.robotsAndGraph.pswSolvedTime + "," + this.robotsAndGraph.numLeaves + "," + numBranches + "," + avgDist + "," + totalSwaps + "\n");
		        		
		        		numSuccesses++;
		        		exampleSolved = true;
		        		break;
		        	}
					
		        	// Could be a problem with our adjlist
		        	for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++){
		        		this.robotsAndGraph.robotList.get(a).adjList = this.robotsAndGraph.adjList;
		        		this.robotsAndGraph.robotList.get(a).directNetwork = this.robotsAndGraph.adjList.directCommunication(this.robotsAndGraph.robotList.get(a).currNode, 2);
		        	}
		        	
		        	Robot highestPriority = null;
		    		// Get highest-priority unsolved robot
		    		for(int b = 0; b < this.robotsAndGraph.robotList.size(); b++){
		    			/*
		    			if(k == 0){
		    				this.robotsAndGraph.robotList.get(b).directNetwork = this.robotsAndGraph.adjList.directCommunication(this.robotsAndGraph.robotList.get(b).currNode, 2);
		    			}
		    			*/
		    			
		    			if(highestPriority == null && !this.robotsAndGraph.robotList.get(b).solved){
		    				highestPriority = this.robotsAndGraph.robotList.get(b);
		    			}
		    			else if(highestPriority == null){
		    				continue;
		    			}
		    			else if((this.robotsAndGraph.robotList.get(b).priority < highestPriority.priority) && (!this.robotsAndGraph.robotList.get(b).solved)){
		    				highestPriority = this.robotsAndGraph.robotList.get(b);
		    			}
		    		}
					
		    		// Calling plan() function on each robot to run PSW on each robot at each time step
					for(int l = 0; l < this.robotsAndGraph.robotList.size(); l++) {
						this.robotsAndGraph.robotList.get(l).plan(highestPriority);
					}
				}
				
				if(!exampleSolved) {
					System.out.println("Graph: " + j + " was not solved by PSW");
					System.out.println("Num Leaves: " + this.robotsAndGraph.numLeaves);
	        		System.out.println("Num Branches: " + numBranches);
	        		System.out.println("Avg Distance: " + avgDist);
	        		
	        		writer.append(this.robotsAndGraph.nodeL.size() + "," + this.robotsAndGraph.robotList.size() + "," + this.robotsAndGraph.gpSolvedTime + "," + this.robotsAndGraph.pswSolvedTime + "," + this.robotsAndGraph.numLeaves + "," + numBranches + "," + avgDist + "," + totalSwaps + "\n");
	        		
	        		for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++) {
	        			totalSwaps += this.robotsAndGraph.robotList.get(a).numSwaps;
	        		}
	        		
	        		System.out.println("Total Swaps: " + totalSwaps);
				}
			}
			
			System.out.println("PSW solved: " + numSuccesses + " examples");
			
			writer.close();
		}
		catch(Exception ex) {
			
		}
	}
	
	public String getPSWIndividualTime() {
		System.out.println("Get PSW Time");
		String csvString = "";
		
		// Upper bound on number of time steps we'll allow PSW to run on the example 
		int[] upperBounds = new int[this.robotsAndGraphList.numExamples];
		for(int i = 0; i < upperBounds.length; i++) {
			upperBounds[i] = this.robotsAndGraphList.list[i].nodeL.size()*this.robotsAndGraphList.list[i].nodeL.size()*this.robotsAndGraphList.list[i].robotList.size()*this.robotsAndGraphList.list[i].robotList.size();
		}
		
		System.out.println("iterate");
		// Iterating through the single example
		for(int j = 0; j < this.robotsAndGraphList.numExamples; j++){
			this.robotsAndGraphList.currExample = j;
			this.robotsAndGraph = this.robotsAndGraphList.list[j];
			
			// Resetting and putting in starting formation
			this.robotsAndGraph.startingFormation();
			
			int distSum = 0;
			
			for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++) {
				distSum += this.robotsAndGraph.robotList.get(a).path.size();
			}
			
			float avgDist = (float) distSum / this.robotsAndGraph.robotList.size();
			
			int numBranches = 0;
			
			int totalSwaps = 0;
			
			// Get number of branches
			for(int a = 0; a < this.robotsAndGraph.nodeL.size(); a++) {
				if(this.robotsAndGraph.nodeL.get(a).branch) {
					numBranches++;
				}
			}
			
			boolean exampleSolved = false;
			
			// Iterate through allowable number of time steps
			for(int k = 0; k < upperBounds[j]; k++) {
				// Check if example has been solved yet
				boolean allSolved = true;
	        	
	        	for(int i = 0; i < this.robotsAndGraph.robotList.size(); i++){
	        		if(!this.robotsAndGraph.robotList.get(i).solved){
	        			allSolved = false;
	        		}
	        	}
	        	
	        	if(allSolved){
	        		for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++) {
	        			totalSwaps += this.robotsAndGraph.robotList.get(a).numSwaps;
	        		}
	        		
	        		System.out.println("All solved");
	        		
	        		this.robotsAndGraph.pswSolvedTime = String.valueOf(k);
	        		
	        		csvString = this.robotsAndGraph.nodeL.size() + "," + this.robotsAndGraph.robotList.size() + "," + timeStepSum + "," + this.robotsAndGraph.pswSolvedTime + "," + this.robotsAndGraph.numLeaves + "," + numBranches + "," + avgDist + "," + totalSwaps + "\n";
	        		
	        		exampleSolved = true;
	        		break;
	        	}
				
	        	for(int a = 0; a < this.robotsAndGraph.robotList.size(); a++){
	        		this.robotsAndGraph.robotList.get(a).adjList = this.robotsAndGraph.adjList;
	        		this.robotsAndGraph.robotList.get(a).directNetwork = this.robotsAndGraph.adjList.directCommunication(this.robotsAndGraph.robotList.get(a).currNode, 2);
	        	}
	        	
	        	Robot highestPriority = null;
	    		// Get highest-priority unsolved robot
	    		for(int b = 0; b < this.robotsAndGraph.robotList.size(); b++){
	    			/*
	    			if(k == 0){
	    				this.robotsAndGraph.robotList.get(b).directNetwork = this.robotsAndGraph.adjList.directCommunication(this.robotsAndGraph.robotList.get(b).currNode, 2);
	    			}
	    			*/
	    			
	    			if(highestPriority == null && !this.robotsAndGraph.robotList.get(b).solved){
	    				highestPriority = this.robotsAndGraph.robotList.get(b);
	    			}
	    			else if(highestPriority == null){
	    				continue;
	    			}
	    			else if((this.robotsAndGraph.robotList.get(b).priority < highestPriority.priority) && (!this.robotsAndGraph.robotList.get(b).solved)){
	    				highestPriority = this.robotsAndGraph.robotList.get(b);
	    			}
	    		}
				
	    		// Calling plan() function on each robot to run PSW on each robot at each time step
				for(int l = 0; l < this.robotsAndGraph.robotList.size(); l++) {
					this.robotsAndGraph.robotList.get(l).plan(highestPriority);
				}
			}
			
			if(!exampleSolved) {
				System.out.println("Graph: " + j + " was not solved by PSW");
        		
        		csvString = this.robotsAndGraph.nodeL.size() + "," + this.robotsAndGraph.robotList.size() + "," + timeStepSum + "," + this.robotsAndGraph.pswSolvedTime + "," + this.robotsAndGraph.numLeaves + "," + numBranches + "," + avgDist + "," + totalSwaps + "\n";
			}
		}
		System.out.println("return csv");
		return csvString;
	}
	
	public void setGraphPanelProgram(GPCandidateProgram bestProgram){
		if(bestProgram != null) {
			System.out.println("BEST PROGRAM");
	        System.out.println(bestProgram.toString());
	        System.out.println("DEPTH: " + bestProgram.getProgramDepth());
		}
        
        for(int i = 0; i < this.robotsAndGraphList.list.length; i++){
        	this.robotsAndGraphList.currExample = i;
    		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
    		this.robotsAndGraph.runBestProgramOnGUI(bestProgram, timeStepsArr[i]);
        }
	}
	
	public void displayTestExamples(GPCandidateProgram bestProgram){
		JFrame f = new JFrame("GraphPanel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gp = new GraphPanel();
        f.add(this.gp.control, BorderLayout.NORTH);
        f.add(new JScrollPane(this.gp), BorderLayout.CENTER);
        f.pack();
        f.setLocationByPlatform(true);
        f.setVisible(true);
        
        this.robotsAndGraphList.currExample = 0;
        this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
        
        // Setting maxTimeSteps here before currentTimeStep is set to 0 in startingFormation()
        this.gp.maxTimeSteps = this.robotsAndGraph.currentTimeStep;
        this.robotsAndGraph.startingFormation();
        
        this.gp.nodes = this.robotsAndGraph.nodeL;
        this.gp.edges = this.robotsAndGraph.edgeL;
        this.gp.robotL = this.robotsAndGraph.robotList;
        this.gp.robotsAndGraph = this.robotsAndGraph;
        this.gp.robotsAndGraph.bestProgram = bestProgram;
        this.gp.bestProgram = bestProgram;
        this.gp.robotsAndGraphList = this.robotsAndGraphList;
        this.gp.solvedArr = new boolean[this.robotsAndGraph.robotList.size()];
        this.gp.adjList = this.robotsAndGraph.adjList;
        
        for(int a = 0; a < this.gp.solvedArr.length; a++) {
			this.gp.solvedArr[a] = false;
		}
        
        this.gp.repaint();
	}
	
	public void displaySingleExample(){
		JFrame f = new JFrame("GraphPanel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gp = new GraphPanel();
        f.add(this.gp.control, BorderLayout.NORTH);
        f.add(new JScrollPane(this.gp), BorderLayout.CENTER);
        f.pack();
        f.setLocationByPlatform(true);
        f.setVisible(true);
        
        GPCandidateProgram bestProgram = (GPCandidateProgram) run.getBestProgramOverAllRuns();
        
        this.robotsAndGraphList.currExample = 0;
        this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
        
        // Setting maxTimeSteps here before currentTimeStep is set to 0 in startingFormation()
        this.gp.maxTimeSteps = this.robotsAndGraph.currentTimeStep;
        this.robotsAndGraph.startingFormation();
        
        this.gp.nodes = this.robotsAndGraph.nodeL;
        this.gp.edges = this.robotsAndGraph.edgeL;
        this.gp.robotL = this.robotsAndGraph.robotList;
        this.gp.robotsAndGraph = this.robotsAndGraph;
        this.gp.robotsAndGraph.bestProgram = bestProgram;
        this.gp.bestProgram = bestProgram;
        this.gp.robotsAndGraphList = this.robotsAndGraphList;
        this.gp.solvedArr = new boolean[this.robotsAndGraph.robotList.size()];
        this.gp.adjList = this.robotsAndGraph.adjList;
        
        for(int a = 0; a < this.gp.solvedArr.length; a++) {
			this.gp.solvedArr[a] = false;
		}
        
        this.gp.repaint();
	}
	
	public void runAgainstTestSet() {
		this.trainingAndTestSet.switchToTest();
		
		GPCandidateProgram bestProgram = (GPCandidateProgram) run.getBestProgramOverAllRuns();
		
		getTestTimes(bestProgram);
		
		System.out.println("PSW Test Set Times:");
		
		getPSWTestSetTimes();
		
		displayTestExamples(bestProgram);
	}
	
	@Override
	public Class<?> getReturnType() {
		return Void.class;
	}
}
