package br.ufrn.strproject2.views;

import br.ufrn.strproject2.models.CpuInfo;
import br.ufrn.strproject2.models.HostProcess;
import br.ufrn.strproject2.utils.CoresManager;
import br.ufrn.strproject2.utils.ProcessesUtil;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author dhiogoboza
 */
public class MainStage implements Runnable, EventHandler<WindowEvent>, ChangeListener<String> {
	
	private static final String TAG = "MainStage";
	
	private static final int LOOP_DELAY = 500;
	private static final int DELAY_PROCESSES = 2000;
	private static final int MAX_DATA_POINTS = 100;
	
	private int delayCount = 0;

	private TabPane center;
	private Scene stageScene;
	
	private BorderPane layoutPane;
    
    private double prefWidth;
	private double prefHeight;
    
    public ObservableList<HostProcess> userProcessesList = FXCollections.observableArrayList();
	public ObservableList<HostProcess> allProcessesList = FXCollections.observableArrayList();
    
    private CoresManager coresManager;
	private LineChart<Number, Number> cpuChart;
	private LineChart<Number, Number> memoryChart;
	private NumberAxis xCPUAxis;
	private NumberAxis xMemoryAxis;
	private XYChart.Series memorySeries;
    
    private Stage myStage;
    private CheckBox checkBoxAllProcesses;
    private TextField inputFilter;
    private TableView viewProcesses;
    private ContextMenu contextMenu;
	
	private String filterString;
	
	private int currentGraphPosition = 0;
	
	private ScheduledExecutorService updateProcessesService;
    
    private final EventHandler<ActionEvent> actionEventHandler = (ActionEvent actionEvent) -> {
		String clickedId;
		if (actionEvent.getSource() instanceof CheckBox) {
			updateProcesses();
			return;
		} else if (actionEvent.getSource() instanceof MenuItem) {
			clickedId = ((MenuItem) actionEvent.getSource()).getId();
		} else {
			clickedId = ((Button) actionEvent.getSource()).getId();
		}
		
		switch (clickedId) {
			case "exit":
				updateProcessesService.shutdown();
				if (myStage != null) {
					myStage.close();
				}
				break;
			case "killp":
				ProcessesUtil.killProcess((HostProcess) viewProcesses.getSelectionModel().getSelectedItem());
				updateProcesses();
				break;
			case "pausep":
				ProcessesUtil.pauseProcess((HostProcess) viewProcesses.getSelectionModel().getSelectedItem());
				updateProcesses();
				break;
			case "continuep":
				ProcessesUtil.continueProcess((HostProcess) viewProcesses.getSelectionModel().getSelectedItem());
				updateProcesses();
				break;
		}
	};
	
    private final EventHandler<MouseEvent> mouseEventHandler = (MouseEvent mouseEvent) -> {
		if (mouseEvent.isSecondaryButtonDown()) {
			contextMenu.show(center, mouseEvent.getScreenX(), mouseEvent.getScreenY());
		}
	};
	
	public MainStage(Stage stage) {
		this.myStage = stage;
	}
	
	public void init() {
				
        Group root = new Group();
		stageScene = new Scene(root);
        myStage.setScene(stageScene);
        
        myStage.setResizable(false);
		
		layoutPane = new BorderPane();
        
        root.getChildren().add(layoutPane);
		
        updateDimensions();
        
        initTableContextMenu();
		initCenter();
		initTop();
		
		myStage.setOnCloseRequest(this);
				
		updateProcessesService = Executors.newSingleThreadScheduledExecutor();//Executors.newScheduledThreadPool(1);
		updateProcessesService.scheduleWithFixedDelay(this, 0, LOOP_DELAY, TimeUnit.MILLISECONDS);
	}

	private void initTop() {
		
		TilePane topPane = new TilePane(Orientation.HORIZONTAL);
		
		MenuBar menuBar = new MenuBar();
 
        // --- Menu File
        Menu menuFile = new Menu("File");
		MenuItem add = new MenuItem("Exit");
		add.setId("exit");
        add.setOnAction(actionEventHandler);
        menuFile.getItems().addAll(add);
		
        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");
 
        // --- Menu View
        Menu menuView = new Menu("View");
		add = new MenuItem("Fullscreen");
		add.setId("fs");
        add.setOnAction(actionEventHandler);
        menuView.getItems().addAll(add);
 
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
		
		//pane.setPrefHeight(100);
		//pane.setPrefWidth(getWidth());
		menuBar.setPrefWidth(prefWidth);
		
		topPane.getChildren().add(menuBar);
		
		layoutPane.setTop(topPane);
	}

