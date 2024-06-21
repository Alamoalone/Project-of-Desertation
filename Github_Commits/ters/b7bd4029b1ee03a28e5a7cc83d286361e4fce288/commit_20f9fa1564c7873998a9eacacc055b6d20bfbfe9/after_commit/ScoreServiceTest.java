package services;

import cards.Card;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import players.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cards.CardType.*;
import static org.junit.jupiter.api.Assertions.*;

public class ScoreServiceTest {
  private static ScoreService scoreService;

  @BeforeAll
  public static void init() {
    scoreService = ScoreService.create();
  }

  @Test
  public void testCountScoreOfEmptyCardsIsZero() {
    assertEquals(0d, scoreService.countScore(List.of()));
  }

  @ParameterizedTest
  @CsvSource({
      "1-3-4-5,1.34",
      "3-2-1-12,2.02",
      "1-1-1-1,4",
      "1-1-3-4,2.34",
      "3-7-2-7,.68",
      "4-5-6-7,0",
      "12-7-4-5,.34",
      "1-4-5-6,1",
      "3-2-2-7,1.02",
      "3-2-1-4,1.6800000000000002"})
  public void testCountScore(String cardNumbers, String expectedScore) {
    var cards = Arrays.stream((cardNumbers == null ? "" : cardNumbers).split("-"))
        .map(Integer::parseInt)
        .map(cardNum -> Card.of(cardNum, OROS))
        .toList();

    var score = scoreService.countScore(cards);

    assertEquals(score, Double.parseDouble(expectedScore));
  }

  @Test
  public void testMergeTwoEmptyScores() {
    Executable exec = () -> scoreService.merge(Map.of(), Map.of());
    assertThrows(NullPointerException.class, exec);
  }

  @Test
  public void testMergeOneOfTheScoresContainsMissingTeam() {
    Executable exec = () -> scoreService.merge(Map.of(Team.A, 3), Map.of());
    assertThrows(NullPointerException.class, exec);
  }
}
