package me.rayentickler.bot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
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
	private WebDriver driver;
	private String dateRegex1;
	private String dateRegex2;
	private List<String> goodLocations;
	
	
	public static void main(String[] args) throws InterruptedException {
		AnmeldungBot bot = new AnmeldungBot();
		while ( !bot.finished ) {
			List<String> targetLinks = bot.lookForTermin();
			if ( targetLinks.size() != 0 ) {
				//Check for the validity of the anmeldung date
				bot.finished = bot.checkTerminDesirable(targetLinks);
			}else {
				System.out.println("Cannot find a valid termin.");
			}
			System.out.println("Sleep for 1 minute..");
			Thread.sleep(60000);
		}
	}

	public AnmeldungBot() {
		System.out.println("Starting the chrome driver.");
		System.setProperty("webdriver.chrome.driver", "/home/raylam/Downloads/chromedriver");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		driver = new ChromeDriver(options);
		
		dateRegex1 = PropertyReader.getProperty("dateRegex1").trim();
		dateRegex2 = PropertyReader.getProperty("dateRegex2").trim();
		System.out.println("Date regex1: " + dateRegex1);
		System.out.println("Date regex2: " + dateRegex2);
		
		String goodLocationString = PropertyReader.getProperty("goodLocation").trim();
		goodLocations = Arrays.asList( goodLocationString.split(",") );
	}
	
	public boolean checkTerminDesirable(List<String> targetLinks){

		
		for ( String link : targetLinks ) {
			driver.get(link);
			(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[3]/div/div/div[3]")));
			
			System.out.println("Looking for the date String");
			//Look at the Date column
			List<WebElement> dates = driver.findElements(By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[3]/div/div/div[3]"));
			for ( WebElement date: dates ) {
				System.out.println(date.getText());
				if ( date.getText().matches( dateRegex1 ) || date.getText().matches( dateRegex2 ) ) {
				//if ( date.getText().matches("^.+[0][0-9]\\. März 2020$") ) {
					
					//Look at the timetable
					List<WebElement> burgerAmts = driver.findElements(By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[5]/div[2]"));
					for ( WebElement burgerAmt : burgerAmts ) {
						System.out.println(burgerAmt.getText());
						String message = "<html><body>Found a slot! Date: " + date.getText() + ".<br/>";
						boolean foundGoodLocation = false;
						for ( String goodLocation: goodLocations ) {
							if ( burgerAmt.getText().contains(goodLocation) || burgerAmt.getText().contains(goodLocation.toLowerCase()) ) {
								message += goodLocation + "<br/>";
								foundGoodLocation = true;
							}
						}
						message += "</body></html>";
						if ( foundGoodLocation ) {
							int input = JOptionPane.showConfirmDialog(null, message);
							if ( input == 0 ) {
								return true;
							}
						}
					}
				}
			}
		}	
		return false;
	}
	
	public List<String> lookForTermin() throws InterruptedException {
		
		System.out.println("===========================================================================");
		System.out.println("===========================================================================");
		System.out.println(LocalDateTime.now());
		System.out.println("Clicking the start link.");
		driver.get(startLink);
		
		System.out.println("Looking for the calenders..");
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"top\"]/div/div/div/div[4]/div[2]/div/div/div[4]/div/div[2]/div[2]")));
		
		//Load next month
		//driver.get(nextPageLink);
		//(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.className("calendar-month-table")));
		
		List<WebElement> calenders = driver.findElements(By.className("calendar-month-table"));
		System.out.println("Found the calender.");
		
		List<String> targetLinks = new ArrayList<>();
		//There are two calenders in the start page
		for ( WebElement ele : calenders ) {
			System.out.println("Looking for termin link in the calender..");
			List<WebElement> links = ele.findElements(By.tagName("a"));
			for ( WebElement link: links ) {				
				String href = link.getAttribute("href");
				//Omit the link which leads to the next calender months
				if ( !href.equals(nextPageLink) ) {
					System.out.println(href);
					targetLinks.add(href);
				}
			}
		}
		return targetLinks;
	}
}
