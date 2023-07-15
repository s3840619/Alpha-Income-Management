package controllers;

import Strategies.LineGraphTargetStrategy;
import application.Main;
import components.CurvedFittedAreaChart;
import components.WeekdayAxis;
import components.MonthAxis;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Store;
import models.TargetDataPoint;
import models.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Stream;

public class GraphTileController extends Controller{
	
    private Connection con = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    private Main main;
    private TargetGraphsPageController parent;
    private User selectedUser;

	private LineGraphTargetStrategy strategy;

    @FXML
	private StackPane graphPane;
    @FXML
	private VBox legend;
	@FXML
	private Label graphTitle;
	@FXML
	private MFXButton swapViewButton;
	@FXML
	private Label target1VarianceLabel, target2VarianceLabel;
	
    @FXML
	private void initialize() {}

	@Override
	public void setMain(Main main) {
		this.main = main;
	}
	
	public void setConnection(Connection c) {
		this.con = c;
	}

	public void setParent(TargetGraphsPageController p){this.parent = p;}

	public void setStrategy(LineGraphTargetStrategy strategy){
		this.strategy = strategy;
	}

	@Override
	public void fill() {
    	setGraphView();
	}

	public void updateVariance(){
		double currentActual = strategy.getActualSeries().getData().get(strategy.getActualSeries().getData().size()-1).getYValue().doubleValue();
		double currentTarget1 = strategy.getTarget1Series().getData().get(strategy.getActualSeries().getData().size()-1).getYValue().doubleValue();
		double currentTarget2 = strategy.getTarget2Series().getData().get(strategy.getActualSeries().getData().size()-1).getYValue().doubleValue();
		double target1Variance = currentTarget1 - currentActual;
		double target2Variance = currentActual - currentTarget2;

		if(target1Variance>0){
			target1VarianceLabel.setText(NumberFormat.getCurrencyInstance().format(target1Variance) + " left until you hit Target 1!");
		}else{
			target1VarianceLabel.setText(NumberFormat.getCurrencyInstance().format(-target1Variance) + " over Target 1!");
		}

		if(target2Variance>0){
			target2VarianceLabel.setText(NumberFormat.getCurrencyInstance().format(target2Variance) + " left until you hit Target 2!");
		}else{
			target2VarianceLabel.setText(NumberFormat.getCurrencyInstance().format(-target2Variance) + " over Target 2!");
		}
	}

	public void setGraphView() {
		graphPane.getChildren().clear();
		graphPane.getChildren().add(fillGraph(strategy.getStrategyName(), strategy.getYAxisVisibility()));
		legend.setVisible(true);
		updateVariance();
		swapViewButton.setOnAction(event -> setTableView());
	}

	public void setTableView(){
		graphPane.getChildren().clear();
		graphPane.getChildren().add(fillTable());
		legend.setVisible(false);
		updateVariance();
		swapViewButton.setOnAction(event -> setGraphView());
	}

