package com.homepaintings.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homepaintings.cart.Cart;
import com.homepaintings.cart.CartFactory;
import com.homepaintings.cart.CartRepository;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingFactory;
import com.homepaintings.painting.PaintingType;
import com.homepaintings.users.Authority;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import com.homepaintings.users.WithUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PaintingFactory paintingFactory;
    @Autowired CartFactory cartFactory;
    @Autowired OrderFactory orderFactory;
    @Autowired UsersRepository usersRepository;
    @Autowired CartRepository cartRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDetailsRepository orderDetailsRepository;
    @Autowired ObjectMapper objectMapper;

    final String TEST_EMAIL = "test@email.com";
    List<Painting> paintingList = new ArrayList<>();
    List<Cart> cartList = new ArrayList<>();

    @BeforeEach
    @WithUser(TEST_EMAIL)
    void before() {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Painting painting1 = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 10000, 1000);
        Painting painting2 = paintingFactory.saveNewPainting("painting2", PaintingType.INK, 20000, 1000);
        Painting painting3 = paintingFactory.saveNewPainting("painting3", PaintingType.ANIMATION, 30000, 1000);
        Cart cart1 = cartFactory.saveNewCart(user, painting1, 10, 10 * painting1.getPrice());
        Cart cart2 = cartFactory.saveNewCart(user, painting2, 10, 10 * painting2.getPrice());
        Cart cart3 = cartFactory.saveNewCart(user, painting3, 10, 10 * painting3.getPrice());
        paintingList.addAll(List.of(painting1, painting2, painting3));
        cartList.addAll(List.of(cart1, cart2, cart3));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문화면이 성공적으로 뜨는지 확인")
    void createOrderForm() throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.addAll("cartIdList", cartList.stream().map(cart -> cart.getId().toString()).collect(Collectors.toList()));

        mockMvc.perform(get("/create-order")
                    .params(map))
                .andExpect(status().isOk())
                .andExpect(view().name("order/create-order"))
                .andExpect(model().attributeExists("user", "cartList", "orderForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문하기")
    void createOrder() throws Exception {
        OrderForm orderForm = OrderForm.builder()
                .cartIdList((ArrayList<Long>) cartList.stream().map(cart -> cart.getId()).collect(Collectors.toList()))
                .receiver("tester").phoneNumber("01011111111").address1("123-456").address2("xx시 xx동").address3("xx로 1길 1-1")
                .build();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.addAll("cartIdList", orderForm.getCartIdList().stream().map(id -> id.toString()).collect(Collectors.toList()));
        map.add("receiver", orderForm.getReceiver());
        map.add("phoneNumber", orderForm.getPhoneNumber());
        map.add("address1", orderForm.getAddress1());
        map.add("address2", orderForm.getAddress2());
        map.add("address3", orderForm.getAddress3());

        mockMvc.perform(post("/create-order")
                    .params(map)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        assertTrue(cartRepository.findByUserOrderByCreatedDateTimeDesc(user).isEmpty()); // 주문된 물품은 카트에서 제거
        Orders order = orderRepository.findByUserOrderByCreatedDateTimeDesc(user).get(0);
        assertEquals(order.getReceiver(), orderForm.getReceiver());
        assertEquals(order.getPhoneNumber(), orderForm.getPhoneNumber());
        assertEquals(order.getDeliveryStatus(), DeliveryStatus.READY);
        assertFalse(orderDetailsRepository.findByOrderId(order.getId()).isEmpty());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문하기 - 잘못된 입력값(형식에 맞지 않은 전화번호)")
    void createOrder_with_wrong_value() throws Exception {
        OrderForm orderForm = OrderForm.builder()
                .cartIdList((ArrayList<Long>) cartList.stream().map(cart -> cart.getId()).collect(Collectors.toList()))
                .receiver("tester").phoneNumber("12345").address1("123-456").address2("xx시 xx동").address3("xx로 1길 1-1")
                .build();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.addAll("cartIdList", orderForm.getCartIdList().stream().map(id -> id.toString()).collect(Collectors.toList()));
        map.add("receiver", orderForm.getReceiver());
        map.add("phoneNumber", orderForm.getPhoneNumber());
        map.add("address1", orderForm.getAddress1());
        map.add("address2", orderForm.getAddress2());
        map.add("address3", orderForm.getAddress3());

        mockMvc.perform(post("/create-order")
                    .params(map)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("order/create-order"))
                .andExpect(model().attributeExists("user", "cartList"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        assertFalse(cartRepository.findByUserOrderByCreatedDateTimeDesc(user).isEmpty());
        assertTrue(orderRepository.findByUserOrderByCreatedDateTimeDesc(user).isEmpty());
        assertTrue(orderDetailsRepository.findAll().isEmpty());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문목록화면이 성공적으로 뜨는지 확인")
    void viewOrderList() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        orderFactory.saveNewOrders(user, paintingList, 100);

        Map<String, Object> model = mockMvc.perform(get("/order/list")
                    .param("orderStatus", "UNCOMPLETED"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/view-order-list"))
                .andExpect(model().attributeExists("user", "orderStatus"))
                .andExpect(model().attribute("allOrderCount", 100)) // 0 ~ 99
                .andExpect(model().attribute("completedCount", 33)) // COMPLETED
                .andExpect(model().attribute("uncompletedCount", 67)) // READY + SHIPPING
                .andExpect(authenticated().withUsername(TEST_EMAIL)).andReturn().getModelAndView().getModel();

        List<Orders> orderList = (List<Orders>) model.get("orderList");
        for (int i = 0; i < orderList.size(); i++) {
            Orders order = orderList.get(i);
            DeliveryStatus deliveryStatus = order.getDeliveryStatus();
            assertTrue(deliveryStatus.equals(DeliveryStatus.READY) || deliveryStatus.equals(DeliveryStatus.SHIPPING));
        }
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 주문관리화면이 성공적으로 뜨는지 확인")
    void viewUsersOrderList() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        orderFactory.saveNewOrders(user, paintingList, 100);

        String keywords = "수령인 123-456";
        Map<String, Object> model = mockMvc.perform(get("/admin/order/list")
                    .param("page", "3") // 4 페이지
                    .param("deliveryStatus", "READY") // 34개
                    .param("keywords", keywords))
                .andExpect(status().isOk())
                .andExpect(view().name("order/admin/users-order-list"))
                .andExpect(model().attributeExists("user", "deliveryStatus", "keywords"))
                .andExpect(model().attribute("currentFirstPage", 0))
                .andExpect(model().attribute("currentLastPage", 3))
                .andExpect(model().attribute("currentOffset", 30)) // 3 * 10
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))))
                .andReturn().getModelAndView().getModel();

        Page<Orders> orderList = (Page<Orders>) model.get("orderList");
        assertEquals(orderList.getNumber(), 3);
        assertEquals(orderList.getTotalPages(), 4); // 0 ~ 3
        assertEquals(orderList.getTotalElements(), 34);
        assertEquals(orderList.getNumberOfElements(), 4);
        for (int i = 0; i < orderList.getNumberOfElements(); i++) {
            Orders order = orderList.getContent().get(i);
            assertEquals(order.getDeliveryStatus(), DeliveryStatus.READY);
            String[] str = keywords.split(" ");
            for (String s : str) {
                assertTrue(order.getReceiver().contains(s) || order.getAddress1().contains(s)
                        || order.getAddress2().contains(s) || order.getAddress3().contains(s));
            }
        }
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문상세화면이 성공적으로 뜨는지 확인")
    void getOrderDetails() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Orders order = orderFactory.saveNewOrders(user, paintingList, 1).get(0);

        String responseBody = mockMvc.perform(get("/order/" + order.getId() + "/details")
                .param("orderStatus", "UNCOMPLETED"))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        List<OrderDetailsForm> orderDetailsFormList = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDetailsForm.class));
        List<OrderDetails> byOrderId = orderDetailsRepository.findByOrderId(order.getId());
        for (int i = 0; i < orderDetailsFormList.size(); i++) {
             assertEquals(orderDetailsFormList.get(i).getName(), byOrderId.get(i).getPainting().getName());
             assertEquals(orderDetailsFormList.get(i).getQuantity(), byOrderId.get(i).getQuantity());
        }
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("배송 상태 변경")
    void changeDeliveryStatusToOrderList() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        List<Orders> ordersList = orderFactory.saveNewOrders(user, paintingList, 10);
        List<String> orderIdList = ordersList.stream().map(order -> order.getId().toString()).collect(Collectors.toList());
        List<String> deliveryStatusList = ordersList.stream().map(order -> DeliveryStatus.SHIPPING.toString()).collect(Collectors.toList());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.addAll("orderIdList", orderIdList);
        map.addAll("deliveryStatusList", deliveryStatusList);

        mockMvc.perform(post("/admin/order/list/change/delivery-status")
                    .params(map)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/order/list"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));

        List<Orders> savedOrderList = orderRepository.findByUserOrderByCreatedDateTimeDesc(user);
        savedOrderList.forEach(order -> assertEquals(order.getDeliveryStatus(), DeliveryStatus.SHIPPING)); // READY -> SHIPPING
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문 취소")
    void cancelOrder() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Orders order = orderFactory.saveNewOrders(user, paintingList, 1).get(0);

        mockMvc.perform(post("/order/" + order.getId() + "/cancel").with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertTrue(orderRepository.findById(order.getId()).isEmpty());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문 취소 - 삭제하려는 주문이 없는 경우")
    void cancelOrder_no_value() throws Exception {
        mockMvc.perform(post("/order/1/cancel").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("user"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("주문 취소 - 주문의 배달 상태가 READY 상태가 아닌 경우")
    void cancelOrder_not_READY() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Orders order = orderFactory.saveNewOrders(user, paintingList, 1).get(0);
        order.setDeliveryStatus(DeliveryStatus.SHIPPING);

        mockMvc.perform(post("/order/" + order.getId() + "/cancel").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("user"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

}