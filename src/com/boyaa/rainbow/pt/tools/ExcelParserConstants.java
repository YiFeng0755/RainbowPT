package com.boyaa.rainbow.pt.tools;

/**
 *Customize the location of suite/test data that the parser retrieves from Excel files.
 *  
 * @author JackWang
 *
 */
public class ExcelParserConstants {
	
	public static int time_cell_row = 9;
	public static int time_cell_column  = 0;	
	public static int statistical_cell_row = 11;
	public static int statistical_cell_column = 0;
	
	public static void initialConstants(){
		//Sample data cell location defined
		CellFieldConstants.Time.setRowColumn(time_cell_row, time_cell_column);
		CellFieldConstants.TopActivity.setRowColumn(time_cell_row, time_cell_column + 1);
		CellFieldConstants.AppMem.setRowColumn(time_cell_row, time_cell_column + 2);
		CellFieldConstants.AppMemRatio.setRowColumn(time_cell_row, time_cell_column + 3);
		CellFieldConstants.FreeMemory.setRowColumn(time_cell_row, time_cell_column + 4);
		CellFieldConstants.AppCPU.setRowColumn(time_cell_row, time_cell_column + 5);
		CellFieldConstants.Traffic.setRowColumn(time_cell_row, time_cell_column + 6);
		CellFieldConstants.SendTraffic.setRowColumn(time_cell_row, time_cell_column + 7);
		CellFieldConstants.ReceiveTraffic.setRowColumn(time_cell_row, time_cell_column + 8);
		CellFieldConstants.Fps.setRowColumn(time_cell_row, time_cell_column+9);
		CellFieldConstants.Battery.setRowColumn(time_cell_row, time_cell_column + 14);
		
		//Statistical data cell location defined
		CellFieldConstants.Measurement.setRowColumn(statistical_cell_row, statistical_cell_column);
		}	
	
}
