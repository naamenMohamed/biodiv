
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_list"/>
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Related Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>

<style>
.observations_list {
	top: 0;
}
</style>
<g:javascript>
	$(document).ready(function() {

		window.params = {
		<%
			params.each { key, value ->
				println '"'+key.replaceAll('"','')+'":"'+(value+'').replaceAll('"','')+'",'
			}
		%>
			"tagsLink":"${g.createLink(action: 'tags')}",
			"queryParamsMax":"${params.queryParams?.max}"
		}
	});
</g:javascript>

</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header clearfix">
						<h1>Related Observations</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div>
					<div class="tags_section span3" style="float: right;">
						<obv:showAllTags
							model="['tagFilterByProperty':'Related', 'relatedObvParams':initialParams, 'isAjaxLoad':false]" />
					</div>

					<div class="row">
						<div class="list span9">

							<div class="observations thumbwrap">
								<obv:showSnippet
										model="['observationInstance':parentObservation]"></obv:showSnippet>
								<h5 style="position:relative; top:40px; clear:both">
									<g:if test="${filterProperty == 'nearBy'}">
										Observations nearby
									</g:if>
									<g:elseif test="${filterProperty == 'speciesName'}">
										Observations of same species
									</g:elseif>
								</h5>
								<obv:showObservationsList />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
<r:script>
	$(document).ready(function() {

		$('#tc_tagcloud a').click(function(){
			var tg = $(this).contents().first().text();
			window.location.href = "${g.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	    	return false;
	 	});
	});
</r:script>	
</body>
</html>
