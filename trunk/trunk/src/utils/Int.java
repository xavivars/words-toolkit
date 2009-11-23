package words.utils;
import java.lang.Comparable;
import java.io.Serializable;

/**
 * A wrapper object for int's. Its value can be modified.
 * @author Rafael C. Carrasco
 * @version 1.1
 */
public class Int implements Comparable<Int>, Serializable {
  int n;       // The value.
  static final long serialVersionUID = 1L;

/**
 * Constructs a new Int with the specified int value.
 */
  public Int (int n) { this.n = n; }

/**
 * Constructs a new Int with the value indicated by the string.
 */
  public Int (String s) { n = Integer.parseInt(s); }

/**
 * Returns the value as int.
 */ 
  public int getValue () { return n; }

/**
 * Assigns the specified int value.
 */
  public Int setValue (int n) { this.n = n; return this; }

/**
 * Pre-increments value by 1.
 */
  public Int incValue () { ++n; return this; }

/**
 * Add argument to value.
 */
  public Int add (int n) { this.n += n; return this; }

/**
 * Pre-decrements value by 1.
 */
  public Int decValue () { --n; return this; }

/**
 * Post-increments value by one.
 */
  public Int postIncValue () {
    ++n; return new Int(n - 1);
  }

/**
 * Returns a new Int with incremented value.
 */
  public Int  nextInt()  { return new Int(n+1); }

/**
 * Returns a String object representing the specified Int.
 */
  public String toString() { return String.valueOf(n); } 

/**
 * Tests if two Int objects store the same value.
 */
  public boolean equals ( Int N ) { return n == N.n; }

/**
 * Tests if two Int objects store the same value.
 */
  public boolean equals ( int n ) { return this.n == n; }

/**
 * Compares this object to the specified object.
 * The result is true if and only if the argument is not null 
 * and is an Int object that contains the same int value as this object. 
 */
  public boolean equals (Object object) {
    if ( this == object ) return true;
    if ( !(object instanceof Int) ) return false;
    return n == ((Int)object).n;
  }

/**
 * Compares two Int objects numerically.
 */
  public int compareTo ( Int N ) { 
    if ( n < N.n ) return -1;
    else return ( n == N.n ) ? 0 : 1; 
  } 

}
