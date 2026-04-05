package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class InsightsFragment extends Fragment {

    private CombinedChart chart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);
        
        chart = view.findViewById(R.id.combined_chart);
        setupChart();
        
        return view;
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);
        
        // draw bars behind lines
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(4f);
        rightAxis.setAxisMaximum(8f);
        rightAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(5f);
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Very Bad", "Bad", "Okay", "Good", "Excellent"}));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        final String[] dates = new String[]{"Apr 27", "Apr 30", "May 3", "May 6", "May 9"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));

        CombinedData data = new CombinedData();

        data.setData(generateLineData());
        data.setData(generateBarData());

        chart.setData(data);
        chart.invalidate();
    }

    private LineData generateLineData() {
        LineData d = new LineData();
        ArrayList<Entry> entries = new ArrayList<>();

        // Fake data for Mood (Line)
        entries.add(new Entry(0, 3.8f));
        entries.add(new Entry(1, 3.0f));
        entries.add(new Entry(2, 3.8f));
        entries.add(new Entry(3, 2.5f));
        entries.add(new Entry(4, 3.7f));

        LineDataSet set = new LineDataSet(entries, "Mood");
        set.setColor(Color.parseColor("#26A69A"));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.parseColor("#26A69A"));
        set.setCircleRadius(5f);
        set.setFillColor(Color.parseColor("#26A69A"));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(false);
        
        d.addDataSet(set);
        return d;
    }

    private BarData generateBarData() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Fake data for Sleep (Bar)
        entries.add(new BarEntry(0, 7f));
        entries.add(new BarEntry(0.5f, 6.5f));
        entries.add(new BarEntry(1, 6f));
        entries.add(new BarEntry(1.5f, 7.1f));
        entries.add(new BarEntry(2, 6.2f));
        entries.add(new BarEntry(2.5f, 5.5f));
        entries.add(new BarEntry(3, 6.8f));
        entries.add(new BarEntry(3.5f, 5.8f));
        entries.add(new BarEntry(4, 7f));

        BarDataSet set = new BarDataSet(entries, "Sleep (hrs)");
        set.setColor(Color.parseColor("#B39DDB"));
        set.setDrawValues(false);
        
        float barWidth = 0.2f; 
        BarData d = new BarData(set);
        d.setBarWidth(barWidth);
        
        return d;
    }
}