package computation.graphElements.segments;

import computation.graphElements.Node;
import computation.graphElements.Vector;

public final class SegmentFactory {

    private static long id;

    public SegmentFactory(){
        SegmentFactory.id = 1L;
    }

    public Segment newSegment(Node n1, Node n2){
        return new Segment(getId(),n1,n2);
    }

    public Segment newSegment(Node n1, Node n2, Double percentLength){
        return new Segment(getId(),n1,n2, percentLength);
    }

    public SegmentReflection newSegment(Long correspondingId, Vector v1, Vector v2){
        return new SegmentReflection(getId(), correspondingId, v1, v2);
    }

    public SegmentReflection newSegment(Long correspondingId, Vector v1, Vector v2, Double percentLength, double length){
        return new SegmentReflection(getId(), correspondingId, v1, v2, percentLength, length);
    }

    public SegmentReflection newSegment(Vector v1, Vector v2){
        return new SegmentReflection(getId(), v1, v2);
    }

    public SegmentReflection newSegment(Vector v1, Vector v2, Double percentLength, double overallLength){
        return new SegmentReflection(getId(), v1, v2, percentLength, overallLength);
    }


    public SegmentSoul newSegment(SegmentSoul segmentSoul) {
        return new SegmentReflection(getId(), segmentSoul);
    }

    private synchronized Long getId(){
        return id++;
    }
}
