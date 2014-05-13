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

	public static ArrayList<String> getLinks(String myPage, String myLinkFilter){
		Boolean myGetLinksDebug = false;
		WebDriver myGetLinksDriver = new FirefoxDriver();
		ArrayList<String> myGetLinksURLList = new ArrayList<String>();
		ArrayList<String> myGetLinksUniqueURLList = new ArrayList<String>();

        // And now use this to visit Index site
        myGetLinksDriver.get(myPage);

        // Find the links  by a HTML element
        List<WebElement> myLinks = myGetLinksDriver.findElements(By.tagName("a"));

        if(myGetLinksDebug){
	        //A little Progress indicator.
	        System.out.println("Number of links: " + myLinks.size());
        }
        
        //Traverse Links grabbing the URL and making sure it is Unique
        for (WebElement myElement : myLinks){
        	String myLink = myElement.getAttribute("href");;
        	
            if(myGetLinksDebug){
	            //A little Progress indicator.
	            System.out.println("Processing Link: " + myLink);
            }

            //Have I found a link to a Occupational Outlook Handbook career?
	        if(myLink.contains(myLinkFilter)){

	        	//I Have found one now add back the domain.
		        myGetLinksURLList.add(myLink);
		        
	            if(myGetLinksDebug){
	            	//A little Progress indicator.
	            	System.out.println("I found a Filter link: " + myLink);
	            }
	        } else {
	        	//This is a Link on the page but not to a OOH Career so I'm skipping it.
		        continue;
	        }

        }
        myGetLinksDriver.close();
        
        //Unfortunately the page has multiple references to the same URL
        //HashSet doesn't allow Duplicate values.
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(myGetLinksURLList);
        myGetLinksUniqueURLList.addAll(hs);
		return myGetLinksUniqueURLList;
		
	}
	
	
	public static void main(String[] args) {
		ArrayList<String> uniqueURLList = blsScrape.getLinks("http://www.bls.gov/ooh/a-z-index.htm","/ooh/");

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

    	WebDriver myMainDriver = new FirefoxDriver();
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
        	myMainDriver.get(myURL);
        	
        	//In this example we want the Articles HTML segments from the page.
        	List<WebElement> myArticles = myMainDriver.findElements(By.tagName("article"));
        	for (WebElement article : myArticles){

        		//Initial request was for just the text.
        		//String data = article.getText();

        		//Recent request was for the HTML that was between the Article tag.
	            String data = (String)((JavascriptExecutor)myMainDriver).executeScript("return arguments[0].innerHTML;", article);

	            try {
	            	writer.write("<article><![CDATA[\n");
	            	writer.write(data);
	            	writer.write("\n]]></article>\n");
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
        	} // End of each article Loop
        }//End of each uniqueURLList Loop
    	myMainDriver.close();
        //Write the file XML footer
        try {
        	writer.write("</jobs>\n");
        	writer.close();         
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}//End of Main

}