package com.homepaintings.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingFactory;
import com.homepaintings.painting.PaintingType;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import com.homepaintings.users.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PaintingFactory paintingFactory;
    @Autowired CartFactory cartFactory;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsersRepository usersRepository;
    @Autowired CartRepository cartRepository;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("카트에 물품 추가")
    void createPaintingForm() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Painting painting = paintingFactory.saveNewPainting("painting", PaintingType.LANDSCAPE, 5000, 50);
        CartForm cartForm = new CartForm();
        cartForm.setPaintingName(painting.getName());
        cartForm.setQuantity(10);
        String content = objectMapper.writeValueAsString(cartForm);

        mockMvc.perform(post("/cart/add")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        List<Cart> cartList = cartRepository.findByUserOrderByCreatedDateTimeDesc(user);
        assertFalse(cartList.isEmpty());
        Cart cart = cartList.get(0);
        assertEquals(cart.getPainting(), painting);
        assertEquals(cart.getQuantity(), 10);
        assertEquals(cart.getTotalPrice(), 10 * painting.getPrice());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("카트에 물품 추가 - 잘못된 입력값(없는 물품)")
    void createPaintingForm_with_wrong_value() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        CartForm cartForm = new CartForm();
        cartForm.setPaintingName("painting");
        cartForm.setQuantity(10);
        String content = objectMapper.writeValueAsString(cartForm);

        mockMvc.perform(post("/cart/add")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        List<Cart> cartList = cartRepository.findByUserOrderByCreatedDateTimeDesc(user);
        assertTrue(cartList.isEmpty());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("카트에 페이지가 성공적으로 뜨는지 확인")
    void viewCartList() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Painting painting1 = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        Painting painting2 = paintingFactory.saveNewPainting("painting2", PaintingType.FIGURE, 10000, 50);
        Painting painting3 = paintingFactory.saveNewPainting("painting3", PaintingType.INK, 20000, 50);
        Cart cart1 = cartFactory.saveNewCart(user, painting1, 1, painting1.getPrice() * 1);
        Cart cart2 = cartFactory.saveNewCart(user, painting2, 5, painting2.getPrice() * 5);
        Cart cart3 = cartFactory.saveNewCart(user, painting3, 30, painting3.getPrice() * 30);
        Cart cart4 = cartFactory.saveNewCart(user, painting1, 2, painting1.getPrice() * 2);

        Map<String, Object> model = mockMvc.perform(get("/cart/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/view-cart-list"))
                .andExpect(model().attributeExists("user", "cartList"))
                .andExpect(authenticated().withUsername(TEST_EMAIL)).andReturn().getModelAndView().getModel();

        List<Cart> carList = (List<Cart>) model.get("cartList");
        assertEquals(carList.get(0), cart4);
        assertEquals(carList.get(1), cart3);
        assertEquals(carList.get(2), cart2);
        assertEquals(carList.get(3), cart1);
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("카트에 물품 삭제")
    void removeCart() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Painting painting1 = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        Painting painting2 = paintingFactory.saveNewPainting("painting2", PaintingType.FIGURE, 10000, 50);
        Cart cart1 = cartFactory.saveNewCart(user, painting1, 1, painting1.getPrice() * 1);
        Cart cart2 = cartFactory.saveNewCart(user, painting2, 10, painting2.getPrice() * 10);
        Cart cart3 = cartFactory.saveNewCart(user, painting2, 10, painting2.getPrice() * 10);
        CartListForm cartListForm = new CartListForm();
        cartListForm.setCartIdList(new ArrayList<>(Arrays.asList(cart1.getId(), cart2.getId(), cart3.getId())));
        String content = objectMapper.writeValueAsString(cartListForm);

        mockMvc.perform(post("/cart/remove")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        List<Cart> cartList = cartRepository.findByUserOrderByCreatedDateTimeDesc(user);
        assertTrue(cartList.isEmpty());
    }

}