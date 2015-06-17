package ca.uhn.fhir.rest.server.provider.dstu2;

/*
 * #%L
 * HAPI FHIR Structures - DSTU2 (FHIR v0.5.0)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.Conformance.Rest;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResource;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResourceInteraction;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResourceSearchParam;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import ca.uhn.fhir.model.dstu2.valueset.ConformanceResourceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.OperationParameterUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.ResourceTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.RestfulConformanceModeEnum;
import ca.uhn.fhir.model.dstu2.valueset.SystemRestfulInteractionEnum;
import ca.uhn.fhir.model.dstu2.valueset.TypeRestfulInteractionEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.method.BaseMethodBinding;
import ca.uhn.fhir.rest.method.DynamicSearchMethodBinding;
import ca.uhn.fhir.rest.method.IParameter;
import ca.uhn.fhir.rest.method.OperationMethodBinding;
import ca.uhn.fhir.rest.method.OperationParameter;
import ca.uhn.fhir.rest.method.SearchMethodBinding;
import ca.uhn.fhir.rest.method.SearchParameter;
import ca.uhn.fhir.rest.server.Constants;
import ca.uhn.fhir.rest.server.IServerConformanceProvider;
import ca.uhn.fhir.rest.server.ResourceBinding;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;

/**
 * Server FHIR Provider which serves the conformance statement for a RESTful server implementation
 * 
 * <p>
 * Note: This class is safe to extend, but it is important to note that the same instance of {@link Conformance} is always returned unless {@link #setCache(boolean)} is called with a value of
 * <code>false</code>. This means that if you are adding anything to the returned conformance instance on each call you should call <code>setCache(false)</code> in your provider constructor.
 * </p>
 */
public class ServerConformanceProvider implements IServerConformanceProvider<Conformance> {

	private boolean myCache = true;
	private volatile Conformance myConformance;
	private String myPublisher = "Not provided";
	private final RestfulServer myRestfulServer;

	public ServerConformanceProvider(RestfulServer theRestfulServer) {
		myRestfulServer = theRestfulServer;
	}

	/**
	 * Gets the value of the "publisher" that will be placed in the generated conformance statement. As this is a mandatory element, the value should not be null (although this is not enforced). The
	 * value defaults to "Not provided" but may be set to null, which will cause this element to be omitted.
	 */
	public String getPublisher() {
		return myPublisher;
	}

