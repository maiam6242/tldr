import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

// UI imports

class tldr implements ActionListener {

  private static JFrame frame;
  private static JPanel contentPane;
  private static JTextField keywordField;
  private static JLabel titleLbl;
  private static JLabel instructionsLbl;
  private static JLabel selectInstructionsLbl;
  private static JList preloadedList;
  private static JScrollPane scrollPane;
  private static JButton searchBtn;
  private static JButton searchFileBtn;
  private static JButton textFileBtn;
  private static JButton mergeBtn;
  private static JTextArea console;
  private static String currText = "";
  private static ArrayList<String> keywords = new ArrayList<>();
  private static FileDialog fileDialog;
  private static File file;
  private static File keywordsFile;
  private static ArrayList<SearchThread> threads = new ArrayList<>();
  private static File CSV = null;
  private static Workbook HSSF = null;
  private static Workbook XSSF = null;
  private static FileWriter fileWriter;

  private static boolean testing = true;

  tldr()

  {
     initializeGUI();
  }

  private void initializeGUI()
  {
    /*
      Initializes the user interface using Java Swing.
     */
    initializeFrame();
    initializeLabels();
    initializeTextField();
    initializeDropDown();
    initializeButtons();
    initializeConsole();

    frame.setVisible(true);
  }

  private void initializeFrame()
  {
     /*
      Creates and configures the window.
     */

    // Creates the frame
    // Initialize JFrame object
    frame = new JFrame();
    // Configures frame to close when process terminates
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // Sets size of frame
    frame.setBounds(100, 100, 502, 629);

    // Creates content pane that contains all other components
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

    // Adds content pane to frame
    frame.setContentPane(contentPane);

    // Configures grid bag layout
    GridBagLayout gbl_contentPane = new GridBagLayout();
    // Determines sizes of each row and column
    gbl_contentPane.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
    gbl_contentPane.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    // Determines weights TODO: figure out what the weights mean??
    gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
    gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
    // Adds layout to the content pane
    contentPane.setLayout(gbl_contentPane);

  }

  private void initializeLabels()
  {
    /*
      Creates and positions all the labels (title label and instruction labels).
     */

    // Creates title label
    // Creates label object with the correct text
    titleLbl = new JLabel("TLDR");
    // Configures appearance of title label (font, color, justification)
    titleLbl.setBackground(Color.RED);
    titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
    titleLbl.setHorizontalTextPosition(SwingConstants.CENTER);
    // Configures positioning of title label
    titleLbl.setAlignmentY(Component.TOP_ALIGNMENT);
    titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
    // Configures positioning within the grid bag layout
    GridBagConstraints gbc_titleLabel = new GridBagConstraints();
    gbc_titleLabel.insets = new Insets(0, 0, 5, 5);
    gbc_titleLabel.gridx = 1;
    gbc_titleLabel.gridy = 1;
    contentPane.add(titleLbl, gbc_titleLabel);

    // Creates the instructions label
    // Creates label object with instructions text
    instructionsLbl = new JLabel("Enter search keywords, separated by commas");
    instructionsLbl.setVerticalAlignment(SwingConstants.TOP);
    // Configures positioning within grid bag layout
    GridBagConstraints gbc_instructionsLbl = new GridBagConstraints();
    gbc_instructionsLbl.gridwidth = 3;
    gbc_instructionsLbl.insets = new Insets(0, 0, 5, 5);
    gbc_instructionsLbl.gridx = 1;
    gbc_instructionsLbl.gridy = 2;
    contentPane.add(instructionsLbl, gbc_instructionsLbl);

    // Creates second part of instructions lael
    selectInstructionsLbl = new JLabel("Or select from the dropdown:");
    GridBagConstraints gbc_selectInstructionsLbl = new GridBagConstraints();
    gbc_selectInstructionsLbl.insets = new Insets(0, 0, 5, 5);
    gbc_selectInstructionsLbl.gridx = 1;
    gbc_selectInstructionsLbl.gridy = 3;
    contentPane.add(selectInstructionsLbl, gbc_selectInstructionsLbl);
  }

