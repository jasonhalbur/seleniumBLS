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
        FileWriter myWriter = null;
		WebDriver myDriver = new FirefoxDriver();
		String myFile = "output.xml";
		ArrayList<String> uniqueURLList = null;
		ArrayList<String> myURLArticles = null;
		
        //open file
        try {
        	myWriter = new FileWriter(myFile);
        } catch (IOException e) {
        	e.printStackTrace();
        }
		
		//Get the links from the main index page.
		uniqueURLList = blsScrape.getLinks("http://www.bls.gov/ooh/a-z-index.htm","/ooh/", myDriver);

		writeHeader(myFile, myWriter);
		//For each Unique URL get the Articles.
        for (String myURL : uniqueURLList){
        	//We don't want to crash their site and scraping HTML is hard work so lets take a nap.
        	try {
        		// thread to sleep for 4 seconds.
        		Thread.sleep(4000);
        	} catch (Exception e) {
        		System.out.println(e);
        	}
        	myURLArticles = getArticles(myURL, myDriver);
        	writeData(myURLArticles, myURL, myFile, myWriter);
        }
        writeFooter(myFile, myWriter);
        try {
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		myDriver.close();
	}//End of Main
	
	public static void writeData(ArrayList<String> myWriteDataArticles, String myWriteDataURL, String myWriteDataFileName, FileWriter myWriteDataWriter){
		// Write the data to file

        String myWriteDataJobTitle = null;

    	//Get Job title from myURL
    	String titlePattern = "([^\\/]+)[.]htm$";
    	Pattern linkRegex = Pattern.compile(titlePattern);
    	Matcher titleMatch = linkRegex.matcher(myWriteDataURL);
    	//The name of the page to download, in the URL, is going to be the Job Title in this instance.
    	if(titleMatch.find()){
    		//I've found the title
    		myWriteDataJobTitle = titleMatch.group(1);
    	}
    	try {
    		myWriteDataWriter.write("<jobTitle>" + myWriteDataJobTitle + "</jobTitle>\n");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
       	try {
       		myWriteDataWriter.write("<url><![CDATA[" + myWriteDataURL + "]]></url>\n");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
        for (String myArticle : myWriteDataArticles){
	        try {
	        	myWriteDataWriter.write("<article><![CDATA[\n");
	        	myWriteDataWriter.write(myArticle);
	        	myWriteDataWriter.write("\n]]></article>\n");
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
        }
		
	}
	
	public static void writeHeader(String myWriteHeaderFileName, FileWriter myWriteHeaderWriter){
        //write XML header
        try {
        	myWriteHeaderWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        	myWriteHeaderWriter.write("<jobs>\n");    
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public static void writeFooter(String myWriteFooterFileName, FileWriter myWriteFooterWriter){
        //Write the file XML footer
        try {
        	myWriteFooterWriter.write("</jobs>\n");
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public static ArrayList<String> getArticles(String myGetArticlesURL,WebDriver myGetArticlesDriver){

		ArrayList<String> myReturnArrayList = new ArrayList<String>();
    	//Get the HTML of the page we want
    	myGetArticlesDriver.get(myGetArticlesURL);
    	
    	//In this example we want the Articles HTML segments from the page.
    	List<WebElement> myArticles = myGetArticlesDriver.findElements(By.tagName("article"));
    	for (WebElement article : myArticles){

    		//Get the HTML that was between the Article tag.
            String data = (String)((JavascriptExecutor)myGetArticlesDriver).executeScript("return arguments[0].innerHTML;", article);
            myReturnArrayList.add(data);

    	}
		return myReturnArrayList;
	}
	
	public static ArrayList<String> getLinks(String myPage, String myLinkFilter, WebDriver myGetLinksDriver){

		ArrayList<String> myGetLinksURLList = new ArrayList<String>();
		ArrayList<String> myGetLinksUniqueURLList = new ArrayList<String>();

        // And now use this to visit Index site
        myGetLinksDriver.get(myPage);

        // Find the links  by a HTML element
        List<WebElement> myLinks = myGetLinksDriver.findElements(By.tagName("a"));
        //A little Progress indicator.
        System.out.println("Number of links: " + myLinks.size());
        
        //Traverse Links grabbing the URL and making sure it is Unique
        for (WebElement myElement : myLinks){
        	String myLink = myElement.getAttribute("href");;
        	//Have I found a link to a Occupational Outlook Handbook career?
	        if(myLink.contains(myLinkFilter)){
		        myGetLinksURLList.add(myLink);
            	//A little Progress indicator.
		        System.out.println("I found a Filter link: " + myLink);
	        } else {
	        	//This is a Link on the page but not to a OOH Career so I'm skipping it.
		        continue;
	        }
        }
        
        //Unfortunately the page has multiple references to the same URL
        //HashSet doesn't allow Duplicate values.
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(myGetLinksURLList);
        myGetLinksUniqueURLList.addAll(hs);
		return myGetLinksUniqueURLList;
	}
}