import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchThread implements Runnable {

  private ArrayList<Integer> pageNums = new ArrayList<>();
  private ArrayList<String> keywords = new ArrayList<>();
  private PDDocument doc;
  private String fileName;
  private PDFRenderer renderer;
  private HashMap<String, ArrayList<Loc>> map = new HashMap<>();
  private PDFTextStripper textStripper;
  private ArrayList[] pageLines;

  private final int WHITE = 0;
  private final int BLACK = 1;


  public SearchThread(ArrayList<Integer> pageNums, ArrayList<String> keywords, PDDocument doc, String fileName)
  {
    this.pageNums.addAll(pageNums);
    System.out.println("Page range: [" + pageNums.get(0) + ", " + pageNums.get(pageNums.size() - 1) + "]");

    for (String keyword : keywords)
    {
      this.keywords.add(keyword);
      map.put(keyword, new ArrayList<Loc>());
    }

    this.doc = doc;
    this.fileName = fileName;
    renderer = new PDFRenderer(doc);
    try {
      textStripper = new PDFTextStripper();
      pageLines = new ArrayList[pageNums.size()];
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void pixelAnalysis()
  {
    System.out.println("Starting pixel analysis");
    for (int pageNum : pageNums)
    {
      try {
        BufferedImage pgImg = renderer.renderImageWithDPI(pageNum, 300);
        ArrayList<LineChange> lineChanges = findLineChanges(pgImg);
        ArrayList<LayoutFeature>[] layoutFeatures = identifyLayoutFeatures(lineChanges, pageNum, pgImg.getHeight());
        ArrayList<Space> spaces = convertSpaces(layoutFeatures[0]);
        ArrayList<Line> lines = convertLines(layoutFeatures[1]);
        System.out.println("Printing lines before finding snapshot boundaries");
//        print(lines);
        ArrayList<SectionBreak> sectionBreaks = convertSectionBreaks(layoutFeatures[2]);
        lines = findSnapshotBoundaries(lines, sectionBreaks);
//        print(lines);
        System.out.println("pageNum = " + (pageNum));
        System.out.println("pageNums.indexOf(pageNum) = " + pageNums.indexOf(pageNum));
        pageLines[pageNums.indexOf(pageNum)] = lines;

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

//    printPageLines();
  }

  private void print(ArrayList<Line> lines)
  {
    for (Line line : lines)
    {
      System.out.println("Line: " + line);
    }
  }

  private void printPageLines()
  {
    System.out.println("Printing page lines");
    for (ArrayList<Line> lines : pageLines)
    {
      System.out.println(lines);
    }
  }

  private ArrayList<Line> findSnapshotBoundaries(ArrayList<Line> lines, ArrayList<SectionBreak> sectionBreaks)
  {
    for (Line line : lines) {
      int lineStart = line.startIndex();
      int lineEnd = line.endIndex();

      int startMinDistance = 0;
      int startMinIndex = -1;
      int endMinDistance = 0;
      int endMinIndex = -1;

      for (int sb = 0; sb < sectionBreaks.size(); sb++) {
        if (sectionBreaks.get(sb).endIndex() < lineStart) {
          SectionBreak currSectionBreak = sectionBreaks.get(sb);
          int distance = lineStart - currSectionBreak.startIndex();
          if (startMinDistance == 0 || distance < startMinDistance) {
            startMinIndex = sb;
            startMinDistance = distance;
          }
        }

        if (sectionBreaks.get(sb).startIndex() > lineEnd) {
          SectionBreak currSectionBreak = sectionBreaks.get(sb);
          int distance = currSectionBreak.startIndex() - lineEnd;
          if (endMinDistance == 0 || distance < endMinDistance) {
            endMinIndex = sb;
            endMinDistance = distance;
          }
        }
      }

      SectionBreak startSectionBreak = null;
      if (startMinIndex > 0) {
        startSectionBreak = sectionBreaks.get(startMinIndex - 1);
      } else {
        startSectionBreak = sectionBreaks.get(0);
      }

      SectionBreak endSectionBreak = null;
      if (endMinIndex > 0 && endMinIndex < sectionBreaks.size() - 1) {
        endSectionBreak = sectionBreaks.get(endMinIndex + 1);
      } else {
        endSectionBreak = sectionBreaks.get(sectionBreaks.size() - 1);
      }

      int startIndex = startSectionBreak.endIndex();
      int endIndex = endSectionBreak.startIndex();

      line.setSnapshotBoundaries(startIndex, endIndex);
    }

    return lines;
  }

  private ArrayList<Space> convertSpaces(ArrayList<LayoutFeature> layoutFeatures)
  {
    ArrayList<Space> spaces = new ArrayList<>();
    for (LayoutFeature layoutFeature : layoutFeatures)
    {
      spaces.add((Space) layoutFeature);
    }

    return spaces;
  }

  private ArrayList<Line> convertLines(ArrayList<LayoutFeature> lfs)
  {
    ArrayList<Line> lines = new ArrayList<>();
    for (LayoutFeature lf : lfs)
    {
      lines.add((Line) lf);
    }
    return lines;
  }

  private ArrayList<SectionBreak> convertSectionBreaks(ArrayList<LayoutFeature> lfs)
  {
    ArrayList<SectionBreak> sectionBreaks = new ArrayList<>();
    for (LayoutFeature lf : lfs)
    {
      sectionBreaks.add((SectionBreak) lf);
    }
    return sectionBreaks;
  }


  private ArrayList[] identifyLayoutFeatures(ArrayList<LineChange> lineChanges, int pageNum, int height)
  {
//    System.out.println("lineChanges.size() == " + lineChanges.size());
    ArrayList<LayoutFeature> spaces = new ArrayList<>();
    ArrayList<LayoutFeature> lines = new ArrayList<>();
    ArrayList<LayoutFeature> sectionBreaks = new ArrayList<>();
    ArrayList[] layoutFeatures = new ArrayList[3];

    SummaryStatistics whiteSpaceStats = new SummaryStatistics();
    for (int i = 0; i < lineChanges.size() - 1; i++)
    {
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);

      int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
      if (currLineChange.state() == BLACK && nextLineChange.state() == WHITE)
      {
        Line line = new Line(currLineChange.rowIndex(), nextLineChange.rowIndex(), pageNum);
//        System.out.println(line);
        lines.add(line);
      }
      else
      {
        whiteSpaceStats.addValue(difference);
      }
    }

    double mean = whiteSpaceStats.getMean();
    double stdev = whiteSpaceStats.getStandardDeviation();


//    System.out.println("Mean = " + mean);
//    System.out.println("Stdev = " + stdev);
//    System.out.println("Mean + stdev = " + (mean + stdev));
    for (int i = 0; i < lineChanges.size() - 1; i++)
    {
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);

      if (currLineChange.state() == WHITE && nextLineChange.state() == BLACK)
      {
        int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
//        System.out.println("difference = " + difference);

        if (difference < mean)
        {
          Space space = new Space(currLineChange.rowIndex(), nextLineChange.rowIndex());
//          System.out.println(space);
          spaces.add(space);
        }
        else if (difference >= mean)
        {
          SectionBreak sb = new SectionBreak(currLineChange.rowIndex(), nextLineChange.rowIndex());
//          System.out.println(sb);
          sectionBreaks.add(sb);
        }
      }
    }
    sectionBreaks.add(new SectionBreak(lineChanges.get(lineChanges.size() - 1).rowIndex(), height));

    layoutFeatures[0] = spaces;
    layoutFeatures[1] = lines;
    layoutFeatures[2] = sectionBreaks;

//    System.out.println("There are " + spaces.size() + " spaces.");
//    System.out.println("There are " + lines.size() + " lines.");
//    System.out.println("There are " + sectionBreaks.size() + " section breaks.");
    return layoutFeatures;
  }


  private ArrayList<LineChange> findLineChanges(BufferedImage bim)
  {
    int width = bim.getWidth();
    int height = bim.getHeight();
    ArrayList<LineChange> lineChanges = new ArrayList<>();

    int switchState = -1;
    for (int row = 0; row < height; row++)
    {
      int rowSum = 0;
      for (int col = 0; col < width; col++)
      {
        int pixel = bim.getRGB(col, row);
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        if (red == 255 && green == 255 && blue == 255)
        {
          pixel = WHITE;
        }
        else {
          pixel = BLACK;
        }

        rowSum += pixel;
      }
      if (rowSum == WHITE && switchState != WHITE)
      {
        switchState = WHITE;
        LineChange lineChange = new LineChange(row, switchState);
//        System.out.println("Found a line change: " + lineChange);
        lineChanges.add(lineChange);
      }
      else if (rowSum > WHITE && switchState == WHITE || switchState < 0)
      {
        switchState = BLACK;
        LineChange lineChange = new LineChange(row, switchState);
        lineChanges.add(lineChange);
      }
    }

//    for (LineChange lc : lineChanges)
//    {
//      System.out.println(lc);
//    }
    return lineChanges;
  }


  @Override
  public void run()
  {
    pixelAnalysis();
    for (int pgNum : pageNums)
    {
      System.out.println("On page " + pgNum);
      ArrayList<String> lines = extractTextFromPage(pgNum);
      if (lines != null) {
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++)
        {
          String textLine = lines.get(lineIndex);
          findKeywordsInLine(textLine, pgNum, lineIndex);
        }
      }
    }

    takeSnapshots();
  }

  private void findKeywordsInLine(String textLine, int pageNum, int line)
  {
    String[] words = textLine.split(" ");
    for (String word : words)
    {
      String keyword = matchKeyword(word);
      if (keyword != null)
      {
        System.out.println("Found keyword " + keyword + " on line " + line);
        ArrayList<Loc> locs = map.get(keyword);
        locs.add(new Loc(pageNum, line));
        map.put(keyword, locs);
      }
    }
  }

  @Nullable
  private String matchKeyword(String word) {
//    System.out.println("Checking this word: " + word);
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
      ArrayList<String> pgLines = new ArrayList<>();

      for (String pageLine : pageLines) {
        // TODO: figure out what this is for?
        if (pageLine.length() > 5) {
          pgLines.add(pageLine);
        }
      }

      return pgLines;
    } catch (IOException e) {
      // TODO: Deal w/ exception
      e.printStackTrace();
    }
    return null;
  }

  private void takeSnapshots()
  {
    for (String key : map.keySet())
    {
      ArrayList<Loc> locs = map.get(key);
      for (Loc loc : locs)
      {
        int pageNum = loc.page();
        int lineNum = loc.line();
//        System.out.println("pageNum / lineNum: " + pageNum + " / " + lineNum);
//        System.out.println("pageLines.length = " + pageLines.length);
        ArrayList<Line> lines = pageLines[pageNums.indexOf(pageNum)];
//        System.out.println("lines.size() = " + lines.size());
        Line line = lines.get(lineNum);
        String filePath = snapshotLine(line, key, lineNum);
        if (filePath != null) loc.setFilePath(filePath);
      }
    }
  }

  private String snapshotLine(Line line, String keyword, int lineNum)
  {
    int page = line.page();
    try {
      BufferedImage pgImg = renderer.renderImageWithDPI((page), 300);
      int startIndex = line.startSnapshotIndex();
      int endIndex = line.endSnapshotIndex();
//      System.out.println("[ " + startIndex + ", " + endIndex + "]");
//      System.out.println("Height = " + pgImg.getHeight());
      if (endIndex == 0)
      {
//        System.out.println("End index was 0");
        endIndex = pgImg.getHeight();
      }
      int imHeight = endIndex - startIndex;

      BufferedImage keywordSnapshot = pgImg.getSubimage(0, startIndex, pgImg.getWidth(), imHeight);
      String dirPath = makeDirectory(fileName + "-" + keyword);
      String filePath = makeFilePath(keyword + "-pg" + page + "-line" + lineNum + ".png", dirPath);
      ImageIO.write(keywordSnapshot, "png", new File(filePath));

      return filePath;

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
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
