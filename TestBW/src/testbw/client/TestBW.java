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
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
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
	
	private TabLayoutPanel tabPanel = new TabLayoutPanel(2.5, Unit.EM);
	
	// Project input section --------------------------------------------------
	private DecoratorPanel projInputDec = new DecoratorPanel();
	private VerticalPanel inputMainVPanel = new VerticalPanel();
	private Label inputFieldsProjectLabel = new Label();
	private VerticalPanel inputFieldsVPanelProject = new VerticalPanel();
	private TextBox dbInputBox = new TextBox();
	private TextBox serverName = new TextBox();
	private TextBox passwordBox = new PasswordTextBox();
	
	// Query parameter input section ------------------------------------------
	private DecoratorPanel queryInputDec = new DecoratorPanel();
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
	
	// Output text (console) area ---------------------------------------------
	private VerticalPanel consoleOutputVPanel = new VerticalPanel();
	private TextArea ta = new TextArea();
	private Label taLabel = new Label();
	
	// Query result: seat distribution ----------------------------------------
	private VerticalPanel distVPanel = new VerticalPanel();
	private PieChart piechart;
	private Label distResultLabel = new Label();
	
	// Query result: Wahlkreis winners ----------------------------------------
	private HorizontalPanel wkHPanel = new HorizontalPanel();
	private VerticalPanel wkErstTableVPanel = new VerticalPanel();
	private VerticalPanel wkZweitTableVPanel = new VerticalPanel();
	private CellTable<TableEntry> wkErstTable = new CellTable<TableEntry>();
	private CellTable<TableEntry> wkZweitTable = new CellTable<TableEntry>();
	private SimplePager wkErstTablePager;
	private SimplePager wkZweitTablePager;
	private ListDataProvider<TableEntry> wkErstTableDataProvider = new ListDataProvider<TableEntry>();
	private ListDataProvider<TableEntry> wkZweitTableDataProvider = new ListDataProvider<TableEntry>();
	
	// Query results: Bundestag members ---------------------------------------
	private VerticalPanel membersTableVPanel = new VerticalPanel();
	private CellTable<TableEntry> membersTable = new CellTable<TableEntry>();
	private SimplePager membersPager;
	private ListDataProvider<TableEntry> membersTableDataProvider = new ListDataProvider<TableEntry>();
	
	// Query results: Ueberhangsmandate ---------------------------------------
	private VerticalPanel mandateTableVPanel = new VerticalPanel();
	private CellTable<TableEntry> mandateTable = new CellTable<TableEntry>();
	private SimplePager mandatePager;
	private ListDataProvider<TableEntry> mandateTableDataProvider = new ListDataProvider<TableEntry>();
	

	// Services ---------------------------------------------------------------
	// ------------------------------------------------------------------------
	private SetupStaticDBServiceAsync setupSvc = GWT.create(SetupStaticDBService.class);	
	private GeneratorServiceAsync generateSvc = GWT.create(GeneratorService.class);
	private LoaderServiceAsync loaderSvc = GWT.create(LoaderService.class);
	private SeatDistributionServiceAsync seatDistSvc = GWT.create(SeatDistributionService.class);
	private WahlkreissiegerServiceAsync wkSiegerSvc = GWT.create(WahlkreissiegerService.class);
	private GetMembersServiceAsync getMembersSvc = GWT.create(GetMembersService.class);
	private GetMandateServiceAsync getMandateSvc = GWT.create(GetMandateService.class);


	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		
		// GUI elements -----------------------------------------------------------
		// ------------------------------------------------------------------------
		
		// Seat distribution --------------------------------------------------
		distVPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		distVPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);	
		
		// Wahlkreis winners --------------------------------------------------
		wkHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		wkHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		wkHPanel.setSpacing(50);
		wkErstTableVPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		wkZweitTableVPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		
		// Bundestag members --------------------------------------------------
		membersTableVPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		membersTableVPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		
		// Ueberhangsmandate --------------------------------------------------
		mandateTableVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		mandateTableVPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

		// Projects input section ---------------------------------------------
		inputFieldsVPanelProject.add(inputFieldsProjectLabel);
		inputFieldsVPanelProject.add(serverName);
		inputFieldsVPanelProject.add(dbInputBox);
		inputFieldsVPanelProject.add(passwordBox);
		projInputDec.add(inputFieldsVPanelProject);
		inputMainVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		inputMainVPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		inputMainVPanel.setSpacing(30);
		inputMainVPanel.add(projInputDec);
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
		queryInputDec.add(inputFieldsVPanelQuery);
		inputFieldsHPanelQuery.add(yearInput);
		inputFieldsHPanelQuery.add(wahlkreisInput);
		inputMainVPanel.add(queryInputDec);
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
		inputMainVPanel.add(controlsVPanel);
		controlsVPanel.setSpacing(5);
		buttonsPanelLabel.setText("Controls");
		
		// Output text (console) area -----------------------------------------
		consoleOutputVPanel.add(taLabel);
		consoleOutputVPanel.add(ta);
		inputMainVPanel.add(consoleOutputVPanel);
		inputMainVPanel.add(outputVPanel);
		consoleOutputVPanel.setBorderWidth(5);
		consoleOutputVPanel.setSpacing(5);
		taLabel.setText("Console Output");
		taLabel.setVisible(true);
		ta.setWidth("500px");
		ta.setHeight("300px");
		
		// Root ----------------------------------------------------------------
		HorizontalPanel inputHolder = new HorizontalPanel();
		inputHolder.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		inputHolder.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		inputHolder.setSize(""+RootLayoutPanel.get().getOffsetWidth()+"px", ""+RootLayoutPanel.get().getOffsetHeight()+"px");
		inputHolder.add(inputMainVPanel);
		tabPanel.setAnimationDuration(500);
		tabPanel.add(inputHolder, "Start");
		RootLayoutPanel.get().add(tabPanel);
		//RootPanel.get("setupDB").add(tabPanel);


		
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
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Setting up database ...");
				setupDB();
			}
		});

		// listen for mouse events on the Generate data button.
		generateButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Generating data ...");
				generateData();
			}
		});
		
		
		// listen for mouse events on the load data button.
		loaderButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Loading data ...");
				loadData();
			}
		});

		// listen for mouse events on the analysis button.
		analysisButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Analyzing data ...");
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
			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {

				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Error setting up the database: " + caught.getMessage());
				//serverMessageLabel.setVisible(true);
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": " + s);
				//serverMessageLabel.setVisible(true);
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
			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {

				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Error generating data: " + caught.getMessage());
				//serverMessageLabel.setVisible(true);
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": " + s);
				//serverMessageLabel.setVisible(true);
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
			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {

				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": Error loading data: " + caught.getMessage());
				//serverMessageLabel.setVisible(true);
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": " + s);
				//serverMessageLabel.setVisible(true);
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

		// Initialize the service proxies.
		if (seatDistSvc == null) {
			seatDistSvc = (SeatDistributionServiceAsync) GWT.create(SeatDistributionService.class);
			ServiceDefTarget target = (ServiceDefTarget) seatDistSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "distribution");
		}
		
		if (wkSiegerSvc == null) {
			wkSiegerSvc = (WahlkreissiegerServiceAsync) GWT.create(WahlkreissiegerService.class);
			ServiceDefTarget target = (ServiceDefTarget) wkSiegerSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "wahlkreissieger");
		}
		
		if (getMembersSvc == null) {
			getMembersSvc = (GetMembersServiceAsync) GWT.create(GetMembersService.class);
			ServiceDefTarget target = (ServiceDefTarget) getMembersSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "members");
		}
		
		if (getMandateSvc == null) {
			getMandateSvc = (GetMandateServiceAsync) GWT.create(GetMandateService.class);
			ServiceDefTarget target = (ServiceDefTarget) getMandateSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "mandate");
		}
		
		
		// Prepare parameters
		String[] projectInput = new String[3];
		String[] queryInput = new String[2];
		projectInput[0] = serverName.getText();
		projectInput[1] = dbInputBox.getText();
		projectInput[2] = passwordBox.getText();
		queryInput[0] = dropList.get(yearInput.getSelectedIndex());
		queryInput[1] = wahlkreisInput.getText();
		
		
		// Setup all required callback objects and 
		// make the call to each respective service.
		((SeatDistributionServiceAsync) seatDistSvc).getSeatDistribution(projectInput, queryInput, setupSeatDistCallback());
		((WahlkreissiegerServiceAsync) wkSiegerSvc).getWahlkreissieger(projectInput, queryInput, setupWKSiegerCallback());
		((GetMembersServiceAsync) getMembersSvc).getMembers(projectInput, queryInput, setupMembersCallback());
		((GetMandateServiceAsync) getMandateSvc).getMandate(projectInput, queryInput, setupMandateCallback());
	}
			
	// Setup callback objects -------------------------------------------------
	// ------------------------------------------------------------------------
	
	// initialize
	public AsyncCallback<Integer> setupInitializeCallback(){
		
		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while initializing analyzer: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(Integer i) {	}
		};
		
		return callback;
	}
	
	// close
	public AsyncCallback<Integer> setupCloseCallback(){
		
		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while closing analyzer: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(Integer i) {	}
		};
		
		return callback;
	}
	
	// seat distribution
	public AsyncCallback< ArrayList<String> > setupSeatDistCallback(){

		AsyncCallback< ArrayList<String> > callback = new AsyncCallback< ArrayList<String> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting seat distribution: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<String> s) {

				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Seat distribtion analysis complete.");

				// Seat distribution ------------------------------------------
				distVPanel.clear();
				piechart = new PieChart(createTable(s), createOptions());
				distVPanel.add(distResultLabel);
				distVPanel.add(piechart);
				HorizontalPanel distTabPanel = new HorizontalPanel();
				distTabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
				distTabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
				distTabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
				distTabPanel.add(distVPanel);
				tabPanel.add(distTabPanel, "Sitzverteilung");
				tabPanel.setVisible(true);

			}
		};

		return callback;
	}

	// wahlkreissieger
	public AsyncCallback< ArrayList<ArrayList<String>> > setupWKSiegerCallback(){


		AsyncCallback< ArrayList<ArrayList<String>> > callback = new AsyncCallback< ArrayList<ArrayList<String>> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting the Wahlkreissieger: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Wahlkreissieger analysis complete.");
				
				ArrayList<String> erststimmen = s.get(0);
				ArrayList<String> zweitstimmen = s.get(1);

				int erstColLength = getDelimLength(erststimmen, "$$");
				int zweitColLength = getDelimLength(zweitstimmen, "$$");

				// Get data in table format
				WKErststimmen = extractRows(erststimmen, erstColLength);
				WKZweitstimmen = extractRows(zweitstimmen, zweitColLength);

				// Add columns
				wkErstTable.addColumn((new TextColumnWrapper(0)).testCol, "Wahlkreis");
				wkErstTable.addColumn((new TextColumnWrapper(1)).testCol, "Kandidatennummer");
				wkErstTable.addColumn((new TextColumnWrapper(2)).testCol, "Anzahl");

				wkZweitTable.addColumn((new TextColumnWrapper(0)).testCol, "Wahlkreis");
				wkZweitTable.addColumn((new TextColumnWrapper(1)).testCol, "Partei");
				wkZweitTable.addColumn((new TextColumnWrapper(2)).testCol, "Anzahl");		    

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
				wkHPanel.clear();
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
				HorizontalPanel wkTabPanel = new HorizontalPanel();
				wkTabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
				wkTabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
				wkTabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
				wkTabPanel.add(wkHPanel);
				tabPanel.add(wkTabPanel, "Wahlkreissieger");
			}
		};

		return callback;
	}

	// members
	public AsyncCallback< ArrayList<String> > setupMembersCallback(){

		AsyncCallback< ArrayList<String> > callback = new AsyncCallback< ArrayList<String> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting Bundestag members: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<String> s) {

				ta.setText(ta.getText() + "\n" + "-> " +
						 DateTimeFormat.getFullTimeFormat().format(new Date()) +": Bundestag members analysis complete.");

			    int membersColLength = getDelimLength(s, "$$");
			    
			    // Get data in table format
				bwMembers = extractRows(s, membersColLength);
		
			    // Add the columns.
			    membersTable.addColumn((new TextColumnWrapper(1).testCol), "Kandidat");
			    membersTable.addColumn((new TextColumnWrapper(1).testCol), "Partei");
			    
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
				HorizontalPanel membersTabPanel = new HorizontalPanel();
				membersTabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
				membersTabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
				membersTabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
				membersTabPanel.add(membersTableVPanel);
				tabPanel.add(membersTabPanel, "Bundestagsmitglieder");
			}
		};

		return callback;
	}
	
	// ueberhangsmandate
	public AsyncCallback< ArrayList<String> > setupMandateCallback(){
		
		AsyncCallback< ArrayList<String> > callback = new AsyncCallback< ArrayList<String> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting Ueberhangsmandate: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<String> s) {
				
				ta.setText(ta.getText() + "\n" + "-> " +
						 DateTimeFormat.getFullTimeFormat().format(new Date()) +": Ueberhangsmandate analysis complete.");

				// todo: fix this
				int umandateColLength;
				if (s.size()==0){
					s.add(" ");
					s.add(" ");
					umandateColLength = 2;
				} else {
					umandateColLength = getDelimLength(s, "$$");
				}
			    
			    // Get data in table format
				umandate = extractRows(s, umandateColLength);
							    
			    // Add the columns.
			    mandateTable.addColumn((new TextColumnWrapper(1).testCol), "Partei");
			    mandateTable.addColumn((new TextColumnWrapper(1).testCol), "Ueberhangsmandate");
			    
			    // Create pagers to control the table.
			    SimplePager.Resources pagerResourcesMandateTable = GWT.create(SimplePager.Resources.class);
			    mandatePager = new SimplePager(TextLocation.CENTER, pagerResourcesMandateTable, true, 3, true);
			    mandatePager.setDisplay(mandateTable);
			    
			    // Set row count
			    mandateTable.setRowCount(umandate.size(), true);
			    
			    // Load data
				mandateTableDataProvider.addDataDisplay(mandateTable);
				mandateTableDataProvider.setList(umandate);
				
				// Add table to UI
				mandateTableVPanel.clear();
				mandateTable.setTitle("Ueberhangsmandate " + dropList.get(yearInput.getSelectedIndex()));
				mandateTableVPanel.add(mandateTable);
				mandateTableVPanel.add(mandatePager);
				HorizontalPanel mandateTabPanel = new HorizontalPanel();
				mandateTabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
				mandateTabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
				mandateTabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
				mandateTabPanel.add(mandateTableVPanel);
				tabPanel.add(mandateTabPanel, "Ueberhangsmandate");
			}
		};

		return callback;
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
        options.setChartArea(chartArea);
        options.setHeight(800);
        options.setWidth(800);
        options.setLegend(LegendPosition.LEFT);
        options.setLineWidth(5);
        options.setTitle("Sitzverteilung " + dropList.get(yearInput.getSelectedIndex()));
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


	
	


	// Tables info --------------------------------------------------------	
	
	/**
	 * A class representing a row of a table
	 */
	public static class TableEntry {
		private final ArrayList<String> cols;
		
		public TableEntry(ArrayList<String> cols){
			this.cols = new ArrayList<String>(cols);
		}
	}
	
	/**
	 *Wrapper for the TextColumn class, extend provided info by a column
	 *number of interest ( = index)
	 */
	public class TextColumnWrapper {
		private int index = -1;
		
		TextColumnWrapper(int index){
			this.index = index;
		}
		
		TextColumn<TableEntry> testCol = new TextColumn<TableEntry>(){
		    @Override
		      public String getValue(TableEntry entry) {
		        return entry.cols.get(index);
		      }
		};
	}
	
	private static List<TableEntry> WKErststimmen;
	private static List<TableEntry> WKZweitstimmen;
	private static List<TableEntry> bwMembers;
	private static List<TableEntry> umandate;
	
	
	
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
		
	public List<TableEntry> extractRows(ArrayList<String> toBeParsed, int colLength){
		
		ArrayList<TableEntry> result = new ArrayList<TableEntry>();
		
		for (int i = 0; i < toBeParsed.size(); i=i+colLength+1){
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++){
				temp.add(toBeParsed.get(i+j));
			}
			result.add(new TableEntry(temp));
		}
		
		return result;
	}
}