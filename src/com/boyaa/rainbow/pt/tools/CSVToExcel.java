package com.boyaa.rainbow.pt.tools;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import com.boyaa.rainbow.pt.service.RainbowPTService;

import android.util.Log;




public class CSVToExcel {
	public static String csvFileName;
	public static String xlsFilePath;
	public static String xlsFileName;
	public static String xlsFileNameNoEx;
	private final static String LOG_TAG = "Rainbow-" + CSVToExcel.class.getSimpleName();
	
	public static void convertToXLS(String resFilePath) throws IOException{	
		makeExcelFileInfo(resFilePath, ".xls");		
		ArrayList<ArrayList<String>> arList=null;
		ArrayList<String> al=null;		
		String thisLine;		
		FileInputStream fis = new FileInputStream(resFilePath);			
		 InputStreamReader isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
		 BufferedReader myInput = new BufferedReader(isr);
		arList = new ArrayList<ArrayList<String>>();
		while((thisLine = myInput.readLine()) != null){
			al = new ArrayList<String>();
			String strar[] = thisLine.split(",");
			for(int j=0;j<strar.length;j++){
				al.add(strar[j]);
			}
			arList.add(al);
			System.out.println();
		}
		try{
			myInput.close();
			isr.close();
			fis.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			HSSFWorkbook hwb = new HSSFWorkbook();
			HSSFSheet sheet = hwb.createSheet(xlsFileNameNoEx);			
			for(int k=0;k<arList.size();k++){
				ArrayList<String> ardata = (ArrayList<String>)arList.get(k);
				HSSFRow row = sheet.createRow((short) 0+k);
				for(int p=0;p<ardata.size();p++){				
					HSSFCell cell = row.createCell(p);					
					String data = ardata.get(p).toString();				
					if(data.startsWith("=")){
						cell.setCellType(Cell.CELL_TYPE_STRING);
						data=data.replaceAll("\"", "");
						data=data.replaceAll("=", "");
						cell.setCellValue(data);
					}else if(data.startsWith("\"")){
						data=data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(data);
					}else{
						data=data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(data);
					}
					//*/
					// cell.setCellValue(ardata.get(p).toString());
				}
				System.out.println();
			}
			FileOutputStream fileOut = new FileOutputStream(xlsFilePath);
			hwb.write(fileOut);
			fileOut.close();
			System.out.println("Your excel file has been generated");			
			new File(resFilePath).delete();			
			
		} catch ( Exception ex ) {
			ex.printStackTrace();
		} 
	}
	
