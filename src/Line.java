public class Line extends LayoutFeature {

    private int startIndex;
    private int endIndex;
    private int page;
    private int startSnapshotIndex;
    private int endSnapshotIndex;

    public Line(int startIndex, int endIndex, int page)
    {
        super(startIndex, endIndex);
        this.page = page;
    }

    @Override
    public String toString()
    {
        return "Line between row " + startIndex + " and " + endIndex;
    }

    public void setSnapshotBoundaries(int startSnapshotIndex, int endSnapshotIndex)
    {
        this.startSnapshotIndex = startSnapshotIndex;
        this.endSnapshotIndex = endSnapshotIndex;
    }

    public int startSnapshotIndex() { return startSnapshotIndex; }

    public int endSnapshotIndex() { return endSnapshotIndex; }

    public int page() { return page; }
}