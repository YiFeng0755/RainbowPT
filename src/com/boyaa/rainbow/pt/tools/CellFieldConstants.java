package com.boyaa.rainbow.pt.tools;

public enum CellFieldConstants{
	Time, TopActivity, AppMem, AppMemRatio, FreeMemory, AppCPU, Traffic, SendTraffic, ReceiveTraffic, Fps,Battery,
	Measurement;
	int rowNumber;
	int columnNumber;
	
	public void setRowColumn(int rn, int cn){  
		this.rowNumber = rn;
		this.columnNumber = cn;
	}  
	public int getRow() {  
		return this.rowNumber; 
	}  
	public int getColumn() {  
		return this.columnNumber; 
	}
}