package security

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User implements Serializable {
	private static final long serialVersionUID = 1

	String username
	String password
	boolean enabled = true
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false

	Set<SecurityRole> getAuthorities() {
		(UserSecurityRole.findAllByUser(this) as List<UserSecurityRole>)*.securityRole as Set<SecurityRole>
	}

	static constraints = {
		username 		title: 'User Name', example:"Bob",
						blank: false, unique: true, nullable: false
		password 		example:"p123", blank: false, password: true, nullable: false
		enabled 		description: 'Is user active', nullable: false
		accountExpired 	description: 'Has user account expired', nullable: false
		accountLocked 	description: 'User account has been locked', nullable: false
		passwordExpired description: 'Password has expired', nullable: false
	}

	static mapping = {
		password column: '`password`'
	}
}
