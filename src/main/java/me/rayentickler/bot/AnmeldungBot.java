package me.rayentickler.bot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AnmeldungBot {
	private String startLink = "https://service.berlin.de/terminvereinbarung/termin/tag.php?termin=1&anliegen[]=120686&dienstleisterlist=122210,122217,122219,122227,122231,122243,122252,122260,122262,122254,122271,122273,122277,122280,122282,122284,122291,122285,122286,122296,150230,122301,122297,122294,122312,122314,122304,122311,122309,317869,324433,325341,324434,122281,324414,122283,122279,122276,122274,122267,122246,122251,122257,122208,122226&herkunft=http%3A%2F%2Fservice.berlin.de%2Fdienstleistung%2F120686%2F";
	private String nextPageLink = "https://service.berlin.de/terminvereinbarung/termin/day/1580511600/";
	private boolean finished = false;
	private boolean goToNextMonth = false;
	private WebDriver driver;
	private String dateRegex1;
	private String dateRegex2;
	private List<String> goodLocations;

	public AnmeldungBot() {
		System.out.println("Starting the chrome driver.");
		System.setProperty("webdriver.chrome.driver", "/home/raylam/Downloads/chromedriver");

		ChromeOptions options = new ChromeOptions();
		options.addArguments(
				"--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
		options.addArguments("--start-maximized");
		driver = new ChromeDriver(options);

		dateRegex1 = PropertyReader.getProperty("dateRegex1").trim();
		dateRegex2 = PropertyReader.getProperty("dateRegex2").trim();
		System.out.println("Date regex1: " + dateRegex1);
		System.out.println("Date regex2: " + dateRegex2);

		if ("Y".equals(PropertyReader.getProperty("goToNextMonth").trim())) {
			goToNextMonth = true;
		}

		String goodLocationString = PropertyReader.getProperty("goodLocation").trim();
		goodLocations = Arrays.asList(goodLocationString.split(","));
	}

	public static void main(String[] args) throws InterruptedException {
		AnmeldungBot bot = new AnmeldungBot();
		while (!bot.finished) {
			List<Termin> terminList = bot.lookForTermin();
			if (terminList.size() != 0) {
				// Check for the validity of the anmeldung date
				bot.finished = bot.checkTerminLocationGood(terminList);
			} else {
				System.out.println("Cannot find a valid termin.");
			}
			System.out.println("Sleep for 1 minute..");
			Thread.sleep(60000);
		}
	}

	public boolean checkTerminLocationGood(List<Termin> terminList) throws InterruptedException {

		/*
		 * An TimeoutException would be throw when the website gives up CAPTCHA. In this
		 * case just give up and alert the user.
		 * 
		 * The CAPTCHA appear quite often because of unknown reason, now there is no way
		 * to mitigate this. A courtesy of 10 seconds wait was added.
		 * 
		 * Changing VPN location might work.
		 * 
		 */
		try {
			for (Termin termin : terminList) {
				driver.get(termin.getHref());
				// Wait for the timetable's title to appear
				(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[5]/div[1]/h2")));

				// Look at the timetable for burgeramt location
				List<WebElement> burgerAmts = driver.findElements(By.className("timetable"));
				for (WebElement burgerAmt : burgerAmts) {
					System.out.println(burgerAmt.getText());
					String message = "<html><body>Found a slot! Date: " + termin.getDate() + ".<br/>";
					boolean foundGoodLocation = false;
					for (String goodLocation : goodLocations) {
						if (burgerAmt.getText().contains(goodLocation)) {
							message += goodLocation + "<br/>";
							foundGoodLocation = true;
						}
					}
					message += "</body></html>";

					if (foundGoodLocation) {
						// Ask user if it is good enough
						int input = JOptionPane.showConfirmDialog(null, message);
						if (input == 0) {
							return true;
						}
					}
				}
				Thread.sleep(3000);
			}
		} catch (TimeoutException ex) {
			System.out.println(ex);
			JOptionPane.showMessageDialog(null, "There is some problem with the Bot");
		}
		return false;
	}

	public List<Termin> lookForTermin() throws InterruptedException {

		System.out.println("===========================================================================");
		System.out.println("===========================================================================");
		System.out.println(LocalDateTime.now());
		System.out.println("Clicking the start link.");
		driver.get(startLink);

		System.out.println("Looking for the calenders..");
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[4]/div/div[2]/div[2]")));

		// Load next month
		if (goToNextMonth) {
			driver.get(nextPageLink);
			(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[4]/div")));
		}

		List<WebElement> calenders = driver.findElements(By.className("calendar-month-table"));
		System.out.println("Found the calender.");

		List<Termin> terminList = new ArrayList<>();
		// There are two calenders in the start page
		for (int i = 0; i < calenders.size(); i++) {
			WebElement ele = calenders.get(i);

			System.out.println("Looking for termin link in the calender..");
			List<WebElement> links = ele.findElements(By.tagName("a"));
			WebElement month;

			month = ele.findElement(By.className("month"));

			for (WebElement link : links) {
				String href = link.getAttribute("href");
				// Omit the link which leads to the next calender months
				if (!href.equals(nextPageLink)) {
					System.out.println(href);
					String date = checkTerminDateGood(link, month);
					if (!"BAD".equals(date)) {
						Termin termin = new Termin(date, href);
						terminList.add(termin);
					}

				}
			}
		}
		return terminList;
	}

	public String checkTerminDateGood(WebElement link, WebElement month) {
		String monthYear = month.getText();
		String date = link.getText();
		String completeDate = date + "-" + monthYear;
		completeDate = completeDate.replace(" ", "-");
		System.out.println(completeDate);

		if (completeDate.matches(dateRegex1) || completeDate.matches(dateRegex2)) {
			return completeDate;
		}

		return "BAD";
	}
}
