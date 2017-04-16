/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.liderahenk.liderconsole.core.model;

public enum SearchFilterEnum {

	EQ("="), NOT_EQ("!="), GT(">="), LT("<=");

	private String operator;

	private SearchFilterEnum(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}

	/**
	 * Provide mapping enums with a fixed ID in JPA (a more robust alternative
	 * to EnumType.String and EnumType.Ordinal)
	 * 
	 * @param id
	 * @return related SearchFilterEnum enum
	 * @see http://blog.chris-ritchie.com/2013/09/mapping-enums-with-fixed-id-in
	 *      -jpa.html
	 * 
	 */
	public static SearchFilterEnum getType(String operator) {
		if (operator == null) {
			return null;
		}
		for (SearchFilterEnum type : SearchFilterEnum.values()) {
			if (operator.equals(type.getOperator())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching type for operator: " + operator);
	}
	
	public static String[] getOperators() {
		SearchFilterEnum[] values = SearchFilterEnum.values();
		String[] operators = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			operators[i] = values[i].getOperator();
		}
		return operators;
	}

}
