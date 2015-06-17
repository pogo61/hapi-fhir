package ca.uhn.fhir.jpa.entity;

/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2015 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

//@formatter:off
@Entity
@Table(name = "HFJ_SPIDX_NUMBER" /*, indexes= {@Index(name="IDX_SP_NUMBER", columnList="SP_VALUE")}*/ )
@org.hibernate.annotations.Table(appliesTo = "HFJ_SPIDX_NUMBER", indexes= {
		@org.hibernate.annotations.Index(name="IDX_SP_NUMBER", columnNames= {"RES_TYPE", "SP_NAME", "SP_VALUE"}
	)})
//@formatter:on
public class ResourceIndexedSearchParamNumber extends BaseResourceIndexedSearchParam {

	private static final long serialVersionUID = 1L;

	@Column(name = "SP_VALUE", nullable = true)
	public BigDecimal myValue;
	
	public ResourceIndexedSearchParamNumber() {
	}
	
	public ResourceIndexedSearchParamNumber(String theParamName, BigDecimal theValue) {
		setParamName(theParamName);
		setValue(theValue);
	}

	public BigDecimal getValue() {
		return myValue;
	}

	public void setValue(BigDecimal theValue) {
		myValue = theValue;
	}

}