	private void initCenter() {
		center = new TabPane();
        
		addTab("Processos", initTabProcesses());
		addTab("Recursos", initTabResources());
		
        HBox centerContainer = new HBox(center);
        centerContainer.setPadding(new Insets(0, 10, 10, 10));
        centerContainer.setBackground(new Background(new BackgroundFill(
                Color.gray(0.865), CornerRadii.EMPTY, Insets.EMPTY)));
        
		layoutPane.setCenter(centerContainer);
	}
	
	private void addTab(String tabTitle, Node tabContent) {
		Tab tab = new Tab();
		tab.setText(tabTitle);
		tab.setContent(tabContent);
		tab.setClosable(false);
		
		center.getTabs().add(tab);
	}

    public void show() {
        init();
        myStage.show();
    }

    private void updateDimensions() {
        prefWidth = 600;
        prefHeight = 520;
        
        layoutPane.setPrefSize(prefWidth, prefHeight);
    }
	
	private Node initTabResources() {
		
		HBox coresContainer = new HBox();
		
		coresContainer.setPrefWidth(prefWidth);
		
		coresManager = new CoresManager();
		
		int coresCount = Runtime.getRuntime().availableProcessors();//ManagementFactory.getThreadMXBean().getThreadCount();
		
		Label coreUsage;
		double coreWidth = prefWidth / coresCount;
		
		xCPUAxis = new NumberAxis(0, 100, 10);
		
		NumberAxis yAxis = new NumberAxis(0, 100, 25); 
		
		
		cpuChart = new LineChart<>(xCPUAxis, yAxis);
		cpuChart.setAnimated(false);
		cpuChart.setTitle("Uso de CPU");
		
		XYChart.Series series;
		for (int i = 0; i < coresCount; i++) {
			coreUsage = new Label();
			series = new XYChart.Series();
			series.setName("cpu" + i);
			
			cpuChart.getData().add(series);
			
			coreUsage.setPrefWidth(coreWidth);
			coreUsage.setAlignment(Pos.CENTER);
			
			coresContainer.getChildren().add(coreUsage);
			
			coresManager.addCpuInfo(new CpuInfo("cpu" + i, coreUsage, series));
		}
		
		
		HBox memoryContainer = new HBox();
		
		xMemoryAxis = new NumberAxis(0, 100, 10);
		
		memoryChart = new LineChart<>(xMemoryAxis, new NumberAxis(0, 100, 25));
		memoryChart.setTitle("Uso de memória");
		memoryChart.setAnimated(false);
		
		memorySeries = new XYChart.Series();
		memorySeries.setName("Uso de memória");
		memoryChart.getData().add(memorySeries);
		
		VBox vBox = new VBox(cpuChart, coresContainer, memoryChart, memoryContainer);
		
		return vBox;
	}
	
	private Node initTabProcesses() {
		userProcessesList = FXCollections.observableArrayList();
		allProcessesList = FXCollections.observableArrayList();
        
		ProcessesUtil.getProcesses(allProcessesList, true);
        ProcessesUtil.getProcesses(userProcessesList, false);
        
        TableColumn pidCol = new TableColumn("PID");
        TableColumn nameCol = new TableColumn("Nome do processo");
		TableColumn userCol = new TableColumn("Usuário");
        TableColumn cpuCol = new TableColumn("CPU (%)");
		TableColumn memCol = new TableColumn("Memória (%)");
        
        pidCol.setCellValueFactory(
            new PropertyValueFactory<>("pid")
        );
		
		nameCol.setCellValueFactory(
            new PropertyValueFactory<>("readableName")
        );
		
		userCol.setCellValueFactory(
            new PropertyValueFactory<>("user")
        );
		
        cpuCol.setCellValueFactory(
            new PropertyValueFactory<>("CPU")
        );
		
		memCol.setCellValueFactory(
            new PropertyValueFactory<>("MEM")
        );
		
        viewProcesses = new TableView();
        viewProcesses.setEditable(false);
        viewProcesses.getColumns().addAll(pidCol, nameCol, userCol, cpuCol, memCol);
        viewProcesses.setItems(userProcessesList);
        
        viewProcesses.setOnMousePressed(mouseEventHandler);
        
        checkBoxAllProcesses = new CheckBox("Processos de todos usuários");
        checkBoxAllProcesses.setOnAction(actionEventHandler);
        
        inputFilter = new TextField();
		inputFilter.textProperty().addListener(this);
		
		Label labelFilter = new Label("Filtrar:");
		labelFilter.setPadding(new Insets(0, 10, 0, 0));
        
		HBox hBoxFilter = new HBox(labelFilter, inputFilter);
		hBoxFilter.setAlignment(Pos.BASELINE_RIGHT);
		HBox.setHgrow(hBoxFilter, Priority.ALWAYS); 
		
        HBox hBoxOptions = new HBox(checkBoxAllProcesses, hBoxFilter);
        hBoxOptions.setPrefWidth(prefWidth);
        hBoxOptions.setPadding(new Insets(10));
		
		return new VBox(viewProcesses, hBoxOptions);
	}

