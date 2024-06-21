package org.hsh.games.aoe.threads;

import org.hsh.games.aoe.ResourceAmount;
import org.hsh.games.aoe.entities.Consumer;

import java.util.List;

public class ResourceConsumptionThread<T extends Consumer> extends Thread {
    private Runnable onThreadInterruptedListener;
    private final T consumer;
    private final List<ResourceAmount> playerResources;

    public ResourceConsumptionThread(T consumer, List<ResourceAmount> playerResources) {
        this.consumer = consumer;
        this.playerResources = playerResources;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(consumer.getTimeBetweenConsumptions());
                List<ResourceAmount> consumptionTypes = consumer.getConsumptionType();
                synchronized (playerResources) {
                    for (ResourceAmount consumptionType : consumptionTypes) {
                        for (ResourceAmount resource : playerResources) {
                            if (resource.getResource() == consumptionType.getResource()) {
                                int resourceLeft = resource.getAmount() - consumptionType.getAmount();
                                if (resourceLeft < 0) {
                                    throw new IllegalStateException("Recursos Insuficientes!");
                                } else {
                                    resource.setAmount(resourceLeft);
                                    System.out.printf("Trabalhador consumiu %d de %s%n", consumptionType.getAmount(), consumptionType.getResource().getDescription());
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException | IllegalStateException e) {
                if (onThreadInterruptedListener != null) {
                    onThreadInterruptedListener.run();
                }
                interrupt();
            }
        }
    }

    public void setOnThreadInterruptedListener(Runnable listener) {
        this.onThreadInterruptedListener = listener;
    }
}
