package com.netmonitor.ui;

import com.netmonitor.model.BandwidthSample;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * Real-time dynamic bandwidth usage chart powered by JFreeChart.
 * Renders dual-series (Download & Upload) with dark dashboard aesthetics and smooth sliding time window.
 */
public class BandwidthChartPanel extends JPanel {

    private final XYSeries downloadSeries;
    private final XYSeries uploadSeries;
    private final XYSeriesCollection dataset;
    private final JFreeChart chart;
    private final ChartPanel chartPanel;

    private boolean showInMbps = false;
    private double timeSec = 0;

    public BandwidthChartPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_CARD);
        setBorder(UITheme.createCardBorder());

        // Maximum 120 data points (2 minutes at 1s intervals)
        downloadSeries = new XYSeries("Download", true, false);
        downloadSeries.setMaximumItemCount(120);

        uploadSeries = new XYSeries("Upload", true, false);
        uploadSeries.setMaximumItemCount(120);

        dataset = new XYSeriesCollection();
        dataset.addSeries(downloadSeries);
        dataset.addSeries(uploadSeries);

        chart = ChartFactory.createXYLineChart(
                "Real-Time Bandwidth Usage",
                "Time (seconds elapsed)",
                "Speed (KB/s)",
                dataset,
                PlotOrientation.VERTICAL,
                true,   // Legend
                true,   // Tooltips
                false   // URLs
        );

        applyDarkTheme();

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(UITheme.BG_CARD);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(true);

        // Header controls (Title + Unit Switch)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel title = new JLabel("LIVE TRAFFIC VISUALIZER");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);

        JToggleButton unitToggle = new JToggleButton("Show in Mbps");
        unitToggle.setFont(UITheme.FONT_SMALL);
        unitToggle.setFocusPainted(false);
        unitToggle.addActionListener(e -> {
            showInMbps = unitToggle.isSelected();
            unitToggle.setText(showInMbps ? "Showing in Mbps" : "Show in Mbps");
            NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
            rangeAxis.setLabel(showInMbps ? "Speed (Mbps)" : "Speed (KB/s)");
        });
        controls.add(unitToggle);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void applyDarkTheme() {
        chart.setBackgroundPaint(UITheme.BG_CARD);
        chart.getTitle().setPaint(UITheme.TEXT_PRIMARY);
        chart.getTitle().setFont(UITheme.FONT_HEADER);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(UITheme.BG_CARD);
            chart.getLegend().setItemPaint(UITheme.TEXT_SECONDARY);
            chart.getLegend().setItemFont(UITheme.FONT_BODY);
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(20, 27, 45));
        plot.setDomainGridlinePaint(new Color(51, 65, 85, 120));
        plot.setRangeGridlinePaint(new Color(51, 65, 85, 120));
        plot.setOutlinePaint(UITheme.BORDER_COLOR);

        // Series Renderers & Colors
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, UITheme.ACCENT_CYAN);       // Download: Sky Cyan
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesPaint(1, UITheme.ACCENT_EMERALD);    // Upload: Emerald Green
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        plot.setRenderer(renderer);

        // Domain Axis (Time)
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setLabelPaint(UITheme.TEXT_MUTED);
        domainAxis.setTickLabelPaint(UITheme.TEXT_MUTED);
        domainAxis.setTickLabelFont(UITheme.FONT_SMALL);
        domainAxis.setAutoRange(true);
        domainAxis.setAutoRangeIncludesZero(false);

        // Range Axis (Speed)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelPaint(UITheme.TEXT_MUTED);
        rangeAxis.setTickLabelPaint(UITheme.TEXT_MUTED);
        rangeAxis.setTickLabelFont(UITheme.FONT_SMALL);
        rangeAxis.setAutoRangeIncludesZero(true);
    }

    public void addSample(BandwidthSample sample) {
        timeSec += 1.0;
        double downVal = showInMbps ? sample.getDownloadMbps() : sample.getDownloadSpeedKBps();
        double upVal = showInMbps ? sample.getUploadMbps() : sample.getUploadSpeedKBps();

        downloadSeries.add(timeSec, downVal);
        uploadSeries.add(timeSec, upVal);
    }

    public void clearChart() {
        downloadSeries.clear();
        uploadSeries.clear();
        timeSec = 0;
    }
}
