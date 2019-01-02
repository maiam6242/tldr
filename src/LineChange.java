public class LineChange {
    private int rowIndex;
    private int state;
    private final int WHITE = 0;
    private final int BLACK = 1;

    LineChange(int rowIndex, int state)
    {
        this.rowIndex = rowIndex;
        this.state = state;
    }

    public String toString() {
        String lineChangeDescription = "[" + rowIndex + ", ";
        if (state == WHITE) lineChangeDescription += "WHITE]";
        else if (state != WHITE) lineChangeDescription += "BLACK]";
        // TODO: Fill out the else here with the third-case scenario where something went
        //  wrong when calculating row sums of image
        return lineChangeDescription;
    }

    int rowIndex() {
        return rowIndex;
    }

    int state() {
        return state;
    }
}