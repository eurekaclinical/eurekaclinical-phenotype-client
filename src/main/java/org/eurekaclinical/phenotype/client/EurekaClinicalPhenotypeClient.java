/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
package org.eurekaclinical.phenotype.client;

import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.eurekaclinical.eureka.client.comm.CohortDestination;
import org.eurekaclinical.eureka.client.comm.Phenotype;
import org.eurekaclinical.eureka.client.comm.SystemPhenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.phenotype.client.comm.FrequencyType;
import org.eurekaclinical.phenotype.client.comm.RelationOperator;
import org.eurekaclinical.phenotype.client.comm.ThresholdsOperator;
import org.eurekaclinical.phenotype.client.comm.TimeUnit;
import org.eurekaclinical.phenotype.client.comm.ValueComparator;
import org.eurekaclinical.standardapis.exception.HttpStatusException;
import org.protempa.PropositionDefinition;

/**
 * @author hrathod
 */
public class EurekaClinicalPhenotypeClient extends EurekaClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(EurekaClinicalPhenotypeClient.class);
	private static final GenericType<List<TimeUnit>> TimeUnitList = new GenericType<List<TimeUnit>>() {
	};
	private static final GenericType<List<FrequencyType>> FrequencyTypeList = new GenericType<List<FrequencyType>>() {
	};
	private static final GenericType<List<RelationOperator>> RelationOperatorList = new GenericType<List<RelationOperator>>() {
	};
	private static final GenericType<List<ThresholdsOperator>> ThresholdsOperatorList = new GenericType<List<ThresholdsOperator>>() {
	};
	private static final GenericType<List<ValueComparator>> ValueComparatorList = new GenericType<List<ValueComparator>>() {
	};
	private static final GenericType<List<SystemPhenotype>> SystemPhenotypeList = new GenericType<List<SystemPhenotype>>() {
	};
	private static final GenericType<List<Phenotype>> PhenotypeList
			= new GenericType<List<Phenotype>>() {
	};

        
        private static final GenericType<List<PropositionDefinition>> PropositionList
			= new GenericType<List<PropositionDefinition>>() {
	};
        
	private static final GenericType<List<CohortDestination>> CohortDestinationListType
			= new GenericType<List<CohortDestination>>() {
	};
	private static final GenericType<List<String>> SystemPhenotypeSearchResultsList = new GenericType<List<String>>() {
	};


	private final URI phenotypeUrl;

	@Inject
	public EurekaClinicalPhenotypeClient(String inPhenotypeUrl) {
		super();
		LOGGER.info("Using phenotype-services URL {}", inPhenotypeUrl);
		this.phenotypeUrl = URI.create(inPhenotypeUrl);
	}

	@Override
	protected URI getResourceUrl() {
		return this.phenotypeUrl;
	}

	public List<Phenotype> getPhenotypes(String[] inKeys, boolean summarized) throws ClientException {
		List<Phenotype> result = new ArrayList<>();
		if (inKeys != null) {
			List<String> userPhenotypes = new ArrayList<>();
			List<String> systemPhenotypes = new ArrayList<>();
			for (String key : inKeys) {
				if (key.startsWith("USER:")) {
					userPhenotypes.add(key);
				} else {
					systemPhenotypes.add(key);
				}
			}
			if (!userPhenotypes.isEmpty()) {
				for (String userPhenotype : userPhenotypes) {
					result.add(getUserPhenotype(userPhenotype, summarized));
				}
			}
			if (!systemPhenotypes.isEmpty()) {
				result.addAll(getSystemPhenotypes(systemPhenotypes, summarized));
			}
		}
		return result;
	}

	public List<Phenotype> getUserPhenotypes(boolean summarized) throws ClientException {
		final String path = "/api/protected/phenotypes";
		if (summarized) {
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("summarize", "true");
			return doGet(path, queryParams, PhenotypeList);
		} else {
			return doGet(path, PhenotypeList);
		}
	}
        
        public List<PropositionDefinition> getPhenotypes2Proposition() throws ClientException {
		final String path = "/api/protected/phenotypes2prop";
		return doGet(path, PropositionList);
	}

	public Phenotype getUserPhenotype(String inKey, boolean summarized) throws ClientException {
		if (inKey == null) {
			throw new IllegalArgumentException("inKey cannot be null");
		}
		/*
		 * The inKey parameter may contain spaces, slashes and other 
		 * characters that are not allowed in URLs, so it needs to be
		 * encoded. We use UriBuilder to guarantee a valid URL. The inKey
		 * string can't be templated because the slashes won't be encoded!
		 */
		String path = UriBuilder
				.fromPath("/api/protected/phenotypes/")
				.segment(inKey)
				.build().toString();

		if (summarized) {
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("summarize", "true");
			return doGet(path, queryParams, Phenotype.class);
		} else {
			return doGet(path, Phenotype.class);
		}
	}
        
	public URI saveUserPhenotype(Phenotype inPhenotype) throws ClientException {
		final String path = "/api/protected/phenotypes";
		URI phenotypeURI = doPostCreate(path, inPhenotype);
		return phenotypeURI;
	}         
        
	public void updateUserPhenotype(Long inId, Phenotype inPhenotype) throws
			ClientException {
		if (inId == null) {
			throw new IllegalArgumentException("inId cannot be null");
		}                
		final String path = "/api/protected/phenotypes/"+ inId;
		doPut(path, inPhenotype);
	}        

	public void deleteUserPhenotype(Long inId) throws
			ClientException {
		if (inId == null) {
			throw new IllegalArgumentException("inId cannot be null");
		}
		/*
		 * The inKey parameter may contain spaces, slashes and other 
		 * characters that are not allowed in URLs, so it needs to be
		 * encoded. We use UriBuilder to guarantee a valid URL. The inKey
		 * string can't be templated because the slashes won't be encoded!
		 */
		final String path = "/api/protected/phenotypes/"+ inId;
		doDelete(path);
	}

	public List<SystemPhenotype> getSystemPhenotypes() throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/concepts/").build().toString();
		return doGet(path, SystemPhenotypeList);
	}

	public List<SystemPhenotype> getSystemPhenotypes(List<String> inKeys, boolean summarize) throws ClientException {
		if (inKeys == null) {
			throw new IllegalArgumentException("inKeys cannot be null");
		}
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		for (String key : inKeys) {
			formParams.add("key", key);
		}
		formParams.add("summarize", Boolean.toString(summarize));
		String path = UriBuilder.fromPath("/api/protected/concepts/")
				.build().toString();
		return doPost(path, formParams, SystemPhenotypeList);
	}

	public SystemPhenotype getSystemPhenotype(String inKey, boolean summarize) throws ClientException {
		List<SystemPhenotype> result = getSystemPhenotypes(Collections.singletonList(inKey), summarize);
		if (result.isEmpty()) {
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		} else {
			return result.get(0);
		}
	}

	public List<TimeUnit> getTimeUnitsAsc() throws ClientException {
		final String path = "/api/protected/timeunits";
		return doGet(path, TimeUnitList);
	}

	public TimeUnit getTimeUnit(Long inId) throws ClientException {
		final String path = "/api/protected/timeunits/" + inId;
		return doGet(path, TimeUnit.class);
	}

	public TimeUnit getTimeUnitByName(String inName) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/timeunits/byname/")
				.segment(inName)
				.build().toString();
		return doGet(path, TimeUnit.class);
	}

	public List<RelationOperator> getRelationOperatorsAsc() throws ClientException {
		final String path = "/api/protected/relationops";
		return doGet(path, RelationOperatorList);
	}

	public RelationOperator getRelationOperator(Long inId) throws ClientException {
		final String path = "/api/protected/relationops/" + inId;
		return doGet(path, RelationOperator.class);
	}

	public RelationOperator getRelationOperatorByName(String inName) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/relationops/byname/")
				.segment(inName)
				.build().toString();
		return doGet(path, RelationOperator.class);
	}

	public List<ThresholdsOperator> getThresholdsOperators() throws ClientException {
		final String path = "/api/protected/thresholdsops/";
		return doGet(path, ThresholdsOperatorList);
	}

	public ThresholdsOperator getThresholdsOperator(Long inId) throws ClientException {
		final String path = "/api/protected/thresholdsops/" + inId;
		return doGet(path, ThresholdsOperator.class);
	}

	public ThresholdsOperator getThresholdsOperatorByName(
			String inName) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/thresholdsops/byname/")
				.segment(inName)
				.build().toString();
		return doGet(path, ThresholdsOperator.class);
	}

	public List<ValueComparator> getValueComparatorsAsc() throws ClientException {
		final String path = "/api/protected/valuecomps";
		return doGet(path, ValueComparatorList);
	}

	public ValueComparator getValueComparator(Long inId) throws ClientException {
		final String path = "/api/protected/valuecomps/" + inId;
		return doGet(path, ValueComparator.class);
	}

	public ValueComparator getValueComparatorByName(String inName) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/valuecomps/byname/")
				.segment(inName)
				.build().toString();
		return doGet(path, ValueComparator.class);
	}

	public List<FrequencyType> getFrequencyTypesAsc() throws ClientException {
		final String path = "/api/protected/frequencytypes";
		return doGet(path, FrequencyTypeList);
	}

	//Search Functionality
	public List<String> getSystemPhenotypeSearchResults(String searchKey) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/concepts/search/")
				.segment(searchKey)
				.build().toString();
		return doGet(path, SystemPhenotypeSearchResultsList);
	}

	//Search Functionality
	public List<SystemPhenotype> getSystemPhenotypeSearchResultsBySearchKey(String searchKey) throws ClientException {
		final String path = UriBuilder.fromPath("/api/protected/concepts/propsearch/")
				.segment(searchKey)
				.build().toString();
		return doGet(path, SystemPhenotypeList);
	}
	
}
