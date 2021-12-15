/*
 * Programmer: Dan Hopp
 * Date: 27-FEB-2020
 * Description: Via the WebCrawl, parse the line by:

    Trimming the spaces.
    Remove the HTML tags. If it's an "only-HTML" line (Scripts and codeing),
    then remove all text.
    Remove any commas, numbers, or special characters.
    Remove any common or unwanted words.
   
   Split the formatted string by the spaces and add each word to the Word Object
   array. If the word already exists, up the word's count by 1.

   (To make the split string less of a mess, I thought about making a function 
    to remove extra spaces in-between the words. But, one line to check the 
    content's length is smaller than say 12 or so lines of code)
 */
package lab3;

class FormatLine {
    
    //List of common words to remove
    public static final String COMMON_WORDS_REGEX = "( a | the | in | on | for"
            + " | with | and | or | you | i | your | my | if | as | are | can"
            + " | of | at | is | to | be | it | its | they | his | her | has"
            + " | was | by | that | this | we | not | us | our | ours | from"
            + " | so | he | hers | also | st | nd | rd | th | no | yes | will"
            + " | have | which | use | an"
            + " | copyright | copy | all | rights | reserved | retrieved"
            + " | https | com | www | txt | http | function | return | var"
            + " | length | else | null | true | data | false | test | nodetype"
            + " | font | px | edu | line | lato | size | li"
            + " | event | type | typeof | style | call | nodename | parentnode"
            + " | url | body | jquery | href | mm | mw | output | parser | cs"
            + " | width | left | br | nbsp | utm | amp | touchenabled | true"
            + " | responsive | top | gt | lt | raquo | laquo | bull | mdash"
            + " | quot | uri | wc | iri | org | larr | uarr | rarr | harr"
            + " | hellip | darr )";
    
    /*For whatever reason, \p{P} will not remove <, =, +, or >.
    For sure it doesn't remove |     */
    public static final String REGEX_PUNCT_ODDITY = "[<=>|+]";
    
