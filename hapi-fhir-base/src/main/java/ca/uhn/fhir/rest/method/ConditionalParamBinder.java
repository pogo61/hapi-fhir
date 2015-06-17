package ca.uhn.fhir.rest.method;

/*
 * #%L
 * HAPI FHIR - Core Library
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu.valueset.RestfulOperationTypeEnum;
import ca.uhn.fhir.rest.server.Constants;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;

class ConditionalParamBinder implements IParameter {

	private RestfulOperationTypeEnum myOperationType;

	ConditionalParamBinder(RestfulOperationTypeEnum theOperationType) {
		Validate.notNull(theOperationType, "theOperationType can not be null");
		myOperationType = theOperationType;
	}

	@Override
	public void translateClientArgumentIntoQueryArgument(FhirContext theContext, Object theSourceClientArgument, Map<String, List<String>> theTargetQueryArguments, IBaseResource theTargetResource) throws InternalErrorException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object translateQueryParametersIntoServerArgument(RequestDetails theRequest, byte[] theRequestContents, BaseMethodBinding<?> theMethodBinding) throws InternalErrorException, InvalidRequestException {

		if (myOperationType == RestfulOperationTypeEnum.CREATE) {
			String retVal = theRequest.getServletRequest().getHeader(Constants.HEADER_IF_NONE_EXIST);
			if (isBlank(retVal)) {
				return null;
			}
			if (retVal.startsWith(theRequest.getFhirServerBase())) {
				retVal = retVal.substring(theRequest.getFhirServerBase().length());
			}
			return retVal;
		} else if (myOperationType != RestfulOperationTypeEnum.DELETE && myOperationType != RestfulOperationTypeEnum.UPDATE) {
			return null;
		}
		
		if (theRequest.getId() != null && theRequest.getId().hasIdPart()) {
			return null;
		}
		boolean haveParam = false;
		for (String next : theRequest.getParameters().keySet()) {
			if (!next.startsWith("_")) {
				haveParam=true;
				break;
			}
		}
		if (!haveParam) {
			return null;
		}
		
		int questionMarkIndex = theRequest.getCompleteUrl().indexOf('?');
		return theRequest.getResourceName() + theRequest.getCompleteUrl().substring(questionMarkIndex);
	}

	@Override
	public void initializeTypes(Method theMethod, Class<? extends Collection<?>> theOuterCollectionType, Class<? extends Collection<?>> theInnerCollectionType, Class<?> theParameterType) {
		// nothing
	}

}
