import org.jetbrains.annotations.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class SearchThread implements Runnable {

  private ArrayList<Integer> pageNums = new ArrayList<>();
  private static ArrayList<String> keywords = new ArrayList<>();
  private PDDocument doc;
  public static String fileName;
  private PDFRenderer renderer;
  private HashMap<String, ArrayList<Loc>> map = new HashMap<>();
  private PDFTextStripper textStripper;
  private ArrayList<ArrayList<Line>> pageLines;
  private static ArrayList<String> oneWordKeywords = tldr.oneWordKeywords;
  private static ArrayList<String> multiWordKeywords = tldr.multiWordKeywords;
  private String[] wordsFromLine;
  private boolean testing = tldr.testing;
  private ArrayList<String> linesFromPage;



  private static int totalNumberInstances = 0;

  private final int WHITE = 0;
  private final int BLACK = 1;

  SearchThread(ArrayList<Integer> pageNums,
                @NotNull ArrayList<String> keywords, String filePath, String fileName)
  {
    /*
     * Parameters:
     * - pageNums --> list of page numbers in the doc that this thread will search through
     * - keywords --> list of keywords the thread will search for
     * - doc --> the document (as a PDDocument object) that this thread will search through
     * - fileName --> String representation of the name of the file this thread will search through
     */

    synchronized (this){
    // Copies all pageNums from the pageNums paramter to the pageNums class variable
    // Making a copy prevents pointer errors from arising later
    this.pageNums.addAll(pageNums);

    //System.out.println("Page range for this thread: [" + pageNums.get(0) +
    //          ", " + pageNums.get(pageNums.size()) + "]");

    // Adds all keywords to keywords class variable
    // Adds all keywords as keys to hashmap (to initialize the hashmap)
    for (String keyword : keywords) {
      SearchThread.keywords.add(keyword);
      map.put(keyword, new ArrayList<Loc>());
    }

    try {
      this.doc = PDDocument.load(new File(filePath));

    } catch (IOException e) {
      e.printStackTrace();
    }

    this.fileName = fileName;
    //makes desktop directory with filename to put all other folders into
    makeTitleDirectory(fileName);

    renderer = new PDFRenderer(doc);
    try {
      textStripper = new PDFTextStripper();

      // Initializes pageLines so that it is the same length as pageNums
      // Each index in pageLines corresponds to the same page in pageNums,
      // and contains all the Line objects for that page
      pageLines = new ArrayList<ArrayList<Line>>();
    } catch (IOException e) {
      e.printStackTrace();
    }}

  }

  public void run()
  {

    for (int pgNum : pageNums) {
      if(testing)
        System.out.println("Currently analyzing page " + pgNum);

      // Analyzes each page and finds the section breaks, lines, and spaces
      pixelAnalysis(pgNum);

      // Extracts text from the page
     linesFromPage = extractTextFromPage(pgNum);
//      System.out.println("Number of text lines on page " + pgNum + ": " + lines.size());

      if (linesFromPage != null) {

        // For each line of text on page, looks for the keyword and records if the keyword is found to
        // snapshot later
        for (int lineIndex = 0; lineIndex < linesFromPage.size(); lineIndex++) {
          String textLine = linesFromPage.get(lineIndex);
          findKeywordsInLine(textLine, pgNum, lineIndex);
        }
      }
    }

    takeSnapshots();


    //TODO: Write summary sheet here if possible


    //TODO: This is where the threads end... does this need to be changed??
//
//    try {
//      doc.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

  private void pixelAnalysis(int pageNum)
  {
    /*
     * Paramters: pageNum - the number of the page that will be analyzed
     *
     * This method analyzes the pixels in the image representation of the given page to identify key layout features like
     * spaces, section breaks, and lines. It also determines and records, for each line, the nearest section breaks in
     * order to make snapshotting easier in the future.
     */
    if(testing){
    System.out.println("Starting pixel analysis");
    System.out.println("Pixel analysis page " + pageNum);}
    try {

      // Converts the page to an image
      BufferedImage pgImg = renderer.renderImageWithDPI(pageNum-1, 300);

      // Finds all points where the rows in the image change from all-white to not-all-white and vice versa
      ArrayList<LineChange> lineChanges = findLineChanges(pgImg);

      // Uses the line changes to identify spaces, lines, and section breaks
      ArrayList<ArrayList<LayoutFeature>> layoutFeatures = identifyLayoutFeatures(lineChanges, pageNum, pgImg.getHeight());

      // Conversion of recognized layout features into appropriate objects for greater future functionality
      ArrayList<Space> spaces = convertSpaces(layoutFeatures.get(0));
      ArrayList<Line> lines = convertLines(layoutFeatures.get(1), pageNum);
      ArrayList<SectionBreak> sectionBreaks = convertSectionBreaks(layoutFeatures.get(2));

      // Finds and records the snapshot boundaries (nearest section breaks) for each line
      lines = findSnapshotBoundaries(lines, sectionBreaks);

      // Records the visually-determined Lines in the pageLines array for future retrieval
      pageLines.add(lines);

      } catch (IOException e) {
      // TODO: Deal with IOException exception here
        e.printStackTrace();
      }
  }

  private ArrayList<LineChange> findLineChanges(@NotNull BufferedImage bim)
  {
    /*
     * Paramters:
     * bim - the image of the page
     *
     * This method finds all points where the rows of pixels in the image of the page changes from all-white to
     * not-all-white and vice versa. The return of this method is then used in pixelAnalysis to identify various
     * layout features like spaces, lines, and section breaks (which then helps with snapshotting).
     */

    // Determines the width and height of the image
    int width = bim.getWidth();
    int height = bim.getHeight();
    ArrayList<LineChange> lineChanges = new ArrayList<>();

    // Considers each row in the image
    int switchState = -1;
    for (int row = 0; row < height; row++) {

      // Determines if the row is all-white or not-all-white
      int rowSum = 0;
      for (int col = 0; col < width; col++) {

        // Retrieves the RGB values of each pixel in the row
        int pixel = bim.getRGB(col, row);
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        // Determines if the pixel is white or not white, and gives the pixel a numerical
        // value accordingly (0 or 1 respectively)
        if (red == 255 && green == 255 && blue == 255) {
          pixel = WHITE;
        } else {
          pixel = BLACK;
        }

        // Adds the numerical value of the pixel into the numerical value of the row, which is then used to determine
        // if the row is white or not-white
        rowSum += pixel;
      }

      // Determine if this row is a point where the rows have changed from all white to not-all-white or vice versa
      if (rowSum == WHITE && switchState != WHITE) {
        // We have found a point where the row changes to all white

        // Updates the switch state
        switchState = WHITE;

        // Creates a new LineChange object and adds it to the list of line changes
        LineChange lineChange = new LineChange(row, switchState);
        lineChanges.add(lineChange);

      } else if (rowSum > WHITE && switchState == WHITE || switchState < 0) {
        // We have found a point where the row changes to not-all-white (or is this is the first row change,
        // which generally happens when the first line is found)

        // Updates the switch state
        switchState = BLACK;

        // Creates a new LineChange object and adds it to the list of line changes
        LineChange lineChange = new LineChange(row, switchState);
        lineChanges.add(lineChange);
      }
    }

    return lineChanges;
  }

  private ArrayList<ArrayList<LayoutFeature>> identifyLayoutFeatures(@NotNull ArrayList<LineChange> lineChanges, int pageNum, int height)
  {
    /*
     * Parameters:
     * lineChanges -- list of line changes, or rows where the image of the page changes from all-white to not-all-white and vice versa.
     * pageNum -- the number of the page that is being analyzed
     * height -- the height of the page
     *
     * This method analyzes the line changes to find paces, lines, and section breaks. To save space,
     * it returns an array of three arraylists of generic LayoutFeature objects that are then cast to their
     * appropriate types in the pixelAnalysis method.
     */

    ArrayList<LayoutFeature> spaces = new ArrayList<>();
    ArrayList<LayoutFeature> lines = new ArrayList<>();
    ArrayList<LayoutFeature> sectionBreaks = new ArrayList<>();
    ArrayList<ArrayList<LayoutFeature>> layoutFeatures = new ArrayList<>();

    // Gathers statistics about the differing amounts of white space between Lines
    SummaryStatistics whiteSpaceStats = new SummaryStatistics();
    for (int i = 0; i < lineChanges.size() - 1; i++) {
      // Retrieves the current LineChange and the next LineChange
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);

      // Determines if the line has changed from all-white to not-all-white or vice versa (basically identifying if
      // we're looking a section of whitespace or a line
      int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
      if (currLineChange.state() == BLACK && nextLineChange.state() == WHITE) {
        // A change from black to white indicates a line
        Line line = new Line(currLineChange.rowIndex(), nextLineChange.rowIndex(), pageNum);
        lines.add(line);
      } else {
        // Stores the width of the section of whitespace; this wil help us determine if that section was
        // a space or a section break
        whiteSpaceStats.addValue(difference);
      }
    }

    // Determines the average width of a section of whitespace; this will help us differentiate between spaces and
    // section breaks
    // I've also determined the standard deviation. We don't use this currently but we could use it for a wider/narrow range?
    double mean = whiteSpaceStats.getMean();
    double stdev = whiteSpaceStats.getStandardDeviation();
    for (int i = 0; i < lineChanges.size() - 1; i++) {

      // Retrieves the current and next line change
      LineChange currLineChange = lineChanges.get(i);
      LineChange nextLineChange = lineChanges.get(i + 1);

      // Narrows selection of selected line changes to only sections of white space
      if (currLineChange.state() == WHITE && nextLineChange.state() == BLACK) {

        // Calculates the width of the section of white space
        int difference = nextLineChange.rowIndex() - currLineChange.rowIndex();
        // If it is a less-than-average width, then this section must be a space
        if (difference < mean) {
          Space space = new Space(currLineChange.rowIndex(), nextLineChange.rowIndex());
          spaces.add(space);
        } else
        {
          // Otherwise, this section is wider than average, therefore it must be a section break
          // TODO: Investigate this statistical analysis. I feel like in reality, the distribution of widths of
          //  sections of whitespace is actually bimodal, and I wonder if it's possible for us to find modes insteads?
          //  Research analysis of bimodal distributions
          SectionBreak sb = new SectionBreak(currLineChange.rowIndex(), nextLineChange.rowIndex());
          sectionBreaks.add(sb);
        }
      }
    }

    // Adds the end page margins as a final section break to make it easier when
    // snapshotting lines towards the end of the page
    sectionBreaks.add(new SectionBreak(lineChanges.get(lineChanges.size() - 1).rowIndex(), height));

    // Fills layoutFeatures array so that the spaces, lines, and sectionBreaks can be returned
    // in a more space-friendly way (and all at once, by one method)
    layoutFeatures.add(spaces);
    layoutFeatures.add(lines);
    layoutFeatures.add(sectionBreaks);
    if(testing){
      System.out.println("There are " + spaces.size() + " spaces.");
      System.out.println("There are " + lines.size() + " lines.");
      System.out.println("There are " + sectionBreaks.size() + " section breaks" +
              ".");}
    return layoutFeatures;
  }

  private void print(@NotNull ArrayList<Line> lines)
  {
    /*
     * Parameter: Takes in a list of Line objects to be printed
     *
     * Prints Line objects in the given list.
     */

    for (Line line : lines) {

      System.out.println("Line: " + line);
    }
  }

  private void printPageLines()
  {
    /*
     * Print the list of Lines for each page by iterating through pageLines.
     */

    System.out.println("Printing page lines");
    for (ArrayList<Line> lines : pageLines) {
      print(lines);
    }
  }

  private ArrayList<Space> convertSpaces(@NotNull ArrayList<LayoutFeature> layoutFeatures)
  {
    /*
     * Converts a generic list of layout features into space objects.
     *
     * This method is used when processing the layout features determined by pixelAnalysis.
     * The conversion is so that we can use Space-specific methods (like the specific toString
     * for printing out Spaces, which will help in testing).
     */

    // Casts each LayoutFeature object into a Space object
    ArrayList<Space> spaces = new ArrayList<>();
    for (LayoutFeature layoutFeature : layoutFeatures) {
      spaces.add(new Space(layoutFeature.startIndex(), layoutFeature.endIndex()));
    }

    return spaces;
  }

  private ArrayList<Line> convertLines(@NotNull ArrayList<LayoutFeature> lfs, int pageNum)
  {
    /*
     * Converts a generic list of layout features into Line objects.
     *
     * This method is used when processing the layout features determined by pixelAnalysis.
     * The conversion is so that we can use Line-specific methods (like the specific toString
     * for printing out Lines, which will help in testing, and the setSnapshotBoundaries which
     * is essential for snapshotting).
     */

    // Casts each LayoutFeature object into a Line object
    ArrayList <Line> lines = new ArrayList<>();
    for (LayoutFeature lf : lfs) {
      lines.add(new Line(lf.startIndex(), lf.endIndex(), pageNum));
    }
    return lines;
  }

  private ArrayList<SectionBreak> convertSectionBreaks(@NotNull ArrayList<LayoutFeature> lfs)
  {
    /*
     * Converts a generic list of layout features into SectionBreak objects.
     *
     * This method is used when processing the layout features determined by pixelAnalysis.
     * The conversion is so that we can use SectionBreak-specific methods (like the specific
     * toString for printing out SectionBreaks, which will help in testing).
     */

    // Casts each LayoutFeature object into a SectionBreak object
    ArrayList<SectionBreak> sectionBreaks = new ArrayList<>();
    for (LayoutFeature lf : lfs) {
      sectionBreaks.add(new SectionBreak(lf.startIndex(), lf.endIndex()));
    }
    return sectionBreaks;
  }

  @Contract("_, _ -> param1")
  private ArrayList<Line> findSnapshotBoundaries(@NotNull ArrayList <Line> lines, ArrayList<SectionBreak> sectionBreaks)
  {
    /*
     * Parameters:
     * lines --> list of Lines, determined visually through pixelAnalysis
     * sectionBreaks --> list of SectionBreaks determined visually through pixelAnalysis
     *
     * This method finds the closest Section Breaks before and after each Line in order to find the boundaries for a
     * snapshot if the a keyword was found on this line.
     */

    // TODO: Make smarter/faster by ordering Lines & SectionBreaks by start and end indeces, possibly?

    for (Line line : lines) {

      // Retrieves the start and end row indeces for the line
      int lineStart = line.startIndex();
      int lineEnd = line.endIndex();

      // Initializes the distance and index records for the start Section Break (closest Section Break before the Line)
      // and the end Section Break (closest Section Break after the line)

      // Distance between the start Section Break and the start of the current Line
      int startMinDistance = 0;
      // Index of the start Section Break in the list of Section Breaks
      int startMinIndex = -1;
      // Distance between the end of the current Line and the end Section Break
      int endMinDistance = 0;
      // Index of the end Section Break in the list of Section Breaks
      int endMinIndex = -1;

      // Iterates through the list of Section Breaks
      for (int sb = 0; sb < sectionBreaks.size(); sb++) {

        // Gets the current Section Break
        SectionBreak currSectionBreak = sectionBreaks.get(sb);

        // Determines if the current Section Break is before or after the Line
        if (currSectionBreak.endIndex() < lineStart) {
          // The end of this section break is before the start of the line, so the section break must be before the line

          // Retrieves the distance between the line and the current section break
          // We know this must be the order of the subtraction because the start of the line must be greater than the
          // end of the section break
          int distance = lineStart - currSectionBreak.startIndex();

          // Updates the record of the closest start Section Break if there hasn't been one initialized yet
          // (startMinDistance == 0), or if the current section break is closer (distance < startMinDistance)
          if (startMinDistance == 0 || distance < startMinDistance) {
            startMinIndex = sb;
            startMinDistance = distance;
          }
        }
        if (currSectionBreak.startIndex() > lineEnd) {
          // THe start of this section break is after the end of the line, so the section break must be after the line

          // Retrieves the distance between the line and the current section break
          // We know this must be the order of the subtraction because the start of the section break must be greater
          // than the end of the line
          int distance = currSectionBreak.startIndex() - lineEnd;

          // Updates the record of the closest end Section Break if there hasn't been one initialized yet
          // (endMinDistance == 0), or if the current section break is closer (distance < endMinDistance)
          if (endMinDistance == 0 || distance < endMinDistance) {
            endMinIndex = sb;
            endMinDistance = distance;
          }
        }
      }

      // Retrieves the startSectionBreak from the list of section breaks
      SectionBreak startSectionBreak = null;
      // Checks for valid index found for the start section break
      // We check if startMinIndex - 1 is greater than zero and not just startMinIndex because we end up retrieving the
      // Section Break at (startMinIndex - 1) in the list of section breaks
      if ((startMinIndex - 1) > 0) {
        // Retrieves the section break before the closest one before the Line in order to increase the context of the snapshot
        // TODO: Consider just going a few extra rows before the startMinIndex rather than an entire additional sectionBreak?
        //  Perhaps we could just go a half-section-break afterwards. We can determine what the width of a sectionBreak
        //  is and use that?
        startSectionBreak = sectionBreaks.get(startMinIndex - 1);
      } else {
        // If not valid, default to the first section break found
        startSectionBreak = sectionBreaks.get(0);
      }

      // Retrieves the endSectionBreak from the list of section breaks
      SectionBreak endSectionBreak = null;
      // Checks for valid index found for the end section break
      // We check that (endMinInex + 1) is within valid bounds and not endMinIndex, because we end up retrieving the
      // SectionBreak at (endMinIndex + 1) in the list of section breaks
      if ((endMinIndex + 1) > 0 && (endMinIndex + 1) < sectionBreaks.size() - 1) {
        // Retrieves the section break after the closest one after the Line in order to increase the context of the snapshot
        // TODO: See startMinIndex TODO above
        endSectionBreak = sectionBreaks.get(endMinIndex + 1);
      } else {
        endSectionBreak = sectionBreaks.get(sectionBreaks.size() - 1);
      }

      // Retrieves the bounds of the snapshot; the starting is the end of the previous section break and the ending is
      // the start of the following section break
      int startIndex = startSectionBreak.endIndex();
      int endIndex = endSectionBreak.startIndex();

      // Updates the line with the snapshot boundaries
      // Now these boundaries can be retrieved from the Line when it is time to take a snapshot of the line
      line.setSnapshotBoundaries(startIndex, endIndex);
    }

    return lines;
  }

  @Nullable
  private ArrayList<String> extractTextFromPage(int pg)
  {
    /*
    Parameters:
    pg - the given page number from which to extract text

    Extracts the text from the page and filters out lines that are too short. Returns an ArrayList of Strings that is
    all the lines of text that are long enough to be considered not-empty.

    The lines of text are returned are compared against the lines of text that are found through the pixel analysis.
     */

    if(testing)
      System.out.println("Page for Text Stripper: " + pg);

    // Sets the range of the textStripper to only remove text from this page
    textStripper.setStartPage(pg);
    textStripper.setEndPage(pg);
    try {

      // Breaks the text up into lines
      String pageText = textStripper.getText(doc);
      String[] pageLines = pageText.split("\n");
      ArrayList<String> pgLines = new ArrayList<>();

      // FIlters out lines of text that are empty
      for (String pageLine : pageLines) {
        if (pageLine.trim().length() > 0) {
          pgLines.add(pageLine);
        }
      }

      if(!testing){
        System.out.println("size of pgLines: " + pgLines.size()+" on page "+ pg);
        System.out.println("Original size of pageLines: " + pageLines.length+
                " on page "+ pg);

      }

      return pgLines;

    } catch (IOException e) {
      // TODO: Deal w/ exception
      e.printStackTrace();
    }
    return null;
  }

  private synchronized void findKeywordsInLine(@NotNull String textLine, int pageNum,
                                   int line)
  {

    //array of words in a line separated based on spaces
    wordsFromLine = textLine.split(" ");

    String word;
    // for each word in the line of text from document
    for (int i = 0; i < wordsFromLine.length; i ++)
    {
      ArrayList<String> keyword = matchKeyword(wordsFromLine[i], i, line);

      if(keyword != null){
        for (String s : keyword) {
          word = s;
          //not getting here the second time
//      System.out.println("Found keyword: "+keyword);
          if (word != null) {
            if (testing)
              System.out.println("Found keyword " + word + " on line " + line);
            ArrayList<Loc> locs = map.get(word);

//        ArrayList<Line> lines = pageLines.get(pageNums.indexOf(pageNum - 1));
//        System.out.println("Size of Lines on page " + pageNum + ": " + lines);
            System.out.println("Line number where keyword was found on page " + pageNum + ": " + line);
            System.out.println("Number of visual lines: " + pageLines.get(pageNums.indexOf(pageNum)).size());

            //this is short term solution for the out of bounds errors
            //TODO: Should this just equal .size() instead??
            if (line >= pageLines.get(pageNums.indexOf(pageNum)).size()) {
              line = pageLines.get(pageNums.indexOf(pageNum)).size() - 1;
            }
            locs.add(new Loc(pageNum, line));
            map.put(word, locs);
          }
        }

  }

    }

        for (int i = 0; i< wordsFromLine.length; i++)
    {
        wordsFromLine[i] = null;
    }
  }

  @Nullable
  private synchronized ArrayList<String> matchKeyword(String randomWord,
                                           int positionOfWord, int line)
  {/*

  Inputs: Word from line of text to be checked against each keyword and the
  position of the word in the line (int)
  Returns: the String of the keyword or keyphrase that was being searched for
   if the phrase or word was matched
  */

    //TODO: What if one of the two word phrases begins with same word as a
    // single word one??

    ArrayList<String> toBeReturned = new ArrayList<>();
    randomWord = randomWord.toLowerCase();
    int numOtherLines = 0;

    for (String keyword : oneWordKeywords) {
//      System.out.println("keyword3" + keyword);

        // I used .matches in CB, and I don't remember why
      if(matchesWord(randomWord, keyword))
     {
        toBeReturned.add(keyword);
      }
    }

    //check if there are no one word keywords that work and no two word ones
    // to be checked

    if(multiWordKeywords.isEmpty()){
      if(toBeReturned.isEmpty()){
        return null;
      }
      else return toBeReturned;
    }
    // this loop is used to check against the keywords that are one word
//    System.out.println("Checking this word: " + word);

    //TODO: Check the length then next X # of words, then go from there...

    //this loop is used to check against key words that are multiple words
    for (int g = 0; g < multiWordKeywords.size(); g++) {
      String keyPhrase = multiWordKeywords.get(g);

      //split the phrase into an array of separate words
      String[] separateWordsFromPhrase = keyPhrase.split(" ");

      int count = 0;

      String[] substringFromLine = new String[separateWordsFromPhrase.length];

      if(matchesWord(randomWord, separateWordsFromPhrase[0])){
        count++;
        if(testing){
        System.out.println("Count is: " + count);
        System.out.println("Matching word is: " + randomWord + " and " + separateWordsFromPhrase[0]);
        System.out.println("separateWordsFromPhrase[1]: " + separateWordsFromPhrase[1]);
        System.out.println("substringFromLine[1]: " + substringFromLine[1]);
      }
      }

      else
        return null;
      //TODO: CHANGE THIS FOR MULTI THAT START WITH SAME WORD



      //fills the array of substring from line, this is a count starting at 0
      // of how many are in the array
      int wordsIn = 0;

      for (int i = 0; wordsIn < separateWordsFromPhrase.length; i++){
        //this fills the entire string of words, if the phrase has three
        // words starting with "the" the first word in this array would also
        // be "the"

        if(i + positionOfWord < wordsFromLine.length)
        {

        String wordToBePutIn = wordsFromLine[positionOfWord + i];
        substringFromLine[wordsIn] = wordToBePutIn;
        wordsIn++;
        }
        else {
          i = -1;
          positionOfWord = 0;
          numOtherLines++;
          String text = linesFromPage.get(line + numOtherLines);

          if(!testing)
          System.out.println("text from next line: " + text);

          wordsFromLine = text.split(" ");
        }


      }

      if(!testing){
        if(wordsIn != separateWordsFromPhrase.length){
          tldr.print("Something wrong with keyword matching loops");
        }
      }

      //check that the arrays match
      for(int m = 1; m < substringFromLine.length; m++){
        if(matchesWord(substringFromLine[m], separateWordsFromPhrase[m])){
          count ++;
//          System.out.println();
//          System.out.println("Count is: " + count);
//          System.out.println("m is: " + m);
        }
        else
          return null;
      }

      if (count == separateWordsFromPhrase.length){

        if(testing){
          System.out.println("keyphrase: "+ keyPhrase);
        }

        toBeReturned.add(keyPhrase);
      }

    }
    if(toBeReturned.size() > 0){
      return toBeReturned;
    }
    else
    return null;
  }

  private synchronized void takeSnapshots()
  {

    for (String key : map.keySet()) {
      if(testing)
      System.out.println("Taking snapshots for keyword: " + key);

      ArrayList<Loc> locs = map.get(key);

      totalNumberInstances += locs.size();
     if(testing) {

       System.out.println("Size of map.keySet (how many keywords searched): " + map.keySet().size());
       System.out.println("Keyword: " + key);
       System.out.println("map.get(key) size: " + map.get(key).size());

       if(map.get(key).size()>0)
       System.out.println("map.get(key) sub 0: " + map.get(key).get(0));
       System.out.println("Size of locs ArrayList: " + locs.size());
     }
      //this one is throwing: Exception in thread "Thread-0" java.lang
      // .IndexOutOfBoundsException: Index 0 out-of-bounds for length 0
      if(map.get(key).size()>0)

        if(testing)
      System.out.println("First instance of locs: "+ locs.get(0));

      //TODO: Figure this out for when multiple keywords are inputted
      //TODO:^^ Is this todo still live??

      for (Loc loc : locs) {
        if(testing)
          System.out.println("line: " + loc.getLine());
        int pageNum = loc.getPage();
        int lineNum = loc.getLine();

        if(testing)
        System.out.println("Line Num: " + lineNum);

        if (pageNums.indexOf(pageNum) != -1)
        {
          if(testing)
          System.out.println(key + " found on " + pageNum + " page, line " + lineNum);

          // Retrieves the Lines for that page number (from pageLines)
          ArrayList<Line> lines = pageLines.get(pageNums.indexOf(pageNum));
          // error pops up on pg 266, line 23, page 243 line 12, page 127 line 38, page 280 line 29, page 204 line 42,
          // page 142 line 30, page 173 line 52, page 49 line 47, page 197 line 47
          if(testing)
          System.out.println("lines.size() = " + lines.size());
          //pageLines has first page indexed at 0
          if(testing)
          System.out.println("lines.size() of previous"+ pageNum +" page = " +
                           pageLines.get(pageNums.indexOf(pageNum)));
          if(testing)
          System.out.println("lines.size(): " + lines.size() +" on page " + pageNum);

          // Retrieves the correct line
          Line line = lines.get(lineNum);
          String filePath = snapshotLine(line, key, lineNum);

          if(testing)
          System.out.println("Filepath of snapshot: " + filePath);

          if (filePath != null)
            loc.setFilePath(filePath);

          if(testing)
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("map.get(key).get(0).getFilePath(): "+map.get(key).get(0).getFilePath());
        }
      }
      if(testing)
      System.out.println("Done Snapshotting " +key);
      //locs.clear();
    }

  //after taking all snapshots, write the summary sheet
  tldr.writeSummarySheet(map);
  }

  @Nullable
  private String snapshotLine(@NotNull Line line, String keyword, int lineNum)
  {
    int page = line.page();
    try {
      BufferedImage pgImg = renderer.renderImageWithDPI((page-1), 300);
      int startIndex = line.startSnapshotIndex();
      int endIndex = line.endSnapshotIndex();
//      System.out.println("[ " + startIndex + ", " + endIndex + "]");
//      System.out.println("Height = " + pgImg.getHeight());
      if (endIndex == 0) {
//        System.out.println("End index was 0");
        endIndex = pgImg.getHeight();
      }
      int imHeight = endIndex - startIndex;

      BufferedImage keywordSnapshot = pgImg.getSubimage(0, startIndex, pgImg.getWidth(), imHeight);
      String dirPath = makeDirectory(fileName + "-" + keyword, fileName);
      String pageString = Integer.toString(page);
      String filePath = makeFilePath(keyword + "-pg" + pageString + "-line" + lineNum + ".png", dirPath);
      ImageIO.write(keywordSnapshot, "png", new File(filePath));

      return filePath;

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private String makeTitleDirectory(String title)
  {
    /* Creates a directory in the users home folder
  Inputs: Name of the Directory to be created
  Returns: Path of the directory
    */
    Path path = Paths.get(System.getProperty("user.home"),"Desktop", title);

    if(testing)
      System.out.print("Path: " + path + " ");

    if(!Files.isDirectory(path))
    try{
      Files.createDirectories(path);}
    catch (IOException e){
      e.printStackTrace();
    }
    else
      tldr.print("Directory with this title already exists");

    if(Files.isDirectory(path)){
      return path.toString();
    }

    else
      tldr.print("Sorry directory was not created");
    return makeTitleDirectory(title);

  }

  @Nullable
  private String makeDirectory(String word, String title)
  {
    /* Creates a directory in the users home folder under the name of the
    title doc
  Inputs: Name of the Directory to be created
  Returns: Path of the directory
    */
    Path path = Paths.get(System.getProperty("user.home"),"Desktop", title,
            word);
    if(testing)
    System.out.print("Path: " + path);

    try{
    Files.createDirectories(path);}
    catch (IOException e){
      e.printStackTrace();
    }


    if(Files.isDirectory(path)){
      return path.toString();
    }

    else
      tldr.print("Sorry directory was not created");
      return makeDirectory(word, title);


  }

  @NotNull
  private String makeFilePath(String name)
  {/*Creates a new file in the users desktop directory
    Inputs: Name of the File to be created
    Returns: Path of the newly created file
    */

    File file =
            new File(System.getProperty("user.home") + File.separator +
                    "Desktop" + File.separator + name);
	if(testing)
    tldr.print("File path produced: " + System.getProperty("user.home") + File.separator +
            "Desktop" + File.separator + name);

    return file.getAbsolutePath();
  }

  @NotNull
  private String makeFilePath(String nameOfFile, String dirpath)
  {
    /*
    Creates a new file
    Inputs: Name of the File to be created and the path of the directory wanted
    Returns: Path of the newly created file
    */

    File file = new File(dirpath + File.separator + nameOfFile);

    if(testing)
    tldr.print("Dirpath produced: " + dirpath + File.separator + nameOfFile);

    if(testing)
    tldr.print("Absolute path: " + file.getAbsolutePath());

    return file.getAbsolutePath();
  }

  @Contract(pure = true)
  synchronized HashMap<String, ArrayList<Loc>> getHashMap()
  {
    /*
      Returns HashMap for use in main tldr class
     */


    return map;

  }



  private boolean matchesWord(@NotNull String randomWord, String wordToMatch)
  {
    return randomWord.contains(wordToMatch)
            || randomWord.contains(wordToMatch.toLowerCase())
            || randomWord.contains(wordToMatch.toUpperCase())
            // this doesn't really need to be checked because randomWord will
            // always be lowercase
            || randomWord.matches(
            "\\p{Punct}\\p{IsPunctuation}" + wordToMatch)
            || randomWord.matches(wordToMatch + "\\p{Punct" +
            "}\\p" + "{IsPunctuation}");
  }
}
