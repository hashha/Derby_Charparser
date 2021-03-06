/**
 * 
 */
package outputter.data;

import outputter.knowledge.Dictionary;

/**
 * @author updates
 *
 */
public class NegatedQuality extends Quality {
	Quality parentQuality;
	FormalRelation negation = Dictionary.complementof;
	
	

	/**
	 * 
	 */
	public NegatedQuality() {
		negation.setConfidenceScore(1);
	}

	/**
	 * @param string
	 * @param label
	 * @param id
	 * @param iri
	 */
	public NegatedQuality(String string, String label, String id, Quality parentQuality) {
		super("not "+string, label, id);
		this.parentQuality = parentQuality;
		negation.setConfidenceScore(1);
	}

	public NegatedQuality(Quality quality, Quality parentQuality) {
		super("not "+quality.getString(), quality.getLabel(), quality.getId());
		this.parentQuality = parentQuality;
		negation.setConfidenceScore(1);
	}
	
	
	public void setParentQuality(Quality parentQuality){
		this.parentQuality = parentQuality;
	}
	
	public Quality getParentQuality(){
		return this.parentQuality;
	}

	public String getFullString()
	{		
		//return this.parentQuality.getString()+" and ("+negation.getLabel()+" some "+this.string+")";
		return this.string;
	}
	
	public String getFullLabel()
	{
		return this.parentQuality.getLabel()+" and ("+negation.getLabel()+" some "+this.getLabel()+")";
	}
	
	public String getFullId()
	{
		return this.parentQuality.getId()+" and ("+negation.getId()+" some "+this.getId()+")";
	}
	
	/**
	 * not waisted => shape and complement_of some waisted
	 */
	public String toString(){
		return "phrase="+this.string+" quality="+parentQuality.getLabel()+" and "+negation.toString()+ " some "+ this.label+" score="+this.confidenceScore;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