  private void initializeTextField()
  {
    /*
      Creates and configures text field where user can input keywords.
     */

    // Create keyword text field
    keywordField = new JTextField();

    // Connect keyword field to instructions label
    instructionsLbl.setLabelFor(keywordField);

    // Configure appearance of keyword field
    keywordField.setFont(new Font("Tahoma", Font.PLAIN, 14));

    // Configure positioning of text field within the grid bag layout
    GridBagConstraints gbc_keywordField = new GridBagConstraints();
    gbc_keywordField.gridwidth = 3;
    gbc_keywordField.insets = new Insets(0, 0, 5, 5);
    gbc_keywordField.fill = GridBagConstraints.HORIZONTAL;
    gbc_keywordField.gridx = 1;
    gbc_keywordField.gridy = 4;

    // Add keyword field to content pane
    contentPane.add(keywordField, gbc_keywordField);

    // TODO: Figure out what this does??
    keywordField.setColumns(10);

  }

  private void initializeDropDown()
  {
    /*
      Initializes the dropdown from which user can select from preloaded keywords.
     */

    preloadedList = new JList<>(fillPreloaded());
    // Configures positioning of list within the grid bag layout
    GridBagConstraints gbc_preloadedList = new GridBagConstraints();
    gbc_preloadedList.insets = new Insets(0, 0, 5, 5);
    gbc_preloadedList.fill = GridBagConstraints.BOTH;
    gbc_preloadedList.gridx = 1;
    gbc_preloadedList.gridy = 5;

    // Creates scroll bar
    // Initializes scroll pane
    scrollPane = new JScrollPane();
    // Configures positioning of scroll bar within grid bag layout
    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 1;
    gbc_scrollPane.gridy = 6;
    // Adds scrollbar to content pane
    contentPane.add(scrollPane, gbc_scrollPane);
    // Connects scroll bar to the list of preloaded keywords
    scrollPane.setViewportView(preloadedList);

    // Allows user to select multiple items from the list
    preloadedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

  }

  @Contract(pure = true)
  private String[] fillPreloaded()
  {
    /*
      Fills list of preloaded keywords with keywords (currently hardcoded).
    */

    String[] words = new String[]{"100", "16", "304", "AP", "B", "HGO", "John", "MOB", "MOE",
            "P", "PGO", "SMH", "SMPH", "automatic", "bitch", "boot", "bottom bitch", "bottom girl", "cash", "choose up",
            "daddy", "family", "folks", "hocialize", "hocializing", "hoe", "king", "money", "new bunny", "peace", "queen",
            "renegade", "rose", "square", "stack", "the game", "the life", "track", "trap", "trick",
            "turnout"};

    return words;
  }

  private void initializeButtons()
  {
    /*
      Creates and configures all the buttons
     */

    // Configures GridBagConstraints object that'll be reused for all the objects (for simplicity)
    GridBagConstraints gbc_button = new GridBagConstraints();
    gbc_button.insets = new Insets(0, 0, 5, 5);
    gbc_button.fill = GridBagConstraints.BOTH;
    gbc_button.gridx = 1;

    // Creates search button
    // Initializes the search button with the search text
    searchBtn = new JButton("Search");
    // Configures position of search button within grid bag layout
    gbc_button.gridy = 7;
    // Adds search button to content pane
    contentPane.add(searchBtn, gbc_button);
    searchBtn.addActionListener(this);

    // Creates button for opening a file
    // Initializes button for opening file
    searchFileBtn = new JButton("Open a File");
    // Configures position of file opening button within grid bag layout
    gbc_button.gridy = 16;
    // Adds file opening button to content pane
    contentPane.add(searchFileBtn, gbc_button);
    searchFileBtn.addActionListener(this);

    // Creates button for opening keywords text file
    // Initializes button for opening keywords text file
    textFileBtn = new JButton("Open a Text File with Keywords");
    // Configures position of text file opening button within grid bag layout
    gbc_button.gridy = 17;
    // Adds text file opening button to content pane
    contentPane.add(textFileBtn, gbc_button);
    textFileBtn.addActionListener(this);

    // Creates button for merging files
    // Initializes merging files button
    mergeBtn = new JButton("Merge PDFs");
    // Configures position of merging files button within grid bag layout
    gbc_button.gridy = 18;
    // Adds merging files button to content pane
    contentPane.add(mergeBtn, gbc_button);
    mergeBtn.addActionListener(this);

  }

