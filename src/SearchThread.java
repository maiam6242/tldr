import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchThread implements Runnable {
  private ArrayList<Integer> pages;
  private ArrayList<String> keywords;
  private PDDocument doc;
  private PDFRenderer renderer;
  private boolean testing = true;
  private final int WHITE = 0;
  private final int BLACK = 1;
  private PDFTextStripper textStripper;
  private ArrayList<ArrayList<Line>> docSectionBreaks = new ArrayList<>();
  private HashMap<String, ArrayList<Loc>> map;
  private String fileName;

  SearchThread(ArrayList<Integer> pages, ArrayList<String> keywords, PDDocument doc, String fileName)
  {
    this.pages = pages;
    this.keywords = keywords;
    this.doc = doc;
    this.renderer = new PDFRenderer(this.doc);
    this.fileName = fileName;

    for (int page : this.pages)
    {
      if (testing) System.out.println(page);
    }

    map = new HashMap<>();
    for (String keyword : keywords)
    {
      map.put(keyword, new ArrayList<Loc>());
    }

    try {
      textStripper = new PDFTextStripper();
    } catch (IOException e) {
      // TODO: Deal w/ exception
      e.printStackTrace();
    }

    //HashMap hashmap = new HashMap<String, ArrayList<Loc>>();
    // Put all the keywords into HashMap


    // save locations of section breaks into arraylist (?) (for each page)
  }

  private ArrayList<LayoutFeature> pixelAnalysis(int pageNum)
  {
    ArrayList<LayoutFeature> lines = null;
    if (testing) System.out.println("Starting pixel analysis");
    ArrayList<Line> pageLineSnapshots = new ArrayList<>();

    // Get the current page
    if (testing) System.out.println("On page " + pageNum);
    PDPage page = doc.getPage(pageNum-1);

      try {
        // Convert the page to an image
        BufferedImage pageImg = renderer.renderImageWithDPI((pageNum - 1), 300);
        File outputFile = new File(System.getProperty("user.home") + "/page-" + pageNum + ".png");
        ImageIO.write(pageImg, "png", outputFile);

        int height = pageImg.getHeight();

        // Find all the line changes
        ArrayList<LineChange> lineChanges = findLineChanges(pageImg);

        // Identify spaces, lines, and section breaks
        ArrayList<ArrayList<LayoutFeature>> layoutFeatures = identifyLayout(lineChanges);
        ArrayList<LayoutFeature> spaces = layoutFeatures.get(0);
        lines = layoutFeatures.get(1);
        ArrayList<LayoutFeature> sectionBreaks = layoutFeatures.get(2);

        if (testing)
        {
          System.out.println("There are " + spaces.size() + " spaces.");
          System.out.println("There are " + sectionBreaks.size() + " section breaks.");
          System.out.println("There are " + lines.size() + " lines.");
        }

        for (int i = 0; i < lines.size(); i++)
        {
          // For eaach line, find the nearest section breaks on either side
          Line line = (Line) lines.get(i);
          int lineStart = line.startIndex();
          int lineEnd = line.endIndex();

          // Search for the closest section break before this line
          // Find the index of the section break closest to the start of the line
          int startMinDistance = 0;
          int startMinIndex = -1;
          int endMinDistance = 0;
          int endMinIndex = -1;
          for (int sb = 0; sb < sectionBreaks.size(); sb++)
          {
            if (sectionBreaks.get(sb).endIndex() < lineStart)
            {
              SectionBreak currSectionBreak = (SectionBreak) sectionBreaks.get(sb);
              int distance = lineStart - currSectionBreak.startIndex();
              if (startMinDistance == 0 || distance < startMinDistance)
              {
                startMinIndex = sb;
                startMinDistance = distance;
              }
            }

            if (sectionBreaks.get(sb).startIndex() > lineEnd)
            {
              SectionBreak currSectionBreak = (SectionBreak) sectionBreaks.get(sb);
              int distance = currSectionBreak.startIndex() - lineEnd;
              if (endMinDistance == 0 || distance < endMinDistance)
              {
                endMinIndex = sb;
                endMinDistance = distance;
              }
            }
          }

          SectionBreak startSectionBreak = null;
          // Checks if the nearest section break is the top margin
          if (startMinIndex > 0) {
            startSectionBreak = (SectionBreak) sectionBreaks.get(startMinIndex - 1);
          }
          else
          {
            startSectionBreak = (SectionBreak) sectionBreaks.get(0);
          }

          SectionBreak endSectionBreak = null;
          if (endMinIndex > 0 && endMinIndex < sectionBreaks.size() - 1)
          {
            endSectionBreak = (SectionBreak) sectionBreaks.get(endMinIndex);
          }
          else
          {
            endSectionBreak = (SectionBreak) sectionBreaks.get(sectionBreaks.size() - 1);
          }

          int startIndex = startSectionBreak.endIndex();
          int endIndex = endSectionBreak.startIndex();

          if (endIndex == 0) endIndex = height;
          line.setSnapshotBoundaries(startIndex, endIndex);

          // Find snapshot boundaries for each line
          pageLineSnapshots.add(line);
        }

        docSectionBreaks.add(pageLineSnapshots);

      } catch (IOException e) {
        // TODO: Respond to catch
        e.printStackTrace();
      }

    return lines;
  }

  private ArrayList<ArrayList<LayoutFeature>> identifyLayout(ArrayList<LineChange> lineChanges) {

    ArrayList<LayoutFeature> spaces = new ArrayList<LayoutFeature>();
    ArrayList<LayoutFeature> lines = new ArrayList<LayoutFeature>();
    ArrayList<LayoutFeature> sectionBreaks = new ArrayList<LayoutFeature>();
    ArrayList<ArrayList<LayoutFeature>> layoutFeatures = new ArrayList<ArrayList<LayoutFeature>>();

    SummaryStatistics whiteSpaceStats = new SummaryStatistics();
    for (int i = 0; i < lineChanges.size() - 1; i++)
    {
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);
      int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
      if (currLineChange.state() == BLACK && nextLineChange.state() == WHITE)
      {
        Line line = new Line(currLineChange.rowIndex(), nextLineChange.rowIndex());
        lines.add(line);
      }
      else
      {
        whiteSpaceStats.addValue(difference);
      }
    }

    double mean = whiteSpaceStats.getMean();
    double stdev = whiteSpaceStats.getStandardDeviation();
    for (int i = 0; i < lineChanges.size() - 1; i++)
    {
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);

      if (currLineChange.state() == WHITE && nextLineChange.state() == BLACK)
      {
        int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
        if (difference < mean)
        {
          Space space = new Space(currLineChange.rowIndex(), nextLineChange.rowIndex());
          spaces.add(space);
        }
        else
        {
          SectionBreak sb = new SectionBreak(currLineChange.rowIndex(), nextLineChange.rowIndex());
          sectionBreaks.add(sb);
        }
      }
    }

    layoutFeatures.add(spaces);
    layoutFeatures.add(lines);
    layoutFeatures.add(sectionBreaks);
    return layoutFeatures;
  }

  private ArrayList<LineChange> findLineChanges(BufferedImage pageImg)
  {
    // Get the image's dimensions
    int width = pageImg.getWidth();
    int height = pageImg.getHeight();
    if (testing)
    {
      System.out.println("Width: " + width);
      System.out.println("Heigt: " + height);
    }

    // Record color of each pixel (white or black)
    int[][] pixels = new int[height][width];
    for (int row = 0; row < height; row++)
    {
      for (int col = 0; col < width; col++)
      {
        int pixelColor = pageImg.getRGB(col, row);
        int red = (pixelColor >> 16) & 0xff;
        int green = (pixelColor >> 8) & 0xff;
        int blue = (pixelColor) & 0xff;

        if (red == 255 && green == 255 && blue == 255) {
          pixels[row][col] = WHITE;
        }
        else
        {
          pixels[row][col] = BLACK;
        }
      }
    }

    // Record whether the rows are black or white
    int[] rowSums = new int[height];
    for (int row = 0; row < height; row++)
    {
      int rowSum = 0;
      for (int col = 0; col < pixels[row].length; col++)
      {
        rowSum += pixels[row][col];
      }
      rowSums[row] = rowSum;
    }

    // Determine points where the rows change from white to black
    int switchState = -1;
    ArrayList<LineChange> lineChanges = new ArrayList<LineChange>();
    for (int i = 0; i < rowSums.length; i++)
    {
      int rowSum = rowSums[i];

      // The row switched to white
      if (rowSum == WHITE && switchState != WHITE)
      {
        switchState = WHITE;
        LineChange lineChange = new LineChange(i, switchState);
        lineChanges.add(lineChange);
      }

      // The row switched to black, or this is the first detected line change (generally accounting for a margin,
      // or something to that effect)
      else if (rowSum > WHITE && switchState == WHITE || switchState < 0)
      {
        switchState = BLACK;
        LineChange lineChange = new LineChange(i, switchState);
        lineChanges.add(lineChange);
      }
    }

    return lineChanges;

  }

  @Override
  public void run() {

    ArrayList<ArrayList<LayoutFeature>> lineList = new ArrayList<ArrayList<LayoutFeature>>();

    for (int pg : pages)
    {
      lineList.add(pixelAnalysis(pg));
    }

    for (int pg : pages)
    {
        ArrayList<LayoutFeature> textLines = lineList.get(pg);
        ArrayList<String> pageLines = extractTextFromPage(pg);

        for (int i = 0; i < pageLines.size(); i++)
        {
          String line = pageLines.get(i);
          String[] words = line.split(" ");

          for (String word : words)
          {
            String keyword = matchKeyword(word);
            if (keyword != null)
            {
              Line keywordLine = (Line) textLines.get(i);
              ArrayList<Loc> locs = map.get(keyword);
              locs.add(new Loc(pg, i));
              map.put(keyword, locs);
            }
          }
        }
    }

    /*
    for (page : pages)
    {
    extractTextFromPage()
    for (word : pageText)
    {
    if (word is keyword)
    {
    snapshotLine()
    saveLocToHashMap()
  }
  }
  }
    */

  }

  @Nullable
  private String matchKeyword(String word) {
    if (testing) System.out.println("Checking this word: " + word);
    word = word.toLowerCase();
    for (String keyword : keywords) {

      /*
      TODO: Figure out what we want to do here and whether we want to make
       methods for this like what we did before with the isKeywordHere stuff
       (think about time and scalability)
      */

      //keyhero(keyword, word))
      if (word.contains(keyword) || word.contains(keyword.toLowerCase())
              || word.contains(keyword.toUpperCase())) {
        return keyword;
      }
    }
    return null;
  }

  @Nullable
  private ArrayList<String> extractTextFromPage(int pg)
  {
    textStripper.setStartPage(pg);
    textStripper.setEndPage(pg);
    try {
      String pageText = textStripper.getText(doc);
      String[] pageLines = pageText.split("\n");
      ArrayList<String> pgLines = new ArrayList<String>();

      for (int i = 0; i < pageLines.length; i++)
      {
        // TODO: figure out what this is for?
        if (pageLines[i].length() > 5)
        {
          pgLines.add(pageLines[i]);
        }
      }

      return pgLines;
    } catch (IOException e) {
      // TODO: Deal w/ exception
      e.printStackTrace();
    }
    return null;
  }

  private void snapshotLine()
  {
    for (String keyword : map.keySet())
    {
      ArrayList<Loc> locs = map.get(keyword);
      for (Loc loc: locs)
      {
        int page = loc.page();
        int line = loc.line();

        try {
          BufferedImage pageImg = renderer.renderImageWithDPI(page, 300);
          ArrayList<Line> pageSectionBreaks = docSectionBreaks.get(page);

          Line pageLine = pageSectionBreaks.get(line);
          int startSnapshotIndex = pageLine.startSnapshotIndex();
          int endSnapshotIndex = pageLine.endSnapshotIndex();

          int imHeight = endSnapshotIndex - startSnapshotIndex;
          BufferedImage keywordSnapshot = pageImg.getSubimage(0, startSnapshotIndex,
                  pageImg.getWidth(), imHeight);
          String dirPath = makeDirectory(fileName + "-" + keyword);
          String filePath = makeFilePath(keyword + "-pg" + page + "-line" + line + ".png", dirPath);
          ImageIO.write(keywordSnapshot, "png", new File(filePath));
        } catch (IOException e) {
          // TODO: Deal with exception
          e.printStackTrace();
        }

      }
    }
  }

  @NotNull
  private String makeDirectory(String word) {
    File g = new File(System.getProperty("user.home") + File.separator + word);
    g.mkdirs();
    return g.getAbsolutePath();

  }

  //TODO: edit these a bit to improve on our original implementations
  private String makeFilePath(String j) {
    String name = j;
    File file = new File(System.getProperty("user.home") + File.separator + name);
//		print("File path produced: " + System.getProperty("user.home") + File.separator + name);

    String absolutePath = file.getAbsolutePath();
    return absolutePath;
  }

  private String makeFilePath(String j, String dirpath) {
    String name = j;
    File file = new File(dirpath + File.separator + name);
//		print("Dirpath produced: " + dirpath + File.separator + name);

    String absolutePath = file.getAbsolutePath();
//		print("Absolute path: " + absolutePath);
    return absolutePath;
  }

  @Contract(pure = true)
  public HashMap getHashMap()
  {
    /*
      Returns HashMap for use in main tldr class
     */
    return map;
  }

  private boolean analyzeKeywords (@NotNull String keyword, String line)
  {
    /*
    This is basically keyhero... Looks at whether the keyword is one word or
    multiple and sends it to the right iskeywordhere method
    Inputs: Keyword(s)
    Returns: whether or not keyword is in line (return of iskeywordhere methods)
     */
  //TODO: Check that both inputs are not null

    int indexOfSpace = keyword.indexOf(" ");

    //there is no space in the word
    if (indexOfSpace == -1) {
      return isKeywordHere(keyword, line);
    }
    // keyword is actually multiple words
    else
    {
      String [] words = keyword.split(" ");
      return isKeywordHere(words, line);
    }

  }

  //TODO: Decide if this is the route that we want to take
  private boolean isKeywordHere(String [] words, String line)
  {

    return true;
  }

  private boolean isKeywordHere(String word, String line)
  {
    return true;
  }

}
