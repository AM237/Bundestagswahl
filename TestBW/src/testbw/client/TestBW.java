package testbw.client;

// Dependencies
import testbw.client.SetupStaticDBService;
import testbw.client.SetupStaticDBServiceAsync;

// Java API
import java.util.ArrayList;
import java.util.Date;

// GWT GUI API
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

// Visualization API
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;


public class TestBW implements EntryPoint {

	// GUI elements
	private VerticalPanel mainVPanel = new VerticalPanel();
	private VerticalPanel inputVPanel = new VerticalPanel();
	private VerticalPanel inputFieldsVPanelProject = new VerticalPanel();
	private VerticalPanel inputFieldsVPanelQuery = new VerticalPanel();
	private Label inputFieldsLabel = new Label();
	private VerticalPanel buttonsVPanel = new VerticalPanel();
	private VerticalPanel outputVPanel = new VerticalPanel();
	private HorizontalPanel buttonsPanel = new HorizontalPanel();
	private Label buttonsPanelLabel = new Label();
	private Button setupDBButton = new Button("Setup");
	private Button generateButton = new Button("Generate");
	private Button loaderButton = new Button("Load");
	private Button analysisButton = new Button("Analyze");
	private TextBox dbInputBox = new TextBox();
	private TextBox projectName = new TextBox();
	private TextBox passwordBox = new PasswordTextBox();
	private TextBox yearInput = new TextBox();
	private TextBox wahlkreisInput = new TextBox();
	private Label resultLabel = new Label();
	private DialogBox dialogBox = new DialogBox();
	private Button closeButton = new Button("Close");
	private VerticalPanel dialogVPanel = new VerticalPanel();
	private PieChart piechart;
	private TextArea ta = new TextArea();
	private Label taLabel = new Label();
	private Button outputClear = new Button("Clear");
	private HorizontalPanel outputLabel = new HorizontalPanel();
	private VerticalPanel generateOutputVert = new VerticalPanel();

	// Services
	private SetupStaticDBServiceAsync setupSvc = GWT.create(SetupStaticDBService.class);	
	private GeneratorServiceAsync generateSvc = GWT.create(GeneratorService.class);
	private LoaderServiceAsync loaderSvc = GWT.create(LoaderService.class);
	private AnalysisServiceAsync analysisSvc = GWT.create(AnalysisService.class);


	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		
		///////////////////////////////////////////////////////////////////////
		// GUI Elements
		///////////////////////////////////////////////////////////////////////
		
		// Analysis pop up ----------------------------------------------------
		dialogBox.addStyleName("dialogBox");
		dialogBox.setText("Analysis Results");
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		closeButton.getElement().setId("closeButton");
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.addStyleName("dialogVPanel");
		dialogBox.add(dialogVPanel);
		

		// Build GUI -----------------------------------------------------------
		inputFieldsVPanelProject.add(inputFieldsLabel);
		inputFieldsVPanelProject.add(projectName);
		inputFieldsVPanelProject.add(dbInputBox);
		inputFieldsVPanelProject.add(passwordBox);
		inputFieldsVPanelQuery.add(yearInput);
		inputFieldsVPanelQuery.add(wahlkreisInput);
		buttonsPanel.add(setupDBButton);
		buttonsPanel.add(generateButton);
		buttonsPanel.add(loaderButton);
		buttonsPanel.add(analysisButton);
		buttonsPanel.add(outputClear);
		buttonsVPanel.add(buttonsPanelLabel);
		buttonsVPanel.add(buttonsPanel);
		buttonsVPanel.add(resultLabel);
		outputLabel.add(taLabel);
		generateOutputVert.add(outputLabel);
		generateOutputVert.add(ta);
		outputVPanel.add(generateOutputVert);
		inputVPanel.add(inputFieldsVPanelProject);
		inputVPanel.add(inputFieldsVPanelQuery);
		mainVPanel.add(inputVPanel);
		mainVPanel.add(buttonsVPanel);
		mainVPanel.add(outputVPanel);
		RootPanel.get("setupDB").add(mainVPanel);
		
		
		// Widget options -----------------------------------------------------
		mainVPanel.setSpacing(30);
		mainVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		
		inputVPanel.setSpacing(10);
		inputFieldsLabel.setText("Inputs");
		projectName.setTitle("Enter Server name ... ");
		dbInputBox.setTitle("Enter database name ...");
		passwordBox.setTitle("Enter password ...");
		yearInput.setTitle("For which year should results be analyzed?");
		wahlkreisInput.setTitle("Give overview for which Wahlkreis (number)?");
		projectName.setFocus(true);
		