	public MFXTableView fillTable(){
		MFXTableView dataTable = new MFXTableView<TargetDataPoint>();
		MFXTableColumn dateCol;
		MFXTableColumn actualValueCol;
		MFXTableColumn target1ValueCol;
		MFXTableColumn target2ValueCol;
		dateCol = new MFXTableColumn<>("Date",false, Comparator.comparing(TargetDataPoint::getDate));
		actualValueCol = new MFXTableColumn<>("Actual",false, Comparator.comparing(TargetDataPoint::getActual));
		target1ValueCol = new MFXTableColumn<>("Target 1",false, Comparator.comparing(TargetDataPoint::getTarget1));
		target2ValueCol = new MFXTableColumn<>("Target 2",false, Comparator.comparing(TargetDataPoint::getTarget2));

		dateCol.setRowCellFactory(store -> new MFXTableRowCell<>(TargetDataPoint::getDateString));
		actualValueCol.setRowCellFactory(store -> new MFXTableRowCell<>(TargetDataPoint::getActual));
		target1ValueCol.setRowCellFactory(store -> new MFXTableRowCell<>(TargetDataPoint::getTarget1));
		target2ValueCol.setRowCellFactory(store -> new MFXTableRowCell<>(TargetDataPoint::getTarget2));

		dataTable.getTableColumns().addAll(dateCol, actualValueCol, target1ValueCol, target2ValueCol);

		//Create list of datapoints from series
		XYChart.Series<Number,Number> actualSeries = strategy.getActualSeries();
		XYChart.Series<Number,Number> target1Series = strategy.getTarget1Series();
		XYChart.Series<Number,Number> target2Series = strategy.getTarget2Series();

		ObservableList<TargetDataPoint> dataPoints = FXCollections.observableArrayList();
		//Add datapoints to table
		for(int i = 0; i < target2Series.getData().size(); i++) {
			LocalDate date = strategy.getStartDate().plusDays(i);
			String actual = "";
			if(i<actualSeries.getData().size()){
				actual = actualSeries.getData().get(i).getYValue().toString();
			}
			String target1 = target1Series.getData().get(i).getYValue().toString();
			String target2 = target2Series.getData().get(i).getYValue().toString();

			TargetDataPoint dataPoint = new TargetDataPoint(date, actual, target1, target2);
			dataPoints.add(dataPoint);
		}

		dataTable.setItems(dataPoints);

		dataTable.setMaxHeight(Double.MAX_VALUE);
		dataTable.setMaxWidth(Double.MAX_VALUE);
		dataTable.setFooterVisible(false);
		return dataTable;
	}
	public StackPane fillGraph(String title, Boolean showYAxis){
		graphTitle.setText(title);

		//defining a series
		XYChart.Series<Number,Number> actualSeries = strategy.getActualSeries();
		XYChart.Series<Number,Number> target1Series = strategy.getTarget1Series();
		XYChart.Series<Number,Number> target2Series = strategy.getTarget2Series();

		NumberAxis yAxis = new NumberAxis();

		double maxY = Stream.of(actualSeries, target1Series, target2Series)
				.flatMap(series -> series.getData().stream())
				.mapToDouble(data -> data.getYValue().doubleValue())
				.max()
				.orElse(0.0);

		yAxis.setLabel(strategy.getAxisLabel());
		yAxis.setTickLabelsVisible(showYAxis);
		yAxis.setTickMarkVisible(showYAxis);
		yAxis.setMinorTickVisible(false);
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(Math.ceil(maxY / calculateTickUnit(maxY*1.25)) * calculateTickUnit(maxY*1.25));
		yAxis.setTickUnit(calculateTickUnit(maxY*1.25));

		ValueAxis xAxis;
		CurvedFittedAreaChart areaChart;
		if(strategy.getLength()<=7) {
			actualSeries = strategy.getActualSeries();
			target1Series = strategy.getTarget1Series();
			target2Series = strategy.getTarget2Series();
			xAxis = new WeekdayAxis();
			xAxis.setLabel("Day");
			xAxis.setMinorTickVisible(false);
			xAxis.setAutoRanging(false);
			xAxis.setLowerBound(0);
			xAxis.setUpperBound(strategy.getLength());
			areaChart = new CurvedFittedAreaChart((WeekdayAxis) xAxis, yAxis);
		}else if(strategy.getLength()>31){
			actualSeries = strategy.getActualSeries();
			target1Series = strategy.getTarget1Series();
			target2Series = strategy.getTarget2Series();
			xAxis = new MonthAxis();
			xAxis.setLabel("Month");
			xAxis.setMinorTickVisible(false);
			xAxis.setAutoRanging(false);
			xAxis.setLowerBound(0);
			xAxis.setUpperBound(strategy.getLength()+1);
			areaChart = new CurvedFittedAreaChart((MonthAxis) xAxis, yAxis);
		}else{
			incrementXValues(actualSeries);
			incrementXValues(target1Series);
			incrementXValues(target2Series);
			xAxis = new NumberAxis(0, strategy.getLength(), 1);
			xAxis.setLabel("Day");
			xAxis.setMinorTickVisible(false);
			xAxis.setAutoRanging(false);
			xAxis.setLowerBound(1);
			xAxis.setUpperBound(strategy.getLength()+1);
			areaChart = new CurvedFittedAreaChart((NumberAxis) xAxis, yAxis);
		}

		LineChart<Number,Number> target1Chart = new LineChart<>(xAxis, yAxis);
		LineChart<Number,Number> target2Chart = new LineChart<>(xAxis, yAxis);


		actualSeries.setName("Actual");
		target1Series.setName("Target 1");
		target2Series.setName("Target 2");
		areaChart.getData().add(actualSeries);
		target1Chart.getData().add(target1Series);
		target2Chart.getData().add(target2Series);

		areaChart.setHorizontalGridLinesVisible(false);
		areaChart.setVerticalGridLinesVisible(false);
		areaChart.setAnimated(false);
		areaChart.setCreateSymbols(false);
		areaChart.setLegendVisible(false);
		areaChart.setStyle("-fx-stroke: #0F60FF;" + "-fx-stroke-width: 3px;");

		target1Chart.setHorizontalGridLinesVisible(false);
		target1Chart.setVerticalGridLinesVisible(false);
		target1Chart.setAnimated(false);
		target1Chart.setCreateSymbols(false);
		target1Chart.setLegendVisible(false);
		target1Chart.setStyle("-fx-stroke: #FFBD29;" + "-fx-stroke-width: 2px;");
		target1Chart.getStylesheets().add("views/CSS/Target1LineChart.css");

		target2Chart.setHorizontalGridLinesVisible(false);
		target2Chart.setVerticalGridLinesVisible(false);
		target2Chart.setAnimated(false);
		target2Chart.setCreateSymbols(false);
		target2Chart.setLegendVisible(false);
		target2Chart.setStyle("-fx-stroke: #FF298D;" + "-fx-stroke-width: 2px;");
		target2Chart.getStylesheets().add("views/CSS/Target2LineChart.css");

		StackPane finalPane = layerCharts(target2Chart, target1Chart, areaChart);
		finalPane.setFocusTraversable(false);

		return finalPane;
	}

