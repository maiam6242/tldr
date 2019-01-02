class LayoutFeature {
    private int startIndex;
    private int endIndex;

    LayoutFeature(int startIndex, int endIndex)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    int startIndex()
    {
        return startIndex;
    }

    int endIndex()
    {
        return endIndex;
    }
}