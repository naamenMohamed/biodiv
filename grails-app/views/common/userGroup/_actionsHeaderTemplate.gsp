
	<div>

		<div class="observation-icons pull-right">
			<sec:permitted className='species.groups.UserGroup'
								id='${userGroupInstance.id}'
								permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

								<a class="btn btn-large btn-primary "
									href="${createLink(action:'edit', id:userGroupInstance.id)}">
									<i
					class="icon-edit"></i>Edit Group </a>
								
			</sec:permitted>
							
			<sec:permitted className='species.groups.UserGroup'
				id='${userGroupInstance.id}'
				permission='${org.springframework.security.acls.domain.BasePermission.WRITE}'>

				<a id="inviteMembers" class="btn btn-large btn-primary" href="#"><i
					class="icon-envelope"></i> <g:message code="userGroup.members.label"
						default="Invite Members" /> </a>
			</sec:permitted>
			
			<div class="modal hide" id="inviteMembersDialog">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Invite friends as members</h3>
				</div>
				<div class="modal-body">
					<p>Send an invitation to invite your friends to join and
						contribute in this interesting group…</p>
					<div>
						<div id="invite_memberMsg">
							
						</div>
						<form id="inviteMembersForm" method="post"
							style="background-color: #F2F2F2;">
							<sUser:selectUsers model="['id':members_autofillUsersId]" />
							<input type="hidden" name="memberUserIds" id="memberUserIds" />
						</form>
					</div>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
						id="invite" class="btn btn-primary">Invite</a>
				</div>
			</div>



			<uGroup:isNotAMember model="['userGroupInstance':userGroupInstance]">

				<g:if test="${userGroupInstance.allowUsersToJoin}">
					<a class="btn btn-large btn-success" id="joinUs"> <i
						class="icon-plus"></i> Join Us</a>
				</g:if>
				<g:else>
					<a class="btn btn-large btn-success" id="requestMembership"> <i
						class="icon-plus"></i> Request Membership</a>
				</g:else>


			</uGroup:isNotAMember>
			<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
				<a class="btn btn-large btn-primary" id="leaveUs"><i
					class="icon-minus"></i>Leave this group</a>
				<div class="modal hide" id="leaveUsModalDialog">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">×</button>
						<h3>Do you want to leave this group???</h3>
					</div>
					<div class="modal-body">
						<p>
						We would like to know your feedback and any ideas on making this group a more interesting and a happening place.
						We are thankful for your wonderful contribution to this group and would like to hear from you soon.</p>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
							id="leave" class="btn btn-primary">Leave</a>
					</div>
				</div>
			</uGroup:isAMember>

		</div>
	</div>


<r:script>
function reloadActionsHeader() {
	$.ajax({
        	url: "${createLink(action:'actionsHeader',id:userGroupInstance.id) }",
        	 method: "POST",
            dataType: "html",
            success: function(data) {
            	$("#actionsHeader").html(data);
            }
    });
}

$(document).ready(function(){
	$("#joinUs").click(function() {
		if($("#joinUs").hasClass('disabled')) return false;
		$.ajax({
        	url: "${createLink(action:'joinUs',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$("#joinUs").html("Joined").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert-error').addClass('alert-success').html(data.msg);
            		document.location.reload(true);
            	} else {
            		//$("#joinUs").html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            		reloadActionsHeader();
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
	
	$("#requestMembership").click(function() {
		if($("#requestMembership").hasClass('disabled')) return false;
		$.ajax({
        	url: "${createLink(action:'requestMembership',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$("#requestMembership").html("Sent Request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            	} else {
            		//$("#requestMembership").html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            		reloadActionsHeader();
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert alert-success').addClass('alert alert-error');
				});
            }
		});
	})
	
	$("#leaveUs").click(function() {
		if($("#leaveUs").hasClass('disabled')) return false;
		$('#leaveUsModalDialog').modal('show');
	});
	
	$("#leave").click(function() {
		if($("#leave").hasClass('disabled')) return false;
		$.ajax({
        	url: "${createLink(action:'leaveUs',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$("#leaveUs").html("Thank You").removeClass("btn-info").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            		//reloadMembers();
            		document.location.reload(true)
            	} else {
            		$("#leaveUs").html("Couldn't Leave").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            	}
            	$('#leaveUsModalDialog').modal('hide');
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
	
	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	$("#invite").click(function(){
		$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#inviteMembersForm').ajaxSubmit({ 
			url:'${createLink(action:'inviteMembers',id:userGroupInstance.id)}',
			dataType: 'json', 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			
			success: function(data, statusText, xhr, form) {
				if(data.statusComplete) {
					$('#inviteMembersDialog').modal('hide');
					$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				} else {
					$("#invite_memberMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}				
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						
					});
           } 
     	});	
	});
     	
   	$('#inviteMembersDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
	$('#leaveUsModalDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
		
	$('#inviteMembers').click(function(){
			$.ajax({ 
	         	url:"${createLink(controller:'SUser', action:'isLoggedIn')}",
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#memberUserIds').val('');
						$('#inviteMembersDialog').modal('show');
						return false;
					}else{
						window.location.href = "${createLink(controller:'login')}?spring-security-redirect="+window.location.href;
					}
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	return false;
				} 
	     	});
	});
		
});
</r:script>