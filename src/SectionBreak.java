public class SectionBreak extends LayoutFeature {

    private int startIndex;
    private int endIndex;

    SectionBreak(int startIndex, int endIndex)
    {
        super(startIndex, endIndex);
    }

    @Override
    public String toString()
    {
        return "Section break between row " + startIndex() + " and " + endIndex();
    }

    public int width() { return endIndex - startIndex; };
}