  private void initializeConsole()
  {
    /*
      Creates console where output messages are printed.
     */

    // Creates console and configures settings
    console = new JTextArea();
    console.setEditable(false);
    console.setLineWrap(true);

    // Creates scroll ar for console
    JScrollPane consoleScrollPane = new JScrollPane(console);
    GridBagConstraints gbc_console = new GridBagConstraints();
    gbc_console.gridheight = 2;
    gbc_console.insets = new Insets(0, 0, 5, 5);
    gbc_console.fill = GridBagConstraints.BOTH;
    gbc_console.gridx = 1;
    gbc_console.gridy = 8;
    contentPane.add(consoleScrollPane, gbc_console);
  }

  public void actionPerformed(ActionEvent event)
  {
    /*
      Controls response to button clicks for each of the buttons.
     */

    /*
      Responds to user clicking the search button with the following steps:
      1. Gets all user-inputted keywords
      2. Gets all user-selected keywords from preloaded list
      3. Searches file for keywords
     */
    if (event.getSource() == searchBtn)
    {
      if (testing) System.out.println("Search button clicked.");
      getInputtedKeywords();
      getSelectedKeywords();
      if (keywords.size() > 0)
      {
        try {
          searchKeywords();
        } catch (IOException e) {
          print(e.getMessage());
          e.printStackTrace();
        }
      }
      else
      {
        print("ERROR: Please select or input keywords.");
      }
    }

    /*
      Responds to the user clicking the button to open PDF file:
      1. Opens file dialog
      2. Retrieves selected file
     */
    if (event.getSource() == searchFileBtn)
    {
      if (testing) System.out.println("Open file button clicked.");
      openSearchFile();
    }

    /*
      Responds to user clicking the button to open a text file:
      1. Opens file dialog
      2. Retrieves selected text file
      3. Reads text file and retrieves keywords found in file
     */
    if (event.getSource() == textFileBtn)
    {
      if (testing) System.out.println("Open text file button clicked.");

        openKeywordsFile();

    }

    /*
      Responds to user clicking the button to merge files:
      1. Opens file dialog
      2. Retrieves files
      3. Merges files
     */
    if (event.getSource() == mergeBtn)
    {
      if (testing) System.out.println("Merge files button clicked.");
       mergePDFFiles();
    }
  }

  private void createSummarySheet(File docName)

  {
    /* Creates a file based on the Office Version Installed
        Input: File (presumably the one inputted originally by user) which will
        have the same name as summary sheet
     */
    // creates file based on Office Version Installed

    int officeVersion;
    officeVersion = getOfficeVersion();

        //this is a .CSV file
        if (officeVersion == 0)
        {
          createCSVFile(docName);
        }

        //this is a .XLS file
        if(officeVersion <= 7 || officeVersion > 83)
        {
          createHSSFFile(docName);
        }

        //this is a .XLSX file
        if(officeVersion > 7)
        {
          createXSSFFile(docName);
        }
  }

  private void writeSummarySheet()
  {
    /*
    Checks which static variable has been written (which sheet type has been
    created), gets the content of hash map then writes it to the sheet
     */
    //checks if any sheet has been created
    if(CSV == null && XSSF == null && HSSF == null)
    {
      if(testing)
      System.out.println("Summary sheet not created when writeSummarySheet " +
              "called");
      createSummarySheet(file);
      writeSummarySheet();
    }

    //else writes content to doc type which isn't null
     else if(CSV != null)
    {
      writeCSVFile();
    }

    else if(XSSF != null)
    {
      writeXSSFFile();
    }

    else if(HSSF != null)
    {
      writeHSSFFile();
    }

  }
  private void writeCSVFile()
  {

  }