	@Override
	@Metadata
	public Conformance getServerConformance(HttpServletRequest theRequest) {
		if (myConformance != null && myCache) {
			return myConformance;
		}

		Conformance retVal = new Conformance();

		retVal.setPublisher(myPublisher);
		retVal.setDate(DateTimeDt.withCurrentTime());
		retVal.setFhirVersion("0.5.0"); // TODO: pull from model
		retVal.setAcceptUnknown(false); // TODO: make this configurable - this is a fairly big effort since the parser
										// needs to be modified to actually allow it

		retVal.getImplementation().setDescription(myRestfulServer.getImplementationDescription());
		retVal.getSoftware().setName(myRestfulServer.getServerName());
		retVal.getSoftware().setVersion(myRestfulServer.getServerVersion());
		retVal.addFormat(Constants.CT_FHIR_XML);
		retVal.addFormat(Constants.CT_FHIR_JSON);

		Rest rest = retVal.addRest();
		rest.setMode(RestfulConformanceModeEnum.SERVER);

		Set<SystemRestfulInteractionEnum> systemOps = new HashSet<SystemRestfulInteractionEnum>();

		List<ResourceBinding> bindings = new ArrayList<ResourceBinding>(myRestfulServer.getResourceBindings());
		Collections.sort(bindings, new Comparator<ResourceBinding>() {
			@Override
			public int compare(ResourceBinding theArg0, ResourceBinding theArg1) {
				return theArg0.getResourceName().compareToIgnoreCase(theArg1.getResourceName());
			}
		});

		for (ResourceBinding next : bindings) {

			Set<TypeRestfulInteractionEnum> resourceOps = new HashSet<TypeRestfulInteractionEnum>();
			RestResource resource = rest.addResource();

			String resourceName = next.getResourceName();
			RuntimeResourceDefinition def = myRestfulServer.getFhirContext().getResourceDefinition(resourceName);
			resource.getTypeElement().setValue(def.getName());
			resource.getProfile().setReference(new IdDt(def.getResourceProfile(myRestfulServer.getServerBaseForRequest(theRequest))));

			TreeSet<String> includes = new TreeSet<String>();

			// Map<String, Conformance.RestResourceSearchParam> nameToSearchParam = new HashMap<String,
			// Conformance.RestResourceSearchParam>();
			for (BaseMethodBinding<?> nextMethodBinding : next.getMethodBindings()) {
				if (nextMethodBinding.getResourceOperationType() != null) {
					String resOpCode = nextMethodBinding.getResourceOperationType().getCode();
					if (resOpCode != null) {
						TypeRestfulInteractionEnum resOp = TypeRestfulInteractionEnum.VALUESET_BINDER.fromCodeString(resOpCode);
						if (resOp == null) {
							throw new InternalErrorException("Unknown type-restful-interaction: " + resOpCode);
						}
						if (resourceOps.contains(resOp) == false) {
							resourceOps.add(resOp);
							resource.addInteraction().setCode(resOp);
						}
					}
				}

				if (nextMethodBinding.getSystemOperationType() != null) {
					String sysOpCode = nextMethodBinding.getSystemOperationType().getCode();
					if (sysOpCode != null) {
						SystemRestfulInteractionEnum sysOp = SystemRestfulInteractionEnum.VALUESET_BINDER.fromCodeString(sysOpCode);
						if (sysOp == null) {
							throw new InternalErrorException("Unknown system-restful-interaction: " + sysOpCode);
						}
						if (systemOps.contains(sysOp) == false) {
							systemOps.add(sysOp);
							rest.addInteraction().setCode(sysOp);
						}
					}
				}

				if (nextMethodBinding instanceof SearchMethodBinding) {
					handleSearchMethodBinding(rest, resource, resourceName, def, includes, (SearchMethodBinding) nextMethodBinding);
				} else if (nextMethodBinding instanceof DynamicSearchMethodBinding) {
					handleDynamicSearchMethodBinding(resource, def, includes, (DynamicSearchMethodBinding) nextMethodBinding);
				} else if (nextMethodBinding instanceof OperationMethodBinding) {
					OperationMethodBinding methodBinding = (OperationMethodBinding)nextMethodBinding;
					OperationDefinition op = new OperationDefinition();
					rest.addOperation().setName(methodBinding.getName()).getDefinition().setResource(op);;
					
					op.setStatus(ConformanceResourceStatusEnum.ACTIVE);
					op.setDescription(methodBinding.getDescription());
					op.setIdempotent(methodBinding.isIdempotent());
					op.setCode(methodBinding.getName());
					op.setInstance(methodBinding.isInstanceLevel());
					op.addType().setValue(methodBinding.getResourceName());
					
					for (IParameter nextParamUntyped : methodBinding.getParameters()) {
						if (nextParamUntyped instanceof OperationParameter) {
							OperationParameter nextParam = (OperationParameter)nextParamUntyped;
							Parameter param = op.addParameter();
							param.setUse(OperationParameterUseEnum.IN);
							if (nextParam.getParamType() != null) {
								param.setType(nextParam.getParamType().getCode());
							}
							param.setMin(nextParam.getMin());
							param.setMax(nextParam.getMax() == -1 ? "*" : Integer.toString(nextParam.getMax()));
							param.setName(nextParam.getName());
						}
					}
				}

				Collections.sort(resource.getInteraction(), new Comparator<RestResourceInteraction>() {
					@Override
					public int compare(RestResourceInteraction theO1, RestResourceInteraction theO2) {
						TypeRestfulInteractionEnum o1 = theO1.getCodeElement().getValueAsEnum();
						TypeRestfulInteractionEnum o2 = theO2.getCodeElement().getValueAsEnum();
						if (o1 == null && o2 == null) {
							return 0;
						}
						if (o1 == null) {
							return 1;
						}
						if (o2 == null) {
							return -1;
						}
						return o1.ordinal() - o2.ordinal();
					}
				});

			}

			for (String nextInclude : includes) {
				resource.addSearchInclude(nextInclude);
			}

		}

		myConformance = retVal;
		return retVal;
	}

	private void handleDynamicSearchMethodBinding(RestResource resource, RuntimeResourceDefinition def, TreeSet<String> includes, DynamicSearchMethodBinding searchMethodBinding) {
		includes.addAll(searchMethodBinding.getIncludes());

		List<RuntimeSearchParam> searchParameters = new ArrayList<RuntimeSearchParam>();
		searchParameters.addAll(searchMethodBinding.getSearchParams());
		sortRuntimeSearchParameters(searchParameters);

		if (!searchParameters.isEmpty()) {

			for (RuntimeSearchParam nextParameter : searchParameters) {

				String nextParamName = nextParameter.getName();

				// String chain = null;
				String nextParamUnchainedName = nextParamName;
				if (nextParamName.contains(".")) {
					// chain = nextParamName.substring(nextParamName.indexOf('.') + 1);
					nextParamUnchainedName = nextParamName.substring(0, nextParamName.indexOf('.'));
				}

				String nextParamDescription = nextParameter.getDescription();

				/*
				 * If the parameter has no description, default to the one from the resource
				 */
				if (StringUtils.isBlank(nextParamDescription)) {
					RuntimeSearchParam paramDef = def.getSearchParam(nextParamUnchainedName);
					if (paramDef != null) {
						nextParamDescription = paramDef.getDescription();
					}
				}

				RestResourceSearchParam param;
				param = resource.addSearchParam();

				param.setName(nextParamName);
				// if (StringUtils.isNotBlank(chain)) {
				// param.addChain(chain);
				// }
				param.setDocumentation(nextParamDescription);
				// param.setType(nextParameter.getParamType());
			}
		}
	}

