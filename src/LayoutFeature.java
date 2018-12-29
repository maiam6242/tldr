public class LayoutFeature {
    private int startIndex;
    private int endIndex;

    public LayoutFeature(int startIndex, int endIndex)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int startIndex()
    {
        return startIndex;
    }

    public int endIndex()
    {
        return endIndex;
    }
}
