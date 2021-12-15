/*
 * Programmer: Dan Hopp
 * Date: 27-FEB-2020
 * Description: Class for the Word Object(s). Contains:

    String for the word
    Count for the word
    Default constructor
    Constructor with a pass-a-value
    Getter methods
    A method to up the word's count by 1
    A default sort order by using the word's count
    Print template for the export file's text

   How-to's, help, and guidance for sorting objects within an ArrayList were 
   taken from:
   https://docs.oracle.com/javase/8/docs/api/
   https://howtodoinjava.com/sort/sort-arraylist-objects-comparable-comparator/

 */
package lab3;

class WordObject implements Comparable<WordObject>{
    
    private final String WORD;
    private int count;

    //Default constructor
    public WordObject(){
        WORD = "";
        count = 1;
    }

    //Constructor with pass-a-value
    public WordObject(String newWord){
        WORD = newWord;
        count = 1;
    }

    //Getter methods
    public String getWord() {
        return WORD;
    }
    public int getCount() {
        return count;
    }

    //Up the word's count by 1
    public void addToWordCount(){
        count = count + 1;
    }

    //Setting up a default sort order by using the word's count
    @Override
    public int compareTo(WordObject obj) {
        //Flipping comparing operands so the ordering will be in desc order
        return obj.count - this.count;
    }
    
    //The print template for the export file's text
    @Override
    public String toString(){
        return WORD + " " + count;
    }
    
}