	private double calculateTickUnit(double range) {
		double rawTickUnit = range / 10;
		double scaleFactor = Math.pow(10, Math.floor(Math.log10(rawTickUnit)));
		double normalizedTickUnit = Math.ceil(rawTickUnit / scaleFactor);
		if (normalizedTickUnit < 2) {
			normalizedTickUnit = 1;
		} else if (normalizedTickUnit < 5) {
			normalizedTickUnit = 2;
		} else {
			normalizedTickUnit = 5;
		}
		return normalizedTickUnit * scaleFactor;
	}

	public XYChart.Series<Number, Number> incrementXValues(XYChart.Series<Number, Number> originalSeries) {
		XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
		newSeries.setName(originalSeries.getName());

		for (XYChart.Data<Number, Number> data : originalSeries.getData()) {
			Number newXValue = data.getXValue().doubleValue() + 1;
			Number yValue = data.getYValue();
			newSeries.getData().add(new XYChart.Data<>(newXValue, yValue));
		}
		return newSeries;
	}

	@SafeVarargs
	private StackPane layerCharts(final XYChart<Number, Number> ... charts) {
		for (int i = 1; i < charts.length; i++) {
			configureOverlayChart(charts[i]);
		}

		StackPane stackpane = new StackPane();
		stackpane.getChildren().addAll(charts);
		return stackpane;
	}

	private void configureOverlayChart(final XYChart<Number, Number> chart) {
		chart.setAlternativeRowFillVisible(false);
		chart.setAlternativeColumnFillVisible(false);
		chart.setHorizontalGridLinesVisible(false);
		chart.setVerticalGridLinesVisible(false);
		chart.getXAxis().setVisible(false);
		chart.getYAxis().setVisible(false);
	}
}
