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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeChildDefinition.IAccessor;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeChildPrimitiveDatatypeDefinition;
import ca.uhn.fhir.context.RuntimePrimitiveDatatypeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.i18n.HapiLocalizer;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.param.CollectionBinder;
import ca.uhn.fhir.rest.param.ResourceParameter;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;

public class OperationParameter implements IParameter {

	private IConverter myConverter;
	@SuppressWarnings("rawtypes")
	private Class<? extends Collection> myInnerCollectionType;
	private int myMax;
	private int myMin;
	private final String myName;
	private final String myOperationName;
	private Class<?> myParameterType;
	private RestSearchParameterTypeEnum myParamType;

	public OperationParameter(String theOperationName, OperationParam theOperationParam) {
		this(theOperationName, theOperationParam.name(), theOperationParam.min(), theOperationParam.max());
	}

	OperationParameter(String theOperationName, String theParameterName, int theMin, int theMax) {
		myOperationName = theOperationName;
		myName = theParameterName;
		myMin = theMin;
		myMax = theMax;
	}

	private static void addClientParameter(FhirContext theContext, Object theSourceClientArgument, IBaseResource theTargetResource, BaseRuntimeChildDefinition paramChild, BaseRuntimeElementCompositeDefinition<?> paramChildElem, String theName) {
		if (theSourceClientArgument instanceof IBaseResource) {
			IBase parameter = createParameterRepetition(theContext, theTargetResource, paramChild, paramChildElem, theName);
			paramChildElem.getChildByName("resource").getMutator().addValue(parameter, (IBaseResource) theSourceClientArgument);
		} else if (theSourceClientArgument instanceof IBaseDatatype) {
			IBase parameter = createParameterRepetition(theContext, theTargetResource, paramChild, paramChildElem, theName);
			paramChildElem.getChildByName("value[x]").getMutator().addValue(parameter, (IBaseDatatype) theSourceClientArgument);
		} else if (theSourceClientArgument instanceof Collection) {
			Collection<?> collection = (Collection<?>) theSourceClientArgument;
			for (Object next : collection) {
				addClientParameter(theContext, next, theTargetResource, paramChild, paramChildElem, theName);
			}
		} else {
			throw new IllegalArgumentException("Don't know how to handle value of type " + theSourceClientArgument.getClass() + " for paramater " + theName);
		}
	}

	private static IBase createParameterRepetition(FhirContext theContext, IBaseResource theTargetResource, BaseRuntimeChildDefinition paramChild, BaseRuntimeElementCompositeDefinition<?> paramChildElem, String theName) {
		IBase parameter = paramChildElem.newInstance();
		paramChild.getMutator().addValue(theTargetResource, parameter);
		IPrimitiveType<?> value;
		if (theContext.getVersion().getVersion().equals(FhirVersionEnum.DSTU2_HL7ORG)) {
			value = (IPrimitiveType<?>) theContext.getElementDefinition("string").newInstance(theName);
		} else {
			value = new StringDt(theName);
		}
		paramChildElem.getChildByName("name").getMutator().addValue(parameter, value);
		return parameter;
	}

	public int getMax() {
		return myMax;
	}

	public int getMin() {
		return myMin;
	}

	public String getName() {
		return myName;
	}

	public RestSearchParameterTypeEnum getParamType() {
		return myParamType;
	}

	@Override
	public void initializeTypes(Method theMethod, Class<? extends Collection<?>> theOuterCollectionType, Class<? extends Collection<?>> theInnerCollectionType, Class<?> theParameterType) {
		myParameterType = theParameterType;
		if (theInnerCollectionType != null) {
			myInnerCollectionType = CollectionBinder.getInstantiableCollectionType(theInnerCollectionType, myName);
		} else {
			myMax = 1;
		}
	}

	public OperationParameter setConverter(IConverter theConverter) {
		myConverter = theConverter;
		return this;
	}

	@Override
	public void translateClientArgumentIntoQueryArgument(FhirContext theContext, Object theSourceClientArgument, Map<String, List<String>> theTargetQueryArguments, IBaseResource theTargetResource) throws InternalErrorException {
		assert theTargetResource != null;
		Object sourceClientArgument = theSourceClientArgument;
		if (sourceClientArgument == null) {
			return;
		}

		if (myConverter != null) {
			sourceClientArgument = myConverter.outgoingClient(sourceClientArgument);
		}

		addParameterToParameters(theContext, theTargetResource, sourceClientArgument, myName);
	}

