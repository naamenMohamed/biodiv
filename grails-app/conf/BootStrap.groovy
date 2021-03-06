import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import species.Field;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.groups.SpeciesGroup;

class BootStrap {

	private static final log = LogFactory.getLog(this);
	
	def grailsApplication
	def setupService;
	def namesIndexerService
	def navigationService
	def springSecurityService

	/**
	 * 
	 */
	def init = { servletContext ->
		//grailsApplication.config.'grails.web.disable.multipart' = true
		initDefs();
		initUsers();
		//initGroups();
		initNames();
		initFilters();
	}

	def initDefs() {
		if(Field.count() == 0) {
			log.debug ("Initializing db.")
			setupService.setupDefs()
		}
	}
	
	/**
	 * 
	 * @return
	 */
	def initUsers() {
		createOrUpdateUser('admin@strandls.com', 'admin', true);
//		createOrUpdateUser('sravanthi', 'sra123', true);
//		createOrUpdateUser('janaki', 'janaki', false);
//		createOrUpdateUser('prabha', 'prabha', false);
//		createOrUpdateUser('rahool', 'rahool', false);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param isAdmin
	 */
	private void createOrUpdateUser(email, password, boolean isAdmin) {
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(flush:true, failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(flush:true, failOnError: true)
		def fbRole = Role.findByAuthority('ROLE_FACEBOOK') ?: new Role(authority: 'ROLE_FACEBOOK').save(flush:true, failOnError: true)
		def drupalAdminRole = Role.findByAuthority('ROLE_DRUPAL_ADMIN') ?: new Role(authority: 'ROLE_DRUPAL_ADMIN').save(flush:true, failOnError: true)

		def user = SUser.findByEmail(email) ?: new SUser(
				email: email,
				password: password,
				enabled: true).save(failOnError: true)

		if (!user.authorities.contains(userRole)) {
			SUserRole.create user, userRole
		}

		if(isAdmin) {
			if (!user.authorities.contains(adminRole)) {
				SUserRole.create user, adminRole
			}
		}
	}

	/**
	 * 	
	 * @return
	 */
	def initGroups() {
		def groups = SpeciesGroup.list();
		def subItems = [];
		def allGroup;
		groups.eachWithIndex { SpeciesGroup group, index ->
			subItems.add([controller:'speciesGroup', title:group.name, order:index, action:'show', params:[id:group.id], path:'show/'+group.id]);
			if(group.name.equalsIgnoreCase("All")) {
				allGroup = group;
			}
		}
		navigationService.registerItem('dashboard', [controller:'speciesGroup', order:30, title:'Groups', action:'list', path:(allGroup)?'show/'+allGroup.id:'list', subItems:subItems])
		navigationService.updated()
	}

	/**
	 * 
	 * @return
	 */
	def initNames() {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		namesIndexerService.load(indexStoreDir);
	}

	/**
	 * 
	 */
	def initFilters() {
		if(grailsApplication.config.checkin.drupal) {
			SpringSecurityUtils.clientRegisterFilter('drupalAuthCookieFilter', SecurityFilterPosition.CAS_FILTER.order + 1);
		}
	}
	
	/**
	 * 
	 */
	def destroy = {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		//namesIndexerService.store(indexStoreDir);
	}
}
