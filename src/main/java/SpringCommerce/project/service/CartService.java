package SpringCommerce.project.service;

import SpringCommerce.project.dto.CartDTO;
import SpringCommerce.project.model.Cart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CartService {
    Optional<CartDTO> getCartDTOById(Long id);

    Optional<Cart> getCartById(Long id);

    boolean deleteCartById(Long id);

    Cart saveOrUpdateCart(Cart cart);

    List<CartDTO> getAllCarts();

    List<CartDTO> getCartByUserId(Long userId);
}
