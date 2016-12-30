package computation.algorithm;

import computation.ComputationDispatcher;
import computation.algorithm.conditions.ConditionManager;
import computation.graphElements.*;
import computation.graphElements.segments.Segment;
import computation.graphElements.segments.SegmentSoul;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class AlgorithmExecutor implements Callable<List<List<Segment>>>{

    private List<SegmentSoul> shape;
    private Node startNode;
    private ConditionManager conditionManager;
    private int graphKey;
    private Logger log;
    private AlgorithmExecutionResult algorithmResult;


    public AlgorithmExecutor(List<SegmentSoul> shape, Node startNode, ConditionManager conditionManager, int graphKey, AlgorithmExecutionResult result){
        this.shape = shape;
        this.startNode = startNode;
        this.conditionManager = conditionManager;
        this.graphKey = graphKey;
        this.algorithmResult = result;
        this.log = Logger.getLogger(this.toString() + this.hashCode());
    }

    @Override
    public List<List<Segment>> call() throws Exception {
        //todo initialize new call for next segment
        ExecutorService executorService = ComputationDispatcher.executorService;
        Map<Node,Future<List<List<Segment>>>> futures = new HashMap<>();
        Map<Node,List<Segment>> foundSegments = new HashMap<>();
        Graph graph = ComputationDispatcher.getGraph(graphKey);

        if(shape.size() == 0){
            log.info("Null will be thrown");
        }



            SegmentSoul segmentToMap = shape.remove(0);
            List<Segment> potentialSegments = graph.getSegmentsForNode(startNode);
            potentialSegments.forEach(segment -> {
                EndNodesPredictor endNodesPredictor = new EndNodesPredictor(graph, conditionManager);
                Map<Node, List<Segment>> potentialNodes = endNodesPredictor.getNodes(startNode,segment,segmentToMap, segmentToMap.getVector1());

                potentialNodes.entrySet().forEach(entry -> {
                    if(!foundSegments.containsKey(entry.getKey()) || foundSegments.containsKey(entry.getKey()) && foundSegments.get(entry.getKey()).size() > entry.getValue().size())
                        foundSegments.put(entry.getKey(), entry.getValue());
                });

                if(!shape.isEmpty())
                    potentialNodes.keySet().forEach(n ->
                        futures.put(n,executorService.submit(new AlgorithmExecutor(new LinkedList<>(shape), n,conditionManager, graphKey, algorithmResult)))
                    );
            });


        if(shape.isEmpty() && !foundSegments.isEmpty()){
            List<List<Segment>> result = new LinkedList<>();
            foundSegments.values().forEach(list -> result.add(new LinkedList<>(list)));
            return result;
        }else if(foundSegments.isEmpty()){
            futures.values().forEach(f -> f.cancel(true));
            return new LinkedList<>();
        }

        List<List<Segment>> potentialPaths = new LinkedList<>();
        while (!futures.entrySet().isEmpty()){
            try {
                Iterator<Map.Entry<Node, Future<List<List<Segment>>>>> iterator = futures.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Node, Future<List<List<Segment>>>> futureEntry = iterator.next();
                    if (futureEntry.getValue().isDone()) {
                        if (futureEntry.getValue().get().isEmpty()) {
                            iterator.remove();
                        } else {
                            List<Segment> result = foundSegments.get(futureEntry.getKey());
                            for (List<Segment> list : futureEntry.getValue().get()) {
                                List<Segment> tmp = new LinkedList<>(result);
                                tmp.addAll(list);
                                potentialPaths.add(tmp);
                            }
                            iterator.remove();
                            futures.entrySet().parallelStream().forEach(entry -> entry.getValue().cancel(true));
                        }
                    } else if (futureEntry.getValue().isCancelled()) {
                        iterator.remove();
                    }
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //todo cancel or wait for the result
        log.info(String.format("\tReturning path: %s", potentialPaths));
        return potentialPaths;
    }
}