		yearInput.setSize("380px", "15px");
		wahlkreisInput.setSize("380px", "15px");
		projectName.setSize("380px", "15px");
		dbInputBox.setSize("380px", "15px");
		passwordBox.setSize("380px", "15px");
		
		buttonsVPanel.setSpacing(20);
		buttonsPanelLabel.setText("Operations");
		resultLabel.setText("Message: ");
		resultLabel.setVisible(true);
		
		
		generateOutputVert.setBorderWidth(5);
		taLabel.setText("Console Output");
		taLabel.setVisible(true);
		ta.setWidth("370px");
		ta.setHeight("300px");
	    
	

		/////////////////////////////////////////////////////////////////////////
		// Handles
		/////////////////////////////////////////////////////////////////////////
		
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		
		// Handler to clear output window
		outputClear.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ta.setText("");
			}
		});

		// listen for mouse events on the SetupDB button.
		setupDBButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resultLabel.setText("Setting up database ...");
				resultLabel.setVisible(true);
				setupDB();
			}
		});

		// listen for mouse events on the Generate data button.
		generateButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resultLabel.setText("Generating data ...");
				resultLabel.setVisible(true);
				generateData();
			}
		});
		
		
		// listen for mouse events on the load data button.
		loaderButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resultLabel.setText("Loading data ...");
				resultLabel.setVisible(true);
				loadData();
			}
		});

		// listen for mouse events on the analysis button.
		analysisButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resultLabel.setText("Analysing data ...");
				resultLabel.setVisible(true);
				getAnalysis();
			}
		});

		/////////////////////////////////////////////////////////////////////////
		// Load visualization API
		/////////////////////////////////////////////////////////////////////////
		
		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {

			}
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

				resultLabel.setText("Error setting up the database: " + caught.getMessage());
				resultLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				resultLabel.setText(s);
				resultLabel.setVisible(true);
			}
		};

		// Make the call to the setupDB service.
		String[] input = new String[3];
		input[0] = projectName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();

		((SetupStaticDBServiceAsync) setupSvc).setupStaticDB(input, callback);

	}
	
	/**
	 * Generates low level data (votes) corresponding to the known 
	 * statistics within each Wahlkreis.
	 */
	private void generateData(){
		
		// Initialize the service proxy.
		if (generateSvc == null) {
			generateSvc = (GeneratorServiceAsync) GWT.create(GeneratorService.class);
			ServiceDefTarget target = (ServiceDefTarget) generateSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "generator");
		}

		// Set up the callback object.
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {

				resultLabel.setText("Error generating data: " + caught.toString());
				resultLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				resultLabel.setText(s);
				resultLabel.setVisible(true);
			}
		};

		// Make the call to the generateData service.
		String[] input = new String[3];
		input[0] = projectName.getText();
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

				resultLabel.setText("Error loading data: " + caught.toString());
				resultLabel.setVisible(true);
			}

			public void onSuccess(String s) {
				resultLabel.setText(s);
				resultLabel.setVisible(true);
			}
		};

		// Make the call to the loadData service.
		String[] input = new String[3];
		input[0] = projectName.getText();
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

				resultLabel.setText("Error while getting analysis: " + caught.getMessage());
				resultLabel.setVisible(true);
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<String> s) {

				resultLabel.setText("Analysis complete.");
				resultLabel.setVisible(true);

				dialogVPanel.clear();
				dialogBox.center();
				dialogBox.setText("Analysis Results: Last update: " +  DateTimeFormat.getShortDateTimeFormat().format(new Date()));

				// Draw pie chart
				piechart = new PieChart(createTable(s), createOptions());
				dialogVPanel.add(piechart);

				dialogVPanel.add(closeButton);
				closeButton.setFocus(true);
			}
		};

		// Make the call to the get analysis service.
		String[] projectInput = new String[5];
		String[] queryInput = new String[2];
		projectInput[0] = projectName.getText();
		projectInput[1] = dbInputBox.getText();
		projectInput[2] = passwordBox.getText();
		queryInput[0] = yearInput.getText();
		queryInput[1] = wahlkreisInput.getText();
		
		((AnalysisServiceAsync) analysisSvc).getSeatDistribution(projectInput, queryInput, callback);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Other methods
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Options for pie chart.
	 */
	private PieOptions createOptions() {
		PieOptions options = PieOptions.create();
		options.setWidth(800);
		options.setHeight(600);
		options.set3D(true);
		options.setTitle("Sitzverteilung " + yearInput.getText());
		return options;
	}

	/**
	 * Create data source to feed to pie chart.
	 */
	private AbstractDataTable createTable(ArrayList<String> fromServer) {

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
}