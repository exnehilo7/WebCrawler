/*
 * Programmer: Dan Hopp
 * Date: 27-FEB-2020
 * Description: Main method to start the web crawler program.
    From the user, it will get a starting URL and the desired time for the 
    to program run. The default maximum time the user can webcrawl is 5 min,
    but that can be changed via the Final variable.

    The web crawl can start with a http: or https: address, but it will only 
    scan pages with a http:// protocol (most of the https sites pull no data or 
    are specifically for multimedia content), and if the page allows text data 
    to be read.

    During the crawl the user will be notified of the webpage that is currently
    being scanned, as well as a notification of the current run time in a 
    30-second(default) interval.

    When the time limit is reached, the crawl will stop and a txt file with the
    top 50 words will be exported into the same directory as the program.

    If there's an Illegal Argument Exception or an unforssen Exception, the
    program will exit.
    
    Time tracking help from:
        https://www.geeksforgeeks.org/measure-time-taken-function-java/

 */
package lab3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;


public class FileExport {
    public static void main(String[] args) {
        
        /*A millisecond is one thousandth of a second. Default limit is 5 min*/
        final long MAX_TIME_TO_RUN_IN_MS = 300000;
        Scanner input = new Scanner(System.in);
        File file = new File("web_crawler_results.txt");
        
        //Check if the folder is writeable
        if (!file.canWrite()) {
            System.out.println("Alert! The web crawler results cannot be "
                    + "written to the default folder the program is in. "
                    + "Please check if there are appropriate user " 
                    + "rights for the folder.");
            System.exit(0);
        }
        
        //Creating ArrayList for word objects
        ArrayList<WordObject> wordObjectList = new ArrayList<>();
        
        try (java.io.PrintWriter output = new java.io.PrintWriter(file);){             

            //Get starting URL and time to elapse from the user
            System.out.print("Enter a URL: ");   
            String urlString = input.next();

            //With the max allowable amount, prompt user for time to run
            int runTime = 0;
            //Repeat until a valid entry is entered
            do {
                System.out.print("Enter the time as an integer in seconds "
                        + "that you would like the crawler to run "
                        + "(max " + MAX_TIME_TO_RUN_IN_MS / 1000 + "): ");
                runTime = input.nextInt();

                //Inform user if time-to-run is invalid
                if (runTime > MAX_TIME_TO_RUN_IN_MS) {
                    System.out.println("The time cannot exceed " 
                    + (MAX_TIME_TO_RUN_IN_MS / 1000) + " seconds." );
                }
            } while (runTime > MAX_TIME_TO_RUN_IN_MS);

            //Create the Time Tracker object
            TimeTracker tt = new TimeTracker(runTime);

            //Webcrawl and parse data into the word objects
            System.out.println("Initiating webcrawl...");
            WebCrawl.crawler(wordObjectList, urlString, tt);

            //Sort the Word Object list for export
            Collections.sort(wordObjectList);

            //Print up to the first 50 objects to the file
            System.out.println("Exporting File...");
            for (int i = 0; i < wordObjectList.size(); i++){
                if (i == 50){
                    break;
                }            
                output.println(wordObjectList.get(i));
            }
        }
        /*If it's an invaid URL the user will be able to see via the Scanning
        Webpage message from WebCrawl.crawler*/
        catch (IllegalArgumentException ex) {
            System.out.println("An Illegal Argument Exception was thrown."
                    + " Exiting Program.");
        }
        catch (Exception ex) {
            System.out.println("An exception was thrown! Please contact your"
                + " local dev with the below information:");
            ex.printStackTrace();
        }
    }
}
