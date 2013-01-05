package testbw.client;

// Project dependencies
import testbw.client.SetupStaticDBService;
import testbw.client.SetupStaticDBServiceAsync;
//import testbw.util.Parser;

// Java API
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

// GWT GUI API
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

// Visualization API
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.ChartArea;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;


public class TestBW implements EntryPoint {
	
	// GUI elements -----------------------------------------------------------
	// ------------------------------------------------------------------------
	
	private HorizontalPanel mainHPanel = new HorizontalPanel();
	
	// Project input section --------------------------------------------------
	private VerticalPanel inputMainVPanel = new VerticalPanel();
	private Label inputFieldsProjectLabel = new Label();
	private VerticalPanel inputFieldsVPanelProject = new VerticalPanel();
	private TextBox dbInputBox = new TextBox();
	private TextBox serverName = new TextBox();
	private TextBox passwordBox = new PasswordTextBox();
	
	// Query parameter input section ------------------------------------------
	private VerticalPanel inputFieldsVPanelQuery = new VerticalPanel();
	private HorizontalPanel inputFieldsHPanelQuery = new HorizontalPanel();
	private Label inputFieldsQueryLabel = new Label();
	private ListBox yearInput = new ListBox(false);
	private TextBox wahlkreisInput = new TextBox();
	private List<String> dropList = Arrays.asList("2005", "2009", "2013");
	
	// Controls section -------------------------------------------------------
	private VerticalPanel controlsVPanel = new VerticalPanel();
	private VerticalPanel outputVPanel = new VerticalPanel();
	private HorizontalPanel buttonsHPanel = new HorizontalPanel();
	private Label buttonsPanelLabel = new Label();
	private Button setupDBButton = new Button("Setup");
	private Button generateButton = new Button("Generate");
	private Button loaderButton = new Button("Load");
	private Button analysisButton = new Button("Analyze");
	private Button outputClear = new Button("Clear");
	private Label serverMessageLabel = new Label();
	
	// Output text (console) area ---------------------------------------------
	private VerticalPanel consoleOutputVPanel = new VerticalPanel();
	private TextArea ta = new TextArea();
	private Label taLabel = new Label();

	// Query results section --------------------------------------------------
	private VerticalPanel queryResults1VPanel = new VerticalPanel();
	private VerticalPanel queryResults2VPanel = new VerticalPanel();
	
	// Query result: seat distribution ----------------------------------------
	private VerticalPanel distVPanel = new VerticalPanel();
	private PieChart piechart;
	private Label distResultLabel = new Label();
	
	// Query result: Wahlkreis winners ----------------------------------------
	private HorizontalPanel wkHPanel = new HorizontalPanel();
	private VerticalPanel wkErstTableVPanel = new VerticalPanel();
	private VerticalPanel wkZweitTableVPanel = new VerticalPanel();
	private CellTable<WKTableEntry> wkErstTable = new CellTable<WKTableEntry>();
	private CellTable<WKTableEntry> wkZweitTable = new CellTable<WKTableEntry>();
	private SimplePager wkErstTablePager;
	private SimplePager wkZweitTablePager;
	private ListDataProvider<WKTableEntry> wkErstTableDataProvider = new ListDataProvider<WKTableEntry>();
	private ListDataProvider<WKTableEntry> wkZweitTableDataProvider = new ListDataProvider<WKTableEntry>();
	
	// Query results: Bundestag members ---------------------------------------
	private VerticalPanel membersTableVPanel = new VerticalPanel();
	private VerticalPanel membersVPanel = new VerticalPanel();
	private CellTable<MembersTableEntry> membersTable = new CellTable<MembersTableEntry>();
	private SimplePager membersPager;
	private ListDataProvider<MembersTableEntry> membersTableDataProvider = new ListDataProvider<MembersTableEntry>();
	

