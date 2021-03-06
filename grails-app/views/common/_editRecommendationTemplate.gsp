<%@ page import="species.participation.RecommendationVote.ConfidenceType"%>
<%@ page import="species.Language"%>
<!-- TODO change this r:script which is used by resources framework for script not to be repeated multiple times -->
<r:script>
$(document).ready(function() {

	//TODO : global variables ... may be problematic
	var cacheSN = {}, cacheCN = {}, lastXhrSN, lastXhrCN;
	$("#name").catcomplete({
			appendTo:"#nameSuggestions",
		 	source:function( request, response ) {
				var term = request.term;
				if ( term in cacheSN ) {
					response( cacheSN[ term ] );
					return;
				}
				request.nameFilter = "scientificNames";
				lastXhrSN = $.getJSON( "${createLink(controller:'recommendation', action: 'suggest')}", request, function( data, status, xhr ) {
					cacheSN[ term ] = data;
					if ( xhr === lastXhrSN ) {
						response( data );
					}
				});
			},focus: function( event, ui ) {
				$("#canName").val("");
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$("#nameSuggestions li a").css('border', 0);
				return false;
			},
			select: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#canName" ).val( ui.item.value );
				$("#mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
				return false;
			},open: function(event, ui) {
				$("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			ul.removeClass().addClass("dropdown-menu")
			if(item.category == "General") {
				return $( "<li class='span3'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${resource(dir:'images',file:'no-image.jpg', absolute:true)}"
                                            //${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='span3'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a title='"+item.label.replace(/<.*?>/g,"")+"'><img src='" + item.icon+"' class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.appendTo( ul );
			}
		};
		
		$("#commonName").catcomplete({
			appendTo:"#commonNameSuggestions",
		 	source:function( request, response ) {
				var term = request.term;
				if ( term in cacheCN ) {
					response( cacheCN[ term ] );
					return;
				}
				request.nameFilter = "commonNames";
				lastXhrCN = $.getJSON( "${createLink(controller:'recommendation', action: 'suggest')}", request, function( data, status, xhr ) {
					cacheCN[ term ] = data;
					if ( xhr === lastXhrCN ) {
						response( data );
					}
				});
			},focus: function( event, ui ) {
				$( "#commonName" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$("#commonNameSuggestions li a").css('border', 0);
				return false;
			},select: function( event, ui ) {
				$( "#commonName" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#canName" ).val( ui.item.value );
				$( "#name" ).val( ui.item.value );
				if(ui.item.languageName !== null){
					$("#languageComboBox").val(ui.item.languageName).attr("selected",true);
					$("#languageComboBox").data('combobox').refresh();
				}
				return false;
			},open: function(event, ui) {
				$("#commonNameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			ul.removeClass().addClass("dropdown-menu")
			if(item.category == "General") {
				return $( "<li class='span3'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${resource(dir:'images',file:'no-image.jpg', absolute:true)}"
                                            //${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}
								}
								return $("<li class='span3'></li>")
										.data("item.autocomplete", item)
										.append("<a title='"+ item.label.replace(/<.*?>/g, "")+ "'><img src='"+ item.icon+ "' class='group_icon' style='float:left; background:url("+ item.icon+ " no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>"+ item.label+ ((item.desc) ? '<br>('+ item.desc+ ')': '')+ ((item.languageName) ? '<br>('+ item.languageName+ ')': '') + "</a>")
										.appendTo(ul);
							}
						};

						$("#name")
								.keypress(
										function() {
											//console.log("=== saved val " + $("#mappedRecoNameForcanName").val() + "  newVal  " + $("#name").val()); 
											if ($("#mappedRecoNameForcanName")
													.val() !== $("#name").val()) {
												$("#canName").val('');
											}
										});

					});
</r:script>

<g:hasErrors bean="${recommendationVoteInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${observationInstance}" as="list" />
	</div>
</g:hasErrors>

<div class="row control-group " style="margin-top:5px;">
	<label for="recommendationVote" class="control-label"> <g:message
			code="observation.recommendationVote.label" default="Scientific name" />
	</label>
	<div class="controls">
		<div class="textbox nameContainer">

	<%
		def species_sn_name = ""
		def species_cn_name = ""
		def species_call_comment = null
		
		//showing vote added by creator of the observation
		if(params.action == 'edit' || params.action == 'update'){
			def tmp_reco_vote = observationInstance?.fetchOwnerRecoVote()
			def tmp_cn_reco	= tmp_reco_vote?.commonNameReco
			
			species_call_comment =  tmp_reco_vote?.comment
			species_cn_name = (tmp_cn_reco)? tmp_cn_reco.name : ""
			
			if(tmp_reco_vote && tmp_reco_vote.recommendation.isScientificName){
				species_sn_name = tmp_reco_vote.recommendation.name
			}
		}
	%>
	<g:set var="species_sn_lang"
	value="${species_sn_lang}" />
			<input type="text" name="recoName" id="name" value="${species_sn_name}"
				placeholder='Suggest a scientific name'
				class="input-xlarge ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
			<input type="hidden" name="canName" id="canName" />
			<div id="nameSuggestions" style="display: block;"></div>

		</div>
	</div>
</div>



<div class="row control-group ">
	<label for="recommendationVote" class="control-label"> <g:message
			code="observation.recommendationVote.label" default="Common name" />
	</label>
	<div class="controls">
		<div class="nameContainer textbox" style="position:relative;">
			
			<input type="text" name="commonName" id="commonName"
				value="${species_cn_name}" placeholder='Suggest a common name'
				class="input-xlarge ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
			<input type="hidden" id="mappedRecoNameForcanName" />
			
			<div style="width:90px;">
			<s:chooseLanguage />
			</div>
			<div id="commonNameSuggestions" style="display: block;"></div>

		</div>
	</div>
</div>


<div class="row control-group ">
	<label for="recommendationVote" class="control-label"> <g:message
			code="observation.recommendationVote.label" default="Comment" />
	</label>
	<div class="controls">
		<div class="nameContainer textbox">

			<input type="text" name="recoComment" id="recoComment" value="${species_call_comment}"
				class="input-xlarge ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}"
				placeholder="Write comment on species call"></input>
		</div>
	</div>
</div>



<r:script>
	$(document).ready(function() {
		//$('#recoComment').val('');
		$('#reco-action').click(function() {
			$('#reco-options').show();
			$('#reco-action').hide();
		});
	});

	function cancelRecoComment() {
		$('#recoComment').val('');
		$('#reco-options').hide();
		$('#reco-action').show();
	}
</r:script>
<style>
#reco-options {
	background-clip: padding-box;
	background-color: #FFFFFF;
	border-color: rgba(0, 0, 0, 0.2);
	border-radius: 0 0 5px 5px;
	border-style: solid;
	border-width: 1px;
	box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
	display: none;
	float: left;
	left: 0;
	list-style: none outside none;
	margin: 0;
	min-width: 400px;
	max-width: 400px;
	width: 400px;
	padding: 10px;
	top: 100%;
	z-index: 1000;
	color: #000000;
}

#reco-close {
	position: absolute;
	top: 0;
	right: 0;
}

.nameContainer {
	position:relative;
}

#nameSuggestions {
	width:290px;
}
#commonName {
	width:200px;
}

</style>
