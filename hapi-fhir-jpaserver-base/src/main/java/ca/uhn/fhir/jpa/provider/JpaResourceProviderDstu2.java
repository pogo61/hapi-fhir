package ca.uhn.fhir.jpa.provider;

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

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.jpa.dao.IFhirResourceDao;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.base.resource.BaseOperationOutcome.BaseIssue;
import ca.uhn.fhir.model.dstu2.composite.MetaDt;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.EncodingEnum;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class JpaResourceProviderDstu2<T extends IResource> extends BaseJpaResourceProvider<T> {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JpaResourceProviderDstu2.class);

	public JpaResourceProviderDstu2() {
		// nothing
	}

	public JpaResourceProviderDstu2(IFhirResourceDao<T> theDao) {
		super(theDao);
	}

	@Create
	public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam T theResource, @ConditionalUrlParam String theConditional) {
		startRequest(theRequest);
		try {
			if (theConditional != null) {
				return getDao().create(theResource, theConditional);
			} else {
				return getDao().create(theResource);
			}
		} finally {
			endRequest(theRequest);
		}
	}

	@Delete
	public MethodOutcome delete(HttpServletRequest theRequest, @IdParam IdDt theResource, @ConditionalUrlParam String theConditional) {
		startRequest(theRequest);
		try {
			if (theConditional != null) {
				return getDao().deleteByUrl(theConditional);
			} else {
				return getDao().delete(theResource);
			}
		} finally {
			endRequest(theRequest);
		}
	}

	//@formatter:off
	@Operation(name="$meta", idempotent=true, returnParameters= {
		@OperationParam(name="return", type=MetaDt.class)
	})
	//@formatter:on
	public Parameters meta() {
		Parameters parameters = new Parameters();
		parameters.addParameter().setName("return").setValue(getDao().metaGetOperation());
		return parameters;
	}

	//@formatter:off
	@Operation(name="$meta", idempotent=true, returnParameters= {
		@OperationParam(name="return", type=MetaDt.class)
	})
	//@formatter:on
	public Parameters meta(@IdParam IdDt theId) {
		Parameters parameters = new Parameters();
		parameters.addParameter().setName("return").setValue(getDao().metaGetOperation(theId));
		return parameters;
	}

	//@formatter:off
	@Operation(name="$meta-add", idempotent=true, returnParameters= {
		@OperationParam(name="return", type=MetaDt.class)
	})
	//@formatter:on
	public Parameters metaAdd(@IdParam IdDt theId, @OperationParam(name = "meta") MetaDt theMeta) {
		Parameters parameters = new Parameters();
		parameters.addParameter().setName("return").setValue(getDao().metaAddOperation(theId, theMeta));
		return parameters;
	}

	//@formatter:off
	@Operation(name="$meta-delete", idempotent=true, returnParameters= {
		@OperationParam(name="return", type=MetaDt.class)
	})
	//@formatter:on
	public Parameters metaDelete(@IdParam IdDt theId, @OperationParam(name = "meta") MetaDt theMeta) {
		Parameters parameters = new Parameters();
		parameters.addParameter().setName("return").setValue(getDao().metaDeleteOperation(theId, theMeta));
		return parameters;
	}

	@Update
	public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam T theResource, @IdParam IdDt theId, @ConditionalUrlParam String theConditional) {
		startRequest(theRequest);
		try {
			if (theConditional != null) {
				return getDao().update(theResource, theConditional);
			} else {
				theResource.setId(theId);
				return getDao().update(theResource);
			}
		} catch (ResourceNotFoundException e) {
			ourLog.info("Can't update resource with ID[" + theId.getValue() + "] because it doesn't exist, going to create it instead");
			theResource.setId(theId);
			return getDao().create(theResource);
		} finally {
			endRequest(theRequest);
		}
	}

	@Validate
	public MethodOutcome validate(@ResourceParam T theResource, @IdParam IdDt theId, @ResourceParam String theRawResource, @ResourceParam EncodingEnum theEncoding, @Validate.Mode ValidationModeEnum theMode,
			@Validate.Profile String theProfile) {

		final OperationOutcome oo = new OperationOutcome();

		IParser parser = theEncoding.newParser(getContext());
		parser.setParserErrorHandler(new IParserErrorHandler() {

			@Override
			public void unknownAttribute(IParseLocation theLocation, String theAttributeName) {
				oo.addIssue().setSeverity(IssueSeverityEnum.ERROR).setCode(IssueTypeEnum.INVALID_CONTENT).setDetails("Unknown attribute found: " + theAttributeName);
			}

			@Override
			public void unknownElement(IParseLocation theLocation, String theElementName) {
				oo.addIssue().setSeverity(IssueSeverityEnum.ERROR).setCode(IssueTypeEnum.INVALID_CONTENT).setDetails("Unknown element found: " + theElementName);
			}
		});

		FhirValidator validator = getContext().newValidator();
		validator.setValidateAgainstStandardSchema(true);
		validator.setValidateAgainstStandardSchematron(true);
		ValidationResult result = validator.validateWithResult(theResource);
		for (BaseIssue next : result.getOperationOutcome().getIssue()) {
			oo.getIssue().add((Issue) next);
		}

		// This method returns a MethodOutcome object
		MethodOutcome retVal = new MethodOutcome();
		oo.addIssue().setSeverity(IssueSeverityEnum.INFORMATION).setDetails("Validation succeeded");
		retVal.setOperationOutcome(oo);

		return retVal;
	}

}
