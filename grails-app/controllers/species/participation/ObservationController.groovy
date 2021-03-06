package species.participation

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;

import grails.plugins.springsecurity.Secured
import grails.util.Environment;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.ObservationFlag.FlagType
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.Habitat;
import species.BlockedMails;
import species.auth.SUser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

class ObservationController {

	private static final String OBSERVATION_ADDED = "observationAdded";
	private static final String SPECIES_RECOMMENDED = "speciesRecommended";
	private static final String SPECIES_AGREED_ON = "speciesAgreedOn";
	private static final String SPECIES_NEW_COMMENT = "speciesNewComment";
	private static final String SPECIES_REMOVE_COMMENT = "speciesRemoveComment";
	private static final String OBSERVATION_FLAGGED = "observationFlagged";
	private static final boolean COMMIT = true;
	private static final String OBSERVATION_DELETED = "observationDeleted";

	def grailsApplication;
	def observationService;
	def springSecurityService;
	def mailService;
	def observationsSearchService;
	def namesIndexerService;
	def SUserService;
	
	static allowedMethods = [update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def filteredList = {
		def result;
		//TODO: Dirty hack to feed results through solr if the request is from search
		if(params.action == 'search') {
			result = observationService.getObservationsFromSearch(params)
		} else {
			result = getObservationList(params);
		}
		render (template:"/common/observation/showObservationListTemplate", model:result);
	}

	def list = {
		log.debug params
		
		def model = getObservationList(params);
		if(params.loadMore?.toBoolean()){
			render(template:"/common/observation/showObservationListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, mapViewHtml:mapViewHtml]
			render result as JSON
			return;
		}
	}

	protected def getObservationList(params) {
		def max = Math.min(params.max ? params.int('max') : 9, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredObservation = observationService.getFilteredObservations(params, max, offset, false)
		def observationInstanceList = filteredObservation.observationInstanceList
		def queryParams = filteredObservation.queryParams
		def activeFilters = filteredObservation.activeFilters
		activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]
		
		def totalObservationInstanceList = observationService.getFilteredObservations(params, -1, -1, true).observationInstanceList
		def count = totalObservationInstanceList.size()
		
		//storing this filtered obvs ids list in session for next and prev links
		//http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/1.8.2/org/codehaus/groovy/runtime/DefaultGroovyMethods.java
		//returns an arraylist and invalidates prev listing result
		if(params.append?.toBoolean()) {
			session["obv_ids_list"].addAll(observationInstanceList.collect {it.id});
		} else {
			session["obv_ids_list_params"] = params.clone();
			session["obv_ids_list"] = observationInstanceList.collect {it.id};
		}
		
		log.debug "Storing all observations ids list in session ${session['obv_ids_list']} for params ${params}";
		return [totalObservationInstanceList:totalObservationInstanceList, observationInstanceList: observationInstanceList, observationInstanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}
	

	@Secured(['ROLE_USER'])
	def create = {
		def observationInstance = new Observation()
		observationInstance.properties = params;
		def author = springSecurityService.currentUser;
		def lastCreatedObv = Observation.find("from Observation as obv where obv.author=:author order by obv.createdOn desc ",[author:author]);
		return [observationInstance: observationInstance,lastCreatedObv:lastCreatedObv, 'springSecurityService':springSecurityService]
	}
	

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params;
		if(request.method == 'POST') {
			//TODO:edit also calls here...handle that wrt other domain objects

			params.author = springSecurityService.currentUser;
			def observationInstance;
			try {
				observationInstance =  observationService.createObservation(params);

				if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
					//flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					log.debug "Successfully created observation : "+observationInstance

					params.obvId = observationInstance.id

					def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();

					observationInstance.setTags(tags);

					sendNotificationMail(OBSERVATION_ADDED, observationInstance, request);
					params["createNew"] = true
					chain(action: 'addRecommendationVote', model:['chainedParams':params]);
				} else {
					observationInstance.errors.allErrors.each { log.error it }
					if(params["isMobileApp"]?.toBoolean()){
						render (['error:true']as JSON);
						return
					}else{
						render(view: "create", model: [observationInstance: observationInstance, lastCreatedObv:null])
					}
				}
			} catch(e) {
				e.printStackTrace();
				if(params["isMobileApp"]?.toBoolean()){
					render (['error:true']as JSON);
					return
				}else{
					flash.message = "${message(code: 'error')}";
					render(view: "create", model: [observationInstance: observationInstance, lastCreatedObv:null])
				}
			}
		} else {
			redirect(action: "create")
		}
	}

	@Secured(['ROLE_USER'])
	def update = {
		log.debug params;

		def observationInstance = Observation.get(params.id.toLong())
		params.author = observationInstance.author;
		if(observationInstance)	{
			try {
				observationService.updateObservation(params, observationInstance);

				if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					log.debug "Successfully updated observation : "+observationInstance

					params.obvId = observationInstance.id
					def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
					observationInstance.setTags(tags);

					//redirect(action: "show", id: observationInstance.id)
					params["createNew"] = true
					chain(action: 'addRecommendationVote', model:['chainedParams':params]);
				} else {
					observationInstance.errors.allErrors.each { log.error it }
					render(view: "create", model: [observationInstance: observationInstance])
				}
			} catch(e) {
				e.printStackTrace();
				flash.message = "${message(code: 'error')}";
				render(view: "create", model: [observationInstance: observationInstance])
			}
		}else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	def show = {
		if(params.id) {
			def observationInstance = Observation.findWhere(id:params.id.toLong(), isDeleted:false)
			if (!observationInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "list")
			}
			else {
				observationInstance.incrementPageVisit();
				if(params.pos) {
					int pos = params.int('pos');
					def prevNext = getPrevNextObservations(pos);
					if(prevNext) {
						[observationInstance: observationInstance, prevObservationId:prevNext.prevObservationId, nextObservationId:prevNext.nextObservationId, lastListParams:prevNext.lastListParams]
					} else {
						[observationInstance: observationInstance]
					}
				} else {
					[observationInstance: observationInstance]
				}
			}
		}
	}

