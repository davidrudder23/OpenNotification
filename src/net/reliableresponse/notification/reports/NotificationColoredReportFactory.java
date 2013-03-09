/*
 * Created on May 5, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.reports;

import it.businesslogic.ireport.IReportScriptlet;
import it.businesslogic.ireport.chart.DefaultChartFactory;
import it.businesslogic.ireport.util.Misc;

import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.DefaultCategoryItemRenderer;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class NotificationColoredReportFactory extends DefaultChartFactory {
	static final String PIE3D = "Pie3D";

	static final String PIE = "Pie";

	static final String BAR = "Bar";

	static final String BAR3D = "Bar3D";

	static final String LINE = "Line";

	static final String AREA = "Area";

	static final String CANDLESTICK = "Candlestick";

	public static Image drawChart(String[] parameters,
			IReportScriptlet scriptlet) {
		java.util.Properties props = parseProperties(parameters);
		int width = getParameterAsInteger("width", props, 250);
		int height = getParameterAsInteger("height", props, 100);
		int quality = getParameterAsInteger("quality", props, 1);
		String chartName = props.getProperty("chartName");

		boolean showLegend = getParameterAsBoolean("legend", props, false);
		boolean showTooltips = getParameterAsBoolean("tooltips", props, false);

		if (chartName.equals(PIE3D)) {
			Vector labels = getSeries("serie0", props, scriptlet);
			Vector values = getSeries("serie1", props, scriptlet);

			DefaultKeyedValues dkv = new DefaultKeyedValues();

			for (int i = 0; i < values.size(); ++i) {

				String key = (i + 1) + "";
				if (labels != null) {
					key = "" + labels.get(i);
				}
				dkv.addValue(key, new Double(""
						+ ((values.get(i) != null) ? values.get(i) : "0")));
			}

			double depthFactor = getParameterAsDouble("depthFactor", props, 0.2);
			JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart3D(
					"", new DefaultPieDataset(dkv), showLegend, showTooltips,
					false);
			((PiePlot3D) (chart.getPlot())).setDepthFactor(depthFactor);

			((PiePlot3D) (chart.getPlot()))
					.setForegroundAlpha((float) getParameterAsDouble(
							"foregroundAlpha", props, 0.0));
			setChartProperties(props, chart);
			chart.getCategoryPlot().setRenderer(
					new NotificationItemRenderer(labels));

			return chart.createBufferedImage(width * quality, height * quality);
		} else if (chartName.equals(PIE)) {
			Vector labels = getSeries("serie0", props, scriptlet);
			Vector values = getSeries("serie1", props, scriptlet);

			DefaultKeyedValues dkv = new DefaultKeyedValues();

			for (int i = 0; i < values.size(); ++i) {

				String key = (i + 1) + "";
				if (labels != null) {
					key = "" + labels.get(i);
				}
				dkv.addValue(key, new Double(""
						+ ((values.get(i) != null) ? values.get(i) : "0")));
			}

			JFreeChart chart = org.jfree.chart.ChartFactory
					.createPieChart("", new DefaultPieDataset(dkv), showLegend,
							showTooltips, false);
			setChartProperties(props, chart);
			PiePlot plot = (PiePlot) chart.getPlot();

			for (int i = 0; i < values.size(); i++) {
				String label = (String) labels.elementAt(i);
				BrokerFactory.getLoggingBroker().logDebug(
						"Pie section " + i + " label = " + label);

				if ((label == null) || (label.equals(""))
						|| (label.equalsIgnoreCase("active"))) {
					plot.setSectionPaint(i, new Color (0x02, 0xcd, 0x34));
				} else if (label.equalsIgnoreCase("confirmed")) {
					plot.setSectionPaint(i, new Color (0xff, 0xd8, 0x01));
				} else if (label.equalsIgnoreCase("expired")) {
					plot.setSectionPaint(i, new Color (0xff, 0, 0));
				}
			}
			return chart.createBufferedImage(width * quality, height * quality);
		} else if (chartName.equals(BAR) || chartName.equals(BAR3D)) {
			Vector values = getSeries("serie0", props, scriptlet);
			Vector theCategories = getSeries("serie1", props, scriptlet);
			Vector theSeries = getSeries("serie2", props, scriptlet);

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			if (scriptlet == null) {
				dataset = getSampleCategoryDataset();
			} else {
				for (int i = 0; i < values.size(); ++i) {

					String category = (i + 1) + "";
					if (theCategories != null && theCategories.size() > i) {
						category = "" + theCategories.get(i);
					}

					String theSerie = "";
					if (theSeries != null && theSeries.size() > i) {
						theSerie = "" + theSeries.get(i);
					}

					dataset.addValue(new Double(""
							+ ((values.get(i) != null) ? values.get(i) : "0")),
							theSerie, category);
				}
			}

			int plotOrientation = getParameterAsInteger("plotOrientation",
					props, 1);

			JFreeChart chart = null;

			if (chartName.equals(BAR)) {
				chart = org.jfree.chart.ChartFactory.createBarChart("", Misc
						.nvl(props.getProperty("categoryLabel"), ""), Misc.nvl(
						props.getProperty("valueLabel"), ""), dataset,
						(plotOrientation == 1) ? PlotOrientation.HORIZONTAL
								: PlotOrientation.VERTICAL, // orientation
						showLegend, // include legend
						showTooltips, // tooltips?
						false // URLs?
						);
			} else {
				chart = org.jfree.chart.ChartFactory.createBarChart3D("", Misc
						.nvl(props.getProperty("categoryLabel"), ""), Misc.nvl(
						props.getProperty("valueLabel"), ""), dataset,
						(plotOrientation == 1) ? PlotOrientation.HORIZONTAL
								: PlotOrientation.VERTICAL, // orientation
						showLegend, // include legend
						showTooltips, // tooltips?
						false // URLs?
						);
			}
			setChartProperties(props, chart);
			chart.getCategoryPlot()
					.setRenderer(
							new NotificationBarRenderer(theCategories,
									(BarRenderer) chart.getCategoryPlot()
											.getRenderer()));
			return chart.createBufferedImage(width * quality, height * quality);
		} else if (chartName.equals(LINE)) {
			Vector valuesX = getSeries("serie0", props, scriptlet);
			Vector valuesY = getSeries("serie1", props, scriptlet);
			Vector theSeries = getSeries("serie2", props, scriptlet);

			//XYSeries dataset = new XYSeries("");
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int plotOrientation = getParameterAsInteger("plotOrientation",
					props, 1);
			if (scriptlet == null) {
				dataset = getSampleCategoryDataset();
			} else {
				for (int i = 0; i < valuesX.size(); ++i) {
					String theSerie = "";
					if (theSeries != null && theSeries.size() > i) {
						theSerie = "" + theSeries.get(i);
					}

					dataset.addValue(
							(Number) new Double(""
									+ ((valuesX.get(i) != null) ? valuesX
											.get(i) : "0")),
							(Comparable) theSerie, (Comparable) new Double(""
									+ ((valuesY.get(i) != null) ? valuesY
											.get(i) : "0")));
				}
			}

			JFreeChart chart = null;

			chart = org.jfree.chart.ChartFactory.createLineChart("", Misc.nvl(
					props.getProperty("categoryLabel"), ""), Misc.nvl(props
					.getProperty("valueLabel"), ""), dataset,
					(plotOrientation == 1) ? PlotOrientation.HORIZONTAL
							: PlotOrientation.VERTICAL, // orientation
					showLegend, // include legend
					showTooltips, // tooltips?
					false // URLs?
					);

			setChartProperties(props, chart);
			chart.getCategoryPlot().setRenderer(
					new NotificationItemRenderer(valuesY));
			return chart.createBufferedImage(width * quality, height * quality);
		} else if (chartName.equals(AREA)) {
			Vector valuesX = getSeries("serie0", props, scriptlet);
			Vector valuesY = getSeries("serie1", props, scriptlet);
			Vector theSeries = getSeries("serie2", props, scriptlet);

			//XYSeries dataset = new XYSeries("");
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int plotOrientation = getParameterAsInteger("plotOrientation",
					props, 1);

			if (scriptlet == null) {
				dataset = getSampleCategoryDataset();
			} else {
				for (int i = 0; i < valuesX.size(); ++i) {
					String theSerie = "";
					if (theSeries != null && theSeries.size() > i) {
						theSerie = "" + theSeries.get(i);
					}

					dataset.addValue(
							(Number) new Double(""
									+ ((valuesX.get(i) != null) ? valuesX
											.get(i) : "0")),
							(Comparable) theSerie, (Comparable) new Double(""
									+ ((valuesY.get(i) != null) ? valuesY
											.get(i) : "0")));
				}

			}
			JFreeChart chart = null;

			chart = org.jfree.chart.ChartFactory.createAreaChart("", Misc.nvl(
					props.getProperty("categoryLabel"), ""), Misc.nvl(props
					.getProperty("valueLabel"), ""), dataset,
					(plotOrientation == 1) ? PlotOrientation.HORIZONTAL
							: PlotOrientation.VERTICAL, // orientation
					showLegend, // include legend
					showTooltips, // tooltips?
					false // URLs?
					);

			setChartProperties(props, chart);
			CategoryPlot cplot = (CategoryPlot) chart.getPlot();
			cplot.setForegroundAlpha((float) getParameterAsDouble(
					"foregroundAlpha", props, 0.0));
			chart.getCategoryPlot().setRenderer(
					new NotificationItemRenderer(valuesY));

			return chart.createBufferedImage(width * quality, height * quality);
		}

		return null;
	}

}

class NotificationItemRenderer extends DefaultCategoryItemRenderer {
	Vector names;

	public NotificationItemRenderer(Vector names) {
		this.names = names;
	}

	public Paint getItemPaint(int row, int column) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting item paint for " + row + ":" + column);
		String name = (String) names.elementAt(column);

		if ((name == null) || (name.equals(""))
				|| (name.equalsIgnoreCase("active"))) {
			return new Color (0x02, 0xcd, 0x34);
		}
		if (name.equalsIgnoreCase("confirmed")) {
			return new Color (0xff, 0xd8, 0x01);
		}
		if (name.equalsIgnoreCase("expired")) {
			return new Color (0xff, 0, 0);
		}

		return super.getItemPaint(row, column);
	}
}

class NotificationBarRenderer extends BarRenderer {
	Vector names;

	BarRenderer renderer;

	public NotificationBarRenderer(Vector names, BarRenderer renderer) {
		this.names = names;
		this.renderer = renderer;
	}

	public Paint getItemPaint(int row, int column) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting item paint for " + row + ":" + column);
		String name = (String) names.elementAt(column);

		if ((name == null) || (name.equals(""))
				|| (name.equalsIgnoreCase("active"))) {
			return new Color (0x02, 0xcd, 0x34);
		}
		if (name.equalsIgnoreCase("confirmed")) {
			return new Color (0xff, 0xd8, 0x01);
		}
		if (name.equalsIgnoreCase("expired")) {
			return new Color (0xff, 0, 0);
		}

		switch (column) {
		case 0:
			return Color.RED;
		case 1:
			return Color.BLUE;
		case 2:
			return Color.GREEN;
		case 3:
			return Color.YELLOW;
		case 4:
			return Color.ORANGE;
		case 5:
			return Color.MAGENTA;
		}
		return super.getItemPaint(row, column);
	}

}