package br.ufrn.strproject2.models;

import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

/**
 *
 * @author dhiogoboza
 */
public class CpuInfo {
	private long idle = 0;      //CPU Idle time
    private long total = 0;     //CPU Total time
    private final int conservative = 0;
    private final int average = 1;
    private final int optmistic = 2;
	private String name;
	private double usage = 0;
	
	private final Label label;
	private final XYChart.Series series;

	public CpuInfo(String name, Label label, XYChart.Series series) {
		this.name = name;
		this.label = label;
		this.series = series;
	}

	public long getIdle() {
		return idle;
	}

	public void setIdle(long idle) {
		this.idle = idle;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public double getUsage() {
		return usage;
	}

	public void setUsage(double usage) {
		this.usage = usage > 100? 100 : usage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Label getLabel() {
		return label;
	}

	public XYChart.Series getSeries() {
		return series;
	}
}
