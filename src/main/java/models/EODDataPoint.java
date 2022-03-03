package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EODDataPoint {

	private LocalDate date;
	private double cashAmount;
	private double eftposAmount;
	private double amexAmount;
	private double googleSquareAmount;
	private double chequeAmount;
	private int medschecks;
	private int stockOnHandAmount;
	private int scriptsOnFile;
	private int smsPatients;
	private double tillBalance;
	private double runningTillBalance;
	private String notes;

	public EODDataPoint(ResultSet resultSet) {
	}

	public EODDataPoint(LocalDate previousDay) {
		date = previousDay;
	}


	public LocalDate getDate() {return date;}
	public String getDateString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return formatter.format(date);
	}
	public void setDate(LocalDate date) {this.date = date;}
	public double getCashAmount() {return cashAmount;}
	public String getCashAmountString(){return NumberFormat.getCurrencyInstance().format(cashAmount);}
	public void setCashAmount(double cashAmount) {this.cashAmount = cashAmount;}
	public double getEftposAmount() {return eftposAmount;}
	public String getEftposAmountString(){return NumberFormat.getCurrencyInstance().format(eftposAmount);}
	public void setEftposAmount(double eftposAmount) {this.eftposAmount = eftposAmount;}
	public double getAmexAmount() {return amexAmount;}
	public String getAmexAmountString(){return NumberFormat.getCurrencyInstance().format(amexAmount);}
	public void setAmexAmount(double amexAmount) {this.amexAmount = amexAmount;}
	public double getGoogleSquareAmount() {return googleSquareAmount;}
	public String getGoogleSquareAmountString(){return NumberFormat.getCurrencyInstance().format(googleSquareAmount);}
	public void setGoogleSquareAmount(double googleSquareAmount) {this.googleSquareAmount = googleSquareAmount;}
	public double getChequeAmount() {return chequeAmount;}
	public String getChequeAmountString(){return NumberFormat.getCurrencyInstance().format(chequeAmount);}
	public void setChequeAmount(double chequeAmount) {this.chequeAmount = chequeAmount;}
	public int getMedschecks() {return medschecks;}
	public void setMedschecks(int medschecks) {this.medschecks = medschecks;}
	public int getStockOnHandAmount() {return stockOnHandAmount;}
	public void setStockOnHandAmount(int stockOnHandAmount) {this.stockOnHandAmount = stockOnHandAmount;}
	public int getScriptsOnFile() {return scriptsOnFile;}
	public void setScriptsOnFile(int scriptsOnFile) {this.scriptsOnFile = scriptsOnFile;}
	public int getSmsPatients() {return smsPatients;}
	public void setSmsPatients(int smsPatients) {this.smsPatients = smsPatients;}
	public double getTillBalance() {return tillBalance;}
	public String getTillBalanceString(){return NumberFormat.getCurrencyInstance().format(tillBalance);}
	public void setTillBalance(double tillBalance) {this.tillBalance = tillBalance;}
	public double getRunningTillBalance() {return runningTillBalance;}
	public String getRunningTillBalanceString(){return NumberFormat.getCurrencyInstance().format(runningTillBalance);}
	public void setRunningTillBalance(double runningTillBalance) {this.runningTillBalance = runningTillBalance;}
	public String getNotes() {return notes;}
	public void setNotes(String notes) {this.notes = notes;}

}
