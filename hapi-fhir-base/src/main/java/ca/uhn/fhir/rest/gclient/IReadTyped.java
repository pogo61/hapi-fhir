package ca.uhn.fhir.rest.gclient;

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

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;

public interface IReadTyped<T extends IBaseResource> {

	IReadExecutable<T> withId(String theId);

	IReadExecutable<T> withIdAndVersion(String theId, String theVersion);

	/**
	 * Search using an ID. Note that even if theId contains a base URL it will be
	 * ignored in favour of the base url for the given client. If you want to specify 
	 * an absolute URL including a base and have that base used instead, use
	 * {@link #withUrl(IdDt)}
	 */
	IReadExecutable<T> withId(IdDt theId);

	IReadExecutable<T> withUrl(String theUrl);

	IReadExecutable<T> withUrl(IdDt theUrl);
}
