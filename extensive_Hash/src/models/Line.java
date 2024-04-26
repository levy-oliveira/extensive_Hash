package models;

public class Line {
    private String index;
    private int localDepth;
    private Bucket bucket;

    public Line(String index, Bucket bucket, int localDepth) {
        setIndex(index);
        setBucket(bucket);
        setLocalDepth(localDepth);
    }

    public Line(String index, int localDepth) {
        setIndex(index);
        setLocalDepth(localDepth);
    }

    public Line(Line lineClone) {
        setIndex(lineClone.getIndex());
        setLocalDepth(lineClone.getLocalDepth());
    }

    public Line() {}

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public void setLocalDepth(int localDepth) {
        this.localDepth = localDepth;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }
}
