package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.StationCommandService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
@Import(MetroWebMvcTestSecurityConfig.class)
class StationControllerTest {
    private static final UUID STATION_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID STATION_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID STATION_5 = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StationQueryService stationQueryService;

    @MockBean
    private StationCommandService stationCommandService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RoleQueryService roleQueryService;

    @MockBean
    private CustomAuthEntryPoint authEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void resolveStationByNfc_public200() throws Exception {
        when(stationQueryService.resolveStationByNfc("NFC_1")).thenReturn(
                StationDetailResponse.builder()
                        .id(STATION_1)
                        .code("S1")
                        .name("Central")
                        .isActive(true)
                        .build());

        mockMvc.perform(get("/api/v1/stations/nfc/NFC_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(STATION_1.toString()))
                .andExpect(jsonPath("$.data.name").value("Central"));
    }

    @Test
    void resolveStationByQr_public200() throws Exception {
        when(stationQueryService.resolveStationByQr("QR_2")).thenReturn(
                StationDetailResponse.builder()
                        .id(STATION_2)
                        .code("S2")
                        .name("Airport")
                        .isActive(true)
                        .build());

        mockMvc.perform(get("/api/v1/stations/qr/QR_2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(STATION_2.toString()))
                .andExpect(jsonPath("$.data.name").value("Airport"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stationStats_ok() throws Exception {
        when(stationQueryService.stationStats()).thenReturn(List.of(
                StationStatsResponse.builder()
                        .stationId(STATION_1)
                        .stationName("A")
                        .lineName("L1")
                        .collectorCount(10)
                        .build()));

        mockMvc.perform(get("/api/v1/stations/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stationId").value(STATION_1.toString()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void stationStats_forbiddenWithoutAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/stations/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadStationImage_ok() throws Exception {
        when(stationCommandService.uploadStationImage(eq(STATION_5), any()))
                .thenReturn(new metro.ExoticStamp.modules.metro.presentation.dto.response.StationImageUploadResponse(
                        "http://localhost/uploads/x.jpg"));

        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[20]);

        mockMvc.perform(
                        multipart("/api/v1/stations/" + STATION_5 + "/image")
                                .file(file)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value("http://localhost/uploads/x.jpg"));
    }
}
