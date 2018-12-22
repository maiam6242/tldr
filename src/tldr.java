import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

// UI imports
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class tldr implements ActionListener {

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

  private static boolean testing = true;

  public tldr()
  {
    initializeGUI();
  }

  public void initializeGUI() {
    /**
     * Initializes the user interface using Java Swing.
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
    /**
     * Creates and configures the window.
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
    /**
     * Creates and positions all the labels (title label and instruction labels).
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
    /**
     * Creates and configures text field where user can input keywords.
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
    /**
     * Initializes the dropdown from which user can select from preloaded keywords.
     */

    preloadedList = new JList<String>(fillPreloaded());
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

  private String[] fillPreloaded() {
    /**
     * Fills list of preloaded keywords with keywords (currently hardcoded).
    **/

    String[] words = new String[] { "daddy", "folks", "family", "stack", "trap", "B", "king", "queen", "renegade",
            "P", "peace", "money", "100", "rose", "cash", "the life", "the game", "John", "trick", "track",
            "turnout", "square", "304", "16", "HGO", "PGO", "MOB", "MOE", "bitch", "hoe", "bottom bitch",
            "bottom girl", "automatic", "AP", "hocialize", "hocializing", "boot", "new bunny", "SMH", "SMPH",
            "choose up" };

    return words;
  }

  private void initializeButtons()
  {
    /**
     * Creates and configures all the buttons
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
    /**
     * Creates console where output messages are printed.
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

  @Override
  public void actionPerformed(ActionEvent event) {
    /**
     * Controls response to button clicks for each of the buttons.
     */
    if (event.getSource() == searchBtn)
    {
      if (testing) System.out.println("Search button clicked.");
      getInputtedKeywords();
      getSelectedKeywords();
      searchKeywords();
    }

    if (event.getSource() == searchFileBtn)
    {
      if (testing) System.out.println("Open file button clicked.");
      openSearchFile();
    }

    if (event.getSource() == textFileBtn)
    {
      if (testing) System.out.println("Open text file button clicked.");
      openKeywordsFile();
    }

    if (event.getSource() == mergeBtn)
    {
      if (testing) System.out.println("Merge files button clicked.");
      // mergePDFFiles();
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
