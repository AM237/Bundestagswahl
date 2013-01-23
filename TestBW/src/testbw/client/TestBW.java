package testbw.client;

// Project dependencies
//import testbw.client.SetupStaticDBService;
//import testbw.client.SetupStaticDBServiceAsync;

// Java API
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import testbw.util.InputDirectory;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.ChartArea;
import com.google.gwt.visualization.client.CommonChartOptions;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.Selectable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.OnMouseOverHandler;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.Visualization;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.sun.corba.se.pept.transport.ContactInfo;
// GWT GUI API
// Visualization API

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
	private TextBox wahlkreisVote = new TextBox();
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
	private Button requestVoteButton = new Button("Request Vote Form");
	private Button outputClear = new Button("Clear");

	// Output text (console) area ---------------------------------------------
	private VerticalPanel consoleOutputVPanel = new VerticalPanel();
	private TextArea ta = new TextArea();
	private Label taLabel = new Label();

	// Query result: seat distribution ----------------------------------------
	private AbsolutePanel distPanel = new AbsolutePanel();
	private HashMap<String, Color> colorMap = new HashMap<String, Color>();

	// Query result: Wahlkreis winners ----------------------------------------
	private HorizontalPanel wkHPanel = new HorizontalPanel();

	// Query result: Bundestag members ----------------------------------------
	private HorizontalPanel membersHPanel = new HorizontalPanel();

	// Query result: Ueberhangsmandate ----------------------------------------
	private HorizontalPanel mandateHPanel = new HorizontalPanel();

	// Query result: Wahlkreis Overview ---------------------------------------
	private HorizontalPanel wkOverviewHPanel = new HorizontalPanel();

	// Query result: Knappster Sieger Overview --------------------------------
	private HorizontalPanel knappsterSiegerHPanel = new HorizontalPanel();

	// Query result: Wahlkreis Overview (Erststimmen )-------------------------
	private HorizontalPanel wkOverviewErststimmenHPanel = new HorizontalPanel();

	// Submit vote panel ------------------------------------------------------
	private VerticalPanel submitVotePanel = new VerticalPanel();
	private Button submitVoteButton = new Button("Stimme abgeben");

	// Services ---------------------------------------------------------------

	/*
	 * Dependencies:
	 * 
	 * 1. * -> seatDistSvc 2. getMembersSvc -> get Wahlkreissieger
	 */
	private SetupStaticDBServiceAsync setupSvc = GWT.create(SetupStaticDBService.class);
	private GeneratorServiceAsync generateSvc = GWT.create(GeneratorService.class);
	private LoaderServiceAsync loaderSvc = GWT.create(LoaderService.class);
	private SeatDistributionServiceAsync seatDistSvc = GWT.create(SeatDistributionService.class);
	private WahlkreissiegerServiceAsync wkSiegerSvc = GWT.create(WahlkreissiegerService.class);
	private GetMembersServiceAsync getMembersSvc = GWT.create(GetMembersService.class);
	private GetMandateServiceAsync getMandateSvc = GWT.create(GetMandateService.class);
	private WahlkreisOverviewServiceAsync wkOverviewSvc = GWT.create(WahlkreisOverviewService.class);
	private GetKnappsterSiegerServiceAsync knappsterSiegerSvc = GWT.create(GetKnappsterSiegerService.class);
	private WKOverviewErststimmenServiceAsync wkOverviewErststimmenSvc = GWT.create(WKOverviewErststimmenService.class);
	private RequestVoteServiceAsync requestVoteSvc = GWT.create(RequestVoteService.class);
	private SubmitVoteServiceAsync submitVoteSvc = GWT.create(SubmitVoteService.class);

	// Other global variables -------------------------------------------------
	Images images = GWT.create(Images.class);
	String[] projectInput = new String[3];
	String[] queryInput = new String[2];

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		// TODO: where to put this info?
		colorMap.put("CS", new Color("skyblue", "#6CA5BC", Arrays.asList(0x00c0ff, 0x00a0ff, 0x0080ff, 0x0060ff, 0x0040ff, 0x0020ff, 0x0000ff)));
		colorMap.put("CD", new Color("slateblue", "#4A3F90", Arrays.asList(0x1e174c, 0x362988, 0x4f3cc4, 0x6a5acd, 0x8578d6, 0xa096df, 0xd7d2f1, 0xf2f1fb)));
		colorMap.put("SP", new Color("crimson", "#9A0E2A", Arrays.asList(0x6B0000, 0xBA0000, 0xDB0000, 0xFE2020, 0xFE3939, 0xFE8484, 0xFE8484)));
		colorMap.put("DI", new Color("plum", "#9B709B", Arrays.asList(0xff4565, 0xff5e7a, 0xff788f, 0xff91a4, 0xffabb9, 0xffc4ce, 0xffdee3)));
		colorMap.put("GR", new Color("mediumseagreen", "#246B44", Arrays.asList(0x1b5233, 0x21653f, 0x28784b, 0x2e8b57, 0x349e63, 0x3bb16f, 0x44c17b)));
		colorMap.put("FD", new Color("gold", "#CCAC00", Arrays.asList(0x625300, 0xb39700, 0xccac00, 0xe6c200, 0xffd700, 0xffe34e, 0xfff09d)));
		colorMap.put("PI", new Color("orange", "#CC8400", Arrays.asList(0x892f00, 0xb13c00, 0xd84a00, 0xff5700, 0xff7127, 0xff8b4e, 0xffa576)));

		// GUI elements
		// -----------------------------------------------------------
		// ------------------------------------------------------------------------

		// Seat distribution --------------------------------------------------
		distPanel.setSize("" + RootLayoutPanel.get().getOffsetWidth() + "px", "" + RootLayoutPanel.get().getOffsetHeight() + "px");
		// distPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		// distPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

		// Wahlkreis winners --------------------------------------------------
		wkHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		wkHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		wkHPanel.setSpacing(50);

		// Bundestag members --------------------------------------------------
		membersHPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		membersHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		membersHPanel.setSpacing(50);

		// Ueberhangsmandate --------------------------------------------------
		mandateHPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		mandateHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		mandateHPanel.setSpacing(50);

		// Wahlkreis Overview -------------------------------------------------
		wkOverviewHPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		wkOverviewHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		wkOverviewHPanel.setSpacing(50);

		// Knappster Sieger Overview ------------------------------------------
		knappsterSiegerHPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		knappsterSiegerHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		knappsterSiegerHPanel.setSpacing(50);

		// Wahlkreis Overview (Erststimmen) -----------------------------------
		wkOverviewErststimmenHPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		wkOverviewErststimmenHPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		wkOverviewErststimmenHPanel.setSpacing(50);

		// Voting form --------------------------------------------------------
		submitVotePanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		submitVotePanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		submitVotePanel.setSize("" + RootLayoutPanel.get().getOffsetWidth() + "px", "" + RootLayoutPanel.get().getOffsetHeight() + "px");
		// submitVotePanel.setSpacing(30);

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
		inputFieldsHPanelQuery.add(wahlkreisVote);
		inputMainVPanel.add(queryInputDec);
		wahlkreisInput.setTitle("Give overview for which Wahlkreis (number)?");
		wahlkreisVote.setTitle("Vote in which Wahlkreis (number)?");
		yearInput.setWidth("100px");
		wahlkreisInput.setWidth("100px");
		wahlkreisVote.setWidth("100px");
		for (int i = 0; i < dropList.size(); i++)
			yearInput.addItem(dropList.get(i));

		// Controls section ---------------------------------------------------
		buttonsHPanel.add(setupDBButton);
		buttonsHPanel.add(generateButton);
		buttonsHPanel.add(loaderButton);
		buttonsHPanel.add(analysisButton);
		buttonsHPanel.add(requestVoteButton);
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
		ta.setWidth(RootLayoutPanel.get().getOffsetWidth() / 2 + "px");
		ta.setHeight("300px");

		// Root ----------------------------------------------------------------
		HorizontalPanel inputHolder = new HorizontalPanel();
		inputHolder.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		inputHolder.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		inputHolder.setSize("" + RootLayoutPanel.get().getOffsetWidth() + "px", "" + RootLayoutPanel.get().getOffsetHeight() + "px");
		inputHolder.add(inputMainVPanel);
		tabPanel.setAnimationDuration(500);
		tabPanel.add(inputHolder, "Start");
		// tabPanel.setStyleName("gwt-TabPanel");
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
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Setting up database ...");
				setupDB();
			}
		});

		// listen for mouse events on the Generate data button.
		generateButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Generating data ...");
				generateData();
			}
		});

		// listen for mouse events on the load data button.
		loaderButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Loading data ...");
				loadData();
			}
		});

		// listen for mouse events on the analysis button.
		analysisButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Analyzing data ...");
				getAnalysis();
			}
		});

		// listen for mouse events on the request vote form button.
		requestVoteButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Requesting vote form ...");
				requestVoteForm();
			}
		});

		// Load visualization API ---------------------------------------------
		// --------------------------------------------------------------------

		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {
			}
		};

		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, CoreChart.PACKAGE);
		VisualizationUtils.loadVisualizationApi(onLoadCallback, GeoChart.PACKAGE);
	}

	/**
	 * Sets up a database with required relations and other static data.
	 */
	private void setupDB() {

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

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error setting up the database: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": " + s);
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
	 * Generates low level data (votes) corresponding to the known statistics
	 * within each Wahlkreis.
	 */
	public void generateData() {

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
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": " + "Error generating data: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": " + s);

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
	private void loadData() {

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

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error loading data: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(String s) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": " + s);
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
	 * Analyzes the data in the database according to the analysis services
	 * provided (see above)
	 */
	private void getAnalysis() {

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

		if (knappsterSiegerSvc == null) {
			knappsterSiegerSvc = (GetKnappsterSiegerServiceAsync) GWT.create(GetKnappsterSiegerService.class);
			ServiceDefTarget target = (ServiceDefTarget) knappsterSiegerSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "knappsterSieger");
		}

		if (wkOverviewErststimmenSvc == null) {
			wkOverviewErststimmenSvc = (WKOverviewErststimmenServiceAsync) GWT.create(WKOverviewErststimmenService.class);
			ServiceDefTarget target = (ServiceDefTarget) wkOverviewErststimmenSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "wkOverviewErststimmen");
		}

		// Prepare parameters
		projectInput[0] = serverName.getText();
		projectInput[1] = dbInputBox.getText();
		projectInput[2] = passwordBox.getText();
		queryInput[0] = dropList.get(yearInput.getSelectedIndex());
		queryInput[1] = wahlkreisInput.getText();

		// Setup all required callback objects and
		// make the call to each respective service.
		// All other services require this one to finish first, so delay their
		// asynchronous calls to the callback for this service.
		ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Seat distribution analysis started.");

		((SeatDistributionServiceAsync) seatDistSvc).getSeatDistribution(projectInput, queryInput, setupSeatDistCallback());
	}

	/**
	 * Request a vote form and binds it to the UI
	 */
	private void requestVoteForm() {

		// Initialize the service proxy.
		if (requestVoteSvc == null) {
			requestVoteSvc = (RequestVoteServiceAsync) GWT.create(RequestVoteService.class);
			ServiceDefTarget target = (ServiceDefTarget) requestVoteSvc;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + "requestVote");
		}

		// TODO: manage inputs

		// Make the call to the request vote service.
		String[] input = new String[3];
		String[] query = new String[3];
		input[0] = serverName.getText();
		input[1] = dbInputBox.getText();
		input[2] = passwordBox.getText();
		query[0] = dropList.get(yearInput.getSelectedIndex());
		query[1] = wahlkreisVote.getText();

		// Request voting form.
		((RequestVoteServiceAsync) requestVoteSvc).requestVote(input, query, setupRequestVoteCallback());

	}

	// Setup callback objects -------------------------------------------------
	// ------------------------------------------------------------------------

	// seat distribution
	public AsyncCallback<ArrayList<ArrayList<String>>> setupSeatDistCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting seat distribution: " + caught.getMessage());

			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Seat distribution analysis complete.");

				// Hold all map widgets in this panel
				final DeckPanel dpanel = new DeckPanel();
				dpanel.setSize(RootLayoutPanel.get().getOffsetWidth() + "px", RootLayoutPanel.get().getOffsetHeight() + "px");

				// Hold all piechart widgets in this panel
				VerticalPanel pcharts = new VerticalPanel();
				pcharts.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
				pcharts.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

				// Dummy (empty) start GeoChart
				GeoChart.Options opts = GeoChart.Options.create();
				DataTable dt = DataTable.create();
				dt.addColumn(ColumnType.STRING, "Bundesland");
				dt.addColumn(ColumnType.NUMBER, "Votes");
				dt.addRows(1);
				opts.setRegion("DE");
				opts.setResolution(GeoChart.RESOLUTION.PROVINCES);
				opts.setHeight((int) (RootLayoutPanel.get().getOffsetHeight() * 0.95));
				opts.setWidth((int) (RootLayoutPanel.get().getOffsetWidth()));
				opts.keepAspectRatio(false);
				GeoChart gmap = new GeoChart(dt, opts);

				distPanel.add(gmap, 0, 0);

				// Loop over all incoming table sections (1 section per
				// piechart)
				for (int i = 0; i < s.size(); i = i + 4) {

					final int index = i;

					// Get infos for current section
					final ArrayList<String> currentHeader = s.get(i);
					final ArrayList<String> currentTable = s.get(i + 1);
					final ArrayList<String> currentMapHeader = s.get(i + 2);
					final ArrayList<String> currentMapTable = s.get(i + 3);

					// Format data
					final List<TableEntry> formatted = parseToTableEntry(currentTable, currentHeader.size() - 1);
					final List<TableEntry> formattedMap = parseToTableEntry(currentMapTable, currentMapHeader.size() - 1);

					// Build pie chart
					final AbstractDataTable pieChartData = createTableForPiechart(formatted, currentHeader);
					final PieChart piechart = new PieChart(pieChartData, createOptions(currentHeader.get(0), formatted));

					// Build Geo chart
					// ////////////////////////////////////////////////////////////

					final HashMap<String, Integer> tablesMap = new HashMap<String, Integer>();

					int parteiCol = -1;
					for (int j = 1; j < currentMapHeader.size(); j++) {
						if (currentMapHeader.get(j).toUpperCase().contains("PARTEI")) {
							parteiCol = j;
							break;
						}
					}

					HashSet<String> names = new HashSet<String>();
					for (int j = 0; j < formattedMap.size(); j++) {
						String name = formattedMap.get(j).cols.get(parteiCol - 1);
						if (!names.contains(name))
							names.add(name);
					}

					ArrayList<String> filteredHeader = new ArrayList<String>();
					filteredHeader.add(currentMapHeader.get(0));
					for (int j = 1; j < currentMapHeader.size(); j++) {
						if (j == parteiCol)
							continue;
						filteredHeader.add(currentMapHeader.get(j));
					}

					Iterator<String> it = names.iterator();
					while (it.hasNext()) {

						String filterString = ((String) it.next()).toUpperCase();

						List<TableEntry> filtered = new ArrayList<TableEntry>();
						for (int j = 0; j < formattedMap.size(); j++) {
							String cur = formattedMap.get(j).cols.get(parteiCol - 1).toUpperCase();

							if (cur.equals(filterString.toUpperCase())) {
								filtered.add(formattedMap.get(j));
							}
						}

						GeoChart.Options options = GeoChart.Options.create();
						options.setRegion("DE");
						options.setResolution(GeoChart.RESOLUTION.PROVINCES);
						options.setHeight((int) (RootLayoutPanel.get().getOffsetHeight() * 0.95));
						options.setWidth((int) (RootLayoutPanel.get().getOffsetWidth()));
						options.keepAspectRatio(false);
						options.setColors(colorMap.get(filterString.substring(0, 2).toUpperCase()).alt);

						// Add new Geo chart to collection of available geo
						// charts
						GeoChart chart = new GeoChart(createTableForMapchart(filtered, filteredHeader), options);
						chart.setStyleName("#map_canvas");
						dpanel.add(chart);
						tablesMap.put(filterString.toUpperCase() + "$$" + i, dpanel.getWidgetCount() - 1);
					}

					// End build Geo charts
					// /////////////////////////////////////////////////////////////

					// Store piechart in collection
					pcharts.add(piechart);

					// Handler for mouse over event on current piechart
					piechart.addOnMouseOverHandler(new OnMouseOverHandler() {
						public void onMouseOverEvent(OnMouseOverEvent event) {
							dpanel.setVisible(true);
							String selected = pieChartData.getValueString(event.getRow(), 0) + "$$" + index;
							dpanel.showWidget(tablesMap.get(selected.toUpperCase()));
						}
					});
				}

				// Build distribution panel
				dpanel.setVisible(false);
				distPanel.add(dpanel, 0, 0);
				distPanel.add(pcharts, 0, 0);
				tabPanel.add(distPanel, "Verteilung");

				// Call all dependent services
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Ueberhangsmandate analysis started.");
				((GetMandateServiceAsync) getMandateSvc).getMandate(projectInput, queryInput, setupMandateCallback());

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreis overview analysis started.");
				((WahlkreisOverviewServiceAsync) wkOverviewSvc).getWKOverview(projectInput, queryInput, setupWKOverviewCallback());

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Knappster Sieger analysis started.");
				((GetKnappsterSiegerServiceAsync) knappsterSiegerSvc).getKnappsterSieger(projectInput, queryInput, setupKnappsterSiegerCallback());

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreis overview (Erststimmen) analysis started.");
				((WKOverviewErststimmenServiceAsync) wkOverviewErststimmenSvc).getOverview(projectInput, queryInput, setupWkOverviewErststimmenCallback());
			}
		};

		return callback;
	}

	// wahlkreissieger
	public AsyncCallback<ArrayList<ArrayList<String>>> setupWKSiegerCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting the Wahlkreissieger: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreissieger analysis complete.");

				setupUITables(s, "Wahlkreissieger", (CellPanel) wkHPanel);

				// Call dependent services
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Bundestag members analysis started.");
				((GetMembersServiceAsync) getMembersSvc).getMembers(projectInput, queryInput, setupMembersCallback());
			}
		};

		return callback;
	}

	// members
	public AsyncCallback<ArrayList<ArrayList<String>>> setupMembersCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting Bundestag members: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Bundestag members analysis complete.");

				setupUITables(s, "Bundestagsmitglieder", (CellPanel) membersHPanel);
			}
		};

		return callback;
	}

	// ueberhangsmandate
	public AsyncCallback<ArrayList<ArrayList<String>>> setupMandateCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting Ueberhangsmandate: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Ueberhangsmandate analysis complete.");

				setupUITables(s, "Ueberhangsmandate", (CellPanel) mandateHPanel);

				// Call dependent services
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreissieger analysis started.");
				((WahlkreissiegerServiceAsync) wkSiegerSvc).getWahlkreissieger(projectInput, queryInput, setupWKSiegerCallback());

			}
		};

		return callback;
	}

	// wahlkreis overview
	public AsyncCallback<ArrayList<ArrayList<String>>> setupWKOverviewCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting the Wahlkreis overview: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreis overview complete.");

				setupUITables(s, "Wahlkreisuebersicht", (CellPanel) wkOverviewHPanel);
			}
		};

		return callback;
	}

	// knappsterSieger
	public AsyncCallback<ArrayList<ArrayList<String>>> setupKnappsterSiegerCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting the knappster Sieger: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Knappster Sieger analysis complete.");

				setupUITables(s, "Knappster Sieger", (CellPanel) knappsterSiegerHPanel);
			}
		};

		return callback;
	}

	// WK overview erststimmen
	public AsyncCallback<ArrayList<ArrayList<String>>> setupWkOverviewErststimmenCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting the Wahlkreis overview (Erststimmen): " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Wahlkreis overview (Erststimmen) complete.");

				setupUITables(s, "Wahlkreisuebersicht (Erststimmen)", (CellPanel) wkOverviewErststimmenHPanel);
			}
		};

		return callback;
	}

	// Request voting form
	public AsyncCallback<ArrayList<ArrayList<String>>> setupRequestVoteCallback() {

		AsyncCallback<ArrayList<ArrayList<String>>> callback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while getting voting form: " + caught.getMessage() + ". "
						+ "Has the data been generated and uploaded to the database?");
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(ArrayList<ArrayList<String>> s) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Voting form now available.");

				setupVoteForms(s, "Stimmzettel", (CellPanel) submitVotePanel);
			}
		};

		return callback;
	}

	// Submit voting form
	public AsyncCallback<Void> setupSubmitVoteCallback() {

		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			@SuppressWarnings("deprecation")
			public void onFailure(Throwable caught) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Error while submitting voting form: " + caught.getMessage());
			}

			@SuppressWarnings("deprecation")
			public void onSuccess(Void v) {

				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Voting form successfully submitted.");

				Window.alert("Voting form successfully submitted");
			}
		};

		return callback;
	}

	public void setupVoteForms(ArrayList<ArrayList<String>> s, String tabName, CellPanel layoutPanel) {

		VerticalPanel finalForm = new VerticalPanel();
		finalForm.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		finalForm.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

		HorizontalPanel htabPanel = new HorizontalPanel();
		htabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		htabPanel.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);
		htabPanel.setSpacing(30);

		Image headerPic = new Image(InputDirectory.stimmzettel);
		finalForm.add(headerPic);

		// selected objects repository
		final HashMap<Integer, CandidateInfo> selectedRepo = new HashMap<Integer, CandidateInfo>();

		for (int i = 0; i < s.size(); i = i + 2) {

			ArrayList<String> currentHeader = s.get(i);
			ArrayList<String> currentTable = s.get(i + 1);

			// first element is table name
			int colLength = currentHeader.size() - 1;

			// Get formatted data
			List<CandidateInfo> formatted = parseToCandidateInfo(currentTable, currentHeader, colLength);

			VerticalPanel vPanel = new VerticalPanel();
			vPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			vPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

			// The pager used to display the current range.
			RangeLabelPager rangeLabelPager = new RangeLabelPager();

			final CellList<CandidateInfo> cellList;
			Images images = GWT.create(Images.class);
			CandidateCell candidateCell = new CandidateCell(images.candidate());

			// Set a key provider that provides a unique key for each candidate.
			cellList = new CellList<CandidateInfo>(candidateCell, CandidateInfo.KEY_PROVIDER);
			cellList.setPageSize(formatted.size());

			cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
			cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

			// Add a selection model
			final SingleSelectionModel<CandidateInfo> selectionModel = new SingleSelectionModel<CandidateInfo>(CandidateInfo.KEY_PROVIDER);
			cellList.setSelectionModel(selectionModel);
			selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

				// Keep track of selection changes, update current choices
				public void onSelectionChange(SelectionChangeEvent event) {
					selectedRepo.put(cellList.hashCode(), selectionModel.getSelectedObject());
				}
			});

			ListDataProvider<CandidateInfo> dataProvider = new ListDataProvider<CandidateInfo>();
			dataProvider.addDataDisplay(cellList);
			dataProvider.setList(formatted);
			rangeLabelPager.setDisplay(cellList);

			// Add to UI
			DecoratorPanel dpanel = new DecoratorPanel();
			ScrollPanel scroll = new ScrollPanel();
			scroll.setAlwaysShowScrollBars(true);
			scroll.setHeight(RootLayoutPanel.get().getOffsetHeight() / 2 + "px");
			scroll.setWidth(RootLayoutPanel.get().getOffsetWidth() / 4 + "px");
			scroll.add(cellList);
			dpanel.add(scroll);
			vPanel.add(dpanel);
			vPanel.add(rangeLabelPager);
			htabPanel.add(vPanel);
		}

		// listen for mouse events on the submit vote button.
		submitVoteButton.addClickHandler(new ClickHandler() {
			@SuppressWarnings("deprecation")
			public void onClick(ClickEvent event) {
				ta.setText(ta.getText() + "\n" + "-> " + DateTimeFormat.getFullTimeFormat().format(new Date()) + ": Submitting vote ...");

				// send current choices to server
				ArrayList<ArrayList<String>> choices = new ArrayList<ArrayList<String>>();

				Iterator<Map.Entry<Integer, CandidateInfo>> entries = selectedRepo.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<Integer, CandidateInfo> entry = entries.next();
					CandidateInfo current = entry.getValue();

					ArrayList<String> temp = new ArrayList<String>();
					temp.add(current.name);
					temp.add(current.party);
					temp.add(current.politicianID);
					choices.add(temp);
				}

				// Initialize the service proxy.
				if (submitVoteSvc == null) {
					submitVoteSvc = (SubmitVoteServiceAsync) GWT.create(SubmitVoteService.class);
					ServiceDefTarget target = (ServiceDefTarget) submitVoteSvc;
					target.setServiceEntryPoint(GWT.getModuleBaseURL() + "submitVote");
				}

				// Make the call to the submit vote service.
				String[] input = new String[3];
				String[] query = new String[3];
				input[0] = serverName.getText();
				input[1] = dbInputBox.getText();
				input[2] = passwordBox.getText();
				query[0] = dropList.get(yearInput.getSelectedIndex());
				query[1] = wahlkreisVote.getText();

				// Request voting form.
				((SubmitVoteServiceAsync) submitVoteSvc).submitVote(input, query, choices, setupSubmitVoteCallback());
			}
		});

		// TODO : replace already existing tab if possible
		finalForm.add(htabPanel);
		finalForm.add(submitVoteButton);
		layoutPanel.add(finalForm);
		tabPanel.add(layoutPanel, tabName);
	}

	/**
	 * Takes an incoming table collection representation as list of string lists
	 * and couples them to the graphic interface
	 * 
	 * @param s
	 *            - table collection representation
	 * @param tabName
	 *            - Name of the tab to which the resulting tables should be
	 *            coupled
	 * @param layoutPanel
	 *            - the "root" layout panel of the tab.
	 */
	public void setupUITables(ArrayList<ArrayList<String>> s, String tabName, CellPanel layoutPanel) {

		HorizontalPanel htabPanel = new HorizontalPanel();
		htabPanel.setSize("" + tabPanel.getOffsetWidth() + "px", "" + tabPanel.getOffsetHeight() + "px");
		htabPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		htabPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

		for (int i = 0; i < s.size(); i = i + 2) {

			ArrayList<String> currentHeader = s.get(i);
			ArrayList<String> currentTable = s.get(i + 1);

			// first element is table name
			int colLength = currentHeader.size() - 1;

			List<TableEntry> formatted = parseToTableEntry(currentTable, colLength);
			CellTable<TableEntry> table = new CellTable<TableEntry>();
			VerticalPanel vPanel = new VerticalPanel();
			vPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			vPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

			for (int j = 0; j < colLength; j++) {
				table.addColumn((new TextColumnWrapper(j)).col, currentHeader.get(j + 1));
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

		// TODO : replace already existing tab if possible
		htabPanel.add(layoutPanel);
		tabPanel.add(htabPanel, tabName);
	}

	// Helper methods and classes -----------------------------------------
	// ------------------------------------------------------------------------

	// TODO: separate files

	/**
	 * Returns options object for a pie chart.
	 * 
	 * @param title
	 *            - title the Piechart should have
	 * @param formatted
	 *            - the data to be modelled.
	 * @return PieOptions object.
	 */
	private PieOptions createOptions(String title, List<TableEntry> formatted) {

		List<String> colors = new ArrayList<String>();
		for (int i = 0; i < formatted.size(); i++) {
			colors.add((String) colorMap.get(formatted.get(i).cols.get(0).toUpperCase().substring(0, 2)).name);
		}
		String[] c = new String[colors.size()];

		PieOptions options = PieOptions.create();
		ChartArea chartArea = ChartArea.create();
		options.setChartArea(chartArea);
		options.setHeight((int) (RootLayoutPanel.get().getOffsetHeight() / 3));
		options.setWidth((int) (RootLayoutPanel.get().getOffsetWidth() / 3));
		options.setLegend(LegendPosition.RIGHT);
		options.setColors(colors.toArray(c));
		options.setBackgroundColor("transparent");
		options.setLineWidth(5);
		options.setTitle(title);
		options.set3D(true);

		return options;
	}

	// TODO: merge createTable?

	/**
	 * Create data source to feed to pie chart.
	 * 
	 * @param fromServer
	 *            - formatted data.
	 * @param header
	 *            - header info such as table name, column names
	 * @return DataTable object.
	 */
	private AbstractDataTable createTableForPiechart(List<TableEntry> fromServer, ArrayList<String> header) {

		DataTable dataTable = DataTable.create();

		dataTable.addColumn(ColumnType.STRING, header.get(1));
		dataTable.addColumn(ColumnType.NUMBER, header.get(2));
		dataTable.addRows(fromServer.size());

		for (int i = 0; i < fromServer.size(); i++) {
			TableEntry current = fromServer.get(i);
			dataTable.setValue(i, 0, current.cols.get(0));
			dataTable.setValue(i, 1, new Double(current.cols.get(1)).doubleValue());
		}

		return dataTable;
	}

	/**
	 * Create data source to feed to geo chart.
	 * 
	 * @param fromServer
	 *            - formatted data.
	 * @param header
	 *            - header info such as table name, column names
	 * @return DataTable object.
	 */
	private AbstractDataTable createTableForMapchart(List<TableEntry> fromServer, ArrayList<String> header) {

		DataTable dataTable = DataTable.create();

		dataTable.addColumn(ColumnType.STRING, header.get(1));
		dataTable.addColumn(ColumnType.NUMBER, header.get(2));
		dataTable.addRows(fromServer.size());

		for (int i = 0; i < fromServer.size(); i++) {
			TableEntry current = fromServer.get(i);
			dataTable.setValue(i, 0, "DE-" + current.cols.get(1));
			dataTable.setValue(i, 1, new Double(current.cols.get(2)).doubleValue());
		}

		return dataTable;
	}

	/**
	 * A class representing a row of a cell table. A row is modeled as a list of
	 * strings.
	 */
	public static class TableEntry {
		private final ArrayList<String> cols;

		public TableEntry(ArrayList<String> cols) {
			this.cols = new ArrayList<String>(cols);
		}
	}

	/**
	 * Wrapper for the TextColumn class, extend provided info by a column number
	 * of interest ( = index)
	 */
	public class TextColumnWrapper {
		private int index = -1;

		TextColumnWrapper(int index) {
			this.index = index;
		}

		TextColumn<TableEntry> col = new TextColumn<TableEntry>() {
			@Override
			public String getValue(TableEntry entry) {
				return entry.cols.get(index);
			}
		};
	}

	/**
	 * A color class used to model a color using a main name, an alternative,
	 * and a list of shades in hexademical encoding.
	 */
	public class Color {
		String name;
		String alt;
		List<Integer> hex;

		Color(String name, String alt) {
			this.name = name;
			this.alt = alt;
		}

		Color(String name, String alt, List<Integer> hex) {
			this.name = name;
			this.alt = alt;
			this.hex = hex;
		}
	}

	/**
	 * Converts a table from ArrayList<String> format to ArrayList<TableEntry>
	 * 
	 * @param toBeParsed
	 *            - input table
	 * @param colLength
	 *            - the number of columns in the original table.
	 * @return formatted table.
	 */
	public List<TableEntry> parseToTableEntry(ArrayList<String> toBeParsed, int colLength) {

		ArrayList<TableEntry> result = new ArrayList<TableEntry>();

		// +1 in iterator skips over delimiter $$
		for (int i = 0; i < toBeParsed.size(); i = i + colLength + 1) {
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++) {
				temp.add(toBeParsed.get(i + j));
			}
			result.add(new TableEntry(temp));
		}

		return result;
	}

	/**
	 * Converts a table from ArrayList<String> format to
	 * ArrayList<CandidateInfo>
	 * 
	 * @param toBeParsed
	 *            - input table
	 * @param colLength
	 *            - the number of columns in the original table.
	 * @return formatted table.
	 */
	public List<CandidateInfo> parseToCandidateInfo(ArrayList<String> toBeParsed, ArrayList<String> header, int colLength) {

		ArrayList<CandidateInfo> result = new ArrayList<CandidateInfo>();

		// +1 in iterator skips over delimiter $$
		for (int i = 0; i < toBeParsed.size(); i = i + colLength + 1) {

			CandidateInfo info = new CandidateInfo();
			for (int j = 0; j < colLength; j++) {
				info.setProperty(header.get(j + 1), toBeParsed.get(i + j));
			}
			result.add(info);
		}
		return result;
	}

	/**
	 * Wrapper class providing the GeoChart visualization widget.
	 */
	public static class GeoChart extends Visualization<GeoChart.Options> implements Selectable {

		public static final String PACKAGE = "geochart";

		public enum RESOLUTION {
			COUNTRIES, PROVINCES, METROS;
		}

		public static class Options extends CommonChartOptions {
			public static Options create() {
				return JavaScriptObject.createObject().cast();
			}

			protected Options() {
			}

			public final void setRegion(String region) {
				this.set("region", region);
			}

			public final void setResolution(RESOLUTION resolution) {
				this.set("resolution", resolution.toString().toLowerCase());
			}

			public final void keepAspectRatio(boolean b) {
				this.set("keepAspectRatio", b);
			}
		}

		public GeoChart() {
			super();
		}

		public GeoChart(AbstractDataTable data, Options options) {
			super(data, options);
		}

		@Override
		public void addSelectHandler(SelectHandler handler) {
			Selection.addSelectHandler(this, handler);
		}

		@Override
		public JsArray<Selection> getSelections() {
			return Selection.getSelections(this);
		}

		@Override
		public void setSelections(JsArray<Selection> sel) {
			Selection.setSelections(this, sel);
		}

		@Override
		protected native JavaScriptObject createJso(Element parent) /*-{
			return new $wnd.google.visualization.GeoChart(parent);
		}-*/;

	}

	/**
	 * Information about a candidate.
	 */
	public static class CandidateInfo implements Comparable<CandidateInfo> {

		// The key provider that provides the unique ID of a candidate.
		public static final ProvidesKey<CandidateInfo> KEY_PROVIDER = new ProvidesKey<CandidateInfo>() {
			@Override
			public Object getKey(CandidateInfo item) {
				return item == null ? null : item.getId();
			}
		};

		private String name = "";
		private String listPos = "";
		private String party = "";
		private String politicianID = "";

		private static int nextId = 0;

		// unique id per candidate
		private final int id;

		public CandidateInfo() {
			this.id = nextId;
			nextId++;
		}

		@Override
		public int compareTo(CandidateInfo o) {
			return (o == null || o.name == null) ? -1 : -o.name.compareTo(name);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof CandidateInfo) {
				return id == ((CandidateInfo) o).id;
			}
			return false;
		}

		public String getName() {
			return name;
		}

		public String getListPos() {
			return listPos;
		}

		public String getParty() {
			return party;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setListPos(String listpos) {
			this.listPos = listpos;
		}

		public void setParty(String party) {
			this.party = party;
		}

		public void setProperty(String prop, String value) {
			String s = prop.toLowerCase();
			if (s.contains("parteiname"))
				this.party = value;
			else if (s.contains("kandidatenname"))
				this.name = value;
			else if (s.contains("listenplatz"))
				this.listPos = value;
			else if (s.contains("politikernummer"))
				this.politicianID = value;
		}

		public int getId() {
			return this.id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}

	/**
	 * candidate portrait used
	 */
	static interface Images extends ClientBundle {
		ImageResource candidate();
	}

	/**
	 * The Cell used to render a {@link ContactInfo}.
	 */
	static class CandidateCell extends AbstractCell<CandidateInfo> {

		// html of the image used for candidates
		private final String imageHtml;

		public CandidateCell(ImageResource image) {
			this.imageHtml = AbstractImagePrototype.create(image).getHTML();
		}

		@Override
		public void render(Context context, CandidateInfo value, SafeHtmlBuilder sb) {
			// Value can be null, so do a null check..
			if (value == null) {
				return;
			}

			sb.appendHtmlConstant("<table>");

			// Add the candidate image.
			sb.appendHtmlConstant("<tr><td rowspan='3'>");
			sb.appendHtmlConstant(imageHtml);
			sb.appendHtmlConstant("</td>");

			// Add the remaining details
			sb.appendHtmlConstant("<td style='font-size:160%;'>");
			sb.appendEscaped(value.getName());
			sb.appendHtmlConstant("</td></tr><tr><td style='font-size:120%;'>");
			sb.appendEscaped(value.getListPos().equals("") ? value.getParty() : value.getParty() + " (Listenplatz " + value.getListPos() + ")");
			sb.appendHtmlConstant("</td></tr></table>");
		}
	}

	/**
	 * A pager that displays the current range without any controls to change
	 * the range.
	 */
	public class RangeLabelPager extends AbstractPager {

		// Labels shows range
		private final HTML label = new HTML();

		public RangeLabelPager() {
			initWidget(label);
		}

		@Override
		protected void onRangeOrRowCountChanged() {
			HasRows display = getDisplay();
			Range range = display.getVisibleRange();
			int start = range.getStart();
			int end = start + range.getLength();
			label.setText(start + " - " + end + " : " + display.getRowCount(), HasDirection.Direction.LTR);
		}
	}
}
