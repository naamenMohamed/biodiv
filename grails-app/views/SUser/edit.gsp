<html>
<%@ page import="org.codehaus.groovy.grails.plugins.PluginManagerHolder"%>

<sec:ifNotSwitched>
	<sec:ifAllGranted roles='ROLE_SWITCH_USER'>
		<g:if test='${user.username}'>
			<g:set var='canRunAs' value='${true}' />
		</g:if>
	</sec:ifAllGranted>
</sec:ifNotSwitched>

<head>
<meta name='layout' content='main' />
<r:require modules="observations_show" />

<g:set var="entityName"
	value="${message(code: 'user.label', default: 'User')}" />
<title><g:message code="default.edit.label" args="[entityName]" />
</title>

<style>
.form-horizontal .control-label {
	width: 90px;
}

.form-horizontal .controls {
	margin-left: 110px;
}
</style>
</head>

<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class=" user span12">
				<div class="page-header">

					<h1>
						${fieldValue(bean: user, field: "name")}

						<span style="font-size: 60%; float: right;"
							class="btn btn-primary"> <g:link controller="SUser"
								action="show" id="${user.id}">View my profile
							</g:link> </span>


					</h1>

				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<g:hasErrors bean="${user}">
					<i class="icon-warning-sign"></i>
					<span class="label label-important"> <g:message
							code="fix.errors.before.proceeding" default="Fix errors" /> </span>
					<%--<g:renderErrors bean="${user}" as="list" />--%>
				</g:hasErrors>

				<g:form class="form-horizontal" action="update" name='userEditForm'>
					<g:hiddenField name="id" value="${user?.id}" />
					<g:hiddenField name="version" value="${user?.version}" />

					<div class="super-section" style="clear: both;">
						<div class="row section">
							<div class="figure span3"
								style="float: left; max-height: 220px; max-width: 200px">
								<g:link controller="SUser" action="show" id="${user.id }">
									<img class="normal_profile_pic" src="${user.icon()}" />
								</g:link>
								<div class="prop">
									<span class="name"><i class="icon-time"></i>Member since
									</span>
									<div class="value">
										<g:formatDate format="dd/MM/yyyy" date="${user.dateCreated}"
											type="datetime" style="MEDIUM" />
									</div>
								</div>
								<div class="prop">
									<span class="name"><i class="icon-time"></i>Last visited
									</span>
									<div class="value">
										<g:formatDate format="dd/MM/yyyy" date="${user.lastLoginDate}"
											type="datetime" style="MEDIUM" />
									</div>
								</div>
							</div>
							<div class="span8 observation_story">

								<div
									class="control-group ${hasErrors(bean: user, field: 'username', 'error')}">
									<label class="control-label" for="username"><i
										class="icon-user"></i> <g:message code="suser.username.label"
											default="Username" /> </label>
									<div class="controls">
										<input type="text" name="username" class="input-xlarge"
											id="username" value="${user.username}">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="username">
												<g:renderErrors bean="${user}" as="list" field="username" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'name', 'error')}">
									<label class="control-label" for="name"><i
										class="icon-user"></i> <g:message code="suser.name.label"
											default="Full Name" /> </label>
									<div class="controls">
										<input type="text" name="name" class="input-xlarge" id="name"
											value="${user.name}">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="name">
												<g:renderErrors bean="${user}" as="list" field="name" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'email', 'error')}">
									<label class="control-label" for="email"><i
										class="icon-envelope"></i> <g:message code="suser.email.label"
											default="Email *" /> </label>
									<div class="controls">
										<input type="text" name="email" class="input-xlarge disabled"
											id="email" value="${user.email}" disabled readonly>
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="email">
												<g:renderErrors bean="${user}" as="list" field="email" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'website', 'error')}">
									<label class="control-label" for="website"><i
										class="icon-road"></i> <g:message code="suser.website.label"
											default="Website" /> </label>
									<div class="controls">
										<input type="text" name="website" class="input-xlarge"
											id="website" value="${user.website ?: 'http://'}">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="website">
												<g:renderErrors bean="${user}" as="list" field="website" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'location', 'error')}">
									<label class="control-label" for="location"><i
										class="icon-map-marker"></i> <g:message
											code="suser.location.label" default="Location" /> </label>
									<div class="controls">
										<input type="text" name="location" class="input-xlarge"
											id="location" value="${user.location}" />
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="location">
												<g:renderErrors bean="${user}" as="list" field="location" />
											</g:hasErrors>
										</div>
									</div>
								</div>

							</div>
						</div>


						<div
							class="section control-group  ${hasErrors(bean: user, field: 'aboutMe', 'error')}"
							style="clear: both;">
							<h5>
								<i class="icon-user"></i>About Me
							</h5>
							<textarea cols='100' rows='3' style="width: 100%" name="aboutMe"
								id="aboutMe">
								${user.aboutMe }
							</textarea>
							<div class="help-inline">
								<g:hasErrors bean="${user}" field="aboutMe">
									<g:renderErrors bean="${user}" as="list" field="aboutMe" />
								</g:hasErrors>
							</div>
						</div>

						<div class="section" style="clear: both;">
							<h5>
								<i class="icon-cog"></i>Settings
							</h5>
							<div
								class="control-group ${hasErrors(bean: user, field: 'sendNotification', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="sendNotification" value="${user.sendNotification}" />
										<g:message code='user.sendNotification.label'
											default='Send me email notifications' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="sendNotification">
											<g:renderErrors bean="${user}" as="list"
												field="sendNotification" />
										</g:hasErrors>
									</div>
								</div>
							</div>
							<div
								class="control-group ${hasErrors(bean: user, field: 'allowIdentifactionMail', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="allowIdentifactionMail"
											value="${user.allowIdentifactionMail}" /> <g:message
											code='user.allowIdentifactionMail.label'
											default='Allow identification email' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="sendNotification">
											<g:renderErrors bean="${user}" as="list"
												field="sendNotification" />
										</g:hasErrors>
									</div>
								</div>
							</div>
							<div
								class="control-group ${hasErrors(bean: user, field: 'hideEmailId', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="hideEmailId" value="${user.hideEmailId}" /> <g:message
											code='user.hideEmailId.label'
											default='Hide my email from others' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="hideEmailId">
											<g:renderErrors bean="${user}" as="list" field="hideEmailId" />
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>




						<div class="section" style="clear: both; overflow: auto;">
							<obv:showAllTags
								model="['tagFilterByProperty':'User', 'tagFilterByPropertyValue':user.id, 'isAjaxLoad':false]" />
						</div>

						<sUser:isAdmin model="['user':user]">
							<div class="section" style="clear: both;">
								<h5>
									<i class="icon-cog"></i>
									<g:message code="default.edit.label" args="[entityName]" />
								</h5>
								<%
	def tabData = []
	tabData << [name: 'userinfo', icon: 'icon_user', messageCode: 'spring.security.ui.user.info']
	tabData << [name: 'roles',    icon: 'icon_role', messageCode: 'spring.security.ui.user.roles']
	%>

								<s2ui:tabs elementId='tabs' height='375' data="${tabData}">

									<s2ui:tab name='userinfo' height='275'>
										<table>
											<tbody>

												<s2ui:checkboxRow name='enabled'
													labelCode='user.enabled.label' bean="${user}"
													labelCodeDefault='Enabled' value="${user?.enabled}" />

												<s2ui:checkboxRow name='accountExpired'
													labelCode='user.accountExpired.label' bean="${user}"
													labelCodeDefault='Account Expired'
													value="${user?.accountExpired}" />

												<s2ui:checkboxRow name='accountLocked'
													labelCode='user.accountLocked.label' bean="${user}"
													labelCodeDefault='Account Locked'
													value="${user?.accountLocked}" />

												<s2ui:checkboxRow name='passwordExpired'
													labelCode='user.passwordExpired.label' bean="${user}"
													labelCodeDefault='Password Expired'
													value="${user?.passwordExpired}" />
											</tbody>
										</table>
									</s2ui:tab>

									<s2ui:tab name='roles' height='275'>
										<g:each var="entry" in="${roleMap}">
											<div>
												<g:checkBox name="${entry.key.authority}"
													value="${entry.value}" />
												<g:link controller='role' action='edit' id='${entry.key.id}'>
													${entry.key.authority.encodeAsHTML()}
												</g:link>
											</div>
										</g:each>
									</s2ui:tab>
								</s2ui:tabs>
							</div>
						</sUser:isAdmin>
						</div>
						
						<div class="section form-action"
							style='clear: both; margin-top: 20px; margin-bottom: 40px;'>
							<s2ui:submitButton elementId='update' form='userEditForm'
								messageCode='default.button.update.label'
								class="btn btn-primary" style="float: right; margin-right: 5px;" />

							<sUser:isAdmin model="['user':user]">
								<g:if test='${user}'>
									<a class="btn btn-danger" id="deleteButton"> ${message(code:'default.button.delete.label')}
									</a>
								</g:if>
							</sUser:isAdmin>

							<g:if test='${canRunAs}'>
								<a id="runAsButton"> ${message(code:'spring.security.ui.runas.submit')}
								</a>
							</g:if>

						</div>
				</g:form>

				<sUser:isAdmin model="['user':user]">
					<g:if test='${user}'>
						<s2ui:deleteButtonForm instanceId='${user.id}' />
					</g:if>
				</sUser:isAdmin>

				<g:if test='${canRunAs}'>
					<form name='runAsForm'
						action='${request.contextPath}/j_spring_security_switch_user'
						method='POST'>
						<g:hiddenField name='j_username' value="${user.username}" />
						<input type='submit' class='s2ui_hidden_button' />
					</form>
				</g:if>
			</div>
		</div>
	</div>



	<r:script>
		$(document).ready(function() {
			$('#username').focus();

			$("#runAsButton").button();
			$('#runAsButton').bind('click', function() {
				document.forms.runAsForm.submit();
			});

		});
	</r:script>

</body>
</html>
