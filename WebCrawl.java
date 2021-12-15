/*
 * Programmer: Dan Hopp
 * Date: 27-FEB-2020
 * Description: Crawl through web pages via an URL array list(s). With each new 
    page vsited, put the new URLs into the to-be-traversed list. Then, if the 
    line isn't a part of a block of script or comments, call a function to 
    format the line from unwanted characters. Next, insert the parsed data into 
    the Word Object array list.

    Go to the next webpage page in the list and repeat until TimeElapsed has 
    reached its limit. If a Malformed URL or IO error is thrown, continue on.

   Majority of code for the crawler and scanWebpage was borrowed from an example 
   in Introduction to Java, Y. Liang, 10th ed. Altered to scan a specific web 
   protocol, ignore unwanted URLs, check for a specified running time,
   and pass the current line to a line scrubber.

   Scanning lines from an open stream also borrowed from Introduction to Java.
 */
package lab3;

import java.util.ArrayList;
import java.util.Scanner;


class WebCrawl {

    //HTML to avoid
    final static String INVALID_HTML_REGEX = ".*(\\\\/|ogp\\.me"
            + "|\\.cgi|\\.dtd|\\.xml|\\.pdf|\\.docx|\\&|\\.doc|\\.dot|\\.wbk"
            + "|\\.docm|\\.dotx|\\.docb"
            + "|\\.xls|\\.xlsx|\\.xlt|\\.xlm|\\.xlsm|\\.xltx|\\.xltm|\\.xltb|\\.xla"
            + "|\\.xlam|\\.xll|\\.xlw"
            + "|\\.ppt|\\.pot|\\.pps|\\.pptx|\\.pptm|\\.potx|\\.potm|\\.ppam"
            + "|\\.ppsx|\\.ppsm|\\.sldx|\\.sldm"
            + "|\\.adn|\\.accdb|\\.accdr|\\.accdt|\\.accda|\\.mdw|\\.accde|\\.mam"
            + "|\\.maq|\\.mar|\\.mat|\\.maf|\\.laccdb|\\.ade|\\.adp|\\.mdb|\\.cdb"
            + "|\\.mda|\\.mdn|\\.mdt|\\.mdf|\\.mde|\\.ldb"
            + "|\\.pub|\\.xps"
            + "|\\.gif|\\.jpg|\\.png|\\.apng|\\.ico|\\.cur|\\.jpeg|\\.jfif"
            + "|\\.pjpeg|\\.pjp|\\.svg|\\.tif|\\.tiff|\\.webp|\\.woff"
            + "|\\.mpg|\\.mpeg|\\.avi|\\.wmv|\\.mov|\\.rm|\\.ram|\\.swf|\\.flv"
            + "|\\.ogg|\\.webm|\\.mp4|\\.m3u8"
            + "|\\.mid|\\.midi|\\.wma|\\.aac|\\.wav|\\.mp3"
            + "|moved permanently|the document has moved"
            + "|fbml|img|css|php|xhtml).*";
    
