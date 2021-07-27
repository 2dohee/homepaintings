package com.homepaintings.painting;

import com.homepaintings.users.Authority;
import com.homepaintings.users.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
class PaintingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PaintingRepository paintingRepository;
    @Autowired PaintingFactory paintingFactory;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 추가 페이지가 성공적으로 뜨는지 확인")
    void createPaintingForm() throws Exception {
        mockMvc.perform(get("/admin/create-painting"))
                .andExpect(status().isOk())
                .andExpect(view().name("painting/admin/create-painting"))
                .andExpect(model().attributeExists("user", "paintingForm", "paintingType"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 추가")
    void createPainting() throws Exception {
        mockMvc.perform(post("/admin/create-painting")
                    .param("name", "테스트그림")
                    .param("type", PaintingType.FIGURE.toString())
                    .param("price", "50000")
                    .param("stock", "100")
                    .param("image", "")
                    .param("description", "설명")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view-paintings"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));

        assertTrue(paintingRepository.existsByName("테스트그림"));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 추가 - 잘못된 입력값1(상품명 중복)")
    void createPainting_with_wrong_value_1() throws Exception {
        paintingFactory.saveNewPainting("테스트그림", PaintingType.FIGURE, 50000, 100);
        mockMvc.perform(post("/admin/create-painting")
                    .param("name", "테스트그림")
                    .param("type", PaintingType.FIGURE.toString())
                    .param("price", "50000")
                    .param("stock", "100")
                    .param("image", "")
                    .param("description", "설명")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("painting/admin/create-painting"))
                .andExpect(model().attributeExists("user", "errorMessage", "paintingType"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 추가 - 잘못된 입력값2(존재하지 않는 타입)")
    void createPainting_with_wrong_value_2() throws Exception {
        mockMvc.perform(post("/admin/create-painting")
                    .param("name", "테스트그림")
                    .param("type", "없는 타입")
                    .param("price", "50000")
                    .param("stock", "100")
                    .param("image", "")
                    .param("description", "설명")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("painting/admin/create-painting"))
                .andExpect(model().attributeExists("user", "errorMessage", "paintingType"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));

        assertFalse(paintingRepository.existsByName("테스트그림"));
    }

    @Test
    @DisplayName("상품 조회 페이지가 성공적으로 뜨는지 확인")
    void viewPaintings() throws Exception {
        for (int i = 0; i < 500; i++) {
            PaintingType type;
            if (i % 2 == 0) type = PaintingType.FIGURE;
            else type = PaintingType.LANDSCAPE;
            paintingFactory.saveNewPainting("painting" + i, type, i*1000, 100);
        }

        Map<String, Object> model = mockMvc.perform(get("/view-paintings")
                    .param("page", "20") // 21 페이지
                    .param("type", "FIGURE")
                    .param("keywords", "painting")
                    .param("sorting", "가격")
                    .param("asc", "true"))
                    .andExpect(status().isOk())
                .andExpect(view().name("painting/view-paintings"))
                .andExpect(model().attributeExists("paintingType", "paintingList", "paintingSearchForm"))
                .andExpect(model().attribute("currentFirstPage", 20))
                .andExpect(model().attribute("currentLastPage", 20))
                .andExpect(model().attribute("currentOffset", 240)) // 20 * 12
                .andReturn().getModelAndView().getModel();

        Page<Painting> paintingList = (Page<Painting>) model.get("paintingList");
        assertEquals(paintingList.getNumber(), 20); // 마지막 페이지
        assertEquals(paintingList.getTotalPages(), 21); // 0 ~ 20
        assertEquals(paintingList.getTotalElements(), 250);
        assertEquals(paintingList.getNumberOfElements(), 10);
        for (int i = 0; i < paintingList.getNumberOfElements(); i++) {
            assertEquals(paintingList.getContent().get(i).getName(), "painting" + (480+i*2));
        }
    }

    @Test
    @DisplayName("상품 상세 페이지가 성공적으로 뜨는지 확인")
    void viewPainting() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 10000, 50);
        mockMvc.perform(get("/painting/" + painting.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("painting/details"))
                .andExpect(model().attribute("painting", painting));
    }

    @Test
    @DisplayName("없는 상품 상세 페이지에 접근")
    void viewPainting_no_value() throws Exception {
        mockMvc.perform(get("/painting/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 내용 수정 페이지가 성공적으로 뜨는지 확인")
    void updatePaintingForm() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        Map<String, Object> model = mockMvc.perform(get("/admin/update-painting/" + painting.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("painting/admin/update-painting"))
                .andExpect(model().attributeExists("user", "paintingForm", "paintingType"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))))
                .andReturn().getModelAndView().getModel();
        assertEquals(((PaintingForm) model.get("paintingForm")).getName(), painting.getName());
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 없는 상품 내용 수정 페이지에 접근")
    void updatePainting_no_value() throws Exception {
        mockMvc.perform(get("/admin/update-painting/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
        mockMvc.perform(post("/admin/update-painting/1")
                    .param("name", "1")
                    .param("type", PaintingType.FIGURE.toString())
                    .param("price", "1")
                    .param("stock", "1")
                    .param("image", "")
                    .param("description", "1")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 내용 수정")
    void updatePainting() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        mockMvc.perform(post("/admin/update-painting/" + painting.getId())
                    .param("name", painting.getName()) // 바꾸지 않더라도 중복 검사를 통과함
                    .param("type", PaintingType.FIGURE.toString())
                    .param("price", "50000")
                    .param("stock", "500")
                    .param("image", "")
                    .param("description", "설명")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/update-painting/" + painting.getId()))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
        assertEquals(painting.getName(), painting.getName());
        assertEquals(painting.getType(), PaintingType.FIGURE);
        assertEquals(painting.getPrice(), 50000);
        assertEquals(painting.getStock(), 500);
        assertEquals(painting.getDescription(), "설명");
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 내용 수정 - 잘못된 입력값(상품명 중복)")
    void updatePainting_with_wrong_value() throws Exception {
        Painting painting1 = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        Painting painting2 = paintingFactory.saveNewPainting("painting2", PaintingType.LANDSCAPE, 5000, 50);
        Map<String, Object> model = mockMvc.perform(post("/admin/update-painting/" + painting1.getId())
                .param("name", painting2.getName()) // 중복
                .param("type", PaintingType.FIGURE.toString())
                .param("price", "50000")
                .param("stock", "500")
                .param("image", "")
                .param("description", "설명")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("painting/admin/update-painting"))
                .andExpect(model().attributeExists("user", "errorMessage", "paintingType", "paintingForm"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))))
                .andReturn().getModelAndView().getModel();
        assertEquals(((PaintingForm)model.get("paintingForm")).getName(), painting1.getName()); // 바뀌지 않음
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 상품 삭제")
    void deletePainting() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting1", PaintingType.LANDSCAPE, 5000, 50);
        mockMvc.perform(post("/admin/delete-painting/" + painting.getId()).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/view-paintings"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
        assertTrue(paintingRepository.findById(painting.getId()).isEmpty()); // 삭제됨
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 - 삭제하려는 상품이 없는 경우")
    void deletePainting_no_value() throws Exception {
        mockMvc.perform(post("/admin/delete-painting/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))));
    }

}