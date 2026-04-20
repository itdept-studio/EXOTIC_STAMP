package metro.ExoticStamp.modules.collection.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.collection.CollectionWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminQueryService;
import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.application.view.CollectionAdminStatsView;
import metro.ExoticStamp.modules.collection.presentation.controller.CollectionAdminController;
import metro.ExoticStamp.modules.collection.presentation.dto.CreateCampaignRequest;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionAdminController.class)
@Import({CollectionWebMvcTestSecurityConfig.class, GlobalExceptionHandler.class})
class CollectionAdminControllerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollectionAdminCommandService collectionAdminCommandService;

    @MockBean
    private CollectionAdminQueryService collectionAdminQueryService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthEntryPoint customAuthEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockBean
    private RoleQueryService roleQueryService;

    @Test
    @WithAnonymousUser
    void stats_forbiddenWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/admin/collections/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stats_ok() throws Exception {
        when(collectionAdminQueryService.getStats()).thenReturn(
                CollectionAdminStatsView.builder()
                        .totalStampsCollected(3L)
                        .stampsPerCampaign(List.of())
                        .build());
        mockMvc.perform(get("/api/v1/admin/collections/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStampsCollected").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    void stats_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/collections/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listCampaigns_ok() throws Exception {
        AdminCampaignView v = AdminCampaignView.builder()
                .id(UUID.randomUUID())
                .lineId(LINE_ID)
                .code("C1")
                .name("N")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .defaultCampaign(true)
                .build();
        when(collectionAdminQueryService.listCampaigns(0, 20))
                .thenReturn(PageResult.of(List.of(v), 1, 1, 0));
        mockMvc.perform(get("/api/v1/admin/collections/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("C1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCampaign_created() throws Exception {
        UUID id = UUID.randomUUID();
        AdminCampaignView view = AdminCampaignView.builder()
                .id(id)
                .lineId(LINE_ID)
                .code("NEW")
                .name("Name")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .defaultCampaign(false)
                .build();
        when(collectionAdminCommandService.createCampaign(any())).thenReturn(view);

        CreateCampaignRequest req = new CreateCampaignRequest();
        req.setLineId(LINE_ID);
        req.setCode("NEW");
        req.setName("Name");
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusDays(30));

        mockMvc.perform(post("/api/v1/admin/collections/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("NEW"));
        verify(collectionAdminCommandService).createCampaign(any());
    }
}
