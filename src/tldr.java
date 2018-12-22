public class tldr {

  private static JFrame frame;
  public tldr() {
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
      mergeFiles();
    }

    checkOfficeVersion();
    //the version of office determines the file type and creation methods
    createExcelFile();

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

  private void openSearchFile()
  {

  }

  private void openKeywordsFile()
  {

  }

  private void mergeFiles()
  {

  }

}
