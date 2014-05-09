import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class blsScrape {

	public static void main(String[] args) {
		Boolean myDebug = false;
		
		WebDriver driver = new FirefoxDriver();
		ArrayList<String> myURLList = new ArrayList<String>();
		ArrayList<String> uniqueURLList = new ArrayList<String>();

        // And now use this to visit Index site
        driver.get("http://www.bls.gov/ooh/a-z-index.htm");

        // Find the links  by a HTML element
        List<WebElement> myLinks = driver.findElements(By.tagName("a"));

        if(myDebug){
	        //A little Progress indicator.
	        System.out.println("Number of links: " + myLinks.size());
        }
        
        //Traverse Links grabbing the URL and making sure it is Unique
        for (WebElement myElement : myLinks){
        	String myLink = myElement.getAttribute("href");;
        	
            if(myDebug){
	            //A little Progress indicator.
	            System.out.println("Processing Link: " + myLink);
            }
            
	        //Pattern grabs just the Occupational Outlook Handbook links
	        String linkPattern = "(/ooh/[^\"]+)";
	        Pattern linkRegex = Pattern.compile(linkPattern);
	        Matcher linkMatch = linkRegex.matcher(myLink);

	        //Have I found a link to a Occupational Outlook Handbook career?
	        if(linkMatch.find()){
		        //I Have found one now add back the domain.
		        myLink = "http://www.bls.gov" + linkMatch.group(1);
		        myURLList.add(myLink);
		        
	            if(myDebug){
	            	//A little Progress indicator.
	            	System.out.println("I found an OOH link: " + myLink);
	            }
	        } else {
	        	//This is a Link on the page but not to a OOH Career so I'm skipping it.
		        continue;
	        }

        }

        //Unfortunately the page has multiple references to the same URL
        //HashSet doesn't allow Duplicate values.
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(myURLList);
        uniqueURLList.addAll(hs);

        // Write the data to file
        FileWriter writer = null;
        //open file
        try {
        	writer = new FileWriter("output.xml");
        } catch (IOException e) {
        	e.printStackTrace();
        }

        //write XML header
        try {
        	writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        	writer.write("<jobs>\n");    
        } catch (IOException e) {
        	e.printStackTrace();
        }

        //A little Progress indicator
        System.out.println("Number of unique links to process:" + uniqueURLList.size());
        
        //Go get the data from the individual pages
        for (String myURL : uniqueURLList){
        	String jobTitle = null;
        	
        	//We don't want to crash their site and scraping HTML is hard work so lets take a nap.
        	try {
        		// thread to sleep for 4 seconds.
        		Thread.sleep(4000);
        	} catch (Exception e) {
        		System.out.println(e);
        	}
        	
        	//A little progress indication and write the URL to the file
        	System.out.println("Link to get: " + myURL);
        	try {
        		writer.write("<url><![CDATA[" + myURL + "]]></url>\n");
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	
        	//Get Job title from myURL
        	String titlePattern = "([^\\/]+)[.]htm$";
        	Pattern linkRegex = Pattern.compile(titlePattern);
        	Matcher titleMatch = linkRegex.matcher(myURL);
        	//The name of the page to download, in the URL, is going to be the Job Title in this instance.
        	if(titleMatch.find()){
        		//I've found the title
        		jobTitle = titleMatch.group(1);
        	}
        	
        	//A little progress indication and write the job title to the file
        	System.out.println("Job Title: " + jobTitle);
        	try {
        		writer.write("<jobTitle>" + jobTitle + "</jobTitle>\n");
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	
        	//Get the HTML of the page we want
        	driver.get(myURL);
        	
        	//In this example we want the Articles HTML segments from the page.
        	List<WebElement> myArticles = driver.findElements(By.tagName("article"));
        	for (WebElement article : myArticles){

        		//Initial request was for just the text.
        		//String data = article.getText();

        		//Recent request was for the HTML that was between the Article tag.
	            String data = (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", article);

	            try {
	            	writer.write("<article><![CDATA[\n");
	            	writer.write(data);
	            	writer.write("\n]]></article>\n");
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
        	} // End of each article Loop
        }//End of each uniqueURLList Loop

        //Quit the WebDriver
        driver.quit();
        
        //Write the file XML footer
        try {
        	writer.write("</jobs>\n");
        	writer.close();         
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}//End of Main

}