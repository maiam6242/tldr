public class Space extends LayoutFeature {

    private int startIndex;
    private int endIndex;

    public Space(int startIndex, int endIndex)
    {
        super(startIndex, endIndex);
    }

    @Override
    public String toString()
    {
        return "Space between row " + startIndex + " and " + endIndex;
    }
}
