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
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
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
	private HorizontalPanel distHPanel = new HorizontalPanel();
	
	// Query result: Wahlkreis winners ----------------------------------------
	private HorizontalPanel wkHPanel = new HorizontalPanel();
	
	// Query result: Bundestag members ---------------------------------------
	private HorizontalPanel membersHPanel = new HorizontalPanel();
	
	// Query result: Ueberhangsmandate ---------------------------------------
	private HorizontalPanel mandateHPanel = new HorizontalPanel();
	
	// Query result: Wahlkreis Overview --------------------------------------
	private HorizontalPanel wkOverviewHPanel = new HorizontalPanel();
	

	// Services ---------------------------------------------------------------
	// ------------------------------------------------------------------------
	private SetupStaticDBServiceAsync setupSvc = GWT.create(SetupStaticDBService.class);	
	private GeneratorServiceAsync generateSvc = GWT.create(GeneratorService.class);
	private LoaderServiceAsync loaderSvc = GWT.create(LoaderService.class);
	private SeatDistributionServiceAsync seatDistSvc = GWT.create(SeatDistributionService.class);
	private WahlkreissiegerServiceAsync wkSiegerSvc = GWT.create(WahlkreissiegerService.class);
	private GetMembersServiceAsync getMembersSvc = GWT.create(GetMembersService.class);
	private GetMandateServiceAsync getMandateSvc = GWT.create(GetMandateService.class);
	private WahlkreisOverviewServiceAsync wkOverviewSvc = GWT.create(WahlkreisOverviewService.class);


	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		
		// GUI elements -----------------------------------------------------------
		// ------------------------------------------------------------------------
		
		// Seat distribution --------------------------------------------------
		distHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		distHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		
		
		// Wahlkreis winners --------------------------------------------------
		wkHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		wkHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		wkHPanel.setSpacing(50);
		
		// Bundestag members --------------------------------------------------
		membersHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		membersHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		
		// Ueberhangsmandate --------------------------------------------------
		mandateHPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		mandateHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		
		// Wahlkreis Overview -------------------------------------------------
		wkOverviewHPanel.setSpacing(50);


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
		serverName.setText("Bundestagswahl");
		dbInputBox.setText("user");
		dbInputBox.setTitle("Enter database name ...");
		passwordBox.setTitle("Enter password ...");
		passwordBox.setText("1234");
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
				ta.setText(ta.getText() + "\n" + "-> "+ 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Setting up database ...");
				setupDB();
			}
		});

		// listen for mouse events on the Generate data button.
		generateButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Generating data ...");
				generateData();
			}
		});
		
		
		// listen for mouse events on the load data button.
		loaderButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Loading data ...");
				loadData();
			}
		});

		// listen for mouse events on the analysis button.
		analysisButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> "+ 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Analyzing data ...");
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

				ta.setText(ta.getText() + "\n" + "-> "+ 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Error setting up the database: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> "+ 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": " + s);
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
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> "+ DateTimeFormat.getFullTimeFormat().format(new Date()) +": " + s);
			}
		};

		// Make the call to the loadData service.
		String[] input = new String[3];
		input[0] = serverName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();

		((LoaderServiceAsync) loaderSvc).loadData(input, callback);
	}


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
		
		if (wkOverviewSvc == null) {
			wkOverviewSvc = (WahlkreisOverviewServiceAsync) GWT.create(WahlkreisOverviewService.class);
			ServiceDefTarget target = (ServiceDefTarget) wkOverviewSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "wahlkreisoverview");
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
		((WahlkreisOverviewServiceAsync) wkOverviewSvc).getWKOverview(projectInput, queryInput, setupWKOverviewCallback());
	}
			
	// Setup callback objects -------------------------------------------------
	// ------------------------------------------------------------------------
	
	
	// seat distribution
	public AsyncCallback< ArrayList<ArrayList<String>> > setupSeatDistCallback(){

		AsyncCallback< ArrayList<ArrayList<String>> > callback = new AsyncCallback< ArrayList<ArrayList<String>> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting seat distribution: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Seat distribution analysis complete.");

				HorizontalPanel htabPanel = new HorizontalPanel();
				htabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
				htabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
				htabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
				
				for (int i = 0; i < s.size(); i=i+2){
					
					ArrayList<String> currentHeader = s.get(i);
					ArrayList<String> currentTable = s.get(i+1);

					int colLength = currentHeader.size()-1;
					
					List<TableEntry> formatted = extractRows(currentTable, colLength);
					
					
					PieChart piechart = new PieChart(createTableForPiechart(formatted, currentHeader), createOptions(currentHeader.get(0)));
					distHPanel.add(piechart);
				}
				
				htabPanel.add(distHPanel);
				tabPanel.add(distHPanel, "Verteilung");
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
				
				setupUITables(s, "Wahlkreissieger", (CellPanel)wkHPanel);
			}
		};

		return callback;
	}

	// members
	public AsyncCallback< ArrayList<ArrayList<String>> > setupMembersCallback(){

		AsyncCallback< ArrayList<ArrayList<String>> > callback = new AsyncCallback< ArrayList<ArrayList<String>> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting Bundestag members: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " +
						 DateTimeFormat.getFullTimeFormat().format(new Date()) +": Bundestag members analysis complete.");

				setupUITables(s, "Bundestagsmitglieder", (CellPanel)membersHPanel);
			}
		};

		return callback;
	}
	
	// ueberhangsmandate
	public AsyncCallback< ArrayList<ArrayList<String>> > setupMandateCallback(){
		
		AsyncCallback< ArrayList<ArrayList<String>> > callback = new AsyncCallback< ArrayList<ArrayList<String>> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting Ueberhangsmandate: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {
				
				ta.setText(ta.getText() + "\n" + "-> " +
						 DateTimeFormat.getFullTimeFormat().format(new Date()) +": Ueberhangsmandate analysis complete.");
				
				setupUITables(s, "Ueberhangsmandate", (CellPanel)mandateHPanel);
			}
		};

		return callback;
	}

	// wahlkreis overview
	public AsyncCallback< ArrayList<ArrayList<String>> > setupWKOverviewCallback(){

		AsyncCallback< ArrayList<ArrayList<String>> > callback = new AsyncCallback< ArrayList<ArrayList<String>> >() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +
						": Error while getting the Wahlkreis overview: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + 
						DateTimeFormat.getFullTimeFormat().format(new Date()) +": Wahlkreis overview complete.");

				setupUITables(s, "Wahlkreisuebersicht", (CellPanel)wkOverviewHPanel);
			}
		};

		return callback;
	}
	
	
	public void setupUITables(ArrayList<ArrayList<String>> s, String tabName, CellPanel layoutPanel){
		
		HorizontalPanel htabPanel = new HorizontalPanel();
		htabPanel.setSize(""+tabPanel.getOffsetWidth()+"px", ""+tabPanel.getOffsetHeight()+"px");
		htabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		htabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		
		for (int i = 0; i < s.size(); i=i+2){
			
			ArrayList<String> currentHeader = s.get(i);
			ArrayList<String> currentTable = s.get(i+1);

			// first element is table name
			int colLength = currentHeader.size()-1;
			
			List<TableEntry> formatted = extractRows(currentTable, colLength);
			CellTable<TableEntry> table = new CellTable<TableEntry>();
			VerticalPanel vPanel = new VerticalPanel();
			vPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			vPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
			
			for (int j=0; j < colLength; j++){
				table.addColumn((new TextColumnWrapper(j)).col, currentHeader.get(j+1));
			}
			
			SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
			SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, true, 3, true);
			pager.setDisplay(table);
			
			table.setRowCount(formatted.size(), true);
			
			ListDataProvider<TableEntry> dataProvider = new ListDataProvider<TableEntry>();
			dataProvider.addDataDisplay(table);
			dataProvider.setList(formatted);
			
			// Add to UI
			vPanel.add(table);
			vPanel.add(pager);
			layoutPanel.add(vPanel);
		}
	
		htabPanel.add(layoutPanel);	
		tabPanel.add(htabPanel, tabName);
		
	}
	
	
	
	// Other methods / static classes -----------------------------------------
	// ------------------------------------------------------------------------

	// Seat distribution ------------------------------------------------------
	/**
	 * Options for pie chart.
	 */
	private PieOptions createOptions(String title) {
		PieOptions options = PieOptions.create();
        ChartArea chartArea = ChartArea.create();
        options.setChartArea(chartArea);
        options.setHeight(RootLayoutPanel.get().getOffsetHeight()/2);
        options.setWidth(RootLayoutPanel.get().getOffsetWidth()/2);
        options.setLegend(LegendPosition.RIGHT);
        options.setLineWidth(5);
        options.setTitle(title);
		options.set3D(true);
		return options;
	}

	/**
	 * Create data source to feed to pie chart.
	 */
	private AbstractDataTable createTableForPiechart(List<TableEntry> fromServer, ArrayList<String> header) {

		DataTable dataTable = DataTable.create();

		dataTable.addColumn(ColumnType.STRING, header.get(1));
		dataTable.addColumn(ColumnType.NUMBER, header.get(2)); 
		dataTable.addRows(fromServer.size());

		for (int i = 0; i < fromServer.size(); i++)
		{
			TableEntry current = fromServer.get(i);
			dataTable.setValue(i, 0, current.cols.get(0));
			dataTable.setValue(i, 1, new Double (current.cols.get(1)).doubleValue());
		}

		return dataTable;
	}

	
	// Cell Tables info --------------------------------------------------------	
	
	/**
	 * A class representing a row of a cell table
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
		
		TextColumn<TableEntry> col = new TextColumn<TableEntry>(){
		    @Override
		      public String getValue(TableEntry entry) {
		        return entry.cols.get(index);
		      }
		};
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