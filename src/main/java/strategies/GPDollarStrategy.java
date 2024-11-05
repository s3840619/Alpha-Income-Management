package strategies;

import controllers.TargetGraphsPageController;
import javafx.scene.chart.XYChart;
import models.TillReportDataPoint;
import services.TillReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GPDollarStrategy extends AbstractLineGraphStrategy {
    public static final boolean SHOW_Y_AXIS = false;
    public static final String GRAPH_TITLE = "GP ($)";
    public static final String AXIS_LABEL = "Gross Profit ($)";
    public static final boolean DOLLAR_FORMAT = true;

    private List<TillReportDataPoint> currentTillReportDataPoints;
    private TillReportService tillReportService;

    public GPDollarStrategy(LocalDate startDate, LocalDate endDate, TargetGraphsPageController parent) {
        super(startDate, endDate, parent, "GPDollar");

        try {
            this.tillReportService = new TillReportService();
            this.currentTillReportDataPoints = tillReportService.getTillReportDataPointsByKey(
                    main.getCurrentStore().getStoreID(),
                    startDate,
                    endDate,
                    "Gross Profit ($)"
            );
        } catch (IOException e) {
            parent.throwError(e);
        }
    }

    @Override
    public String getStrategyName() {
        return GRAPH_TITLE;
    }

    @Override
    public boolean getYAxisVisibility() {
        return SHOW_Y_AXIS;
    }

    @Override
    public String getAxisLabel() {
        return AXIS_LABEL;
    }

    @Override
    public Boolean isDollarFormat() {
        return DOLLAR_FORMAT;
    }

    @Override
    public XYChart.Series<Number,Number> getActualSeries() {
        XYChart.Series<Number,Number> series = new XYChart.Series<>();
        int dayDifference = (int) ChronoUnit.DAYS.between(startDate, LocalDate.now());
        int accumulatedQuantity = 0;
        for(int i = 0; i < Math.min(dayDifference, length); i++) {
            LocalDate date = startDate.plusDays(i);
            for(int j = 0; j < currentTillReportDataPoints.size(); j++) {
                if(currentTillReportDataPoints.get(j).getAssignedDate().equals(date)) {
                    accumulatedQuantity += currentTillReportDataPoints.get(j).getAmount();
                    series.getData().add(new XYChart.Data(i, accumulatedQuantity));
                    break;
                }
            }
        }
        return series;
    }
}
