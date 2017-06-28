package repo.web.application.security.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Lazy
@EnableWebSecurity
public class SpringSecurityWebConfiguration extends WebSecurityConfigurerAdapter {
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
			.antMatchers("/images/**", "/img/**", "/css*/**", "/js*/**", "/assets*/**", "/wicket/resource/**/*.js",
				"/wicket/resource/**/*.css", "/wicket/resource/**/*.png", "/wicket/resource/**/*.jpg",
				"/wicket/resource/**/*.gif", "/login/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
					.antMatchers("/resources/**", "/registration").permitAll()
					.anyRequest().authenticated()
					.and()
				.formLogin()
					.loginPage("/login")
					.permitAll()
					.and()
				.rememberMe()
					.and()
				.logout()
					.permitAll()
					.and()
				.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.NEVER)
					.and()
				.csrf()
					.disable();
		
//		http
//			.formLogin()
//			.loginPage("/login")
//			.and()
//			.rememberMe()
//			.and()
//			.sessionManagement()
//			.sessionCreationPolicy(SessionCreationPolicy.NEVER)
//			.and()
//			.csrf()
//			.disable();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}
}