	/**
	 * 
	 * @param pos
	 * @return
	 */
	private def getPrevNextObservations(int pos) {
		def lastListParams = session["obv_ids_list_params"]?.clone();
		if(lastListParams) {
		if(!session["obv_ids_list"]) {
			log.debug "Fetching observations list as its not present in session "
			runLastListQuery(lastListParams);
		}

		long noOfObvs = session["obv_ids_list"].size();
		
		log.debug "Current ids list in session ${session['obv_ids_list']} and position ${pos}";
		def nextObservationId = (pos+1 < session["obv_ids_list"].size()) ? session["obv_ids_list"][pos+1] : null;
		if(nextObservationId == null) {			
			lastListParams.put("append", true);
			def max = Math.min(lastListParams.max ? lastListParams.int('max') : 9, 100)
			def offset = lastListParams.offset ? lastListParams.int('offset') : 0
			lastListParams.offset = offset + session["obv_ids_list"].size();
			log.debug "Fetching new page of observations using params ${lastListParams}";
			runLastListQuery(lastListParams);
			lastListParams.offset = offset;
			nextObservationId = (pos+1 < session["obv_ids_list"].size()) ? session["obv_ids_list"][pos+1] : null;
		}
		def prevObservationId = pos > 0 ? session["obv_ids_list"][pos-1] : null;
		
		lastListParams.remove('isGalleryUpdate');
		lastListParams.remove("append");
		lastListParams['max'] = noOfObvs;
		lastListParams['offset'] = 0;
		return ['prevObservationId':prevObservationId, 'nextObservationId':nextObservationId, 'lastListParams':lastListParams];
		}
	}
	
	private void runLastListQuery(Map params) {
		log.debug params;
		if(params.action == 'search') {
			observationService.getObservationsFromSearch(params);
		} else {
			getObservationList(params);
		}
	}
	
