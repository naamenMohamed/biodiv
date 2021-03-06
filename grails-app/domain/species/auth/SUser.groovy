package species.auth

import species.Resource;
import species.Resource.ResourceType;
import species.participation.Observation;
import species.participation.ObservationFlag;
import species.participation.RecommendationVote;
import species.participation.curation.UnCuratedVotes;
import species.utils.ImageType;

class SUser {

	transient springSecurityService

	def grailsApplication;
	def commentService;

	String username
	String name;
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	String email
	Date dateCreated = new Date();
	Date lastLoginDate = new Date();
	String profilePic
	String website;
	float timezone=0;//offset
	String aboutMe;
	String location;
	boolean sendNotification = true;
	boolean hideEmailId = true;
	boolean allowIdentifactionMail = true;
	
	static hasMany = [openIds: OpenID, flags:ObservationFlag, unCuratedVotes:UnCuratedVotes, observations:Observation, recoVotes:RecommendationVote]
	//static hasOne = [facebookUser:FacebookUser]
	
	static constraints = {
		username blank: false
		name blank: false
		password blank: false
		email email: true, blank: false, unique: true, nullable:false
		profilePic nullable:true
		website nullable:true
		timezone nullable:true
		aboutMe nullable:true
		location nullable:true
		
	}

	static mapping = {
		/*
		 * Just keep in mind that the UUIDHexGenerator is not generating globally unique identifiers, 
		 * as Java can only acquire the IP address of the machine it’s running on 
		 * and not the MAC address of the network interface. 
		 * Also you have to be careful not to run into any conditions where the external system 
		 * could create the same IDs that you generate internally.
		 */
		id generator:"species.utils.PrefillableUUIDHexGenerator"
		password column: '`password`'
		aboutMe type:"text";
		autoTimestamp false;
	}

	Set<Role> getAuthorities() {
		SUserRole.findAllBySUser(this).collect { it.role } as Set
	}

	def beforeValidate() {
		if(this.email) {
			if(!this.name) {
				this.name = this.email.substring(0, this.email.indexOf('@'));
			}

			if(!this.username) {
				this.username = this.email.substring(0, this.email.indexOf('@'));
			}
		}
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}

	def icon() {
		return icon(ImageType.NORMAL);
	}
	
	def icon(ImageType type) {
		if(profilePic) {
			return profilePic;
		}
		
		def baseUrl = grailsApplication.config.speciesPortal.resources.serverURL;
		switch(type) {
			case ImageType.NORMAL : 
			case ImageType.LARGE : return baseUrl+"/users/user_large.png"
			case ImageType.SMALL : return baseUrl+"/users/user.png"
			case ImageType.VERY_SMALL : return baseUrl+"/users/user_small.png"
		}
	}

	@Override
	String toString() {
		return username;
	}
	
	def getWebsiteLink(){
		if(website && website.indexOf("://") == -1){
			return "http://" + website
		}else{
			return website;
		}
	}
	
	def fetchCommentCount(){
		return commentService.getCountByUser(this)
	}
	
}
