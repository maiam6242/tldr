public class Loc {
    private int page;
    private int line;
    private String filePath;

    public Loc(int page, int line)
    {
        this.page = page;
        this.line = line;
    }

    public int page()
    {
        return page;
    }

    public int line()
    {
        return line;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String filePath()
    {
        return filePath;
    }
}
