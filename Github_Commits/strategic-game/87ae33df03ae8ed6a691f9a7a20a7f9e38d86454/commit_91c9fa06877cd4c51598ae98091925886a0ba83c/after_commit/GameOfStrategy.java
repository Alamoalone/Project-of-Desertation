package org.hsh.games.aoe;

import org.hsh.games.aoe.entities.BuildingAndResourceAvailabilityPerLevel;
import org.hsh.games.aoe.entities.ConstructionType;
import org.hsh.games.aoe.entities.Player;
import org.hsh.games.aoe.entities.ResourceType;
import java.util.*;

public class GameOfStrategy {
    private static final Scanner scanner = new Scanner(System.in);

    public void start() {
        PlayerService playerService = createPlayer();
        ConsoleUtils.clearConsole();
        startGame(playerService);
    }

    private PlayerService createPlayer() {
        System.out.println("Escolhe um nome para a tua aldeia: ");

        String farmName = scanner.nextLine();
        return new PlayerService(new Player(farmName));
    }

    private void startGame(PlayerService playerService) {
        while (!playerService.getWorkers().isEmpty()) {
            try {
                Thread.sleep(ApplicationConstants.TIME_TO_SHOW_QUICK_MESSAGE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ConsoleUtils.clearConsole();
            showMenu(playerService);
            int choice = scanner.nextInt();
            scanner.nextLine();

            ConsoleUtils.clearConsole();
            playerService.showResourcesHeader();

            switch (choice) {
                case 1:
                    displayResourcesAvailableToSearchFor(playerService);
                    break;
                case 2:
                    displayBuildingTypes(playerService);
                    break;
                case 0:
                    System.out.println("Obrigado por jogar!");
                    System.exit(0);
                default:
                    System.out.println(ApplicationConstants.MESSAGE_WRONG_OPTION_TRY_AGAIN);
            }
        }
        System.out.println("Game Over! It seems theres no one left!");
    }

    private void showMenu(PlayerService playerService) {
        playerService.showResourcesHeader();
        System.out.println("Menu:");
        System.out.println("1. Procurar por recursos");
        System.out.println("2. Construir/Atualizar edifícios");
        System.out.println("3. Ver status");
        System.out.println("0. Sair do jogo");
        System.out.print(ApplicationConstants.MESSAGE_CHOOSE_OPTION);
    }

    private void displayResourcesAvailableToSearchFor(PlayerService playerService) {
        System.out.println("Lista de Recursos:");

        int index = 1;
        List<ResourceType> availableResources = BuildingAndResourceAvailabilityPerLevel.getByLevel(playerService.getPlayer().getEraAge().getLevel()).getAvailableResources();
        for(ResourceType ct : availableResources) {
            System.out.printf("%d. %s\n", index, ct.getDescription());
            index++;
        }

        int input = scanner.nextInt();
        if(input < 1 || input > availableResources.size()) {
            System.out.println(ApplicationConstants.MESSAGE_WRONG_OPTION_TRY_AGAIN);
            return;
        }
        if(playerService.getWorkerAvailable() != null) {
            playerService.sendWorkersToSearchJob(availableResources.get(input-1));
        }
    }

    private void displayBuildingTypes(PlayerService playerService) {
        System.out.println("Lista de Edificios:");
        List<String> constructionTypes = new ArrayList<>(playerService.getAvailableConstructionTypes());
        for (int i = 0; i < constructionTypes.size(); i++) {
            System.out.println((i + 1) + ". " + constructionTypes.get(i));
        }

        int typeOption = scanner.nextInt();
        scanner.nextLine();

        ConsoleUtils.clearConsole();
        playerService.showResourcesHeader();

        if (typeOption > 0 && typeOption <= constructionTypes.size()) {
            displayBuildingsOfType(playerService, constructionTypes.get(typeOption - 1));
        } else {
            System.out.println(ApplicationConstants.MESSAGE_WRONG_OPTION_TRY_AGAIN);
        }
        playerService.checkForNewEraConditions();
    }

    private void displayBuildingsOfType(PlayerService playerService, String selectedType) {
        List<Building> buildings = playerService.getBuildingsFromConstructionName(selectedType);
        for (int i = 0; i < buildings.size(); i++) {
            System.out.printf("%d. ", i + 1);
            buildings.get(i).showDetails();
            if (!playerService.checkIfBuildingAmountHasReached(buildings.get(i))) {
                System.out.printf("%d Cria novo edificio: %s\n", i + 2, buildings.get(i).getConstructionTypeName());
            }
        }

        int option = getOptionFromUser();
        if (option > 0 && option <= buildings.size()) {
            processSelectedBuilding(playerService, buildings.get(option - 1));
        } else if (option == buildings.size() + 1) {
            processNewBuilding(playerService, new Building(false, Objects.requireNonNull(ConstructionType.getEnumFromConstant(selectedType))));
        } else {
            System.out.println(ApplicationConstants.MESSAGE_WRONG_OPTION_TRY_AGAIN);
        }
    }

    private int getOptionFromUser() {
        System.out.println(ApplicationConstants.MESSAGE_CHOOSE_OPTION);
        int option = scanner.nextInt();
        scanner.nextLine();
        return option;
    }

    private void processSelectedBuilding(PlayerService playerService, Building building) {
        if (playerService.checkIfPlayerHasEnoughResources(building)) {
            ConstructionProcess process = playerService.isFirstTimeBuilding(building) ? ConstructionProcess.CREATION : ConstructionProcess.UPDATE;
            if (playerService.getWorkerAvailable() != null) {
                playerService.sendWorkersToConstructionJob(process, building);
            }
        } else {
            System.out.println(ApplicationConstants.MESSAGE_NOT_ENOUGH_RESOURCES);
        }
    }

    private void processNewBuilding(PlayerService playerService, Building building) {
        if (playerService.checkIfPlayerHasEnoughResources(building)) {
            playerService.addNewBuilding(building);
            playerService.sendWorkersToConstructionJob(ConstructionProcess.CREATION, building);
        } else {
            System.out.println(ApplicationConstants.MESSAGE_NOT_ENOUGH_RESOURCES);
        }
    }
}