  private void writeHSSFFile()
  {

  }

  private void writeXSSFFile()
  {

  }

  private String createHSSFFile(File toBeHSSF)
  {
      /*Creates a file of type .XLS with header
      Input: PDF File with name that is wanted (Name inputted to search)
      Returns: path of HSSF File
      */
      int indexOfPDF = toBeHSSF.getName().lastIndexOf(".pdf");
      File HSSFFile = new File(toBeHSSF.getName().substring(0,indexOfPDF));

      try
      {
          HSSF = WorkbookFactory.create(HSSFFile);
          formatHSSF();
      }
      catch(IOException exception)
      {
          print(exception.getMessage());
          exception.printStackTrace();
      }

      return HSSFFile.getAbsolutePath();
  }

  private void formatHSSF()
  {
      HSSF.createSheet();
      org.apache.poi.ss.usermodel.Font hssfFont = HSSF.createFont();
      hssfFont.setFontName("Tahoma");


  }

  private void createXSSFFile(File toBeXSSF)
  {

  }

  private static void formatXSSF()
  {

  }

  private static void print(String s)
  {
    /*
      Prints a string to the user console and Eclipse console.
     */
    console.setText(currText + s + "\n");
    currText += s + "\n";
    System.out.println(s);
  }

  public void print(ArrayList<String> strings)
  {
    /*
      Prints an ArrayList of strings to the user console and Eclipse console.
     */
    for (String s : strings)
    {
      console.setText(currText + s + "\n");
      currText += s + "\n";
      System.out.println(s);
    }
  }

  public void print(String[] strings)
  {
    /*
      Prints an array of strings to the user console and Eclipse console.
     */
    for (String s : strings)
    {
      console.setText(currText + s + "\n");
      currText += s + "\n";
      System.out.println(s);
    }
  }

  private int getOfficeVersion()
  {
    /* Checks what (if any) version of office is on the system.
       Returns 0 if no office is installed and the version number if office is installed
       The version number is the last two digits of the year that the office package came out (83-Present)
     */

    //TODO: work on making this work regardless of OS (rn windows dependent) and understanding functionality a little bit better
    try{

      //creates process which looks for the office version then reads it, WINDOWS DEPENDENT

      //TODO: look more at process and runtime fcns
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
      String[] fileType = officeVersion.split("=");
      process = Runtime.getRuntime().exec(new String [] {"cmd.exe", "/c", "ftype", fileType[1]});
      officeVersionReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String fileAssociation = officeVersionReader.readLine();
      String fullOfficeVersion = fileAssociation.split("=")[1];
      officeVersion = fullOfficeVersion.replaceAll("[^0-9]+","");
      return Integer.parseInt(officeVersion);

    }
    catch(Exception error){
        print(error.getMessage());
        error.printStackTrace();
    }

    return 0;

  }

  private String createCSVFile(File toBeCSV)
  {
    /* Creates a file of type CSV with header
       Input: PDF File with name that is wanted (Name inputted to search)
       Returns: path of CSV File
     */

    //creates CSV File with same name as inputted PDF file
    int indexOfPDF = toBeCSV.getName().lastIndexOf(".pdf");
    File CSVFile = new File(toBeCSV.getName().substring(0,indexOfPDF));
    CSVFile.setReadable(true);
    CSVFile.setWritable(true);
    CSVFile.setExecutable(true);

    //writes a header to the file
    try
    {
      fileWriter = new FileWriter(CSVFile);
      fileWriter.append("Document Name, Keyword, Page, Line Number, File Path");
      fileWriter.append("\n");
      fileWriter.flush();
    }
    catch(IOException exception)
    {
      exception.printStackTrace();
        print(exception.getMessage());
    }
    //writes the created CSV file to static CSV to be accessed for writing later

    CSV = CSVFile;
    return CSVFile.getAbsolutePath();
  }

