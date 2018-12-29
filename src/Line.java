public class Line extends LayoutFeature {

    private int startIndex;
    private int endIndex;

    public Line(int startIndex, int endIndex)
    {
        super(startIndex, endIndex);
    }

    @Override
    public String toString()
    {
        return "Line between row " + startIndex + " and " + endIndex;
    }
}
