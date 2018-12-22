import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class tldr {

  private static JFrame frame;
  public tldr()
  {
    initializeGUI();
  }

  public void initializeGUI() {
    initializeFrame();
    initializeLabels();
    initializeTextField();
    initializeDropDown();
    initializeButtons();
    initializeConsole();
    initializeFileChoosers();

    frame.setVisible(true);
  }

  private void initializeFrame()
  {

  }

  private void initializeLabels()
  {

  }

  private void initializeTextField()
  {

  }

  private void initializeDropDown()
  {

  }

  private void initializeButtons()
  {

  }

  private void initializeConsole()
  {

  }

  private void initializeFileChoosers()
  {

  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() == btnSearch)
    {
      getInputtedKeywords();
      getSelectedKeywords();
      searchKeywords();
    }

    if (event.getSource() == btnSearchFile)
    {
      openSearchFile();
    }

    if (event.getSource() == btnTextFile)
    {
      openKeywordsFile();
    }

    if (event.getSource() == btnMergeFiles)
    {
      mergePDFFiles();
    }

    getOfficeVersion();
    //the version of office determines the file type and creation methods
    createExcelFile();

  }

  private int getOfficeVersion()
  {
    /** Checks what (if any) version of office is on the system.
     *  Returns 0 if no office is installed and the version number if office is installed
     *  The version number is the last two digits of the year that the office package came out (83-Present)
     */

    //TODO: work on making this work regardless of OS (rn windows dependent) and understanding functionality a little bit better
    try{

      //creates process which looks for the office version then reads it, WINDOWS DEPENDENT

      Process process = Runtime.getRuntime().exec(new String [] {"cmd.exe", "/c", "assoc", ".xls"} );
      BufferedReader officeVersionReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String officeVersion = officeVersionReader.readLine();
      officeVersionReader.close();

      //see if any office is installed

      if (officeVersion == null){
        System.out.println("No Office is Installed");
        System.exit(1);
        return 0;
      }

      //determine what version the office installed is
      String fileType[] = officeVersion.split("=");
      process = Runtime.getRuntime().exec(new String [] {"cmd.exe", "/c", "ftype", fileType[1]});
      officeVersionReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String fileAssociation = officeVersionReader.readLine();
      String fullOfficeVersion = fileAssociation.split("=")[1];
      officeVersion = fullOfficeVersion.replaceAll("[^0-9]+","");
      return Integer.parseInt(officeVersion);

    }
    catch(Exception error){
        error.printStackTrace();
    }

    return 0;

  }

  private void createExcelFile()
  {

  }
  private void getInputtedKeywords()
  {

  }

  private void getSelectedKeywords()
  {

  }

  private void searchKeywords()
  {
    /**
    1. Extract pages from PDF
    2. Split up pages into increments of 20
    3. Create a thread for each increment (possibly put in ArrayList?)
    4. Start each thread
    **/

    createThreads();
    runThreads();
  }

  private void runThreads()
  {
    for (Thread t : threads)
    {
      t.start();
    }

    for (Thread t : threads)
    {
      t.join();
    }
  }

  private void createThreads()
  {
    separateContent();

  }

  private void separateContent()
  {

  }

  private void openSearchFile()
  {

  }

  private void openKeywordsFile()
  {

  }

  private String mergePDFFiles(File[] files)
  {
    /** Takes as many PDF files as provided and merges them into one file
    *   Input: array of values
    *   Returns: path of the created file
    */
    String fileName = "";

    //creates a name for the file
    for (int fileNumber = 0; fileNumber<files.length; fileNumber++)
    {
        fileName += files[fileNumber].getName().split("pdf");
    }

    //creates file itself
    File mergedFile = makeFile(fileName, ".pdf");

    //checks if file was created successfully and creates a file if not
   try
   {
       if (!mergedFile.exists())
       {
            mergedFile.createNewFile();
       }
   }

   catch(IOException exception)
   {
      exception.printStackTrace();
   }

   mergedFile.setWritable(true);
   mergedFile.setReadable(true);

   //creates the documents so that they are accessible
   try{
       PDDocument merged = new PDDocument();
       merged = PDDocument.load(files[0]);
   }
   catch(IOException exception){
       exception.printStackTrace();
  }


   for(int fileNum = 1; fileNum < files.length; fileNum ++)
   {

   }
    return fileName;
  }

  private File makeFile(String fileName, String fileExtension)
  {
      /** Creates a file in home directory
       *  Input: name of file and the extension type
       *  Returns: newly made File
       */

      File newFile = new File(System.getProperty("user.home")+ File.separator + fileName + fileExtension);
      return newFile;
  }

}