	// Services ---------------------------------------------------------------
	// ------------------------------------------------------------------------
	private SetupStaticDBServiceAsync setupSvc = GWT.create(SetupStaticDBService.class);	
	private GeneratorServiceAsync generateSvc = GWT.create(GeneratorService.class);
	private LoaderServiceAsync loaderSvc = GWT.create(LoaderService.class);
	private AnalysisServiceAsync analysisSvc = GWT.create(AnalysisService.class);


	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		
		// GUI elements -----------------------------------------------------------
		// ------------------------------------------------------------------------
		
		// Seat distribution --------------------------------------------------
		distVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		
		
		// Wahlkreis winners --------------------------------------------------
		wkHPanel.setSpacing(20);
		wkHPanel.setBorderWidth(0);
		wkErstTableVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		wkZweitTableVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		
		// Bundestag members --------------------------------------------------
		membersTableVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

		// Projects input section ---------------------------------------------
		inputFieldsVPanelProject.add(inputFieldsProjectLabel);
		inputFieldsVPanelProject.add(serverName);
		inputFieldsVPanelProject.add(dbInputBox);
		inputFieldsVPanelProject.add(passwordBox);
		inputMainVPanel.add(inputFieldsVPanelProject);
		inputMainVPanel.add(controlsVPanel);
		inputMainVPanel.add(outputVPanel);
		inputMainVPanel.setSpacing(30);
		inputMainVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		inputFieldsProjectLabel.setText("Project Inputs");
		inputFieldsQueryLabel.setText("Query Inputs");
		serverName.setTitle("Enter Server name ... ");
		dbInputBox.setTitle("Enter database name ...");
		passwordBox.setTitle("Enter password ...");
		serverName.setFocus(true);
		serverName.setSize("380px", "15px");
		dbInputBox.setSize("380px", "15px");
		passwordBox.setSize("380px", "15px");
		
		// Query parameters input section -------------------------------------
		inputFieldsVPanelQuery.add(inputFieldsQueryLabel);
		inputFieldsVPanelQuery.add(inputFieldsHPanelQuery);
		inputFieldsHPanelQuery.add(yearInput);
		inputFieldsHPanelQuery.add(wahlkreisInput);
		inputMainVPanel.add(inputFieldsVPanelQuery);
		wahlkreisInput.setTitle("Give overview for which Wahlkreis (number)?");
		yearInput.setWidth("100px");
		wahlkreisInput.setWidth("100px");
		for (int i = 0; i < dropList.size(); i++) yearInput.addItem(dropList.get(i));
		
		// Controls section ---------------------------------------------------
		buttonsHPanel.add(setupDBButton);
		buttonsHPanel.add(generateButton);
		buttonsHPanel.add(loaderButton);
		buttonsHPanel.add(analysisButton);
		buttonsHPanel.add(outputClear);
		controlsVPanel.add(buttonsPanelLabel);
		controlsVPanel.add(buttonsHPanel);
		controlsVPanel.add(serverMessageLabel);
		controlsVPanel.setSpacing(5);
		buttonsPanelLabel.setText("Controls");
		serverMessageLabel.setText("Message: ");
		serverMessageLabel.setVisible(true);
		
		// Output text (console) area -----------------------------------------
		consoleOutputVPanel.add(taLabel);
		consoleOutputVPanel.add(ta);
		inputMainVPanel.add(consoleOutputVPanel);
		consoleOutputVPanel.setBorderWidth(5);
		consoleOutputVPanel.setSpacing(5);
		taLabel.setText("Console Output");
		taLabel.setVisible(true);
		ta.setWidth("370px");
		ta.setHeight("300px");

		// All inputs, query results ------------------------------------------
		mainHPanel.add(inputMainVPanel);
		mainHPanel.add(queryResults1VPanel);
		queryResults1VPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		queryResults2VPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		mainHPanel.add(queryResults2VPanel);
		
		// Root ----------------------------------------------------------------
		RootPanel.get("setupDB").add(mainHPanel);
			

		
		// Handles ------------------------------------------------------------
		// --------------------------------------------------------------------
		