	@Secured(['ROLE_USER'])
	def edit = {
		def observationInstance = Observation.findWhere(id:params.id.toLong(), isDeleted:false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
		else {
			render(view: "create", model: [observationInstance: observationInstance, 'springSecurityService':springSecurityService])
		}
	}

	@Secured(['ROLE_ADMIN'])
	def delete = {
		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			try {
				observationInstance.delete(flush: true)
				observationsSearchService.delete(observationInstance.id);
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def upload_resource = {
		log.debug params;

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				def rs = [:]
				Utils.populateHttpServletRequestParams(request, rs);
				def resourcesInfo = [];
				def rootDir = grailsApplication.config.speciesPortal.observations.rootDir
				File obvDir 
				def message;

				if(!params.resources) {
					message = g.message(code: 'no.file.attached', default:'No file is attached')
				}
				
				params.resources.each { f ->
					log.debug "Saving observation file ${f.originalFilename}"

					// List of OK mime-types
					//TODO Move to config
					def okcontents = [
						'image/png',
						'image/jpeg',
						'image/pjpeg',
						'image/gif',
						'image/jpg'
					]

					if (! okcontents.contains(f.contentType)) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							okcontents,
							f.originalFilename
						])
					}
					else if(f.size > grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE) {
						message = g.message(code: 'resource.file.invalid.max.message', args: [
							grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE/1024,
							f.originalFilename,
							f.size/1024
						], default:'File size cannot exceed ${104857600/1024}KB');
					}
					else if(f.empty) {
						message = g.message(code: 'file.empty.message', default:'File cannot be empty');
					}
					else {
						if(!obvDir) {
							if(!params.obvDir) {
								obvDir = new File(rootDir);
								if(!obvDir.exists()) {
									obvDir.mkdir();
								}
								obvDir = new File(obvDir, UUID.randomUUID().toString());
								obvDir.mkdir();
							} else {
								obvDir = new File(rootDir, params.obvDir);
								obvDir.mkdir();
							}
						}

						File file = observationService.getUniqueFile(obvDir, Utils.cleanFileName(f.originalFilename));
						f.transferTo( file );
						ImageUtils.createScaledImages(file, obvDir);
						resourcesInfo.add([fileName:file.name, size:f.size]);
					}
				}
				log.debug resourcesInfo
				// render some XML markup to the response
				if(obvDir && resourcesInfo) {
					render(contentType:"text/xml") {
						observations {
							dir(obvDir.absolutePath.replace(rootDir, ""))
							resources {
								for(r in resourcesInfo) {
									image('fileName':r.fileName, 'size':r.size){}
								}
							}
						}
					}
				} else {
					response.setStatus(500)
					message = [error:message]
					render message as JSON
				}
			} else {
				response.setStatus(500)
				def message = [error:g.message(code: 'no.file.attached', default:'No file is attached')]
				render message as JSON
			}
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			def message = [error:g.message(code: 'file.upload.fail', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addRecommendationVote = {

		if(chainModel?.chainedParams) {
			//need to change... dont pass on params
			chainModel.chainedParams.each {
				params[it.key] = it.value;
			}
			params.action = 'addRecommendationVote'
		}
		params.author = springSecurityService.currentUser;
		log.debug params;

		if(params.obvId) {
			//Saves recommendation if its not present
			def recVoteResult = getRecommendationVote(params)
			def recommendationVoteInstance = recVoteResult?.recVote;
			def recoVoteMsg = recVoteResult?.msg;
			
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if(!recommendationVoteInstance){
					//saving max voted species name for observation instance needed when observation created without species name
					//observationInstance.calculateMaxVotedSpeciesName();
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);

					if(!params["createNew"]){
						redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, recoVoteMsg:recoVoteMsg])
					}else if(params["isMobileApp"]?.toBoolean()){
						render (['success:true, obvId:$observationInstance.id']as JSON);
					}else{
						redirect(action: "show", id: observationInstance.id, params:[postToFB:(params.postToFB?:false)]);
					}
					return

				}else if(!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					observationService.addRecoComment(recommendationVoteInstance.recommendation, observationInstance, params.recoComment);
					observationInstance.lastRevised = new Date();
					//saving max voted species name for observation instance
					observationInstance.calculateMaxVotedSpeciesName();
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);

					if(!params["createNew"]){
						//sending mail to user
						sendNotificationMail(SPECIES_RECOMMENDED, observationInstance, request);
						redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, recoVoteMsg:recoVoteMsg])
					}else if(params["isMobileApp"]?.toBoolean()){
						render (['success:true, obvId:$observationInstance.id'] as JSON);
					}else{
						redirect(action: "show", id: observationInstance.id, params:[postToFB:(params.postToFB?:false)]);
					}
					return
				}
				else {
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
					recommendationVoteInstance.errors.allErrors.each { log.error it }
					render (view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance], params:[postToFB:(params.postToFB?:false)])
				}
			} catch(e) {
				e.printStackTrace()
				if(params["isMobileApp"]?.toBoolean()){
					render (['success:true, obvId:$observationInstance.id'] as JSON);
				}else{
					render(view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance], params:[postToFB:(params.postToFB?:false)])
				}
			}
		} else {
			flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
			log.error flash.message;
			redirect(action: "list")
		}
	}

	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addAgreeRecommendationVote = {
		log.debug params;

		params.author = springSecurityService.currentUser;

		if(params.obvId) {
			//Saves recommendation if its not present
			def recVoteResult = getRecommendationVote(params)
			def recommendationVoteInstance = recVoteResult?.recVote;
			def recoVoteMsg = recVoteResult?.msg;

			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if(!recommendationVoteInstance){
					def result = ['votes':params.int('currentVotes')];
					def r = [
						success : 'true',
						recoVoteMsg:recoVoteMsg]
					render r as JSON
					//redirect(action:getRecommendationVotes, id:params.obvId, params:[ max:3, offset:0, recoVoteMsg:recoVoteMsg])
					return
				}else if(recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					observationInstance.lastRevised = new Date();
					observationInstance.calculateMaxVotedSpeciesName();
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);

					//sending mail to user
					sendNotificationMail(SPECIES_AGREED_ON, observationInstance, request);
					def r = [
						success : 'true',
						recoVoteMsg:recoVoteMsg]
					render r as JSON
					//redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, recoVoteMsg:recoVoteMsg])
					return
				}
				else {
					recommendationVoteInstance.errors.allErrors.each { log.error it }
				}
			} catch(e) {
				e.printStackTrace();
			}
		} else {
			flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
		}
	}

	/**
	 * 
	 */
	def getRecommendationVotes = {
		log.debug params;
		params.max = params.max ? params.int('max') : 1
		params.offset = params.offset ? params.long('offset'): 0

		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			try {
				def results = observationInstance.getRecommendationVotes(params.max, params.offset);
				log.debug results;
				if(results?.recoVotes.size() > 0) {
					def html =  g.render(template:"/common/observation/showObservationRecosTemplate", model:['observationInstance':observationInstance, 'result':results.recoVotes, 'totalVotes':results.totalVotes, 'uniqueVotes':results.uniqueVotes]);
					def speciesNameHtml =  g.render(template:"/common/observation/showSpeciesNameTemplate", model:['observationInstance':observationInstance]);
					def result = [
								success : 'true',
								recoHtml:html,
								uniqueVotes:results.uniqueVotes,
								recoVoteMsg:params.recoVoteMsg,
								speciesNameTemplate:speciesNameHtml,
								speciesName:observationInstance.fetchSpeciesCall()]

					render result as JSON
					return
				} else {
					response.setStatus(500);
					def message = "";
					if(params.offset > 0) {
						message = [info: g.message(code: 'recommendations.nomore.message', default:'No more recommendations made. Please suggest')];
					} else {
						message = [info:g.message(code: 'recommendations.zero.message', default:'No recommendations made. Please suggest')];
					}
					render message as JSON
					return
				}
			} catch(e){
				e.printStackTrace();
				response.setStatus(500);
				def message = ['error' : g.message(code: 'error', default:'Error while processing the request.')];
				render message as JSON
			}
		}
		else {
			response.setStatus(500)
			def message = ['error':g.message(code: 'error', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	/**
	 * 
	 */
	def voteDetails = {
		log.debug params;
		def votes = RecommendationVote.findAll("from RecommendationVote as recoVote where recoVote.recommendation.id = :recoId and recoVote.observation.id = :obvId order by recoVote.votedOn desc", [recoId:params.long('recoId'), obvId:params.long('obvId')]);
		render (template:"/common/voteDetails", model:[votes:votes]);
	}

	/**
	 * 
	 */

	def listRelated = {
		log.debug params;
		def result = observationService.getRelatedObservations(params);

		def model = [observationInstanceList: result.relatedObv.observations.observation, obvTitleList:result.relatedObv.observations.title, observationInstanceTotal: result.relatedObv.count, queryParams: [max:result.max], activeFilters:new HashMap(params), parentObservation:Observation.read(params.long('id')), filterProperty:params.filterProperty, initialParams:new HashMap(params)]
		render (view:'listRelated', model:model)
	}

	/**
	 * 
	 */
	def getRelatedObservation = {
		log.debug params;
		def relatedObv = observationService.getRelatedObservations(params).relatedObv;

		if(relatedObv.observations) {
			relatedObv.observations = observationService.createUrlList2(relatedObv.observations);
		}
		render relatedObv as JSON
	}

	def tags = {
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

	@Secured(['ROLE_USER'])
	def flagDeleted = {
		log.debug params;
		//params.author = springSecurityService.currentUser;
		def observationInstance = Observation.get(id:params.id.toLong())
		if (observationInstance && SUserService.ifOwns(observationInstance.author)) {
			try {
				observationInstance.isDeleted = true;
				observationInstance.save(flush: true)
				sendNotificationMail(OBSERVATION_DELETED, observationInstance, request);
				observationsSearchService.delete(observationInstance.id);
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def flagObservation = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		def obv = Observation.get(params.id.toLong())
		FlagType flag = observationService.getObservationFlagType(params.obvFlag?:FlagType.OBV_INAPPROPRIATE.name());
		def observationFlagInstance = ObservationFlag.findByObservationAndAuthor(obv, params.author)
		if (!observationFlagInstance) {
			try {
				observationFlagInstance = new ObservationFlag(observation:obv, author: params.author, flag:flag, notes:params.notes)
				observationFlagInstance.save(flush: true)
				obv.flagCount++
				obv.save(flush:true)
				observationsSearchService.publishSearchIndex(obv, COMMIT);
				sendNotificationMail(OBSERVATION_FLAGGED, obv, request)
				flash.message = "${message(code: 'observation.flag.added', default: 'Observation flag added')}"
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'observation.flag.error', default: 'Error during addition of flag')}"
			}
		}
		else {
			flash.message  = "${message(code: 'observation.flag.duplicate', default:'Already flagged')}"
		}
		redirect(action: "show", id: params.id)
	}

	@Secured(['ROLE_USER'])
	def deleteObvFlag = {
		log.debug params;
		def obvFlag = ObservationFlag.get(params.id.toLong());
		def obv = Observation.get(params.obvId.toLong());

		if(!obvFlag){
			//response.setStatus(500);
			//def message = [info: g.message(code: 'observation.flag.alreadytDeleted', default:'Flag already deleted')];
			render obv.flagCount;
			return
		}
		try {
			obvFlag.delete(flush: true);
			obv.flagCount--;
			obv.save(flush:true)
			observationsSearchService.publishSearchIndex(obv, COMMIT);
			render obv.flagCount;
			return;
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			def message = [error: g.message(code: 'observation.flag.error.onDelete', default:'Error on deleting observation flag')];
			render message as JSON
		}
	}
	
	@Secured(['ROLE_USER'])
	def deleteRecoVoteComment = {
		log.debug params;
		def recoVote = RecommendationVote.get(params.id.toLong());
		recoVote.comment = null;
		try {
			recoVote.save(flush:true)
			def msg =  [success:g.message(code: 'observation.recoVoteComment.success.onDelete', default:'Comment deleted successfully')]
			render msg as JSON
			return;
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			def message = [error: g.message(code: 'observation.recoVoteComment.error.onDelete', default:'Error on deleting recommendation vote comment')];
			render message as JSON
		}
	}

	def snippet = {
		def observationInstance = Observation.get(params.id)

		render (template:"/common/observation/showObservationSnippetTabletTemplate", model:[observationInstance:observationInstance]);
	}

	private sendNotificationMail(String notificationType, Observation obv, request){
		def conf = SpringSecurityUtils.securityConfig
		def obvUrl = generateLink("observation", "show", ["id": obv.id], request)
		def userProfileUrl = generateLink("SUser", "show", ["id": obv.author.id], request)

		def templateMap = [username: obv.author.name.capitalize(), obvUrl:obvUrl, userProfileUrl:userProfileUrl, domain:Utils.getDomainName(request)]

		def mailSubject = ""
		def body = ""

		switch ( notificationType ) {
			case OBSERVATION_ADDED:
				mailSubject = conf.ui.addObservation.emailSubject
				body = conf.ui.addObservation.emailBody
				break

			case OBSERVATION_FLAGGED :
				mailSubject = "Observation flagged"
				body = conf.ui.observationFlagged.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				break

			case OBSERVATION_DELETED :
				mailSubject = conf.ui.observationDeleted.emailSubject
				body = conf.ui.observationDeleted.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				break

			case SPECIES_RECOMMENDED :
				mailSubject = "Species name suggested"
				body = conf.ui.addRecommendationVote.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				templateMap["currentActivity"] = "recommended a species name"
				break

			case SPECIES_AGREED_ON:
				mailSubject = "Species name suggested"
				body = conf.ui.addRecommendationVote.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				templateMap["currentActivity"] = "agreed on a species suggested"
				break

			case SPECIES_NEW_COMMENT:
				mailSubject = conf.ui.newComment.emailSubject
				body = conf.ui.newComment.emailBody
				break;
			case SPECIES_REMOVE_COMMENT:
				mailSubject = conf.ui.removeComment.emailSubject
				body = conf.ui.removeComment.emailBody
				break;

			default:
				log.debug "invalid notification type"
		}

		if (body.contains('$')) {
			body = evaluate(body, templateMap)
		}

		if ( Environment.getCurrent().getName().equalsIgnoreCase("pamba")) {
			if(obv.author.sendNotification){
				mailService.sendMail {
					to obv.author.email
					bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com"
					from conf.ui.notification.emailFrom
					subject mailSubject
					html body.toString()
				}
			}else{
				//sending mail only to website manager
				mailService.sendMail {
					bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com"
					from conf.ui.notification.emailFrom
					subject mailSubject
					html body.toString()
				}
			}
		} else {
			if(obv.author.sendNotification){
				mailService.sendMail {
					to obv.author.email
					from conf.ui.notification.emailFrom
					subject mailSubject
					html body.toString()
				}
			}
		}
	}

	private String generateLink( String controller, String action, linkParams, request) {
		createLink(base: Utils.getDomainServerUrl(request),
				controller:controller, action: action,
				params: linkParams)
	}

	private String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}


	def unsubscribeToIdentificationMail = {
		log.debug "$params"
		if(params.userId){
			def user = SUser.get(params.userId.toLong())
			user.allowIdentifactionMail = false;
			if(!user.save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
		}
		BlockedMails bm = new BlockedMails(email:params.email);
		if(!bm.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
		render "${message(code: 'user.unsubscribe.identificationMail', args: [params.email])}"
	}


	/*
	 * @param params
	 * @return
	 */
	private Map getRecommendationVote(params) {
		def observation = params.observation?:Observation.get(params.obvId);
		def author = params.author;
		
		def reco, commonNameReco;
		if(params.recoId)
			reco = Recommendation.get(params.long('recoId'));
		else{
			def recoResultMap = observationService.getRecommendation(params);
			reco = recoResultMap.mainReco;
			commonNameReco =  recoResultMap.commonNameReco;
		}
		ConfidenceType confidence = observationService.getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());

		RecommendationVote existingRecVote = RecommendationVote.findByAuthorAndObservation(author, observation);
		RecommendationVote newRecVote = new RecommendationVote(observation:observation, recommendation:reco, commonNameReco:commonNameReco, author:author, confidence:confidence);

		if(!reco){
			log.debug "Not a valid recommendation"
			return null
		}else{
			if(!existingRecVote){
				log.debug " Adding (first time) recommendation vote for user " + author.id +  " reco name " + reco.name
				def msg = "${message(code: 'recommendations.added.message', args: [reco.name])}"
				return [recVote:newRecVote, msg:msg]
			}else{
				if(existingRecVote.recommendation.id == reco.id){
					log.debug " Same recommendation already made by user " + author.id +  " reco name " + reco.name + " leaving as it is"
					def msg = "${message(code: 'reco.vote.duplicate.message', args: [reco.name])}"
					if(existingRecVote.commonNameReco != commonNameReco){
						log.debug "Updating recoVote as common name changed"
						existingRecVote.commonNameReco = commonNameReco;
						if(!existingRecVote.save(flush:true)){
							existingRecVote.errors.allErrors.each { log.error it }
						}
						observationService.addRecoComment(existingRecVote.recommendation, observation, params.recoComment);
					}
					return [recVote:null, msg:msg]
				}else{
					log.debug " Overwriting old recommendation vote for user " + author.id +  " new reco name " + reco.name + " old reco name " + existingRecVote.recommendation.name
					def msg = "${message(code: 'recommendations.overwrite.message', args: [existingRecVote.recommendation.name, reco.name])}"
					try{
						existingRecVote.delete(flush: true,, failOnError:true)
					}catch (Exception e) {
						e.printStackTrace();
					}
					return [recVote:newRecVote, msg:msg]
				}
			}
		}
	}

	/**
	 * Count   
	 */
	def count = {
		render Observation.count();
	}

	/**
	 * 
	 */
	def newComment = {
		log.debug params;
		if(!params.obvId) {
			log.error  "No Observation selected"
			response.setStatus(500)
			render (['error':"Coudn't find the specified observation with id $params.obvId"] as JSON);
		}
		def observationInstance = Observation.read(params.long('obvId'));
		if(observationInstance) {
			observationInstance.updateObservationTimeStamp();
			//observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
			sendNotificationMail(SPECIES_NEW_COMMENT, observationInstance, request);
			render (['success:true']as JSON);
		} else {
			response.setStatus(500)
			render (['error':"Coudn't find the specified observation with id $params.obvId"] as JSON);
		}
	}

	/**
	 *
	 */
	def removeComment = {
		log.debug params;
		if(!params.obvId) {
			log.error "No Observation selected"
			response.setStatus(500)
			render (['error':"Coudn't find the specified observation with id $params.obvId"] as JSON);
		}

		def observationInstance = Observation.read(params.long('obvId'));
		if(observationInstance) {

			observationInstance.updateObservationTimeStamp();
			//observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
			sendNotificationMail(SPECIES_REMOVE_COMMENT, observationInstance, request);
			render (['success:true']as JSON);
		} else {
			response.setStatus(500)
			render (['error':"Coudn't find the specified observation with id $params.obvId"] as JSON);
		}
	}
	@Secured(['ROLE_USER'])
	def sendIdentificationMail = {
		log.debug params;
		def currentUserMailId = springSecurityService.currentUser?.email;
		Map emailList = getUnBlockedMailList(params.userIdsAndEmailIds, request);
		if(emailList.isEmpty()){
			log.debug "No valid email specified for identification."
		}else if (Environment.getCurrent().getName().equalsIgnoreCase("pamba") || Environment.getCurrent().getName().equalsIgnoreCase("saturn")) {
			def mailSubject = params.mailSubject
			for(entry in emailList.entrySet()){
				def body = observationService.getIdentificationEmailInfo(params, request, entry.getValue()).mailBody
				mailService.sendMail {
					to entry.getKey()
					bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com"
					from currentUserMailId
					subject mailSubject
					html body.toString()
				}
				log.debug " mail sent for identification "
			}
		}
		render (['success:true']as JSON);
	}

	private Map getUnBlockedMailList(String userIdsAndEmailIds, request){
		Map result = new HashMap();
		userIdsAndEmailIds.split(",").each{
			String candidateEmail = it.trim();
			if(candidateEmail.isNumber()){
				SUser user = SUser.get(candidateEmail.toLong());
				candidateEmail = user.email.trim();
				if(user.allowIdentifactionMail){
					result[candidateEmail] = generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail, userId:user.id], request) ;
				}else{
					log.debug "User $user.id has unsubscribed for identification mail."
				}
			}else{
				if(BlockedMails.findByEmail(candidateEmail)){
					log.debug "Email $candidateEmail is unsubscribed for identification mail."
				}else{
					result[candidateEmail] = generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail], request) ;
				}
			}
		}
		return result;
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search = {
		log.debug params;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		def model = observationService.getObservationsFromSearch(params);
		
		if(params.append) {
			session["obv_ids_list"].addAll(model.totalObservationIdList);
		} else {
			session["obv_ids_list_params"] = params.clone();
			session["obv_ids_list"] = model.totalObservationIdList;
		}
		model.remove('totalObservationIdList');
		model['isSearch'] = true;
		log.debug "Storing all observations ids list in session ${session['obv_ids_list']}";
		
		if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
		} else {
			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def filteredTags = model.tags;
			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, mapViewHtml:mapViewHtml]
			render result as JSON
		}
	}


	/**
	 *
	 */
	def advSearch = {
		log.debug params;
		String query  = "";
		def newParams = [:]
		for(field in params) {
			if(!(field.key ==~ /action|controller|sort|fl|start|rows/) && field.value ) {
				if(field.key.equalsIgnoreCase('name')) {
					newParams[field.key] = field.value;
					query = query + " " +field.value;
				} else {
					newParams[field.key] = field.value;
					query = query + " " + field.key + ': "'+field.value+'"';
				}
			}
		}
		if(query) {
			newParams['query'] = query;
			redirect (action:"search", params:newParams);
		}
		render (view:'advSearch', params:newParams);
	}

	/**
	 * 
	 */

	def nameTerms = {
		log.debug params;
		params.field = params.field?:"autocomplete";
		List result = new ArrayList();

		params.max = Math.min(params.max ? params.int('max') : 5, 10)
		def namesLookupResults = namesIndexerService.suggest(params)
		result.addAll(namesLookupResults);

		def queryResponse = observationsSearchService.terms(params);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];

		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Observations"]);
		}

		render result as JSON;
	}

	/**
	 *
	 */
	def terms = {
		log.debug params;
		params.field = params.field?:"autocomplete";
		List result = new ArrayList();
		def queryResponse = observationsSearchService.terms(params);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];

		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":""]);
		}

		render result as JSON;
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH END /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	def getFilteredLanguage = {
		render species.Language.filteredList() 
	}
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// json API ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	@Secured(['ROLE_USER'])
	def getObv = {
		log.debug params;
		render Observation.read(params.id.toLong()) as JSON
	} 
	
	@Secured(['ROLE_USER'])
	def getList = {
		log.debug params;
		render getObservationList(params) as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getHabitatList = {
		log.debug params;
		def res = new HashMap()
			Habitat.list().each {
			res[it.id] = it.name
		}
		render res as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getGroupList = {
		log.debug params;
		def res = new HashMap()
		SpeciesGroup.list().each {
			res[it.id] = it.name
		
		}
		render res as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getThumbObvImage = {
		log.debug params;
		def baseUrl = grailsApplication.config.speciesPortal.observations.serverURL
		def mainImage = Observation.read(params.id.toLong()).mainImage()
		def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null
		render baseUrl + imagePath 
	}
	
	@Secured(['ROLE_USER'])
	def getFullObvImage = {
		log.debug params;
		def baseUrl = grailsApplication.config.speciesPortal.observations.serverURL
		def mainImage = Observation.read(params.id.toLong()).mainImage()
		def gallImagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix):null
		render baseUrl + gallImagePath
	}
	
	
	@Secured(['ROLE_USER'])
	def getUserImage = {
		log.debug params;
		render SUser.read(params.id.toLong()).icon() 
		
	}
	
	@Secured(['ROLE_USER'])
	def getUserInfo = {
		log.debug params;
		def res = new HashMap()
		def u = SUser.read(params.id.toLong())
		res["id"] = u.id
		res["aboutMe"] = u.aboutMe
		res["dateCreated"] =  u.dateCreated
		res["email"] = u.email
		res["lastLoginDate"] = u.lastLoginDate
		res["location"] = u.location 
		res["name"] = u.name
		res["username"] = u.username
		res["website"] = u.website
		render res as JSON
	}		
}
