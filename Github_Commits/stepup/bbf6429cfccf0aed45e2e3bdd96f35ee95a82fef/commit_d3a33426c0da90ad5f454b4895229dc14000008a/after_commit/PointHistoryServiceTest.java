package com.pi.stepup.domain.rank.service;

import com.pi.stepup.domain.dance.constant.DanceType;
import com.pi.stepup.domain.dance.domain.RandomDance;
import com.pi.stepup.domain.rank.dao.PointHistoryRepository;
import com.pi.stepup.domain.rank.dao.PointPolicyRepository;
import com.pi.stepup.domain.rank.domain.PointHistory;
import com.pi.stepup.domain.rank.domain.PointPolicy;
import com.pi.stepup.domain.rank.dto.RankRequestDto.PointUpdateRequestDto;
import com.pi.stepup.domain.rank.dto.RankResponseDto.PointHistoryFindResponseDto;
import com.pi.stepup.domain.user.dao.UserRepository;
import com.pi.stepup.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pi.stepup.domain.rank.constant.PointType.FIRST_PRIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {
    @InjectMocks
    PointHistoryServiceImpl pointHistoryService;

    @Mock
    UserRepository userRepository;

    @Mock
    PointPolicyRepository pointPolicyRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    private PointHistory pointHistory;
    private User user;
    private PointPolicy pointPolicy;
    private PointUpdateRequestDto pointUpdateRequestDto;
    private RandomDance randomDance;
    private List<PointHistory> pointHistories;

    @Test
    @BeforeEach
    public void init() {
        makePointPolicy();
        makeUser();
        makePointHistory();
        makePointUpdateReqeustDto();
        makeRandomDance();
    }

    @Test
    @DisplayName("포인트 적립 테스트")
    public void pointUpdateServiceTest() {
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
        when(pointPolicyRepository.findOne(any())).thenReturn(Optional.ofNullable(pointPolicy));

        pointHistoryService.update(pointUpdateRequestDto);

        assertThat(user.getPoint()).isEqualTo(FIRST_PRIZE.getPoint() * 2);
    }

    @Test
    @DisplayName("포인트 적립 내역 조회 테스트")
    public void readAllPointHistoryServiceTest() {
        makePointHistories();
        when(pointHistoryRepository.findAll()).thenReturn(pointHistories);

        List<PointHistoryFindResponseDto> pointHistoryFindResponseDtos
                = pointHistoryService.readAll();

        assertThat(pointHistories.size()).isEqualTo(pointHistoryFindResponseDtos.size());
    }

    private void makePointPolicy() {
        pointPolicy = PointPolicy.builder()
                .pointType(FIRST_PRIZE)
                .point(FIRST_PRIZE.getPoint())
                .build();
    }

    private void makeUser() {
        user = User.builder()
                .id("user")
                .password("pass")
                .point(0)
                .build();
    }

    private void makePointHistory() {
        pointHistory = PointHistory.builder().build();
    }

    private void makePointUpdateReqeustDto() {
        pointUpdateRequestDto = PointUpdateRequestDto.builder()
                .pointPolicyId(1L)
                .count(2)
                .randomDanceId(1L)
                .id("user")
                .build();
    }

    private void makeRandomDance() {
        randomDance = RandomDance.builder()
                .id(1L)
                .title("title")
                .content("content")
                .danceType(DanceType.RANKING)
                .build();
    }

    private List<PointHistory> makePointHistories() {
        pointHistories = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PointHistory tmp = PointHistory.builder()
                    .user(user)
                    .randomDance(randomDance)
                    .pointPolicy(pointPolicy)
                    .count(0)
                    .build();
            pointHistories.add(tmp);
        }
        return pointHistories;
    }
}