package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class InsightsFragment extends Fragment {

    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);
        
        barChart = view.findViewById(R.id.sleep_bar_chart);
        setupSleepChart();
        
        return view;
    }

    private void setupSleepChart() {
        // Basic configuration
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        // Y-Axis (Left) - Showing text-like indicators
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setLabelCount(5);
        leftAxis.setTextColor(Color.parseColor("#BCBCBC"));
        leftAxis.setDrawAxisLine(false);
        // Custom formatting for left side to mimic "Goal" markers
        leftAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"DEEP", "", "REM", "", "LIGHT"}));

        // Y-Axis (Right) - Showing 1, 2, 3 as in the image
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(4f);
        rightAxis.setLabelCount(4, true);
        rightAxis.setTextColor(Color.parseColor("#BCBCBC"));
        rightAxis.setDrawAxisLine(false);

        // X-Axis (Days)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#BCBCBC"));
        xAxis.setGranularity(1f);
        xAxis.setYOffset(10f);
        
        final String[] days = new String[]{"29/30", "1/02", "3/04", "5/06", "7/08", "9/10", "11/12"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        // Add the Yellow Limit Line (Target/Average line)
        LimitLine ll = new LimitLine(6.5f, "");
        ll.setLineColor(Color.parseColor("#FBC02D"));
        ll.setLineWidth(2f);
        leftAxis.addLimitLine(ll);

        // Generate Sample Data
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 6.2f));
        entries.add(new BarEntry(1, 5.8f));
        entries.add(new BarEntry(2, 8.4f));
        entries.add(new BarEntry(3, 7.5f));
        entries.add(new BarEntry(4, 6.4f));
        entries.add(new BarEntry(5, 5.2f));
        entries.add(new BarEntry(6, 6.8f));

        BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
        
        // Periwinkle/Blue color from image
        dataSet.setColor(Color.parseColor("#8BA4F7"));
        dataSet.setDrawValues(false); 
        dataSet.setHighLightColor(Color.parseColor("#FFD54F")); // Marker color
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f); 

        barChart.setData(barData);
        barChart.animateY(1000); // Add a nice entrance animation
        barChart.invalidate();
    }
}