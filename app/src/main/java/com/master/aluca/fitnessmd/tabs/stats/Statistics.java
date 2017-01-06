package com.master.aluca.fitnessmd.tabs.stats;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.bluetooth.DBHelper;
import com.master.aluca.fitnessmd.datatypes.StepsDayReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aluca on 11/7/16.
 */
public class Statistics {

    private static final String LOG_TAG = "Fitness_Statistics";

    private static Statistics mInstance = null;
    private com.github.mikephil.charting.charts.BarChart mChart;
    TextView tvStatsAverageSteps, tvStatsTotalSteps;


    private Context mContext = null;
    private MainActivity mMainActivity;
    private DBHelper mDB = null;
    private TextView tvDateToday;

    /*public static Statistics getInstance(Context context, MainActivity mainActivity) {
        if (mInstance == null) {
            mInstance = new Statistics(context, mainActivity);
        }
        return mInstance;
    }*/

    public Statistics(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG, "Statistics");
        mContext = context;
        mMainActivity = mainActivity;
        if(mDB == null) {
            mDB = new DBHelper(mContext).openWritable();
        }
    }

    public void setup() {
        tvDateToday = (TextView) mMainActivity.findViewById(R.id.tvDateToday);
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDateToday.setText(s.format(new Date()));

        tvStatsAverageSteps = (TextView) mMainActivity.findViewById(R.id.tvStatsAverageSteps);
        tvStatsTotalSteps = (TextView) mMainActivity.findViewById(R.id.tvStatsTotalSteps);

        setStatsAverageSteps();
        setStatsTotalSteps();

        mChart = (com.github.mikephil.charting.charts.BarChart) mMainActivity.findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setForm(LegendForm.NONE);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setScaleXEnabled(false);
        mChart.setScaleYEnabled(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter();

        mChart.setDrawValueAboveBar(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);
        //xAxis.setLabelRotationAngle(20);
        xAxis.setTextColor(mMainActivity.getResources().getColor(R.color.tab_menu_background));
        //xAxis.setTextSize(10f);



        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMaximum(10000);
        rightAxis.setAxisMinimum(0);


        /*
            TODO - set limit based on DoctorTab recommendation
         */
        setData(7, 10000);
        if (mChart.getData() != null)
            mChart.getData().setHighlightEnabled(false);
    }

    private void setData(int count, float range) {

        ArrayList<StepsDayReport> stepsDayReports = mDB.getLastWeekReport(System.currentTimeMillis());
        Log.d(LOG_TAG,"stepsDayReports.size : " + stepsDayReports.size());


        Log.d(LOG_TAG, "count : " + count + " >>> range : " + range);
        ArrayList<BarEntry> barValues = new ArrayList<>();
        /*for (int i = 1; i < count + 1; i++) {
            float mult = (range + 1);
            int val = (int) (Math.random() * mult);
            Log.d(LOG_TAG, "mult : " + mult);
            Log.d(LOG_TAG, "val : " + val);
            barValues.add(new BarEntry(-1-i, val));
        }*/

        String dateFormat = "EEE,MMM d";
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        Calendar cal = Calendar.getInstance();
        if (stepsDayReports.size() == 0) {
            Log.d(LOG_TAG, "NO DATA CHART TEXT");
            Paint paint = mChart.getPaint(Chart.PAINT_INFO);
            paint.setTextSize(24);
            mChart.setNoDataText("No results for last 7 days");
            mChart.setNoDataTextColor(mContext.getResources().getColor(R.color.tab_menu_background));
            mChart.clear();
            mChart.invalidate();
        } else {
            for (int i = 0; i < stepsDayReports.size(); i++) {
                barValues.add(new BarEntry(-2-i, stepsDayReports.get(i).getSteps()));
                //barValues.add(new BarEntry(s.format(new Date(stepsDayReports.get(i).getDay())),stepsDayReports.get(i).getSteps()));
                Log.d(LOG_TAG,"stepsDayReports.size : " + stepsDayReports.size());
                Log.d(LOG_TAG,"stepsDayReports.get(i).getSteps() : " + stepsDayReports.get(i).getSteps());
                //String yesterday = getCalculatedDate((int) (value + 1));
                //Log.d(LOG_TAG, (int)value + " day(s) ago was : " + yesterday);

                //cal.add(Calendar.DAY_OF_YEAR, days);
                //return s.format(new Date(cal.getTimeInMillis()));
            }
            Log.d(LOG_TAG, "create mchart data");
            BarDataSet set1 = new BarDataSet(barValues, "");
            set1.setColors(ColorTemplate.MATERIAL_COLORS);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueTextSize(10);
            //data.setBarWidth(0.8f);
            mChart.setData(data);
        }
    }

    private void setStatsAverageSteps() {
        StepsDayReport averageStepsRaport = mDB.getAverageSteps();
        int averageSteps = averageStepsRaport.getSteps();
        tvStatsAverageSteps.setText(String.valueOf(averageSteps));
    }
    private void setStatsTotalSteps() {
        StepsDayReport averageStepsRaport = mDB.getTotalSteps();
        int averageSteps = averageStepsRaport.getSteps();
        tvStatsTotalSteps.setText(String.valueOf(averageSteps));
    }
}