  private void getInputtedKeywords()
  {
    /*
      Retrieves user-inputted keywords
     */

    // Gets user-inputted string from text field
    String keywordString = keywordField.getText();

    // Adds each individual inputted keyword to list of keywords
    String[] keywordsArray = keywordString.split(",");
    trim(keywordsArray);
    for (String keyword : keywordsArray)
    {
      if (!keyword.equals(""))
      {
        keywords.add(keyword);
      }
    }

    // Removes white space from keywords
    trim(keywords);
  }

  private void trim(@NotNull String[] arr)
  {
    /*
      Removes white space from words in a String array.
     */
    for (int i = 0; i < arr.length; i++)
    {
      arr[i] = arr[i].trim();
    }
  }

  private void trim(@NotNull ArrayList<String> arr)
  {
    /*
      Removes white space from words in an ArrayList of strings.
     */
    for (int i = 0; i < arr.size(); i++)
    {
      arr.set(i, arr.get(i).trim());
    }
  }

  private void getSelectedKeywords()
  {
    /*
      Retrieves user-selected keywords from preloaded list and adds them to list of keywords to search.
     */

    // Retrieves selected words
    ArrayList<String> selectedKeywords;

    // Adds words to list of keywords
    if (preloadedList.getSelectedValuesList().size() > 0)
    {
      selectedKeywords = (ArrayList<String>)preloadedList.getSelectedValuesList();
      keywords.addAll(selectedKeywords);
    }
  }

  private void searchKeywords() throws IOException
  {
    /*
    1. Extract pages from PDF
    2. Split up pages into increments of 20
    3. Create a thread for each increment (possibly put in ArrayList?)
    4. Start each thread
    */

    // TODO: Implement search keywords method
    createThreads(separateContent());
    runThreads();
  }

  private void runThreads()
  {
    // TODO: Implement run threads method
//    for (Thread t : threads)
//    {
//      t.start();
//    }
//
//    for (Thread t : threads)
//    {
//      t.join();
//    }
  }

  private void createThreads(ArrayList<ArrayList<PDPage>> pageGroups)
  {
    // TODO: Implement create threads method

    try
    {
      if (pageGroups != null) {
        for (ArrayList<PDPage> pageGroup : pageGroups) {
          threads.add(new SearchThread(pageGroup, keywords));
        }
      }
    }
    catch(Exception exception)
    {
      exception.printStackTrace();
      print(exception.getMessage());
    }
  }

  @Nullable
  private ArrayList<ArrayList<PDPage>> separateContent()
  {
    // TODO: put what method does in comment here with inputs & outputs
    // TODO: Implement separate content method
    if (file != null)
    {
        try {
            PDDocument doc = PDDocument.load(file);
            // TODO: why is this textStripper created?
            PDFTextStripper textStripper = new PDFTextStripper();


//      ArrayList<SearchThread> threads = new ArrayList<SearchThread>();
            ArrayList<ArrayList<PDPage>> pageGroups = new ArrayList<>();
            int numGroups = doc.getNumberOfPages() / 20;
            int index = 0;
            for (int i = 0; i < numGroups; i++) {
                ArrayList<PDPage> pageGroup = new ArrayList<>();
                for (index = i * 20; index < i * 20 + 20; index++) {
                    pageGroup.add(doc.getPage(index + 1));
                }
                pageGroups.add(pageGroup);
            }

            ArrayList<PDPage> pageGroup = new ArrayList<>();
            for (int i = index; i < doc.getNumberOfPages(); i++) {
                pageGroup.add(doc.getPage(i));
            }
            pageGroups.add(pageGroup);

            return pageGroups;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
            print(exception.getMessage());
        }
    }
    return null;
  }

