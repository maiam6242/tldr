public class Line extends LayoutFeature {

    private int startIndex;
    private int endIndex;
    private int page;
    private int startSnapshotIndex;
    private int endSnapshotIndex;

    Line(int startIndex, int endIndex, int page)
    {
        super(startIndex, endIndex);
        this.page = page;
    }

    public String toString()
    {
        return "Line between row " + startIndex() + " and " + endIndex();
    }

    void setSnapshotBoundaries(int startSnapshotIndex, int endSnapshotIndex)
    {
        this.startSnapshotIndex = startSnapshotIndex;
        this.endSnapshotIndex = endSnapshotIndex;
    }

    int startSnapshotIndex() { return startSnapshotIndex; }

    int endSnapshotIndex() { return endSnapshotIndex; }

    int page() { return page; }
}