package computation.algorithm;

import computation.ComputationDispatcher;
import computation.algorithm.conditions.ConditionManager;
import computation.graphElements.*;
import computation.graphElements.Vector;
import computation.graphElements.segments.Segment;
import computation.graphElements.segments.SegmentSoul;
import computation.utils.ReferenceRotor;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class AlgorithmInitExecutor implements Runnable{

    private List<SegmentSoul> shape;
    private Node startNode;
    private ConditionManager conditionManager;
    private int graphKey;
    private Logger log;


    public AlgorithmInitExecutor(List<SegmentSoul> shape, Node startNode, ConditionManager conditionManager, int graphKey){
        this.shape = shape;
        this.startNode = startNode;
        this.conditionManager = conditionManager;
        this.graphKey = graphKey;
        this.log = Logger.getLogger(this.toString() + this.hashCode());
    }

    @Override
    public void run() {
        ReferenceRotor referenceRotor = new ReferenceRotor();
        ExecutorService executorService = ComputationDispatcher.executorService;
        Map<Node,List<Segment>> foundSegments = new HashMap<>();
        Graph graph = ComputationDispatcher.getGraph(graphKey);

        if(shape.size() == 0){
            log.info("Null will be thrown");
        }
        AlgorithmExecutionResult algorithmResult = new AlgorithmExecutionResult(startNode);
        ComputationDispatcher.addNewAlgorithmResult(startNode, algorithmResult);

        List<Segment> initialSegmentsFromMap = graph.getSegmentsForNode(startNode);
        initialSegmentsFromMap.forEach(segment -> {
            Vector shapeVector = shape.get(0).getVector1();
            this.shape = referenceRotor.rotateShapeToFit(shape, segment.getVectorFromNode(startNode), shapeVector );
            EndNodesPredictor endNodesPredictor = new EndNodesPredictor(graph,conditionManager);
            Map<Node, List<Segment>> potentialNodes = endNodesPredictor.getNodes(startNode,segment,shape.get(0), shapeVector);

            if(!potentialNodes.isEmpty()) {

                potentialNodes.entrySet().forEach(entry -> {
                    if(!foundSegments.containsKey(entry.getKey()) || foundSegments.containsKey(entry.getKey()) && foundSegments.get(entry.getKey()).size() > entry.getValue().size())
                        foundSegments.put(entry.getKey(), entry.getValue());
                    });

                if (!shape.subList(1, shape.size()).isEmpty())
                    potentialNodes.keySet().forEach(n -> {
                        List<SegmentSoul> tmp = referenceRotor.rotateShapeToFit(shape, new Vector(startNode, n), shapeVector);
                        executorService.submit(new AlgorithmExecutor(new LinkedList<>(tmp.subList(1, shape.size())), n, conditionManager, graphKey, algorithmResult));
                    });
            }
        });

        if(!foundSegments.isEmpty()){
            algorithmResult.setPathsToEndNodes(foundSegments);
        }
    }
}