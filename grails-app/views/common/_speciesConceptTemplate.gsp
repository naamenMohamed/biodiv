
<%@page import="species.Reference"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<div class="ui-widget  <%=sparse?'':'menubutton'%>">
	<g:set var="fieldCounter" value="${1}" />
	<a href="#content" <%=sparse?'style=\"display:none\"':''%>> ${concept.key} </a>

	<!-- speciesConcept section -->
	<div
		class="speciesConcept <%=concept.key.equals(grailsApplication.config.speciesPortal.fields.OVERVIEW)?'defaultSpeciesConcept':''%>"
		id="speciesConcept${conceptCounter++}" <%=sparse?'':'style=\"display:none\"'%>>
		<div class="speciesFieldHeader ui-dialog-titlebar  ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#taxonRecordName"> ${concept.key}</a> 
		</div>
		

		<!-- speciesField section -->
		<div id="speciesField${conceptCounter}.${fieldCounter++}"
			class="speciesField ui-widget-content">

			<g:if test="${concept.value.containsKey('speciesFieldInstance')}">
				<g:each in="${ concept.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
				<g:showSpeciesField
					model="['speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
				</g:each>
			</g:if>
			<g:else>
				<g:each in="${concept.value}" var="category">
					<br/>
					<g:if test="${!category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SUMMARY) }">

						<div class="" style="clear:both">
							<div
								class="speciesFieldHeader category-header ui-dialog-titlebar  ui-helper-clearfix ">


								<a class="category-header-heading"
									href="#speciesField${conceptCounter}.${fieldCounter}"><h4> ${category.key} </h4>
								</a>

							</div>

							<div id="speciesField${conceptCounter}.${fieldCounter++}"
								class="<%=category.key.equals(grailsApplication.config.speciesPortal.fields.BRIEF)?'defaultSpeciesField':''%> speciesField  ">

								<g:if test="${category.value.containsKey('speciesFieldInstance') || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS) || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
									<g:if test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.COMMON_NAME)}">
										<div>
											<g:showSpeciesFieldHelp
												model="['speciesFieldInstance':(speciesInstance.commonNames as List)[0]]" /><br/>
											<%
										Map names = new LinkedHashMap();
										speciesInstance.commonNames.each(){
											String languageName = it?.language?.name ?: "Others";
											if(!names.containsKey(languageName)) {
												names.put(languageName, new ArrayList());
											}
											names.get(languageName).add(it)
										};
										%>
											<g:if test="${names}">
												<table style="width:100%;">
													<g:each in="${names}">
														<tr class="">
															<td class="grid_3"><b> ${it.key} </b></td>
															<td style="width:100%;"><g:each in="${it.value}">
															<g:showSpeciesFieldAttribution
												model="['speciesFieldInstance':it]" />
																	<a href="#" class="speciesName"> ${it.name}</a>
																	
																	</g:each></td>
														</tr>
													</g:each>
												</table>
											</g:if>
										</div>
									</g:if>

									<g:elseif
										test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS)}">
										<g:showSpeciesFieldToolbar model="${category.value[0]}" />
										<br />
										<div id="map">
										<div class=" message ui-state-highlight ui-corner-all">
											This is a
											demo map component to show the distribution of the species.
											The current map is only indicative.<br/>
										</div>
										<br />
										<div id="map1311326056727" class="occurenceMap"
											style="height: 600px; width: 100%"></div>
										</div>
									</g:elseif>

									<g:elseif test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
										<g:showSpeciesFieldToolbar model="${category.value[0]}" />
										<%def references = speciesInstance.fields.collect{it.references};
										Map refs = new LinkedHashMap();
										references.each(){
											if(it) {
												it.each() {
													refs.put(it?.url?.trim()?:it?.title, it)
												}
											}
										};
									
										if(category.value.get('speciesFieldInstance')[0]?.description) {
											category.value.get('speciesFieldInstance')[0]?.description?.replaceAll(/<.*?>/, '\n').split('\n').each() {
												if(it) {
												if(it.startsWith("http://")) {
													refs.put(it, new Reference(url:it));
												} else {
													refs.put(it, new Reference(title:it));
												}
												}
											}
										}
										references = refs.values();
										%>
										<g:if test="${references }">
											<ol class="references">
												<g:each in="${references}" var="r">
													<li><g:if test="${r.url}">
															<a href="${r.url}" target="_blank"> ${r.title?r.title:r.url}
															</a>
														</g:if> <g:else>
															${r?.title}
														</g:else>
													</li>
												</g:each>

											</ol>
										</g:if>
									</g:elseif>

									<g:else>
										<g:each in="${ category.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
										<g:showSpeciesField
											model="['speciesInstance' : speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
										</g:each>
									</g:else>
								</g:if>

								<g:if test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SYNONYMS)}">

									<div>
									 
										<g:showSpeciesFieldHelp
											model="['speciesFieldInstance':(speciesInstance.synonyms as List)[0]]" />
									<br/>
										<g:each in="${speciesInstance.synonyms}">
										<g:showSpeciesFieldAttribution
											model="['speciesFieldInstance':it]" />
											<div class="">
												<span class="grid_3"><b> ${it?.relationship?.value()} </b> </span> <span>
													<a class="speciesName" href="#"> ${it?.name} </a> </span>
											</div>
										</g:each>

									</div>

								</g:if>

								<g:elseif
									test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME)}">
									<!-- ignore -->
								</g:elseif>
								<g:elseif
									test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY) || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY)}">
									
								</g:elseif>
								<g:else>
									<g:each in="${category.value}">
										<g:if
											test="${!it.key.equals('field') && !it.key.equals('speciesFieldInstance')}">

											<div>
												<g:each in="${ it.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
												<g:showSpeciesField
													model="['speciesInstance': speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
												</g:each>
											</div>
										</g:if>
									</g:each>
								</g:else>
							</div>
							<br/>
						</div>
					</g:if>
				</g:each>

			</g:else>

		</div>

	</div>
</div>

