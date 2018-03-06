package com.boyaa.rainbow.pt.tools;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;



import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.ChartUtilities;
import org.afree.chart.axis.DateAxis;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.time.Day;
import org.afree.data.time.Hour;
import org.afree.data.time.Month;
import org.afree.data.time.Second;
import org.afree.data.time.TimeSeries;
import org.afree.data.time.TimeSeriesCollection;
import org.afree.data.xy.XYDataset;
import org.afree.graphics.SolidColor;
import org.afree.ui.RectangleInsets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;





public class CreateLineChart{
	
	public static String XAxisLabel = "采样时间(HH:mm:ss)";
	public static String yAxisLabel = "";
	public static String excelFilePath = "";
	public static AFreeChart cpuChart = null;	
	public static AFreeChart memoryChart = null;
	public static AFreeChart netChart = null;
	public static AFreeChart netSndRcvChart = null;
	public static AFreeChart fpsChart = null;
	
	 
	/**
	 * 创建一个Time Series Chart
	 * @param title ：图片标题
	 * @param timeAxisLabel：  x-axis label
	 * @param valueAxisLabel： y-axis label
	 * @param dataset： data
	 * @return
	 */
    private static AFreeChart createChart(String title, String timeAxisLabel, String valueAxisLabel, XYDataset dataset) {

        AFreeChart chart = ChartFactory.createTimeSeriesChart(
        	title,            // title
        	timeAxisLabel,    // x-axis label
        	valueAxisLabel,   // y-axis label
            dataset,          // data
            true,             // create legend?
            true,             // generate tooltips?
            false             // generate URLs?
        );

        chart.setBackgroundPaintType(new SolidColor(Color.YELLOW));
        XYPlot plot = (XYPlot) chart.getPlot();       
        setXYPolt(plot);    
        return chart;

    }
    
   

  /*  public static void setXYPolt(XYPlot plot) { 
    	plot.setBackgroundPaintType(new SolidColor(Color.LTGRAY));
    	plot.setDomainGridlinePaintType(new SolidColor(Color.WHITE));
    	plot.setRangeGridlinePaintType(new SolidColor(Color.WHITE));
    	//plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    	plot.setDomainCrosshairVisible(true);
    	plot.setRangeCrosshairVisible(true);

    	XYItemRenderer r = plot.getRenderer();
    	if (r instanceof XYLineAndShapeRenderer) {
    		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
    		renderer.setBaseShapesVisible(true);
    		renderer.setBaseShapesFilled(true);
    		renderer.setDrawSeriesLineAsPath(true);
    	}

    	DateAxis axis = (DateAxis) plot.getDomainAxis();
    	axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

    }*/
    
    public static void setXYPolt(XYPlot plot) {     	
    	plot.setDomainGridlinePaintType(new SolidColor(Color.LTGRAY));
    	plot.setRangeGridlinePaintType(new SolidColor(Color.LTGRAY));
    	//plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    	plot.setDomainCrosshairVisible(true);
    	plot.setRangeCrosshairVisible(false);

    	XYItemRenderer r = plot.getRenderer();
    	if (r instanceof XYLineAndShapeRenderer) {
    		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;    	
    		renderer.setBaseShapesVisible(true);
    		renderer.setBaseShapesFilled(true);    		
    	}

    	DateAxis axis = (DateAxis) plot.getDomainAxis();
    	axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

    }  