    private void initTableContextMenu() {
        TextField textField = new TextField("Type Something"); // we will add a popup menu to this text field
        contextMenu = new ContextMenu();
		
        MenuItem pauseP = new MenuItem("Stop process");
		pauseP.setId("pausep");
				
		MenuItem continueP = new MenuItem("Continue process");
		continueP.setId("continuep");
				
		MenuItem killP = new MenuItem("Kill process");
		killP.setId("killp");
        
		pauseP.setOnAction(actionEventHandler);
		continueP.setOnAction(actionEventHandler);
		killP.setOnAction(actionEventHandler);
		
		
        contextMenu.getItems().addAll(pauseP, continueP, new SeparatorMenuItem(), killP);
        // ...
        textField.setContextMenu(contextMenu);
    }
	
	private void updateProcesses() {
		int current = viewProcesses.getSelectionModel().getSelectedIndex();
		
		if (checkBoxAllProcesses.isSelected()) {
			allProcessesList = FXCollections.observableArrayList();
			ProcessesUtil.getProcesses(allProcessesList, true, filterString);
			
			viewProcesses.setItems(allProcessesList);
		} else {
			userProcessesList = FXCollections.observableArrayList();
			ProcessesUtil.getProcesses(userProcessesList, false, filterString);
			
			viewProcesses.setItems(userProcessesList);
		}
		
		viewProcesses.sort();
		viewProcesses.getSelectionModel().select(current);
	}
	
	private void backgroudFinishCallback() {
		HashMap<String, CpuInfo> cpus = coresManager.getCPUsInfo();
		
		CpuInfo cpuInfo;
		XYChart.Data data;
		
		for (Entry<String, CpuInfo> entry: cpus.entrySet()) {
			Rectangle rect = new Rectangle(0, 0);
			rect.setVisible(false);
		
			cpuInfo = entry.getValue();
			data = new XYChart.Data(currentGraphPosition, cpuInfo.getUsage());
			data.setNode(rect);
			
			if (currentGraphPosition > MAX_DATA_POINTS) {	
				cpuInfo.getSeries().getData().remove(0);
			}
			
			cpuInfo.getSeries().getData().add(data);
			cpuInfo.getLabel().setText(cpuInfo.getName() + ": " + Math.round(cpuInfo.getUsage()) + "%");
		}
		
		
		
		OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		
		//os.getTotalPhysicalMemorySize()
		
		double usedPercent = 100 * ((double) (1.0 -  ((double) os.getFreePhysicalMemorySize() / (double) os.getTotalPhysicalMemorySize())));
		
		//System.out.println("usedPercent: " + Runtime.getRuntime().freeMemory() / 1024);
		//System.out.println("usedPercent: " + Runtime.getRuntime().totalMemory()/ 1024);
		//System.out.println("        divi: " + ((double)Runtime.getRuntime().freeMemory()/(double)Runtime.getRuntime().totalMemory()));
		//System.out.println("\n\n");
		
		
		
		usedPercent = Math.round(usedPercent > 99.9? 100 : usedPercent);
		
		Rectangle memRect = new Rectangle(0, 0);
		memRect.setVisible(false);
		
		XYChart.Data memoryData = new XYChart.Data(currentGraphPosition, usedPercent);
		memoryData.setNode(memRect);
		
		memoryChart.setTitle("Uso de memória (" + usedPercent + "%)");
		
		if (currentGraphPosition > MAX_DATA_POINTS) {	
			memorySeries.getData().remove(0);
		}
		
		memorySeries.getData().add(memoryData);
		
		if (currentGraphPosition > MAX_DATA_POINTS) {
			xCPUAxis.setLowerBound(currentGraphPosition - MAX_DATA_POINTS);
			xCPUAxis.setUpperBound(currentGraphPosition);
			
			xMemoryAxis.setLowerBound(currentGraphPosition - MAX_DATA_POINTS);
			xMemoryAxis.setUpperBound(currentGraphPosition);
		}
		
		currentGraphPosition ++;
	}
	
	@Override
	public void run() {
		if (delayCount == DELAY_PROCESSES) {
			updateProcesses();
		}
		
		coresManager.getCPUProc();
		Platform.runLater(this::backgroudFinishCallback);
		
		delayCount += LOOP_DELAY;
	}

	@Override
	public void handle(WindowEvent t) {
		updateProcessesService.shutdown();
	}
	
	@Override
	public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
		filterString = newValue;

		updateProcesses();
	}
	
}