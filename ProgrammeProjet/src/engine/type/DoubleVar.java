package engine.type;


/**
 * Classe représentant une variable de type caractère
 *
 * @author Corentin Athinault, Mathieu Chougui, Clémence Edouard
 * @version 1.0b du 06/01/2017
 */
public class DoubleVar extends Variable implements Comparable<DoubleVar>
{
	private double value;

	/**
	 * Constructeur de la classe.
	 *
	 * @param name     Le nom de la variable
	 * @param strValue La valeur de la variable
	 */
	public DoubleVar( String name, String strValue )
	{
		super( name, strValue );
		type = "double";
		if(strValue != null)
			this.value = Double.parseDouble( strValue );
	}

	/**
	 * Méthode permettant d'obtenir la valeur de la variable
	 *
	 * @return La valeur de la variable
	 */
	public double getValue()
	{
		return value;
	}

	/**
	 * Méthode permettant de modifier la valeur de la variable
	 *
	 * @param value1 la future valeur de la variable
	 */
	public void setValue( double value1 )
	{
		if( !constant ) this.value = value1;
	}

	/**
	 * Méthode permettant de comparer l'objet courant et un autre de même type
	 *
	 * @param other L'objet avec lequelle comparer l'objet courant
	 * @return Une valeur permettant de comparer les deux objets
	 */
	public int compareTo( DoubleVar other )
	{
		return (int) (this.value - other.getValue());
	}
}