 /*Start the web crawl*/  
    static void crawler(ArrayList<WordObject> wordObjectList,
            String startingURL, TimeTracker tt) 
     {

        //URL ArrayLists
        ArrayList<String> listOfPendingURLs = new ArrayList<>();
        ArrayList<String> listOfTraversedURLs = new ArrayList<>();

        //Add new URL
        listOfPendingURLs.add(startingURL);
        /*Keep going until the pending URL list is empty or the total time to 
        run is reached*/
        while (!listOfPendingURLs.isEmpty()
                && tt.isStoptimeReached(false) == false) {
            //Pull out the first item in the list
            String urlString = listOfPendingURLs.remove(0);
            //If the traversed list doesn't already contain the pending URL
            if (!listOfTraversedURLs.contains(urlString)) {

                System.out.println("Scanning webpage for URL: " + urlString);

                //Add the URL to the traversed list  
                listOfTraversedURLs.add(urlString);
                
                //Scan the webpage
                for (String s : scanWebpage(urlString, wordObjectList, tt)) {
                    /*If the URL isn't in the Traversed list, add the new URLs
                    to the Pending list*/
                    if (!listOfTraversedURLs.contains(s)) {
                        listOfPendingURLs.add(s);
                    }
                }
            }
        }
    }
  
/*Get the sub URLs from a webpage. Afterwards, send the current line to the 
string scrubbing methods. Split the seperated words into an array, and 
create or add to a Word Object for each item in the array.*/
    static ArrayList<String> scanWebpage (String urlString, 
            ArrayList<WordObject> wordObjectList, TimeTracker tt) {

        boolean scriptBlockFound = false;
        
        //List for URLs found on the page
        ArrayList<String> list = new ArrayList<>();

        try {
            //Open page for stream
            java.net.URL url = new java.net.URL(urlString);
            Scanner urlPageInput = new Scanner(url.openStream());
            
            int current = 0;
            //Keep going until the last line is reached or the time is up
            while (urlPageInput.hasNext() && 
                    tt.isStoptimeReached(true) == false) {
                String line = urlPageInput.nextLine();

                //Get index of where http:// is at, starting at [0]
                current = line.indexOf("http://", current);
                //As long as it's not the first item on the line:
                while (current > 0) {
                    //Get the index of the end of the URL
                    int endIndex = line.indexOf("\"", current);
                    //Is a URL found?
                    if (endIndex > 0) { 
                        
                        String currentURL = line.substring(current, endIndex);
                        
                        //Ignore any invalid URLs
                        if (isHtmlValid(currentURL)) {
                            list.add(currentURL);
                        }
                        /*Get the next URL in line, starting from the end of the
                        previous URL*/
                        current = line.indexOf("http:", endIndex);
                    } else {
                        current = -1;
                    }
                }
                
                //Ignore blocks of html script or comments
                if (line.contains("<script") || line.contains("/*")){
                    scriptBlockFound = true;
                }
                /*If it's the end of the script block, or if it's on the same 
                line as the begin tag*/
                if(line.contains("</script") || line.contains("*/")){
                    scriptBlockFound = false;
                }    
                
                if (!scriptBlockFound){
                
                    //Call function to scrub line
                    line = FormatLine.scrubString(line);

                    /*If the formatted line isn't all blank spaces, split it into 
                    an array of strings, and then pass the array
                    into the function to create or add to an object*/
                    if (line.length() > 0) {
                        String[] wordArray = line.split("[ ]");           
                        createObject(wordObjectList, wordArray);

                    }
                }
            }
        }
        catch (java.net.MalformedURLException ex) {
            //Move along....  Move along....
        }
        catch (java.io.IOException ex) {
            //Move along....  Move along.... 
        }
        return list;
    }

/*Ignore whatever this is: http:\/\/www. Or URLs with php, xhtml, etc in 
the name*/
    static boolean isHtmlValid(String currentURL){
        boolean isValid = true;

        if (currentURL.toLowerCase().matches(INVALID_HTML_REGEX)
                //Ignore URLs that are just "http://", or less
                || currentURL.length() < 8){ 
            isValid = false;
        }
        
        return isValid;
    }
    
/*Pass an array of words into the method and see if the object for that 
world already exists. If it does, add 1 to its count. If not, create the 
object.*/
    static void createObject(ArrayList<WordObject> wordObjectList, 
            String[] wordArray){

        //var for WordObject navigation
        int arrayListIndex = 0;
        
        //Cycle through the list of words
        for (int i = 0; i < wordArray.length; i ++){
            boolean isWordInArrayList = false;
            //If the word is greater than 1 character, continue
            if ((wordArray[i].length()) > 1){

                /*Cycle through the current list of objects to see if the word 
                already exists*/
                for (int j = 0; j < wordObjectList.size(); j++){
                    if (wordArray[i].equals(wordObjectList.get(j).getWord())) {
                        isWordInArrayList = true;
                        arrayListIndex = j;
                    }
                }
                
                //If the word was found in the ArrayList, up the count by 1
                if(isWordInArrayList){
                    wordObjectList.get(arrayListIndex).addToWordCount();
                }
                //Else add the word as a new object in the ArrayList
                else {
                    wordObjectList.add(new WordObject(wordArray[i]));
                }
            }
        } 
    }
}
