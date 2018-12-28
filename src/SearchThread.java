import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

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

  SearchThread(ArrayList<Integer> pages, ArrayList<String> keywords, PDDocument doc)
  {
    this.pages = pages;
    this.keywords = keywords;
    this.doc = doc;
    this.renderer = new PDFRenderer(this.doc);

    for (int page : this.pages)
    {
      System.out.println(page);
    }

    //HashMap hashmap = new HashMap<String, ArrayList<Loc>>();
    // Put all the keywords into HashMap


    // save locations of section breaks into arraylist (?) (for each page)
  }

  private void pixelAnalysis()
  {
    System.out.println("Starting pixel analysis");
    for (int pageNum : pages)
    {
      // Get the current page
      System.out.println("On page " + pageNum);
      PDPage page = doc.getPage(pageNum-1);

      try {
        // Convert the page to an image
        BufferedImage pageImg = renderer.renderImageWithDPI((pageNum - 1), 300);
        File outputFile = new File(System.getProperty("user.home") + "/page-" + pageNum + ".png");
        ImageIO.write(pageImg, "png", outputFile);

        int width = pageImg.getWidth();
        int height = pageImg.getHeight();
        System.out.println("Width: " + width);
        System.out.println("Height: " + height);
      } catch (IOException e) {
        // TODO: Respond to catch
        e.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    pixelAnalysis();

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

  private void extractTextFromPage()

  {

  }

  private void snapshotLine()

  {

  }

  private void saveLocToHashMap()

  {

  }

  public HashMap getHashMap()

  {
        return null;
  }

}