    /**
     * 创建单条折线图
     * @author JackWang
     * @param lineLable
     * @param excelFilePath
     * @param dataColumnIndex
     * @return
     * @throws Exception
     */
    private static XYDataset createDataset(String lineLable, String excelFilePath, int dataColumnIndex) throws Exception{
        TimeSeries tsLine = new TimeSeries(lineLable);
        FileInputStream fis = new FileInputStream(excelFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(fis);
		HSSFSheet dataSourceSheet = wb.getSheetAt(0);		
		int rows = dataSourceSheet.getLastRowNum();		
		//int sampleDataStartRow = 10;
		int sampleDataStartRow = CellFieldConstants.Time.getRow() + 1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String timeValue;
		String dataValue;		
		int sec,minute,hour,day,month,year; 

		//yAxisLabel = dataSourceSheet.getRow(9).getCell(dataColumnIndex).getStringCellValue().trim();
		yAxisLabel = dataSourceSheet.getRow(CellFieldConstants.Time.getRow()).getCell(dataColumnIndex).getStringCellValue().trim();
		for(int r = sampleDataStartRow; r <= rows; r++){
			HSSFRow dataRow = dataSourceSheet.getRow(r);
			if (dataRow == null) {//跳过空行
				continue;
			}		    			
			//timeValue = dataRow.getCell(0).getStringCellValue().trim();
			timeValue = dataRow.getCell( CellFieldConstants.Time.getColumn()).getStringCellValue().trim();
			if(timeValue.equals("") || timeValue.length() == 0){
				break;
			}			
			dataValue = dataRow.getCell(dataColumnIndex).getStringCellValue().trim();		
			sec = Integer.parseInt(timeValue.substring(17));
			minute = Integer.parseInt(timeValue.substring(14, 16));
			hour = Integer.parseInt(timeValue.substring(11, 13));
			day = Integer.parseInt(timeValue.substring(8, 10));
			month = Integer.parseInt(timeValue.substring(5, 7));
			year = Integer.parseInt(timeValue.substring(0, 4));					
			tsLine.add(new Second(sec,minute,hour,day,month,year), Double.parseDouble(dataValue));				
		}		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();		
		dataset.addSeries(tsLine);
        return dataset;

    }
	
    /**
     * 创建多条折线图
     * @author JackWang 
     * @param yAxisLabel
     * @param lineLable
     * @param excelFilePath
     * @return
     * @throws Exception
     */
    private static XYDataset createDatasetForMultiLine(String yAxisLabel, LinkedHashMap<String, String> lineLable, String excelFilePath) throws Exception{      
        FileInputStream fis = new FileInputStream(excelFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(fis);
		HSSFSheet dataSourceSheet = wb.getSheetAt(0);		
		int rows = dataSourceSheet.getLastRowNum();		
		//int sampleDataStartRow = 10;
		int sampleDataStartRow = CellFieldConstants.Time.getRow() + 1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String timeValue;
		String dataValue;		
		int sec,minute,hour,day,month,year; 
		TimeSeries tsLine = null;
		int dataColumnIndex;
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		Iterator<Entry<String, String>> iter = lineLable.entrySet().iterator();   
		while(iter.hasNext()){
			LinkedHashMap.Entry<String, String> entry = (LinkedHashMap.Entry<String, String>) iter.next(); 
			tsLine = new TimeSeries(entry.getKey());	
			dataColumnIndex = Integer.parseInt(entry.getValue());			
			for(int r = sampleDataStartRow; r <= rows; r++){
				HSSFRow dataRow = dataSourceSheet.getRow(r);
				if (dataRow == null) {//跳过空行
					continue;
				}		    			
				//timeValue = dataRow.getCell(0).getStringCellValue().trim();
				timeValue = dataRow.getCell( CellFieldConstants.Time.getColumn()).getStringCellValue().trim();
				if(timeValue.equals("") || timeValue.length() == 0){
					break;
				}			
				dataValue = dataRow.getCell(dataColumnIndex).getStringCellValue().trim();		
				sec = Integer.parseInt(timeValue.substring(17));
				minute = Integer.parseInt(timeValue.substring(14, 16));
				hour = Integer.parseInt(timeValue.substring(11, 13));
				day = Integer.parseInt(timeValue.substring(8, 10));
				month = Integer.parseInt(timeValue.substring(5, 7));
				year = Integer.parseInt(timeValue.substring(0, 4));					
				tsLine.add(new Second(sec,minute,hour,day,month,year), Double.parseDouble(dataValue));				
			}						
			dataset.addSeries(tsLine);
		}
		return dataset;
    }

    
	/**
	 * 生成图片
	 * @author JackWang 
	 */
	public static void makeChat(){	
		XYDataset cpuDataSet = null;
		XYDataset memoryDataSet = null;		
		XYDataset netDataSet = null;
		XYDataset netSndRcvDataSet = null;
		XYDataset fpsDataSet = null;
		String yAxisLabel_cpu = "";
		String yAxisLabel_memory = "";
		String yAxisLabel_net = "";
		String yAxisLabel_net_SndRcv = "";
		String yAxisLabel_fps = "";
		LinkedHashMap<String, String> xAixlineLable = new LinkedHashMap<String, String>();
		try {
			//cpuDataSet = createDataset("CPU", excelFilePath, 4);
			cpuDataSet = createDataset("CPU", excelFilePath, CellFieldConstants.AppCPU.getColumn());
			yAxisLabel_cpu = yAxisLabel; 			  
			//memoryDataSet = createDataset("Memory", excelFilePath, 1);
			memoryDataSet = createDataset("Memory", excelFilePath, CellFieldConstants.AppMem.getColumn());
			yAxisLabel_memory = yAxisLabel;
			//netDataSet = createDataset("Net", excelFilePath, 5);
			netDataSet = createDataset("Net", excelFilePath, CellFieldConstants.Traffic.getColumn());
			yAxisLabel_net = yAxisLabel;
			
			yAxisLabel_net_SndRcv = "发送与接收流量(KB)";
			xAixlineLable.put("Send", String.valueOf(CellFieldConstants.SendTraffic.getColumn()));
			xAixlineLable.put("Receive", String.valueOf(CellFieldConstants.ReceiveTraffic.getColumn()));
			netSndRcvDataSet = createDatasetForMultiLine(yAxisLabel_net_SndRcv, xAixlineLable, excelFilePath);
			
			fpsDataSet = createDataset("FPS", excelFilePath, CellFieldConstants.Fps.getColumn());
			yAxisLabel_fps = yAxisLabel;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cpuChart = createChart("CPU性能监控数据", XAxisLabel, yAxisLabel_cpu, cpuDataSet);	
		memoryChart = createChart("Memory性能监控数据", XAxisLabel, yAxisLabel_memory, memoryDataSet);
		netChart = createChart("Net性能监控数据", XAxisLabel, yAxisLabel_net, netDataSet);
		netSndRcvChart =  createChart("Net性能监控数据(发送/接收)", XAxisLabel, yAxisLabel_net_SndRcv, netSndRcvDataSet);
		fpsChart = createChart("FPS性能监控数据", XAxisLabel, yAxisLabel_fps, fpsDataSet);
	}
	
	public static void chartToImage(String sheetLable, Bitmap drawingCache) throws Exception{
		FileInputStream fis = new FileInputStream(excelFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(fis);	
		HSSFSheet cpuSheet = wb.createSheet(sheetLable);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
		drawingCache.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);       
        HSSFPatriarch patriarch= cpuSheet.createDrawingPatriarch();
       // HSSFClientAnchor anchor=new HSSFClientAnchor(0, 0, 512, 255, (short)1, 1, (short)10, 20);
        HSSFClientAnchor anchor=new HSSFClientAnchor(0, 0, 512, 255, (short)1, 1, (short)20, 40);  
        anchor.setAnchorType(3);       
        patriarch.createPicture(anchor, wb.addPicture(byteArrayOutputStream.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG));
		// Write the output to a file	      
		FileOutputStream fileOut = new FileOutputStream(excelFilePath);
		wb.write(fileOut);
		fileOut.close();
		fis.close();
	}

	
	

}
