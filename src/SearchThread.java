import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;

public class SearchThread implements Runnable {
  private ArrayList<PDPage> pages;
  private ArrayList<String> keywords;

  public SearchThread(ArrayList<PDPage> pages, ArrayList<String> keywords)
  {
    this.pages = pages;
    this.keywords = keywords;

    //HashMap hashmap = new HashMap<String, ArrayList<Loc>>();
    // Put all the keywords into HashMap

    //pixelAnalysis()
    // save locations of section breaks into arraylist (?) (for each page)
  }

  public void run() {
    /**
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
    **/
    {

    }

  }
}
