package ampullen.model;

import java.text.SimpleDateFormat;

import ampullen.jsondb.Observable;

public class Tournament extends Observable{

	String name;
	long date;
	String location;
	String format;
	String division;
	//String venue;
	String teamFee;
	String playersFee;
	long registrationDeadline;
	long paymentDeadline;
	String schedule;
	String ucLink;
	String comment;
	
	public Tournament(){}
	
	public Tournament(String name, long date, String location, String format, String division, String teamFee,
			String playersFee, long registrationDeadline, long paymentDeadline) {
		super();
		this.name = name;
		this.date = date;
		this.location = location;
		this.format = format;
		//this.venue = venue;
		this.teamFee = teamFee;
		this.division = division;
		this.playersFee = playersFee;
		this.registrationDeadline = registrationDeadline;
		this.paymentDeadline = paymentDeadline;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		notifyObservers();
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
		notifyObservers();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
		notifyObservers();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
		notifyObservers();
	}

	/*public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
		notifyObservers();
	}*/

	public String getTeamFee() {
		return teamFee;
	}

	public void setTeamFee(String teamFee) {
		this.teamFee = teamFee;
		notifyObservers();
	}

	public String getPlayersFee() {
		return playersFee;
	}

	public void setPlayersFee(String playersFee) {
		this.playersFee = playersFee;
		notifyObservers();
	}

	public long getRegistrationDeadline() {
		return registrationDeadline;
	}

	public void setRegistrationDeadline(long registrationDeadline) {
		this.registrationDeadline = registrationDeadline;
		notifyObservers();
	}

	public long getPaymentDeadline() {
		return paymentDeadline;
	}

	public void setPaymentDeadline(long paymentDeadline) {
		this.paymentDeadline = paymentDeadline;
		notifyObservers();
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
		notifyObservers();
	}

	public String getUcLink() {
		return ucLink;
	}

	public void setUcLink(String ucLink) {
		this.ucLink = ucLink;
		notifyObservers();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		notifyObservers();
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	@Override
	public String toString() {
		return "Tournament [name=" + name + ", date=" + date + ", location=" + location + ", format=" + format
				+ ", division=" + division + ", teamFee=" + teamFee + ", playersFee=" + playersFee
				+ ", registrationDeadline=" + registrationDeadline + ", paymentDeadline=" + paymentDeadline
				+ ", schedule=" + schedule + ", ucLink=" + ucLink + ", comment=" + comment + "]";
	}
	
	public String getInfoMarkup(){
		
		SimpleDateFormat dateformat = new SimpleDateFormat("DD.MM.yyyy");
		
		String s = String.format(
				"\n**%s**\n"
				+ "**Datum**: " + dateformat.format(date)
				+ "\n**Ort**: %s\n"
				+ "**Format**: %s %s\n"
				+ "**Teamfee**: %s\n"
				+ "**Playersfee**: %s\n"
				+ "**Deadline Anmeldung**: " + dateformat.format(registrationDeadline) + "\n" //%8$td.%8$tm.%8$tY
				+ "**Deadline Zahlung**: " + dateformat.format(paymentDeadline) + "\n\n"
				, name, location, format, division, teamFee, playersFee);
		
		s += (schedule != null ? String.format("**Schedule:** %s\n\n", schedule) : "");
		s += (ucLink != null ? String.format("**Ultimate Central:** %s\n\n", ucLink) : "");
		s += (ucLink != null ? String.format("**Kommentar:** %s", comment) : "");
		
		return s;
		
	}
	
}