	private void handleSearchMethodBinding(Rest rest, RestResource resource, String resourceName, RuntimeResourceDefinition def, TreeSet<String> includes, SearchMethodBinding searchMethodBinding) {
		includes.addAll(searchMethodBinding.getIncludes());

		List<IParameter> params = searchMethodBinding.getParameters();
		List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();
		for (IParameter nextParameter : params) {
			if ((nextParameter instanceof SearchParameter)) {
				searchParameters.add((SearchParameter) nextParameter);
			}
		}
		sortSearchParameters(searchParameters);
		if (!searchParameters.isEmpty()) {
			// boolean allOptional = searchParameters.get(0).isRequired() == false;
			//
			// OperationDefinition query = null;
			// if (!allOptional) {
			// RestOperation operation = rest.addOperation();
			// query = new OperationDefinition();
			// operation.setDefinition(new ResourceReferenceDt(query));
			// query.getDescriptionElement().setValue(searchMethodBinding.getDescription());
			// query.addUndeclaredExtension(false, ExtensionConstants.QUERY_RETURN_TYPE, new CodeDt(resourceName));
			// for (String nextInclude : searchMethodBinding.getIncludes()) {
			// query.addUndeclaredExtension(false, ExtensionConstants.QUERY_ALLOWED_INCLUDE, new StringDt(nextInclude));
			// }
			// }

			for (SearchParameter nextParameter : searchParameters) {

				String nextParamName = nextParameter.getName();

				String chain = null;
				String nextParamUnchainedName = nextParamName;
				if (nextParamName.contains(".")) {
					chain = nextParamName.substring(nextParamName.indexOf('.') + 1);
					nextParamUnchainedName = nextParamName.substring(0, nextParamName.indexOf('.'));
				}

				String nextParamDescription = nextParameter.getDescription();

				/*
				 * If the parameter has no description, default to the one from the resource
				 */
				if (StringUtils.isBlank(nextParamDescription)) {
					RuntimeSearchParam paramDef = def.getSearchParam(nextParamUnchainedName);
					if (paramDef != null) {
						nextParamDescription = paramDef.getDescription();
					}
				}

				RestResourceSearchParam param = resource.addSearchParam();
				param.setName(nextParamUnchainedName);
				if (StringUtils.isNotBlank(chain)) {
					param.addChain(chain);
				}
				param.setDocumentation(nextParamDescription);
				if (nextParameter.getParamType() != null) {
					param.getTypeElement().setValueAsString(nextParameter.getParamType().getCode());
				}
				for (Class<? extends IResource> nextTarget : nextParameter.getDeclaredTypes()) {
					RuntimeResourceDefinition targetDef = myRestfulServer.getFhirContext().getResourceDefinition(nextTarget);
					if (targetDef != null) {
						ResourceTypeEnum code = ResourceTypeEnum.VALUESET_BINDER.fromCodeString(targetDef.getName());
						if (code != null) {
							param.addTarget(code);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the cache property (default is true). If set to true, the same response will be returned for each invocation.
	 * <p>
	 * See the class documentation for an important note if you are extending this class
	 * </p>
	 */
	public void setCache(boolean theCache) {
		myCache = theCache;
	}

	/**
	 * Sets the value of the "publisher" that will be placed in the generated conformance statement. As this is a mandatory element, the value should not be null (although this is not enforced). The
	 * value defaults to "Not provided" but may be set to null, which will cause this element to be omitted.
	 */
	public void setPublisher(String thePublisher) {
		myPublisher = thePublisher;
	}

	private void sortRuntimeSearchParameters(List<RuntimeSearchParam> searchParameters) {
		Collections.sort(searchParameters, new Comparator<RuntimeSearchParam>() {
			@Override
			public int compare(RuntimeSearchParam theO1, RuntimeSearchParam theO2) {
				return theO1.getName().compareTo(theO2.getName());
			}
		});
	}

	private void sortSearchParameters(List<SearchParameter> searchParameters) {
		Collections.sort(searchParameters, new Comparator<SearchParameter>() {
			@Override
			public int compare(SearchParameter theO1, SearchParameter theO2) {
				if (theO1.isRequired() == theO2.isRequired()) {
					return theO1.getName().compareTo(theO2.getName());
				}
				if (theO1.isRequired()) {
					return -1;
				}
				return 1;
			}
		});
	}
}
