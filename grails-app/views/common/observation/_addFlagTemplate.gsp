<%@ page import="species.participation.ObservationFlag.FlagType"%>
<%@ page import="species.participation.Observation"%>
<%@page import="species.utils.ImageType"%>
<div>
	<div class="btn-group">
		<a id="flag-action" class="btn btn-mini" data-toggle="dropdown"
			href="#"> <g:if test="${observationInstance.flagCount>0}">
				<i class="icon-flag icon-red"></i>
			</g:if> <g:else>
				<i class="icon-flag"></i>
			</g:else> Flag
		</a>


		<div id="flag-options" class="popup-form" style="display: none">
			<h5>Why?</h5>
			<form id="flag-form"
				action="${createLink(controller:'observation', action:'flagObservation', id:observationInstance.id)}">
				<g:each in="${FlagType.list() }" var="flag" status="i">
					<g:if test="${i > 0}">
						<input type="radio" name="obvFlag" value="${flag}">
						${flag.value()}</input>
						<br />
					</g:if>
					<g:else>
						<input type="radio" name="obvFlag" value="${flag}" CHECKED>
						${flag.value()}</input>
						<br />
					</g:else>
				</g:each>

				<input type="text" name="notes" placeholder="Any other reason"></input><br />
				<input class="btn btn-danger" type="submit" value="Flag"></input>
				<div id="flag-close" class="popup-form-close" value="close">
					<i class="icon-remove"></i>
				</div>
			</form>

			<div id="flagged">
				<g:if test="${observationInstance.flagCount>0}">
					<span id="flagHeadings" style="font-weight: bold">Who flagged and why:</span>
				</g:if>
				<div>
					<g:each var="flagInstance" in="${observationInstance.fetchAllFlags()}">
						<li style="padding: 0 5px; clear: both;">
							<span class="flagInstanceClass">
							<g:link controller="SUser" action="show" id="${flagInstance.author?.id}">
							<img class="very_small_profile_pic"
								src="${flagInstance.author?.icon(ImageType.VERY_SMALL)}"
								title="${flagInstance.author.name}"/></g:link> : ${flagInstance.flag.value()} ${flagInstance.notes ? ": " + flagInstance.notes : ""}</span>
							<sUser:ifOwns model="['user':flagInstance.author]">
								<a href="#" onclick="removeFlag(${flagInstance.id}, ${flagInstance.observation.id}, $(this).parent()); return false;"><span class="deleteFlagIcon" data-original-title="Remove this flag" ><i class="icon-trash"></i></span></a>
							</sUser:ifOwns>
							
						</li>
					</g:each>
				</div>
			</div>
			<div id="flagMessage">
			</div>

		</div>
	</div>
</div>

<r:script>
$(document).ready(function(){
	$(".deleteFlagIcon").tooltip({"placement":"right"});
});

$('#flag-action').click(function(){
	$('#flag-options').show();
});
$('#flag-form').submit(function(){
	$('#flag-options').hide();
	
});
$('#flag-close').click(function(){
	$('#flag-options').hide();
});

function removeFlag(flagId, obvId, flagComponent){ 
	$.ajax({
		url: "${createLink(controller:'observation', action:'deleteObvFlag')}",
		data:{"id":flagId, "obvId":obvId},
		
		success: function(data){
			if(parseInt(data) == 0){
				$("#flagHeadings").hide();
				$("#flag-action>i").removeClass("icon-red");
			}
			$(".deleteFlagIcon").tooltip('hide');
			flagComponent.remove();
			$("#flagMessage").html('');
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			$(".deleteFlagIcon").tooltip('hide');
			var msg = $.parseJSON(xhr.responseText);
			$("#flagMessage").html(msg["error"]).show().removeClass().addClass('alert alert-error');
		}
	});
}

</r:script>

<style>

#flagged {
	clear: both;
	padding-top: 10px;
}
</style>
