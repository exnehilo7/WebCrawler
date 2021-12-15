/*
 * Programmer: Dan Hopp
 * Date: 27-FEB-2020
 * Description: An object with functions to:

    See if the target end time has been reached.
    Calcualte 30 second intervals from the user's requested run time. (To alter 
    the interval, change the final variable.)
    Give feedback to the user for when an interval has passed
    (30, 60, 90, etc...).
 */
package lab3;

import java.util.ArrayList;

public class TimeTracker {
    
    private final int TIME_INTERVAL = 30;  //in seconds
    private final long SEC_TO_MS_CONVERSION = 1000;
    private boolean stopCrawl;
    private ArrayList<Integer> messageIntervals;
    private int arrayIndex;
    private long startTime;
    private long stopTime;
    private int runTime;
    
//Don't use the default constructor for this program
    public TimeTracker(){
        
    }
    
/*TimeTracker constructor*/  
    public TimeTracker(int runTime){
        this.runTime = runTime;
        
        //Calculate start and stop times
        startTime = System.currentTimeMillis();
        this.stopTime = startTime + (long)(this.runTime * SEC_TO_MS_CONVERSION);
        
        //Populate message interval array
        messageIntervals = new ArrayList<>();
        createTimeIntervals();
        arrayIndex = 0;
        
        //Set the webcrawl's toggle
        stopCrawl = false;
    }
    
/*Function to see if the stop time has been reached, and to give the user
 feedback as the time elapses. Boolean pass-by-value is an option to call the 
 feedback message function, specifically to prevent spam for when the function 
 is called from within a rapid loop*/    
    public boolean isStoptimeReached(boolean callMessageFunction){
        
        //Current time is < the calculated end time from the user's entry ((secondsTOmilliseconds) + current time)
        if (System.currentTimeMillis() > stopTime){
            stopCrawl = true;
        }
        
        if (callMessageFunction == true) {
            timeIntervalMessage();
        }
            
        return stopCrawl;
    }
    
/*If a time interval has been reached, inform the user*/
    private void timeIntervalMessage(){
        
        if (((System.currentTimeMillis() - startTime) / SEC_TO_MS_CONVERSION)  
                > messageIntervals.get(arrayIndex)){
            System.out.println("Crawl time at " + messageIntervals.get(arrayIndex) 
                       + " seconds...");
            //Move to the next interval
            arrayIndex = arrayIndex + 1;
        }
        
    }
    
/*Create an array list with the interval's elements, in 30 second blocks*/
    private void createTimeIntervals(){

        int temp = 0;
        int numberOfIntervals = runTime / TIME_INTERVAL;
        
        //If the user's input is equal to or greater than the TIME_INTERVAL 
        if (numberOfIntervals > 0) {
            //Populate array with values
            for (int i = 0; i < numberOfIntervals; i++) { 
                temp = temp + TIME_INTERVAL;
                messageIntervals.add(temp);
            }
        }
        //Else just add one item
        else {
            messageIntervals.add(TIME_INTERVAL);
        }
    }
}