	/**
	 * 
	 * @param filePath
	 * @param fileExtension
	 */
	public static void totalStatistical(String excelFilePath) throws Exception{		
		ArrayList<String> resultValue = null;
		Map<String, ArrayList<String>> result = dataCalculation(excelFilePath);
		FileInputStream fis = new FileInputStream(excelFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(fis);
		HSSFSheet dataSourceSheet = wb.getSheetAt(0);
		//int shiftRowNumber = 7;
		int shiftRowNumber = 10;
		int rows = dataSourceSheet.getLastRowNum();	
		//int dataStartRow = 10;
		//int dataStartRow = CellFieldConstants.Time.getRow() + 1;
//		int dataStartRow = CellFieldConstants.Time.getRow() + 3;
		int dataStartRow=CellFieldConstants.Time.getRow()+4;//原来表格上方没有总流量，新增总流量行
		//dataSourceSheet.shiftRows(9, rows, shiftRowNumber);				
		dataSourceSheet.shiftRows(CellFieldConstants.Time.getRow(), rows, shiftRowNumber); //将采样数据向下移动shiftRowNumber变量指定的行数
		CellFieldConstants.Time.setRowColumn(CellFieldConstants.Time.getRow() + shiftRowNumber, 0);//移动后，更新CellFieldConstants.Time常量所在开始行与列
		CellFieldConstants.Measurement.setRowColumn(dataStartRow, 0);//移动后，更新CellFieldConstants.Measurement常量所在开始行与列
		
		try{
			//增加测试时长到Excel表格中
//			HSSFRow dataRow_timeDuration = dataSourceSheet.createRow(dataStartRow - 4);
			HSSFRow dataRow_timeDuration = dataSourceSheet.createRow(dataStartRow - 5);//需要在时间下增加一行总流量
			dataRow_timeDuration.createCell(0).setCellValue("测试时长(小时:分:秒)：");
			dataRow_timeDuration.createCell(1).setCellValue(result.get("Net").get(7));
			
			HSSFRow dataRow_netTotal = dataSourceSheet.createRow(dataStartRow - 4);
			dataRow_netTotal.createCell(0).setCellValue("应用使用总流量(KB)：");
			dataRow_netTotal.createCell(1).setCellValue(result.get("Net").get(1));
			
			//增加平均流量到Excel表格中
			HSSFRow dataRow_netAvg = dataSourceSheet.createRow(dataStartRow - 3);	
			dataRow_netAvg.createCell(0).setCellValue("应用平均流量(KB/s)：");
			dataRow_netAvg.createCell(1).setCellValue(result.get("Net").get(6));
			
			//增加电量消耗到Excel表格中
			HSSFRow dataRow_batteryConsumption = dataSourceSheet.createRow(dataStartRow - 2);	
			dataRow_batteryConsumption.createCell(0).setCellValue("电量消耗(%)：");
			dataRow_batteryConsumption.createCell(1).setCellValue(result.get("Battery").get(0));

		}catch(Exception e){
			Log.e(LOG_TAG, "增加电量消耗到Excel表格中：");
			e.printStackTrace();
		}

		HSSFRow dataRow = dataSourceSheet.createRow(dataStartRow);	
		dataRow.createCell(0).setCellValue("度量(Measurement)");
		dataRow.createCell(1).setCellValue("最小值(Min)");
		dataRow.createCell(2).setCellValue("最大值(Max)");
		dataRow.createCell(3).setCellValue("平均值(Avg)");
		dataRow.createCell(4).setCellValue("中间值(Median)");
		dataRow.createCell(5).setCellValue("标准差(SD)");
		dataRow.createCell(6).setCellValue("方差(Variance)");	
		int r_start = dataStartRow + 1;
//		int r_end = r_start + 2;
		int r_end = r_start + 2;//去掉总流量行，增加FPS统计
		for(int r = r_start; r <= r_end; r++){
			dataRow = dataSourceSheet.createRow(r);				
			//使用if语句代替switch
			if(r == r_start){
				dataRow.createCell(0).setCellValue("应用占用CPU率(%)");
				resultValue = result.get("CPU");
			}else if(r == (r_start + 1)){
				dataRow.createCell(0).setCellValue("应用占用内存PSS(MB)");
				resultValue = result.get("Memory");
			}
			else{
				dataRow.createCell(0).setCellValue("应用FPS(帧)");
				resultValue = result.get("Fps");
			} //去掉总流量行，总流量行中的平均值计算不正确，而且意义不大，暂时删除
			
			for(int c = 1; c <= 6; c++){
				dataRow.createCell(c).setCellValue(resultValue.get(c-1));
			}
		}
		FileOutputStream fileOut = new FileOutputStream(excelFilePath);
		wb.write(fileOut);
		fileOut.close();
		fis.close();		
	}
	
	public static  Map<String, ArrayList<String>> dataCalculation(String excelFilePath) throws Exception{
		ArrayList<String> timeAL = new  ArrayList<String>();
		ArrayList<Double> cpuAL = new  ArrayList<Double>();
		ArrayList<Double> memoryAL = new  ArrayList<Double>();
		ArrayList<Double> netAL = new  ArrayList<Double>();
		ArrayList<Double> batteryAL = new  ArrayList<Double>();
		ArrayList<Double> FPSAL = new ArrayList<Double>();
		
		ArrayList<String> tmpAL = null;	
		DecimalFormat fomart;
		fomart = new DecimalFormat();
		fomart.setMaximumFractionDigits(2);
		fomart.setMinimumFractionDigits(0);
		Map<String, ArrayList<String>> resultData = new HashMap<String, ArrayList<String>>();
		FileInputStream fis = new FileInputStream(excelFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(fis);
		HSSFSheet dataSourceSheet = wb.getSheetAt(0);		
		int rows = dataSourceSheet.getLastRowNum();	
		//int dataStartRow = 10;
		int dataStartRow = CellFieldConstants.Time.getRow() + 1;
		String timeValue;
		double dataValue;		
		for(int r = dataStartRow; r <= rows; r++){
			HSSFRow dataRow = dataSourceSheet.getRow(r);
			if (dataRow == null) {//跳过空行
				continue;
			}		    			
			timeValue = dataRow.getCell(0).getStringCellValue().trim();
			if(timeValue.equals("") || timeValue.length() == 0){
				break;
			}
			timeAL.add(timeValue);
			dataValue = Double.parseDouble(dataRow.getCell(CellFieldConstants.AppCPU.getColumn()).getStringCellValue().trim());
			cpuAL.add(dataValue);			
			dataValue = Double.parseDouble(dataRow.getCell(CellFieldConstants.AppMem.getColumn()).getStringCellValue().trim());
			memoryAL.add(dataValue);
			dataValue = Double.parseDouble(dataRow.getCell(CellFieldConstants.Traffic.getColumn()).getStringCellValue().trim());
			netAL.add(dataValue);
			dataValue = Double.parseDouble(dataRow.getCell(CellFieldConstants.Battery.getColumn()).getStringCellValue().trim());
			batteryAL.add(dataValue);
			dataValue = Double.parseDouble(dataRow.getCell(CellFieldConstants.Fps.getColumn()).getStringCellValue().trim());
			FPSAL.add(dataValue);			
			
		}
		double sdData = 0.0;
		tmpAL = new  ArrayList<String>();		
		tmpAL.add(fomart.format(getMin(cpuAL)));//最小值
		tmpAL.add(fomart.format(getMax(cpuAL)));//最大值
		tmpAL.add(fomart.format(getAverage(cpuAL)));//平均值
		tmpAL.add(fomart.format(getMedia(cpuAL)));//中间值
		sdData = getStandardDevition(cpuAL);
		tmpAL.add(fomart.format(sdData));//标准差
		tmpAL.add(fomart.format(sdData * sdData));//方差
		resultData.put("CPU", tmpAL);
		
		tmpAL = new  ArrayList<String>();		
		tmpAL.add(fomart.format(getMin(memoryAL)));//最小值
		tmpAL.add(fomart.format(getMax(memoryAL)));//最大值
		tmpAL.add(fomart.format(getAverage(memoryAL)));//平均值
		tmpAL.add(fomart.format(getMedia(memoryAL)));//中间值
		sdData = getStandardDevition(memoryAL);
		tmpAL.add(fomart.format(sdData));//标准差
		tmpAL.add(fomart.format(sdData * sdData));//方差
		resultData.put("Memory", tmpAL);
		
		tmpAL = new ArrayList<String>();
		tmpAL.add(fomart.format(getMin(FPSAL)));
		tmpAL.add(fomart.format(getMax(FPSAL)));
		tmpAL.add(fomart.format(getAverage(FPSAL)));
		tmpAL.add(fomart.format(getMedia(FPSAL)));
		sdData = getStandardDevition(FPSAL);
		tmpAL.add(fomart.format(sdData));
		tmpAL.add(fomart.format(sdData * sdData));
		resultData.put("Fps", tmpAL);
		
		
		tmpAL = new  ArrayList<String>();		
		//最大值
		Double netMax = getMax(netAL);
		//最小值
		Double netMin = getMin(netAL);
		//平均每秒流量
		Double netAvg = 0.0;
		String durationTime = "0";
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			long durationSeconds = (timeFormatter.parse(timeAL.get(timeAL.size() -1)).getTime() - (timeFormatter.parse(timeAL.get(0)).getTime()))/1000;
			long seconds = durationSeconds % 60;
			long durationMinutes = durationSeconds / 60;
			long minutes = durationMinutes % 60;
			long durationHours = durationMinutes / 60;			
			durationTime = String.valueOf(durationHours) + ":" +  String.valueOf(minutes)  + ":" +  String.valueOf(seconds); 
			//计算平均每秒流量
			netAvg = (netMax - netMin)/durationSeconds;
		}catch(Exception e){
			e.printStackTrace();			
		}
		tmpAL.add(fomart.format(netMin));//最小值
		tmpAL.add(fomart.format(netMax));//最大值		
		tmpAL.add(fomart.format(getAverage(netAL)));//平均值		
		tmpAL.add(fomart.format(getMedia(netAL)));//中间值
		sdData = getStandardDevition(netAL);
		tmpAL.add(fomart.format(sdData));//标准差
		tmpAL.add(fomart.format(sdData * sdData));//方差
		tmpAL.add(fomart.format(netAvg));//平均每秒流量
		tmpAL.add(durationTime);//测试时长			
		resultData.put("Net", tmpAL);
		
		
		tmpAL = new  ArrayList<String>();
		double batteryConsumption = -1;
		try{
			batteryConsumption =  batteryAL.get(0) - batteryAL.get(batteryAL.size() -1);			
		}catch(Exception e){
			Log.e(LOG_TAG, "计算电量消耗出现错误");
			e.printStackTrace();
		}
		if(batteryConsumption < 0){
			tmpAL.add("N/A");//电量消耗数据不正常
		}else if(batteryConsumption == 0.0){
			tmpAL.add("<1%");//电量消耗小于1%
		}else{
			tmpAL.add(fomart.format(batteryConsumption));//电量消耗
		}
		resultData.put("Battery", tmpAL);
		
		
		try{
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultData;
	}
	
	
	public static double getMedia(List<Double> list) {
		double media = 0.0;
		Double[] myArray = (Double[])(list.toArray(new Double[0]));
		Arrays.sort(myArray);
		int len = myArray.length;		
		if(len % 2 == 0){
			media = (myArray[len / 2] + myArray[len / 2 -1])/2;
		}else{
			media = myArray[(len-1)/2];
		}		
		return media;
	}
	
	
	public static double getAverage(List<Double> list) {
		double sum = 0;
		for(int i=0;i<list.size();i++){
			sum += list.get(i);
		}
		return sum / list.size();
	}
	
	//求标准方差
	public static double getStandardDevition(List<Double> list){
		double sum = 0;
		double avg =  getAverage(list);
		int totalCount = list.size();
		/*for (int i = 0; i < totalCount; i++){
			 sum += Math.sqrt((list.get(i) -avg) * (list.get(i)-avg));			
		}		
		return (sum / (totalCount - 1));
		*/
		for (int i = 0; i < totalCount; i++){
			 sum += (list.get(i) -avg) * (list.get(i)-avg);
		}
		sum /= totalCount;		
		return Math.sqrt(sum);
		
	}

	
	//获取ArrayList中的最大值
	public static double getMax(List<Double> list){
		double maxDevation = 0.0;
		try {			
			int totalCount = list.size();
			if (totalCount >= 1){
				double max = list.get(0);
				for (int i = 0; i < totalCount; i++){
					double temp = list.get(i);
					if (temp > max){
						max = temp;
					}
				} 
				maxDevation = max;
			}
			return maxDevation;
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return maxDevation;
	}
	
	//获取ArrayList中的最小值
	public static double getMin(List<Double> list){
		double minDevation = 0.0;
		try {			
			int totalCount = list.size();
			if (totalCount >= 1){
				double min = list.get(0);
				for (int i = 0; i < totalCount; i++){
					double temp = list.get(i);
					if (min > temp){
						min = temp;
					}
				} 
				minDevation = min;
			}
			return minDevation;
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return minDevation;
	}

	

	/* 
	 * 根据CSV文件路径生成Excel文件名与路径相关信息
	 */  
	public static void makeExcelFileInfo(String filePath, String fileExtension) {
		File file = new File(filePath);	
		if(file.exists()){
			String filename = file.getName();
			if ((filename != null) && (filename.length() > 0)) {   
				int dot = filename.lastIndexOf('.');   
				if ((dot >-1) && (dot < (filename.length()))) { 
					xlsFileNameNoEx = filename.substring(0, dot);
					xlsFileName  =  xlsFileNameNoEx + fileExtension; 
					xlsFilePath = file.getParent() + File.separator + xlsFileName;
					
				}   
			}
		}
	} 
}
