package computation.algorithm;

import computation.ComputationDispatcher;
import computation.algorithm.conditions.ConditionManager;
import computation.graphElements.Graph;
import computation.graphElements.Node;
import computation.graphElements.segments.Segment;
import computation.graphElements.segments.SegmentSoul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AlgorithmExecutor implements Runnable{

    private List<SegmentSoul> shape;
    private Node startNode;
    private ConditionManager conditionManager;
    private int graphKey;
    private Logger logger;
    private AlgorithmExecutionResult parentAlgorithmResult;


    public AlgorithmExecutor(List<SegmentSoul> shape, Node startNode, ConditionManager conditionManager, int graphKey, AlgorithmExecutionResult result){
        this.shape = shape;
        this.startNode = startNode;
        this.conditionManager = conditionManager;
        this.graphKey = graphKey;
        this.parentAlgorithmResult = result;
        this.logger = LoggerFactory.getLogger(AlgorithmExecutor.class);
    }

    @Override
    public void run() {
        ExecutorService executorService = ComputationDispatcher.executorService;
        Map<Node,List<Segment>> foundSegments = new HashMap<>();
        Graph graph = ComputationDispatcher.getGraph(graphKey);

        if(shape.size() == 0){
            logger.warn("Checking Empty Shape!");
            return;
        }

        AlgorithmExecutionResult currentAlgorithmResult = new AlgorithmExecutionResult(startNode);
        SegmentSoul segmentToMap = shape.remove(0);
        List<Segment> potentialSegments = graph.getSegmentsForNode(startNode);

        potentialSegments.forEach(segment -> {
            SegmentFinder segmentFinder = new SegmentFinder(graph, conditionManager);
            Map<Node, List<Segment>> potentialNodes = segmentFinder.getNodes(startNode,segment,segmentToMap, segmentToMap.getVector1());

            if(!potentialNodes.isEmpty()) {
                potentialNodes.entrySet().forEach(entry -> {
                    if (!foundSegments.containsKey(entry.getKey()) || foundSegments.containsKey(entry.getKey()) && foundSegments.get(entry.getKey()).size() > entry.getValue().size())
                        foundSegments.put(entry.getKey(), entry.getValue());
                });

                if (!shape.isEmpty())
                    potentialNodes.keySet().forEach(n -> ComputationDispatcher.addFuture(this.graphKey, executorService.submit(new AlgorithmExecutor(new LinkedList<>(shape), n, conditionManager, graphKey, currentAlgorithmResult))));
            }
        });

        if(!foundSegments.isEmpty()){
            currentAlgorithmResult.setPathsToEndNodes(foundSegments);
            parentAlgorithmResult.addResultForNode(startNode, currentAlgorithmResult);
            currentAlgorithmResult.setFinished(true);
        }
    }
}
