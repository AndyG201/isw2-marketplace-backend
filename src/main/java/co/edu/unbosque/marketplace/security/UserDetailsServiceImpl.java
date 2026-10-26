package co.edu.unbosque.marketplace.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	 private final UsuarioRepository usuarioRepository;

	    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
	        this.usuarioRepository = usuarioRepository;
	    }

	    @Override
	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	        return usuarioRepository.findByCorreoIgnoreCase(email)
	                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
	    }
}