	public static void addParameterToParameters(FhirContext theContext, IBaseResource theTargetResource, Object sourceClientArgument, String theName) {
		RuntimeResourceDefinition def = theContext.getResourceDefinition(theTargetResource);
		BaseRuntimeChildDefinition paramChild = def.getChildByName("parameter");
		BaseRuntimeElementCompositeDefinition<?> paramChildElem = (BaseRuntimeElementCompositeDefinition<?>) paramChild.getChildByName("parameter");

		addClientParameter(theContext, sourceClientArgument, theTargetResource, paramChild, paramChildElem, theName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object translateQueryParametersIntoServerArgument(RequestDetails theRequest, byte[] theRequestContents, BaseMethodBinding<?> theMethodBinding) throws InternalErrorException, InvalidRequestException {
		List<Object> matchingParamValues = new ArrayList<Object>();

		if (theRequest.getRequestType() == RequestTypeEnum.GET) {
			String[] paramValues = theRequest.getParameters().get(myName);
			if (paramValues != null && paramValues.length > 0) {
				if (IPrimitiveType.class.isAssignableFrom(myParameterType)) {
					for (String nextValue : paramValues) {
						FhirContext ctx = theRequest.getServer().getFhirContext();
						RuntimePrimitiveDatatypeDefinition def = (RuntimePrimitiveDatatypeDefinition) ctx.getElementDefinition((Class<? extends IBase>) myParameterType);
						IPrimitiveType<?> instance = def.newInstance();
						instance.setValueAsString(nextValue);
						matchingParamValues.add(instance);
					}
				} else {
					HapiLocalizer localizer = theRequest.getServer().getFhirContext().getLocalizer();
					String msg = localizer.getMessage(OperationParameter.class, "urlParamNotPrimitive", myOperationName, myName);
					throw new MethodNotAllowedException(msg, RequestTypeEnum.POST);
				}
			}
		} else {

			FhirContext ctx = theRequest.getServer().getFhirContext();

			if (theRequest.getRequestType() == RequestTypeEnum.GET) {
				return null;
			}

			Class<? extends IBaseResource> wantedResourceType = theMethodBinding.getContext().getResourceDefinition("Parameters").getImplementingClass();
			IBaseResource requestContents = ResourceParameter.loadResourceFromRequest(theRequest, theRequestContents, theMethodBinding, wantedResourceType);

			RuntimeResourceDefinition def = ctx.getResourceDefinition(requestContents);

			BaseRuntimeChildDefinition paramChild = def.getChildByName("parameter");
			BaseRuntimeElementCompositeDefinition<?> paramChildElem = (BaseRuntimeElementCompositeDefinition<?>) paramChild.getChildByName("parameter");

			RuntimeChildPrimitiveDatatypeDefinition nameChild = (RuntimeChildPrimitiveDatatypeDefinition) paramChildElem.getChildByName("name");
			BaseRuntimeChildDefinition valueChild = paramChildElem.getChildByName("value[x]");
			BaseRuntimeChildDefinition resourceChild = paramChildElem.getChildByName("resource");

			IAccessor paramChildAccessor = paramChild.getAccessor();
			List<IBase> values = paramChildAccessor.getValues(requestContents);
			for (IBase nextParameter : values) {
				List<IBase> nextNames = nameChild.getAccessor().getValues(nextParameter);
				if (nextNames != null && nextNames.size() > 0) {
					IPrimitiveType<?> nextName = (IPrimitiveType<?>) nextNames.get(0);
					if (myName.equals(nextName.getValueAsString())) {

						if (myParameterType.isAssignableFrom(nextParameter.getClass())) {
							matchingParamValues.add(nextParameter);
						} else {
							List<IBase> paramValues = valueChild.getAccessor().getValues(nextParameter);
							List<IBase> paramResources = resourceChild.getAccessor().getValues(nextParameter);
							if (paramValues != null && paramValues.size() > 0) {
								tryToAddValues(paramValues, matchingParamValues);
							} else if (paramResources != null && paramResources.size() > 0) {
								tryToAddValues(paramResources, matchingParamValues);
							}
						}

					}
				}
			}

		}

		if (matchingParamValues.isEmpty()) {
			return null;
		}

		if (myInnerCollectionType == null) {
			return matchingParamValues.get(0);
		}

		try {
			@SuppressWarnings("rawtypes")
			Collection retVal = myInnerCollectionType.newInstance();
			retVal.addAll(matchingParamValues);
			return retVal;
		} catch (InstantiationException e) {
			throw new InternalErrorException("Failed to instantiate " + myInnerCollectionType, e);
		} catch (IllegalAccessException e) {
			throw new InternalErrorException("Failed to instantiate " + myInnerCollectionType, e);
		}
	}

	private void tryToAddValues(List<IBase> theParamValues, List<Object> theMatchingParamValues) {
		for (Object nextValue : theParamValues) {
			if (nextValue == null) {
				continue;
			}
			if (myConverter != null) {
				nextValue = myConverter.incomingServer(nextValue);
			}
			if (!myParameterType.isAssignableFrom(nextValue.getClass())) {
				throw new InvalidRequestException("Request has parameter " + myName + " of type " + nextValue.getClass().getSimpleName() + " but method expects type " + myParameterType.getSimpleName());
			}
			theMatchingParamValues.add(nextValue);
		}
	}

	public interface IConverter {

		Object incomingServer(Object theObject);

		Object outgoingClient(Object theObject);

	}

}
