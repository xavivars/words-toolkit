package words.hmm;
/**
 * HashMap extension that associates a real value to every object
 */
public class Value<Type> extends java.util.HashMap<Type, Double> {    
    /**
     * Set the value associated to an object.
     * @param object an object of class Type
     * @param value the new value for this object
     */
    public void setValue (Type object, double value) {
		put(object, value);
    }
    /**
     * Increase the value associated to an object.
     * @param object an object of class Type
     * @param value the value added to this object
     */
    public void addValue (Type object, double value) {
		if ( containsKey(object) ) {
			put(object, get(object) + value);
		} else { 
			put(object, value);
		}
    }
    /**
     * Maximize the value associated to an object.
     * @param object an object of class Type
     * @param value the value to be compared with the one
     * associated to this object (and the larger is stored).
     */
    public void maxValue (Type object, double value) {
		if ( containsKey(object) ) {
			put(object, Math.max(get(object), value));
		} else { 
			put(object, value);
		}
    }
    /**
     * Get the value associated to an object.
     * @param object an object of class Type
     * @return the value associated to this object
     */
    public double getValue (Type object) {
		return containsKey(object) ? get(object) : 0;
    }
    /**
     * String representation
     * @return a string representing the mapping 
     */
    public String toString() {
		StringBuffer buff;
		buff = new StringBuffer();
		for (Type object : keySet()){
			buff.append(object + " " + get(object) + "\n");
		}
		return buff.toString();
    }
}