    public static final String REGEX_HTML_ENTITIES = "(\\&nbsp;|\\&hellip;"
            + "|\\&larr;|\\&uarr;|\\&rarr;|\\&harr;|\\&darr;|\\&gt;|\\&lt;"
            + "|\\&amp;|\\&quot;|\\&apos;|\\&cent;|\\&pound;|\\&yen;"
            + "|\\&euro;|\\&copy;|\\&reg;"
            + "|\\&#\\d{4};)";
    
/*Remove HTML tags, punctuation, common words, numbers, and trim blank 
spaces from the string*/
    static String scrubString(String rawText){

        //Remove any spaces on the ends
        rawText = rawText.trim();
                
        //Change text to lowercase
        rawText = rawText.toLowerCase();  
        
        //Remove HTML tags and code
        rawText = removeHTML(rawText);
        
        //Remove punctuation
        rawText = rawText.replaceAll("\\p{P}", " ");
        
        //Remove numbers
        rawText = rawText.replaceAll("[0-9]", "");
        
        //Remove the characters \p{P} missed
        rawText = rawText.replaceAll(REGEX_PUNCT_ODDITY, " ");           
        
        //Clean up line if previous replacements removed all data
        rawText = rawText.trim();
        
        //If the entire line wasn't all HTML or code, remove the common words
        if (rawText.length() > 0) {
            rawText = removeCommonWords(rawText); 
        }
        
        //One last trim
        rawText = rawText.trim();
        
        return rawText;
    }
    
    
/*Remove all things HTML. If the line has <script>, ignore. Remove basic tags by
finding the start and end indexes of the <>'s:
    <footer>Das Foot</footer> = Das Foot
Remove HTML entities such as &nbsp; &rarr;  etc
If the line is only HTML code with no HTML tags, ignore.
    */
    static String removeHTML(String rawText){
        
        StringBuilder formattedString = new StringBuilder(rawText);
        
        //If line is <script>, remove
        if (rawText.matches(".*script>.*")){
            rawText = "";
        } 
        
        /*Remove as many HTML tags from the line as possible,
        until no more </ are found. Contians a counter to exit incase of an 
        infinte loop. Yes, somewere out there in the net, there's a line that 
        has:
             searchText = searchText.replace(/</gi, "< "); */
        int loopcount = 0; 
        while (rawText.matches(".*</.+")){
            
            ++loopcount;
            
            //Get index number of <
            int firstIndex = rawText.indexOf('<');
            //Get index number of >
            int lastIndex = rawText.indexOf('>');

            /*If the first index is less than the last index, insert a blank 
            space so the formatted string will not have 
            Clumped up text.Such as thisline.*/
            if (firstIndex < lastIndex){
                formattedString.replace(firstIndex, lastIndex + 1, " ");
            }
            /*Else delete everything to the left of the first index, inclusive
             (in case there's a line such as:
                style=\"display:none;width:0px;height:0px\"></iframe>)  */
            else {
                formattedString.replace(0, firstIndex, " ");
            }
            /*Put StringBuilder back into a String so subsequent RexEx's 
            will work */
            rawText = formattedString.toString();
            
            if (loopcount > 5000){
                break;
            }
        }
        
        //Reduce string builder storage size, just in case
        formattedString.trimToSize();
        
        //Remove HTML Character Entities
        rawText = replaceAllLoop(rawText, REGEX_HTML_ENTITIES);

        //Cleanup
        rawText = rawText.trim();

        /*What if the line is just HTML with no end tag? ie:
            <ul class="ldst__banner clearfix">
         Or, what if the line is something like:
            var cookie_suffix = '';
         Or, any other funky HTML syntaxes in the line?
        If there are, then return ""   */        
        if (rawText.length() > 0) {
            //Get last character
            String lastChar = rawText.charAt(rawText.length() - 1) + "";

            if (rawText.startsWith("<")
                //Codeing operands or assignemnts
                || lastChar.matches(REGEX_PUNCT_ODDITY)
                || lastChar.matches("\\p{P}")
                //Comments                   
                || rawText.matches(".*//.*")    
                //Unwanted file extentions or HTML no-page/moved messages    
                || !WebCrawl.isHtmlValid(rawText)){

                rawText = "";
            }
        }
        
        return rawText;
    }
    
/*Function to remove common words and non-breaking space characters. Repeat 
the replaceAll until no more concurrent common words are in the string*/
    static String removeCommonWords(String rawText){
        
        rawText = replaceAllLoop(rawText, COMMON_WORDS_REGEX);
        
        //Remove non-breaking space characters
        rawText = rawText.replace('\u00A0',' ');
        
        //Check for "bookend" common words in the string
        rawText = removeBookendCommonWords(rawText, COMMON_WORDS_REGEX);
     
        return rawText;
    }

/*Loop to match a word and replace it with a space, until there are no more 
words to replace*/     
    static String replaceAllLoop(String rawText, String finalString){
        /*Inserting a blank space so the formatted string will not have
            Clumped up text.Such as thisLINE.     */
        while (rawText.matches(".*" + finalString + ".*")){
            rawText = rawText.replaceAll(finalString, " ");
        }
        return rawText;
    }
    
/*What if a common word is at the beginning of a line with no spaces 
before it? Or at the end with no spaces after?*/    
    static String removeBookendCommonWords(String rawText, String finalString){
        
        //Prep COMMON_WORDS_REGEX for array split
        String commonWordListPrep = finalString.replaceAll("[(|)]", "");
        
        String[] commonWordsArray = commonWordListPrep.split(" ");        
        
        //Loop through the array
        for (int i = 0; i < commonWordsArray.length; i++){
            //Get the common word's length
            int commWordLength = commonWordsArray[i].length();
            //See if the common word's length is the same as rawText's
            boolean isCommWrdLenEqToTxtLn = (rawText.length() == commWordLength);
            //Skip spaces/blank items in the array
            if (commWordLength > 0) {
                /*If the rawText's length is equal to or greater than the common
                word, continue*/
                if (rawText.length() >= commWordLength){

                    //Check the left bookend of rawText
                    if (rawText.startsWith(commonWordsArray[i])){
                        /*Make sure the character after the word in rawText is a
                        space, to avoid changing something like "to" in 
                        "towards"*/
                        if(!isCommWrdLenEqToTxtLn){
                            if (rawText.charAt(commWordLength) == ' '){
                                rawText = rawText.replaceFirst
                                    (commonWordsArray[i], " ");
                            }
                        }
                        else{
                            //Remove common word
                            rawText = rawText.replaceFirst
                                    (commonWordsArray[i], " ");
                        }
                    }

                    //Check the right bookend of rawText
                    if (rawText.endsWith(commonWordsArray[i])){
                        if(!isCommWrdLenEqToTxtLn){
                            /*Make sure the character before the word in rawText
                            is a space, to avoid changing something like "by" in
                            "willoughby"*/
                           if(rawText.charAt((rawText.length() - commWordLength)
                                   - 1 ) == ' '){
                               //Call function
                               rawText = replaceRightBookend(rawText, 
                                       commonWordsArray[i], commWordLength);
                           }
                        }
                        else{
                            //Call function
                            rawText = replaceRightBookend(rawText, 
                                       commonWordsArray[i], commWordLength);
                        }
                    }
                }
            }
        } //End of for loop
        return rawText;
    }

/*replaceFirst will not properly replace the 2nd "by" in "Willoughby by". 
 Changing string to stringBuilder so word can be replaced via string indexes*/    
    static String replaceRightBookend(String rawText, String commonWord,
            int commWordLength){
        
        StringBuilder tempStrBuilder = new StringBuilder(rawText);
        
        tempStrBuilder.replace(rawText.length() - commWordLength, 
                rawText.length(), " ");
        
        rawText = tempStrBuilder.toString();
        
        return rawText;
    }
}
