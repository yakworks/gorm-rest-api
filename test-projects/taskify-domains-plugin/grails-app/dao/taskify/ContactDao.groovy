package taskify

import gorm.tools.dao.DefaultGormDao
import grails.gorm.transactions.Transactional

@Transactional
class ContactDao extends DefaultGormDao<Contact> {
	@Override
	Contact create(Map params) {
		String name = params.remove("name")
		if (name) {
			def (fname, lname) = name.split()
			params.firstName = fname
			params.lastName = lname
		}
		super.create(params)
	}

	Contact inactivate(Long id) {
		Contact contact = Contact.get(id)
		contact.inactive = true
		contact.persist()
		contact
	}
}
