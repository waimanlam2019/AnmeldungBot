package me.rayentickler.bot;

public class Termin {
	private String date;
	private String href;

	public Termin(String date, String href) {
		super();
		this.date = date;
		this.href = href;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
}