  private void openSearchFile()
  {
    /*
      Creates file dialog so user can select a PDF file.
     */

    // Opens file dialog with correct configuration: only PDF files, only one file
    fileDialog = new FileDialog(frame, "Open a PDF File", FileDialog.LOAD);
    fileDialog.setFile("*.pdf");
    fileDialog.setVisible(true);

    // Gets selected file
    if (fileDialog.getFile().contains(".pdf"))

    {
      String path = fileDialog.getDirectory() + fileDialog.getFile();
      file = new File(path);
      print("Selected File: " + file.getName());
    }
  }

  private void openKeywordsFile()
  {
    /*
      Creates file dialog so user can select a text file.
     */

    // Opens file dialog with correct configuration: only text files, only one file
    fileDialog = new FileDialog(frame, "Open Text File with Keywords", FileDialog.LOAD);
    fileDialog.setFile("*.txt");
    fileDialog.setVisible(true);


    //checks that file is inputted
    if(fileDialog.getFile() == null)
    {
        print("Please input a text file");
        if(testing)
        {
            System.out.println("No file inputted when 'Text File with Keywords' " +
                    "button pushed");
        }
    }

    // Gets selected file and reads keywords within file
    if (fileDialog.getFile()!= null && fileDialog.getFile().contains(".txt"))

    {
      keywordsFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
      readKeywords();
    }
    else
    {
        print("Please input a text file");
        if(testing)
        {
            System.out.println("Text file not inputted when 'TextFile with " +
                    "Keywords' button pushed");
        }
    }
  }

  private static void readKeywords()
  {
    /*
      Retrieves keywords from inputted text file and adds keywords to list of keywords.
     */
    try {
        BufferedReader reader = new BufferedReader(new FileReader(keywordsFile));
        String str;
        while ((str = reader.readLine()) != null) {
            keywords.add(str);
        }
        reader.close();
    }
    catch(IOException exception)
    {
        exception.printStackTrace();
        print(exception.getMessage());
    }
  }

  @NotNull
  private String mergePDFFiles()
  {
    /* Takes as many PDF files as provided and merges them into one file
        Input: array of values
        Returns: path of the created file, if exception thrown, returns blank string
     */

    fileDialog = new FileDialog(frame, "Open Files to Merge");
    fileDialog.setFile("*.pdf");
    fileDialog.setMultipleMode(true);
    fileDialog.setVisible(true);
    File[] files = fileDialog.getFiles();

    String fileName = "";

    //creates a name for the file
    for (File file: files) {
      fileName += file.getName().split("pdf");
    }

    //creates file itself
    File mergedFile = makeFile(fileName, ".pdf");

    //checks if file was created successfully and creates a file if not
    try {
      if (!mergedFile.exists())
      {
        mergedFile.createNewFile();
      }
    } catch (IOException exception)
    {
      exception.printStackTrace();
      print(exception.getMessage());
    }

    mergedFile.setWritable(true);
    mergedFile.setReadable(true);

    //creates the documents so that they are accessible
    try {
      PDDocument merged;
      merged = PDDocument.load(files[0]);

      //iterates through the files and adds to first file page by page
      for (int fileNum = 1; fileNum < files.length; fileNum++) {
          PDDocument doc = PDDocument.load(files[fileNum]);

          for (int pageNumber = 0; pageNumber < doc.getNumberOfPages(); pageNumber++) {
              merged.addPage(doc.getPage(pageNumber));
          }
      }
        merged.save(mergedFile);
        merged.close();

        return mergedFile.getAbsolutePath();

    }
    catch(IOException exception)
    {
      exception.printStackTrace();
      print(exception.getMessage());
    }
  return "";
  }

  @NotNull
  @Contract("_, _ -> new")
  private File makeFile(String fileName, String fileExtension)
  {
      /* Creates a file in home directory
         Input: name of file and the extension type
         Returns: newly made File
       */

      return new File(System.getProperty("user.home")+ File.separator + fileName + fileExtension);

  }

}
