package repo.web.wicket.validator;

import java.io.Serializable;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class DatabaseValidator<T extends Serializable> extends StringValidator {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	@Autowired
	private SessionFactory sessionFactory;
	
	public static class UniqueInDatabaseValidator<T extends Serializable> extends DatabaseValidator<T> {
		private static final long serialVersionUID = 1L;

		private String property;
		private SessionFactory sessionFactory;
		private Class<T> domainClass;

		public UniqueInDatabaseValidator(Class<T> domainClass, String property) {
			this.property = property;
			this.domainClass = domainClass;
		}

		@Override
		public void validate(IValidatable<String> validatable) {
			SimpleExpression expression = Restrictions.eq(property, validatable.getValue());
			Object user = sessionFactory.getCurrentSession().createCriteria(domainClass).add(expression).uniqueResult();
			if(user != null) {
				validatable.error(new ValidationError(this, "findByProperty." + property));
			}
		}
	}
	
	public static <T extends Serializable> DatabaseValidator<T> findByProperty(Class<T> domainClass, String property){
		return new UniqueInDatabaseValidator<>(domainClass, property);
	}
}