		// Handler to clear output window
		outputClear.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ta.setText("");
			}
		});

		// listen for mouse events on the SetupDB button.
		setupDBButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				serverMessageLabel.setText("Setting up database ...");
				serverMessageLabel.setVisible(true);
				setupDB();
			}
		});

		// listen for mouse events on the Generate data button.
		generateButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				serverMessageLabel.setText("Generating data ...");
				serverMessageLabel.setVisible(true);
				generateData();
			}
		});
		
		
		// listen for mouse events on the load data button.
		loaderButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				serverMessageLabel.setText("Loading data ...");
				serverMessageLabel.setVisible(true);
				loadData();
			}
		});

		// listen for mouse events on the analysis button.
		analysisButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				serverMessageLabel.setText("Analysing data ...");
				serverMessageLabel.setVisible(true);
				getAnalysis();
			}
		});
		
		// Load visualization API ---------------------------------------------
		// --------------------------------------------------------------------
		
		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {}
		};

		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, CoreChart.PACKAGE);
	}


	/**
	 * Sets up a database with required relations and other static data.
	 */
	private void setupDB(){

		// Initialize the service proxy.
		if (setupSvc == null) {
			setupSvc = (SetupStaticDBServiceAsync) GWT.create(SetupStaticDBService.class);
			ServiceDefTarget target = (ServiceDefTarget) setupSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "setupStaticDB");
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {

				serverMessageLabel.setText("Error setting up the database: " + caught.getMessage());
				serverMessageLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				serverMessageLabel.setText(s);
				serverMessageLabel.setVisible(true);
			}
		};

		// Make the call to the setupDB service.
		String[] input = new String[3];
		input[0] = serverName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();

		((SetupStaticDBServiceAsync) setupSvc).setupStaticDB(input, callback);

	}
	
	/**
	 * Generates low level data (votes) corresponding to the known 
	 * statistics within each Wahlkreis.
	 */
	public void generateData(){
		
		// Initialize the service proxy.
		if (generateSvc == null) {
			generateSvc = (GeneratorServiceAsync) GWT.create(GeneratorService.class);
			ServiceDefTarget target = (ServiceDefTarget) generateSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "generator");
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {

				serverMessageLabel.setText("Error generating data: " + caught.toString());
				serverMessageLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				serverMessageLabel.setText(s);
				serverMessageLabel.setVisible(true);
			}
		};

		// Make the call to the generateData service.
		String[] input = new String[3];
		input[0] = serverName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();

		((GeneratorServiceAsync) generateSvc).generateData(input, callback);
	}
	
	
	/**
	 * Load generated data into the database, add constraints.
	 */
	private void loadData(){
		
		// Initialize the service proxy.
		if (loaderSvc == null) {
			loaderSvc = (LoaderServiceAsync) GWT.create(LoaderService.class);
			ServiceDefTarget target = (ServiceDefTarget) loaderSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "loader");
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {

				serverMessageLabel.setText("Error loading data: " + caught.toString());
				serverMessageLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				serverMessageLabel.setText(s);
				serverMessageLabel.setVisible(true);
			}
		};

		// Make the call to the loadData service.
		String[] input = new String[3];
		input[0] = serverName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();

		((LoaderServiceAsync) loaderSvc).loadData(input, callback);
	}



	/**
	 * Computes the distribution of seats in the Bundestag (based on Zweitstimmen).
	 */
	private void getAnalysis(){

		// Initialize the service proxy.
		if (analysisSvc == null) {
			analysisSvc = (AnalysisServiceAsync) GWT.create(AnalysisService.class);
			ServiceDefTarget target = (ServiceDefTarget) analysisSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "analysis");
		}


		// Set up the callback object.
		AsyncCallback< ArrayList<String> > callback = new AsyncCallback< ArrayList<String> >() {
			public void onFailure(Throwable caught) {

				serverMessageLabel.setText("Error while getting analysis: " + caught.getMessage());
				serverMessageLabel.setVisible(true);
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<String> s) {
				
				ArrayList<ArrayList<String>> parsed = parse(s, "##");
			
				serverMessageLabel.setText("Analysis complete: " + DateTimeFormat.getShortDateTimeFormat().format(new Date()));
				serverMessageLabel.setVisible(true);

				// Seat distribution ------------------------------------------
				distVPanel.clear();
				piechart = new PieChart(createTable(parsed.get(0)), createOptions());
				distVPanel.add(distResultLabel);
				distVPanel.add(piechart);
				queryResults1VPanel.remove(distVPanel);
				queryResults1VPanel.add(distVPanel);

							
				// Wahlkreissieger --------------------------------------------
				//Parser parser =  new Parser();
				ArrayList<String> wks = new ArrayList<String>(parsed.get(1));
				ArrayList<ArrayList<String>> wksTables = parse(wks, "&&");
				ArrayList<String> erststimmen = wksTables.get(0);
				ArrayList<String> zweitstimmen = wksTables.get(1);
				
				int erstColLength = getDelimLength(erststimmen, "$$");
				int zweitColLength = getDelimLength(zweitstimmen, "$$");
				
				// Get data in table format
				WKErststimmen = extractRows(erststimmen, erstColLength);
				WKZweitstimmen = extractRows(zweitstimmen, zweitColLength);
				
			    // Create table columns
			    TextColumn<WKTableEntry> wahlkreisColumn = new TextColumn<WKTableEntry>() {
			      @Override
			      public String getValue(WKTableEntry entry) {
			        return entry.wahlkreis;
			      }
			    };
			    TextColumn<WKTableEntry> idColumn = new TextColumn<WKTableEntry>() {
			      @Override
			      public String getValue(WKTableEntry entry) {
			        return entry.identifier;
			      }
			    };
			    TextColumn<WKTableEntry> quantColumn = new TextColumn<WKTableEntry>() {
			      @Override
			      public String getValue(WKTableEntry entry) {
			        return entry.quantity;
			      }
			    };
			    wahlkreisColumn.setSortable(true);
			    idColumn.setSortable(true);
			    quantColumn.setSortable(true);
			    
			    // Add the columns.
			    wkErstTable.addColumn(wahlkreisColumn, "Wahlkreis");
			    wkErstTable.addColumn(idColumn, "Kandidatennummer");
			    wkErstTable.addColumn(quantColumn, "Anzahl");
			    
			    wkZweitTable.addColumn(wahlkreisColumn, "Wahlkreis");
			    wkZweitTable.addColumn(idColumn, "Partei");
			    wkZweitTable.addColumn(quantColumn, "Anzahl");
				
			    // Create pagers to control the tables.
			    SimplePager.Resources pagerResourcesErstTable = GWT.create(SimplePager.Resources.class);
			    SimplePager.Resources pagerResourcesZweitTable = GWT.create(SimplePager.Resources.class);
			    wkErstTablePager = new SimplePager(TextLocation.CENTER, pagerResourcesErstTable, true, 3, true);
			    wkZweitTablePager = new SimplePager(TextLocation.CENTER, pagerResourcesZweitTable, true, 3, true);
			    wkErstTablePager.setDisplay(wkErstTable);
			    wkZweitTablePager.setDisplay(wkZweitTable);
			    
			    // Set the total row count. This isn't strictly necessary, but it affects
			    // paging calculations, so its good habit to keep the row count up to date.
			    wkErstTable.setRowCount(WKErststimmen.size(), true);
			    wkZweitTable.setRowCount(WKZweitstimmen.size(), true);
			    
			    // Load data
				wkErstTableDataProvider.addDataDisplay(wkErstTable);
				wkErstTableDataProvider.setList(WKErststimmen);
				wkZweitTableDataProvider.addDataDisplay(wkZweitTable);
				wkZweitTableDataProvider.setList(WKZweitstimmen);
				
				// Add table to UI
				wkErstTableVPanel.clear();
				wkZweitTableVPanel.clear();
				wkErstTable.setTitle("Wahlkreissieger " + dropList.get(yearInput.getSelectedIndex()) + ": Kandidaten");
				wkZweitTable.setTitle("Wahlkreissieger " + dropList.get(yearInput.getSelectedIndex()) + ": Parteien");
				wkErstTableVPanel.add(wkErstTable);
				wkErstTablePager.setPageSize(15);
				wkZweitTablePager.setPageSize(15);
				wkErstTableVPanel.add(wkErstTablePager);
				wkZweitTableVPanel.add(wkZweitTable);
				wkZweitTableVPanel.add(wkZweitTablePager);
			    wkHPanel.add(wkErstTableVPanel);
			    wkHPanel.add(wkZweitTableVPanel);
			    queryResults2VPanel.add(wkHPanel);
			    
			    
			    // Bundestag members ------------------------------------------
			    // ------------------------------------------------------------
			    
			    ArrayList<String> members = new ArrayList<String>(parsed.get(2));
			    
			    for (int i = 0; i < members.size(); i++)
			    	System.out.println(members.get(i));
			    
			    int membersColLength = getDelimLength(members, "$$");
			    
			    // Get data in table format
				bwMembers = extractRowsMem(members, membersColLength);
				
			    // Create table columns
			    TextColumn<MembersTableEntry> candColumn = new TextColumn<MembersTableEntry>() {
			      @Override
			      public String getValue(MembersTableEntry entry) {
			        return entry.candNum;
			      }
			    };
			    TextColumn<MembersTableEntry> partyColumn = new TextColumn<MembersTableEntry>() {
			      @Override
			      public String getValue(MembersTableEntry entry) {
			        return entry.party;
			      }
			    };

			    candColumn.setSortable(true);
			    partyColumn.setSortable(true);
			    
			    // Add the columns.
			    membersTable.addColumn(candColumn, "Kandidat");
			    membersTable.addColumn(partyColumn, "Partei");
			    
			    // Create pagers to control the table.
			    SimplePager.Resources pagerResourcesMembersTable = GWT.create(SimplePager.Resources.class);
			    membersPager = new SimplePager(TextLocation.CENTER, pagerResourcesMembersTable, true, 3, true);
			    membersPager.setDisplay(membersTable);
			    
			    // Set row count
			    membersTable.setRowCount(bwMembers.size(), true);
			    
			    // Load data
				membersTableDataProvider.addDataDisplay(membersTable);
				membersTableDataProvider.setList(bwMembers);
				
				// Add table to UI
				membersTableVPanel.clear();
				membersTable.setTitle("Bundestagmitglieder " + dropList.get(yearInput.getSelectedIndex()));
				membersTableVPanel.add(membersTable);
				membersTableVPanel.add(membersPager);
				queryResults2VPanel.add(membersTableVPanel);
			}
		};

		// Make the call to the get analysis service.
		String[] projectInput = new String[5];
		String[] queryInput = new String[2];
		projectInput[0] = serverName.getText();
		projectInput[1] = dbInputBox.getText();
		projectInput[2] = passwordBox.getText();
		queryInput[0] = dropList.get(yearInput.getSelectedIndex());
		queryInput[1] = wahlkreisInput.getText();
		
		((AnalysisServiceAsync) analysisSvc).analyze(projectInput, queryInput, callback);
	}

	
	// Other methods / static classes -----------------------------------------
	// ------------------------------------------------------------------------

	// Seat distribution ------------------------------------------------------
	/**
	 * Options for pie chart.
	 */
	private PieOptions createOptions() {
		PieOptions options = PieOptions.create();
	
        ChartArea chartArea = ChartArea.create();
        chartArea.setTop(50);
        chartArea.setHeight(400);
        chartArea.setWidth(400);
        chartArea.setLeft(50);
        options.setChartArea(chartArea);
        options.setHeight(400);
        options.setLegend(LegendPosition.RIGHT);
        options.setLineWidth(5);
        options.setTitle("Sitzverteilung " + dropList.get(yearInput.getSelectedIndex()));
        options.setWidth(400);
		options.set3D(true);
		return options;
	}

	/**
	 * Create data source to feed to pie chart.
	 */
	private AbstractDataTable createTable(List<String> fromServer) {

		DataTable dataTable = DataTable.create();

		dataTable.addColumn(ColumnType.STRING, "Partei");
		dataTable.addColumn(ColumnType.NUMBER, "Anteil"); 
		dataTable.addRows(fromServer.size());

		for (int i = 0; i < fromServer.size(); i=i+2)
		{
			dataTable.setValue(i, 0, fromServer.get(i));
			dataTable.setValue(i, 1, new Double (fromServer.get(i+1)).doubleValue());
		}

		return dataTable;
	}

	// Wahlkreissieger --------------------------------------------------------
	
	public static class TableEntry { }
	
	// Data type representing an entry in the output tables
	public static class WKTableEntry extends TableEntry {
		private final String wahlkreis;
		private final String identifier;
		private final String quantity;

		public WKTableEntry(String wk, String id, String quant) {
			this.wahlkreis = wk;
			this.identifier = id;
			this.quantity = quant;
		}
		
		public WKTableEntry(ArrayList<String> properties){
			this.wahlkreis = properties.get(0);
			this.identifier = properties.get(1);
			this.quantity = properties.get(2);
		}
	}

	// Data to be modeled in the tables
	private static List<WKTableEntry> WKErststimmen;
	private static List<WKTableEntry> WKZweitstimmen;
	
	
	// Bundestag members -----------------------------------------------------
	
	// Data type representing an entry in the output table
	public static class MembersTableEntry extends TableEntry {
		private final String candNum;
		private final String party;

		public MembersTableEntry(String candNum, String party) {
			this.candNum = candNum;
			this.party = party;
		}
		
		public MembersTableEntry(ArrayList<String> properties){
			this.candNum = properties.get(0);
			this.party = properties.get(1);
		}
	}
	
	// Data to be modeled in the tables
	private static List<MembersTableEntry> bwMembers;
	
	// Parser -----------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Parse string returned from server for analysis.
	 * Splits an ArrayList<String> along the given delimiter,
	 * result is an ArrayList of Lists
	 * @param s - input list
	 * @param delim - delimiter string
	 * @return see above
	 */
	private ArrayList<ArrayList<String>> parse(ArrayList<String> s, String delim){
		
		int counter = 0;
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		
		List<String> temp = s;
		while(temp.indexOf(delim) != -1){
			list.add(new ArrayList<String>());
			temp = temp.subList(temp.indexOf(delim)+1, temp.size());
		}
		list.add(new ArrayList<String>());
		
		for (int i = 0; i < s.size(); i++){
			if (s.get(i).equals(delim)) {
				counter++;
				continue;
			}
			list.get(counter).add(s.get(i));
		}
		return list;
	}
	
	/**
	 * Returns the length of the lists of strings between the given delimiter
	 * @param s - input list
	 * @param delim - delimiter string
	 * @return -1 if length inconsistent
	 */
	private int getDelimLength(ArrayList<String> s, String delim){
		
		ArrayList<Integer> counters = new ArrayList<Integer>();
		
		int counter = 0;
		for (int i = 0; i < s.size(); i++){
			if (!s.get(i).equals(delim)){
				counter++;
			} else {
				counters.add(new Integer(counter));
				counter = 0;
				continue;
			}
		}
		
		HashSet<Integer> set = new HashSet<Integer>(counters);
		if (set.size() > 1) return -1;
		return counters.get(0);
	}
	
	// todo: clean this up
	public List<WKTableEntry> extractRows(ArrayList<String> toBeParsed, int colLength){
		
		ArrayList<WKTableEntry> result = new ArrayList<WKTableEntry>();
		
		for (int i = 0; i < toBeParsed.size(); i=i+colLength+1){
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++){
				temp.add(toBeParsed.get(i+j));
			}
			result.add(new WKTableEntry(temp));
		}
		
		return result;
	}
	
	
	public List<MembersTableEntry> extractRowsMem(ArrayList<String> toBeParsed, int colLength){
		
		ArrayList<MembersTableEntry> result = new ArrayList<MembersTableEntry>();
		
		for (int i = 0; i < toBeParsed.size(); i=i+colLength+1){
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++){
				temp.add(toBeParsed.get(i+j));
			}
			result.add(new MembersTableEntry(temp));
		}
		
		return result;
	}
}